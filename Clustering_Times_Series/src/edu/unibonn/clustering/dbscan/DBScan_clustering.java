package edu.unibonn.clustering.dbscan;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import edu.unibonn.main.Sensor;

public class DBScan_clustering
{
	static public enum Type_of_cluster { CLUSTER, NOISE };
	
	public ArrayList<Cluster_DBScan> cluster_DBScan_euclidean_d_dims(ArrayList<Sensor> sensors, double epsilon, double minPts) throws Exception
	{
		epsilon = 50;
        minPts = 3;
		
		//DBScan algorithm
		
		Queue<Sensor> unvisited_points = (Queue) new LinkedList<Sensor>();
		ArrayList<Sensor> noise_points = new ArrayList<Sensor>();
		ArrayList<Cluster_DBScan> return_clusters = new ArrayList<Cluster_DBScan>();
		
		int count_of_clusters = -1;
		
		//First initialize the unvisited points.
		unvisited_points.addAll(sensors);
		
		while(!unvisited_points.isEmpty()) //While there exist unvisited points.
		{
			//Get next unvisited and mark it as visited. (i.e. remove it from the queue)
			Sensor current_point = unvisited_points.poll();
			
			Set<Sensor> current_neighborhood = get_e_neighborhood_of(current_point, sensors, epsilon);
			
			int neighborhood_size = current_neighborhood.size();
			
			System.out.println("Neighborhood_size: "+neighborhood_size);
			
			if(neighborhood_size < minPts)
			{
				noise_points.add(current_point);
			}
			else
			{
				count_of_clusters++;
				
				Cluster_DBScan new_cluster = expand_cluster(current_point, current_neighborhood, sensors, unvisited_points, return_clusters, count_of_clusters, epsilon, minPts);
				
				return_clusters.add(new_cluster);
			}
		}
		
		System.out.println("\n -Clusters found: "+ return_clusters.size());
		System.out.println("\n -Noise points found: "+ noise_points.size());
		
		return return_clusters;
	}
	
	private Cluster_DBScan expand_cluster(Sensor point, Set<Sensor> current_neighborhood, ArrayList<Sensor> instances_I, Queue<Sensor> unvisited_points, ArrayList<Cluster_DBScan> return_clusters, int cluster_id, double epsilon, double minPts)
	{
		Cluster_DBScan new_cluster = new Cluster_DBScan(String.valueOf(cluster_id));
		new_cluster.addMembership(point);
		
		Object[] current_neighbor_array = current_neighborhood.toArray();
		
		for (int i = 0; i < current_neighborhood.size(); i++)
		{
			Sensor current_neighbor = (Sensor)current_neighbor_array[i];
			
			if(unvisited_points.contains(current_neighbor))
			{
				unvisited_points.remove(current_neighbor);
				
				Set<Sensor> current_neighborhood_of_neighbor = get_e_neighborhood_of(current_neighbor, instances_I, epsilon);
				
				if(current_neighborhood_of_neighbor.size() >= minPts)
				{
					current_neighborhood.addAll(current_neighborhood_of_neighbor);
					current_neighbor_array = current_neighborhood.toArray();
				}
			}

			if(!point_belongs_to_any_cluster(current_neighbor, return_clusters) && !new_cluster.getMembership().contains(current_neighbor))
			{
				new_cluster.addMembership(current_neighbor);
			}
		}
		
		return new_cluster;
	}

	private boolean point_belongs_to_any_cluster(Sensor point, ArrayList<Cluster_DBScan> clusters)
	{
		for (Iterator iterator = clusters.iterator(); iterator.hasNext();)
		{
			Cluster_DBScan current_cluster = (Cluster_DBScan) iterator.next();
			
			if(current_cluster.getMembership().contains(point))
			{
				return true;
			}
		}
		
		return false;
	}

	private Set<Sensor> get_e_neighborhood_of(Sensor point, ArrayList<Sensor> instances_I, double epsilon)
	{
		Set<Sensor> return_neighborhood_points = new HashSet<Sensor>();
		
		double mean = 0;
		
		for (Iterator iterator = instances_I.iterator(); iterator.hasNext();)
		{
			Sensor current_point = (Sensor) iterator.next();
			
			double distance = point.euclidean_distance_to(current_point);
			
			//System.out.println("Distance: "+distance);
			
			if(distance <= epsilon)
			{
				return_neighborhood_points.add(current_point);
			}
			
			mean = mean + distance;
		}
		
		System.out.println("Distance MEAN: "+ (mean/(instances_I.size()-1)) );
		
		return return_neighborhood_points;
	}
}
