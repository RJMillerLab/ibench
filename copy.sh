#!/bin/sh

# step 1
cp out0/Schema*xml ~/millercode/trampExGen/branches/2525-Mehrnaz

# step 2
cp ~/millercode/Clio_MapMerge/branches/Radu/com.ibm.clio.gui/S*sql ~/millercode/trampExGen/branches/2525-Mehrnaz

# retrieve the name of the Schemas...xml file to add as parameter in TrampExGen
ls ~/millercode/trampExGen/branches/2525-Mehrnaz/Schemas*
