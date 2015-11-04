#!/usr/bin/env bash

#We trigger the Spark Job
spark-submit --py-files CrossValidation.zip --master yarn-client  --num-executors 2 --driver-memory 1024m  --executor-memory 512m   --executor-cores 1 $1 $2 $3 $4 $5 $6 $7 $8 $9 &
