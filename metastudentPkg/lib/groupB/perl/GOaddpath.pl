
# usage: ./perl GOaddpath.pl (GOterm_file) (GOterm2term_file) < (inputFile) > (outputFile)

use strict;

# load GOfunctions
require GOfunctions;
GOfunctions::import($ARGV[0],$ARGV[1]);
print STDERR "Goterm-file: $ARGV[0] GoTerm2Term-file: $ARGV[1]\n";

# -- parameter --
my $GOseperator = ',';

# -- main loop --
while(my $line=<STDIN>) {
    
    if(substr($line,0,1) eq '#') {
	# copy over comments
	print $line;
    } else {
        
	# expected format:
	# (target) \t (GoTerm1),(GoTerm2),... \t (evalue) \t (bitscore)
	
        my @table = split(/\t/,$line,3);
        my @GO = split($GOseperator,$table[1]);
        my $scores = $table[2];

        @GO = GOfunctions::goFilter(@GO);    # filter out cellular_components and anything else unusual
        @GO = GOfunctions::goAddPath(@GO);  # add all GO terms lying on the path to root
        @GO = GOfunctions::goFilter(@GO);   # filter out anything unusual which might have been added
        
	print $table[0] . "\t" . join($GOseperator,@GO) . "\t$scores" unless $#GO == -1;
    }
}

