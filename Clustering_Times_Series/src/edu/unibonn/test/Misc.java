package edu.unibonn.test;

import java.util.ArrayList;
import java.util.Random;

public class Misc {

	public static void main(String[] args) 
	{
		new Misc().tests();
	}

	private void tests()
	{
		double[] test = new double[100];
		ArrayList<Double> toPlot = new ArrayList<Double>();
		
		for (int i = 0; i < test.length; i++) 
		{
			test[i] = LogNormal(10, 10);
			
			if(toPlot.contains(new Double(test[i]).intValue()))
			{
				
			}
			
			//System.out.println(new Double(LogNormal(10, 10)).intValue());
		}
	}
	
	public static double LogNormal(double mean, double stddev) 
	{
	    Random randGen = new Random();
	    
	    double varx = Math.pow(stddev, 2);
	    double ess = Math.log(1.0 + (varx/Math.pow(mean,2)));
	    double mu = Math.log(mean) - (0.5*Math.pow(ess, 2));
	    return Math.pow(Math.E, (mu+(ess*randGen.nextGaussian())));
	}

}
