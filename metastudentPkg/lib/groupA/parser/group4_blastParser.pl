#/usr/local/bin/perl

use strict;

print "insert name/path of infile: ";
my $infile = <STDIN>;
open(FILE, $infile);
$infile =~ s/\.\S*//;
my $outfile = $infile . "_evalues.out";

my $read = 0;
my $go = 0;

open(OUT, ">$outfile");
while(<FILE>)
{
	
	if($_ =~ /^Results from round 2/)
	{
		$read = 1;
	}
	if($read == 1)
	{
		if($_  =~ /^>GO/)
		{
			$go = 1;
		}
		if($go == 1)
		{
			if($_ !~ /Length/)
			{
				my $write = $_;
				$write =~ s/\s+//g;
				print OUT $write;
				print $write;					
			}
			else
			{
				$go = 0;
				print OUT "\n";
				print "\n";
			}
		}
		if($_ =~ /Expect = (\S+?),/)
		{
			print OUT $1."\n";
			print "$1\n";
		}
	}
}

close(FILE);
close(OUT);
