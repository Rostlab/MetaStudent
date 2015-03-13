#!/usr/bin/perl
# AUTHOR: Peter Hoenigschmid

use strict;
use warnings;
#use diagnostics;
no warnings 'deprecated';
use Data::Dumper;

use GO::IO::Dotty;

###!------------------------------------------------------------------
###!Usage:
###!
###!Split a xml-obo GO-tree file into two seperate ones (MFO, BPO)
###!treehandler.pl -i GRAPHFILE -split -d OUTPUTDIR [OPTIONS]
###!
###!Use a target blast output to predict GO terms
###!treenhandler.pl -mfo MFOTREE -bpo BPOTREE -pred BLASTFILE -method 1|2|3 [OPTIONS]
###!
###!OPTIONS
###!-log LEVEL : Level of logoutput
###!-o FILE : output into a file
###!------------------------------------------------------------------

use GO::Parser;
use GO::Model::Graph;

my $log = 0;

# print message according to given log level
# params: needed loglevel, message 
sub logprint {
    my $lvl = shift;
    my $msg = shift;
    if ( $log >= $lvl ) {
        print $msg;
    }
}

# parse command line parameters
# params: parameter array
sub parse_opts {
    my %opts;
    my $prev;
    foreach (@_) {
        if (/^-/) {
            $opts{$_} = '';
            $prev = $_;
        }
        else {
            $opts{$prev} = $_;
            $prev = '';
        }
    }
    %opts;
}

# read the perlfile and print every line starting with ###!
sub print_usage {
    my $usage = '';
    open FH, "< $0" or die("Something weird happened...");
    while (<FH>) {
        $usage .= substr( $_, 4 ) if substr( $_, 0, 4 ) eq '###!';
    }
    close FH;
    print $usage;
}

# create the graph
# param: filename
sub create_graph {
    my $file = shift;
    my %graph = ();
    open( FHE, $file ) or die "Error: $!\n";
    while( <FHE> )
    {
    	my @line = split( /\s+/, $_ );
    	push( @{$graph{$line[0]}}, $line[1] );
    }
    close FHE;
    return \%graph;
}

# write a graph to the given file
# params: filename, graph ref
sub write_graph {
    my $file      = shift;
    my $graph_ref = shift;

    my $p = GO::Parser->new(
        { format => "GO::Parsers::obj_emitter", handler => 'obo_xml' } );
    $p->handler->file($file);
    $p->emit_graph($$graph_ref);
}

# get the parents in the given graph to the given acc number
# params: graph ref, acc number
sub get_parents {
    my $graph = shift;
    my $acc   = shift;
    my @result = @{$graph->{$acc}};

    return (@result, $acc);
}

# Returns a shortened float value (2 digits)
sub sf {
    my $val = shift;
    sprintf( "%.2f", $val );
}

# returns minimum of n values
sub min {
    return (sort({$a<=>$b} @_))[0];
}

# returns maximum of n values
sub max {
	my ($val1, $val2) = @_;
    return (sort({$b<=>$a} @_))[0];
}

# calculates confidence and prints it as the second value. first value is the GO acc. other values are not interesting for the user.
# params
sub print_confidence {
    my $graph = ${shift(@_)};
    my $method = shift;
    my @input = @{shift(@_)};
    my $precs = shift;
    my $recs = shift;
    my $scoring = shift;

    my %go2direct_pred;
    my %go2single_score;
    my %go2single_support;

    my %go2score;
    my %go2support;

    my $targetname;
    my %targetgos;

    # stop if target has no blast hits
    if (@input < 2)
    {
        return 0;
    }

    foreach (@input) 
    {
        if (/^Target: (.*)/) # match target names, and recognize GO terms if any 
        { 
		    $targetname = $1;
            if ($targetname =~ m/GO:/) 
            {
                foreach (split(',', $targetname)) 
                {
                    if (exists $$graph{$_}) 
                    {
                        $targetgos{$_} = 1;
                    }
                }

                # get parents of target GO terms
                foreach (keys(%targetgos)) 
                {
                    if (exists $$graph{$_}) 
                    {
                        foreach (get_parents($graph, $_)) 
                        {
                            $targetgos{$_->acc} = 1;
                        }
                    }
                }
            }
        }
        else 
        {
            my @sp = split('\s+|\t+');
            my @gos = split( ',', $sp[5] );
    
	    
            my @scrs = split( '/', $sp[3] );
            my @idents = split('/', $sp[2]);
		    my $scr = 0;
		    if ($scoring == '0')
		    {
		       $scr = $scrs[0] / $scrs[1];
		    }
		    else
		    {
		      $scr = $idents[0] / $idents[1];
		    }

            foreach (@gos) 
            {
                # check if node is present in the current tree, update positive score and support for each direct hit
                if ( exists $graph->{$_} ) 
                {
                    $go2single_score{$_} = $scr if (not $go2single_score{$_} or $scr > $go2single_score{$_});
                    $go2single_support{$_}++;
                    $go2direct_pred{$_} = $_ if (not $go2direct_pred{$_});
                }
                else 
                {
                    logprint(1, "info: $_ not found in current tree.\n");
                }
            }

            # stop if method is one (only first blast hit)
            last if ($method == 1);
        }
    }

    # get parents for direct hits and update their support
    foreach my $direct_pred (keys(%go2direct_pred)) {
            my %current_parents;
            foreach ( get_parents( $graph, $direct_pred ) ) {
                $current_parents{ $_ }++;
            }

            foreach ( keys %current_parents ) {
                # if method 2 is used, every node gets the maximal support from their direct hits 
                # for method 1 and 3 its the cumulative support
                if ($method == 2) {
                    if ($go2support{$_}) {
                        $go2support{$_} = max($go2support{$_}, $go2single_support{$direct_pred});
                    }
                    else {
                        $go2support{$_} = $go2single_support{$direct_pred};
                    }
                }
                else {
                    $go2support{$_} += $go2single_support{$direct_pred};
                }
            }
    }

    if (keys(%go2support) < 1) {
        return 0;
    }

    # calculate maximum support (excluding the root node)
    my $maxsupport = $go2support{(sort({$go2support{$b} <=> $go2support{$a}} keys(%go2support)))[1]};

    # update scores for the parents of the direct hits according to their support and the maximum positives score
    foreach my $direct_pred (keys(%go2direct_pred)) {

            my %current_parents;
            foreach ( get_parents( $graph, $direct_pred ) ) {
                $current_parents{ $_ }++;
            }

            foreach (keys %current_parents) {
                # use alignment positives or not according to the method
                if ($method == 2) {
                    $go2score{$_} += ($go2single_support{$direct_pred} / $maxsupport); 
                }
                else {
                    $go2score{$_} += $go2single_score{$direct_pred} * ($go2single_support{$direct_pred} / $maxsupport); 
                }
            }
    }

    # calculate precision and recall
    my $max_score = 0;
    foreach (keys(%go2direct_pred)) {
        $max_score = max($go2score{$_}, $max_score); 
    }

    my %max_preds;
    foreach (keys(%go2direct_pred)) {
        if ($go2score{$_} == $max_score) { 
            $max_preds{$_} = 1;
        }
    }

    foreach (keys(%max_preds)) {
        if (exists $$graph{$_}) {
            foreach (get_parents($graph, $_)) {
                $max_preds{$_} = 1;
            }
        }
    }

    if (%targetgos) {
    
        my $tp = 0; # true positives
        my $fp = 0; # false positives
        my $fn = 0; # false negatives
    
        foreach (keys(%max_preds)) {
            $targetgos{$_} += 2;
        }
            
        foreach (keys(%targetgos)) {
            if ($targetgos{$_} == 1) {
                $fn++;
            }
            elsif ($targetgos{$_} == 2) {
                $fp++;
            } 
            elsif ($targetgos{$_} == 3) {
                $tp++;
            }
        }
    
        # calc prec and recall and push them to the arrays
        my $precision = $tp / ($tp + $fp); 
        my $recall = $tp / ($tp + $fn);

        push @$precs, $precision;
        push @$recs, $recall;
    }

    # print results in the CAFA supported format (into the result array)
    my @result;
    foreach (sort {$go2score{$b} <=> $go2score{$a}} keys (%go2direct_pred)) {
        push(@result, $targetname . " "
          . $_ . " "
          . sf( min($go2score{$_},1) ) . " "
          . "\n");
    }

    return @result;
}

##############################################################################
# Parse parameters, check for file, create graph #############################
##############################################################################

my %opts    = parse_opts('-method', 3, @ARGV);

my $file;
my $graph;
my $file_mfo;
my $file_bpo;
my $file_cco;
my $graph_mfo;
my $graph_bpo;
my $graph_cco;


# read graph(s)
if ($opts{'-i'}) {
    $file = $opts{'-i'} or die(print_usage);
    $graph   = create_graph($file);
}
if ($opts{'-mfo'} and $opts{'-bpo'}) {
    $file_mfo    = $opts{'-mfo'} or die(print_usage);
    $file_bpo    = $opts{'-bpo'} or die(print_usage);
    $file_cco    = $opts{'-cco'} or die(print_usage);
    
    print STDERR "parsing tree $file_mfo (1/1)...";
    $graph_mfo   = create_graph($file_mfo);

    print STDERR "done\n";
#    print STDERR "parsing tree $file_bpo (2/2)...";
#    $graph_bpo   = create_graph($file_bpo);
    print STDERR "done\n";
}

$log = $opts{'-log'} if ( exists $opts{'-log'} );
if (exists $opts{'-o'})
{
    open(STDOUT, "> ".$opts{'-o'});
}

##############################################################################
# Methods ####################################################################
##############################################################################

if ( defined $opts{'-split'} and exists $opts{'-d'} ) {
    my $out_dir = $opts{'-d'};

    my $bpo_sub = $graph;
    write_graph( "$out_dir/" . get_raw_filename($file) . ".bpo.obo-xml", \$bpo_sub );

    my $mfo_sub = $graph;
    write_graph( "$out_dir/" . get_raw_filename($file) . ".mfo.obo-xml", \$mfo_sub );
}
elsif ( $opts{'-pred'} ) {
    my %go2direct_pred;
    my %go2single_score;
    my %go2single_support;

    # temporary saving of precisions and recalls
    my @precs;
    my @recs;

    my @input;
    my @output = ();

    open( FH, "< " . $opts{'-pred'} ) or die("Could not find file $opts{'-pred'}\n");

    # print header
    print "AUTHOR GROUP_C\n";
    print "MODEL 1\n";
    print "KEYWORDS sequence alignments\n";

    # run confidence calculation
    while (<FH>) {
        chomp;
        if (/^\*\*\*/) {
            my @mfo_res = print_confidence(\$graph_mfo, $opts{'-method'}, \@input, \@precs, \@recs, $opts{'-scoring'});
#            my @bpo_res = print_confidence(\$graph_bpo, $opts{'-method'}, \@input, \@precs, \@recs, $opts{'-scoring'});
            
            push(@output, @mfo_res) if (@mfo_res);
#            push(@output, @bpo_res) if (@bpo_res);

            # clear input
            @input = ();
        }
        elsif (length($_) > 0) {
            push @input, $_;
        }
    }
    close(FH);
 
    # caluclate precision and recalls if any (@precs and @recs are filled in the confidence method)
    if (@precs or @recs) {
        my $pre;
        my $rec;
        foreach (@precs) {
            $pre += $_;
        }
        $pre /= @precs;
        foreach (@recs) {
            $rec += $_;
        }
        $rec /= @recs;
        print 'ACCURACY PR='.sf($pre).'; RC='.sf($rec)."\n";
    }
    
    # print the output
    foreach (@output) {
        if ($_) {
            print;
        }
    }

    print "END\n";
}
else {
    print_usage;
}
