package com.websocket.findMemShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class TraceAdviceAdapter extends AdviceAdapter {
	private String MethodName;
	private String ClassName;
	private Map<String,List<String>> discoveredCalls;

    protected TraceAdviceAdapter(final MethodVisitor mv, final int access, final String name, final String desc,String ClassName,Map<String,List<String>> discoveredCalls) {
        super(ASM5, mv, access, name, desc);
        this.MethodName = name;
        this.ClassName = ClassName;
        this.discoveredCalls = discoveredCalls;
    }
    
    @Override
    public void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc, final boolean itf) {
        //System.out.println("MethodInsn:"+this.ClassName+"#"+this.MethodName+" -> "+owner+"#"+name);
        
        if(discoveredCalls.containsKey(this.ClassName+"#"+this.MethodName)) {
        	discoveredCalls.get(this.ClassName+"#"+this.MethodName).add(owner+"#"+name);
        }else {
        	List<String> list = new ArrayList<>();
        	list.add(owner+"#"+name);
        	discoveredCalls.put(this.ClassName+"#"+this.MethodName, list);
        }
        
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
	
}