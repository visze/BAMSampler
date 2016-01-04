package de.charite.compbio.bamsampler.model;

import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.util.List;

import picard.sam.MergeSamFiles;

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
