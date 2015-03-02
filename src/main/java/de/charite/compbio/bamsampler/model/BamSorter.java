package de.charite.compbio.bamsampler.model;

import java.io.File;

import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMFileReader.ValidationStringency;

public class BamSorter extends net.sf.picard.sam.SortSam {
	
	public BamSorter(File input, File output, SortOrder sortOrder) {
		super.VALIDATION_STRINGENCY = ValidationStringency.SILENT;
		this.INPUT = input;
		this.OUTPUT = output;
		this.SORT_ORDER = sortOrder;
	}
	
	public void run(){
		doWork();
	}

}
