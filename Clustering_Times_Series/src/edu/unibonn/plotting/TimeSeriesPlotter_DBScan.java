package edu.unibonn.plotting;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import edu.unibonn.clustering.model.Cluster_DBScan;
import edu.unibonn.clustering.model.Sensor;

/**
 * An example of a time series chart.  For the most part, default settings are used, except that
 * the renderer is modified to show filled shapes (as well as lines) at each data point.
 *
 */
public class TimeSeriesPlotter_DBScan extends ApplicationFrame
{    
    public TimeSeriesPlotter_DBScan(String title, ArrayList<Cluster_DBScan> clusters, LocalDateTime from)
    {
    	super(title);
        final XYDataset dataset = createDataset(clusters);     
        //final XYDataset dataset_cetroids = createDataset_centroids(clusters, from); 
        final JFreeChart chart = createChart(dataset, clusters);
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
    
	private XYDataset createDataset(ArrayList<Cluster_DBScan> clusters)
	{
		int dimensionality = clusters.get(0).getDimensionality();
		
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		for (int i = 0; i < clusters.size(); i++)
		{
			Cluster_DBScan current_cluster = clusters.get(i);	
			ArrayList<Sensor> member_time_series = current_cluster.getMembership();

			for (Iterator iterator = member_time_series.iterator(); iterator.hasNext();)
			{
				final TimeSeries s1 = new TimeSeries("Cluster_"+current_cluster.getCluster_id(), Hour.class);
				
				Sensor current_series = (Sensor) iterator.next();
				
				for (int j = 0; j < dimensionality; j++)
				{
					LocalDateTime current_record_time = current_cluster.getInitial_record_time().plusHours(j);
					
					s1.add(new Hour( current_record_time.getHour(), current_record_time.getDayOfMonth(), current_record_time.getMonthValue(), current_record_time.getYear() ), current_series.getMeasurement(j));
				}

				dataset.addSeries(s1);
			}
		}

        dataset.setDomainIsPointsInTime(true);

        return dataset;
	}

    private JFreeChart createChart(final XYDataset dataset, ArrayList<Cluster_DBScan> clusters)
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
			Cluster_DBScan current_cluster = clusters.get(i);	
			ArrayList<Sensor> member_time_series = current_cluster.getMembership();
			
			for (int j = 0; j < member_time_series.size(); j++)
			{
				renderer.setSeriesPaint(j+temp_count, getColor(i));
			}
			temp_count = temp_count + member_time_series.size();
		}
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();    
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        
        final ValueAxis axis_y = plot.getRangeAxis();
        axis_y.setRange(0, 100);
        
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
	        default: curr_color = Color.WHITE;
	                 break;
    	}
    	
    	return curr_color;
	}
}