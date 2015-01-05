[![Build Status](https://travis-ci.org/visze/BAMSampler.svg?branch=master)](https://travis-ci.org/visze/BAMSampler)

# BAMSampler

This tool uses several BAM files to generated a new sampled BAM. It goes over each reads and choose a read by a probability of 1 divided by the number of BAMS to sample. The output is a new sorted and indexed BAM.

The sampling is similar to picardtools BAMSampler but with merging, sorting and indexing. For multiple threads several tmp BAMS are generated (each for each thread) and are merged after sampling.

## Installation

1. Download the master branch from git.
```bash
git co https://github.com/visze/BAMSampler.git BAMSampler
```
2. Install files with maven
```bash
cd BAMSampler
mvn package
```

## Run BAMSampler
```bash
java -jar target/bamsampler-<version>.jar
```

The command-line help will be:

<pre>
-o,--output <arg>             Output BAM-File
-a,--sample-addition <arg>    Adds the string after the sample in the read
                              name. Read name will be like sample_addition_oldname
-i,--input <arg>              TSV-List (header BAM and Sample) of
                              BAM-files to choose.
                              First row: path to BAM.
                              Second row: Sample-name
-h,--help                     Print this help message.
-t,--threads <arg>            Number of threads for sampling
</pre>

Example command can be:
```bash
cd target
java -jar target/bamsampler-<version>.jar --output /tmp/NC_000919.1_BAMsamples.bam --sample-addition testBAM --input resources/NC_000919.1_BAMsamples.tsv --threads 4
```

or

```bash
cd target
java -jar bamsampler-<version>.jar --output /tmp/NC_000919.1_SAMsamples.bam --sample-addition testSAM --input resources/NC_000919.1_BAMsamples.tsv --threads 4
```
