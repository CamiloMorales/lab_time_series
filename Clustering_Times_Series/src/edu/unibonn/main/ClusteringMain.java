package edu.unibonn.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.time.Day;
import org.jfree.ui.RefineryUtilities;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.unibonn.clustering.dbscan.Cluster_DBScan;
import edu.unibonn.clustering.dbscan.DBScan_clustering;
import edu.unibonn.clustering.kmeans.Cluster_KMeans;
import edu.unibonn.clustering.kmeans.KMeans_clustering;
import edu.unibonn.main.Sensor.Cell_type;
import edu.unibonn.plotting.TimeSeriesPlotter_DBScan;
import edu.unibonn.plotting.TimeSeriesPlotter_KMeans;

public class ClusteringMain
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("STARTING!");

		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		
		String pathCSV = "generated_sensor_values/1_full.csv";
		
		boolean normalized = false;
		
		sensors = new ClusteringMain().loadDataCSV(pathCSV, normalized);
		
		LocalDateTime from = sensors.get(0).getInitial_record_time();
		System.out.println("FROM:"+from);
		
		int dimensions = sensors.get(0).getDimensions();
		
		System.out.println("TO:"+from.plusHours(dimensions-1));
		
		//KMEANS ##########################################################################################
		
		int temp_kmeans_or_dbscan = 0; //0-KMeans, 1-DBScan
		
		if(temp_kmeans_or_dbscan == 0)
		{
			int min_k = 3;
	        int max_k = 3;

	        int number_of_tries = 1;

	        for (int current_k = min_k; current_k <= max_k; current_k++)
	        {
	        	for (int i = 1; i <= number_of_tries; i++)
	        	{
	        		long initial_time = System.currentTimeMillis();
	        		
	        		ArrayList<Cluster_KMeans> clusters = new KMeans_clustering().cluster_KMeans_euclidean_d_dims(sensors, current_k); //day = 1 Because real data we have is from a Monday.

	        		long final_time = System.currentTimeMillis();
	        		
	        		System.out.println("Execution took: "+((double)(final_time-initial_time)/1000)+"secs.");
	        		System.out.println("Execution took: "+(((double)(final_time-initial_time)/1000)/60)+"mins.");
	        		
	        		String clustering_id = "KMeans_k_"+current_k+ "_try_"+ i ;
	        		
	                //final TimeSeriesPlotter_KMeans demo_1 = new TimeSeriesPlotter_KMeans(clustering_id, clusters);
	                
	                //demo_1.pack();
	                //RefineryUtilities.centerFrameOnScreen(demo_1);
	                //demo_1.setVisible(true);
	                
	                String separate_clustering_id_ = new String();
	                
	                for (int j = 0; j < clusters.size(); j++)
	                {
	                	if(clusters.get(j).getMembership().size() > 0)
	                	{
	                		separate_clustering_id_ = "KMeans_k_"+current_k+ "_try_"+ i +"_only_cluster_"+clusters.get(j).getCluster_id()+"has_"+clusters.get(j).getMembership().size()+"members_("+((float)clusters.get(j).getMembership().size()*100)/821+"%)";
	                    	
	                    	ArrayList<Cluster_KMeans> current_cluster = new ArrayList<Cluster_KMeans>();
	                    	current_cluster.add(clusters.get(j));
	                    	
	                    	final TimeSeriesPlotter_KMeans demo_2 = new TimeSeriesPlotter_KMeans(separate_clustering_id_, current_cluster);
	                        
	                    	//demo_2.pack();
	                        //RefineryUtilities.centerFrameOnScreen(demo_2);
	                        //demo_2.setVisible(true);
	                	}                	
					}
	                
	                exportToCVS_clusterMembership_KMeans(clusters, clustering_id);
				}
			}
		}
		else if(temp_kmeans_or_dbscan == 1)
		{
			//DBScan ##########################################################################################

	        int epsilon = 100;
	        int minPts = 8;
	
			ArrayList<Cluster_DBScan> clusters = new DBScan_clustering().cluster_DBScan_euclidean_d_dims(sensors, epsilon, minPts); //day = 1 Because real data we have is from a Monday.
	
			String dbscan_clustering_id = "DBScan_epsilon_"+epsilon+"_minPts_"+minPts;
			
	        final TimeSeriesPlotter_DBScan demo_1 = new TimeSeriesPlotter_DBScan(dbscan_clustering_id, clusters, from);
//	        demo_1.pack();
//	        RefineryUtilities.centerFrameOnScreen(demo_1);
//	        demo_1.setVisible(true);

	        String separate_clustering_id_ = new String();
            
            for (int j = 0; j < clusters.size(); j++)
            {
            	if(clusters.get(j).getMembership().size() > 0)
            	{
            		separate_clustering_id_ = "DBScan_epsilon_"+epsilon+"_only_cluster_"+clusters.get(j).getCluster_id()+"has_"+clusters.get(j).getMembership().size()+"members_("+((float)clusters.get(j).getMembership().size()*100)/821+"%)";
                	
                	ArrayList<Cluster_DBScan> current_cluster = new ArrayList<Cluster_DBScan>();
                	current_cluster.add(clusters.get(j));
                	
                	final TimeSeriesPlotter_DBScan demo_DBScan = new TimeSeriesPlotter_DBScan(separate_clustering_id_, current_cluster, from);
                    //demo_2.pack();
                    //RefineryUtilities.centerFrameOnScreen(demo_2);
                    //demo_2.setVisible(true);
            	}                	
			}
//	        exportToCVS_clusterMembership_DBScan(clusters, dbscan_clustering_id);
		}
		else
		{
			System.out.println("ERROR!");
		}
	}
	
	//from: http://viralpatel.net/blogs/java-read-write-csv-file/
	private static void exportToCVS_clusterMembership_KMeans(ArrayList<Cluster_KMeans> clusters, String clustering_id)
	{
		String csv = "./csv_clusters_membership_output/"+clustering_id+".csv";

		System.out.println(" -In \""+csv+ "\""+":");
		
		double count_absolute = 0;
		double count_percentage = 0;
		
		CSVWriter writer;
		try
		{
			writer = new CSVWriter(new FileWriter(csv));
			List<String[]> data = new ArrayList<String[]>();
			
			for (int i = 0; i < clusters.size(); i++)
			{
				Cluster_KMeans current_cluster = clusters.get(i);
				
				ArrayList<Sensor> all_members = current_cluster.getMembership();
				
				for (int j = 0; j < all_members.size(); j++)
				{
					data.add(new String[] {all_members.get(j).getId(), current_cluster.getCluster_id()});
				}

				count_absolute = count_absolute + all_members.size();
				count_percentage = count_percentage + ((float)all_members.size()/821)*100;
				
				System.out.println("\t - Cluster "+i+" has "+all_members.size()+" members. ("+((float)all_members.size()/821)*100+"%)");
			}
			
			System.out.println("Total number of series: "+count_absolute);
			System.out.println("Total percentage of series: "+count_percentage+"%");
			
			writer.writeAll(data);
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}	
	}
	
	private static void exportToCVS_clusterMembership_DBScan(ArrayList<Cluster_DBScan> clusters, String clustering_id)
	{
		String csv = "./csv_clusters_membership_output/"+clustering_id+".csv";

		System.out.println(" -In \""+csv+ "\""+":");
		
		CSVWriter writer;
		try
		{
			writer = new CSVWriter(new FileWriter(csv));
			List<String[]> data = new ArrayList<String[]>();
			
			for (int i = 0; i < clusters.size(); i++)
			{
				System.out.println("\t Writing cluster: "+(i+1));
				
				Cluster_DBScan current_cluster = clusters.get(i);
				
				ArrayList<Sensor> all_members = current_cluster.getMembership();
				
				for (int j = 0; j < all_members.size(); j++)
				{
					data.add(new String[] {all_members.get(j).getId(), current_cluster.getCluster_id()});
				}

				//System.out.println("\t - Cluster "+i+" has "+all_members.size()+" members. ("+((float)all_members.size()/821)*100+"%)");
			}
			
			writer.writeAll(data);
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}	
	}

	private ArrayList<Sensor> loadDataCSV(String dataFilePath, boolean normalized)
	{
		long initial_time = System.currentTimeMillis();
		
		CSVReader reader = null;
		ArrayList<Sensor> return_matrix = new ArrayList<Sensor>();
		
		try
		{		
			reader = new CSVReader(new FileReader(dataFilePath), ';');
	
			try(BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) 
			{	
				String[] times_row = br.readLine().split(";");
				
				int dimensions = times_row.length-1;
				
				LocalDateTime initial_record_time = LocalDateTime.parse(times_row[1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
				
				int counter_i = 1;
				
				String[] actual_row = null;
				Sensor current_sensor = null;
				
				for(String line; (line = br.readLine()) != null; )
			    {
					actual_row = line.split(";");

					current_sensor = new Sensor(actual_row[0], dimensions, initial_record_time);
					
					if(!normalized)
					{
						for (int j = 1; j <= dimensions; j++)
						{
							current_sensor.addMeasurement(j-1, Double.valueOf(actual_row[j].replace(",", ".")));
						}
					}
					else
					{
						//NORMALIZE
						//1. Create a 24dim double vector with the absolute values.
						//2. Find the highest value, that is going to be 100.
						//3. Escalate all other values between 0 and 1.
						
						double[] absolute_values = new double[dimensions];
						double current_max = 0;
						
						for (int j = 0; j < dimensions; j++)
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
								current_sensor.addMeasurement(j, (absolute_values[j]*100)/current_max);
							}
						}
						else if(current_max == 0)
						{
							for (int j = 0; j < absolute_values.length; j++)
							{
								current_sensor.addMeasurement(j, 0);
							}
						}
						else
						{
							System.out.println("ERROR ON DATA LOADING.(1)");
						}
					}
									
					return_matrix.add(current_sensor);

			    	if(counter_i%100 == 0)
			    	{
			    		System.out.println("Reading file, row"+ counter_i);
			    	}
			    	
			    	counter_i++;
			    }
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
		
		long final_time = System.currentTimeMillis();
		
		System.out.println("Loading the data took: "+((double)(final_time-initial_time)/1000)+"secs.");
		System.out.println("Loading the data took: "+(((double)(final_time-initial_time)/1000)/60)+"mins.");
		
		return return_matrix;
	}
	
//	private ArrayList<Sensor> loadDataCSV(String dataFilePath, boolean normalized)
//	{
//		CSVReader reader = null;
//		ArrayList<Sensor> return_matrix = new ArrayList<Sensor>();
//		
//		try
//		{		
//			reader = new CSVReader(new FileReader(dataFilePath), ';');
//	
//			//List<String[]> pre_matrix = new ArrayList<String[]>();//reader.readAll();
//			ArrayDeque<String[]> pre_matrix = new ArrayDeque<String[]>();//reader.readAll();
//			
//			try(BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) 
//			{	
//				int counter_i = 0;
//				for(String line; (line = br.readLine()) != null; )
//			    {
//			    	pre_matrix.add(line.split(";"));
//	            	System.out.println("Reading file: "+ (float)counter_i*100/120000 );
//			    	counter_i++;
//			    	
////			    	if(((float)counter_i*100/120000)%5 == 0)
////			    	{
////			    		System.out.println("Calling GC.");
////			    		Thread.sleep(5000);
////			    		System.gc();
////			    		Thread.sleep(5000);
////			    	}
//			    }
//			}
//			
//			int rows = pre_matrix.size();
//			//int columns = pre_matrix.get(0).length;
//			int columns = pre_matrix.getFirst().length;
//			
//			int dimensions = columns-1;
//			
//			//String[] times = pre_matrix.get(0);
//			String[] times = pre_matrix.getFirst();
//
//			LocalDateTime initial_record_time = LocalDateTime.parse(times[1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
//
//			Iterator<String[]> itr= pre_matrix.iterator();
//			
//			int i = 0;
//			//for (int i = 1; i < rows; i++)
//			itr.next();//first row is time.
//			while(itr.hasNext())
//			{		
//				//String[] actual_row = pre_matrix.get(i);
//				String[] actual_row = itr.next();
//
//				Sensor current_sensor = new Sensor(actual_row[0], dimensions, initial_record_time);
//				
//				if(!normalized)
//				{
//					for (int j = 1; j < columns; j++)
//					{
//						double current_erlang = Double.valueOf(actual_row[j].replace(",", "."));
//						current_sensor.addMeasurement(j-1, current_erlang);
//					}
//				}
//				else
//				{
//					//NORMALIZE
//					//1. Create a 24dim double vector with the absolute values.
//					//2. Find the highest value, that is going to be 100.
//					//3. Escalate all other values between 0 and 1.
//					
//					double[] absolute_values = new double[dimensions];
//					double current_max = 0;
//					
//					for (int j = 0; j < columns-1; j++)
//					{
//						double current_erlang = Double.valueOf(actual_row[j+1].replace(",", "."));
//						absolute_values[j] = current_erlang;
//						
//						if(current_max < current_erlang)
//						{
//							current_max = current_erlang;
//						}
//					}
//					
//					//Because of sensor: DXB971A (and maybe more).
//					if(current_max > 0)
//					{
//						for (int j = 0; j < absolute_values.length; j++)
//						{
//							double current_erlang_normalized = (absolute_values[j]*100)/current_max;
//							current_sensor.addMeasurement(j, current_erlang_normalized);
//						}
//					}
//					else if(current_max == 0)
//					{
//						for (int j = 0; j < absolute_values.length; j++)
//						{
//							current_sensor.addMeasurement(j, 0);
//						}
//					}
//					else
//					{
//						System.out.println("ERROR ON DATA LOADING.(1)");
//					}
//				}
//								
//				return_matrix.add(current_sensor);
//				
//				double percentaje = ((i-1)+1.0)*100/rows;
//            	System.out.println("Loading data: "+ percentaje );
//            	
//            	i++;
//			}
//			
//			//printMatrix(data_matrix);
//		}
//		catch(Exception e) 
//		{
//		    System.err.println(e.getMessage());
//		    e.printStackTrace();
//		}
//		finally
//		{
//			try 
//			{
//				reader.close();
//			} 
//			catch (IOException e) 
//			{
//				System.err.println(e.getMessage());
//			}
//		}
//		
//		return return_matrix;
//	}
}
