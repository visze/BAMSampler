package de.charite.compbio.bamsampler.model;

import java.io.File;
import java.util.List;

import net.sf.picard.sam.MergeSamFiles;
import net.sf.samtools.SAMFileReader.ValidationStringency;

public class BamFileMerger extends MergeSamFiles{
	public BamFileMerger(List<File> input, File output) {
		super.VALIDATION_STRINGENCY = ValidationStringency.LENIENT;
		this.INPUT = input;
		this.OUTPUT = output;
	}
	
	public void run() {
		doWork();
	}
}
