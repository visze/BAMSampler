package de.charite.compbio.bamsampler.model;

import java.io.File;
import java.util.List;

import net.sf.picard.sam.MergeSamFiles;

public class BamFileMerger extends MergeSamFiles{
	public BamFileMerger(List<File> input, File output) {
		this.INPUT = input;
		this.OUTPUT = output;
	}
	
	public void run() {
		doWork();
	}
}
