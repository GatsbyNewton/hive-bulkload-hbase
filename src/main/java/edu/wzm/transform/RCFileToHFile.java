package edu.wzm.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.wzm.utils.HiveTableUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.hive.serde2.columnar.BytesRefWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hive.hcatalog.rcfile.RCFileMapReduceInputFormat;

public class RCFileToHFile {
	
	public static class ParseMapper extends Mapper<LongWritable, BytesRefArrayWritable, ImmutableBytesWritable, KeyValue>{
//		private List<String> fieldName = null;
		private String[] fieldName = null;

		@Override
		protected void setup(Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
			Configuration conf = context.getConfiguration();
            
            String schema = conf.get("schema");
            fieldName = schema.split(":");

//			fieldName = new ArrayList<String>();
//			fieldName.add("id");
//			fieldName.add("name");
//			fieldName.add("age");
		}
		
		@Override
		protected void map(LongWritable key, BytesRefArrayWritable values,
				Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub

			Text line = new Text();
			List<String> fields = new ArrayList<String>();
			int size = values.size();
			for(int i = 0; i < size; i++){
				BytesRefWritable value = values.get(i);
				line.set(value.getData(), value.getStart(), value.getLength());
				fields.add(line.toString());
			}
			
			String rowKey = fields.get(0);
			String columnFamily = "cf";
			int length = fieldName.length;
			ImmutableBytesWritable hKey = new ImmutableBytesWritable();
			hKey.set(rowKey.getBytes());
			KeyValue kv = null;
			for(int i = 1; i < length; i++){
                kv = new KeyValue(hKey.get(), columnFamily.getBytes(), fieldName[i].getBytes(), fields.get(i).getBytes());
                context.write(hKey, kv);
			}
			
		}
	}

}
