#!/usr/bin/env bash

args=("$@")
ZIP_FILE_NAME=${args[0]}
FILES=""

for i in $(seq 1 $#)
 do
  FILES+=" ${args[$i]}"
 done

cd /tmp
zip $ZIP_FILE_NAME $FILES