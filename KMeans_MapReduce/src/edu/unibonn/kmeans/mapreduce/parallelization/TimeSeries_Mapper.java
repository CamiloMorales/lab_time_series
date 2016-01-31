package edu.unibonn.kmeans.mapreduce.parallelization;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Mapper;

public class TimeSeries_Mapper  extends Mapper<TimeSeries_nd_Centroid, TimeSeries_nd_Point, TimeSeries_nd_Centroid, TimeSeries_nd_Point> 
{
	private final ArrayList<TimeSeries_nd_Centroid> centroids_list = new ArrayList<TimeSeries_nd_Centroid>();

	@Override
	protected void setup(Context context) throws IOException, InterruptedException
	{
		super.setup(context);
		
		Configuration conf = context.getConfiguration();

		int dimensionality = Integer.valueOf(conf.get("num.dimensionality"));
		
		Path centroids = new Path(conf.get("centroids.path"));
		FileSystem fs = FileSystem.get(conf);
		
		try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, centroids, conf))
		{
			TimeSeries_nd_Centroid key = new TimeSeries_nd_Centroid(dimensionality);

			while (reader.next(key, new IntWritable()))
			{
				TimeSeries_nd_Centroid current_centroid = new TimeSeries_nd_Centroid(key);
				current_centroid.setCluster_id(key.getCluster_id());
				centroids_list.add(current_centroid);
			}
		}
	}
	
	@Override
	protected void map(TimeSeries_nd_Centroid key, TimeSeries_nd_Point value, Context context) throws IOException, InterruptedException
	{
		double current_closest_distance = Double.POSITIVE_INFINITY;
		double current_closest_cluster_id = -1;
		int current_closest_cluster_index = -1;

		for (int j = 0; j < centroids_list.size(); j++) //Iterate over all the clusters.
		{
			TimeSeries_nd_Centroid current_centroid = centroids_list.get(j);
			
			double actual_distance = current_centroid.euclidean_distance_to(value);
			//double actual_distance = clusters.get(j).Dynamic_Time_Warping_distance_to(specific_day.get(i));
			
			if(actual_distance < current_closest_distance)
			{
				current_closest_distance = actual_distance;
				current_closest_cluster_id = current_centroid.getCluster_id();
				current_closest_cluster_index = j;
			}
		}
		
		context.write(centroids_list.get(current_closest_cluster_index), value);
	}
}
