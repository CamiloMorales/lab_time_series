package edu.unibonn.clustering.kmeans;

import java.time.LocalDate;
import java.util.ArrayList;
import edu.unibonn.main.Measurement;

public class Day_24d
{
	private String id;
	private LocalDate day;
	private double[] measurements;
	
	public Day_24d(String id, LocalDate day) {
		super();
		this.id = id;
		this.day = day;
		this.measurements = new double[24];
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public LocalDate getDay() {
		return day;
	}
	public void setDay(LocalDate day) {
		this.day = day;
	}

	public void addMeasurement(double measurement, int hour_index)
	{
		this.measurements[hour_index] = measurement;
	}
	
	public double getMeasurement(int hour_index)
	{
		return this.measurements[hour_index];
	}
}
