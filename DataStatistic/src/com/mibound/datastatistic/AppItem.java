package com.mibound.datastatistic;

public class AppItem {
	private int id;
	private String name;
	private String uid;
	private String packagename;
	private String starttime;
	private String stoptime;
	private long usage;
	
	public AppItem(int id, String name, String uid, String packagename,
			String starttime, String stoptime, long usage) {
		super();
		this.id = id;
		this.name = name;
		this.uid = uid;
		this.packagename = packagename;
		this.starttime = starttime;
		this.stoptime = stoptime;
		this.usage = usage;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getPackagename() {
		return packagename;
	}
	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getStoptime() {
		return stoptime;
	}
	public void setStoptime(String stoptime) {
		this.stoptime = stoptime;
	}
	public long getUsage() {
		return usage;
	}
	public void setUsage(long usage) {
		this.usage = usage;
	}
	
}
