#!/bin/bash

# Bash Script Invocation: nohup ./runExp4.sh N 
# where N is the number of desired iterations per Configuration File

today=$(date +%b%d-%H:%M)
echo "iBench - Scalability Experiment 4 on $today"

i=1

# Iterate over all directories
for dir in ~/iBench/scalability/exp4-TargetReuse/*
do

  if [ -f $dir ]; then
     continue
  fi

  echo "Processing directory $dir"

  shopt -s nullglob
  
  # Iterate over all config files in $dir
  for ConfigFullPath in $dir/config*.txt
  do
  
  # grab Config name and assembly desired outpath
  filename="${ConfigFullPath##*/}"
  base="${filename%.[^.]*}"
  
  outpath=$dir/run-$today-$base/

  echo " $((i++)) - Run with Config $filename and Out Path $outpath"

  # Invoke Linearization with this template config
  if [ $# -eq 0 ]; then 
    #echo "Call iBench with -i1 -f -c $ConfigFullPath -p $outpath"
    ./iBench.sh -i 1 -f -c $ConfigFullPath -p $outpath
  else
    #echo "Call iBench with -i $1 -c $ConfigFullPath -p $outpath"
    ./iBench.sh -i $1 -f -c $ConfigFullPath -p $outpath
  fi

  # done internal for-loop
  done

# done external for-loop
done

now=$(date +%b%d-%H:%M)

echo "iBench - Scalability Experiment 4 - STARTED on $today DONE on $now"


