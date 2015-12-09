# Project Name

2-3 sentences description

## HOWTO Install

## HOWTO Run, Basics

* Input
* Output
* Expected Results
* ...

## Method Description

* Authors
* Publications
* Description (ML ? )
* Training / Test Data
* ...

## Evaluation

Perhaps:

* Performance measures used (F1 ?, Accuracy ?, ROC Curve ?, ...)
* Comparison with other tools
* ...

## (OPTIONAL) HOWTO Run, Extended

This is optional. Only needed if the tool has many configuration options. If the conf options are also already well described in a man page, this fact can just be cited on the `HOWTO Run, Basics`




METASTUDENT - predictor of gene ontology terms from protein sequence
====================================================================

Metastudent predicts Gene Ontology (GO) terms from the Molecular
Function Ontology (MFO) and Biological Process Ontology (BPO) for input
protein sequences by homology-based inference from already annotated
proteins.
      
DEVELOPMENT YEAR
================
2012

AUTHOR
======
Tobias Hamp <hampt@rostlab.org>

How to install the package
=============================================
1) git clone https://github.com/Rostlab/MetaStudent.git
2) cd MetaStudent
3) sudo apt-get install pp-popularity-contest ( required during execution of the program )
4) python setup.py install
       
How to generate the distributable tar archive
=============================================
$ setup.py sdist

Running the Program
===================
metastudent -i FASTA_FILE -o RESULT_FILE_PREFIX [--debug] [--keep-temp]
[--silent] [--output-blast] [--blast-only] [--all-predictions]
[--ontologies=MFO or BPO or MFO,BPO]
[--blast-kickstart-databases=BLAST_RESULT_FILE(S)] [--temp-dir=DIR]
[--config=CONFIG_FILE] !!! Make sure your fasta file contains at most
500 sequences !!!

Large (1 GB in total) data files necessary for the operation of
metastudent are downloaded automatically on the first use of the
program.  The download is restartable.  You can also make an explicit
call to metastudentdata (by default /usr/bin/metastudentdata) to
download the data files.  In case the data directory (by default
/usr/share/metastudent-data) is not writable and you are not root, the
operation is reattempted with sudo.

OPTIONS

       -i FASTA_FILE
           The input fasta file. Please try to remove any special formattings
           (e.g. whitespaces) in the sequences before using them as input. Due
           to high memory usage, make sure your fasta file contains at most
           500 sequences.

       -o RESULT_FILE_PREFIX
           The file name prefix of the output files. GO terms are organized in
           ontologies. Metatstudent treats each ontology differently and
           outputs one result file for each. For example, if
           <RESULT_FILE>=./myresult and MFO (Molecular Function Ontology) and
           BPO (Biological Process Ontology) ontologies are selected (see
           option --ontologies), then metastudent creates two output files:
           ./myresult.MFO.txt and ./myresult.BPO.txt.

       --debug
           Print extra debugging messages.

       --keep-temp
           Whether to keep the temp directories after metastudent has finished
           (they can be useful when errors occur or in combination with
           --blast-kickstart-databases).

       --silent
           No progress messages (stdout), only errors (stderr).

       --output-blast
           Whether to output the result of the BLAST runs. Useful in
           combination with --blast-kickstart-databases. Output file name
           format is RESULT_FILE_PREFIX.<BLAST_OPTIONS>.blast.

       --blast-only
           Whether to only output the result of the BLAST runs, and nothing
           else. See options --output-blast and --blast-kickstart-databases.

       --all-predictions
           Whether to output the prediction results of the individual
           predictors. File name format of the output file is
           <RESULT_FILE_PREFIX>.<ONTOLOGY>.<METHOD>.txt.

       --ontologies=MFO or BPO or MFO,BPO
           A comma separated list of ontologies to create predictions for.
           Default is MFO,BPO. If used in combination with
           --blast-kickstart-databases, the number and order of the ontologies
           must correspond to the kickstart files.

       --blast-kickstart-databases=<BLAST_RESULT_FILES>
           Since running BLAST is usually the part that takes the longest in
           metastudent, this option allows you to re-use the output of a
           previous run. This is useful to test, for example, different
           parameters or when you have lost a prediction. The number of
           kickstart files must correspond to the number of ontologies (see
           option --ontologies). Separate the file paths with commas. For
           example:
           --blast-kickstart-databases=<RESULT_FILE_MFO>,<RESULT_FILE_BPO>
           (kickstart for both ontologies) or
           --blast-kickstart-databases=,<RESULT_FILE_BPO> (only kickstart BPO;
           note the comma).

       --temp-dir=DIR
           The parent temp directory to use instead of the one specified with
           tmpDir in the metastudent configuration file.

       --config=FILE
           The path to a custom metastudent configuration file; overrides all
           settings of the configuration files found in the FILES section of
           this man page.

FILES

       <package_data_dir>/metastudentrc.default
           The metastudent configuration file.

       <sysconfdir>/metastudentrc
           The metastudent configuration file, overrides
           <package_data_dir>/metastudentrc.default.

       <homedir>/.metastudentrc
           The metastudent configuration file, overrides
           <sysconfdir>/metastudentrc.

EXAMPLES

       The example test.fasta file can be found in <package_doc_dir>/examples
       (usually /usr/share/doc/metastudent/examples).

       Predict the GO terms for the sequences in test.fasta for both the MFO
       and the BPO ontology:
            metastudent -i test.fasta -o test.result

       Create the BLAST output to predict the MFO terms for sequences in
       test.fasta (not the actual predictions, yet; see next example).
            metastudent -i test.fasta -o test.result --blast-only --output-blast --ontologies=MFO

       Predict the MFO and BPO terms for sequences in test.fasta with a
       precomputed MFO BLAST output (see previous example; note the comma at
       the end).
            metastudent -i test.fasta -o test.result --ontologies=MFO,BPO --blast-kickstart-databases=test.result_eval0.001_iters3_srcexp.mfo.blast,


OUTPUT FORMAT
=============
For each selected ontology (see --ontologies), one output file is
produced (see -o).  Each line in each file associates a protein with a
GO term and a reliability for the association (0.0 to 1.0). The
following format is used: <PROTEIN ID><TAB><GO_TERM><TAB><RELIABILITY>
       

REFERENCES
==========
Hamp, T., Kassner, R., Seemayer, S., Vicedo, E., Schaefer, C., Achten,
D., ... & Rost, B. (2013). Homology-based inference sets the bar high
for protein function prediction. BMC Bioinformatics, 14(Suppl 3), S7.

MANPAGE
=======       
http://manpages.ubuntu.com/manpages/saucy/man1/metastudent.1.html#contenttoc8
       
DOCUMENTATION
============= 
https://rostlab.org/owiki/index.php/Metastudent
 



