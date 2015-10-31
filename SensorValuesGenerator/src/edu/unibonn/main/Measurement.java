package edu.unibonn.main;

import java.time.LocalDateTime;

public class Measurement
{
	private LocalDateTime record_time;
	private double erlang;
	
	public LocalDateTime getRecord_time()
	{
		return record_time;
	}

	public void setRecord_time(LocalDateTime record_time)
	{
		this.record_time = record_time;
	}

	public double getErlang()
	{
		return erlang;
	}
	
	public void setErlang(double erlang)
	{
		this.erlang = erlang;
	}
}
