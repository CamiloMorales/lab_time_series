package edu.unibonn.main;

import java.time.LocalDateTime;
import java.util.ArrayList;

import edu.unibonn.generator.SampleGenerator;

public class Sensor 
{
	static public enum Cell_type { WORKING_AREA, RESIDENTIAL_AREA, RURAL_AREA };
	
	private String id;
	private Cell_type type;
	private ArrayList<Measurement> measurements;
	
	public Sensor(String id) 
	{
		this.id = id;
	}

	public String getId() 
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public ArrayList<Measurement> getMeasurements()
	{
		return measurements;
	}
	
	public void setMeasurements(ArrayList<Measurement> measurements)
	{
		this.measurements = measurements;
	}
	
	public Cell_type getType() 
	{
		return type;
	}

	public void setType(Cell_type type)
	{
		this.type = type;
	}
	
	public void generate_measurements_from_to_regarding_type(LocalDateTime from, LocalDateTime to)
	{
		System.out.println("Generating measurements fo the sensor: "+ this.id + ", of type: "+ this.type +", from: "+ from +" - to: "+ to);
		
		LocalDateTime current_time = from;
		
		while(current_time.isBefore(to)) 
		{
			System.out.println("Current time: "+current_time);
			
			Measurement current_measurement = SampleGenerator.getInstance().generateMeasurement(current_time, type);			
			current_time = current_time.plusHours(1);
		}
	}
}