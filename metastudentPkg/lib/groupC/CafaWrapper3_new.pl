
use Cwd 'abs_path';

if (!$ARGV[0] || !$ARGV[1]) {die"**Usage: perl CafaWrapper1.pl database outputfolder**"};
my $database=abs_path($ARGV[0]);
my $outputfile=abs_path($ARGV[1]);
my $scoring = $ARGV[2];
my $tmpDir = $ARGV[3];

`perl exercise3.pl $database $tmpDir/blast.out`;

my $peterput=`perl treehandler_new.pl -mfo fullTransitiveClosureGO.txt -bpo fullTransitiveClosureGO.txt -method 3 -pred $tmpDir/blast.out -scoring $scoring`;

open (FH,"> $outputfile");
print FH $peterput;
close FH;
#unlink "blast.out";
