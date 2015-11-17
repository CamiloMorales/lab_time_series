package edu.unibonn.generator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Random;

import edu.unibonn.main.Measurement;
import edu.unibonn.main.Sensor.Cell_type;

public class SampleGenerator
{
	static public enum Segment_of_day { MORNING, AFTERNOON, NIGHT };
	static public enum Segment_of_week { WORKDAY, WEEKEND };
	
	private static SampleGenerator instance = null;
	
	/*
	 * Snippet from: http://stackoverflow.com/questions/21674599/generating-a-lognormal-distribution-from-an-array-in-java
	 */
	public static double LogNormal(double mean, double stddev) 
	{
	    Random randGen = new Random();
	    
	    double varx = Math.pow(stddev, 2);
	    double ess = Math.log(1.0 + (varx/Math.pow(mean,2)));
	    double mu = Math.log(mean) - (0.5*Math.pow(ess, 2));
	    
	    double rand_log_normal_num = Math.pow(Math.E, (mu+(ess*randGen.nextGaussian())));
	    
	    if(rand_log_normal_num > 100)
	    {
	    	return 100;
	    }
	    
	    return rand_log_normal_num;
	}
	
	/*
	 * Snippet from: http://stackoverflow.com/questions/5853187/skewing-java-random-number-generation-toward-a-certain-number 
	 */
    static public double nextSkewedBoundedDouble(double min, double max, double skew, double bias)
    {
    	Random randGen = new Random();
    	
    	double range = max - min;
        double mid = min + range / 2.0;
        double unitGaussian = randGen.nextGaussian();
        double biasFactor = Math.exp(bias);
        double retval = mid+(range*(biasFactor/(biasFactor+Math.exp(-unitGaussian/skew))-0.5));
        return retval;
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
		if(type == Cell_type.WORKING_AREA)
		{
			//Check if working day or weekend
			if(isWorkingDay(current_time.getDayOfWeek()))
			{
				//Check if the time belongs to the Morning, afternoon or night.
				int segment_of_day = getSegmentOfDay(current_time.getHour()); //0=Morning (00:00-08:00), 1=Afternoon (08:00-20:00), 2=Night (20:00-00:00)
				
				if(segment_of_day == 0)
				{
					double sample = generateSample(Cell_type.WORKING_AREA, Segment_of_week.WORKDAY, Segment_of_day.MORNING); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 1)
				{
					double sample = generateSample(Cell_type.WORKING_AREA, Segment_of_week.WORKDAY, Segment_of_day.AFTERNOON); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 2)
				{
					double sample = generateSample(Cell_type.WORKING_AREA, Segment_of_week.WORKDAY, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
			}
			else //Weekend
			{
				//Check if the time belongs to the Morning, afternoon or night.
				int segment_of_day = getSegmentOfDay(current_time.getHour()); //0=Morning (00:00-08:00), 1=Afternoon (08:00-20:00), 2=Night (20:00-00:00)
				
				if(segment_of_day == 0)
				{
					double sample = generateSample(Cell_type.WORKING_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 1)
				{
					double sample = generateSample(Cell_type.WORKING_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 2)
				{
					double sample = generateSample(Cell_type.WORKING_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
			}
		}
		else if(type == Cell_type.RESIDENTIAL_AREA)
		{
			//Check if working day or weekend
			if(isWorkingDay(current_time.getDayOfWeek()))
			{
				//Check if the time belongs to the Morning, afternoon or night.
				int segment_of_day = getSegmentOfDay(current_time.getHour()); //0=Morning (00:00-08:00), 1=Afternoon (08:00-20:00), 2=Night (20:00-00:00)
				
				if(segment_of_day == 0)
				{
					double sample = generateSample(Cell_type.RESIDENTIAL_AREA, Segment_of_week.WORKDAY, Segment_of_day.MORNING); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 1)
				{
					double sample = generateSample(Cell_type.RESIDENTIAL_AREA, Segment_of_week.WORKDAY, Segment_of_day.AFTERNOON); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 2)
				{
					double sample = generateSample(Cell_type.RESIDENTIAL_AREA, Segment_of_week.WORKDAY, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
			}
			else //Weekend
			{
				//Check if the time belongs to the Morning, afternoon or night.
				int segment_of_day = getSegmentOfDay(current_time.getHour()); //0=Morning (00:00-08:00), 1=Afternoon (08:00-20:00), 2=Night (20:00-00:00)
				
				if(segment_of_day == 0)
				{
					double sample = generateSample(Cell_type.RESIDENTIAL_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 1)
				{
					double sample = generateSample(Cell_type.RESIDENTIAL_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 2)
				{
					double sample = generateSample(Cell_type.RESIDENTIAL_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
			}
		}
		else if(type == Cell_type.RURAL_AREA)
		{
			//Check if working day or weekend
			if(isWorkingDay(current_time.getDayOfWeek()))
			{
				//Check if the time belongs to the Morning, afternoon or night.
				int segment_of_day = getSegmentOfDay(current_time.getHour()); //0=Morning (00:00-08:00), 1=Afternoon (08:00-20:00), 2=Night (20:00-00:00)
				
				if(segment_of_day == 0)
				{
					double sample = generateSample(Cell_type.RURAL_AREA, Segment_of_week.WORKDAY, Segment_of_day.MORNING); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 1)
				{
					double sample = generateSample(Cell_type.RURAL_AREA, Segment_of_week.WORKDAY, Segment_of_day.AFTERNOON); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 2)
				{
					double sample = generateSample(Cell_type.RURAL_AREA, Segment_of_week.WORKDAY, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
			}
			else //Weekend
			{
				//Check if the time belongs to the Morning, afternoon or night.
				int segment_of_day = getSegmentOfDay(current_time.getHour()); //0=Morning (00:00-08:00), 1=Afternoon (08:00-20:00), 2=Night (20:00-00:00)
				
				if(segment_of_day == 0)
				{
					double sample = generateSample(Cell_type.RURAL_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 1)
				{
					double sample = generateSample(Cell_type.RURAL_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
				else if(segment_of_day == 2)
				{
					double sample = generateSample(Cell_type.RURAL_AREA, Segment_of_week.WEEKEND, Segment_of_day.NIGHT); 
					return new Measurement(current_time, sample);
				}
			}
		}

		return null; //Error
	}

	private static double generateSample(Cell_type workingArea, Segment_of_week workday, Segment_of_day morning)
	{
		int general_std_dev = 2;
		
		if(workingArea == Cell_type.WORKING_AREA)
		{
			 if(workday == Segment_of_week.WORKDAY)
			 {
				 if(morning == Segment_of_day.MORNING)
				 {
					 return LogNormal(1, general_std_dev);
				 }
				 else if(morning == Segment_of_day.AFTERNOON)
				 {
					 return LogNormal(50, general_std_dev);
				 }
				 else if(morning == Segment_of_day.NIGHT)
				 {
					 return LogNormal(5, general_std_dev);
				 }
			 }
			 else if(workday == Segment_of_week.WEEKEND)
			 {
				 if(morning == Segment_of_day.MORNING)
				 {
					 return LogNormal(5, general_std_dev);
				 }
				 else if(morning == Segment_of_day.AFTERNOON)
				 {
					 return LogNormal(5, general_std_dev);
				 }
				 else if(morning == Segment_of_day.NIGHT)
				 {
					 return LogNormal(5, general_std_dev);
				 }
			 } 
		}
		else if(workingArea == Cell_type.RESIDENTIAL_AREA)
		{
			if(workday == Segment_of_week.WORKDAY)
			 {
				 if(morning == Segment_of_day.MORNING)
				 {
					 return LogNormal(2, general_std_dev);
				 }
				 else if(morning == Segment_of_day.AFTERNOON)
				 {
					 return LogNormal(10, general_std_dev);
				 }
				 else if(morning == Segment_of_day.NIGHT)
				 {
					 return LogNormal(40, general_std_dev);
				 }
			 }
			 else if(workday == Segment_of_week.WEEKEND)
			 {
				 if(morning == Segment_of_day.MORNING)
				 {
					 return LogNormal(10, general_std_dev);
				 }
				 else if(morning == Segment_of_day.AFTERNOON)
				 {
					 return LogNormal(18, general_std_dev);
				 }
				 else if(morning == Segment_of_day.NIGHT)
				 {
					 return LogNormal(14, general_std_dev);
				 }
			 }
		}
		else if(workingArea == Cell_type.RURAL_AREA)
		{
			if(workday == Segment_of_week.WORKDAY)
			 {
				 if(morning == Segment_of_day.MORNING)
				 {
					 return LogNormal(45, general_std_dev);
				 }
				 else if(morning == Segment_of_day.AFTERNOON)
				 {
					 return LogNormal(10, general_std_dev);
				 }
				 else if(morning == Segment_of_day.NIGHT)
				 {
					 return LogNormal(5, general_std_dev);
				 }
			 }
			 else if(workday == Segment_of_week.WEEKEND)
			 {
				 if(morning == Segment_of_day.MORNING)
				 {
					 return LogNormal(8, general_std_dev);
				 }
				 else if(morning == Segment_of_day.AFTERNOON)
				 {
					 return LogNormal(8, general_std_dev);
				 }
				 else if(morning == Segment_of_day.NIGHT)
				 {
					 return LogNormal(5, general_std_dev);
				 }
			 }
		}
		
		return -1; //Error
	}

	private static int getSegmentOfDay(int hour)
	{
		int return_segment = -1;
		
		if(hour >= 0 && hour < 8)
		{
			return_segment = 0; //Morning
		}
		else if(hour >= 8 && hour < 20)
		{
			return_segment =  1; //Afternoon
		}
		if(hour >= 20)
		{
			return_segment =  2; //Night
		}
		
		return return_segment;
	}

	private static boolean isWorkingDay(DayOfWeek dayOfWeek)
	{
		boolean is_working_day = false;
		
		if(  dayOfWeek.equals(DayOfWeek.MONDAY)
		  || dayOfWeek.equals(DayOfWeek.TUESDAY) 
		  || dayOfWeek.equals(DayOfWeek.WEDNESDAY)
		  || dayOfWeek.equals(DayOfWeek.THURSDAY)
		  || dayOfWeek.equals(DayOfWeek.FRIDAY))
		{
			is_working_day = true;
		}
		
		return is_working_day;
	}
}
