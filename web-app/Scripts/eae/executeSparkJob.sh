#!/usr/bin/env bash

args=("$@")
STR=""
for i in $(seq 1 $#)
 do
  STR+=" ${args[$i]}"
 done

#We trigger the Spark Job
pdsh -w ssh:centos@${args[0]} "spark-submit --py-files CrossValidation.zip --master yarn-client  --num-executors 2 --driver-memory 1024m  --executor-memory 512m   --executor-cores 1 $STR" &
