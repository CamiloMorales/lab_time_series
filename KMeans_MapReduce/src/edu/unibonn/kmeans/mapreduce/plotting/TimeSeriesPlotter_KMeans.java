package edu.unibonn.kmeans.mapreduce.plotting;

import java.awt.BasicStroke;
/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -------------------
 * TimeSeriesDemo.java
 * -------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TimeSeriesDemo.java,v 1.19 2004/04/26 19:12:03 taqua Exp $
 *
 * Changes
 * -------
 * 08-Apr-2002 : Version 1 (DG);
 * 25-Jun-2002 : Removed unnecessary import (DG);
 *
 */
import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
//import org.jfree.ui.Spacer;
import org.jfree.util.Log;
import org.jfree.util.PrintStreamLogTarget;

import edu.unibonn.kmeans.mapreduce.utils.Cluster_KMeans;
import edu.unibonn.kmeans.mapreduce.utils.Day_24d;
import edu.unibonn.kmeans.mapreduce.utils.Measurement;
import edu.unibonn.kmeans.mapreduce.utils.Sensor;


/**
 * An example of a time series chart.  For the most part, default settings are used, except that
 * the renderer is modified to show filled shapes (as well as lines) at each data point.
 *
 */
public class TimeSeriesPlotter_KMeans extends ApplicationFrame {

    /**
     * A demonstration application showing how to create a simple time series chart.  This
     * example uses monthly data.
     *
     * @param title  the frame title.
     */
//    public TimeSeriesPlotter(final String title) {
//        
//        super(title);
//        final XYDataset dataset = createDataset();
//        final JFreeChart chart = createChart(dataset);
//        final ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
//        chartPanel.setMouseZoomable(true, false);
//        
//        setContentPane(chartPanel);
//
//    }

//    public TimeSeriesPlotter(String title, ArrayList<Sensor> sensors)
//    {
//    	super(title);
//        final XYDataset dataset = createDataset(sensors);
//        final JFreeChart chart = createChart(dataset);
//        final ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
//        chartPanel.setMouseZoomable(true, false);
//        setContentPane(chartPanel);
//	}
    
    public TimeSeriesPlotter_KMeans(String title, ArrayList<Cluster_KMeans> clusters, LocalDateTime from)
    {
    	super(title);
        final XYDataset dataset = createDataset(clusters, from);     
        final XYDataset dataset_cetroids = createDataset_centroids(clusters, from); 
        final JFreeChart chart = createChart(dataset, dataset_cetroids, clusters);
        final ChartPanel chartPanel = new ChartPanel(chart);
        
        chartPanel.setPreferredSize(new java.awt.Dimension(2560, 1600));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
        
        try
        {
			ChartUtilities.saveChartAsJPEG(new File("./jpgs/"+title+".jpg"), chart, 1920, 1200);
		} 
        catch (Exception e)
        {
			e.printStackTrace();
		}
	}

//	public TimeSeriesPlotter(String title, ArrayList<Cluster_DBScan> clusters, LocalDateTime from)
//	{
//		super(title);
//		final XYDataset dataset = createDataset(clusters, from);     
//		final JFreeChart chart = createChart(dataset, clusters);
//		final ChartPanel chartPanel = new ChartPanel(chart);
//		
//		chartPanel.setPreferredSize(new java.awt.Dimension(2560, 1600));
//		chartPanel.setMouseZoomable(true, false);
//		setContentPane(chartPanel);
//		  
//		try
//		{
//			ChartUtilities.saveChartAsJPEG(new File("./jpgs/"+title+".jpg"), chart, 1920, 1200);
//		} 
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}

//	private XYDataset createDataset(ArrayList<Cluster_DBScan> clusters, LocalDateTime from)
//	{
//		final TimeSeriesCollection dataset = new TimeSeriesCollection();
//		
//		for (int i = 0; i < clusters.size(); i++)
//		{
//			Cluster_DBScan current_cluster = clusters.get(i);	
//			ArrayList<Day_24d> member_time_series = current_cluster.getMembership();
//
//			for (Iterator iterator = member_time_series.iterator(); iterator.hasNext();)
//			{
//				final TimeSeries s1 = new TimeSeries("Cluster_"+current_cluster.getCluster_id(), Hour.class);
//				
//				Day_24d current_series = (Day_24d) iterator.next();
//				
//				for (int j = 0; j < 24; j++)
//				{
//					s1.add(new Hour( j, from.getDayOfMonth(), from.getMonthValue(), from.getYear() ), current_series.getMeasurement(j));
//				}
//
//				dataset.addSeries(s1);
//			}
//		}
//
//        dataset.setDomainIsPointsInTime(true);
//
//        return dataset;
//	}

    private XYDataset createDataset_centroids(ArrayList<Cluster_KMeans> clusters, LocalDateTime from)
	{
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		for (int i = 0; i < clusters.size(); i++)
		{
			Cluster_KMeans current_cluster = clusters.get(i);	
			double[] center_of_mass = current_cluster.getCenter_of_mass();
			
			final TimeSeries s1 = new TimeSeries("Cluster_"+current_cluster.getCluster_id(), Hour.class);

			for (int j = 0; j < 24; j++)
			{
				s1.add(new Hour( j, from.getDayOfMonth(), from.getMonthValue(), from.getYear() ), center_of_mass[j]);
			}
			
			dataset.addSeries(s1);
		}
		
        dataset.setDomainIsPointsInTime(true);

        return dataset;
	}
    
	private XYDataset createDataset(ArrayList<Cluster_KMeans> clusters, LocalDateTime from)
	{
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
//		for (int i = 0; i < clusters.size(); i++)
//		{
//			Cluster_KMeans current_cluster = clusters.get(i);	
//			double[] center_of_mass = current_cluster.getCenter_of_mass();
//			
//			final TimeSeries s1 = new TimeSeries("Cluster_"+current_cluster.getCluster_id(), Hour.class);
//
//			for (int j = 0; j < 24; j++)
//			{
//				s1.add(new Hour( j, from.getDayOfMonth(), from.getMonthValue(), from.getYear() ), center_of_mass[j]);
//			}
//			
//			dataset.addSeries(s1);
//		}
		
		for (int i = 0; i < clusters.size(); i++)
		{
			Cluster_KMeans current_cluster = clusters.get(i);	
			ArrayList<Day_24d> member_time_series = current_cluster.getMembership();

			for (Iterator iterator = member_time_series.iterator(); iterator.hasNext();)
			{
				final TimeSeries s1 = new TimeSeries("Cluster_"+current_cluster.getCluster_id(), Hour.class);
				
				Day_24d current_series = (Day_24d) iterator.next();
				
				for (int j = 0; j < 24; j++)
				{
					s1.add(new Hour( j, from.getDayOfMonth(), from.getMonthValue(), from.getYear() ), current_series.getMeasurement(j));
				}

				dataset.addSeries(s1);
			}
		}

        dataset.setDomainIsPointsInTime(true);

        return dataset;
	}

	private XYDataset createDataset(ArrayList<Sensor> sensors)
	{
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		for (int i = 0; i < sensors.size(); i++)
		//for (int i = 0; i < 100; i++)
		{
			Sensor current_sensor = sensors.get(i);	
			ArrayList<Measurement> current_measurements = current_sensor.getMeasurements();
			
			final TimeSeries s1 = new TimeSeries("Sensor "+i, Hour.class);

			for (int j = 0; j < current_measurements.size(); j++)
			{
				Measurement current_measurement = current_measurements.get(j);
				
				LocalDateTime current_time = current_measurement.getRecord_time();
				
				s1.add(new Hour( current_time.getHour(), current_time.getDayOfMonth(), current_time.getMonthValue(), current_time.getYear() ), current_measurement.getErlang());
			}
			
			dataset.addSeries(s1);
		}

        dataset.setDomainIsPointsInTime(true);

        return dataset;
	}

	/**
     * Creates a chart.
     * 
     * @param dataset  a dataset.
	 * @param clusters 
     * 
     * @return A chart.
     */
//    private JFreeChart createChart(final XYDataset dataset, ArrayList<Cluster_DBScan> clusters) {
//
//        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
//            "Sensors",
//            "Time", "Erlang",
//            dataset,
//            false, //t
//            true, //t
//            false //f
//        );
//
//        ChartUtilities.applyCurrentTheme(chart);
//        
//        //chart.setBackgroundPaint(Color.white);
//
////        final StandardLegend sl = (StandardLegend) chart.getLegend();
////        sl.setDisplaySeriesShapes(true);
//
//        final XYPlot plot = chart.getXYPlot();
//        plot.setBackgroundPaint(Color.WHITE);
//        plot.setDomainGridlinePaint(Color.white);
//        plot.setRangeGridlinePaint(Color.white);
////        plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
//        plot.setDomainCrosshairVisible(true);
//        plot.setRangeCrosshairVisible(true);
//        
//        final XYItemRenderer renderer = plot.getRenderer();
//        
//        if (renderer instanceof StandardXYItemRenderer) 
//        {
//            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
//            //rr.setPlotShapes(true);
//            rr.setShapesFilled(true);
//            rr.setItemLabelsVisible(true);
//        }
//
//        int temp_count = 0;
//        
//        //for (int i = 0; i < clusters.size(); i++)
//        for (int i = 0; (i < 11) && (i < clusters.size()); i++)
//		{
//			Cluster_DBScan current_cluster = clusters.get(i);	
//			ArrayList<Day_24d> member_time_series = current_cluster.getMembership();
//			
//			for (int j = 0; j < member_time_series.size(); j++)
//			{
//				renderer.setSeriesPaint(j+temp_count, getColor(i));
//			}
//			temp_count = temp_count + member_time_series.size();
//		}
//        
//        final DateAxis axis = (DateAxis) plot.getDomainAxis();    
//        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
//        
//        final ValueAxis axis_y = plot.getRangeAxis();
//        axis_y.setRange(0, 100);
//        
//        return chart;
//
//    }
    
    private JFreeChart createChart(final XYDataset dataset, final XYDataset dataset_centroids, ArrayList<Cluster_KMeans> clusters)
    {
    	final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Sensors",
            "Time", "Erlang",
            dataset,
            false, //t
            true, //t
            false //f
        );
        
        ChartUtilities.applyCurrentTheme(chart);
        
        //chart.setBackgroundPaint(Color.white);

//        final StandardLegend sl = (StandardLegend) chart.getLegend();
//        sl.setDisplaySeriesShapes(true);

        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
//        plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        final XYItemRenderer renderer = plot.getRenderer();
        
        if (renderer instanceof StandardXYItemRenderer) 
        {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
            //rr.setPlotShapes(true);
            rr.setShapesFilled(true);
            rr.setItemLabelsVisible(true);
        }

        int temp_count = 0;
        
        //for (int i = 0; i < clusters.size(); i++)
        for (int i = 0; (i < 11) && (i < clusters.size()); i++)
		{
			Cluster_KMeans current_cluster = clusters.get(i);	
			ArrayList<Day_24d> member_time_series = current_cluster.getMembership();
			
			for (int j = 0; j < member_time_series.size(); j++)
			{
				renderer.setSeriesPaint(j+temp_count, getColor(i));
			}
			temp_count = temp_count + member_time_series.size();
		}
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();    
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));

        //final ValueAxis axis_y = plot.getRangeAxis();
        //axis_y.setRange(0, 20);
        
        plot.setDataset(1,dataset_centroids);
        plot.setRenderer(1, new StandardXYItemRenderer());
        
        for (int i = 0; (i < clusters.size()); i++)
		{
        	//plot.getRenderer(1).setSeriesPaint(i, getColor(i));
        	plot.getRenderer(1).setSeriesPaint(i, Color.BLACK);
        	plot.getRenderer(1).setSeriesStroke(i, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,1.0f, new float[] {10.0f, 6.0f}, 0.0f));
		}

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        
        return chart;
    }  
    
    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    private Paint getColor(int color_number) 
    {
    	Color curr_color = null;
    	
    	switch (color_number) 
    	{
    	    case 0:  curr_color = Color.BLUE;
	                 break;
	        case 1:  curr_color = Color.GREEN;
	                 break;
	        case 2:  curr_color = Color.RED;
	                 break;
	        case 3:  curr_color = Color.PINK;
	                 break;
	        case 4:  curr_color = Color.MAGENTA;
   		 			 break; 
	        case 5:  curr_color = Color.CYAN;
	                 break;
	        case 6:  curr_color = Color.DARK_GRAY;
	                 break;
	        case 7:  curr_color = Color.LIGHT_GRAY;
	                 break;
	        case 8:  curr_color = Color.YELLOW;
	                 break;
	        case 9:  curr_color = Color.ORANGE;
	                 break;
	        case 10: curr_color = Color.BLACK;
	                 break;
//	        case 11: curr_color = Color.YELLOW;
//	                 break;
//	        case 12: curr_color = Color.BLACK;
//	                 break;
//	        case 13: curr_color = Color.BLACK;
//            		break;
//	        case 14: curr_color = Color.BLACK;
//            		break;
//	        case 15: curr_color = Color.BLACK;
//    				break;
//	        case 16: curr_color = Color.BLACK;
//    				break;
//	        case 17: curr_color = Color.BLACK;
//    				break;
//	        case 18: curr_color = Color.BLACK;
//    				break;
//	        case 20: curr_color = Color.BLACK;
//    				break;
//	        case 21: curr_color = Color.BLACK;
//    				break;
//	        case 22: curr_color = Color.BLACK;
//    				break;
//	        case 23: curr_color = Color.BLACK;
//    				break;
	        default: curr_color = Color.WHITE;
	                 break;
    	}
    	
    	return curr_color;
	}

	/**
     * Creates a dataset, consisting of two series of monthly data.
     *
     * @return the dataset.
     */
    private XYDataset createDataset() {

        final TimeSeries s1 = new TimeSeries("L&G European Index Trust", Month.class);
        s1.add(new Month(2, 2001), 181.8);
        s1.add(new Month(3, 2001), 167.3);
        s1.add(new Month(4, 2001), 153.8);
        s1.add(new Month(5, 2001), 167.6);
        s1.add(new Month(6, 2001), 158.8);
        s1.add(new Month(7, 2001), 148.3);
        s1.add(new Month(8, 2001), 153.9);
        s1.add(new Month(9, 2001), 142.7);
        s1.add(new Month(10, 2001), 123.2);
        s1.add(new Month(11, 2001), 131.8);
        s1.add(new Month(12, 2001), 139.6);
        s1.add(new Month(1, 2002), 142.9);
        s1.add(new Month(2, 2002), 138.7);
        s1.add(new Month(3, 2002), 137.3);
        s1.add(new Month(4, 2002), 143.9);
        s1.add(new Month(5, 2002), 139.8);
        s1.add(new Month(6, 2002), 137.0);
        s1.add(new Month(7, 2002), 132.8);

        final TimeSeries s2 = new TimeSeries("L&G UK Index Trust", Month.class);
        s2.add(new Month(2, 2001), 129.6);
        s2.add(new Month(3, 2001), 123.2);
        s2.add(new Month(4, 2001), 117.2);
        s2.add(new Month(5, 2001), 124.1);
        s2.add(new Month(6, 2001), 122.6);
        s2.add(new Month(7, 2001), 119.2);
        s2.add(new Month(8, 2001), 116.5);
        s2.add(new Month(9, 2001), 112.7);
        s2.add(new Month(10, 2001), 101.5);
        s2.add(new Month(11, 2001), 106.1);
        s2.add(new Month(12, 2001), 110.3);
        s2.add(new Month(1, 2002), 111.7);
        s2.add(new Month(2, 2002), 111.0);
        s2.add(new Month(3, 2002), 109.6);
        s2.add(new Month(4, 2002), 113.2);
        s2.add(new Month(5, 2002), 111.6);
        s2.add(new Month(6, 2002), 108.8);
        s2.add(new Month(7, 2002), 101.6);

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
        dataset.addSeries(s2);

        dataset.setDomainIsPointsInTime(true);

        return dataset;

    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {

//        Log.getInstance().addTarget(new PrintStreamLogTarget());
//        final TimeSeriesPlotter demo = new TimeSeriesPlotter("Time Series Demo 1");
//        demo.pack();
//        RefineryUtilities.centerFrameOnScreen(demo);
//        demo.setVisible(true);

    }

}