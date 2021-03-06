package edu.unibonn.clustering.kmeans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import edu.unibonn.clustering.model.Cluster_KMeans;
import edu.unibonn.clustering.model.Sensor;

public class KMeans_clustering
{
	public ArrayList<Cluster_KMeans> cluster_KMeans_euclidean_d_dims(ArrayList<Sensor> sensors, int k, int T1, int T2, int kmeans_initialization, int kmeans_expectation_distance, int kmeans_recalcultaion_or_centroids) throws Exception
	{
		System.out.println("Starting KMEANS");
		
		ArrayList<Cluster_KMeans> clusters = null;
		
		if(kmeans_initialization == 0) //RANDOM INITIALIZATION
		{
			//1- Generate randomly (uniform) the initial clusters.
			clusters = generate_random_centroids(sensors, k);
		}
		else if(kmeans_initialization == 1)
		{
			//1- Find k and generate initial clusters with CANOPY. 
			clusters = generate_centroids_with_canopy(sensors, T1, T2);	
			k = clusters.size();
		}
		else
		{
			throw new Exception("KMEANS: INVALID INITIALIZATION PARAMETER.");
		}
	
		//2-Until no further improvements, do:
		double previous_total_clustering_squared_error = -1; //For initialization.
		double actual_total_clustering_squared_error = -1;//For initialization.
		
		int total_iterations_to_converge = 0;

		while(previous_total_clustering_squared_error > actual_total_clustering_squared_error || previous_total_clustering_squared_error == -1)
		{
			long initial_time = System.currentTimeMillis();
			
			total_iterations_to_converge++;
			System.out.println("\n Iteration: "+total_iterations_to_converge);
			
			System.out.println("previous_total_clustering_squared_error: "+previous_total_clustering_squared_error);
			System.out.println("actual_total_clustering_squared_error: "+actual_total_clustering_squared_error);
			
			//2.1- Iterate over all the points, for each point (in d dimensions) calculate the distance to each centroid, and assign it to the closest centroid.
			
			previous_total_clustering_squared_error = actual_total_clustering_squared_error; //Save the previous total square error for comparing at the end of this iteration.
			actual_total_clustering_squared_error = 0; //re-inititialize the total square error.
			
			for (int j = 0; j < k; j++) //Before each iteration we reset the membership vector of the clusters BUT not the new position (random in the first iteration). 
									    //That way we can re-calculate and re-assign the points to the new centroids and re-calculate the quality, and then decide (comparing with the qualty of the previous iteration) weather to iterate again or finish.
										//In the first iteration the position of the centroids are random, and the initial previous_clustering_quality (sum of the squared error of each cluster) is Infinity. (We guarantee at least the 2 iterations)
			{
				clusters.get(j).reset_membership_vector();
			}
			
			//EXPECTATION PHASE
			for (int i = 0; i < sensors.size(); i++) //Iterate over all the points.
			{
				double current_closest_distance = Double.POSITIVE_INFINITY;
				int current_closest_cluster_index = -1;

				Sensor current_d_point = sensors.get(i);
				
				double actual_distance = -1;
				
				for (int j = 0; j < k; j++) //Iterate over all the clusters.
				{
					if(kmeans_expectation_distance == 0) //EUCLIDEAN
					{
						actual_distance = clusters.get(j).euclidean_distance_to(current_d_point);
					}
					else if(kmeans_expectation_distance == 1) //DTW
					{
						actual_distance = clusters.get(j).Dynamic_Time_Warping_distance_to(current_d_point);
					}

					if(actual_distance < current_closest_distance)
					{
						current_closest_distance = actual_distance;
						current_closest_cluster_index = j;
					}
				}
				
				if(current_closest_cluster_index == -1)
				{
					throw new Exception("KMEANS: ERROR IN EXPECTATION PHASE.");
				}
				
				clusters.get(current_closest_cluster_index).addMembership(current_d_point);
				
				if((float)i*100/sensors.size() % 5 == 0)
				{
					System.out.println("EXPECTATION PHASE: "+ (float)i*100/sensors.size()+"%");
				}
			}
			
			//MAXIMIZATION PHASE
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				if(kmeans_recalcultaion_or_centroids == 0) //ARITHMETIC MEAN
				{
					clusters.get(i).recalculatePositionOfCentroid();
				}
				else if(kmeans_recalcultaion_or_centroids == 0) //ARITHMETIC MEAN
				{
					clusters.get(i).recalculatePositionOfCentroid_DBA();
				}
				else
				{
					throw new Exception("KMEANS: ERROR IN MAXIMIZATION PHASE.");
				}
				
				System.out.println("MAXIMIZATION PHASE: "+ (float)(i+1)*100/k+"%");
			}
			
			//Finally: Calculate the new total clustering squared error:
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				actual_total_clustering_squared_error += clusters.get(i).getClusterSquareError(); 
			}
			
			long final_time = System.currentTimeMillis();
    		
    		System.out.println("Iteration "+total_iterations_to_converge+"took: "+((double)(final_time-initial_time)/1000)+"secs.");
		}

		System.out.println("\n -KMeans execution FINISHED for k="+k+".\n -Quality measure (Total Cluster square error (within-cluster variation))= "+Math.sqrt(actual_total_clustering_squared_error)+". Total number of iterations to converge: "+ total_iterations_to_converge);
		
		return clusters;
	}

	private ArrayList<Cluster_KMeans> generate_centroids_with_canopy(ArrayList<Sensor> sensors, double T1, double T2)
	{
		int dimensions = sensors.get(0).getDimensions();
		
		ArrayList<Cluster_KMeans> canopies = new ArrayList<Cluster_KMeans>();
		
		Queue<Sensor> copy_of_data = new LinkedList<Sensor>(); 
		copy_of_data.addAll(sensors);
		
		int count = 0;

		while(!copy_of_data.isEmpty())
		{
			System.out.println("Canopy. Points left to visist: "+ copy_of_data.size());
			
			Sensor current_point = copy_of_data.poll();

			Cluster_KMeans new_canopy = new Cluster_KMeans(String.valueOf(count), dimensions);
			new_canopy.addMembership(current_point);
			
			ArrayList<Sensor> elements_to_remove_from_queue = new ArrayList<Sensor>(); 
			
			for (Iterator iterator = copy_of_data.iterator(); iterator.hasNext();) 
			{
				Sensor point_d = (Sensor) iterator.next();
				
				double current_distance = current_point.euclidean_distance_to(point_d);
				
				if(current_distance < T1) //T1 > T2
				{
					new_canopy.addMembership(current_point);

					if(current_distance < T2) //Lies inside T2
					{
						//Just remove the point.
						elements_to_remove_from_queue.add(point_d);
					}
				}			
			}
			
			canopies.add(new_canopy);
			
			count++;
			
			for (Iterator iterator = elements_to_remove_from_queue.iterator(); iterator.hasNext();) 
			{
				Sensor point_d = (Sensor) iterator.next();
				copy_of_data.remove(point_d);
			}
		}
		
		ArrayList<Cluster_KMeans> centroids = new ArrayList<Cluster_KMeans>();
		
		for (int j = 0; j < canopies.size(); j++)
		{
			Cluster_KMeans cluster_KMeans = canopies.get(j);		
			cluster_KMeans.recalculatePositionOfCentroid();

			Cluster_KMeans new_centroid = new Cluster_KMeans(Integer.valueOf(j).toString(), dimensions);
			new_centroid.setCenter_of_mass(cluster_KMeans.getCenter_of_mass());
			
			centroids.add(new_centroid);	
		}
		
		return canopies;
	}

	private ArrayList<Cluster_KMeans> generate_random_centroids(ArrayList<Sensor> sensors, int k) throws Exception
	{
		ArrayList<Cluster_KMeans> random_centroids = new ArrayList<Cluster_KMeans>();
		ArrayList<Integer> previous_random_ints = new ArrayList<Integer>();
		
		int count_repetitions = 0;
		
		int dimensions = sensors.get(0).getDimensions();
		
		Random r = new Random();
		
		for (int i = 0; i < k; i++)
		{
			int curr_rand = r.nextInt(sensors.size());
			
			//curr_rand = i * 8; //Make it determinstic, just for testing.
			
			if(!previous_random_ints.contains(curr_rand))
			{
				previous_random_ints.add(curr_rand);
				random_centroids.add(new Cluster_KMeans(Integer.valueOf(i).toString(), sensors.get(curr_rand), dimensions));				
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
		}
		
		return random_centroids;
	}
	
	//An optional way to know when to stop the iterations.
	private boolean haveTheCentroidsMembershipChanged(ArrayList<Cluster_KMeans> clusters, ArrayList<Cluster_KMeans> previous_clusters)
	{
		for (int i = 0; i < clusters.size() && i < previous_clusters.size(); i++)
		{
			ArrayList<Sensor> current_actual_cluster = clusters.get(i).getMembership();
			ArrayList<Sensor> current_previous_cluster = previous_clusters.get(i).getMembership();
			
			if(current_actual_cluster.size() != current_previous_cluster.size())
			{
				return true;
			}
			else
			{
				for (int j = 0; j < current_actual_cluster.size(); j++)
				{
					if(!current_previous_cluster.contains(current_actual_cluster.get(j)))
					{
						return true;
					}		
				}
			}
		}
		
		return false;
	}
}
