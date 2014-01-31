#!/usr/bin/perl

# Author: Yannis Velegrakis (velgias)
# Summer 2007
#
# It reads the nodes.dmp of the NCBI datase and converts it into an 
# XMLised table named Nodes. 

print "package tresc.benchmark.data;\n";
print "\n";
print "public class Values\n";
print "{\n";


print "\npublic static String[] Words = {\n";

while( <> ) # reads a line into input variable $_
{
    chomp($_);
    print "\t\"$_\",\n";
}

print "ferrari\n";

print "}\n";

print "}\n";

#while ($inputline)
#{
#    chomp($inputline);
#    print $inputline;
#}
