package de.charite.compbio.bamsampler.model;

import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.ValidationStringency;

import java.io.File;

public class BamSorter extends picard.sam.SortSam {
	
	public BamSorter(File input, File output, SortOrder sortOrder) {
		super.VALIDATION_STRINGENCY = ValidationStringency.LENIENT;
		this.INPUT = input;
		this.OUTPUT = output;
		this.SORT_ORDER = sortOrder;
	}
	
	public void run(){
		doWork();
	}

}
