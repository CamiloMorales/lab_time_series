package edu.unibonn.main;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

import edu.unibonn.main.Sensor.Cell_type;

public class ValuesGeneratorMain
{
	public static void main(String[] args)
	{
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		
		for (int i = 0; i < 6; i++)
		{
			sensors.add(new Sensor("Sensor_"+i));
		}
		
		new ValuesGeneratorMain().generateMesurements(sensors);
	}
	
	private void generateMesurements(ArrayList<Sensor> sensors)
	{
		LocalDateTime from = LocalDateTime.of(2015, Month.OCTOBER, 25, 00, 00, 00);
		System.out.println("FROM:"+from);
		
		LocalDateTime to = LocalDateTime.of(2015, Month.NOVEMBER, 03, 00, 00, 00);
		System.out.println("TO:"+to);
		
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
