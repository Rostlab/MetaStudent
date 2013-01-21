

# usage: ./perl GOmultifiles.pl <GOterm_file> <GOterm2term_file> <folder> <regex>

use strict;

# load GOfunctions
require GOfunctions;
GOfunctions::import($ARGV[0],$ARGV[1]);

my $dir = $ARGV[2];
opendir(DIR, $dir) or die "could nor open dir: ".$ARGV[2]."\n";
 my @files = grep (/$ARGV[3]/,readdir(DIR));
closedir(DIR);

print @files;

foreach my $file (@files) {
    
    $file = $dir.$file;    
    open(INFILE, "<".$file) or die "could not open input file! ($file)\n";
    open(OUTFILE, "> ".$file.".GOpath") or die "could not open output file! ($file.GOpath)\n";



# --- same as GOaddpath ---

    # -- parameter --
    my $GOseperator = ',';
    #my $GOprefix = 'GO:';
    
    
    # -- main loop --
    
    my $output = "";
    
    while(my $line=<INFILE>) {
        
        if(substr($line,0,1) eq '#') {
            $output .= $line;
        } else {
            
            my @table = split(/\t/,$line,2);    # file "parser"
            my @GO = split($GOseperator,$table[0]);
            
            @GO = GOfunctions::goFilter(@GO);    # filter out cellular_components and anything else unusual
            
            @GO = GOfunctions::goAddPath(@GO);  # add all GO terms lying on the path to root
            
            @GO = GOfunctions::goFilter(@GO);   # filter out anything unusual which might have been added
            
            if($#GO != -1) {    # remove empty hits
                $output .= join("\t",join($GOseperator,@GO),$table[1]); 
            }
        }
    }
    
    print OUTFILE $output;
    
    # print $output;
    
    close(INFILE);
    close(OUTFILE);

}