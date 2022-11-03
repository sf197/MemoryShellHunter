package com.websocket.findMemShell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.websocket.findMemShell.checkAndDel.getControllerResult;
import com.websocket.findMemShell.checkAndDel.getWsConfigResult;

public class SearchCallsThread extends Thread{
	Map<String,List<String>> discoveredCalls;
	String sinkMethod = "java/lang/Runtime#exec";
	Stack stack = new Stack();
	int count = 0;
	List<String> visitedClass = new ArrayList<>();
	
	public SearchCallsThread(Map<String,List<String>> discoveredCalls) {
		this.discoveredCalls = discoveredCalls;
	}
	
	public void checkWsConfig(ConfigPath cp) {
		//System.out.println("WsConfig Class: \n"+cp.getClassName().replaceAll("\\.", "/")+"#onMessage"+"\n");
		
		if(discoveredCalls.containsKey(cp.getClassName().replaceAll("\\.", "/")+"#onMessage")) {
			List<String> list = discoveredCalls.get(cp.getClassName().replaceAll("\\.", "/")+"#onMessage");
	        for(String str : list) {
	        	if(dfsSearchSink(str)) {
	        		stack.push(str);
	        		stack.push(cp.getClassName().replaceAll("\\.", "/")+"#onMessage");
	        		StringBuilder sb = new StringBuilder();
	        		while(!stack.empty()) {
	        			sb.append("->");
	        			sb.append(stack.pop());
	        		}
	        		System.out.println("CallEdge: "+sb.toString());
	        		if(getWsConfigResult.deleteConfig(cp.getPath())) {
	        			System.out.println("Delete Class "+cp.getPath()+" Succeed");
	        		}else {
	        			System.out.println("Delete Class "+cp.getPath()+" Failed");
	        		}
	        		break;
	        	}
	        }
		}
	}
	
	public void checkControllerPath(ConfigPath cp) {
//		//内存马常规检测方式
//		String className = cp.getClassName().split("#")[0];
//		String classNamePath = className.replace(".", "/") + ".class";
//        URL is = App.servletContext.getClass().getClassLoader().getResource(classNamePath);
//        if (is == null) {
//            return "在磁盘上没有对应class文件，可能是内存马";
//        } else {
//            return is.getPath();
//        }
		
		//System.out.println("Controller Class: \n"+cp.getClassName().replaceAll("\\.", "/"));
		
		if(discoveredCalls.containsKey(cp.getClassName().replaceAll("\\.", "/"))) {
			List<String> list = discoveredCalls.get(cp.getClassName().replaceAll("\\.", "/"));
	        for(String str : list) {
	        	if(dfsSearchSink(str)) {
	        		stack.push(str);
	        		stack.push(cp.getClassName().replaceAll("\\.", "/"));
	        		StringBuilder sb = new StringBuilder();
	        		while(!stack.empty()) {
	        			sb.append("->");
	        			sb.append(stack.pop());
	        		}
	        		System.out.println("Controller CallEdge: "+sb.toString());
	        		break;
	        	}
	        }
		}
	}
	
	@Override
    public void run() {
		while(true) {
			List<ConfigPath> result = getWsConfigResult.getWsConfig();
			getControllerResult.getControllerMemShell(result);
			if(result != null && result.size() != 0) {
				for(ConfigPath cp : result) {
					if(!cp.getClassName().contains("#")) {
						checkWsConfig(cp);		//Check WebSocket Memory Shell
					}else {
						//Normal Memory Shell Checked
						checkControllerPath(cp);
					}
				}
			}
			
			System.out.println("Thread-"+count+" Running...");
			try {
				count++;
				Thread.sleep(20000);		//间隔20秒探测
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
	
	public boolean dfsSearchSink(String enterMethod) {
		if(discoveredCalls.containsKey(enterMethod) && !visitedClass.contains(enterMethod)) {
			visitedClass.add(enterMethod);
			List<String> list = discoveredCalls.get(enterMethod);
			for(String m:list) {
				if(m.equals(sinkMethod)) {
					stack.push(m);
					return true;
				}
				if(dfsSearchSink(m)) {
					stack.push(m);
					return true;
				}
			}
			return false;
		}else {
			return false;
		}
	}
}
