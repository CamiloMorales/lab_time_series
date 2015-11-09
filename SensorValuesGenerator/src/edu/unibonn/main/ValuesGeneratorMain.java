package edu.unibonn.main;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

import org.jfree.data.time.Day;
import org.jfree.ui.RefineryUtilities;

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
		
		LocalDateTime from = LocalDateTime.of(2015, Month.OCTOBER, 26, 00, 00, 00);
		System.out.println("FROM:"+from);
		
		LocalDateTime to = LocalDateTime.of(2015, Month.NOVEMBER, 9, 00, 00, 00);
		System.out.println("TO:"+to);
		
		new ValuesGeneratorMain().generateMesurements(from, to, sensors);
		
		//Plot Sensors data:
		final TimeSeriesPlotter demo = new TimeSeriesPlotter("Sensor Working areas", sensors);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        
        int k = 3; //Number of clusters
        
        ArrayList<Cluster> clusters = new KMeans_clustering().cluster_KMeans_euclidean_24d_montags(sensors, from, to, k);
        
        final TimeSeriesPlotter demo_1 = new TimeSeriesPlotter("Clustering", clusters, from);
        demo_1.pack();
        RefineryUtilities.centerFrameOnScreen(demo_1);
        demo_1.setVisible(true);
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
