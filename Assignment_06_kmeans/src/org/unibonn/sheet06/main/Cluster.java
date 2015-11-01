package org.unibonn.sheet06.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cluster
{
	private double[] centroid;
	private List<Integer> membership_vector; //Indexes of samples belonging to this cluster.
	
	private Cluster(double[] position)
	{
		this.centroid = position;
		this.membership_vector = new ArrayList<Integer>();
	}

	public Cluster(double[] centroid, List<Integer> membership_vector)
	{
		this.centroid = centroid;
		this.membership_vector = membership_vector;
	}
	
	public double[] getCentroid() {
		return centroid;
	}

	public void setCentroid(double[] centroid) {
		this.centroid = centroid;
	}

	public List<Integer> getMembership_vector() {
		return membership_vector;
	}

	public void setMembership_vector(List<Integer> membership_vector) {
		this.membership_vector = membership_vector;
	}

	static public Cluster generateUniformlyRandomCluster(int dimensions)
	{
		double[] dimensions_vector = new double[dimensions];
		
		Random r = new Random();
		
		for (int i = 0; i < dimensions; i++)
		{
			double randomValue = 10 * r.nextDouble(); //Pick a uniformly distributed random number between 0.0 and 10. (Iris data goes from 0.1 to less than 7.9, so 10 not to be very far from the Iris data boundaries)
			dimensions_vector[i] = randomValue;
		}
		
		return new Cluster(dimensions_vector);
	}
	
	public static List<Cluster> generateCopyOfClusters(List<Cluster> clusters)
	{
		List<Cluster> copies = new ArrayList<Cluster>();
		
		for (int i = 0; i < clusters.size(); i++)
		{
			Cluster actual = clusters.get(i);
			
			copies.add(new Cluster(actual.getCentroid(), actual.getMembership_vector()));
		}
		
		return copies;
	}

	public double distanceTo(double[] point_in_d_dimensions, int typeOfDistance)
	{
		double final_distance = 0;
		double dimensions = centroid.length;
		
		if(typeOfDistance == 2) //Euclidean distance.
		{
			double sum_of__squared_differences = 0;
			
			for (int i = 0; i < dimensions; i++)
			{	
				sum_of__squared_differences += Math.pow(this.centroid[i]-point_in_d_dimensions[i], 2); //Sum of the square difference dimension by dimension.				
			}
			
			final_distance = Math.sqrt(sum_of__squared_differences);
		}
		else if(typeOfDistance == 1) //Manhattan distance.
		{
			for (int i = 0; i < dimensions; i++)
			{	
				final_distance += Math.abs(this.centroid[i] - point_in_d_dimensions[i] ); //Sum of difference dimension by dimension.			
			}
		}
		else
		{
			System.err.println("Error calculating the distances.");
			System.exit(0);
		}

		return final_distance;
	}

	public void reset_membership_vector()
	{
		this.membership_vector = new ArrayList<Integer>();
	}

	public void assignMembership(int i)
	{
		this.membership_vector.add(i);
	}

	public double getClusterSquareError(double[][] data_matrix, int typeOfDistance)
	{
		double total_squared_error = 0;
		int number_of_points_in_cluster = membership_vector.size();//just for readability.
		
		//Sum the values of each point in each dimension and divide by the number of points.
		for (int i = 0; i < number_of_points_in_cluster; i++)
		{	
			int index_of_current_member = membership_vector.get(i);
			total_squared_error += Math.pow(distanceTo(data_matrix[index_of_current_member], typeOfDistance), 2);			
		}

		return total_squared_error;
	}

	@Override
	public String toString()
	{
		StringBuffer total_string = new StringBuffer();
		
		total_string.append("Cluster with centroid in position: [ ");
		
		for (int i = 0; i < centroid.length; i++)
		{
			total_string.append(centroid[i]+", ");
		}
		total_string.replace(total_string.length()-2, total_string.length()-1, "] "+"Total number of members: "+membership_vector.size());
		
		return total_string.toString();
	}

	public void recalculatePositionOfCentroid(double[][] data_matrix)
	{
		double[] sum_values_each_dimension = new double[centroid.length];
		int dimensions = centroid.length; //just for readability.
		int number_of_points_in_cluster = membership_vector.size();//just for readability.
		
		if(number_of_points_in_cluster > 0)
		{
			//Initialization
			for (int j = 0; j < dimensions; j++)
			{
				sum_values_each_dimension[j] = 0;
			}
			
			//Sum the values of each point in each dimension and divide by the number of points.
			for (int i = 0; i < number_of_points_in_cluster; i++)
			{	
				int index_of_current_member = membership_vector.get(i);
				
				for (int j = 0; j < dimensions; j++)
				{
					sum_values_each_dimension[j] += data_matrix[index_of_current_member][j];
				}				
			}
			
			for (int i = 0; i < dimensions; i++)
			{
				centroid[i] = sum_values_each_dimension[i]/number_of_points_in_cluster;
			}
		}
	}
	
	public int getMaxIntersectionWithSpecies(double[][] data_matrix)
	{		
		int intersection_with_setosa = getIntersectionWith(1, data_matrix);
		int intersection_with_versicolor = getIntersectionWith(2, data_matrix);
		int intersection_with_virginical = getIntersectionWith(3, data_matrix);
		
		return Math.max(Math.max(intersection_with_setosa, intersection_with_versicolor), intersection_with_virginical);
	}

	private int getIntersectionWith(int species, double[][] data_matrix)
	{
		int cardinality = 0;
		
		for (int i = 0; i < membership_vector.size(); i++)
		{
			int current_member_index = membership_vector.get(i);
			
			if(data_matrix[current_member_index][centroid.length] == Double.valueOf(species) ) //Species column
			{
				cardinality++;
			}
		}
		
		return cardinality;
	}

	public String getStringOfCentroid()
	{
		StringBuffer result = new StringBuffer();
		
		result.append("[");
		
		for (int i = 0; i < centroid.length; i++)
		{
			result.append(centroid[i]+", ");
		}
		
		result.replace(result.length()-2, result.length()-1, "] ");
		
		return result.toString();
	}
}
