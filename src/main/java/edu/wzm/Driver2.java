package edu.wzm;

import edu.wzm.transform.RCFileToHFile;
import edu.wzm.utils.HiveTableUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat;
import org.apache.hadoop.hbase.mapreduce.SimpleTotalOrderPartitioner;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hive.hcatalog.common.HCatUtil;
import org.apache.hive.hcatalog.rcfile.RCFileMapReduceInputFormat;

import java.util.List;

/**
 * Created by GatsbyNewton on 2016/3/24.
 */
public class Driver2 extends Configured implements Tool{

    private static Configuration conf = new Configuration();
    private static Configuration hconf = null;
    private static HBaseAdmin hadmin = null;

    public static void connectHBase(){
        final String HBASE_CONFIG_ZOOKEEPER_CLIENT = "hbase.zookeeper.property.clientPort";
        final String HBASE_ZOOKEEPER_CLIENT_PORT = "2181";
        final String HBASE_CONFIG_ZOOKEEPER_QUORUM = "hbase.zookeeper.quorum";
        final String HBASE_ZOOKEEPER_SERVER = "hbase38,hbase43,hbase00";

        conf.set(HBASE_CONFIG_ZOOKEEPER_CLIENT, HBASE_ZOOKEEPER_CLIENT_PORT);
        conf.set(HBASE_CONFIG_ZOOKEEPER_QUORUM, HBASE_ZOOKEEPER_SERVER);
        hconf = HBaseConfiguration.create(conf);
        try{
            hadmin = new HBaseAdmin(hconf);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args)throws Exception{
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if(otherArgs.length != 4){
            System.err.println("Usage: <rcfile> <hfile> <schemafile> <hbasetable>");
            System.exit(1);
        }

        String path = System.getProperty("user.dir") + otherArgs[2];
        List<String> fieldNames = HiveTableUtils.getFieldName(path);
        StringBuilder sb = new StringBuilder(fieldNames.get(0));
        int size = fieldNames.size();
        for(int i = 1; i < size; i++){
            sb.append(":").append(fieldNames.get(i));
        }

        conf.set("schema", sb.toString());
		
		if(ToolRunner.run(conf, new Driver(), otherArgs) == 0){
			// Importing the generated HFiles into a HBase table
			LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
			loader.doBulkLoad(new Path(otherArgs[1]), new HTable(hconf ,otherArgs[3])）;
			System.exit(0);
		}
		else{
			System.exit(1);
		}
    }

    @SuppressWarnings("deprecation")
    @Override
    public int run(String[] strings) throws Exception {

        Configuration config = getConf();
        Driver.connectHBase();

        Job job = new Job(config, "RCFile to HFile");
        job.setJarByClass(Driver.class);
        job.setMapperClass(RCFileToHFile.ParseMapper.class);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(KeyValue.class);

        //Reduce's number is 0.
        job.setNumReduceTasks(0);

        job.setPartitionerClass(SimpleTotalOrderPartitioner.class);

        job.setInputFormatClass(RCFileMapReduceInputFormat.class);
//		job.setOutputFormatClass(HFileOutputFormat.class);

        HTable table = new HTable(config, strings[3]);
        HFileOutputFormat.configureIncrementalLoad(job, table);

        RCFileMapReduceInputFormat.addInputPath(job, new Path(strings[0]));
        FileOutputFormat.setOutputPath(job, new Path(strings[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }
}
