#!/usr/bin/env bash

scp $1 centos@$3:/tmp/$2  # We copy the file to the Spark master

pdsh -W $3 "sudo su -U hdfs hadoop fs -put /tmp/$2"  # We remotly execute the hadoop put

pdsh -W $3 "rm -rf /tmp/$2" # We clean up the remote temp file


