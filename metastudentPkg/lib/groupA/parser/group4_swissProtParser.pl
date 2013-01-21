
use strict;

#print "enter path to file: ";
#open(FILE, <STDIN>);
my $infile = "/mnt/opt/data/pp2_exercise/uniprot_sprot.dat";
open(FILE, $infile);
print "enter output file name: ";
my $outfile = <STDIN>;
open(WRITE, '>>'.$outfile);

my @go;
my $ID;
my $readlines = 0;
my $seq; 
my $countGoSeq = 0;
my $sumOfGo = 0;
my %hash = ();
while(<FILE>){	
	my $actLine = $_;	
	if($actLine =~ /^ID/){  #suche nach text
		my @words = split(/\s+/, $actLine);
		$ID = @words[1];	
		#print $ID . "\n";
	}
	if($actLine =~ /^DR\s+?GO;\s+?(GO:.+?);/){
		push(@go, $1);
		$hash{$1} = 0;
	}
	if($actLine =~ /^\/\//){ #neuer Swissprot Eintrag
		$readlines = 0;
		if(scalar(@go) > 0){
			print WRITE ">" . join(",", @go) . "\n" . $seq . "\n";
			$countGoSeq++;
			$sumOfGo += scalar(@go);
		}
		@go = undef;
		shift(@go);
		$seq = "";
		$ID = "";
	}	
	if($readlines == 1) {
		$seq .= $actLine;
		$seq =~ s/\s//g;	
	}
	if($actLine =~ /^SQ/){
		$readlines = 1;
	}

}
open(STAT, '>'.$outfile . ".statistics");
print STAT "# of GO-annotated sequences: " . $countGoSeq . "\n";
print STAT "avg. # of GO's per sequence: " . $sumOfGo / $countGoSeq . "\n";
print STAT "# of distinct GO numbers: " . keys(%hash) . "\n";

close(STAT);
close(WRITE);
close(FILE);
exit;
