package edu.unibonn.clustering.dbscan;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import edu.unibonn.main.Day_24d;
import edu.unibonn.main.Sensor;

public class DBScan_clustering
{
	static public enum Type_of_cluster { CLUSTER, NOISE };
	
	public ArrayList<Cluster_DBScan> cluster_DBScan_euclidean_24d_specific_day(ArrayList<Sensor> sensors, LocalDateTime from, LocalDateTime to, double epsilon, double minPts, DayOfWeek day) throws Exception
	{
		epsilon = 50;
        minPts = 3;
		
		ArrayList<Day_24d> points_24d = new ArrayList<Day_24d>();
		
		//Generate the 24d points
		for (int i = 0 ; i < sensors.size(); i++)
		{
			ArrayList<Day_24d> current_points_24d = sensors.get(i).generate_24d_points();
			points_24d.addAll(current_points_24d);
		}

		ArrayList<Day_24d> instances_I = filter_day(points_24d, day);
		
		//DBScan algorithm
		
		Queue<Day_24d> unvisited_points = (Queue) new LinkedList<Day_24d>();
		ArrayList<Day_24d> noise_points = new ArrayList<Day_24d>();
		ArrayList<Cluster_DBScan> return_clusters = new ArrayList<Cluster_DBScan>();
		
		int count_of_clusters = -1;
		
		//First initialize the unvisited points.
		for (int i = 0; i < instances_I.size(); i++)
		{
			unvisited_points.add(instances_I.get(i));
		}
		
		while(!unvisited_points.isEmpty()) //While there exist unvisited points.
		{
			//Get next unvisited and mark it as visited. (i.e. remove it from the queue)
			Day_24d current_point = unvisited_points.poll();
			
			Set<Day_24d> current_neighborhood = get_e_neighborhood_of(current_point, instances_I, epsilon);
			
			int neighborhood_size = current_neighborhood.size();
			
			System.out.println("Neighborhood_size: "+neighborhood_size);
			
			if(neighborhood_size < minPts)
			{
				noise_points.add(current_point);
			}
			else
			{
				count_of_clusters++;
				
				Cluster_DBScan new_cluster = expand_cluster(current_point, current_neighborhood, instances_I, unvisited_points, return_clusters, count_of_clusters, epsilon, minPts);
				
				return_clusters.add(new_cluster);
			}
		}
		
		System.out.println("\n -Clusters found: "+ return_clusters.size());
		System.out.println("\n -Noise points found: "+ noise_points.size());
		
		return return_clusters;
	}
	
	private Cluster_DBScan expand_cluster(Day_24d point, Set<Day_24d> current_neighborhood, ArrayList<Day_24d> instances_I, Queue<Day_24d> unvisited_points, ArrayList<Cluster_DBScan> return_clusters, int cluster_id, double epsilon, double minPts)
	{
		Cluster_DBScan new_cluster = new Cluster_DBScan(String.valueOf(cluster_id));
		new_cluster.addMembership(point);
		
		Object[] current_neighbor_array = current_neighborhood.toArray();
		
		for (int i = 0; i < current_neighborhood.size(); i++)
		{
			Day_24d current_neighbor = (Day_24d)current_neighbor_array[i];
			
			if(unvisited_points.contains(current_neighbor))
			{
				unvisited_points.remove(current_neighbor);
				
				Set<Day_24d> current_neighborhood_of_neighbor = get_e_neighborhood_of(current_neighbor, instances_I, epsilon);
				
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
		
//		for (Iterator iterator = current_neighborhood.iterator(); iterator.hasNext();)
//		{
//			Day_24d current_neighbor = (Day_24d) iterator.next();
//			
//			if(unvisited_points.contains(current_neighbor))
//			{
//				unvisited_points.remove(current_neighbor);
//				
//				Set<Day_24d> current_neighborhood_of_neighbor = get_e_neighborhood_of(current_neighbor, instances_I, epsilon);
//				
//				if(current_neighborhood_of_neighbor.size() >= minPts)
//				{
//					current_neighborhood.addAll(current_neighborhood_of_neighbor);
//				}
//			}
//			else
//			{
//				if(point_belongs_to_any_cluster(current_neighbor, return_clusters))
//				{
//					new_cluster.addMembership(current_neighbor);
//				}
//			}
//		}
		
		return new_cluster;
	}

	private boolean point_belongs_to_any_cluster(Day_24d point, ArrayList<Cluster_DBScan> clusters)
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

	private Set<Day_24d> get_e_neighborhood_of(Day_24d point, ArrayList<Day_24d> instances_I, double epsilon)
	{
		Set<Day_24d> return_neighborhood_points = new HashSet<Day_24d>();
		
		double mean = 0;
		
		for (Iterator iterator = instances_I.iterator(); iterator.hasNext();)
		{
			Day_24d current_point = (Day_24d) iterator.next();
			
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
}
