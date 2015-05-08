package de.charite.compbio.bamsampler.model;

import htsjdk.samtools.ValidationStringency;

import java.io.File;

import picard.sam.BuildBamIndex;

public class BAMIndexer extends BuildBamIndex {
	
	public BAMIndexer(File input) {
		super.VALIDATION_STRINGENCY = ValidationStringency.LENIENT;
		this.INPUT = input.getAbsolutePath();
	}
	
	public void run() {
		doWork();
	}

}
