#!/usr/bin/perl
use Carp;
if (!$ARGV[0] || !$ARGV[1]) {die"**Usage: perl exercise3.pl [inputfile] [outputfile]\nperl exercise3.pl [inputfile] [outputfile] [optional: -e0.1] [optional: -h0.1] [optional: -j2] [optional: databasefile]\nthe above examples are the current default values, default database is the sprot_go_80 from exercise 2\n"}
my $input=$ARGV[0];
my $output=$ARGV[1];
my $database="/mnt/project/interres/CAFAEval/sprot_go.fasta"; #default blast datenbank
my $e="-e0.1"; #default blast parameter kann mit $ARGV[2] �bergeben werden. Format -e[wert] zB: 'perl example3.pl input output -e0.5'
my $h="-h0.1"; #s.o
my $j="-j2"; #s.o
my @out;
if ($ARGV[2]) {$e=$ARGV[2];}
if ($ARGV[3]) {$h=$ARGV[3];}
if ($ARGV[4]) {$j=$ARGV[4];}
if ($ARGV[5]) {$database=$ARGV[5];}

#### - builds a hash containing targetid and target sequence 
#$fhin="FHIN";
#open($fhin,$input)|| die"** could not open input-file $input\n";  
#while(<$fhin>){
#    next if !/^>(\S+)|^(\S+)/;
#    if ($1) {
#        if ($sequence) {$targets{$targetid}=$sequence;}
#        $sequence="";
#        $targetid=$1;
#    }
#    else {$sequence=$sequence.$2;} 
#}
#$targets{$targetid}=$sequence;
#close $fhin;

sub isfasta {
    my $line = shift;
    if ($line =~ /^>/){
    return 1;}
    else {return}
}
$fhin="FHIN";
open($fhin,$input)|| die"** could not open input-file $input\n"; 
@blastoutput=<$fhin>;
close $fhin;
if (isfasta(@blastoutput)){print "Now running blast\n";
my @cmd = qq|blastpgp -i $input -d $database $e $h $j|;
@blastoutput=`@cmd`; #Systemaufruf f�r blastpgp
if($?){ confess("@cmd failed: ".($?>>8)); }
print "... done\n";
}
foreach (@blastoutput) { 
    next if !/^>.*?:(.+)$|^\s+(Score)|(^Searching)|^Query=\s(.*)|^\s(Identities)/;
    if ($1 && $true==1) {$identifier.="GO:".$1; $identifier=~s/,GO:|,/,GO:/g;}
    elsif ($2 && $true==1) {
        $_=~/Score\s=\s+(\d+).*Expect\s=\s(.+),/;
        $score=$1;
        $evalue=$2;
    }
    elsif ($5 && $true==1){
        if($_=~/Identities\s=\s(\d+\/\d+).*/){
	$identities=$1;}	
	if ($_=~/Positives\s=\s(\d+\/\d+).*/){
	$positives=$1;}
	if($_=~/Gaps\s=\s(\d+\/\d+).*/){
        $gaps=$1;}
	else {
	$gaps = "0";}
        
	
        if ($identifier) {
            $targets{$targetid}=$identifier;
            push (@out, "$evalue\t$score\t$identities\t$positives\t$gaps\t$identifier\n"); undef $identifier; undef $evalue; undef $score; undef $identities; undef $positives; undef $gaps;
	}
        #else {print $fhout "$1\t\talternative alignment\n";}
    }
    elsif ($3) {$true=1;}
    elsif ($4) {
        $target.=$4;
#       if ($target =~ /.*:(.+)$/) {$target="GO:".$1; $target=~s/,/,GO:/g; }
        push (@out,"Target: $target\n");
        $targetid=$target;
	$targets{$targetid}="no prediction";
        $true=0;
        undef $target;
    }
}

#print "Predictions:\n";
#foreach $target (keys %targets){
#    print "$target\t$targets{$target}\n";
#}
my $fhout="FHOUT";
$c=0;
open ($fhout,"> $output") || die"** could not open output-file $output\n";
foreach (@out) {   
    print $fhout "***\n" if /^Target/ && $c++>0;;
    print $fhout $_;
}
print $fhout "***\n";
close $fhout;
