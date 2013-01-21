
## GOaddpath.pl

Adds the path up to the root(s) for all GO terms in the first column. Doesn't touch the other columns.
GO terms are additionally filtered to only include 'molecular_function' and 'biological_process'.
Doubles are also deleted.

usage: ./perl GOaddpath.pl <GOterm_file> <GOterm2term_file> <inputFile> <outputFile>
example: perl GOaddpath.pl ./../data/goterm.txt ./../data/goterm2term.txt ./../examples/ex2_parser_output ./../examples/go_test_output



## GOeval.pl

Reads in the first two columns as target and predicted GO terms.
Filters, add's Path and removes doubles.
Then evaluates the prediction.

usage: ./perl GOeval.pl <GOterm_file> <GOterm2term_file> <inputFile> <outputFile>
example perl GOeval.pl ./../data/goterm.txt ./../data/goterm2term.txt ./../examples/blast-out_cv.tab ./../examples/testoutput



## GOmultifiles.pl

runs through a directory and does the same as GOaddpath.pl to any file that matches the regular expression.

usage: ./perl GOmultifiles.pl <GOterm_file> <GOterm2term_file> <folder> <regex>
example: perl GOmultifiles.pl ./../data/goterm.txt ./../data/goterm2term.txt ./../examples/ \.tab$


## GOfunctions.pm

# gofilter(GOarray)
Filters out all GO terms which are not annotated as 'molecular_function' or 'biological_process'

# goAddPath(GOarray)
Adds the path up to the root for any GO term in the array.
Uses the relationships: 1 - is_a ; 13 - negatively_regulates; 14 - part_of; 15 - positively_regulates; 16 - regulates
(also removes any doubles from the array)


