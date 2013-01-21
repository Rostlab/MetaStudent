
use strict;
use warnings;
use diagnostics;

use Getopt::Long;
use Pod::Usage;


sub uniq {
	my @list = @_;
	my @out = ();
	my %seen = ();
	foreach my $item(@list) {
		push @out, $item unless $seen{$item}++;
	}
	return @out;
}

my ($infile, $outfile, $gofile, $help, $man);
my $exclude = "GO:0005575";

GetOptions(	'infile|i=s'	=> \$infile,
		'outfile|o=s'	=> \$outfile,
		'gofile|g=s'	=> \$gofile,
		'exclude|x=s'	=> \$exclude,
		'help|h|?'	=> \$help,
		'man'		=> \$man,
) or pod2usage(2);

pod2usage(1) if $help;
pod2usage(-exitstatus => 0, -verbose => 2) if $man;

if(!$infile) {
	print STDERR "No input file specified!\n";
	pod2usage(2);
}

if(!$outfile) {
	print STDERR "No output file specified!\n";
	pod2usage(2);
}

if(!$gofile) {
	print STDERR "No GO hierarchy file specified!\n";
	pod2usage(2);
}

if(!$exclude) {
	print STDERR "No clude term specified!\n";
	pod2usage(2);
}


open(GO, "< $gofile") or die("Could not open $gofile for reading!");

# load go hash: $go{$id} -> list of is_a terms
my %go = ();
while(my $line = <GO>) {
	chomp $line;

	next if substr($line,0,1) eq "#";

	my @l = split(/\t/, $line);
	
	my $id = shift @l;
	$go{$id} = \@l;
}

close(GO);

open(IN, "< $infile") or die("Could not open $infile for reading!");
open(OUT, "> $outfile") or die("Could not open $outfile for writing!");

my $skip = 0;
while(my $line = <IN>) {
	chomp $line;

	# are we in an identifier line?
	if(substr($line,0,1) eq ">") {
		# yes, so filter out terms


		# get all GO terms
		my @ids = split(",",substr($line,1));

		my $done = 0;
		while (!$done) {
			my @new_ids = ();
			
			$done = 1;
			foreach my $id (@ids) {
				# travel up one hierarchy level
				
				if(not defined($go{$id})) {
					push @new_ids, $id;
					next;
				}

				my @is_a = @{$go{$id}};
				# are we at the top yet?
				if(scalar @is_a > 0) {
					# no, so add nodes of new level
					push @new_ids, @is_a;
					$done = 0;
				} else {
					# yes, so keep top level nodes
					push @new_ids, $id;
				}
			}
			@ids = @new_ids;
		}

		my @uniq_ids = &uniq(@ids);

		$skip = ((scalar @uniq_ids) == 1) && ($uniq_ids[0] eq $exclude);

	}
	
	# it's sequence, so print out unless we're skipping an entry
	print OUT "$line\n" unless $skip;
}

close(IN);
close(OUT);

exit 0;

=pod

=head1 TITLE

filter_fasta.pl - Filter FASTA file by GO hierarchy

=head1 SYNOPSIS

filter_fasta.pl -i infile.f -o outfile.f -g go_hierarchy.txt -x 0005575

=head1 DESCRIPTION

	-i	Infile in FASTA format, with GO identifiers as sequence names
	-o	Outfile in FASTA format, in the same format, but excluding paths that lead to -x
	-x	Exclude this GO term

=cut
