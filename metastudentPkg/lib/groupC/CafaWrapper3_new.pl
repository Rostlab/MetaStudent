#!/usr/bin/perl
use Carp;
use Cwd 'abs_path';

if (!$ARGV[0] || !$ARGV[1]) {die"**Usage: perl CafaWrapper1.pl database outputfolder**"};
my $database=abs_path($ARGV[0]);
my $outputfile=abs_path($ARGV[1]);
my $scoring = $ARGV[2];
my $tmpDir = $ARGV[3];

my @cmd = qq|exercise3.pl $database $tmpDir/blast.out|;
system(@cmd) && confess("@cmd failed: ".($?>>8));

@cmd = qq|treehandler_new.pl -mfo fullTransitiveClosureGO.txt -bpo fullTransitiveClosureGO.txt -cco fullTransitiveClosureGO.txt -method 3 -pred $tmpDir/blast.out -scoring $scoring|;
my $peterput=`@cmd`;
if($?){ confess("@cmd failed: ".($?>>8)); }

open (FH,">", $outputfile) || confess("failed to open '> $outputfile': $!");
print FH $peterput;
close FH;
#unlink "blast.out";
