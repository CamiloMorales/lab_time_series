package edu.unibonn.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BigFileAppender {

	public static void main(String[] args)
	{
		String path_file_1="./generated_sensor_values/";
		//String path_file_2="./generated_sensor_values/2.csv";
		
		BigFileAppender fa = new BigFileAppender();
		
		for (int i = 1; i <= 99; i++)
		{
			fa.appendFiles(path_file_1+"1.csv", path_file_1+(i+1)+".csv");
		}		
	}

	private void appendFiles(String path_file_1, String path_file_2)
	{
		try
		{
		    Files.write(Paths.get(path_file_1), Files.readAllBytes(Paths.get(path_file_2)), StandardOpenOption.APPEND);
		}
		catch (IOException e)
		{
		    System.out.println("ERROR APPENDING FILES!");
		}
	}
}
