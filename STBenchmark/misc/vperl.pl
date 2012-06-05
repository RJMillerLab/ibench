#!/cygdrive/c/Apps/Perl/bin/perl -w

#########################################################################
#                                                                       #
#  This first example shows how to create a twig, parse a file into it  #
#  get the root of the document, its children, access a specific child  #
#  and get the text of an element                                       #
#                                                                       #
#########################################################################

use strict;
use XML::Twig;

my $field= $ARGV[0] || 'lalakis';

my $twig= new XML::Twig;

$twig->parsefile( "smallSample.xml");    # build the twig
my $root= $twig->root; 

my @entries= $root->children;

# sort it on the text of the field
#my @sorted= sort {    $b->first_child( $field)->text 
#                  <=> $a->first_child( $field)->text }
#            @players;

print '<?xml version="1.0"?>';   # print the XML declaration

foreach my $entry (@entries)     # the sorted list 
{ 
    # get the 1st child named protein
    my $protein  = $entry->first_child('protein'); 

    # $child->paste( $parent) puts a new element  as a 1st child under parent
    # you can give the first argument to be last)chile before, or after
    
    # Create an element 
    # my $eblg= new XML::Twig::Elt( 'blg', $blg);   # create the element
        
    $protein->print;
    print "\n"; 
 }
