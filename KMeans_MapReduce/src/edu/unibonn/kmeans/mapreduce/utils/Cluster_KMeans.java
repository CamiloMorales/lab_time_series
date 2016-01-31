package edu.unibonn.kmeans.mapreduce.utils;

import java.util.ArrayList;

public class Cluster_KMeans
{
	static public enum DTW_path_move { INITIAL, DIAGONAL, UP, LEFT };
	
	private String cluster_id;
	private double[] center_of_mass;
	private ArrayList<Day_24d> membership;
	
	public Cluster_KMeans(String cluster_id) {
		this.cluster_id = cluster_id;
		this.center_of_mass = new double[24];
		this.membership = new ArrayList<Day_24d>();
	}
	
	public Cluster_KMeans(String cluster_id, Day_24d day_24d)
	{
		this.cluster_id = cluster_id;
		this.membership = new ArrayList<Day_24d>();
		this.center_of_mass = new double[24];
		
		for (int i = 0; i < 24; i++)
		{
			this.center_of_mass[i] = day_24d.getMeasurement(i);
		}
	}

	public String getCluster_id() {
		return cluster_id;
	}
	public void setCluster_id(String cluster_id) {
		this.cluster_id = cluster_id;
	}
	public double[] getCenter_of_mass() {
		return center_of_mass;
	}
	public void setCenter_of_mass(double[] center_of_mass) {
		this.center_of_mass = center_of_mass;
	}
	public ArrayList<Day_24d> getMembership() {
		return membership;
	}
	public void setMembership(ArrayList<Day_24d> membership) {
		this.membership = membership;
	}

	public void reset_membership_vector()
	{
		this.membership = new ArrayList<Day_24d>();
	}

	public double euclidean_distance_to(Day_24d day_24d)
	{
		double final_distance = 0;

		double sum_of_squared_differences = 0;
		
		for (int i = 0; i < 24; i++)
		{	
			sum_of_squared_differences += Math.pow(this.center_of_mass[i]-day_24d.getMeasurement(i), 2); //Sum of the square difference dimension by dimension.				
		}
		
		final_distance = Math.sqrt(sum_of_squared_differences);

		return final_distance;
	}
	
	public double Dynamic_Time_Warping_distance_to(Day_24d to_point)
	{	
		double[][] squared_differences = new double[24][24];
		
		for (int x = 0; x < 24; x++)
		{	
			for (int y = 0; y < 24; y++)
			{	
				squared_differences[x][y] = Math.pow(this.center_of_mass[x]-to_point.getMeasurement(y), 2);
			}					
		}

		double[][] accumulated_cost_matrix = new double[24][24];
		accumulated_cost_matrix[0][0] = squared_differences[0][0];

		//First the first row
		for (int y = 1; y < 24; y++)
		{	
			accumulated_cost_matrix[0][y] = squared_differences[0][y] + accumulated_cost_matrix[0][y-1];
		}
		
		//Then the first Column
		for (int x = 1; x < 24; x++)
		{	
			accumulated_cost_matrix[x][0] = squared_differences[x][0] + accumulated_cost_matrix[x-1][0];
		}

		//Then the rest
		for (int x = 1; x < 24; x++)
		{	
			for (int y = 1; y < 24; y++)
			{	
				double diagonal = accumulated_cost_matrix[x-1][y-1];
				double up = accumulated_cost_matrix[x][y-1];
				double left = accumulated_cost_matrix[x-1][y];
				
				accumulated_cost_matrix[x][y] = Math.min(Math.min(diagonal, up), left) + squared_differences[x][y];
			}
		}
		
		return Math.sqrt(accumulated_cost_matrix[23][23]);
	}

	public void addMembership(Day_24d day_24d)
	{
		this.membership.add(day_24d);
	}

	public void recalculatePositionOfCentroid()
	{
		double[] sum_values_each_dimension = new double[24];
		int number_of_points_in_cluster = this.membership.size();//just for readability.
		
		if(number_of_points_in_cluster > 0)
		{
			//Initialization
			for (int j = 0; j < 24; j++)
			{
				sum_values_each_dimension[j] = 0;
			}
			
			//Sum the values of each point in each dimension and divide by the number of points.
			for (int i = 0; i < number_of_points_in_cluster; i++)
			{	
				Day_24d current_member = this.membership.get(i);
				
				for (int j = 0; j < 24; j++)
				{
					sum_values_each_dimension[j] += current_member.getMeasurement(j);
				}				
			}
			
			for (int i = 0; i < 24; i++)
			{
				center_of_mass[i] = sum_values_each_dimension[i]/number_of_points_in_cluster;
			}
		}
	}

	public double getClusterSquareError() 
	{
		double total_squared_error = 0;
		int number_of_points_in_cluster = this.membership.size();//just for readability.
		
		//Sum the values of each point in each dimension and divide by the number of points.
		for (int i = 0; i < number_of_points_in_cluster; i++)
		{	
			Day_24d current_member = this.membership.get(i);
			total_squared_error += Math.pow(euclidean_distance_to(current_member), 2);			
			//total_squared_error += Math.pow(Dynamic_Time_Warping_distance_to(current_member), 2);
		}

		return total_squared_error;
	}

	public void recalculatePositionOfCentroid_DBA()
	{
		ArrayList<Double>[] tupleAssociation = new ArrayList[center_of_mass.length];
		
		for (int i = 0; i < tupleAssociation.length; i++)
		{
			tupleAssociation[i] = new ArrayList<Double>(24);
		}
		
		for (int j = 0; j < membership.size(); j++)
		{
			Day_24d current_member = membership.get(j);
			double[][] squared_differences = new double[24][24];
			
			for (int x = 0; x < 24; x++)
			{	
				for (int y = 0; y < 24; y++)
				{	
					squared_differences[x][y] = Math.pow(this.center_of_mass[x]-current_member.getMeasurement(y), 2);
				}					
			}

			double[][] accumulated_cost_matrix = new double[24][24];
			accumulated_cost_matrix[0][0] = squared_differences[0][0];
					
			DTW_path_move[][] path_matrix = new DTW_path_move[24][24];
			path_matrix[0][0] = DTW_path_move.INITIAL;
			
			int[][] optimal_path_length = new int[24][24];
			optimal_path_length[0][0] = 0;

			//First the first row
			for (int y = 1; y < 24; y++)
			{	
				accumulated_cost_matrix[0][y] = squared_differences[0][y] + accumulated_cost_matrix[0][y-1];
				path_matrix[0][y] = DTW_path_move.UP;
				optimal_path_length[0][y] = y;
			}
			
			//Then the first Column
			for (int x = 1; x < 24; x++)
			{	
				accumulated_cost_matrix[x][0] = squared_differences[x][0] + accumulated_cost_matrix[x-1][0];
				path_matrix[x][0] = DTW_path_move.LEFT;
				optimal_path_length[x][0] = x;
			}

			//Then the rest
			for (int x = 1; x < 24; x++)
			{	
				for (int y = 1; y < 24; y++)
				{	
					double diagonal = accumulated_cost_matrix[x-1][y-1];
					double up = accumulated_cost_matrix[x][y-1];
					double left = accumulated_cost_matrix[x-1][y];
					
					if(Math.min(Math.min(diagonal, up), left) == left)
					{
						path_matrix[x][y] = DTW_path_move.LEFT;
						optimal_path_length[x][y] = optimal_path_length[x-1][y]+1;					
					}
					else if(Math.min(diagonal, up) == diagonal)
					{
						path_matrix[x][y] = DTW_path_move.DIAGONAL;
						optimal_path_length[x][y] = optimal_path_length[x-1][y-1]+1;
					}
					else if(Math.min(diagonal, up) == up)
					{
						path_matrix[x][y] = DTW_path_move.UP;
						optimal_path_length[x][y] = optimal_path_length[x][y-1]+1;
					}
					else
					{
						System.out.println("ERROR AT DTW-BA CENTROID CALCULATION:");
					}
					
					accumulated_cost_matrix[x][y] = Math.min(Math.min(diagonal, up), left) + squared_differences[x][y];
				}
			}
			
			int length_optimal_path = optimal_path_length[24-1][24-1];
			
			int dimensions_of_centroid = 24-1;
			int dimensions_of_series = 24-1;
			
			for (int i = length_optimal_path; i >= 0; i--)
			{
				tupleAssociation[dimensions_of_centroid].add(current_member.getMeasurement(dimensions_of_series));
				
				if(path_matrix[dimensions_of_centroid][dimensions_of_series].equals(DTW_path_move.DIAGONAL))
				{
					dimensions_of_centroid--;
					dimensions_of_series--;
				}
				else if(path_matrix[dimensions_of_centroid][dimensions_of_series].equals(DTW_path_move.UP))
				{
					dimensions_of_series--;
				}
				else if(path_matrix[dimensions_of_centroid][dimensions_of_series].equals(DTW_path_move.LEFT))
				{
					dimensions_of_centroid--;
				}
			}
		}

		for (int i = 0; i < tupleAssociation.length; i++)
		{
			ArrayList<Double> current_tuple_association = tupleAssociation[i];
			
			double current_count = 0;
			
			for (int j = 0; j < current_tuple_association.size(); j++)
			{
				current_count = current_count + current_tuple_association.get(j);
			}
			
			center_of_mass[i] = (double) current_count/current_tuple_association.size();
		}
	}
}
