package edu.unibonn.plotting;

import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import edu.unibonn.clustering.model.Cluster_KMeans;
import edu.unibonn.clustering.model.Sensor;
import edu.unibonn.distances.histogram.RepeatedNumber;

public class DistancesHistogramPlotter extends ApplicationFrame
{
    public DistancesHistogramPlotter(String title, String pathOutputImages, List<RepeatedNumber> histogram)
    {
    	super(title);
 
        final DefaultCategoryDataset dataset = createDataset(histogram); 
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        
        chartPanel.setPreferredSize(new java.awt.Dimension(2560, 1600));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
        
        File outputPath = new File(pathOutputImages);
        
        try
        {
			ChartUtilities.saveChartAsJPEG(new File(outputPath.getAbsolutePath()+File.separator+title+".jpg"), chart, 1920, 1200);
		} 
        catch (Exception e)
        {
			e.printStackTrace();
		}
	}

	private DefaultCategoryDataset createDataset(List<RepeatedNumber> histrogram)
	{
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();  
	
		RepeatedNumber current_number = null;
		
	    for (int i = 0; i < histrogram.size(); i++)
	    {
	    	current_number = histrogram.get(i);
	    	dataset.addValue( current_number.getRepetitions(), String.valueOf(current_number.getNumber()), "repetitions" );	
		}
		
	    return dataset; 
    }
    
    private JFreeChart createChart(CategoryDataset dataset)
    {

    	final JFreeChart chart = ChartFactory.createBarChart(
            "Sensors",
            "Time", 
            "Erlang",
            dataset,
            PlotOrientation.VERTICAL, // the plot orientation
            false,                    // include legend
            false,
            false
        );

        return chart;   
	}
}