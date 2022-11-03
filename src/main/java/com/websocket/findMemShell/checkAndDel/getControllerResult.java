package com.websocket.findMemShell.checkAndDel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.websocket.findMemShell.App;
import com.websocket.findMemShell.ConfigPath;

public class getControllerResult {
	public static void getControllerMemShell(List<ConfigPath> classList) {
		try {
			Object servletContext = App.servletContext;
			if(servletContext == null) {
				return;
			}
			Method getAttribute = servletContext.getClass().getClassLoader().loadClass("org.apache.catalina.core.ApplicationContextFacade").getDeclaredMethod("getAttribute", String.class);
			Object context = getAttribute.invoke(servletContext, "org.springframework.web.context.WebApplicationContext.ROOT");
			Method getBean = servletContext.getClass().getClassLoader().loadClass("org.springframework.beans.factory.BeanFactory").getDeclaredMethod("getBean", Class.class);
			Class requestMappingHandlerMapping = servletContext.getClass().getClassLoader().loadClass("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping");
			Object mappingHandlerMapping = getBean.invoke(context, requestMappingHandlerMapping);
			
			Field mappingRegistryField = servletContext.getClass().getClassLoader().loadClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping").getDeclaredField("mappingRegistry");
			mappingRegistryField.setAccessible(true);
			Object mappingRegistry = mappingRegistryField.get(mappingHandlerMapping);
			Method getRegistrations = servletContext.getClass().getClassLoader().loadClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$MappingRegistry").getDeclaredMethod("getRegistrations");
			getRegistrations.setAccessible(true);
			Map<?, ?> registry = (Map<?, ?>) getRegistrations.invoke(mappingRegistry);
			
			Method getMapping = servletContext.getClass().getClassLoader().loadClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$MappingRegistration").getDeclaredMethod("getMapping");
			getMapping.setAccessible(true);
			Method getPatternsCondition = servletContext.getClass().getClassLoader().loadClass("org.springframework.web.servlet.mvc.method.RequestMappingInfo").getDeclaredMethod("getPatternsCondition");
			getPatternsCondition.setAccessible(true);
			Method getPatterns = servletContext.getClass().getClassLoader().loadClass("org.springframework.web.servlet.mvc.condition.PatternsRequestCondition").getDeclaredMethod("getPatterns");
			getPatterns.setAccessible(true);
			Method getHandlerMethod = servletContext.getClass().getClassLoader().loadClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$MappingRegistration").getDeclaredMethod("getHandlerMethod");
			getHandlerMethod.setAccessible(true);
			Collection<?> registryValues = registry.values();
			for(Object value : registryValues) {
				Object mapping = getMapping.invoke(value);
				Object patternsCondition = getPatternsCondition.invoke(mapping);
				Set<String> patterns = (Set<String>) getPatterns.invoke(patternsCondition);
				String path = patterns.iterator().next();
				String methodName = getHandlerMethod.invoke(value).toString().split("\\(")[0];
				
				ConfigPath cp = new ConfigPath(path,methodName);
				classList.add(cp);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
