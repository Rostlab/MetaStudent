

# usage: ./parse_blast.pl <inputFile> <outputFile>

use strict;

open(INFILE, "< $ARGV[0]") or die "could not open input file!\n";
open(OUTFILE, "> $ARGV[1]") or die "could not open output file!\n";

#my $iteration = 2;
my $output = "";

while(my $line=<INFILE>) {

	chomp($line);

	# if there is a new round, erase results from previous round (we are only interested in the last PSI-BLAST iteration)
	if(index($line, "Results from round") == 0) {

		print $line, "\n";
		$output = "";

	} elsif(index($line, ">") == 0) {
		
		# extract the first identifier line from the FASTA header
		my $current_go_terms = substr($line,1);
		
		# continue reading the header in case it is wrapped into multiple lines, until we hit the line containing "Length"
		while($line = <INFILE> and index($line, "Length") < 0) {
			$line =~ s/\s//g;
			$current_go_terms .= $line;
		}
		
		# then continue reading until we hit the line containing the E-value and extrat it
		do {
			$line = <INFILE>;
		} until ($line =~ m/Expect = (.*),/i);
		
		print $current_go_terms, "\t", $1, "\n";
		$output .= $current_go_terms . "\t$1\n";
	}

}

print OUTFILE $output;

close(INFILE);
close(OUTFILE);
