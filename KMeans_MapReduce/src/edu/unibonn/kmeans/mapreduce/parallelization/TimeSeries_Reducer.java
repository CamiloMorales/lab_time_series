package edu.unibonn.kmeans.mapreduce.parallelization;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Reducer;

import edu.unibonn.kmeans.mapreduce.main.KMeans_MR_clustering;

public class TimeSeries_Reducer extends Reducer<TimeSeries_nd_Centroid, TimeSeries_nd_Point, TimeSeries_nd_Centroid, TimeSeries_nd_Point>
{
	//In case more than centroid per reducer. 
	//For the preparations of the next iterations in the CleanUp.
	private final ArrayList<TimeSeries_nd_Centroid> new_centroids_list = new ArrayList<TimeSeries_nd_Centroid>();

	@Override
	protected void reduce(TimeSeries_nd_Centroid key, Iterable<TimeSeries_nd_Point> values, Context context) throws IOException, InterruptedException
	{
		super.setup(context);
		
		Configuration conf = context.getConfiguration();
		int dimensionality = Integer.valueOf(conf.get("num.dimensionality"));
		
		ArrayList<TimeSeries_nd_Point> members = new ArrayList<TimeSeries_nd_Point>();
		
		double[] new_center_of_mass = new double[dimensionality];
		
		for (TimeSeries_nd_Point current_point : values)
		{
			TimeSeries_nd_Point current = current_point.create_copy();
			
			members.add(current); //For writing later.
			
			double[] current_measurements = current_point.getMeasurements();
			
			for (int i = 0; i < dimensionality; i++)
			{
				new_center_of_mass[i] = new_center_of_mass[i] + current_measurements[i];
			}
		}
		
		double number_of_members = members.size();
		
		for (int i = 0; i < dimensionality; i++)
		{
			new_center_of_mass[i] = new_center_of_mass[i]/number_of_members;
		}
		
		TimeSeries_nd_Centroid new_centroid = new TimeSeries_nd_Centroid(new_center_of_mass);
		new_centroid.setCluster_id(key.getCluster_id()); 
		
		new_centroids_list.add(new_centroid);
		
		for (int i = 0; i < members.size(); i++)
		{
			TimeSeries_nd_Point current_member = members.get(i);
			context.write(new_centroid, current_member);
		}
		
		boolean centroid_changed = false;
		
		double[] previous_centroid = key.getCenter_of_mass();
		
		for (int i = 0; i < dimensionality; i++)
		{
			if(new Double(new_center_of_mass[i]).floatValue() != new Double(previous_centroid[i]).floatValue())
			{
				centroid_changed = true;
				break;
			}
		}
		
		if (centroid_changed)
		{
			context.getCounter(KMeans_MR_clustering.Global_Hadoop_Counter.NO_CHANGE).increment(1);
		}			
	}
	
	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException
	{
		//Update the centroids in the sequence file with the new centroids.
		
		//First delete the previous centroids.
		super.cleanup(context);
		Configuration conf = context.getConfiguration();
		Path outPath = new Path(conf.get("centroids.path"));
		FileSystem fs = FileSystem.get(conf);
		fs.delete(outPath, true);
		
		//Now, crete (write) the new centroids that will be the input for the next iteration (if needed).
		
		try (SequenceFile.Writer out = SequenceFile.createWriter(fs, context.getConfiguration(), outPath, TimeSeries_nd_Centroid.class, IntWritable.class)) 
		{
			for (TimeSeries_nd_Centroid current_centroid : new_centroids_list)
			{
				out.append(current_centroid, new IntWritable(0));
				//System.out.println("New centroid: "+current_centroid.toString());
			}
		}
	}
}
