package de.charite.compbio.bamsampler.model;

import java.io.File;

import net.sf.picard.sam.BuildBamIndex;

public class BAMIndexer extends BuildBamIndex {
	
	public BAMIndexer(File input) {
		this.INPUT = input.getAbsolutePath();
	}
	
	public void run() {
		doWork();
	}

}
