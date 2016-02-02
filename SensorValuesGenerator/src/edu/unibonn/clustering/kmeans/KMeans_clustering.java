package edu.unibonn.clustering.kmeans;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import edu.unibonn.main.Day_24d;
import edu.unibonn.main.Sensor;

public class KMeans_clustering
{
	//int day: 1- Montag, 2- Dienstag, ..., 7 Sontag.
	public ArrayList<Cluster_KMeans> cluster_KMeans_euclidean_24d_specific_day(ArrayList<Sensor> sensors, LocalDateTime from, LocalDateTime to, int k, DayOfWeek day) throws Exception
	{
		//System.out.println("Starting KMEANS");
		
		ArrayList<Day_24d> points_24d = new ArrayList<Day_24d>();
		
		//Generate the 24d points
		for (int i = 0 ; i < sensors.size(); i++)
		{
			ArrayList<Day_24d> current_points_24d = sensors.get(i).generate_24d_points();
			points_24d.addAll(current_points_24d);
			
			double percentaje = (i+1.0)*100/sensors.size();
        	System.out.println("Generating 24d points: "+ percentaje );
		}
		
		//ArrayList<Day_24d> only_montags = filter_montags(points_24d);
		ArrayList<Day_24d> specific_day = filter_day(points_24d, day);
		
		//1- We generate randomly (uniform) the initial clusters.
		ArrayList<Cluster_KMeans> clusters = generate_random_centroids(specific_day, k);
		
		double T1 = 150; //200
		double T2 = 100; //50
		
		//ArrayList<Cluster_KMeans> clusters = generate_centroids_with_canopy(specific_day, T1, T2);	
		//k = clusters.size();
		
		//2-Until we get no improvement (quality dont improve anymore), do:
		double previous_total_clustering_squared_error = -1; //For initialization.
		double actual_total_clustering_squared_error = -1;//For initialization.
		
		int total_iterations_to_converge = 0;
		
		ArrayList<Cluster_KMeans> previous_clusters = new ArrayList<Cluster_KMeans>(); 
		
		//while( previous_total_clustering_squared_error < actual_total_clustering_squared_error || actual_total_clustering_squared_error == -1)
		while(haveTheCentroidsMembershipChanged(clusters, previous_clusters) || actual_total_clustering_squared_error == -1)
		{
			total_iterations_to_converge++;
			
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
					//double actual_distance = clusters.get(j).Dynamic_Time_Warping_distance_to(specific_day.get(i));
					
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
				
				System.out.println("Mapping: "+ (i+1.0)*100/specific_day.size()+"%");
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				clusters.get(i).recalculatePositionOfCentroid(); //Means.
				//clusters.get(i).recalculatePositionOfCentroid_DBA();
				
				System.out.println("Reducing: "+ (i+1.0)*100/k+"%");
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				actual_total_clustering_squared_error += clusters.get(i).getClusterSquareError(); //Calculate the new total clustering squared error.
			}
			
			total_iterations_to_converge++;
			System.out.println("Iteration: "+total_iterations_to_converge);
		}

		System.out.println("\n -KMeans execution FINISHED for k="+k+".\n -Quality measure (Total Cluster square error (within-cluster variation))= "+Math.sqrt(actual_total_clustering_squared_error)+". Total number of iterations to converge: "+ total_iterations_to_converge);
		
		return clusters;
	}

	
	private boolean haveTheCentroidsMembershipChanged(ArrayList<Cluster_KMeans> clusters, ArrayList<Cluster_KMeans> previous_clusters)
	{
		boolean return_value = true;
		
		for (int i = 0; i < clusters.size() && i < previous_clusters.size(); i++)
		{
			ArrayList<Day_24d> current_actual_cluster = clusters.get(i).getMembership();
			ArrayList<Day_24d> current_previous_cluster = previous_clusters.get(i).getMembership();
			
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

	private ArrayList<Cluster_KMeans> generate_centroids_with_canopy(ArrayList<Day_24d> specific_day, double T1, double T2)
	{
		ArrayList<Cluster_KMeans> canopies = new ArrayList<Cluster_KMeans>();
		
		Queue<Day_24d> copy_of_data = new LinkedList<Day_24d>(); 
		copy_of_data.addAll(specific_day);
		
		int count = 0;

		while(!copy_of_data.isEmpty())
		{
			Day_24d current_point = copy_of_data.poll();
			
			//Cluster_KMeans new_canopy = new Cluster_KMeans("Canopy "+count);
			Cluster_KMeans new_canopy = new Cluster_KMeans(String.valueOf(count));
			new_canopy.addMembership(current_point);
			
			ArrayList<Day_24d> elements_to_remove_from_queue = new ArrayList<Day_24d>(); 
			
			for (Iterator iterator = copy_of_data.iterator(); iterator.hasNext();) 
			{
				Day_24d day_24d = (Day_24d) iterator.next();
				
				double curent_distance = current_point.euclidean_distance_to(day_24d);
				
				if(curent_distance < T1) //T1 > T2
				{
					new_canopy.addMembership(current_point);

					if(curent_distance < T2) //Lies inside T2
					{
						//Just remove the point.
						elements_to_remove_from_queue.add(day_24d);
					}
				}			
			}
			
			canopies.add(new_canopy);
			
			count++;
			
			for (Iterator iterator = elements_to_remove_from_queue.iterator(); iterator.hasNext();) 
			{
				Day_24d day_24d = (Day_24d) iterator.next();
				copy_of_data.remove(day_24d);
			}
		}
		
		ArrayList<Cluster_KMeans> centroids = new ArrayList<Cluster_KMeans>();
		
		for (int j = 0; j < canopies.size(); j++)
		{
			Cluster_KMeans cluster_KMeans = canopies.get(j);		
			cluster_KMeans.recalculatePositionOfCentroid();

			Cluster_KMeans new_centroid = new Cluster_KMeans(Integer.valueOf(j).toString());
			new_centroid.setCenter_of_mass(cluster_KMeans.getCenter_of_mass());
			
			centroids.add(new_centroid);	
		}
		
		return canopies;
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


//	public ArrayList<Cluster_KMeans> cluster_KMeans_euclidean_24d_montags(ArrayList<Sensor> sensors, LocalDateTime from, LocalDateTime to, int k) throws Exception
//	{
//		ArrayList<Day_24d> points_24d = new ArrayList<Day_24d>();
//		
//		//Generate the 24d points
//		for (int i = 0 ; i < sensors.size(); i++)
//		{
//			ArrayList<Day_24d> current_points_24d = sensors.get(i).generate_24d_points();
//			points_24d.addAll(current_points_24d);
//		}
//		
//		ArrayList<Day_24d> only_montags = filter_montags(points_24d);
//		
//		//1- We generate randomly (uniform) the initial clusters.
//		ArrayList<Cluster_KMeans> clusters = generate_random_centroids(only_montags, k);
//		
//		//2-Until we get no improvement (quality dont improve anymore), do:
//		double previous_total_clustering_squared_error = -1; //For initialization.
//		double actual_total_clustering_squared_error = -1;//For initialization.
//		
//		while(actual_total_clustering_squared_error < previous_total_clustering_squared_error || actual_total_clustering_squared_error == -1)
//		{
//			//2.1- Iterate over all the points, for each point (in d dimensions) calculate the distance to each centroid, and assign it to the closest centroid.
//			
//			previous_total_clustering_squared_error = actual_total_clustering_squared_error; //Save the previous total square error for comparing at the end of this iteration.
//			actual_total_clustering_squared_error = 0; //re-inititialize the total square error.
//			
//			for (int j = 0; j < k; j++) //Before each iteration we reset the membership vector of the clusters BUT not the new position (random in the first iteration). 
//									    //That way we can re-calculate and re-assign the points to the new centroids and re-calculate the quality, and then decide (comparing with the qualty of the previous iteration) weather to iterate again or finish.
//										//In the first iteration the position of the centroids are random, and the initial previous_clustering_quality (sum of the squared error of each cluster) is Infinity. (We guarantee at least the 2 iterations)
//			{
//				clusters.get(j).reset_membership_vector();
//			}
//			
//			for (int i = 0; i < only_montags.size(); i++) //Iterate over all the points.
//			{
//				double current_closest_distance = Double.POSITIVE_INFINITY;
//				int current_closest_cluster_index = -1;
//
//				for (int j = 0; j < k; j++) //Iterate over all the clusters.
//				{
//					//double actual_distance = clusters.get(j).euclidean_distance_to(only_montags.get(i));
//					double actual_distance = clusters.get(j).Dynamic_Time_Warping_distance_to(only_montags.get(i));
//					
//					if(actual_distance < current_closest_distance)
//					{
//						current_closest_distance = actual_distance;
//						current_closest_cluster_index = j;
//					}
//				}
//				
//				clusters.get(current_closest_cluster_index).addMembership(only_montags.get(i));
//			}
//			
//			for (int i = 0; i < k; i++) //Iterate over all the clusters.
//			{
//				//clusters.get(i).recalculatePositionOfCentroid(); //Means.
//				clusters.get(i).recalculatePositionOfCentroid_DBA(); //Means.
//			}
//			
//			for (int i = 0; i < k; i++) //Iterate over all the clusters.
//			{
//				actual_total_clustering_squared_error += clusters.get(i).getClusterSquareError(); //Calculate the new total clustering squared error.
//			}
//		}
//
//		System.out.println("Finished");
//		
//		return clusters;
//	}
//	
	private ArrayList<Cluster_KMeans> generate_random_centroids(ArrayList<Day_24d> only_montags, int k) throws Exception
	{
		ArrayList<Cluster_KMeans> random_centroids = new ArrayList<Cluster_KMeans>();
		ArrayList<Integer> previous_random_ints = new ArrayList<Integer>();
		
		int count_repetitions = 0;
		
		Random r = new Random();
		
		for (int i = 0; i < k; i++)
		{
			int curr_rand = r.nextInt(only_montags.size());
			
			//curr_rand = i * 8; //For testing.
			
			if(!previous_random_ints.contains(curr_rand))
			{
				previous_random_ints.add(curr_rand);
				random_centroids.add(new Cluster_KMeans(Integer.valueOf(i).toString(),only_montags.get(curr_rand)));				
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
