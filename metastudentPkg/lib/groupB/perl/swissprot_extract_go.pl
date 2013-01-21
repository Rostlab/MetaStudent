
use strict;
use warnings;
use diagnostics;


# swissprot_extract_go.pl - Extract SwissProt->GO data from uniprot_sprot.dat
# Usage: 
#  $ swissprot_extract_go.pl < uniprot_sprot.dat > sprot_go.lst

while(my $line = <STDIN>) {
	chomp $line;
	my $type = substr($line, 0, 2);
	my $content = length($line) >= 4 ? substr($line, 5) : "";
	
	if($type eq "ID") {
		if($content =~ m/^([0-9A-Z]+_[0-9A-Z]+)/g) {
			print $1;
		} else {
			die("Unknown identifier line:\n$line");
		}
	} elsif($type eq "DR") {

		if($content =~ m/^GO;\s*(GO:[0-9]{7});/g) {
			print "\t$1";
		}
	} elsif($type eq "//") {
		print "\n";
	}
}
