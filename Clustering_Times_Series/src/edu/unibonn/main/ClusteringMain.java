package edu.unibonn.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.jfree.ui.RefineryUtilities;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.unibonn.clustering.dbscan.DBScan_clustering;
import edu.unibonn.clustering.kmeans.KMeans_clustering;
import edu.unibonn.clustering.model.Cluster_DBScan;
import edu.unibonn.clustering.model.Cluster_KMeans;
import edu.unibonn.clustering.model.Sensor;
import edu.unibonn.plotting.TimeSeriesPlotter_DBScan;
import edu.unibonn.plotting.TimeSeriesPlotter_KMeans;

public class ClusteringMain
{
	public static void main(String[] args)
	{
		String pathCSV = args[0];
		String pathOutputImages = args[1];
		String pathOutputClusterMembership = args[2];
		boolean normalize_data = (Integer.valueOf(args[3]) == 0? true: false); //0-NORMALIZE, 1-NOT NORMALIZE.
		int kmeans_or_dbscan = Integer.valueOf(args[4]); //0=KMEANS, 1=DBSCAN.
		
		//KMeans parameters:
		int kmeans_initialization = -1; //0-RANDOM , 1-CANOPY.
		int kmeans_k = -1; //IF CANOPY IS USED, THIS PARAMETER IS IGNORED.
		
		//CANOPY INPUT PARAMETERS. IF RANDOM INITIALIZATION IS USED, THIS PARAMETERS ARE IGNORED.
		int t1 = -1; 
		int t2 = -1;
		
		int kmeans_expectation_distance = -1; //0-EUCLIDEAN DISTANCE , 1-DYNAMIC TIME WARPING.
		int kmeans_recalcultaion_or_centroids = -1; //0-ARITHMETIC MEAN; 1-DTW BARYCENTER AVERAGING.
		
		int kmeans_min_members_per_cluster = -1; //MINIMUM AMOUNT OF MEMBER PER CLUSTER NOT TO BE CONSIDERED NOISE.
		
		//DBScan parameters:
		int dbscan_epsilon = -1;
		int dbscan_minpts = -1;

		if(kmeans_or_dbscan == 0) //0-KMeans, 1-DBScan
		{
			kmeans_initialization = Integer.valueOf(args[5]);
			kmeans_k = Integer.valueOf(args[6]);
			t1 = Integer.valueOf(args[7]); 
			t2 = Integer.valueOf(args[8]);
			kmeans_expectation_distance = Integer.valueOf(args[9]);
			kmeans_recalcultaion_or_centroids = Integer.valueOf(args[10]);
			kmeans_min_members_per_cluster = Integer.valueOf(args[11]);
		}
		else if(kmeans_or_dbscan == 1)
		{
			dbscan_minpts = Integer.valueOf(args[5]);
			dbscan_epsilon = Integer.valueOf(args[6]);
		}
		else
		{
			System.out.println("ERROR: INCORRECT PARAMETERS.");
			System.exit(1);
		}
		
		try
		{
			ClusteringMain.execute_clustering(pathCSV, pathOutputImages, pathOutputClusterMembership, normalize_data, kmeans_or_dbscan, kmeans_initialization, kmeans_k, t1, t2, kmeans_expectation_distance, kmeans_recalcultaion_or_centroids, kmeans_min_members_per_cluster, dbscan_epsilon, dbscan_minpts);
		}
		catch (Exception e)
		{
			System.out.println("FATAL ERROR.");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void execute_clustering(String pathCSV, String pathOutputImages, String pathOutputClusterMembership, boolean normalized, int temp_kmeans_or_dbscan, int kmeans_initialization, int current_k, int t1, int t2, int kmeans_expectation_distance, int kmeans_recalcultaion_or_centroids, int kmeans_min_members_per_cluster, int epsilon, int minpts) throws Exception
	{
		System.out.println("STARTING!");

		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		
		sensors = new ClusteringMain().loadDataCSV(pathCSV, normalized);
		
		LocalDateTime from = sensors.get(0).getInitial_record_time();
		System.out.println("FROM:"+from);
		
		int dimensions = sensors.get(0).getDimensions();
		
		System.out.println("TO:"+from.plusHours(dimensions-1));
		
		//KMEANS ##########################################################################################

		if(temp_kmeans_or_dbscan == 0)
		{
			long initial_time = System.currentTimeMillis();
    		
    		ArrayList<Cluster_KMeans> clusters = new KMeans_clustering().cluster_KMeans_euclidean_d_dims(sensors, current_k, t1, t2, kmeans_initialization, kmeans_expectation_distance, kmeans_recalcultaion_or_centroids);

    		long final_time = System.currentTimeMillis();
    		
    		System.out.println("Execution took: "+((double)(final_time-initial_time)/1000)+"secs.");
    		System.out.println("Execution took: "+(((double)(final_time-initial_time)/1000)/60)+"mins.");
    		
    		ArrayList<Cluster_KMeans> not_noise_clusters = new ArrayList<Cluster_KMeans>();
    		String separate_clustering_id_ = new String();
    		
            for (int j = 0; j < clusters.size(); j++)
            {
            	int current_amount_of_members = clusters.get(j).getMembership().size();
            	
            	if(current_amount_of_members > kmeans_min_members_per_cluster)
            	{
            		not_noise_clusters.add(clusters.get(j));

                	ArrayList<Cluster_KMeans> current_cluster = new ArrayList<Cluster_KMeans>();
                	current_cluster.add(clusters.get(j));
                	
                	separate_clustering_id_ = "KMeans_k_"+clusters.size()+"_only_cluster_"+clusters.get(j).getCluster_id()+"_has_"+clusters.get(j).getMembership().size()+"_members";
                	final TimeSeriesPlotter_KMeans demo_2 = new TimeSeriesPlotter_KMeans(separate_clustering_id_, current_cluster, pathOutputImages, false);
                	
                	separate_clustering_id_ = "KMeans_k_"+clusters.size()+"_only_CENTROID_of_cluster_"+clusters.get(j).getCluster_id()+"_has_"+clusters.get(j).getMembership().size()+"_members";
                	final TimeSeriesPlotter_KMeans demo_3 = new TimeSeriesPlotter_KMeans(separate_clustering_id_, current_cluster, pathOutputImages, true);
                    
//                	demo_2.pack();
//                  RefineryUtilities.centerFrameOnScreen(demo_2);
//                  demo_2.setVisible(true);
            	}    	
			}
            
            final TimeSeriesPlotter_KMeans demo_1 = new TimeSeriesPlotter_KMeans("KMeans_k_"+not_noise_clusters.size()+"_all_not_noise_clusters", not_noise_clusters, pathOutputImages, false);
            
//          demo_1.pack();
//          RefineryUtilities.centerFrameOnScreen(demo_1);
//          demo_1.setVisible(true);
            
            exportToCVS_clusterMembership_KMeans(not_noise_clusters, "KMeans_k_"+not_noise_clusters.size()+"_only_not_noise_clusters", pathOutputClusterMembership);
            exportToCVS_clusterMembership_KMeans(clusters, "KMeans_k_"+clusters.size()+"_all_clusters", pathOutputClusterMembership);
		}
		else if(temp_kmeans_or_dbscan == 1)
		{
			//DBScan ##########################################################################################      
			
			ArrayList<Cluster_DBScan> clusters = new DBScan_clustering().cluster_DBScan_euclidean_d_dims(sensors, epsilon, minpts); //day = 1 Because real data we have is from a Monday.
	
			String dbscan_clustering_id = "DBScan_epsilon_"+epsilon+"_minPts_"+minpts;
			
	        final TimeSeriesPlotter_DBScan demo_1 = new TimeSeriesPlotter_DBScan(dbscan_clustering_id, clusters, from);
//	        demo_1.pack();
//	        RefineryUtilities.centerFrameOnScreen(demo_1);
//	        demo_1.setVisible(true);

	        String separate_clustering_id_ = new String();
	        
            for (int j = 0; j < clusters.size(); j++)
            {            	
        		separate_clustering_id_ = "DBScan_epsilon_"+epsilon+"_only_cluster_"+clusters.get(j).getCluster_id()+"_has_"+clusters.get(j).getMembership().size()+"_members";
            	
            	ArrayList<Cluster_DBScan> current_cluster = new ArrayList<Cluster_DBScan>();
            	current_cluster.add(clusters.get(j));
            	
            	final TimeSeriesPlotter_DBScan demo_DBScan = new TimeSeriesPlotter_DBScan(separate_clustering_id_, current_cluster, from);
                //demo_2.pack();
                //RefineryUtilities.centerFrameOnScreen(demo_2);
                //demo_2.setVisible(true);
			}
            
	        exportToCVS_clusterMembership_DBScan(clusters, dbscan_clustering_id, pathOutputClusterMembership);
		}
		else
		{
			throw new Exception("KMEANS: INVALID CLUSTERING ALGORITHM PARAMETER.");
		}	
	}

	private static void exportToCVS_clusterMembership_KMeans(ArrayList<Cluster_KMeans> clusters, String clustering_id, String pathOutputClusterMembership)
	{
		File outputPath = new File(pathOutputClusterMembership);
		
		String csv = outputPath.getAbsolutePath()+File.separator+clustering_id+".csv";

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
	
	private static void exportToCVS_clusterMembership_DBScan(ArrayList<Cluster_DBScan> clusters, String clustering_id, String pathOutputClusterMembership)
	{
		File outputPath = new File(pathOutputClusterMembership);
		
		String csv = outputPath.getAbsolutePath()+File.separator+clustering_id+".csv";

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
}
