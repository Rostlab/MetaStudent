package GOfunctions;


use strict;


# -- parameter --
#my $GOprefix = 'GO:';
my $GOseperator = ',';
my @GOrelationships = ("1","14");
# 1 - is_a ; 13 - negatively_regulates; 14 - part_of; 15 - positively_regulates; 16 - regulates
 
my @goterm;
my @goterm2termarray;
my %gonr = ();
my %goterm2term = ();


my $filegoterm;
my $filegoterm2term;

sub import {
    
    $filegoterm = $_[0];
    $filegoterm2term = $_[1];
    
    
    open(GOTERM, "< $filegoterm") or die "could not open goterm file! ($filegoterm) \n";
    (@goterm) = <GOTERM>; # read file into list
    close(GOTERM);
    
    open(GOTERM2TERM, "< $filegoterm2term") or die "could not open goterm2term file! ($filegoterm2term)\n";
    (@goterm2termarray) = <GOTERM2TERM>; # read file into list
    close(GOTERM2TERM);
    
    
    # -- creat hash for Term2Nr --
    
    foreach my $go (@goterm) {
        
        my @tmp = split(/\t/,$go);
        $gonr{$tmp[3]} = $tmp[0];
    }
    
    # -- creat hash to find parents --
    
    foreach my $line (@goterm2termarray) {
        
        my @tmp = split(/\t/,$line);
	
	  my $relation = 0;
	  foreach my $rel(@GOrelationships) {
		if($tmp[1] eq $rel) {$relation = 1}
	  }

        if($relation) {    # only chosen relationships
            
            if (exists $goterm2term{$tmp[3]}) {     # if it has a relationship
                my @tmp2 = @{$goterm2term{$tmp[3]}};
                push(@tmp2,$tmp[2]);
                $goterm2term{$tmp[3]} = [@tmp2];
            } else {
                my @tmp2 = ();
                push(@tmp2,"$tmp[2]");
                $goterm2term{"$tmp[3]"} = [@tmp2];
            }
        }
    }
}



    
    
    






# -- functions --

sub goNR2TERM {
    
    my @tmp = split(/\t/,$goterm[$_[0]-1]);
    return $tmp[3];
}

sub goTERM2NR {
    
    return $gonr{$_[0]};
}

sub goFilter {
    
    my @goarray = @_;
    my @gofiltered = ();
    
    foreach my $go (@goarray) {
	my $nr = goTERM2NR($go);
	next unless $nr;

        my @split = split(/\t/,$goterm[$nr -1]);
        
        if($split[2] eq 'molecular_function' || $split[2] eq 'biological_process' || $split[2] eq 'cellular_component') {
            
            push(@gofiltered,$go);
        }
    }
    return @gofiltered;
}

sub getType {
	my $self = shift @_;
	my $term = shift @_;
	
	my $nr = goTERM2NR($term);
	return undef unless $nr;

	my @split = split(/\t/, $goterm[$nr -1]);
	return uc(substr($split[2], 0,1));
}

sub goAddPath {
    
    my @GO = @_;
    
    foreach my $go(@GO) {
        #my $go = substr($GO[$i],length($GOprefix));
        
        my @parents = ();
        my $nr = goTERM2NR($go);
	if(defined $nr && defined $goterm2term{ $nr } ) {
            @parents = @{$goterm2term{ goTERM2NR($go) }};
        }
        
        foreach my $parent(@parents) {
            $parent = goNR2TERM($parent);
            #$parent = $GOprefix.$parent;
            
            if('GO:' eq substr($parent,0,3)) {  # check that it is a GO nr
                
                my $new = 1;    # check that it is new
                foreach my $tmp(@GO) {
                    
                    if($parent eq $tmp) {
                        $new = 0;
                    }
                }
                if($new) {push(@GO,$parent)};
            }
        }
    }
    
    return keys %{{ map { $_ => 1 } @GO }}; # make sure no double
}

1;
