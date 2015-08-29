#!/usr/bin/env bash

scp $0 centos@$1:/tmp/$1  # We copy the file to the Spark master

pdsh -W $2 "sudo su -U hdfs hadoop fs -put /tmp/"+ $1  # We remotly execute the hadoop put

pdsh -W $2 "rm -rf /tmp/"+ $1 # We clean up the remote temp file

