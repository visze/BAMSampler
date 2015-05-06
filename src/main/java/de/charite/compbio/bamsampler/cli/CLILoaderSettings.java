package de.charite.compbio.bamsampler.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class CLILoaderSettings {
	
	public static Map<String,String> SAMPLES;
	public static String OUTPUT;
	public static String ADDITION = "sampled";
	public static int THREADS = 1;
	
	
	
	public static void parseArgs(String[] args) throws ParseException, IOException {
		
		//options
		Option help = new Option("h","help",false,"Print this help message.");
		Option input = new Option("i","input",true,"TSV-List (header BAM and Sample) of BAM-files to choose.\n First row: path to BAM. \n Second row: Sample-name");
		Option output = new Option("o","output",true,"Output BAM-File");
		Option threads = new Option("t","threads",true,"Number of threads for sampling.");
		Option sampleAddition = new Option("a","sample-addition",true,"Adds the string after the sample in the read name. Read name will be like sample_addition_oldname");
		
		//required
		input.setRequired(true);
		output.setRequired(true);
		
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption(help);
		options.addOption(input);
		options.addOption(output);
		options.addOption(sampleAddition);
		options.addOption(threads);
		
		CommandLineParser parser = new GnuParser();
//		CommandLine cmd;
		try { 
			CommandLine cmd = parser.parse( options, args); 
			if(args.length == 0 || cmd.hasOption("h")) {
				throw new MissingOptionException("Please Insert an argument");
			}
			String bamsPath = cmd.getOptionValue("i");
			
			loadSamplesAndBams(bamsPath);
			
			OUTPUT = cmd.getOptionValue("o");
			
			if (cmd.hasOption("a"))
				ADDITION = (cmd.getOptionValue("a"));
			if (cmd.hasOption("t"))
				THREADS = (Integer.parseInt(cmd.getOptionValue("t")));
			
		} catch (MissingOptionException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "BAMSampler", options );
			System.exit(0);
		}
	}



	private static void loadSamplesAndBams(String bamsPath) throws IOException {
		File bamsFile = new File(bamsPath);
		BufferedReader br = new BufferedReader(new FileReader(bamsFile));
		CSVParser parser = new CSVParser(br, CSVFormat.TDF.withHeader());
		parser.getHeaderMap();
		SAMPLES = new HashMap<String,String>();
		for (CSVRecord csvRecord : parser) {
			SAMPLES.put(csvRecord.get("Sample"),csvRecord.get("BAM"));
		}
		parser.close();
	}

}
