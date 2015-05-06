package de.charite.compbio.bamsampler.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMFileReader.ValidationStringency;

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
		
		for (Entry<String,String> entry : samples.entrySet()) {
			
			String nameAddition = entry.getKey() + "_" + addition;
			final SAMFileReader in = new SAMFileReader(new File(entry.getValue()));
			
			//stringency SILENT to omit failures in mark duplicate reads
			in.setValidationStringency(ValidationStringency.SILENT);
			
			List<SAMReadGroupRecord> rgs = new ArrayList<SAMReadGroupRecord>();
			SAMReadGroupRecord rg = new SAMReadGroupRecord(addition);
			rg.setSample("Sampled");
			rgs.add(rg);
			in.getFileHeader().setReadGroups(rgs);
			
			//only works with BAMs not with SAMs
			if (in.isBinary())
				in.enableIndexMemoryMapping(false);
			
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
			in.close();
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
