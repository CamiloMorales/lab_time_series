package edu.unibonn.main;

import java.time.LocalDateTime;

import edu.unibonn.generator.SampleGenerator;

public class Sensor 
{
	static public enum Cell_type { WORKING_AREA, RESIDENTIAL_AREA, RURAL_AREA };
	
	private String id;
	private Cell_type type;
	private LocalDateTime initial_record_time;
	private short dimensions;
	private byte[] measurements;
	
	public Sensor(String id, short dimensions) 
	{
		this.id = id;
		this.dimensions = dimensions;
		this.measurements = new byte[this.dimensions];
	}

	public Sensor(String id, short dimensions, LocalDateTime from) 
	{
		this.id = id;
		this.initial_record_time = from;
		this.dimensions = dimensions;
		this.measurements = new byte[this.dimensions];
	}

	public String getId() 
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public Cell_type getType() 
	{
		return type;
	}

	public void setType(Cell_type type)
	{
		this.type = type;
	}
	
	public LocalDateTime getInitial_record_time()
	{
		return initial_record_time;
	}

	public void setInitial_record_time(LocalDateTime initial_record_time)
	{
		this.initial_record_time = initial_record_time;
	}

	public void generate_measurements()
	{
		if(this.dimensions <= 0)
		{
			System.out.println("ERROR GENERATING MEASUREMENTS in the SENSOR.");
		}
		
		for (int i = 0; i < this.dimensions; i++)
		{
			this.measurements[i] = (byte)SampleGenerator.getInstance().generateMeasurement(initial_record_time.plusHours(i), type);
		}
	}

	public void addMeasurement(int i, byte current_erlang)
	{
		this.measurements[i] = current_erlang;
	}

	public short getDimensions()
	{
		return this.dimensions;
	}

	public byte getMeasurement(short i)
	{
		return this.measurements[i];
	}
	
	public double euclidean_distance_to(Sensor to_point)
	{	
		double final_distance = 0;

		double sum_of_squared_differences = 0;
		
		for (short i = 0; i < to_point.getDimensions(); i++)
		{	
			sum_of_squared_differences += Math.pow(this.measurements[i]-to_point.getMeasurement(i), 2); //Sum of the square difference dimension by dimension.				
		}
		
		final_distance = Math.sqrt(sum_of_squared_differences);

		return final_distance;
	}
	
	public double Dynamic_Time_Warping_distance_to(Sensor to_point)
	{	
		short dimensions = to_point.getDimensions();
		
		double[][] squared_differences = new double[24][24];
		
		for (short x = 0; x < dimensions; x++)
		{	
			for (short y = 0; y < dimensions; y++)
			{	
				squared_differences[x][y] = Math.pow(this.measurements[x]-to_point.getMeasurement(y), 2);
			}					
		}

		double[][] accumulated_cost_matrix = new double[dimensions][dimensions];
		
		//We always start at point (0,0).
		accumulated_cost_matrix[0][0] = squared_differences[0][0];
		
		//First the first row
		for (short y = 1; y < dimensions; y++)
		{	
			accumulated_cost_matrix[0][y] = squared_differences[0][y] + accumulated_cost_matrix[0][y-1];
		}
		
		//Then the first Column
		for (short x = 1; x < dimensions; x++)
		{	
			accumulated_cost_matrix[x][0] = squared_differences[x][0] + accumulated_cost_matrix[x-1][0];
		}
		
		//Then the rest
		for (short x = 1; x < dimensions; x++)
		{	
			for (short y = 1; y < dimensions; y++)
			{	
				double diagonal_down = accumulated_cost_matrix[x-1][y-1];
				double down = accumulated_cost_matrix[x][y-1];
				double left = accumulated_cost_matrix[x-1][y];
				
				accumulated_cost_matrix[x][y] = Math.min(Math.min(diagonal_down, down), left) + squared_differences[x][y];
			}
		}
		
		return Math.sqrt(accumulated_cost_matrix[dimensions-1][dimensions-1]);
	}
	
	@Override
	public boolean equals(Object other)
	{
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Sensor))return false;
	    
	    Sensor otherMyClass = (Sensor)other;
	    
	    if(otherMyClass.id.equals(this.id))
	    {
	    	return true;
	    }
	    
	    return false;
	}

	public void setDimensionality(short dimensions)
	{
		this.dimensions = dimensions;
	}
}