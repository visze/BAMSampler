package de.charite.compbio.bamsampler;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.BAMIndexMetaData;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.ParseException;

import de.charite.compbio.bamsampler.cli.CLILoaderSettings;
import de.charite.compbio.bamsampler.model.BAMIndexer;
import de.charite.compbio.bamsampler.model.BamFileMerger;
import de.charite.compbio.bamsampler.model.BamSampler;
import de.charite.compbio.bamsampler.model.BamSorter;

public class Main {

	public static void main(String[] args) throws ParseException, SQLException, IOException, InterruptedException {

		CLILoaderSettings.parseArgs(args);

		getReadCount(CLILoaderSettings.SAMPLES);

		File tmpFile = sampleReads(CLILoaderSettings.SAMPLES, CLILoaderSettings.ADDITION, CLILoaderSettings.OUTPUT,
				CLILoaderSettings.THREADS);
		File outputFile = new File(CLILoaderSettings.OUTPUT);

		sortBam(tmpFile, outputFile);

		tmpFile.delete();

		indexBam(outputFile);

		System.out.println("Finished!");

	}

	public static void indexBam(File input) {
		// File in = new File(output);
		// final SAMFileReader bam = new SAMFileReader(in);
		//
		// BuildBamIndex.createIndex(bam, new File(output+".bai"));
		BAMIndexer bamIndexer = new BAMIndexer(input);
		bamIndexer.run();

	}

	public static void sortBam(File tmpFile, File output) {
		System.out.println("Sort BAM!");
		BamSorter bamSorter = new BamSorter(tmpFile, output, SAMFileHeader.SortOrder.coordinate);
		bamSorter.run();
		System.out.println("BAM sorted!");

	}

	public static File sampleReads(Map<String, String> samples, String addition, String output, int threads)
			throws IOException, InterruptedException {

		// create Threads
		double threadSize = (double) samples.size() / (double) threads;
		int count = 0;
		double probability = 1.0 / (double) samples.size();
		SAMFileHeader header = createHeader(samples, addition);

		List<BamSampler> sampler = new ArrayList<BamSampler>();
		List<String> sampleSet = new ArrayList<String>();
		sampleSet.addAll(samples.keySet());

		int num = 1;
		while (count < samples.size()) {
			int start = count;
			int end = count + (int) Math.round(threadSize) - 1;
			if (end < start)
				end = start;
			if (end >= samples.size())
				end = samples.size() - 1;

			Map<String, String> samplerSamples = new HashMap<String, String>();
			for (int i = start; i <= end; i++) {
				samplerSamples.put(sampleSet.get(i), samples.get(sampleSet.get(i)));
			}
			System.out.format("Create thread %d with %d samples\n", num, samplerSamples.size());
			sampler.add(new BamSampler(samplerSamples, header, addition, output, probability));

			count = end + 1;
			num += 1;
		}

		// run threads
		ExecutorService executorService = Executors.newFixedThreadPool(threads);
		for (BamSampler thread : sampler) {
			executorService.execute(thread);
		}
		executorService.shutdown();
		executorService.awaitTermination(3, TimeUnit.DAYS);

		List<File> files = new ArrayList<File>();
		int kept = 0;
		int total = 0;
		for (BamSampler thread : sampler) {
			kept += thread.getKept();
			total += thread.getTotal();
			files.add(thread.getTmpFile());
		}

		System.out.println("Kept " + kept + " out of " + total + " reads.");

		// merge files of threads
		if (files.size() == 1)
			return files.get(0);
		File outputTmpFile = File.createTempFile(output + "_tmp", ".bam");
		BamFileMerger bamFileMerger = new BamFileMerger(files, outputTmpFile);
		bamFileMerger.run();

		return outputTmpFile;

	}

	private static SAMFileHeader createHeader(Map<String, String> samples, String id) throws IOException {
		SAMFileHeader header = null;
		SamReaderFactory factory = SamReaderFactory.make();
		factory.validationStringency(ValidationStringency.LENIENT);
		for (Entry<String, String> entry : samples.entrySet()) {
			final SamReader in = factory.open(new File(entry.getValue()));
			// stringency SILENT to omit failures in mark duplicate reads
			if (header == null) {
				header = in.getFileHeader();
				List<SAMReadGroupRecord> rgs = new ArrayList<SAMReadGroupRecord>();
				SAMReadGroupRecord rg = new SAMReadGroupRecord(id);
				rg.setSample("Sampled");
				rgs.add(rg);
				header.setReadGroups(rgs);

			} else {
				SAMSequenceDictionary dict = header.getSequenceDictionary();
				for (SAMSequenceRecord sequenceRecord : in.getFileHeader().getSequenceDictionary().getSequences()) {
					if (dict.getSequence(sequenceRecord.getSequenceName()) == null)
						dict.addSequence(sequenceRecord);
				}
				header.setSequenceDictionary(dict);
			}
			in.close();
		}
		return header;
	}

	public static Map<String, Integer> getReadCount(Map<String, String> samples) throws IOException {

		SamReaderFactory factory = SamReaderFactory.make();
		factory.validationStringency(ValidationStringency.LENIENT);

		Map<String, Integer> output = new HashMap<String, Integer>();
		for (Entry<String, String> entry : samples.entrySet()) {
			output.put(entry.getKey(), 0);

			SamReader in = factory.open(new File(entry.getValue()));

			// use index if binary
			if (in.hasIndex()) {

				AbstractBAMFileIndex index = (AbstractBAMFileIndex) in.indexing().getIndex();

				for (int i = 0; i < index.getNumberOfReferences(); i++) {
					BAMIndexMetaData meta = index.getMetaData(i);
					output.put(entry.getKey(), output.get(entry.getKey()) + meta.getAlignedRecordCount());
				}
			} else {
				int total = 0;
				for (final SAMRecord rec : in) {
					if (rec.isSecondaryOrSupplementary())
						continue;
					++total;
				}
				output.put(entry.getKey(), total);
			}
			in.close();
			System.out.format("Number of reads of sample %s: %d\n", entry.getKey(), output.get(entry.getKey()));
		}
		return output;
	}

}
