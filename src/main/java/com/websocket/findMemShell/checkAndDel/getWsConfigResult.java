package com.websocket.findMemShell.checkAndDel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.websocket.findMemShell.App;
import com.websocket.findMemShell.ConfigPath;

public class getWsConfigResult {
	
	public static boolean deleteConfig(String className) {
		try {
			Object servletContext = App.servletContext;
			if(servletContext == null) {
				return false;
			}
			Method getAttribute = servletContext.getClass().getClassLoader().loadClass("org.apache.catalina.core.ApplicationContextFacade").getDeclaredMethod("getAttribute", String.class);
			Object wsServerContainer = getAttribute.invoke(servletContext, "javax.websocket.server.ServerContainer");
			
			Class<?> obj = servletContext.getClass().getClassLoader().loadClass("org.apache.tomcat.websocket.server.WsServerContainer");
		    Field field = obj.getDeclaredField("configExactMatchMap");
		    field.setAccessible(true);
		    ConcurrentHashMap  configMap = (ConcurrentHashMap) field.get(wsServerContainer);
			
		    if(configMap.containsKey(className)){
                configMap.remove(className);
                return true;
            }
		    
			return false;
		}catch(Exception e) {
			return false;
		}
	}
	
	public static void getWsConfig(List<ConfigPath> classList) {
		try {
			Object servletContext = App.servletContext;
			if(servletContext == null) {
				return;
			}
			//System.out.println("servletContext ClassLoader: "+servletContext.getClass().getClassLoader());
			Method getAttribute = servletContext.getClass().getClassLoader().loadClass("org.apache.catalina.core.ApplicationContextFacade").getDeclaredMethod("getAttribute", String.class);
			Object wsServerContainer = getAttribute.invoke(servletContext, "javax.websocket.server.ServerContainer");
			
			Class<?> obj = servletContext.getClass().getClassLoader().loadClass("org.apache.tomcat.websocket.server.WsServerContainer");
		    Field field = obj.getDeclaredField("configExactMatchMap");
		    field.setAccessible(true);
		    Map<String, Object> configExactMatchMap = (Map<String, Object>) field.get(wsServerContainer);
		    
		    // ??????configExactMatchMap, ????????????????????? websocket ??????
		    Set<String> keyset = configExactMatchMap.keySet();
		    for (String key : keyset) {
		    	System.out.println("configExactMatchMap key:" + key);
		    	Object object = servletContext.getClass().getClassLoader().loadClass("org.apache.tomcat.websocket.server.WsServerContainer").getDeclaredMethod("findMapping", String.class).invoke(wsServerContainer, key);
		        Class<?> wsMappingResultObj = servletContext.getClass().getClassLoader().loadClass("org.apache.tomcat.websocket.server.WsMappingResult");
		        Field configField = wsMappingResultObj.getDeclaredField("config");
		        configField.setAccessible(true);
		        Object serverEndpointConfig = configField.get(object);
		        
		        Class<?> clazz = (Class<?>) servletContext.getClass().getClassLoader().loadClass("javax.websocket.server.ServerEndpointConfig").getDeclaredMethod("getEndpointClass").invoke(serverEndpointConfig);
		        ConfigPath cp = new ConfigPath(key,clazz.getName());
		        classList.add(cp);
		    }
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
