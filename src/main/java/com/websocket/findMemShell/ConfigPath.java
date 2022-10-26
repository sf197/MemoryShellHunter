package com.websocket.findMemShell;

public class ConfigPath {

	String path;
	String className;
	
	public ConfigPath(String path,String className) {
		this.path = path;
		this.className = className;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
}
