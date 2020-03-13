package com.nexteon.doit.core.entity;


/*
 * @author "Vikas Chauhan"
 * This class represents the request JSON body where each field denotes to the key in the request JSON.
 */
public class TableRequest {

	private String path;
	private String nodeName;
	private String propertyName;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
}
