
use strict;
use warnings;
use diagnostics;

while (my $line = <STDIN>) {
	if(substr($line,0,1) eq ">") {
		chomp $line;
		
		if($line =~ m/^>[^:]+:([0-9,]+)$/g) {
			my $terms = $1;
			my @t = split(/,/, $terms);

			@t = map { "GO:$_" } @t;
			print ">" . join(",", @t) . "\n";
		} else {
			die "Could not recognize identifier line!";	
		}

	} else {
		print $line;
	}
}
