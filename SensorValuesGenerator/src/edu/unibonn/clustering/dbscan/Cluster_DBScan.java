package edu.unibonn.clustering.dbscan;

import java.util.ArrayList;
import edu.unibonn.clustering.dbscan.DBScan_clustering.Type_of_cluster;
import edu.unibonn.main.Day_24d;

public class Cluster_DBScan
{
	private String cluster_id;
	private Type_of_cluster type;
	private ArrayList<Day_24d> membership;
	
	public Cluster_DBScan(String cluster_id) {
		this.cluster_id = cluster_id;
		this.membership = new ArrayList<Day_24d>();
	}
	
	public Cluster_DBScan(String cluster_id, Day_24d day_24d)
	{
		this.cluster_id = cluster_id;
		this.membership = new ArrayList<Day_24d>();
	}

	public String getCluster_id() {
		return cluster_id;
	}
	public void setCluster_id(String cluster_id) {
		this.cluster_id = cluster_id;
	}

	public ArrayList<Day_24d> getMembership() {
		return membership;
	}
	public void setMembership(ArrayList<Day_24d> membership) {
		this.membership = membership;
	}

	public void reset_membership_vector()
	{
		this.membership = new ArrayList<Day_24d>();
	}

	public void addMembership(Day_24d day_24d)
	{
		this.membership.add(day_24d);
	}
}
