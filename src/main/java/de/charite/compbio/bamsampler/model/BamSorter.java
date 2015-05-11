package de.charite.compbio.bamsampler.model;

import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;

import picard.sam.SortSam;

public class BamSorter extends SortSam {
	
	public BamSorter(File input, File output, SortOrder sortOrder) {
		SamReaderFactory.setDefaultValidationStringency(ValidationStringency.LENIENT);
		super.VALIDATION_STRINGENCY = ValidationStringency.LENIENT;
		this.INPUT = input;
		this.OUTPUT = output;
		this.SORT_ORDER = sortOrder;
	}
	
	public void run(){
		doWork();
	}

}
