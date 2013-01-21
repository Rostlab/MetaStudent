
use strict;
use warnings;
use diagnostics;
use Getopt::Long;
use List::Util 'shuffle';

my ($percentage);

GetOptions( "percentage|p=f"	=> \$percentage) or die("Error parsing options!");

my $skipping = 1;

my @sequences = ();

my $seqbuf = "";
while(my $line = <STDIN>) {
	
	if(substr($line,0,1) eq ">") {
		push @sequences, $seqbuf unless $seqbuf eq "";
		$seqbuf = "";
	}

	$seqbuf .= $line;
}
push @sequences, $seqbuf unless $seqbuf eq "";
my $seqcount = ((scalar @sequences) * $percentage) / 100; 

print STDERR "Got " . (scalar @sequences) . " sequences. Will take $seqcount ($percentage%) of them. \n";

my @shuffled = shuffle(@sequences);

for(my $i = 0; $i < $seqcount; $i++) {
	print $shuffled[$i];
}
