package edu.unibonn.plotting;

import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import edu.unibonn.clustering.kmeans.Cluster_KMeans;
import edu.unibonn.main.Sensor;

public class TimeSeriesPlotter_KMeans extends ApplicationFrame
{
    public TimeSeriesPlotter_KMeans(String title, ArrayList<Cluster_KMeans> clusters, LocalDateTime from)
    {
    	super(title);
        final XYDataset dataset = createDataset_clusters(clusters, from);     
        final XYDataset dataset_cetroids = createDataset_centroids(clusters, from); 
        final JFreeChart chart = createChart(dataset, dataset_cetroids, clusters);
        final ChartPanel chartPanel = new ChartPanel(chart);
        
        chartPanel.setPreferredSize(new java.awt.Dimension(2560, 1600));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
        
        try
        {
			ChartUtilities.saveChartAsJPEG(new File("./jpgs/"+title+".jpg"), chart, 65500, 1200);
		} 
        catch (Exception e)
        {
			e.printStackTrace();
		}
	}

    private XYDataset createDataset_centroids(ArrayList<Cluster_KMeans> clusters, LocalDateTime from)
	{
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		for (int i = 0; i < clusters.size(); i++)
		{
			Cluster_KMeans current_cluster = clusters.get(i);	
			double[] center_of_mass = current_cluster.getRecalculated_center_of_mass();
			
			final TimeSeries s1 = new TimeSeries("Cluster_"+current_cluster.getCluster_id(), Hour.class);

			for (int j = 0; j < current_cluster.getDimensionality(); j++)
			{
				LocalDateTime current_record_time = from.plusHours(j);
				
				s1.add(new Hour( current_record_time.getHour(), current_record_time.getDayOfMonth(), current_record_time.getMonthValue(), current_record_time.getYear() ), center_of_mass[j]);
			}
			
			dataset.addSeries(s1);
		}
		
        dataset.setDomainIsPointsInTime(true);

        return dataset;
	}
    
	private XYDataset createDataset_clusters(ArrayList<Cluster_KMeans> clusters, LocalDateTime from)
	{
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		for (int i = 0; i < clusters.size(); i++)
		{
			Cluster_KMeans current_cluster = clusters.get(i);	
			HashSet<String> member_time_series = current_cluster.getMembership();

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
			HashSet<String> member_time_series = current_cluster.getMembership();
			
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