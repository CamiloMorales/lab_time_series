package org.unibonn.sheet06.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import au.com.bytecode.opencsv.CSVReader;

public class KMeansMain {

	public static void main(String[] args) 
	{
		System.out.print("\nDATA MINING - Assignment 06: K-Means clustering.\n\n");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("- Select an option: \n");
		System.out.print("\t1- Perform K-Means clustering with Iris.data as input and Manhattan distance. (k=3) \n");
		System.out.print("\t2- Perform K-Means clustering with Iris.data as input and Euclidean distance. (k=3) \n");
		System.out.print("\t3- Perform 10x K-Means clustering (for both distances and k=3) generating  for each iteration 200 random samples according to 3 randomly generated Gaussian distrbutions, and compare the cluster centers obteained with the means of the Gaussian distributions.\n");
		System.out.print("\n\t Your option: \n");
		
        try
        {
        	int option = Integer.parseInt(br.readLine());
        	
        	int k = 3; //Number of clusters.
        	
        	if(option == 1) //Manhattan distance with Iris data.
        	{
        		String dataFilePath = "./input_data/iris.data";
        		
        		KMeansMain obj = new KMeansMain();
        		
        		double [][] data_matrix = obj.loadData(dataFilePath);
        		
        		List manhattan_final_clusters = obj.kmeansClustering(data_matrix, 1, k); //1= Manhattan distance, 3=k.
        		
        		System.out.println("\n- Finals clusters:");
        		
        		for (int i = 0; i < k; i++)
        		{
        			System.out.println("\t"+manhattan_final_clusters.get(i).toString());
				}
        		
        		double purity = calculatePurity(data_matrix, manhattan_final_clusters);
        		
        		System.out.println("\n\tPurity of this clustering is: "+purity);
        	}
        	else if(option == 2) //Euclidean distance with Iris data.
        	{
        		String dataFilePath = "./input_data/iris.data";
        		
        		KMeansMain obj = new KMeansMain();
        		
        		double [][] data_matrix = obj.loadData(dataFilePath);
        		
        		List euclidean_final_clusters = obj.kmeansClustering(data_matrix, 2, k); //1= Euclidean distance, 3=k.
        		
        		System.out.println("\n- Finals clusters:");
        		
        		for (int i = 0; i < k; i++)
        		{
        			System.out.println("\t"+euclidean_final_clusters.get(i).toString());
				}
        		
        		double purity = calculatePurity(data_matrix, euclidean_final_clusters);
        		
        		System.out.println("\n\tPurity: "+purity);
        	}
        	else if(option == 3)
        	{
        		for (int iter = 0; iter < 10; iter++)
        		{
        			KMeansMain obj = new KMeansMain();
            		
            		//1- Generate m1, m2, m3 and variance. (With uniform probability)
            		
            		double [] gaussians_means = new double[3]; 
            		
            		Random r = new Random();
            		
            		double m1 = 10 * r.nextDouble(); //Generates a uniformly random a double.
            		double m2 = 10 * r.nextDouble(); //Generates a uniformly random a double.
            		double m3 = 10 * r.nextDouble(); //Generates a uniformly random a double.
            	
//            		double m1 = 1;  //Just for testing.
//            		double m2 = 50; //Just for testing.
//            		double m3 = 99; //Just for testing.
            		
            		gaussians_means[0] = m1;
            		gaussians_means[1] = m2;
            		gaussians_means[2] = m3;
            		
            		double variance = 2 * r.nextDouble(); //Generates a uniformly random a double. (Between 0 and 2 to have a small variance)       		
            		
            		double [][] data_matrix = obj.randomlyGenerate200samples(gaussians_means, variance);
            		
            		System.out.println("\nIteration number:"+(iter+1)+"\n");
            		
            		System.out.println("Random Gaussians:\n");
            		System.out.println("1- Gaussian N("+m1+","+variance+")");
            		System.out.println("2- Gaussian N("+m2+","+variance+")");
            		System.out.println("3- Gaussian N("+m3+","+variance+")");
            		
            		System.out.println("\nResult of clustering with Manhattan distance:\n");
            		
            		List<Cluster> manhattan_final_clusters = obj.kmeansClustering(data_matrix, 1, k); //1= Manhattan distance, 3=k.
            		
            		System.out.println("- Results:");
            		
            		for (int i = 0; i < k; i++)
            		{
            			System.out.println("\t"+manhattan_final_clusters.get(i).toString());
    				}
            		
            		System.out.println("\nResult of clustering with Euclidean distance:\n");
            		
            		List<Cluster> euclidean_final_clusters = obj.kmeansClustering(data_matrix, 2, k); //2= Euclidean distance, 3=k.
            		
            		System.out.println("- Results:");
            		
            		for (int i = 0; i < k; i++)
            		{
            			System.out.println("\t"+euclidean_final_clusters.get(i).toString());
    				}
				}   
        		
        		System.out.println("\nAs a general observation, we see that the final centroids tend to approach to the Means of the Gaussians that generated the 1d-points.");
        	}
        	else
        	{
        		System.err.println("ERROR: INVALID OPTION.");
        	}
        }
        catch(Exception nfe)
        {
        	nfe.printStackTrace();
        }	
	}
	
	private static double calculatePurity(double[][] data_matrix, List<Cluster> clusters)
	{
		double purity = 0;
		double n = data_matrix.length; //Number of instances.
		
		for (int i = 0; i < 3; i++)
		{
			int max_intersection_with_species = clusters.get(i).getMaxIntersectionWithSpecies(data_matrix);
			
			purity += max_intersection_with_species;
		}
		
		return purity/n;
	}

	private double[][] randomlyGenerate200samples(double[] gaussians_means, double variance)
	{
		int number_of_instances = 200; //For readability.
		int dimensions = 2; //1 and an extra one that represents the Species (for this implementation of kmeans we need this column even if it is empty)
	
		double [][] data_matrix = new double [number_of_instances][dimensions];
			
		Random uniform = new Random();
		
		Random gaussian = new Random();
		
		for (int i = 0; i < number_of_instances; i++)
		{
		    int gaussian_index = uniform.nextInt(3);
			
			for (int j = 0; j < dimensions-1; j++) //We leave the species empty.
			{
				data_matrix[i][j] = Math.abs(gaussians_means[gaussian_index] + gaussian.nextGaussian() * variance);
			}
		}
		
		//this.printMatrix(data_matrix); //Just for testing.
		
		return data_matrix;
	}

	private List<Cluster> kmeansClustering(double[][] data_matrix, int typeOfDistance, int k)
	{
		//Algorithm:
		//1- We generate randomly (uniform) the initial clusters. (between 0 and 10 since the Iris data goes from 0.1 to less than 7.9)		
		List<Cluster> clusters = new ArrayList<Cluster>();
		
		int dimensions = data_matrix[0].length-1; //Just for readability. (We ignore the Species column for calculations)

		System.out.println("- Initial centroids: ");
		
		for (int i = 0; i < k; i++)
		{
			clusters.add(Cluster.generateUniformlyRandomCluster(dimensions));
			
			System.out.println("\tInitial centroid "+(i+1)+":" + clusters.get(i).getStringOfCentroid());
		}
		
//		double [] temp = new double[] {2.0}; //Just for testing.
//		clusters.get(0).setCentroid(temp); //Just for testing.
//		
//		double [] temp1 = new double[] {50.0}; //Just for testing.
//		clusters.get(1).setCentroid(temp1); //Just for testing.
//		
//		double [] temp2 = new double[] {98.0}; //Just for testing.
//		clusters.get(2).setCentroid(temp2); //Just for testing.
		
		//2-Until we get no improvement (quality dont improve anymore), do:
		
		double previous_total_clustering_squared_error = -1; //For initialization.
		double actual_total_clustering_squared_error = -1;//For initialization.
		
		while(actual_total_clustering_squared_error < previous_total_clustering_squared_error || actual_total_clustering_squared_error == -1)
		{
			//2.1- Iterate over all the points, for each point (in d dimensions) calculate the distance to each centroid, and assign it to the closest centroid.
			
			previous_total_clustering_squared_error = actual_total_clustering_squared_error; //Save the previous total square error for comparing at the end of this iteration.
			actual_total_clustering_squared_error = 0; //re-inititialize the total square error.
			
			for (int j = 0; j < k; j++) //Before each iteration we reset the membership vector of the clusters BUT not the new position (random in the first iteration). 
									    //That way we can re-calculate and re-assign the points to the new centroids and re-calculate the quality, and then decide (comparing with the qualty of the previous iteration) weather to iterate again or finish.
										//In the first iteration the position of the centroids are random, and the initial previous_clustering_quality (sum of the squared error of each cluster) is Infinity. (We guarantee at least the 2 iterations)
			{
				clusters.get(j).reset_membership_vector();
			}
			
			for (int i = 0; i < data_matrix.length; i++) //Iterate over all the points.
			{
				double current_closest_distance = Double.POSITIVE_INFINITY;
				int current_closest_cluster = -1;
				
				for (int j = 0; j < k; j++) //Iterate over all the clusters.
				{
					double actual_distance = clusters.get(j).distanceTo(data_matrix[i], typeOfDistance);
					
					if(actual_distance < current_closest_distance)
					{
						current_closest_distance = actual_distance;
						current_closest_cluster = j;
					}
				}
				
				clusters.get(current_closest_cluster).assignMembership(i);
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				clusters.get(i).recalculatePositionOfCentroid(data_matrix); //Means.
			}
			
			for (int i = 0; i < k; i++) //Iterate over all the clusters.
			{
				actual_total_clustering_squared_error += clusters.get(i).getClusterSquareError(data_matrix, typeOfDistance); //Calculate the new total clustering squared error.
			}
		}

		return clusters;
	}

	private double[][] loadData(String dataFilePath)
	{
		CSVReader reader = null;
		double [][] return_matrix = null;
		
		try
		{		
			reader = new CSVReader(new FileReader(dataFilePath), ',');
	
			List<String[]> pre_matrix = reader.readAll();

			int rows = pre_matrix.size();
			int columns = pre_matrix.get(0).length;
			
			return_matrix = new double[rows][columns];
			
			for (int i = 0; i < rows; i++) 
			{		
				String[] actual_row = pre_matrix.get(i);
				
				for (int j = 0; j < columns; j++)
				{
					if(j==columns-1) //Species column
					{
						if(actual_row[j].equals("Iris-setosa"))
						{
							return_matrix[i][j] = 1;
						}
						else if(actual_row[j].equals("Iris-versicolor"))
						{
							return_matrix[i][j] = 2;
						}
						else if(actual_row[j].equals("Iris-virginica"))
						{
							return_matrix[i][j] = 3;
						}
					}
					else
					{
						return_matrix[i][j] = Double.valueOf(actual_row[j]);
					}
				}
			}
			
			//printMatrix(data_matrix);
		}
		catch(Exception e) 
		{
		    System.err.println(e.getMessage());
		    e.printStackTrace();
		}
		finally
		{
			try 
			{
				reader.close();
			} 
			catch (IOException e) 
			{
				System.err.println(e.getMessage());
			}
		}
		
		return return_matrix;
	}
	
	private void printMatrix(double[][] data_matrix)
	{
		int rows = data_matrix.length;
		int columns = data_matrix[0].length;
		
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < columns; j++)
			{
				System.out.print(data_matrix[i][j]+",");
			}
			System.out.println();
		}
	}
}
