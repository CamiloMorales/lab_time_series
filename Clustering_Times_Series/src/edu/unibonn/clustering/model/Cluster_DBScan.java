package edu.unibonn.clustering.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import edu.unibonn.clustering.dbscan.DBScan_clustering;
import edu.unibonn.clustering.dbscan.DBScan_clustering.Type_of_cluster;

public class Cluster_DBScan
{
	private String cluster_id;
	private Type_of_cluster type;
	private ArrayList<Sensor> membership;
	
	public Cluster_DBScan(String cluster_id) {
		this.cluster_id = cluster_id;
		this.membership = new ArrayList<Sensor>();
	}
	
	public Cluster_DBScan(String cluster_id, Sensor point_d)
	{
		this.cluster_id = cluster_id;
		this.membership = new ArrayList<Sensor>();
	}

	public String getCluster_id() {
		return cluster_id;
	}
	public void setCluster_id(String cluster_id) {
		this.cluster_id = cluster_id;
	}

	public ArrayList<Sensor> getMembership() {
		return membership;
	}
	public void setMembership(ArrayList<Sensor> membership) {
		this.membership = membership;
	}

	public void reset_membership_vector()
	{
		this.membership = new ArrayList<Sensor>();
	}

	public void addMembership(Sensor point_d)
	{
		this.membership.add(point_d);
	}
	
	public int getDimensionality()
	{
		return this.membership.get(0).getDimensions();
	}

	public LocalDateTime getInitial_record_time()
	{
		return this.membership.get(0).getInitial_record_time();
	}
}
