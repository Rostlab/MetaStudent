
use strict;

open(INFILE, "< $ARGV[0]") or die "could not open input file! (".$ARGV[0].")\n";
open(OUTFILE, "> $ARGV[1]") or die "could not open output file! (".$ARGV[1].")\n";
open(DB, "< $ARGV[2]") or die "could not open output file! (".$ARGV[2].")\n";

my $db = join("", <DB>);


while(my $line=<INFILE>) {
    
    if(substr($line,0,1) eq '#') {
        
        my @split = split(/\s+/,$line);
        
        my @GOs = split(/,/,$split[1]);
        
        for(my $i; $i < @GOs.length; $i++) {
            
            $GOs[$i] = 'GO:'.$GOs[$i];
        }
        $split[1] = join(',',@GOs);
        
        print OUTFILE join("\t",@split)."\n";
    } elsif (substr($line,0,1) eq '>') {
        
        my $pattern = $line;
        chomp $pattern;
        $pattern =~ s/-/\\|/;
        
        $db =~ /($pattern):([^\n]*)/;
        my $id = $1;
        
        my @GOs = split(/,/,$2);
        
        for(my $i; $i < @GOs.length; $i++) {
            
            $GOs[$i] = 'GO:'.$GOs[$i];
        }
        my $gos = join(',',@GOs);
        
        print OUTFILE $id."\t".$gos."\n";
        
    } else {
        print OUTFILE $line;
    }
}