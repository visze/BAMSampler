package de.charite.compbio.bamsampler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import static org.junit.Assert.*;
import de.charite.compbio.bamsampler.cli.CLILoaderSettings;

public class BAMSamplerTest {

	// BAM TEST
	String samplesBAM = "NC_000919.1_BAMsamples.tsv";
	String outputBAMFolder = "test_output";
	String outputBAMName = "NC_000919.1_BAM_sampled.bam";
	String additionBAM = "testbam";

	// test SAM
	String samplesSAM = "NC_000919.1_SAMsamples.tsv";
	String outputSAMFolder = "test_output";
	String outputSAMName = "NC_000919.1_SAM_sampled.bam";
	String additionSAM = "testsam";
	
	@Test
	public void testCLI() {
		URL samplesFilePath = this.getClass().getResource(samplesBAM);
		try {
			CLILoaderSettings.parseArgs(new String[]{"-o", "bar", "-i", samplesFilePath.getPath(), "-a", "fuu", "-t", "30"});
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Could not parse this");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load BAM-Files");
		}
		assertTrue(CLILoaderSettings.SAMPLES.containsKey("Sample1"));
		assertTrue(CLILoaderSettings.SAMPLES.containsKey("Sample2"));
		assertTrue(CLILoaderSettings.SAMPLES.containsKey("Sample3"));
		assertTrue(CLILoaderSettings.SAMPLES.containsKey("Sample4"));
		assertEquals(CLILoaderSettings.SAMPLES.get("Sample1"), "mapping_files/NC_000919.1_1.fasta.bam");
		assertEquals(CLILoaderSettings.SAMPLES.get("Sample2"), "mapping_files/NC_000919.1_2.fasta.bam");
		assertEquals(CLILoaderSettings.SAMPLES.get("Sample3"), "mapping_files/NC_000919.1_3.fasta.bam");
		assertEquals(CLILoaderSettings.SAMPLES.get("Sample4"), "mapping_files/NC_000919.1_4.fasta.bam");
		assertEquals(CLILoaderSettings.ADDITION, "fuu");
		assertTrue(CLILoaderSettings.THREADS == 30);
		assertEquals(CLILoaderSettings.OUTPUT, "bar");
	}

	@Test
	public void testBamSamplerReadCountBAM() {
		try {
			loadCLI(samplesBAM, outputBAMFolder + "/" + outputBAMName, additionBAM);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load BAM-Files");
		}
		Map<String, Integer> counts = Main.getReadCount(CLILoaderSettings.SAMPLES);

		assertTrue(counts.containsKey("Sample1"));
		assertTrue(counts.containsKey("Sample2"));
		assertTrue(counts.containsKey("Sample3"));
		assertTrue(counts.containsKey("Sample4"));
		assertTrue(40000 == counts.get("Sample1"));
		assertTrue(40000 == counts.get("Sample2"));
		assertTrue(40000 == counts.get("Sample3"));
		assertTrue(40000 == counts.get("Sample4"));
	}
	
	@Test
	public void testBamSamplerReadCountSAM() {
		try {
			loadCLI(samplesSAM, outputSAMFolder + "/" + outputSAMName, additionSAM);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load BAM-Files");
		}
		Map<String, Integer> counts = Main.getReadCount(CLILoaderSettings.SAMPLES);

		assertTrue(counts.containsKey("Sample1"));
		assertTrue(counts.containsKey("Sample2"));
		assertTrue(counts.containsKey("Sample3"));
		assertTrue(counts.containsKey("Sample4"));
		assertTrue(40000 == counts.get("Sample1"));
		assertTrue(40000 == counts.get("Sample2"));
		assertTrue(40000 == counts.get("Sample3"));
		assertTrue(40000 == counts.get("Sample4"));
	}

	@Test
	public void testBamSamplerBAM() {
		try {
			loadCLI(samplesBAM, outputBAMFolder + "/" + outputBAMName, additionBAM);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load BAM-Files");
		}

		File tmpFile;
		try {
			tmpFile = Main.sampleReads(CLILoaderSettings.SAMPLES, CLILoaderSettings.ADDITION, CLILoaderSettings.OUTPUT, CLILoaderSettings.THREADS);
			
			File outputFile = new File(CLILoaderSettings.OUTPUT);
			Main.sortBam(tmpFile, outputFile);
			tmpFile.delete();
			
			Main.indexBam(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load BAM-Files");
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Threading fails. Takes to long!");
		}
		
		assertTrue(true);
	}
	
	@Test
	public void testBamSamplerBAMThread() {
		try {
			loadCLI(samplesBAM, outputBAMFolder + "/" + outputBAMName, additionBAM);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load BAM-Files");
		}

		File tmpFile;
		try {
			tmpFile = Main.sampleReads(CLILoaderSettings.SAMPLES, CLILoaderSettings.ADDITION, CLILoaderSettings.OUTPUT, 4);
			
			File outputFile = new File(CLILoaderSettings.OUTPUT);
			Main.sortBam(tmpFile, outputFile);
			tmpFile.delete();
			
			Main.indexBam(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load BAM-Files");
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Threading fails. Takes to long!");
		}
		
		assertTrue(true);
	}
	
	@Test
	public void testBamSamplerSAM() {
		try {
			loadCLI(samplesSAM, outputSAMFolder + "/" + outputSAMName, additionSAM);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load SAM-Files");
		}

		File tmpFile;
		try {
			tmpFile = Main.sampleReads(CLILoaderSettings.SAMPLES, CLILoaderSettings.ADDITION, CLILoaderSettings.OUTPUT, CLILoaderSettings.THREADS);
			
			File outputFile = new File(CLILoaderSettings.OUTPUT);
			Main.sortBam(tmpFile, outputFile);
			tmpFile.delete();
			
			Main.indexBam(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not load SAM-Files");
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Threading fails. Takes to long!");
		}
		
		assertTrue(true);
	}

	private void loadCLI(String sampleFile, String outputFolder, String addition) throws IOException {

		URL samplesFilePath = this.getClass().getResource(sampleFile);

		CLILoaderSettings.ADDITION = addition;
		String outputBAMPATH = samplesFilePath.getPath().replace(sampleFile, outputFolder);

		CLILoaderSettings.OUTPUT = outputBAMPATH;
		Map<String, String> samples = loadSamplesAndBams(samplesFilePath.getPath().toString(), samplesFilePath.getPath().replace(sampleFile, ""));

		CLILoaderSettings.SAMPLES = samples;
	}

	private Map<String, String> loadSamplesAndBams(String bamsPath, String outputPath) throws IOException {
		File bamsFile = new File(bamsPath);
		BufferedReader br = new BufferedReader(new FileReader(bamsFile));
		CSVParser parser = new CSVParser(br, CSVFormat.TDF.withHeader());
		parser.getHeaderMap();
		Map<String, String> output = new HashMap<String, String>();
		for (CSVRecord csvRecord : parser) {
			output.put(csvRecord.get("Sample"), outputPath + "/" + csvRecord.get("BAM"));
		}
		parser.close();
		return output;
	}

}
