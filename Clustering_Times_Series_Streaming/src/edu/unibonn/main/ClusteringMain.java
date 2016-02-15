package edu.unibonn.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import edu.unibonn.clustering.kmeans.Cluster_KMeans;
import edu.unibonn.clustering.kmeans.KMeans_clustering;
import edu.unibonn.plotting.TimeSeriesPlotter_KMeans;

public class ClusteringMain
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("STARTING!");

		String pathCSV = "generated_sensor_values/1_orig.csv";
	
		BufferedReader br = new BufferedReader(new FileReader(pathCSV));

		String[] times_row = br.readLine().split(";");
		
		int dimensions = times_row.length-1;
		
		LocalDateTime from = LocalDateTime.parse(times_row[1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
		System.out.println("FROM:"+from);
		
		//KMEANS ##########################################################################################
		
		int min_k = 3;
        int max_k = 3;

        int number_of_tries = 1;

        for (int current_k = min_k; current_k <= max_k; current_k++)
        {
        	for (int i = 1; i <= number_of_tries; i++)
        	{
        		long initial_time = System.currentTimeMillis();
        		
        		ArrayList<Cluster_KMeans> clusters = new KMeans_clustering().cluster_KMeans_euclidean_d_dims(br, current_k, dimensions); //day = 1 Because real data we have is from a Monday.

        		long final_time = System.currentTimeMillis();
        		
        		System.out.println("Execution took: "+((double)(final_time-initial_time)/1000));
        		
        		String clustering_id = "KMeans_k_"+current_k+ "_try_"+ i ;
        		
                //final TimeSeriesPlotter_KMeans demo_1 = new TimeSeriesPlotter_KMeans(clustering_id, clusters);
                
                //demo_1.pack();
                //RefineryUtilities.centerFrameOnScreen(demo_1);
                //demo_1.setVisible(true);
                
//                String separate_clustering_id_ = new String();
//                
//                for (int j = 0; j < clusters.size(); j++)
//                {
//                	if(clusters.get(j).getMembership().size() > 0)
//                	{
//                		separate_clustering_id_ = "KMeans_k_"+current_k+ "_try_"+ i +"_only_cluster_"+clusters.get(j).getCluster_id()+"has_"+clusters.get(j).getMembership().size()+"members_("+((float)clusters.get(j).getMembership().size()*100)/821+"%)";
//                    	
//                    	ArrayList<Cluster_KMeans> current_cluster = new ArrayList<Cluster_KMeans>();
//                    	current_cluster.add(clusters.get(j));
//                    	
//                    	//final TimeSeriesPlotter_KMeans demo_2 = new TimeSeriesPlotter_KMeans(separate_clustering_id_, current_cluster);
//                        
//                    	//demo_2.pack();
//                        //RefineryUtilities.centerFrameOnScreen(demo_2);
//                        //demo_2.setVisible(true);
//                	}                	
//				}
                
                exportToCVS_clusterMembership_KMeans(clusters, clustering_id);
			}
		}
	}
	
	//from: http://viralpatel.net/blogs/java-read-write-csv-file/
	private static void exportToCVS_clusterMembership_KMeans(ArrayList<Cluster_KMeans> clusters, String clustering_id)
	{
		int total_number_of_series = 120000;
		
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
				
				HashSet<String> all_members = current_cluster.getMembership();
				
				Iterator<String> iter = all_members.iterator();
				
				while(iter.hasNext())
				{
					data.add(new String[] {iter.next(), current_cluster.getCluster_id()});
				}

				count_absolute = count_absolute + all_members.size();
				count_percentage = count_percentage + ((float)all_members.size()/total_number_of_series)*100;
				
				System.out.println("\t - Cluster "+i+" has "+all_members.size()+" members. ("+((float)all_members.size()/total_number_of_series)*100+"%)");
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
}
