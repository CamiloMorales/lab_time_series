package edu.unibonn.clustering.kmeans;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import edu.unibonn.main.Sensor;

public class KMeans_clustering
{
	//int day: 1- Montag, 2- Dienstag, ..., 7 Sontag.
	public ArrayList<Cluster_KMeans> cluster_KMeans_euclidean_d_dims(BufferedReader br, int k, int dimensions) throws Exception
	{
		System.out.println("Starting KMEANS");
		
		//1- We generate randomly (uniform) the initial clusters.
		ArrayList<Cluster_KMeans> clusters = generate_random_centroids(k, dimensions);
		
		//2-Until change of membership assignment, do:
		
		int total_iterations_to_converge = 0;
		
		ArrayList<Cluster_KMeans> previous_clusters = new ArrayList<Cluster_KMeans>();
		
		while(clusters_have_changed_assignment(clusters, previous_clusters))
		{
			previous_clusters = new ArrayList<Cluster_KMeans>(clusters);
			
			total_iterations_to_converge++;
			System.out.println("\n Iteration: "+total_iterations_to_converge);
			
			//2.1- Iterate over all the points, for each point (in d dimensions) calculate the distance to each centroid, and assign it to the closest centroid.
			
			if(total_iterations_to_converge > 1) //Is not the first operation.
			{
				for (int j = 0; j < k; j++) //Before each iteration we reset the membership vector of the clusters BUT not the new position (random in the first iteration). 
				    //That way we can re-calculate and re-assign the points to the new centroids and re-calculate the quality, and then decide (comparing with the qualty of the previous iteration) weather to iterate again or finish.
					//In the first iteration the position of the centroids are random, and the initial previous_clustering_quality (sum of the squared error of each cluster) is Infinity. (We guarantee at least the 2 iterations)
				{
					clusters.get(j).prepare_for_new_iteration();
				}
			}	
			
			String[] current_point_string = null;
			double[] dimensions_current_point = new double[dimensions];
			
			int counter_points = 0;
			
			for(String line; (line = br.readLine()) != null; ) //Iterate over all the points.
			{
				double current_closest_distance = Double.POSITIVE_INFINITY;
				int current_closest_cluster_index = -1;

				current_point_string = line.split(";");
				
				for (int i = 0; i < dimensions; i++)
				{
					dimensions_current_point[i] = Double.valueOf(current_point_string[i+1]);
				}
				
				double actual_distance = 0;
				
				for (int j = 0; j < k; j++) //Iterate over all the clusters.
				{
					actual_distance = clusters.get(j).euclidean_distance_to(dimensions_current_point);

					if(actual_distance < current_closest_distance)
					{
						current_closest_distance = actual_distance;
						current_closest_cluster_index = j;
					}
				}
				
				if(current_closest_cluster_index == -1)
				{
					System.out.println("STOP");
				}
				
				clusters.get(current_closest_cluster_index).addMembershipAndSumDimensions(current_point_string[0], dimensions_current_point, current_closest_distance);
				
				if((counter_points+1.0)% 100 == 0)
				{
					System.out.println("Mapping/Reducing point: "+ counter_points);
				}
			}
			
			br.close();
			
			String pathCSV = "generated_sensor_values/1_short.csv";
			br = new BufferedReader(new FileReader(pathCSV));
			br.readLine(); //We dont need the headers.
		}

		br.close();
		
		System.out.println("\n -KMeans execution FINISHED for k="+k+".\n Total number of iterations to converge: "+ total_iterations_to_converge);
		
		return clusters;
	}

	private boolean clusters_have_changed_assignment(ArrayList<Cluster_KMeans> clusters, ArrayList<Cluster_KMeans> previous_clusters)
	{
		if(previous_clusters.size() == 0) //first time
		{
			return true;
		}
		
		for (int i = 0; i < clusters.size(); i++)
		{
			if(clusters.get(i).getMembership().size() != previous_clusters.get(i).getMembership().size())
			{
				return true;
			}

			if(!clusters.get(i).getMembership().containsAll(previous_clusters.get(i).getMembership()))
			{
				return true;	
			}
		}
		return false;
	}

	private ArrayList<Cluster_KMeans> generate_random_centroids(int k, int dimensions) throws Exception
	{
		ArrayList<Cluster_KMeans> random_centroids = new ArrayList<Cluster_KMeans>();
		
		for (int i = 0; i < k; i++)
		{
			random_centroids.add(new Cluster_KMeans(Integer.valueOf(i).toString(), dimensions, generateRandomVectorOfDimesions(dimensions)));	
		}
		
		return random_centroids;
	}


	private double[] generateRandomVectorOfDimesions(int dimensions)
	{
		Random rand = new Random();
		double[] return_vector = new double[dimensions];
		
		for (int i = 0; i < dimensions; i++)
		{
			return_vector[i]=rand.nextInt(101);
		}
		
		return return_vector;
	}
}
