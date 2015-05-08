package de.charite.compbio.bamsampler.model;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class BamSampler implements Runnable {
	
	private static Long RANDOM_SEED = 1L;
	
	private Map<String, String> samples;
	private String addition;
	private SAMFileHeader header;
	private File tmpFile;
	private double probability;
	private long total;
	private long kept;
	
	public BamSampler(Map<String, String> samples, SAMFileHeader header, String addition, String output, double probability) throws IOException {
		this.samples = samples;
		this.header = header;
		this.addition = addition;
		this.tmpFile = File.createTempFile(output+"_tmp", ".bam");
		this.tmpFile.deleteOnExit();
		this.probability = probability;
	}

	public void run() {
		final Random r = RANDOM_SEED == null ? new Random() : new Random(RANDOM_SEED);
		
		final SAMFileWriter out = new SAMFileWriterFactory().makeSAMOrBAMWriter(header, false, tmpFile);
		SamReaderFactory.setDefaultValidationStringency(ValidationStringency.LENIENT);
		SamReaderFactory factory = SamReaderFactory.make();

		for (Entry<String,String> entry : samples.entrySet()) {
			
			String nameAddition = entry.getKey() + "_" + addition;

			final SamReader in = factory.open(new File(entry.getValue()));
			
			List<SAMReadGroupRecord> rgs = new ArrayList<SAMReadGroupRecord>();
			SAMReadGroupRecord rg = new SAMReadGroupRecord(addition);
			rg.setSample("Sampled");
			rgs.add(rg);
			in.getFileHeader().setReadGroups(rgs);
			
//			//only works with BAMs not with SAMs
//			if (in.isBinary())
//				in.enableIndexMemoryMapping(false);
			
			final Map<String,Boolean> decisions = new HashMap<String,Boolean>();

			for (final SAMRecord rec : in) {
				if (rec.isSecondaryOrSupplementary()) 
					continue;
				++total;
				final String key = rec.getReadName();
				final Boolean previous = decisions.remove(key);
				final boolean keeper;
				if (previous == null) {
					keeper = r.nextDouble() <= probability;
					if (rec.getReadPairedFlag()) 
						decisions.put(key, keeper);
				} else {
					 keeper = previous;
				}
				if (keeper) {
					String readName = nameAddition + rec.getReadName();
					rec.setReadName(readName);
					rec.setAttribute("RG", addition);
					out.addAlignment(rec);
					++kept;
				}
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.printf("Sample %s processed\n", entry.getKey());
		}
		out.close();

	}
	
	public long getKept() {
		return kept;
	}
	
	public long getTotal() {
		return total;
	}
	
	public File getTmpFile() {
		return tmpFile;
	}

}
