

# usage: ./perl GOeval.pl <GOterm_file> <GOterm2term_file> <inputFile> <outputFile>


use strict;

# load GOfunctions
require GOfunctions;
GOfunctions::import($ARGV[0],$ARGV[1]);


open(INFILE, "< $ARGV[2]") or die "could not open input file!\n";
open(OUTFILE, "> $ARGV[3]") or die "could not open output file!\n";

# -- parameter --
my $GOseperator = ',';



# -- comparer --

sub compare {
    my ($target_ref, $prediction_ref) = @_;
    my @GOtarget = @{$target_ref};
    my @GOprediction = @{$prediction_ref};
    
    @GOtarget = keys %{{ map { $_ => 1 } @GOtarget }}; # remove duplicates, just to be sure
    @GOprediction = keys %{{ map { $_ => 1 } @GOprediction }};
    
    
    my $correct = 0;
    foreach my $target(@GOtarget) {
        
        foreach my $prediction(@GOprediction) {
            
            if($target eq $prediction) {
                $correct++;
            }
        }
    }
    
    my $precision = 0.0;
    if(scalar(@GOprediction) != 0) {
        $precision = $correct/scalar(@GOprediction);
    }
    my $recall= 0.0;
    if(scalar(@GOtarget) != 0) {
        $recall = $correct/scalar(@GOtarget);
    }
    
    return($precision,$recall,$correct,scalar(@GOtarget),scalar(@GOprediction));
}


my $output = "";
my $avrpre = 0;
my $avrrec = 0;
my $size = 0;

my $tmppre; my $tmprec; my $tmpcor; my $tmptar; my $tmppred;
$output .= "#precision\trecall\tcorrect\ttargets\tpredictions\n"; # header vor output

while(my $line=<INFILE>) {
    
    
    if(substr($line,0,1) eq '#') {
        #$output .= $line;
    } else {
        
        
        # file "parser"
        # format: GOtargets \t GOprediction \t evalue \t bitscore
        my @table = split(/\t/,$line,3);    
        my @GOtarget = split($GOseperator,$table[0]);
        my @GOprediction = split($GOseperator,$table[1]);
        
        
        @GOtarget =  GOfunctions->goAddPath(GOfunctions->goFilter(@GOtarget));    # remove cellular_component and add the path GOs
        @GOprediction =  GOfunctions->goAddPath(GOfunctions->goFilter(@GOprediction));
        
        
        # eval
        ($tmppre, $tmprec, $tmpcor, $tmptar, $tmppred) = compare(\@GOtarget,\@GOprediction);
        
        if($tmptar > 0) {
            $output .= join("\t",$tmppre, $tmprec, $tmpcor, $tmptar, $tmppred)."\n";
        
        $avrpre += $tmppre;
        $avrrec += $tmprec;
        $size++;
        }
    }
}

$output = "#precision:\t".$avrpre/$size."\trecall:\t". $avrrec/$size."\tsize:\t".$size ."\n".$output;

print OUTFILE $output;

#print $output;


close(INFILE);
close(OUTFILE);