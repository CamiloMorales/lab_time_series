package edu.unibonn.plotting;

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

import edu.unibonn.clustering.dbscan.Cluster_DBScan;
import edu.unibonn.clustering.kmeans.Cluster_KMeans;
import edu.unibonn.main.Sensor;

public class TimeSeriesPlotter_Sensors extends ApplicationFrame
{
    public TimeSeriesPlotter_Sensors(String title, ArrayList<Sensor> sensors)
    {
    	super(title);
        final XYDataset dataset = createDataset(sensors);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
	}
    
    private JFreeChart createChart(final XYDataset dataset) {

        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Sensors",
            "Time", "Erlang",
            dataset,
            false,
            true,
            false
        );

        chart.setBackgroundPaint(Color.white);

//        final StandardLegend sl = (StandardLegend) chart.getLegend();
//        sl.setDisplaySeriesShapes(true);

        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
//        plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        final XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
            //rr.setPlotShapes(true);
            rr.setShapesFilled(true);
            rr.setItemLabelsVisible(true);
        }
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy hh:mm"));
        
        return chart;

    }
    
	private XYDataset createDataset(ArrayList<Cluster_KMeans> clusters, LocalDateTime from)
	{
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		for (int i = 0; i < clusters.size(); i++)
		{
			Cluster_KMeans current_cluster = clusters.get(i);	
			ArrayList<Sensor> member_time_series = current_cluster.getMembership();

			for (Iterator iterator = member_time_series.iterator(); iterator.hasNext();)
			{
				final TimeSeries s1 = new TimeSeries("Cluster_"+current_cluster.getCluster_id(), Hour.class);
				
				Sensor current_series = (Sensor) iterator.next();
				
				for (int j = 0; j < current_series.getDimensions(); j++)
				{
					LocalDateTime current_record_time = current_series.getInitial_record_time().plusHours(j);
					s1.add(new Hour( current_record_time.getHour(), current_record_time.getDayOfMonth(), current_record_time.getMonthValue(), current_record_time.getYear() ), current_series.getMeasurement(j));
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
		{
			Sensor current_sensor = sensors.get(i);	
			
			final TimeSeries s1 = new TimeSeries("Sensor "+i, Hour.class);

			for (int j = 0; j < current_sensor.getDimensions(); j++)
			{
				LocalDateTime current_record_time = current_sensor.getInitial_record_time().plusHours(j);
				s1.add(new Hour( current_record_time.getHour(), current_record_time.getDayOfMonth(), current_record_time.getMonthValue(), current_record_time.getYear() ), current_sensor.getMeasurement(j));
			}
			
			dataset.addSeries(s1);
		}

        dataset.setDomainIsPointsInTime(true);

        return dataset;
	}

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
			ArrayList<Sensor> member_time_series = current_cluster.getMembership();
			
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
}