package edu.unibonn.main;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.time.Day;
import org.jfree.ui.RefineryUtilities;

import au.com.bytecode.opencsv.CSVReader;
import edu.unibonn.clustering.kmeans.Cluster;
import edu.unibonn.clustering.kmeans.Day_24d;
import edu.unibonn.clustering.kmeans.KMeans_clustering;
import edu.unibonn.main.Sensor.Cell_type;
import edu.unibonn.plotting.TimeSeriesPlotter;

public class ValuesGeneratorMain
{
	public static void main(String[] args) throws Exception
	{
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		
		for (int i = 0; i < 18; i++)
		{
			sensors.add(new Sensor("Sensor_"+i));
		}
		
//		LocalDateTime from = LocalDateTime.of(2015, Month.OCTOBER, 26, 00, 00, 00);
//		System.out.println("FROM:"+from);
//		
//		LocalDateTime to = LocalDateTime.of(2015, Month.NOVEMBER, 9, 00, 00, 00);
//		System.out.println("TO:"+to);
		
		//new ValuesGeneratorMain().generateMesurements(from, to, sensors);
		
		String pathCSV = "input_data/real_data_time_series_Time_vs_Sensors.csv";
		
		boolean normalized = true;
		
		sensors = new ValuesGeneratorMain().loadDataCSV(pathCSV, normalized);
		
		LocalDateTime from = sensors.get(0).getMeasurements().get(0).getRecord_time();
		System.out.println("FROM:"+from);
		
		ArrayList<Measurement> max_measurement = sensors.get(sensors.size()-1).getMeasurements();		
		LocalDateTime to = max_measurement.get(max_measurement.size()-1).getRecord_time();
		System.out.println("TO:"+to);
		
		//Plot Sensors data:
		final TimeSeriesPlotter demo = new TimeSeriesPlotter("Sensor Working areas", sensors);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        
        int min_k = 2;
        int max_k = 15;
        
        int number_of_tries = 3;

        for (int current_k = min_k; current_k <= max_k; current_k++)
        {
        	for (int i = 1; i <= number_of_tries; i++)
        	{
        		ArrayList<Cluster> clusters = new KMeans_clustering().cluster_KMeans_euclidean_24d_specific_day(sensors, from, to, current_k, DayOfWeek.MONDAY); //day = 1 Because real data we have is from a Monday.

                final TimeSeriesPlotter demo_1 = new TimeSeriesPlotter("Clustering with k="+current_k+ " try number= "+ i , clusters, from);
                demo_1.pack();
                RefineryUtilities.centerFrameOnScreen(demo_1);
                demo_1.setVisible(true);
			}
		}
	}
	
	private ArrayList<Sensor> loadDataCSV(String dataFilePath, boolean normalized)
	{
		CSVReader reader = null;
		ArrayList<Sensor> return_matrix = new ArrayList<Sensor>();
		
		try
		{		
			reader = new CSVReader(new FileReader(dataFilePath), ';');
	
			List<String[]> pre_matrix = reader.readAll();

			int rows = pre_matrix.size();
			int columns = pre_matrix.get(0).length;
			
			String[] times = pre_matrix.get(0);

			for (int i = 1; i < rows; i++) 
			{		
				String[] actual_row = pre_matrix.get(i);

				Sensor current_sensor = new Sensor(actual_row[0]);
				
				if(!normalized)
				{
					for (int j = 1; j < columns; j++)
					{
						double current_erlang = Double.valueOf(actual_row[j].replace(",", "."));
						LocalDateTime current_record_time = LocalDateTime.parse(times[j], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
						current_sensor.addMeasurement(new Measurement(current_record_time, current_erlang));
					}
				}
				else
				{
					//NORMALIZE
					//1. Create a 24dim double vector with the absolute values.
					//2. Find the highest value, that is going to be 100.
					//3. Escalate all other values between 0 and 1.
					
					double[] absolute_values = new double[24];
					double current_max = 0;
					
					for (int j = 0; j < columns-1; j++)
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
							double current_erlang_normalized = (absolute_values[j]*100)/current_max;
							LocalDateTime current_record_time = LocalDateTime.parse(times[j+1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
							current_sensor.addMeasurement(new Measurement(current_record_time, current_erlang_normalized));
						}
					}
					else if(current_max == 0)
					{
						for (int j = 0; j < absolute_values.length; j++)
						{
							LocalDateTime current_record_time = LocalDateTime.parse(times[j+1], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
							current_sensor.addMeasurement(new Measurement(current_record_time, 0));
						}
					}
					else
					{
						System.out.println("ERROR ON DATA LOADING.(1)");
					}
				}
								
				return_matrix.add(current_sensor);
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
		
		return return_matrix;
	}

	private void generateMesurements(LocalDateTime from, LocalDateTime to, ArrayList<Sensor> sensors)
	{
		for (int i = 0; (i+3) <= sensors.size() ; i=i+3) 
		{
			Sensor current_sensor_A = sensors.get(i);
			current_sensor_A.setType(Cell_type.WORKING_AREA);
			current_sensor_A.generate_measurements_from_to_regarding_type(from, to);
			
			Sensor current_sensor_B = sensors.get(i+1);
			current_sensor_B.setType(Cell_type.RESIDENTIAL_AREA);
			current_sensor_B.generate_measurements_from_to_regarding_type(from, to);
			
			Sensor current_sensor_C = sensors.get(i+2);
			current_sensor_C.setType(Cell_type.RURAL_AREA);
			current_sensor_C.generate_measurements_from_to_regarding_type(from, to);
		}
	}
}
