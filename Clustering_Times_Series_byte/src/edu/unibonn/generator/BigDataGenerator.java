package edu.unibonn.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import edu.unibonn.main.Sensor;
import edu.unibonn.main.Sensor.Cell_type;
import edu.unibonn.plotting.TimeSeriesPlotter_Sensors;

public class BigDataGenerator {

	public static void main(String[] args) throws Exception 
	{
		long initial_time = System.currentTimeMillis();
		
		int dimensions = 365*24;
		
		int scale = 4*1;
		
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		
		LocalDateTime from = LocalDateTime.of(2015, Month.JANUARY, 01, 00, 00, 00);
		System.out.println("FROM:"+from.format(format));
		
		LocalDateTime to = LocalDateTime.of(2016, Month.JANUARY, 01, 00, 00, 00);
		System.out.println("TO:"+to.format(format));
		
		for (int i = 0; i <= 1; i++)
		{
			ArrayList<Sensor> sensors = new ArrayList<Sensor>();
			
			for (int j = 0; j < scale*3; j++)
			{
				sensors.add(new Sensor("Sensor_"+ (i*(scale*3)+j), dimensions, from));
			}
			
			generateMesurements(sensors);

			write_to_csv(from, to, "./generated_sensor_values/"+(i+1)+".csv", sensors, (i == 0 ? true: false), i*(scale*3), scale*3);
			
			Thread.sleep(3000);
		}
		
		long final_time = System.currentTimeMillis();
		
		System.out.println("Execution took: "+((double)(final_time-initial_time)/1000));
		
		//generateMesurements(from,to,sensors);
		
//		final TimeSeriesPlotter_Sensors demo_2 = new TimeSeriesPlotter_Sensors("Generated Sensors", sensors);
//        demo_2.pack();
//        RefineryUtilities.centerFrameOnScreen(demo_2);
//        demo_2.setVisible(true);
	}
	
	private static void write_to_csv(LocalDateTime from, LocalDateTime to, String fileName, ArrayList<Sensor> sensors, boolean headers, int sensor_start, int total_lines) throws IOException
	{
		//String fileName = "./generated_sensor_values/"+"generation_"+sensors.size()+"_sensor_start_"+sensor_start+"_total_lines_"+total_lines+"_"+System.currentTimeMillis()+".csv";
		
		System.out.println(" -In \""+fileName+ "\""+":");

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
        try 
        {
        	fileWriter = new FileWriter(fileName);
        	bufferedWriter = new BufferedWriter(fileWriter);

        	if(headers)
        	{
        		bufferedWriter.write("Sensor vs Time;");//;21.11.2011 00:00:00;21.11.2011 01:00:00;21.11.2011 02:00:00;21.11.2011 03:00:00;21.11.2011 04:00:00;21.11.2011 05:00:00;21.11.2011 06:00:00;21.11.2011 07:00:00;21.11.2011 08:00:00;21.11.2011 09:00:00;21.11.2011 10:00:00;21.11.2011 11:00:00;21.11.2011 12:00:00;21.11.2011 13:00:00;21.11.2011 14:00:00;21.11.2011 15:00:00;21.11.2011 16:00:00;21.11.2011 17:00:00;21.11.2011 18:00:00;21.11.2011 19:00:00;21.11.2011 20:00:00;21.11.2011 21:00:00;21.11.2011 22:00:00;21.11.2011 23:00:00");
            	
            	LocalDateTime current_time = from;
            	DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            	
        		while(current_time.isBefore(to)) 
        		{
        			bufferedWriter.write(current_time.format(format)+";");
        			current_time = current_time.plusHours(1);
        		}
        		bufferedWriter.newLine();
        	}

            for (int i = 0; i < sensors.size(); i++)
            {
				Sensor current_sensor = sensors.get(i);
            	bufferedWriter.write(current_sensor.getId());

            	for (int j = 0; j < current_sensor.getDimensions(); j++)
            	{
            		bufferedWriter.write(";"+current_sensor.getMeasurement(j));
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

	private static void generateMesurements(ArrayList<Sensor> sensors) throws Exception
	{
		for (int i = 0; (i+3) <= sensors.size() ; i=i+3) 
		{
			Sensor current_sensor_A = sensors.get(i);
			current_sensor_A.setType(Cell_type.WORKING_AREA);
			current_sensor_A.generate_measurements();
			
			Sensor current_sensor_B = sensors.get(i+1);
			current_sensor_B.setType(Cell_type.RESIDENTIAL_AREA);
			current_sensor_B.generate_measurements();
			
			Sensor current_sensor_C = sensors.get(i+2);
			current_sensor_C.setType(Cell_type.RURAL_AREA);
			current_sensor_C.generate_measurements();
			
			double percentaje = (float)i*100/sensors.size();
        	System.out.println("Generating measurements: "+ percentaje );
			
			if(percentaje%10 == 0)
        	{
        		System.out.println("Calling collector: "+ percentaje );
        		System.gc();
        		Thread.sleep(1000);
        	}
		}
	}
	
	
}
