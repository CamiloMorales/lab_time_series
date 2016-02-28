package edu.unibonn.distances.histogram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.ui.RefineryUtilities;

import au.com.bytecode.opencsv.CSVReader;
import edu.unibonn.clustering.model.Sensor;
import edu.unibonn.plotting.DistancesHistogramPlotter;

public class DistancesHistogram
{
	public static void main(String[] args)
	{
		String dataFilePath = args[0];
		boolean normalized = (Integer.valueOf(args[1]) == 0? true: false); //0-NORMALIZE, 1-NOT NORMALIZE.
		String outputPath = args[2];
		
		CSVReader reader = null;
		ArrayList<Sensor> return_matrix = new ArrayList<Sensor>();
		
		try
		{		
			reader = new CSVReader(new FileReader(dataFilePath), ';');
	
			try(BufferedReader br = new BufferedReader(new FileReader(dataFilePath))) 
			{	
				String[] times_row = br.readLine().split(";");
				
				int dimensions = times_row.length-1;
				
				LocalDateTime initial_record_time = LocalDateTime.parse(times_row[1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
				
				int counter_i = 1;
				
				String[] actual_row = null;
				Sensor current_sensor = null;
				
				for(String line; (line = br.readLine()) != null; )
			    {
					actual_row = line.split(";");

					current_sensor = new Sensor(actual_row[0], dimensions, initial_record_time);
					
					if(!normalized)
					{
						for (int j = 1; j <= dimensions; j++)
						{
							current_sensor.addMeasurement(j-1, Double.valueOf(actual_row[j].replace(",", ".")));
						}
					}
					else
					{
						//NORMALIZE
						//1. Create a 24dim double vector with the absolute values.
						//2. Find the highest value, that is going to be 100.
						//3. Escalate all other values between 0 and 1.
						
						double[] absolute_values = new double[dimensions];
						double current_max = 0;
						
						for (int j = 0; j < dimensions; j++)
						{
							double current_erlang = Double.valueOf(actual_row[j+1].replace(",", "."));
							absolute_values[j] = current_erlang;
							
							if(current_max < current_erlang)
							{
								current_max = current_erlang;
							}
						}
						
						//Because of sensor: DXB971A (and maybe more).
						if(current_max > 0)
						{
							for (int j = 0; j < absolute_values.length; j++)
							{
								current_sensor.addMeasurement(j, (absolute_values[j]*100)/current_max);
							}
						}
						else if(current_max == 0)
						{
							for (int j = 0; j < absolute_values.length; j++)
							{
								current_sensor.addMeasurement(j, 0);
							}
						}
						else
						{
							System.out.println("ERROR ON DATA LOADING.(1)");
						}
					}
									
					return_matrix.add(current_sensor);

			    	if(counter_i%100 == 0)
			    	{
			    		System.out.println("Reading file, row"+ counter_i);
			    	}
			    	
			    	counter_i++;
			    }
			}

			//printMatrix(data_matrix);
		}
		catch(Exception e) 
		{
		    System.err.println(e.getMessage());
		    e.printStackTrace();
		}
		finally
		{
			try 
			{
				reader.close();
			} 
			catch (IOException e) 
			{
				System.err.println(e.getMessage());
			}
		}
		
		//
		List<RepeatedNumber> histogram = new ArrayList<>();
		int current_distance = 0;
		
		Sensor current_i = null;
		
		for (int i = 0; i < return_matrix.size(); i++)
		{
			current_i = return_matrix.get(i);
			
			for (int j = 0; j < return_matrix.size(); j++)
			{
				current_distance = (int)current_i.euclidean_distance_to(return_matrix.get(j)); //An approximation, to get smaller histograms.
				
				if(current_distance > 0 && histogram.contains(new RepeatedNumber(current_distance)))
				{
					for (int j2 = 0; j2 < histogram.size(); j2++) 
					{
						if(histogram.get(j2).equals(new RepeatedNumber(current_distance)))
						{
							histogram.get(j2).addRepetition();
						}
					}
				}
				else if(current_distance > 0)
				{
					histogram.add(new RepeatedNumber(current_distance));
				}
			}
		}
		
		for (int i = 0; i < histogram.size(); i++)
		{
			histogram.get(i).normalize();
		}
		
		Collections.sort(histogram);
		
		DistancesHistogramPlotter chart = new DistancesHistogramPlotter("Distances Histrogram", outputPath, histogram);
		
//		chart.pack( );        
//		RefineryUtilities.centerFrameOnScreen( chart );
//		chart.setVisible( true );	
	}
}