package edu.unibonn.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

import org.jfree.ui.RefineryUtilities;

import edu.unibonn.main.Measurement;
import edu.unibonn.main.Sensor;
import edu.unibonn.main.Sensor.Cell_type;
import edu.unibonn.plotting.TimeSeriesPlotter_Sensors;

public class BigDataGenerator {

	public static void main(String[] args) throws Exception 
	{
		LocalDateTime from = LocalDateTime.of(2015, Month.OCTOBER, 26, 00, 00, 00);
		System.out.println("FROM:"+from);
		
		LocalDateTime to = LocalDateTime.of(2015, Month.OCTOBER, 27, 00, 00, 00);
		System.out.println("TO:"+to);
		
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		
		for (int i = 0; i < 100000*3; i++)
		{
			sensors.add(new Sensor("Sensor_"+i));
		}
		
		generateMesurements(from,to,sensors);
		
		final TimeSeriesPlotter_Sensors demo_2 = new TimeSeriesPlotter_Sensors("Generated Sensors", sensors);
        demo_2.pack();
        RefineryUtilities.centerFrameOnScreen(demo_2);
        demo_2.setVisible(true);
        
        write_to_csv(sensors);
	}
	
	private static void write_to_csv(ArrayList<Sensor> sensors) throws IOException
	{
		String fileName = "./generated_sensor_values/"+"generation_"+sensors.size()+"_"+System.currentTimeMillis()+".csv";

		System.out.println(" -In \""+fileName+ "\""+":");

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
        try 
        {
        	fileWriter = new FileWriter(fileName);
        	bufferedWriter = new BufferedWriter(fileWriter);

        	bufferedWriter.write("Sensor vs Time;21.11.2011 00:00:00;21.11.2011 01:00:00;21.11.2011 02:00:00;21.11.2011 03:00:00;21.11.2011 04:00:00;21.11.2011 05:00:00;21.11.2011 06:00:00;21.11.2011 07:00:00;21.11.2011 08:00:00;21.11.2011 09:00:00;21.11.2011 10:00:00;21.11.2011 11:00:00;21.11.2011 12:00:00;21.11.2011 13:00:00;21.11.2011 14:00:00;21.11.2011 15:00:00;21.11.2011 16:00:00;21.11.2011 17:00:00;21.11.2011 18:00:00;21.11.2011 19:00:00;21.11.2011 20:00:00;21.11.2011 21:00:00;21.11.2011 22:00:00;21.11.2011 23:00:00");
        	bufferedWriter.newLine();
        	
            for (int i = 0; i < sensors.size(); i++)
            {
				Sensor current_sensor = sensors.get(i);
            	bufferedWriter.write(current_sensor.getId());
            	
            	ArrayList<Measurement> current_measurements = current_sensor.getMeasurements();
            	
            	for (int j = 0; j < current_measurements.size(); j++)
            	{
            		int erlang = new Double(current_measurements.get(j).getErlang()).intValue();
            		
            		bufferedWriter.write(";"+erlang);
				}
            	
            	bufferedWriter.newLine();
            	
            	double percentaje = (i+1.0)*100/sensors.size();
            	System.out.println("Writing file: "+ percentaje );
            	
            	if(percentaje%10 == 0)
            	{
            		System.out.println("Calling collector: "+ percentaje );
            		System.gc();
            	}
			}
            
            bufferedWriter.close();
        }
        catch(IOException ex)
        {
            System.out.println("Error writing to file '" + fileName + "'");
        }
        finally 
        {
        	bufferedWriter.close();
		}
	}

	private static void generateMesurements(LocalDateTime from, LocalDateTime to, ArrayList<Sensor> sensors)
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
			
			double percentaje = (i+1.0)*100/sensors.size();
        	System.out.println("Generating measurements: "+ percentaje );
			
			if(percentaje%10 == 0)
        	{
        		System.out.println("Calling collector: "+ percentaje );
        		System.gc();
        	}
		}
	}
}
