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

####StudentA 
Begin with 2-iteration PSI-BLAST against all Swiss-Prot proteins with GO annotations (E-Value < 0.1). Extract GO terms of the six top PSI-BLAST hits (or all if fewer than 6 hits found). Each identified GO term is scored 1.0 if the term is found in all 6 hits and 0.5 otherwise. Once the term-score pairs have been extracted, only the leaf terms of their propagation are retained. Then apply the following filter to reduce functional redundancy: (i) create branches by propagating each predicted leaf term separately; (ii) calculate all pairwise branch overlaps, with the overlap being defined as the number of common GO terms in both branches divided by the average branch size.

Next, cluster all branches so that each pair from two different clusters overlaps less than 10%. For each cluster, the branch with the longest path to the root is chosen, reduced to its original leaf term with the original score and output to the user. As the redundancy reduction may filter out highly supported terms, we apply a final correction: if any pair of branches from previous steps overlaps over 90%, the term common to both and with the longest path to the root, i.e., the lowest common ancestor, is added to the result.

![alt tag](http://static-content.springer.com/image/art%3A10.1186%2F1471-2105-14-S3-S7/MediaObjects/12859_2013_5696_Fig2_HTML.jpg)  


####StudentB 
Begin with 2-iteration PSI-BLAST against all Swiss-Prot proteins with GO annotations (E-Value < 0.002 for 1st and E-Value < 0.01 for 2nd round). Each PSI-BLAST hit is associated with the propagation of its GO terms and each term in the propagation is associated with the PSI-BLAST E-Value of the hit. We then define two scores.

The template quality score gauges the reliability of the entire PSI-BLAST query with respect to the goal of assigning function. First, we calculate the raw template score as the average over the logarithms of all returned PSI-BLAST E-Values plus twice the standard deviation (also derived from the log(E-Value)). The standard deviation is added to correct for cases with relatively low top scores and relatively high averages. This raw template score is normalized into a value from 0 to 1 by mapping it into a percentile bin obtained from running PSI-BLAST in the same fashion on a sample of all Swiss-Prot proteins (e.g. a score obtained by 90% of the samples for all Swiss-Prot is scored 0.1 = 1-0.9). We call this percentile the template quality score.

The combined leaf score measures the reliability of each predicted leaf. First, we compile the propagated set of all GO terms for all PSI-BLAST hits. Each term can occur in multiple hits and thus be associated with multiple E-Values. The support of a term is defined as the sum over the logarithm of its E-Values divided by the sum of the logarithm over the E-Values of all hits. The combined leaf score of a leaf in the set of GO terms above is then given by the average support of itself and all of its ancestors.

Finally, we multiply template quality and combined leaf score for each leaf, combine all the leaf-score pairs in one set and output its propagation to the user.

![alt tag](http://static-content.springer.com/image/art%3A10.1186%2F1471-2105-14-S3-S7/MediaObjects/12859_2013_5696_Fig3_HTML.jpg) 

####StudentC

Begin with 2-iteration PSI-BLAST against all Swiss-Prot proteins with GO annotations (E-Value < 0.1). Count how often a particular GO term appeared in the PSI-BLAST hits (without propagation). All nodes with counts are propagated through the GO tree. Instead of taking the maximum count of all children at each parent node, however, their values are summed up and added to that of the parent node (normalization to [0,1] by division by maximal value). We call this type of scoring the max support. The PSI-BLAST scores, on the other hand, are considered as follows.

For each PSI-BLAST hit, we first read off the positive identity. This value is included in the default BLAST output and corresponds to the number of positives divided by the alignment length. (Each mutation column in the default BLAST output with a positive score by BLOSUM62 is a positive.) Then, we multiply the max support of each term with the highest associated positive identity (we may have many positive identities, because a GO term can be associated with multiple PSI-BLAST hits). The method outputs only the one branch corresponding to the highest scoring leaf term.

![alt tag]( http://static-content.springer.com/image/art%3A10.1186%2F1471-2105-14-S3-S7/MediaObjects/12859_2013_5696_Fig4_HTML.jpg) 


###Post-CAFA re-parameterization

After CAFA, we parameterized the above three basic homology-inference methods. For StudentA, we introduced the options to exclude predictions with a score of 0.5 and to choose the number of PSI-BLAST hits to consider (before: 6; now: 1, 5 or 9). For StudentC, we added alternative PSI-BLAST E-Value thresholds (before: 1e-01; now: 1e00, 1e-03 or 1e-06) and percentage pairwise sequence identity as an alternative to the positive identity. We also enabled the optional output of all branches, instead of restricting it to the most probable one. The original implementation of StudentB had a bug: an alternative graph_path table inverted the order of the columns by mistake. The results of this bug were submitted to CAFA. We fixed the bug and allowed for alternatives in the thresholds for E-Values and maximum numbers of PSI-BLAST hits (E-Value before: 1e-02; now: 1e00, 1e-03 or 1e-06; max. number of hits before: 250 [PSI-BLAST default]; now: 5, 50 or 500).

For all methods, we also add the choice of the number of PSI-BLAST iterations (before: 2 for all methods; now: 1, 2 or 3). Finally, we enabled the filtering out of Swiss-Prot annotations with unclear experimental support (optional restriction to the following experimental GO evidence codes: IDA, IMP, IPI, IGI, IEP, TAS, IC, EXP).

The re-parameterization created 36, 54, and 72 different parameter combinations for StudentA-C, respectively. We optimized the parameters by picking the combination leading to the highest Fmax (threshold measure; Eq. 1) on a hold-out data set. This data set comprised all Swiss-Prot proteins annotated with experimentally verified GO terms in 2010 ("Set 2010"). All proteins annotated before 2010 served as templates ("Set < 2010"). This ascertained that there was no overlap to the CAFA targets. In the following, we refer to the optimized student methods as StudentA'-C'.

###Post-CAFA method combination

Due to the end of the lecture during which the methods were developed, we could not combine them. We did this also post-CAFA. We randomly split Set 2010 into two equal parts (Set 2010a and 2010b). Parameters were optimized on the first split (2010a; as before, only with 2010a instead of 2010). These optimized variants of StudentA-C (say StudentA''-C'') were applied to the second split (2010b). Then, we switched the roles of the two sets and repeated the procedure to obtain predictions for each protein in Set 2010. With these predictions, we trained a commonly used meta classifier [13], namely a weighted least-squares linear regression model. This corresponded to the formula x*A' + y*B' + z*C' + i = p, where A', B' and C' are the results of the student methods for each predicted GO term and [x-z] and i are the coefficients to optimize in the regression so that p reflects the reliability of the GO term. In order to meta-predict a new target protein, we first annotate it with methods StudentA'-C'. Each predicted GO term is then converted into a vector of three elements (one dimension for each method) and put into the formula above. The resulting value of p is the reliability of the GO term for the given target. We refer to this predictor as MetaStudent'.

###Baseline classifiers

The CAFA organizers implemented the following three baseline classifiers to gauge the improvement of current function predictiors over old or naïve methods. (1) Priors. Every target has the same annotations and each term's score is the probability of that term occurring in Swiss-Prot. (2) BLAST. Target annotations are simply the maximum sequence identity returned by BLAST under default parameters when aligning a target with all proteins annotated with a given term. (3) GOtcha. Using the same BLAST results as BLAST, Gotcha I-Scores are calculated as the sum of the negative logarithm of the E-Value of the alignment between the target protein and all proteins associated with a given term. Additionally, we introduce Priors', which simply returns the entire GO annotation of a random Swiss-Prot protein. Scores are assigned as in Priors.

###Data sets

We used five different data sets for method development and evaluation. All are exclusively derived from GO and the GO annotated proteins from Swiss-Prot and only differ in their release dates. The first three methods used the GO/Swiss-Prot releases from Oct. 2010 ("Set < 2010_10") for both development and group-internal evaluations. We updated to the versions from Dec. 2010 ("Set < 2010_12") and submitted all 48,298 CAFA targets with each method. For post-CAFA developments, we used the release of Jan. 2010 as the source for template annotations ("Set < 2010"). The independent data set needed for post-CAFA parameter optimization then contained all proteins annotated between January and December 2010 ("Set 2010"). Analogously to CAFA, we ignored proteins that had any GO annotation before January 2010 and only retained experimental annotations in the remaining proteins. Experimental GO evidence codes were: IDA, IMP, IPI, IGI, IEP, TAS, IC, and EXP (same as in CAFA). "Set_2010" contained 1752 targets with BPO and 1351 with MFO annotations.

The CAFA organizers provided the original CAFA targets (436 with BPO and 366 with MFO annotations). They correspond to the proteins annotated between January and May 2011 ("Set 2011"). This set was derived following a similar algorithm as those in "Set 2010". The difference was that the CAFA organizers also excluded annotations from the GOA project in proteins annotated before January 2011 (a resource we left untouched). We used the annotations in "Set < 2010_12" to predict proteins in "Set 2011".


## Evaluation

###Performance measures used 
####Top-20
Given the prediction of a single protein, the top-20 measure first reduces the prediction to the terms with the highest reliability (Figure 1: green nodes with score 0.8). It then defines recall as the number of correctly predicted GO terms divided by the number of all true GO terms. Precision corresponds to the number of correctly predicted GO terms divided by the number of all predicted GO terms. In Figure 1, for example, recall is 1/11 = 0.09 and precision is 1/2 = 0.5. If a target is not predicted at all, it is assigned a recall of 0.0. Precision is not calculated in such a case and has no influence. Repeating this for all targets, we obtain the average recall and precision. This is the first point in the recall-precision curve. In order to fill the curve, we gradually add the terms with 2nd, 3rd, ..., 20th highest reliability to the predictions and recalculate all of the above. 

####Threshold

The threshold measure [4] follows a similar concept as top-20. Instead of considering a certain number of terms for each target at a time the measure demands a threshold between 0.0 and 1.0. In case of a threshold of 0.82, for example, each prediction is reduced to terms with a reliability greater than or equal to 0.82. Recall and precision can then be calculated analogously to the top-20 measure. A curve is obtained by lowering the threshold in steps of 0.01 from 1.0 to 0.0.

####Leaf threshold

The leaf threshold measure, finally, operates exclusively on the leaves of a propagation (red nodes in Figure 1). First, predicted and experimental subgraphs are reduced to their leaf terms (Figure 1: experimental leaves on the left, predicted leaves on the right). Then, we define a threshold T as before, e.g. T = 0.82, and reduce each prediction to the leaves with a reliability ≥ T. The recall of a single prediction is given by the number of correctly predicted leaves divided by the number of all experimental leaves. Precision is defined analogously. Consequently, we can derive a recall-precision curve in the same way as for the threshold measure. In Figure 1, we obtain the first non-empty prediction as soon as the threshold reaches 0.80 (the highest score of all predicted leaves is 0.8). In this case, recall and precision correspond to 0/3 = 0.0 and 0/1 = 0.0.

The leaf threshold measure is orthogonal to the top-20 and threshold measure: in the case of low recall, for example, the former two measures remove specific GO terms from the prediction and retain only the more general terms. Naturally, more general terms have a higher chance to overlap with the experimental propagation than specific terms, resulting in higher precision. However, the leaves of this reduced prediction are not more likely to overlap with the leaves of the experimental annotation. If the full prediction was the best estimate of the experimental leaves, the reduced version could even result in recall = precision = 0.0 by the leaf threshold measure, because the reduction might remove all correctly predicted leaves. Our new measure assesses how well the exact functions of a protein are predicted. Too general or specific predictions are penalized.
Maximum F1 score
The top-20 and threshold measure were the two main metrics in the CAFA meeting. The leaf measure is introduced here for the first time. In order to rank methods, the CAFA organizers additionally used the maximum F1 score over all recall-precision pairs obtained with the threshold measure (Fmax). The F1 score is defined as:


![alt tag](http://static-content.springer.com/image/art%3A10.1186%2F1471-2105-14-S3-S7/MediaObjects/12859_2013_5696_Equ1_HTML.gif)

We also employed Fmax in order to choose among alternative parameters during method development after CAFA.


####Comparison with other tools
* ...
