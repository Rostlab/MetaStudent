#!/usr/bin/perl
use Carp;
use Cwd 'abs_path';

if (!$ARGV[0] || !$ARGV[1] || !$ARGV[2]) {die"**Usage: perl CafaWrapper1.pl database targets outputfolder**"};
my $database=abs_path($ARGV[0]);
my $targets=abs_path($ARGV[1]);
my $outputfolder=abs_path($ARGV[2]);

chdir '/mnt/opt/data/pp2_exercise/groups/groupC/';

my $filename =(rand(1000000)).".blast";
while (-e $filename) {
  $filename =(rand(1000000)).".blast";
}

my @target=split("/",$targets);
my $targetfile=$target[@target-1];

my @cmd = qq|exercise3.pl $targets $filename -e0.1 -h0.1 -j2 $database|;
system(@cmd) && confess("@cmd failed: ".($?>>8));

@cmd = qq|treehandler.pl -mfo mfo.obo-xml -bpo bpo.obo-xml -method 1 -pred $filename|;
my $peterput=`@cmd`;
if($?){ confess("@cmd failed: ".($?>>8)); }

mkdir($outputfolder); 
open (FH,">", "$outputfolder/${targetfile}_1.out") || confess("failed to open '> $outputfolder/${targetfile}_1.out': $!");
print FH $peterput;
close FH;
unlink($filename);


 
