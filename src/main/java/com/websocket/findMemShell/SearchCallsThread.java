package com.websocket.findMemShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SearchCallsThread extends Thread{
	Map<String,List<String>> discoveredCalls;
	String sinkMethod = "java/lang/Runtime#exec";
	Stack stack = new Stack();
	int count = 0;
	List<String> visitedClass = new ArrayList<>();
	
	public SearchCallsThread(Map<String,List<String>> discoveredCalls) {
		this.discoveredCalls = discoveredCalls;
	}
	
	@Override
    public void run() {
		while(true) {
			List<ConfigPath> result = getWsConfigResult.getWsConfig();
			if(result != null && result.size() != 0) {
				for(ConfigPath cp : result) {
					System.out.println("WsConfig Class: \n"+cp.getClassName().replaceAll("\\.", "/")+"#onMessage"+"\n");
					
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
