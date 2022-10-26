package com.websocket.findMemShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class InvokeClassVisitor extends ClassVisitor {
	
	private String ClassName;
	private Map<String,List<String>> discoveredCalls;

    public InvokeClassVisitor(ClassWriter writer,Map<String,List<String>> discoveredCalls) {
        super(Opcodes.ASM4, writer);
        this.discoveredCalls = discoveredCalls;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.ClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
    	return new MyMethodVisitor(methodVisitor, access, name, desc,this.ClassName,discoveredCalls);
    }
    
    class MyMethodVisitor extends MethodVisitor implements Opcodes {
    	private String MethodName;
    	private String ClassName;
    	private String desc;
    	private Map<String,List<String>> discoveredCalls;
    	
        public MyMethodVisitor(MethodVisitor mv, final int access, final String name, final String desc,String ClassName,Map<String,List<String>> discoveredCalls) {
            super(Opcodes.ASM5, mv);
            this.MethodName = name;
            this.ClassName = ClassName;
            this.discoveredCalls = discoveredCalls;
            this.desc = desc;
        }
        
        @Override
        public void visitMethodInsn(final int opcode, final String owner,
                final String name, final String desc, final boolean itf) {
            
            if(discoveredCalls.containsKey(this.ClassName+"#"+this.MethodName)) {
            	discoveredCalls.get(this.ClassName+"#"+this.MethodName).add(owner+"#"+name);
            }else {
            	List<String> list = new ArrayList<>();
            	list.add(owner+"#"+name);
            	discoveredCalls.put(this.ClassName+"#"+this.MethodName, list);
            }
            
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        
        @Override
        public void visitInsn(int opcode) {
        	if (this.MethodName.equals(App.Change_Class_Method) && this.ClassName.equals(App.Change_Class)) {
	            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
	                    || opcode == Opcodes.ATHROW) {
	                //方法在返回之前，打印"end"
	            	mv.visitVarInsn(ALOAD, 0);
	            	mv.visitFieldInsn(GETFIELD, "org/apache/catalina/core/ApplicationContext", "facade", "Ljavax/servlet/ServletContext;");
	            	mv.visitMethodInsn(INVOKESTATIC, "com/websocket/findMemShell/App", "changeServletContext", "(Ljava/lang/Object;)V", false);
	            }
        	}
            mv.visitInsn(opcode);
        }
    }
}
