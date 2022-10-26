package com.websocket.findMemShell;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
	
	public static List<ConfigPath> getWsConfig() {
		try {
			Object servletContext = App.servletContext;
			if(servletContext == null) {
				return null;
			}
			List<ConfigPath> classList = new ArrayList<>();
			//System.out.println("servletContext ClassLoader: "+servletContext.getClass().getClassLoader());
			Method getAttribute = servletContext.getClass().getClassLoader().loadClass("org.apache.catalina.core.ApplicationContextFacade").getDeclaredMethod("getAttribute", String.class);
			Object wsServerContainer = getAttribute.invoke(servletContext, "javax.websocket.server.ServerContainer");
			
			Class<?> obj = servletContext.getClass().getClassLoader().loadClass("org.apache.tomcat.websocket.server.WsServerContainer");
		    Field field = obj.getDeclaredField("configExactMatchMap");
		    field.setAccessible(true);
		    Map<String, Object> configExactMatchMap = (Map<String, Object>) field.get(wsServerContainer);
		    
		    // 遍历configExactMatchMap, 打印所有注册的 websocket 服务
		    Set<String> keyset = configExactMatchMap.keySet();
		    StringBuilder sb = new StringBuilder();
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
		    return classList;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
