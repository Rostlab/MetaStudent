
use strict;
use warnings;
use diagnostics;

my $termfile = $ARGV[0] or die("Please provide a termfile!");

open(TERM, "< $termfile") or die("Could not open termfile at $termfile!");
my %terms = ();
while(my $line = <TERM>) {
	chomp $line;
	my @l = split(/\t/, $line);

	my ($goterm, $type) = ($l[3], $l[2]);

	$terms{$goterm} = 1 if $type eq "biological_process";
	$terms{$goterm} = 1 if $type eq "molecular_function";	
	$terms{$goterm} = 1 if $type eq "cellular_component";	
}

while (my $line = <STDIN>) {
	chomp $line;
	my @l = split(/\t/, $line);
	my $id = shift @l;

	my @newterms = ();

	for my $term (@l) {
		push @newterms, $term if $terms{$term};
	}
	
	print "$id\t" . join(",", @newterms) . "\n" if scalar @newterms;
}


