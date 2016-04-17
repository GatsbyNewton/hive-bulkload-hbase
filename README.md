# hive-bulkload-hbase
Import hive table into hbase as fast as possible.

#Directories
- bin: Contains the shell script that starts the program.
- src: Contains the source code and the test code.
- schema: Contains the schema file of a table.

#Compilation
```shell
$ mvn clean compile

$ mvn clean package

$ mvn assembly:assembly
```

#Description
HBase gives random read and write access to your big data, but getting your big data into HBase can be a challenge. And there are three methods to be able to make it.</br>

1. Use the API to put the data one by one.</br>
2. Hive Integrates HBase. And you can check [HBaseIntegration](https://cwiki.apache.org/confluence/display/Hive/HBaseIntegration) and [here](http://blog.csdn.net/u010376788/article/details/50905476) </br>
3. HBase comes with bulk load capabilities.</br>

However, the first two methods is slower than the last method that you simply bypassed the lot and created the HFiles yourself and copied them directly into the HDFS. The HBase bulk load process consists of two steps if Hive and HBase are on one cluster.</br>

1. HFile preparation via a MapReduce job.</br>
2. Importing the HFile into HBase using LoadIncrementalHFiles.doBulkLoad(eg. Driver2.java).</br>

But HBase bulk load process consists of three steps if Hive and HBase are on different cluster.</br>

1. HFile preparation via a MapReduce job.</br>
2. Copying HFile from Hive cluster to HBase cluster.</br>
3. Importing the HFile into HBase via HBase commands on HBase cluster.

#Usage
The aim of the MapReduce job is to generate HBase date files(HFile) from your input RCFile using HFileOutputFormat. Before you generate HFile, you should get Hive table's schema. And you can make use the following methods to get the schema.</br>
* Reading Hive metadata.
  * Using JDBC to gain from MysSQL
  * Using HCatalog to gain from MySQL
* Parsing a file that records the schema. In my opinion, it is more efficient than reading metadata, even if a table contains serveral thousands columns.</br>

Output from Mapper class are *ImmutableBytesWritable*, *KeyValue*. These classes are used by the subsequent partitioner and reducer to create the HFiles.</br>
There is no need to write your own reducer as the **HFileOutputFormat.configureIncrementalLoad()** as used in the driver code sets the correct reducer and partitioner up for you. </br>
Then, you should copy generated HFile from one cluster to another if Hive and HBase are on different cluster.
```shell
hadoop distcp hdfs://mycluster-hive/hfile/hbase hdfs://mycluster-hbase/hbase/test
```
Finally, import the File into HBase via HBase commands on HBase cluster.
```shell
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles /hbase/test hbase_table
```
Or import the File into HBase via Java code on HBase cluster(eg. Driver2.java).
```java
// Importing the generated HFiles into a HBase table
LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
loader.doBulkLoad(new Path(outputPath, htable);
```
