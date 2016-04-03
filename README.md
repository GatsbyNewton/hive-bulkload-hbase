# hive-bulkload-hbase
Import hive table into hbase as fast as possible.
#Description
HBase gives random read and write access to your big data, but getting your big data into HBase can be a challenge. And there are three
methods to be able to make it.<br>
1 Use the API to put the data one by one.
2 Hive Integrates HBase.
3 HBase comes with bulk load capabilities.
However, the first two methods is slower than  if you simply bypassed the lot and created the HFiles yourself and copied them directly
into the HDFS. 
