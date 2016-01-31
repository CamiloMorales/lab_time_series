package edu.unibonn.kmeans.mapreduce.parallelization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.print.DocFlavor.STRING;

import org.apache.hadoop.io.WritableComparable;

import edu.unibonn.kmeans.mapreduce.utils.Day_24d;

public class TimeSeries_nd_Point implements WritableComparable<TimeSeries_nd_Point> 
{
	private String sensor_id;
	//private LocalDate day; //Conjunction between sensor_id AND day must be unique. (See get_point_unique_id())
	private double[] measurements;
	
	public TimeSeries_nd_Point(String sensor_id, int dimensionality)
	{
		this.sensor_id = sensor_id;
		this.measurements = new double[dimensionality];
	}

	public TimeSeries_nd_Point(Day_24d current_sensor_data)
	{
		this.sensor_id = current_sensor_data.getId();
		this.measurements = new double[24];
		
		for (int i = 0; i < 24; i++)
		{
			this.measurements[i] = current_sensor_data.getMeasurement(i);
		}
	}

	public TimeSeries_nd_Point()
	{
		this.sensor_id = new String();
		this.measurements = new double[24];
	}

	public String getSensor_id() {
		return sensor_id;
	}

	public void setSensor_id(String sensor_id) {
		this.sensor_id = sensor_id;
	}

	public double[] getMeasurements() {
		return measurements;
	}

	public void setMeasurements(double[] measurements) {
		this.measurements = measurements;
	}

	@Override
	public void readFields(DataInput input_nd_point) throws IOException
	{
		int length_sensor_id = input_nd_point.readInt();
		
		this.sensor_id = new String();
		
		for (int i = 0; i < length_sensor_id; i++)
		{
			this.sensor_id = this.sensor_id + input_nd_point.readChar();
		}
		
		int dimensionality = input_nd_point.readInt();
		
		for (int i = 0; i < dimensionality; i++)
		{
			this.measurements[i] = input_nd_point.readDouble();
		}
	}
	
	@Override
	public void write(DataOutput output_nd_point) throws IOException
	{
		output_nd_point.writeInt(sensor_id.length());

		output_nd_point.writeChars(sensor_id);

		output_nd_point.writeInt(measurements.length);
		
		for (int i = 0; i < measurements.length; i++)
		{
			output_nd_point.writeDouble(measurements[i]);
		}
	}
	@Override
	public int compareTo(TimeSeries_nd_Point o)
	{
		return this.sensor_id.compareTo(o.getSensor_id());
	}
	
	@Override
	public String toString()
	{
		String complete_str = "Series[id: "+this.sensor_id+", dimensions: "+this.measurements.toString();
		
//		for (int i = 0; i < this.measurements.length; i++)
//		{
//			complete_str=complete_str+","+this.measurements[i];
//		}
		
		return complete_str+ "]";
	}

	public TimeSeries_nd_Point create_copy()
	{
		TimeSeries_nd_Point ret = new TimeSeries_nd_Point();
		
		ret.sensor_id = this.sensor_id;
		
		double[] copy_measurements = new double[this.measurements.length];
		
		for (int i = 0; i < this.measurements.length; i++)
		{
			copy_measurements[i] = this.measurements[i];
		}
		
		ret.measurements = copy_measurements;

		return ret;
	}

}
