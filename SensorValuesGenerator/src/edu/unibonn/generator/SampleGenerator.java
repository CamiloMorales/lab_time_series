package edu.unibonn.generator;

import java.time.LocalDateTime;
import java.util.Random;

import edu.unibonn.main.Measurement;
import edu.unibonn.main.Sensor.Cell_type;

public class SampleGenerator
{
	private static SampleGenerator instance = null;
	
	private SampleGenerator() 
	{
		
	}
	
	/*
	 * From: http://stackoverflow.com/questions/21674599/generating-a-lognormal-distribution-from-an-array-in-java
	 */
	public static double LogNormal(double mean, double stddev) 
	{
	    Random randGen = new Random();
	    
	    double varx = Math.pow(stddev, 2);
	    double ess = Math.log(1.0 + (varx/Math.pow(mean,2)));
	    double mu = Math.log(mean) - (0.5*Math.pow(ess, 2));
	    return Math.pow(Math.E, (mu+(ess*randGen.nextGaussian())));
	}
	
	public static SampleGenerator getInstance()
	{
		if(instance == null)
		{
		   instance = new SampleGenerator();
		}
		  
		return instance;
	}
	
	public static Measurement generateMeasurement(LocalDateTime current_time, Cell_type type)
	{
		Measurement generated_meaurement = new Measurement();
		
		if(type == Cell_type.WORKING_AREA)
		{
			
		}
		else if(type == Cell_type.RESIDENTIAL_AREA)
		{
			
		}
		else if(type == Cell_type.RURAL_AREA)
		{
			
		}

		return generated_meaurement;
	}
	
}
