package edu.unibonn.clustering.kmeans;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import edu.unibonn.main.Sensor;

public class KMeans_clustering
{
	//int day: 1- Montag, 2- Dienstag, ..., 7 Sontag.
	public ArrayList<Cluster> cluster_KMeans_euclidean_24d_specific_day(ArrayList<Sensor> sensors, LocalDateTime from, LocalDateTime to, int k, DayOfWeek day) throws Exception
	{
		System.out.println("Starting KMEANS");
		
		ArrayList<Day_24d> points_24d = new ArrayList<Day_24d>();
		
		//Generate the 24d points
		for (int i = 0 ; i < sensors.size(); i++)
		{
			ArrayList<Day_24d> current_points_24d = sensors.get(i).generate_24d_points();
			points_24d.addAll(current_points_24d);
		}
		
		//ArrayList<Day_24d> only_montags = filter_montags(points_24d);
		ArrayList<Day_24d> specific_day = filter_day(points_24d, day);
		
		//1- We generate randomly (uniform) the initial clusters.
		ArrayList<Cluster> clusters = generate_random_centroids(specific_day, k);
		
		//2-Until we get no improvement (quality dont improve anymore), do:
		double previous_total_clustering_squared_error = -1; //For initialization.
		double actual_total_clustering_squared_error = -1;//For initialization.
		
		while(actual_total_clustering_squared_error < previous_total_clustering_squared_error || actual_total_clustering_squared_error == -1)
		{
			//2.1- Iterate over all the points, for each point (in d dimensions) calculate the distance to each centroid, and assign it to the closest centroid.
			
			previous_total_clustering_squared_error = actual_total_clustering_squared_error; //Save the previous total square error for comparing at the end of this iteration.
			actual_total_clustering_squared_error = 0; //re-inititialize the total square error.
			
			for (int j = 0; j < k; j++) //Before each iteration we reset the membership vector of the clusters BUT not the new position (random in the first iteration). 
									    //That way we can re-calculate and re-assign the points to the new centroids and re-calculate the quality, and then decide (comparing with the qualty of the previous iteration) weather to iterate again or finish.
										//In the first iteration the position of the centroids are random, and the initial previous_clustering_quality (sum of the squared error of each cluster) is Infinity. (We guarantee at least the 2 iterations)
			{
				clusters.get(j).reset_membership_vector();
			}
			
			for (int i = 0; i < specific_day.size(); i++) //Iterate over all the points.
			{
				double current_closest_distance = Double.POSITIVE_INFINITY;
				int current_closest_cluster_index = -1;
				
				for (int j = 0; j < k; j++) //Iterate over all the clusters.
				{
					double actual_distance = clusters.get(j).euclidean_distance_to(specific_day.get(i));
					
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
				
				clusters.get(current_closest_cluster_index).addMembership(specific_day.get(i));
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				clusters.get(i).recalculatePositionOfCentroid(); //Means.
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				actual_total_clustering_squared_error += clusters.get(i).getClusterSquareError(); //Calculate the new total clustering squared error.
			}
		}

		System.out.println("KMEANS FINISHED");
		
		return clusters;
	}

	
	private ArrayList<Day_24d> filter_day(ArrayList<Day_24d> points_24d, DayOfWeek day)
	{
		ArrayList<Day_24d> specific_day = new ArrayList<Day_24d>();
		
		//Generate the 24d points
		for (int i = 0 ; i < points_24d.size(); i++)
		{
			Day_24d current_point_24d = points_24d.get(i);
			
			if(current_point_24d.getDay().getDayOfWeek().equals(day))
			{
				specific_day.add(current_point_24d);
			}
		}
		
		return specific_day;
	}


	public ArrayList<Cluster> cluster_KMeans_euclidean_24d_montags(ArrayList<Sensor> sensors, LocalDateTime from, LocalDateTime to, int k) throws Exception
	{
		ArrayList<Day_24d> points_24d = new ArrayList<Day_24d>();
		
		//Generate the 24d points
		for (int i = 0 ; i < sensors.size(); i++)
		{
			ArrayList<Day_24d> current_points_24d = sensors.get(i).generate_24d_points();
			points_24d.addAll(current_points_24d);
		}
		
		ArrayList<Day_24d> only_montags = filter_montags(points_24d);
		
		//1- We generate randomly (uniform) the initial clusters.
		ArrayList<Cluster> clusters = generate_random_centroids(only_montags, k);
		
		//2-Until we get no improvement (quality dont improve anymore), do:
		double previous_total_clustering_squared_error = -1; //For initialization.
		double actual_total_clustering_squared_error = -1;//For initialization.
		
		while(actual_total_clustering_squared_error < previous_total_clustering_squared_error || actual_total_clustering_squared_error == -1)
		{
			//2.1- Iterate over all the points, for each point (in d dimensions) calculate the distance to each centroid, and assign it to the closest centroid.
			
			previous_total_clustering_squared_error = actual_total_clustering_squared_error; //Save the previous total square error for comparing at the end of this iteration.
			actual_total_clustering_squared_error = 0; //re-inititialize the total square error.
			
			for (int j = 0; j < k; j++) //Before each iteration we reset the membership vector of the clusters BUT not the new position (random in the first iteration). 
									    //That way we can re-calculate and re-assign the points to the new centroids and re-calculate the quality, and then decide (comparing with the qualty of the previous iteration) weather to iterate again or finish.
										//In the first iteration the position of the centroids are random, and the initial previous_clustering_quality (sum of the squared error of each cluster) is Infinity. (We guarantee at least the 2 iterations)
			{
				clusters.get(j).reset_membership_vector();
			}
			
			for (int i = 0; i < only_montags.size(); i++) //Iterate over all the points.
			{
				double current_closest_distance = Double.POSITIVE_INFINITY;
				int current_closest_cluster_index = -1;
				
				for (int j = 0; j < k; j++) //Iterate over all the clusters.
				{
					double actual_distance = clusters.get(j).euclidean_distance_to(only_montags.get(i));
					
					if(actual_distance < current_closest_distance)
					{
						current_closest_distance = actual_distance;
						current_closest_cluster_index = j;
					}
				}
				
				clusters.get(current_closest_cluster_index).addMembership(only_montags.get(i));
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				clusters.get(i).recalculatePositionOfCentroid(); //Means.
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				actual_total_clustering_squared_error += clusters.get(i).getClusterSquareError(); //Calculate the new total clustering squared error.
			}
		}

		System.out.println("Finished");
		
		return clusters;
	}
	
	private ArrayList<Cluster> generate_random_centroids(ArrayList<Day_24d> only_montags, int k) throws Exception
	{
		ArrayList<Cluster> random_centroids = new ArrayList<Cluster>();
		ArrayList<Integer> previous_random_ints = new ArrayList<Integer>();
		
		int count_repetitions = 0;
		
		Random r = new Random();
		
		for (int i = 0; i < k; i++)
		{
			int curr_rand = r.nextInt(only_montags.size());
			
			//curr_rand = i * 100; //For testing.
			
			if(!previous_random_ints.contains(curr_rand))
			{
				random_centroids.add(new Cluster(Integer.valueOf(i).toString(),only_montags.get(curr_rand)));
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

	private ArrayList<Day_24d> filter_montags(ArrayList<Day_24d> points_24d)
	{
		ArrayList<Day_24d> montags = new ArrayList<Day_24d>();
		
		//Generate the 24d points
		for (int i = 0 ; i < points_24d.size(); i++)
		{
			Day_24d current_point_24d = points_24d.get(i);
			
			if(current_point_24d.getDay().getDayOfWeek().equals(DayOfWeek.MONDAY))
			{
				montags.add(current_point_24d);
			}
		}
		
		return montags;
	}
}
