package com.websocket.findMemShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassPrinter extends ClassVisitor {
	
	private String ClassName;
	private Map<String,List<String>> discoveredCalls;

    public ClassPrinter(ClassWriter writer,Map<String,List<String>> discoveredCalls) {
        super(Opcodes.ASM4, writer);
        this.discoveredCalls = discoveredCalls;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        //System.out.println(name + " extends " + superName + " {");
        this.ClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
    	return new TraceAdviceAdapter(methodVisitor, access, name, desc,this.ClassName,discoveredCalls);
    }
    
    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}