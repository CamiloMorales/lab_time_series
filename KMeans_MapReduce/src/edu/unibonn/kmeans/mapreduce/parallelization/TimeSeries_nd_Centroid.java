package edu.unibonn.kmeans.mapreduce.parallelization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.WritableComparable;

import edu.unibonn.kmeans.mapreduce.utils.Day_24d;

public class TimeSeries_nd_Centroid implements WritableComparable<TimeSeries_nd_Centroid> 
{
	//private DoubleVector center;
	private int kTimesIncremented = 1;
	//private int clusterIndex;
	
	private double cluster_id;
	private double[] center_of_mass;
	//private ArrayList<String> membership;

	public TimeSeries_nd_Centroid(int dimensionality)
	{
		this.cluster_id = -1;
		this.center_of_mass = new double[dimensionality];
		//this.membership = new ArrayList<String>();
		this.kTimesIncremented = 1;
	}
	
	public TimeSeries_nd_Centroid(double cluster_id, int dimensionality)
	{
		this.cluster_id = cluster_id;
		this.center_of_mass = new double[dimensionality];
		//this.membership = new ArrayList<String>();
		this.kTimesIncremented = 1;
	}

	public TimeSeries_nd_Centroid(TimeSeries_nd_Centroid key)
	{
		this.cluster_id = key.getCluster_id();
		
		double[] new_center_of_mass = new double[key.getCenter_of_mass().length];
		
		double[] original_center_of_mass = key.getCenter_of_mass();
		
		for (int i = 0; i < key.getCenter_of_mass().length; i++)
		{
			new_center_of_mass[i] = original_center_of_mass[i];
		}
		
		this.center_of_mass = new_center_of_mass;
		
		//this.membership = key.getMembership();
		this.kTimesIncremented = key.getkTimesIncremented();
	}

	public TimeSeries_nd_Centroid(double[] input_center_of_mass_measurements)
	{
		this.cluster_id = -1;
		
		double[] new_center_of_mass = new double[input_center_of_mass_measurements.length];
		
		for (int i = 0; i < input_center_of_mass_measurements.length; i++)
		{
			new_center_of_mass[i] = input_center_of_mass_measurements[i];
		}
		
		this.center_of_mass = new_center_of_mass;

		//this.membership = new ArrayList<String>();
		this.kTimesIncremented = 1;
	}

	public TimeSeries_nd_Centroid()
	{
		this.cluster_id = -1;
		this.center_of_mass = new double[24]; //24 for testing.
		//this.membership = new ArrayList<String>();
		this.kTimesIncremented = 1;
	}

	public TimeSeries_nd_Centroid(int id, Day_24d day_24d)
	{
		this.cluster_id = id;
		this.center_of_mass = new double[24];
		
		for (int i = 0; i < center_of_mass.length; i++)
		{
			this.center_of_mass[i] = day_24d.getMeasurement(i);
		}
		
		//this.membership = new ArrayList<String>();
		this.kTimesIncremented = 1;
	}

	public int getkTimesIncremented() {
		return kTimesIncremented;
	}

	public void setkTimesIncremented(int kTimesIncremented) {
		this.kTimesIncremented = kTimesIncremented;
	}

	public double getCluster_id() {
		return cluster_id;
	}

	public void setCluster_id(double cluster_id) {
		this.cluster_id = cluster_id;
	}

	public double[] getCenter_of_mass() {
		return center_of_mass;
	}

	public void setCenter_of_mass(double[] center_of_mass) {
		this.center_of_mass = center_of_mass;
	}

//	public ArrayList<String> getMembership() {
//		return membership;
//	}
//
//	public void setMembership(ArrayList<String> membership) {
//		this.membership = membership;
//	}

	@Override
	public void write(DataOutput output_centroid) throws IOException
	{
		output_centroid.writeDouble(cluster_id);
		
		output_centroid.writeInt(center_of_mass.length);		
		for (int i = 0; i < center_of_mass.length; i++)
		{
			output_centroid.writeDouble(center_of_mass[i]);
		}
		
		output_centroid.writeInt(kTimesIncremented);
	}
	
	@Override
	public void readFields(DataInput input_centroid) throws IOException
	{
		this.cluster_id = input_centroid.readDouble();

		int dimensionality = input_centroid.readInt();
		this.center_of_mass = new double[dimensionality];
		
		for (int i = 0; i < dimensionality; i++)
		{
			center_of_mass[i] = input_centroid.readDouble();
		}
		
		this.kTimesIncremented = input_centroid.readInt();
	}

	@Override
	public int compareTo(TimeSeries_nd_Centroid current)
	{
		for (int i = 0; i < this.center_of_mass.length; i++)
		{
			if(this.center_of_mass[i] != current.center_of_mass[i])
			{
				return -1;
			}
		}
		
		return 0;	
	}

	public double euclidean_distance_to(TimeSeries_nd_Point value)
	{
		double final_distance = 0;
		double sum_of_squared_differences = 0;
		
		double[] measurements = value.getMeasurements();
		
		for (int i = 0; i < measurements.length; i++)
		{	
			sum_of_squared_differences += Math.pow(this.center_of_mass[i]-measurements[i], 2); //Sum of the square difference dimension by dimension.				
		}
		
		final_distance = Math.sqrt(sum_of_squared_differences);

		return final_distance;
	}
	
	@Override
	public final String toString()
	{
		String complete_str = "Centroid [id:"+this.cluster_id+"; center_of_mass: "+this.center_of_mass.toString();
		
//		for (int i = 0; i < this.center_of_mass.length; i++)
//		{
//			complete_str=complete_str+","+this.center_of_mass[i];
//		}
		
		return complete_str+ "]";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		TimeSeries_nd_Centroid current = (TimeSeries_nd_Centroid)obj;
		
		boolean are_equal = true;
		
		for (int i = 0; i < this.center_of_mass.length; i++)
		{
			if(this.center_of_mass[i] != current.center_of_mass[i])
			{
				are_equal = false;
				break;
			}
		}
			
		return are_equal;
	}
}