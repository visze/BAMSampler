package de.charite.compbio.bamsampler.model;

import java.io.File;

import net.sf.picard.sam.BuildBamIndex;
import net.sf.samtools.SAMFileReader.ValidationStringency;

public class BAMIndexer extends BuildBamIndex {
	
	public BAMIndexer(File input) {
		super.VALIDATION_STRINGENCY = ValidationStringency.SILENT;
		this.INPUT = input.getAbsolutePath();
	}
	
	public void run() {
		doWork();
	}

}
