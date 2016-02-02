//IMPORTANT: This project is a adaptation of the code in: http://codingwiththomas.blogspot.de/2011/05/k-means-clustering-with-mapreduce.html
package edu.unibonn.kmeans.mapreduce.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import au.com.bytecode.opencsv.CSVReader;
import edu.unibonn.kmeans.mapreduce.parallelization.TimeSeries_Mapper;
import edu.unibonn.kmeans.mapreduce.parallelization.TimeSeries_Reducer;
import edu.unibonn.kmeans.mapreduce.parallelization.TimeSeries_nd_Centroid;
import edu.unibonn.kmeans.mapreduce.parallelization.TimeSeries_nd_Point;
import edu.unibonn.kmeans.mapreduce.plotting.TimeSeriesPlotter_KMeans;
import edu.unibonn.kmeans.mapreduce.utils.Cluster_KMeans;
import edu.unibonn.kmeans.mapreduce.utils.Day_24d;
import edu.unibonn.kmeans.mapreduce.utils.Measurement;
import edu.unibonn.kmeans.mapreduce.utils.Sensor;

import java.time.LocalDate;

public class KMeans_MR_clustering
{
	public static enum Global_Hadoop_Counter { NO_CHANGE }
	
	public static void main(String[] args) throws Exception 
	{
		JobConf conf = new JobConf(KMeans_MR_clustering.class);
		//Configuration conf = new Configuration();
		
		conf.setJar("kmeans_mr.jar");
		
		int dimensionality = 24;
		conf.set("num.dimensionality", String.valueOf(dimensionality));
		
		//Initial interation
		int global_iteration = 1;
		conf.set("num.iteration", String.valueOf(global_iteration));
		
		Path times_series_nd_points = new Path("files/clustering/import/time_series_data");
		Path times_series_nd_centroids = new Path("files/clustering/import/center/centroids.seq");
		
		conf.set("centroids.path", times_series_nd_centroids.toString());
		Path out = new Path("files/clustering/depth_1");
		
		Job job = Job.getInstance(conf);
		job.setJobName("KMeans MapReduce Clustering");
		
		job.setMapperClass(TimeSeries_Mapper.class);
		job.setReducerClass(TimeSeries_Reducer.class);
		job.setJarByClass(TimeSeries_Mapper.class);
		
		FileInputFormat.addInputPath(job, times_series_nd_points);
		FileSystem fs = FileSystem.get(conf);
		
		if (fs.exists(out))
		{
			fs.delete(out, true);
		}

		if (fs.exists(times_series_nd_centroids))
		{
			fs.delete(out, true);
		}

		if (fs.exists(times_series_nd_points))
		{
			fs.delete(times_series_nd_points, true);
		}
		
		
		//String pathCSV = "input_data/real_data_time_series_Time_vs_Sensors.csv";
		String pathCSV = "input_data/generation_600000_1454392454986.csv";
		
		boolean normalized = true;
		
		ArrayList<Sensor> sensors = loadDataCSV(pathCSV, normalized);
		
		ArrayList<Day_24d> points_24d = new ArrayList<Day_24d>();
		
		double percentaje = 0;
		//Generate the 24d points
		for (int i = 0 ; i < sensors.size(); i++)
		{
			ArrayList<Day_24d> current_points_24d = sensors.get(i).generate_24d_points();
			points_24d.addAll(current_points_24d);
			
			percentaje = (i+1.0)*100/sensors.size();
        	System.out.println("Generating 24d points: "+ percentaje );
		}
		
		//ArrayList<Day_24d> only_montags = filter_montags(points_24d);
		ArrayList<Day_24d> specific_day_data = filter_day(points_24d, DayOfWeek.MONDAY);
		
		initializeData(conf, times_series_nd_points, fs, specific_day_data);
		initializeCentroidsRandomly(conf, times_series_nd_centroids, fs, specific_day_data, 20);
		
		FileOutputFormat.setOutputPath(job, out);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setOutputKeyClass(TimeSeries_nd_Centroid.class);
		job.setOutputValueClass(TimeSeries_nd_Point.class);

		int number_of_reducers=3;
		//job.setNumReduceTasks(number_of_reducers);
		
		long TOTAL_initial_time = System.currentTimeMillis();
		
		long initial_time = System.currentTimeMillis();
		job.waitForCompletion(true);	
		long final_time = System.currentTimeMillis();		
		System.out.println("Iteration "+global_iteration+"took: "+((double)(final_time-initial_time)/1000));
		
		long changed_counter = job.getCounters().findCounter(Global_Hadoop_Counter.NO_CHANGE).getValue();
		global_iteration++;
		
		while (changed_counter > 0) //At least 1 centroid have change with respect to the previous iteration.
		{
			//conf = new Configuration();
			
			conf = new JobConf(KMeans_MR_clustering.class);
			conf.setJar("kmeans_mr.jar");
			
			conf.set("num.dimensionality", String.valueOf(dimensionality));
			conf.set("centroids.path", times_series_nd_centroids.toString());
			conf.set("num.iteration", String.valueOf(global_iteration));
			job = Job.getInstance(conf);
			job.setJobName("KMeans MapReduce Clustering "+ global_iteration);

			job.setMapperClass(TimeSeries_Mapper.class);
			job.setReducerClass(TimeSeries_Reducer.class);
			job.setJarByClass(TimeSeries_Mapper.class);

			times_series_nd_points = new Path("files/clustering/depth_" + (global_iteration - 1) + "/");
			out = new Path("files/clustering/depth_" + global_iteration);

			FileInputFormat.addInputPath(job, times_series_nd_points);
			
			if (fs.exists(out))
			{
				fs.delete(out, true);
			}
				
			FileOutputFormat.setOutputPath(job, out);
			
			job.setInputFormatClass(SequenceFileInputFormat.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			
			job.setOutputKeyClass(TimeSeries_nd_Centroid.class);
			job.setOutputValueClass(TimeSeries_nd_Point.class);
			
			
			initial_time = System.currentTimeMillis();
			//job.setNumReduceTasks(number_of_reducers);
			job.waitForCompletion(true);
			final_time = System.currentTimeMillis();		
			System.out.println("Iteration "+global_iteration+"took: "+((double)(final_time-initial_time)/1000));
			
			global_iteration++;
			changed_counter = job.getCounters().findCounter(Global_Hadoop_Counter.NO_CHANGE).getValue();
		}
		
		long TOTAL_final_time = System.currentTimeMillis();		
		System.out.println("GLOBAL clustering took: "+((double)(TOTAL_final_time-TOTAL_initial_time)/1000));
		
		Path result = new Path("files/clustering/depth_" + (global_iteration-1) + "/");

		FileStatus[] final_results = fs.listStatus(result);
		
		ArrayList<Cluster_KMeans> clusters = new ArrayList<Cluster_KMeans>();

		for (FileStatus status : final_results)
		{
			if (!status.isDir())
			{
				Path path = status.getPath();
				if (!path.getName().equals("_SUCCESS"))
				{
					try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf))
					{
						TimeSeries_nd_Centroid key = new TimeSeries_nd_Centroid();
						TimeSeries_nd_Point v = new TimeSeries_nd_Point();
						
						//System.out.println("1111");
						
						while (reader.next(key, v))
						{	
							//System.out.println("22222");
							
							Day_24d current_point = new Day_24d(v.getSensor_id(), LocalDate.now(), v.getMeasurements());
							
							double current_cluster_id = key.getCluster_id();
							
							boolean cluster_exist = false;
							
							for (int i = 0; i < clusters.size(); i++)
							{
								if(current_cluster_id == Double.valueOf(clusters.get(i).getCluster_id()))
								{
									cluster_exist = true;							
									clusters.get(i).addMembership(current_point);
								}
							}
							
							if(!cluster_exist)
							{
								Cluster_KMeans new_centroid = new Cluster_KMeans(String.valueOf(current_cluster_id));
								new_centroid.addMembership(current_point);
								
								clusters.add(new_centroid);
							}
							
									
							String centroid = key.toString();
							String point = v.toString();
							System.out.println(centroid + " / " + point);

						}
						
						for (int i = 0; i < clusters.size(); i++)
						{
							clusters.get(i).recalculatePositionOfCentroid();
						}
					}
				}
			}
		}
		
		//plot(clusters);
	}

	private static void plot(ArrayList<Cluster_KMeans> clusters) 
	{
		//System.out.println("NUmber of clusters:"+ clusters.size());
		
		String separate_clustering_id_ = new String();
        
        for (int j = 0; j < clusters.size(); j++)
        {
        	if(clusters.get(j).getMembership().size() > 0)
        	{
        		separate_clustering_id_ = "KMeans_only_cluster_"+clusters.get(j).getCluster_id()+"has_"+clusters.get(j).getMembership().size()+"members_("+((float)clusters.get(j).getMembership().size()*100)/821+"%)";
            	
            	ArrayList<Cluster_KMeans> current_cluster = new ArrayList<Cluster_KMeans>();
            	current_cluster.add(clusters.get(j));
            	
            	LocalDateTime from = LocalDateTime.now();
            	final TimeSeriesPlotter_KMeans demo_2 = new TimeSeriesPlotter_KMeans(separate_clustering_id_, current_cluster, from);
                //demo_2.pack();
                //RefineryUtilities.centerFrameOnScreen(demo_2);
                //demo_2.setVisible(true);
        	}                	
		}
	}

	private static void initializeData(Configuration conf, Path times_series_nd_points, FileSystem fs, ArrayList<Day_24d> specific_day) throws Exception
	{
		try (SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs, conf, times_series_nd_points, TimeSeries_nd_Centroid.class, TimeSeries_nd_Point.class)) 
		{
			double percentaje = 0;
			
			for (int i = 0; i < specific_day.size(); i++)
			{
				Day_24d current_sensor_data = specific_day.get(i);
				dataWriter.append(new TimeSeries_nd_Centroid(), new TimeSeries_nd_Point(current_sensor_data));
				
				percentaje = (i+1.0)*100/specific_day.size();
            	System.out.println("Writing file: "+ percentaje );
			}
		}
	}

	private static void initializeCentroidsRandomly(Configuration conf, Path times_series_nd_centroids, FileSystem fs, ArrayList<Day_24d> specific_day_data, int k) throws Exception
	{
		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs, conf, times_series_nd_centroids, TimeSeries_nd_Centroid.class, IntWritable.class)) 
		{
			ArrayList<Integer> previous_random_ints = new ArrayList<Integer>();
			
			int count_repetitions = 0;
			
			Random r = new Random();
			
			double percentaje = 0;
			
			for (int i = 0; i < k; i++)
			{
				int curr_rand = r.nextInt(specific_day_data.size());
				
				curr_rand = i * 5; //For testing.
				
				if(!previous_random_ints.contains(curr_rand))
				{
					previous_random_ints.add(curr_rand);
					//random_centroids.add(new Cluster_KMeans(Integer.valueOf(i).toString(),only_montags.get(curr_rand)));	
					
					centerWriter.append(new TimeSeries_nd_Centroid(i, specific_day_data.get(curr_rand)), new IntWritable(0));
					
					//System.out.println("Appended Centroid: "+specific_day_data.get(curr_rand).getId());
				}
				else
				{
					System.out.println("-REPETING RANDOM NUMBER PICKING!");
					i--;
					count_repetitions++;
					
					if(count_repetitions>10)
					{
						System.out.println("TOO MANY RANDOM NUMBER GENERATIONS.");
						throw new Exception("TOO MANY RANDOM NUMBER GENERATIONS.");
					}
				}
				
				percentaje = (i+1.0)*100/k;
            	System.out.println("InitializeCentroidsRandomly: "+ percentaje );
			}
		}
	}
	
	private static ArrayList<Day_24d> filter_day(ArrayList<Day_24d> points_24d, DayOfWeek day)
	{
		ArrayList<Day_24d> specific_day = new ArrayList<Day_24d>();
		
		double percentaje = 0;
		//Generate the 24d points
		for (int i = 0 ; i < points_24d.size(); i++)
		{
			Day_24d current_point_24d = points_24d.get(i);
			
			if(current_point_24d.getDay().getDayOfWeek().equals(day))
			{
				specific_day.add(current_point_24d);
			}
			
			percentaje = (i+1.0)*100/points_24d.size();
        	System.out.println("Filter_day: "+ percentaje );
		}
		
		return specific_day;
	}
	
	private static ArrayList<Sensor> loadDataCSV(String dataFilePath, boolean normalized)
	{
		CSVReader reader = null;
		ArrayList<Sensor> return_matrix = new ArrayList<Sensor>();
		
		try
		{		
			reader = new CSVReader(new FileReader(dataFilePath), ';');
	
			List<String[]> pre_matrix = new ArrayList<String[]>();//reader.readAll();
			
			try(BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) 
			{
			    int counter_i = 0;
				for(String line; (line = br.readLine()) != null; )
			    {
			    	String[] current = line.split(";");
			    	pre_matrix.add(current);
			    	
			    	double percentaje = (counter_i+1.0)*100/600000;
	            	System.out.println("Reading file: "+ percentaje );
			    	counter_i++;
			    }
			}

			int rows = pre_matrix.size();
			int columns = pre_matrix.get(0).length;
			
			String[] times = pre_matrix.get(0);

			for (int i = 1; i < rows; i++) 
			{		
				String[] actual_row = pre_matrix.get(i);

				Sensor current_sensor = new Sensor(actual_row[0]);
				
				if(!normalized)
				{
					for (int j = 1; j < columns; j++)
					{
						double current_erlang = Double.valueOf(actual_row[j].replace(",", "."));
						LocalDateTime current_record_time = LocalDateTime.parse(times[j], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
						current_sensor.addMeasurement(new Measurement(current_record_time, current_erlang));
					}
				}
				else
				{
					//NORMALIZE
					//1. Create a 24dim double vector with the absolute values.
					//2. Find the highest value, that is going to be 100.
					//3. Escalate all other values between 0 and 1.
					
					double[] absolute_values = new double[24];
					double current_max = 0;
					
					for (int j = 0; j < columns-1; j++)
					{
						double current_erlang = Double.valueOf(actual_row[j+1].replace(",", "."));
						absolute_values[j] = current_erlang;
						
						if(current_max < current_erlang)
						{
							current_max = current_erlang;
						}
					}
					
					//Because of sensor: DXB971A (and maybe more).
					if(current_max > 0)
					{
						for (int j = 0; j < absolute_values.length; j++)
						{
							double current_erlang_normalized = (absolute_values[j]*100)/current_max;
							LocalDateTime current_record_time = LocalDateTime.parse(times[j+1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
							current_sensor.addMeasurement(new Measurement(current_record_time, current_erlang_normalized));
						}
					}
					else if(current_max == 0)
					{
						for (int j = 0; j < absolute_values.length; j++)
						{
							LocalDateTime current_record_time = LocalDateTime.parse(times[j+1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
							current_sensor.addMeasurement(new Measurement(current_record_time, 0));
						}
					}
					else
					{
						System.out.println("ERROR ON DATA LOADING.(1)");
					}
				}
								
				return_matrix.add(current_sensor);
				
				double percentaje = ((i-1)+1.0)*100/rows;
            	System.out.println("Loading data: "+ percentaje );
			}
			
			//printMatrix(data_matrix);
		}
		catch(Exception e) 
		{
		    System.err.println(e.getMessage());
		    e.printStackTrace();
		}
		finally
		{
			try 
			{
				reader.close();
			} 
			catch (IOException e) 
			{
				System.err.println(e.getMessage());
			}
		}
		
		return return_matrix;
	}
}
