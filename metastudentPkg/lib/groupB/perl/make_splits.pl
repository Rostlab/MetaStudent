
use strict;
use warnings;
use diagnostics;
use List::Util 'shuffle';

my @sets = @ARGV;

if(scalar @sets == 0) {
	die "No sets specified!";
}

if((scalar @sets) % 2 != 0) {
	die "Need even number of arguments!";
}


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

my @shuffled = shuffle(@sequences);

print STDERR "Have " . (scalar @sequences) . " sequence(s).\n";

my @splits = ();
my $partsum = 0;
for(my $i = 0; $i < scalar @sets; $i+=2) {
	my ($percentage, $file) = ($sets[$i], $sets[$i+1]);
	$partsum += $percentage;
	my @split = ($percentage, $file);
	push @splits, \@split;
	
}

my $pos = 0;
for my $split (@splits) {
	
	my ($percentage, $file) = @$split;
	$percentage /= $partsum;


	my $num = int($percentage * scalar @sequences);
	
	print STDERR "Part: $percentage% - From: $pos To: " . ($pos+$num) . " \n";
	

	open(OUT, "> $file") or die("Could not open $file for writing!");
	for(my $i = 0; $i < $num; $i++) {
		print OUT $shuffled[$pos+$i];
	}
	close(OUT);

	$pos += $num;
}
