package edu.unibonn.clustering.kmeans;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import edu.unibonn.main.Sensor;

public class Cluster_KMeans
{
	private String cluster_id;
	private double[] initial_center_of_mass;
	private double[] sum_dimensions_members;
	private int dimensions;
	private HashSet<String> membership;
	private double intra_cluster_error;
	
	public Cluster_KMeans(String cluster_id, int dimensions, double[] initial_center_of_mass_arg)
	{
		this.cluster_id = cluster_id;
		this.dimensions = dimensions;
		this.sum_dimensions_members = new double[dimensions];
		this.initial_center_of_mass = initial_center_of_mass_arg;
		this.membership = new HashSet<String>();
		this.intra_cluster_error = 0;
	}

	public String getCluster_id() {
		return cluster_id;
	}
	public void setCluster_id(String cluster_id) {
		this.cluster_id = cluster_id;
	}
	public double[] getInitial_center_of_mass() {
		return initial_center_of_mass;
	}
	public void setInitial_center_of_mass(double[] center_of_mass) {
		this.initial_center_of_mass = center_of_mass;
	}
	public HashSet<String> getMembership() {
		return membership;
	}

	public void reset_sum_dimensions_members()
	{
		this.sum_dimensions_members = new double[dimensions];
	}
	
	public void reset_membership_vector()
	{
		this.membership = new HashSet<String>();
	}
	
	public void set_membership_vector(HashSet<String> new_members)
	{
		this.membership = (HashSet<String>)new_members.clone();
	}

	public double euclidean_distance_to(double[] other_center_of_mass)
	{
		double final_distance = 0;

		double sum_of_squared_differences = 0;
		
		for (int i = 0; i < this.dimensions; i++)
		{	
			sum_of_squared_differences += Math.pow(this.initial_center_of_mass[i]-other_center_of_mass[i], 2); //Sum of the square difference dimension by dimension.				
		}
		
		final_distance = Math.sqrt(sum_of_squared_differences);

		return final_distance;
	}
	
	public void addMembershipAndSumDimensions(String sensor_id, double[] point_d, double euclidian_distance_to_sensor)
	{
		this.membership.add(sensor_id);
		
		for (int i = 0; i < this.dimensions; i++)
		{
			this.sum_dimensions_members[i]+=point_d[i];
		}
		
		this.intra_cluster_error+=Math.pow(euclidian_distance_to_sensor,2);
	}

	public double[] getRecalculated_center_of_mass()
	{
		int number_of_points_in_cluster = this.membership.size();//just for readability.
		
		double[] new_center_of_mass = new double[this.dimensions];
		
		for (int i = 0; i < this.dimensions; i++)
		{
			new_center_of_mass[i] = this.sum_dimensions_members[i]/number_of_points_in_cluster;
		}
		
		return new_center_of_mass;
	}
	
	public double getClusterSquareError() 
	{
		return this.intra_cluster_error;
	}

	public int getDimensionality()
	{
		return this.dimensions;
	}
	
	public void prepare_for_new_iteration()
	{
		this.initial_center_of_mass = getRecalculated_center_of_mass();
		this.sum_dimensions_members = new double[dimensions];
		this.membership = new HashSet<String>();
		this.intra_cluster_error = 0;
	}
	
	protected Cluster_KMeans copy()
	{
		Cluster_KMeans new_cluster = new Cluster_KMeans(this.cluster_id, this.dimensions, this.initial_center_of_mass);
		new_cluster.set_membership_vector(this.membership);	
		return new_cluster;
	}
}
