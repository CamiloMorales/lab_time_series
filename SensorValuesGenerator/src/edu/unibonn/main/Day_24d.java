package edu.unibonn.main;

import java.time.LocalDate;
import java.util.ArrayList;

public class Day_24d
{
	private String sensor_id;
	private LocalDate day; //Conjunction between sensor_id AND day must be unique. (See get_point_unique_id())
	private double[] measurements;
	
	public Day_24d(String id, LocalDate day) {
		this.sensor_id = id;
		this.day = day;
		this.measurements = new double[24];
	}
	public String getId() {
		return sensor_id;
	}
	public void setId(String id) {
		this.sensor_id = id;
	}
	public LocalDate getDay() {
		return day;
	}
	public void setDay(LocalDate day) {
		this.day = day;
	}
	
	public String get_point_unique_id()
	{
		return this.sensor_id+"-"+day.toString();
	}

	public void addMeasurement(double measurement, int hour_index)
	{
		this.measurements[hour_index] = measurement;
	}
	
	public double getMeasurement(int hour_index)
	{
		return this.measurements[hour_index];
	}
	
	public double euclidean_distance_to(Day_24d to_point)
	{	
		double final_distance = 0;

		double sum_of_squared_differences = 0;
		
		for (int i = 0; i < 24; i++)
		{	
			sum_of_squared_differences += Math.pow(this.measurements[i]-to_point.getMeasurement(i), 2); //Sum of the square difference dimension by dimension.				
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
				squared_differences[x][y] = Math.pow(this.measurements[x]-to_point.getMeasurement(y), 2);
			}					
		}

		double[][] accumulated_cost_matrix = new double[24][24];
		
		//We always start at point (0,0).
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
				double diagonal_down = accumulated_cost_matrix[x-1][y-1];
				double down = accumulated_cost_matrix[x][y-1];
				double left = accumulated_cost_matrix[x-1][y];
				
				accumulated_cost_matrix[x][y] = Math.min(Math.min(diagonal_down, down), left) + squared_differences[x][y];
			}
		}
		
		return Math.sqrt(accumulated_cost_matrix[23][23]);
	}
	
	@Override
	public boolean equals(Object other)
	{
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Day_24d))return false;
	    
	    Day_24d otherMyClass = (Day_24d)other;
	    
	    if(otherMyClass.get_point_unique_id() == this.get_point_unique_id())
	    {
	    	return true;
	    }
	    
	    return false;
	}
}
