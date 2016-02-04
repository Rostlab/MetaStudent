# METASTUDENT 

Metastudent predicts Gene Ontology (GO) terms from the Molecular
Function Ontology (MFO) and Biological Process Ontology (BPO) for input
protein sequences by homology-based inference from already annotated
proteins.

**Development Year:**  2012

**Authors:** Tobias Hamp <hampt@rostlab.org>

**Publications:** Hamp, T., Kassner, R., Seemayer, S., Vicedo, E., Schaefer, C., Achten, D., ... & Rost, B. (2013),
"Homology-based inference sets the bar high for protein function prediction", BMC Bioinformatics, 14(Suppl 3), S7.

**Manpage:** http://manpages.ubuntu.com/manpages/saucy/man1/metastudent.1.html#contenttoc8

**Documentation:** https://rostlab.org/owiki/index.php/Metastudent

**Elixir:** https://bio.tools/tool/tum.de/MetaStudent/1

## Installation
Metastudent can easily be installed using apt-get command on any debian based system

```shell
# Not Required for ubuntu
sudo apt-get install python-software-properties
sudo apt-add-repository "deb http://rostlab.org/debian/ stable main contrib non-free"
sudo apt-get update # ignore GPG error
sudo apt-get install rostlab-debian-keyring # without verification
sudo apt-get update

# Needed to be done for all debian based machines
sudo apt-get install metastudent
```

All the Metastudent related packages will also be installed. Type 'Y' on the console query to accept installation of all packages.

Metastudent-Data is required for the execution of the program.
Metastudent data will also be installed while installing metastudent package. If the data is not there, the data can be found in the data folder of the github repository. This data folder contains the newest version of data ( version: 201401)

If not set by default, add the metastudent-data path to the DATABASE_BASE_PATH variable accordingly in the config file.

Make sure that the *blastpgp* program is available. If not downloaded automatically ( check /usr/bin/blastpgp program's existance), you can download *blastpgp* from package *blast-2.2.26* for the corresponding platform from the following FTP: 
```
ftp://ftp.ncbi.nlm.nih.gov/blast/executables/release/LATEST/
```
For Linux x64 please follow the following commands. With these commands *blastpgp* binaries are installed and added tho the PATH environment variable:
```
wget ftp://ftp.ncbi.nlm.nih.gov/blast/executables/release/LATEST/blast-2.2.26-x64-linux.tar.gz
tar xf blast-2.2.26-x64-linux.tar.gz
echo -e "[NCBI]\nData=$(pwd)/blast-2.2.26/data/" > ~/.ncbirc
export PATH=$PATH:$(pwd)/blast-2.2.26/bin
```

## Configuration
Metastudent can be configured with a configuration file. The default Metastudent configuration are present in:

```shell
<package_data_dir>/metastudentrc.default # usually under /usr/share/metastudent
```

If the user wants to use its own configuration file, then --config flag can be used

## Running Metastudent
Metastudent can be run by the following command after installation and configuration:
```
metastudent -i FASTA_FILE -o RESULT_FILE_PREFIX [--debug] [--keep-temp]
[--silent] [--output-blast] [--blast-only] [--all-predictions]
[--ontologies=MFO or BPO or MFO,BPO]
[--blast-kickstart-databases=BLAST_RESULT_FILE(S)] [--temp-dir=DIR]
[--config=CONFIG_FILE] 
```
*Please make sure your fasta file contains at most 500 sequences.*

### OPTIONS

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

### FILES

       <package_data_dir>/metastudentrc.default
           The metastudent configuration file.

       <sysconfdir>/metastudentrc
           The metastudent configuration file, overrides
           <package_data_dir>/metastudentrc.default.

       <homedir>/.metastudentrc
           The metastudent configuration file, overrides
           <sysconfdir>/metastudentrc.

### EXAMPLES

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
            metastudent -i test.fasta -o test.result --ontologies=MFO,BPO 
            --blast-kickstart-databases=test.result_eval0.001_iters3_srcexp.mfo.blast,

### OUTPUT FORMAT

       For each selected ontology (see --ontologies), one output file is produced (see -o). 
       Each line in each file associates a protein with a GO term and a reliability for
       the association (0.0 to 1.0). The following format is used: 
       <PROTEIN ID><TAB><GO_TERM><TAB><RELIABILITY>


## HOWTO generate the distributable tar archive
```
$ setup.py sdist
```
## Method Description

###GO (Gene Ontology) for CAFA

GO has three parts: Molecular Function Ontology (MFO), Biological Process Ontology (BPO) and Cellular Component Ontology (CCO). CAFA considered only the MFO and BPO. Both correspond to two directed acyclic graphs and capture different aspects of protein function. Functional keywords ("GO terms") are nodes and their relationships are labeled edges. The ontology is hierarchical: following the edges from a node, each new term corresponds to a more general concept of the original function. All paths converge at the root node, which can simply be interpreted as, e.g., has a molecular function.

The complete functional annotation of each protein has two subgraphs (MFO and BPO). If a subgraph does not contain all the terms that can be inferred by going from its nodes to the root, we perform a propagation. Given a set of GO terms, this operation refers to its extension with all ancestral terms. Ancestrors can be found by following all outgoing paths from the terms to the root: each visited node is an ancestor. If the GO terms have scores (e.g. to reflect their reliability), the latter are also propagated: each parent term is simply assigned the maximum score of its children. Sometimes, a propagated subgraph needs to be reduced again to the leaf terms. A leaf term is not the parent of any other term in the propagation and corresponds to the most exact description of a function for the given protein.

In order to integrate the operations above into our methods, we used the graph_path table provided by the GO consortium. It contains all possible paths in the entire GO graph, pre-calculated by specific path inference rules.

###Assessment of predicted GO annotations

Analogously to CAFA, we use fixed sets of target proteins to compare prediction methods. Each target corresponds to one or two propagated GO subgraphs of experimentally validated terms (depending on whether both BPO and MFO annotations are available or only one of the two). A method is supposed to predict these subgraphs and assign a reliability between 0.0 and 1.0 to each predicted term. Then we assess their accuracy in the following ways, separately for the MFO and BPO. For the first two measures, we exclusively used the original CAFA implementations, GO version, targets and target annotations. Only to implement our new leaf threshold measure, we slightly adapted the programs.

###Homology-based methods

To be UPDATED

* Description (ML ? )
* Training / Test Data
* ...
* 



## Evaluation

TODO

TO BE UPDATED

Perhaps:

* Performance measures used (F1 ?, Accuracy ?, ROC Curve ?, ...)
* Comparison with other tools
* ...
