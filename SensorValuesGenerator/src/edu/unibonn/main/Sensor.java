package edu.unibonn.main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import edu.unibonn.clustering.kmeans.Day_24d;
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
		this.measurements = new ArrayList<Measurement>();
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
			this.measurements.add(current_measurement);
			
			current_time = current_time.plusHours(1);
		}
	}

	public ArrayList<Day_24d> generate_24d_points()
	{
		ArrayList<Day_24d> return_array = new ArrayList<Day_24d>();
		
		LocalDate initial_day = measurements.get(0).getRecord_time().toLocalDate();
		LocalDate last_day_plus_one = measurements.get(measurements.size()-1).getRecord_time().toLocalDate().plusDays(1);
		
		int count = 0;
		
		for (LocalDate curr_day = initial_day; curr_day.isBefore(last_day_plus_one); curr_day = curr_day.plusDays(1))
		{
			Day_24d current_24d = new Day_24d(this.id, curr_day);
			
			for (int j = 0; j < 24; j++)
			{
				Measurement curr_measurement = measurements.get(j+count);
				current_24d.addMeasurement(curr_measurement.getErlang(), curr_measurement.getRecord_time().getHour());
			}
			
			return_array.add(current_24d);
			
			count++;
		}
		
		return return_array;
	}

	public void addMeasurement(Measurement measurement)
	{
		this.measurements.add(measurement);
	}
}