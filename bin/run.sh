#!/usr/bin/env bash

# Transform RCFile into HFile.
# param:
# 	RCFile's input path
#	generated HFile's path
#	schema file's local path
#	hbase table's name
hadoop jar hfile-1.0-SNAPSHOT-jar-with-dependencies.jar edu.wzm.Driver  \
/rcfile/test    \
/hfile/hbase  \
/schema.hql   \
hbase_table

# Distributed copy HFile to mycluster-hbase.
hadoop distcp hdfs://mycluster-hive/hfile/hbase hdfs://mycluster-hbase/hbase/test

# BulkLoad HFile into hbase table on mycluster-hbase.
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles /hbase/test hbase_table