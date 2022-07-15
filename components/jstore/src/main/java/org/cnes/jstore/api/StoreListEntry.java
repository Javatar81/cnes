package org.cnes.jstore.api;

public class StoreListEntry {

	private String name;
	private long size;
	
	public StoreListEntry() {
		super();
	}
	public StoreListEntry(String name, long size) {
		super();
		this.name = name;
		this.size = size;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	

}
