package edu.unibonn.clustering.kmeans;

import java.time.LocalDateTime;
import java.util.ArrayList;

import edu.unibonn.main.Sensor;

public class Cluster_KMeans
{
	static public enum DTW_path_move { INITIAL, DIAGONAL, UP, LEFT };
	
	private String cluster_id;
	private float[] center_of_mass;
	private short dimensions;
	private ArrayList<Sensor> membership;
	
	public Cluster_KMeans(String cluster_id, short dimensions)
	{
		this.cluster_id = cluster_id;
		this.dimensions = dimensions;
		this.center_of_mass = new float[dimensions];
		this.membership = new ArrayList<Sensor>();
	}
	
	public Cluster_KMeans(String cluster_id, Sensor point_d, short dimensions)
	{
		this.cluster_id = cluster_id;
		this.membership = new ArrayList<Sensor>();
		this.dimensions = dimensions;
		this.center_of_mass = new float[dimensions];
		
		for (short i = 0; i < dimensions; i++)
		{
			this.center_of_mass[i] = point_d.getMeasurement(i);
		}
	}

	public String getCluster_id() {
		return cluster_id;
	}
	public void setCluster_id(String cluster_id) {
		this.cluster_id = cluster_id;
	}
	public float[] getCenter_of_mass() {
		return center_of_mass;
	}
	public void setCenter_of_mass(float[] center_of_mass) {
		this.center_of_mass = center_of_mass;
	}
	public ArrayList<Sensor> getMembership() {
		return membership;
	}
	public void setMembership(ArrayList<Sensor> membership) {
		this.membership = membership;
	}

	public void reset_membership_vector()
	{
		this.membership = new ArrayList<Sensor>();
	}

	public double euclidean_distance_to(Sensor point_d)
	{
		double final_distance = 0;

		double sum_of_squared_differences = 0;
		
		for (short i = 0; i < point_d.getDimensions(); i++)
		{	
			sum_of_squared_differences += Math.pow(this.center_of_mass[i]-point_d.getMeasurement(i), 2); //Sum of the square difference dimension by dimension.				
		}
		
		final_distance = Math.sqrt(sum_of_squared_differences);

		return final_distance;
	}
	
	public double Dynamic_Time_Warping_distance_to(Sensor to_point)
	{	
		double[][] squared_differences = new double[to_point.getDimensions()][to_point.getDimensions()];
		
		for (short x = 0; x < to_point.getDimensions(); x++)
		{	
			for (short y = 0; y < to_point.getDimensions(); y++)
			{	
				squared_differences[x][y] = Math.pow(this.center_of_mass[x]-to_point.getMeasurement(y), 2);
			}					
		}

		double[][] accumulated_cost_matrix = new double[to_point.getDimensions()][to_point.getDimensions()];
		accumulated_cost_matrix[0][0] = squared_differences[0][0];

		//First the first row
		for (short y = 1; y < to_point.getDimensions(); y++)
		{	
			accumulated_cost_matrix[0][y] = squared_differences[0][y] + accumulated_cost_matrix[0][y-1];
		}
		
		//Then the first Column
		for (short x = 1; x < to_point.getDimensions(); x++)
		{	
			accumulated_cost_matrix[x][0] = squared_differences[x][0] + accumulated_cost_matrix[x-1][0];
		}

		//Then the rest
		for (short x = 1; x < to_point.getDimensions(); x++)
		{	
			for (short y = 1; y < to_point.getDimensions(); y++)
			{	
				double diagonal = accumulated_cost_matrix[x-1][y-1];
				double up = accumulated_cost_matrix[x][y-1];
				double left = accumulated_cost_matrix[x-1][y];
				
				accumulated_cost_matrix[x][y] = Math.min(Math.min(diagonal, up), left) + squared_differences[x][y];
			}
		}
		
		return Math.sqrt(accumulated_cost_matrix[to_point.getDimensions()-1][to_point.getDimensions()-1]);
	}

	public void addMembership(Sensor point_d)
	{
		this.membership.add(point_d);
	}

	public void recalculatePositionOfCentroid()
	{
		float[] sum_values_each_dimension = new float[this.dimensions];
		int number_of_points_in_cluster = this.membership.size();//just for readability.
		
		if(number_of_points_in_cluster > 0)
		{
			//Initialization
			for (short j = 0; j < this.dimensions; j++)
			{
				sum_values_each_dimension[j] = 0;
			}
			
			//Sum the values of each point in each dimension and divide by the number of points.
			for (short i = 0; i < number_of_points_in_cluster; i++)
			{	
				Sensor current_member = this.membership.get(i);
				
				for (short j = 0; j < this.dimensions; j++)
				{
					sum_values_each_dimension[j] += current_member.getMeasurement(j);
				}				
			}
			
			for (short i = 0; i < this.dimensions; i++)
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
			Sensor current_member = this.membership.get(i);
			total_squared_error += Math.pow(euclidean_distance_to(current_member), 2);			
			//total_squared_error += Math.pow(Dynamic_Time_Warping_distance_to(current_member), 2);
		}

		return total_squared_error;
	}

	public void recalculatePositionOfCentroid_DBA()
	{
		ArrayList<Float>[] tupleAssociation = new ArrayList[center_of_mass.length];
		
		for (int i = 0; i < tupleAssociation.length; i++)
		{
			tupleAssociation[i] = new ArrayList<Float>(this.dimensions);
		}
		
		for (int j = 0; j < membership.size(); j++)
		{
			Sensor current_member = membership.get(j);
			double[][] squared_differences = new double[this.dimensions][this.dimensions];
			
			for (short x = 0; x < this.dimensions; x++)
			{	
				for (short y = 0; y < this.dimensions; y++)
				{	
					squared_differences[x][y] = Math.pow(this.center_of_mass[x]-current_member.getMeasurement(y), 2);
				}					
			}

			double[][] accumulated_cost_matrix = new double[this.dimensions][this.dimensions];
			accumulated_cost_matrix[0][0] = squared_differences[0][0];
					
			DTW_path_move[][] path_matrix = new DTW_path_move[this.dimensions][this.dimensions];
			path_matrix[0][0] = DTW_path_move.INITIAL;
			
			int[][] optimal_path_length = new int[this.dimensions][this.dimensions];
			optimal_path_length[0][0] = 0;

			//First the first row
			for (short y = 1; y < this.dimensions; y++)
			{	
				accumulated_cost_matrix[0][y] = squared_differences[0][y] + accumulated_cost_matrix[0][y-1];
				path_matrix[0][y] = DTW_path_move.UP;
				optimal_path_length[0][y] = y;
			}
			
			//Then the first Column
			for (short x = 1; x < this.dimensions; x++)
			{	
				accumulated_cost_matrix[x][0] = squared_differences[x][0] + accumulated_cost_matrix[x-1][0];
				path_matrix[x][0] = DTW_path_move.LEFT;
				optimal_path_length[x][0] = x;
			}

			//Then the rest
			for (short x = 1; x < this.dimensions; x++)
			{	
				for (short y = 1; y < this.dimensions; y++)
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
			
			int length_optimal_path = optimal_path_length[this.dimensions-1][this.dimensions-1];
			
			short dimensions_of_centroid = (short)(this.dimensions-1);
			short dimensions_of_series = (short)(this.dimensions-1);
			
			for (int i = length_optimal_path; i >= 0; i--)
			{
				tupleAssociation[dimensions_of_centroid].add((float)current_member.getMeasurement(dimensions_of_series));
				
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
			ArrayList<Float> current_tuple_association = tupleAssociation[i];
			
			double current_count = 0;
			
			for (int j = 0; j < current_tuple_association.size(); j++)
			{
				current_count = current_count + current_tuple_association.get(j);
			}
			
			center_of_mass[i] = (float) (current_count/current_tuple_association.size());
		}
	}
	
	public int getDimensionality()
	{
		return this.membership.get(0).getDimensions();
	}

	public LocalDateTime getInitial_record_time()
	{
		return this.membership.get(0).getInitial_record_time();
	}
}
