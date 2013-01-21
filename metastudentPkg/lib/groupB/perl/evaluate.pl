
use strict;
use warnings;
use diagnostics;

# evaluate.pl - Evaluate CAFA predictions

require GOfunctions;

use Getopt::Long;
use Pod::Usage;

my ($gotermfile, $goterm2termfile, $sprot2gofile, $identifier2go, $targetcount, $predictioncount, $prefix, $help, $man);

GetOptions(	"gotermfile|g=s"	=> \$gotermfile,
		"term2termfile|t=s"	=> \$goterm2termfile,
		"sprot2gofile|s=s"	=> \$sprot2gofile,
		"identifier2go|i"	=> \$identifier2go,
		"targetcount|c=i"	=> \$targetcount,
		"predcount|n=i"		=> \$predictioncount,
		"prefix|p=s"		=> \$prefix,
		"help|h|?"		=> \$help,
		"man"			=> \$man
	) or pod2usage(2);

pod2usage(1) if $help;
pod2usage(-exitstatus => 0, -verbose => 2) if $man;

if(not defined $gotermfile) {
	print STDERR "Must specify a gotermfile!\n\n";
	pod2usage(2);
}

if(not defined $goterm2termfile) {
	print STDERR "Must specify a term2termfile!\n\n";
	pod2usage(2);
}

if((not defined $sprot2gofile) && (not $identifier2go)) {
	print STDERR "Must specify a sprot2gofile or use identifier2go mode!!\n\n";
	pod2usage(2);
}

if(defined $sprot2gofile && defined $identifier2go) {
	print STDERR "Must specify either sprot2gofile OR identifier2go mode!\n\n";
	pod2usage(2);
}

if(not defined $prefix) {
	print STDERR "Must specify a prefix!\n\n";
	pod2usage(2);
}

# initialize mark's go expander
GOfunctions::import($gotermfile, $goterm2termfile);

# load actual go terms from sprot2gofile if we have one
my %sprot2go = ();
if(defined $sprot2gofile) {
	open(SPROTGO, "< $sprot2gofile") or die ("Could not open sprot2gofile at $sprot2gofile for reading!");
	while (my $line = <SPROTGO>) {
		chomp $line;
		my ($sprot, $go) = split(/\t/, $line);
		my @g = split(/,/, $go);
		$sprot2go{$sprot}=\@g;
	}
}

# do evaluation
my ($current_target);
my (@observed_b, @observed_m, @predictions);
my (@evaluation);
my ($targets);
while( my $line = <STDIN> ) {
	chomp $line;

	if(substr($line,0,1) eq ">") {
		# start of new target - clear buffers
		@predictions = ();

		my @observed;
		if(defined $identifier2go) {

			# get observed functions from identifier line
			$current_target = substr($line,1);
			@observed = split(/,/, $current_target);

		} else {

			# get observed functions from sprot2go file
			if($line =~ m/^>([0-9A-Za-z]+_[0-9A-Za-z]+)-[0-9A-Z]+$/g) {
				$current_target = $1;
			} else {
				die("Could not recognize identifier line: $line");	
			}
			
			if(defined $sprot2go{$current_target}) { 
				@observed = @{$sprot2go{$current_target}};
			} else {
				# if we don't have the swissprot identifier in the sprot2go file,
				# it means that no MFO or BPO terms were associated with it in swissprot.
				@observed = ();
			}
		}


		print STDERR "Target $current_target";

		# split observed functions by ontologies
		@observed_b = ();
		@observed_m = ();
		foreach my $obs (@observed) {
			my $type = GOfunctions->getType($obs);

			die "Could not recognize GO term $obs!" unless defined $type;
				
			if($type eq "B") {
				push @observed_b, $obs;
			} elsif($type eq "M") {
				push @observed_m, $obs;
			} elsif($type eq "C") {
				# skip cellular process
			} else {
				die "unknown type: $type";
			}
		}

		print STDERR ".";


	} elsif(substr($line,0,2) eq "//") {
		# end of target - evaluate it

		print STDERR ".";

		# filter, blow up, make set
		my %obs_b = %{{map {$_ => 1} GOfunctions->goAddPath(GOfunctions->goFilter(@observed_b))}};
		my %obs_m = %{{map {$_ => 1} GOfunctions->goAddPath(GOfunctions->goFilter(@observed_m))}};

		print STDERR ".";

		# evaluate all scoring schemes
		for(my $score=0; $score < scalar @evaluation; $score++) {
		
			# sort predictions by current scoring scheme
			my @predicted =  sort {$b->[2]->[$score] <=> $a->[2]->[$score]} @predictions;
			
			# split into ontologies
			my @pred_b = map {$_->[0]} grep {$_->[1] eq "B"} @predicted;
			my @pred_m = map {$_->[0]} grep {$_->[1] eq "M"} @predicted;

			# only take top-n predictions if n specified
			if(defined $predictioncount) {
				my $max_b = (scalar @pred_b >= $predictioncount) ? ($predictioncount - 1) : ((scalar @pred_b) - 1);
				my $max_m = (scalar @pred_m >= $predictioncount) ? ($predictioncount - 1) : ((scalar @pred_m) - 1);
				@pred_b = @pred_b[0 .. ($predictioncount-1)] if scalar @pred_b;
				@pred_m = @pred_m[0 .. ($predictioncount-1)] if scalar @pred_m;
			}

			# blow up, make set
			my %prd_b = scalar @pred_b ? %{{map {$_ => 1} GOfunctions->goAddPath(@pred_b)}} : ();
			my %prd_m = scalar @pred_m ? %{{map {$_ => 1} GOfunctions->goAddPath(@pred_m)}} : ();
			
			# compare sets	
			my ($tp_b, $fp_b, $fn_b, $pre_b, $rec_b) = confusion_matrix(\%obs_b, \%prd_b); 
			my ($tp_m, $fp_m, $fn_m, $pre_m, $rec_m) = confusion_matrix(\%obs_m, \%prd_m);

			# add up cumultative precision, recall for scores and ontologies
			$evaluation[$score]->{b_cum_pre} += $pre_b;
			$evaluation[$score]->{b_cum_rec} += $rec_b;
			$evaluation[$score]->{m_cum_pre} += $pre_m;
			$evaluation[$score]->{m_cum_rec} += $rec_m;

			print STDERR "s";
		}
	
		print STDERR "\tDone.\n";

		$targets++;
	} else {
		# prediction inside target
		# term	type	score1	score2	...	scoren
		my @pred = split(/\t/, $line, 3);
		my @scores = split(/\t/, $pred[2]);

		# do we have an evaluation data structure yet?
		if(not @evaluation) {

			# if not, build it up now as we know how many scoring schemes we will have
			@evaluation = ();
			for (my $i=0; $i < scalar @scores; $i++) {
				my %eval = (
					b_cum_pre => 0,
					b_cum_rec => 0,
					m_cum_pre => 0,
					m_cum_rec => 0
				);

				push @evaluation, \%eval;
			}
		}

		# fix comma decimal marks on scores - no idea where they came from
		for(my $i=0; $i < scalar @scores; $i++) {
			$scores[$i] =~ s/,/./g;
		}

		$pred[2] = \@scores;
		push @predictions, \@pred;

	}
}


# we have gone through all predictions now

# print header
my @f_steps = (0.25, 0.5, 1, 2, 4);
print "#profile\tprecision\trecall\tprecision-b\trecall-b\tprecision-m\trecall-m";
foreach my $beta (@f_steps) { print "\tf$beta\tf$beta-b\tf$beta-m"; }
print "\n";

# If we have specified a number of targets by the command line, use that for division.
if($targetcount) { $targets = $targetcount; }

# for each scoring scheme,
for(my $score=0; $score < scalar @evaluation; $score++) {

	# calculate precision and recall for the ontologies
	my $precision_b = $evaluation[$score]->{b_cum_pre} / $targets;
	my $recall_b = $evaluation[$score]->{b_cum_rec} / $targets;
	my $precision_m = $evaluation[$score]->{m_cum_pre} / $targets;
	my $recall_m = $evaluation[$score]->{m_cum_rec} / $targets;

	# calculate combined precision and recall
	my $precision = ($precision_b + $precision_m) / 2;
	my $recall = ($recall_b + $recall_m) / 2;

	print "$prefix-score_$score\t$precision\t$recall\t$precision_b\t$recall_b\t$precision_m\t$recall_m";

	# calculate f-score in steps
	foreach my $beta (@f_steps) {
		my $f_score_b = (1+$beta*$beta) * ($precision_b * $recall_b) / ($beta * $beta * $precision_b + $recall_b);   
		my $f_score_m = (1+$beta*$beta) * ($precision_m * $recall_m) / ($beta * $beta * $precision_m + $recall_m);   
		my $f_score = ($f_score_b + $f_score_m) / 2;
		print "\t$f_score\t$f_score_b\t$f_score_m";
	}
	print "\n";
}

exit 0;


sub confusion_matrix {
	my ($obs, $prd) = @_;

	my %o = %$obs;
	my %p = %$prd;

	my($tp, $fp, $fn) = (0,0,0);

	foreach my $pr (keys %p) {
		if(defined($o{$pr})) {
			$tp++;
		}else{
			$fp++;
		}
	}

	foreach my $ob (keys %o) {
		if(not defined($p{$ob})) {
			$fn++;
		}
	}

	# calculate precision and recall for this point
	my $pre = ($tp + $fp) > 0 ? $tp / ($tp + $fp) : 0;
	my $rec = ($tp + $fn) > 0 ? $tp / ($tp + $fn) : 0;

	return ($tp, $fp, $fn, $pre, $rec);
}

=pod

=head1 TITLE

evaluate.pl - Evaluate CAFA predictions

=head1 SYNOPSIS

	$ ./evaluate.pl -p "MyEvaluation" -g goterm.txt -t goterm2term.txt -s sprot2go.txt < prediction.out > prediction.eval

Options:

	--gotermfile	-g	File listing GO terms in Mark's format
	--term2termfile	-t	File listing Terms to terms in Mark's format
	--targetcount	-c	Specify the number of targets that were initially sent into the predictor. If unspecified, the number of predicted targets will be used.
	--predcount	-n	Only take top-n ter