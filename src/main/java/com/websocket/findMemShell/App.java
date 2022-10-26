package com.websocket.findMemShell;

/**
 * Hello world!
 *
 */
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;


import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
	static Map<String,List<String>> discoveredCalls = new HashMap<>();
	public static final String Change_Class = "org/apache/catalina/core/ApplicationContext";
	public static final String Change_Class_Method = "<init>";
	public static final String Change_Class_Method_Desc = "(Lorg/apache/catalina/core/StandardContext;)V";
	public static int count = 0;
	public static Object servletContext = null;
	
	public static void agentmain(String args, Instrumentation instrumentation) throws Exception {
		//instrumentation.addTransformer(new DefineTransformer(), true);
		loadAgent(args,instrumentation);
    }
	
	private static void loadAgent(String arg, final Instrumentation inst) {
		Class<?>[] classes = Arrays.stream(inst.getAllLoadedClasses())
                //.filter(c -> !c.isArray() && inst.isModifiableClass(c))
                .toArray(Class[]::new);
		
        // 创建DefineTransformer对象
        ClassFileTransformer classFileTransformer = new DefineTransformer();

        // 添加自定义的Transformer，第二个参数true表示是否允许Agent Retransform，
        // 需配合MANIFEST.MF中的Can-Retransform-Classes: true配置
        inst.addTransformer(classFileTransformer, true);

        // 获取所有已经被JVM加载的类对象
        //Class[] loadedClass = inst.getAllLoadedClasses();

        for (Class clazz : classes) {

            if (inst.isModifiableClass(clazz)) {
                // 使用Agent重新加载字节码
            	try {
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    e.printStackTrace();
                }
            }
        }
        
        SearchCallsThread thread = new SearchCallsThread(discoveredCalls);
        thread.start();
        System.out.println("Done!");
    }
	
	
	
	public static void premain(String args, Instrumentation instrumentation) throws Exception {
        agentmain(args,instrumentation);
    }
    
    static class DefineTransformer implements ClassFileTransformer {
    	public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
    		if(Change_Class.equals(s)) {
	            //System.out.println("Dumping File ...");
	    		ClassReader reader = new ClassReader(bytes);
	            ClassWriter writer = new ClassWriter(reader, 0);
	            InvokeClassVisitor visitor = new InvokeClassVisitor(writer,discoveredCalls);
	            reader.accept(visitor, 0);
	            //dumpClass(s,writer.toByteArray());
	            //System.out.println("Dumping end ...");
	            
	            return writer.toByteArray();
    		}
    		
    		ClassReader reader = new ClassReader(bytes);
	        ClassWriter writer = new ClassWriter(reader, 0);
	        ClassPrinter visitor = new ClassPrinter(writer,discoveredCalls);
	        reader.accept(visitor, 0);
	        //dumpClass(s,writer.toByteArray());
	        return writer.toByteArray();
            //return null;
        }
    }
    
    public static void dumpClass(String s,byte[] content){    
        FileOutputStream fileOutputStream = null;
        File file = new File("C:\\Users\\DELL\\Desktop\\fsdownload\\rasp-class-dump\\dumpClass\\"+s.replaceAll("/", "_")+count+".class");
        try {
        	
            if(file.exists()){
                //判断文件是否存在，如果不存在就新建一个txt
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        count++;
     }

    public static void changeServletContext(Object servletContext) {
    	if(App.servletContext == null) {
    		App.servletContext = servletContext;
    		System.out.println("Change servletContext: "+servletContext.getClass());
    	}
    }

}