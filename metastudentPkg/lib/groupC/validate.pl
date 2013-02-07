#!/usr/bin/perl
use Carp;
use Archive::Extract;
$test=$ARGV[0];
$train=$ARGV[1];
$outfolder=$ARGV[2];
$outfolder.="/";
$fhin="FHIN";
$fh2="FHIN";
$fhout="FHOUT";
$outfile=$outfolder."temp.blast";
@cat=();
$c=0;
%hash;
open($fhin,$test);
@testsetfile=<$fhin>;
foreach (@testsetfile){
 next if !/^(>.*)/;
    #print $1."\n";
    $hash{$1}=1; 
}
$cnt=scalar(keys %hash);
foreach (@testsetfile){
    next if !(/^(>.*)/);
    $topush = "Query= $1\n";
    #print $1."\n";
    print ((++$c*100/$cnt)."%\n");
    $_=~/^>(.*):.*/;
    $blast=$1;
    $blast =~ s/\|/-/;
    #print $blast."\n";
    $blast1="/mnt/opt/data/pp2_exercise/blasts/".$blast.".blast.gz";
    $ae=Archive::Extract->new( archive => $blast1);
    $ok = $ae->extract( to => "$outfolder" );
    $blast=$outfolder.$blast.".blast";
    open($fh2,$blast)|| die"file $blast not found";
    @what=<$fh2>;
    $i = 0;
    foreach (@what) {
        #print $_;
        if (/^Results/ && /2/) {$true=1; push(@cat,"Results from round 2\n");}
        if ($true==1){
            #$tempQuery = $_ if /^Query=/;
            if (/^(>.*)/) {
                #print "what $1\n";
                push(@cat,$topush) if ($topush);
                    $topush = "";
                if (not $hash{$1}) {
                    #print "NOT found $1\n";
                    my $i2 = $i;
                    
                    #push(@cat,$tempQuery);
                    #$tempQuery="";
                    while ($i2 < $i+5) {
                        push(@cat,$what[$i2++]);    
                    }
                }
                else {
                   # print "found $1\n";
                }
            }
        }
        $i++;
    }

    close($fh2);
    unlink($blast);
    
}
close($fhin);
open($fhout,"> $outfile");
foreach $line (@cat) {
    print $fhout $line;
    }
close $fhout;
my @cmd = qq|/mnt/opt/data/pp2_exercise/groups/groupC/CafaWrapper3.pl $train $outfile $outfolder|;
system(@cmd) && confess("@cmd failed: ".($?>>8));
#unlink($outfile);
