package com.websocket.findMemShell;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class GrpcClassVisitor extends ClassVisitor {
	
	private String ClassName = null;
	private List<String> Grpc_Methods_list;

    public GrpcClassVisitor(ClassWriter writer,List<String> Grpc_Methods_list) {
        super(Opcodes.ASM4, writer);
        this.Grpc_Methods_list = Grpc_Methods_list;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if(superName.contains("ServiceGrpc")) {
        	try {
    			String cls = Thread.currentThread().getContextClassLoader().loadClass(superName.replaceAll("/", "\\.")).getInterfaces()[0].getName();
    			if(cls.equals("io.grpc.BindableService")) {
    				//System.out.println("SuperName Class:"+cls);
    				this.ClassName = name;
    			}
    			
            } catch (ClassNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
    	super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
    	if(this.ClassName == null) {
    		return methodVisitor;
    	}else {
    		return new MyMethodVisitor(methodVisitor, access, name, desc,this.ClassName,this.Grpc_Methods_list);
    	}
    	
    }
    
    class MyMethodVisitor extends MethodVisitor implements Opcodes {
    	private String MethodName;
    	private String ClassName;
    	private List<String> Grpc_Methods_list;
        public MyMethodVisitor(MethodVisitor mv, final int access, final String name, final String desc,String ClassName,List<String> Grpc_Methods_list) {
            super(Opcodes.ASM5, mv);
            this.MethodName = name;
            this.ClassName = ClassName;
            this.Grpc_Methods_list = Grpc_Methods_list;
        }
        
        @Override
        public void visitMethodInsn(final int opcode, final String owner,
                final String name, final String desc, final boolean itf) {
        	
        	if(!this.Grpc_Methods_list.contains(this.ClassName+"#"+this.MethodName)) {
        		this.Grpc_Methods_list.add(this.ClassName+"#"+this.MethodName);
        		//System.out.println(this.ClassName+"#"+this.MethodName);
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
