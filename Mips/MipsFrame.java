package Mips;

import java.util.ArrayList;
import java.util.HashMap;
import Assem.InstrList;
import Assem.OPER;
import Temp.Label;
import Temp.Temp;
import Temp.TempList;
import Tree.CALL;
import Tree.MOVE;
import Tree.SEQ;
import Tree.Stm;
import Tree.TEMP;
import Util.BoolList;

public class MipsFrame extends Frame.Frame {
    
    static final Temp ZERO = new Temp(); 
    static final Temp AT = new Temp(); 
    static final Temp V0 = new Temp(); 
    static final Temp V1 = new Temp(); 
    static final Temp A0 = new Temp(); 
    static final Temp A1 = new Temp(); 
    static final Temp A2 = new Temp(); 
    static final Temp A3 = new Temp(); 
    static final Temp T0 = new Temp(); 
    static final Temp T1 = new Temp();
    static final Temp T2 = new Temp();
    static final Temp T3 = new Temp();
    static final Temp T4 = new Temp();
    static final Temp T5 = new Temp();
    static final Temp T6 = new Temp();
    static final Temp T7 = new Temp();
    static final Temp S0 = new Temp(); 
    static final Temp S1 = new Temp();
    static final Temp S2 = new Temp();
    static final Temp S3 = new Temp();
    static final Temp S4 = new Temp();
    static final Temp S5 = new Temp();
    static final Temp S6 = new Temp();
    static final Temp S7 = new Temp();
    static final Temp T8 = new Temp(); 
    static final Temp T9 = new Temp();
    static final Temp K0 = new Temp(); 
    static final Temp K1 = new Temp(); 
    static final Temp GP = new Temp(); 
    static final Temp SP = new Temp(); 
    static final Temp FP = new Temp(); 
    static final Temp RA = new Temp(); 
    static final int callerSavesOffset = 0;
    static final int calleeSavesOffset = 0;
    static TempList specialRegs, argRegs, tempSaves, callerSaves, calleeSaves;
    {
        
        specialRegs = L(ZERO, L(AT, L(K0, L(K1, L(GP, L(FP, L(SP, L(RA))))))));
        
        argRegs = L(A0, L(A1, L(A2, L(A3))));
        
        calleeSaves = L(S0, L(S1, L(S2,L(S3, L(S4, L(S5, L(S6, L(S7))))))));
        
        tempSaves = L(T0, L(T1, L(T2, L(T3, L(T4, L(T5, L(T6, L(T7,L(T8, L(T9))))))))));
        callerSaves =  tempSaves;
    }
    static TempList calldefs, returnSink;
    {
        
        calldefs = L(RA, L(argRegs, callerSaves));
        
        returnSink = L(V0, L(specialRegs, calleeSaves));
    }
    
    private static final int wordSize = 4;
    private static HashMap<String,Label> labels = new HashMap<String,Label>();
    private static int numOfCalleeSaves=8;
    
    public ArrayList<MOVE> saveArgs = new ArrayList<MOVE>();
    int offset = 0;

    
    
    public Frame.Frame newFrame(Label name, BoolList formals) {
    	MipsFrame ret = new MipsFrame();
    	ret.name = name; 
    	TempList argReg = argRegs; 
    	for (BoolList f = formals; f != null; f = f.tail, argReg = argReg.tail)
    	{
    		Frame.Access a = ret.allocLocal(f.head);
    	
    	ret.formals = new Frame.AccessList(a, ret.formals);
    	if (argReg != null)
    	ret.saveArgs.add(new MOVE(a.exp(new TEMP(FP)),
    	new TEMP(argReg.head)));
    	}
    	return ret;
    	}
    
    public Frame.Access allocLocal(boolean escape) {
        if (escape) {
        	
            Frame.Access ret = new InFrame(this, offset);
            offset -= wordSize;
            return ret;
        } else {
            return new InReg();
        }
    }

    public Stm procEntryExit1(Stm body) {
    	
    	for (int i = 0; i < saveArgs.size(); ++i)
    	body = new SEQ((MOVE) saveArgs.get(i), body);
    	
    	Frame.Access fpAcc = allocLocal(true);
    	Frame.Access raAcc = allocLocal(true);
    	Frame.Access[] calleeAcc = new Frame.Access[numOfCalleeSaves];
    	TempList calleeTemp = calleeSaves;
    	for (int i = 0;i < numOfCalleeSaves;++i,calleeTemp = calleeTemp.tail) {
    	calleeAcc[i] = allocLocal(true);
    	body = new SEQ(new MOVE(calleeAcc[i].exp(new TEMP(FP)), new TEMP(
    	calleeTemp.head)), body);
    	}
    	
    	body = new SEQ(new MOVE(raAcc.exp(new TEMP(FP)), new TEMP(RA)), body);
    	
    	body = new SEQ(new MOVE(new TEMP(FP), 
    							new Tree.BINOP(Tree.BINOP.PLUS,
    									  new TEMP(SP), 
    									  new Tree.CONST(-offset - wordSize)))
    				   , body);
    	
    	body = new SEQ(
    	new MOVE(fpAcc.expFromStack(new TEMP(SP)), new TEMP(FP)), body);
    	
    	calleeTemp = calleeSaves;
    	for (int i = 0; i < numOfCalleeSaves; ++i, calleeTemp = calleeTemp.tail)
    	body = new SEQ(body, new MOVE(new TEMP(calleeTemp.head),
    	calleeAcc[i].exp(new TEMP(FP))));
    	
    	body = new SEQ(body, new MOVE(new TEMP(RA), raAcc.exp(new TEMP(FP))));
    	
    	body = new SEQ(body, new MOVE(new TEMP(FP), fpAcc.expFromStack(new
    	TEMP(SP))));
    	return body;
    	}
    
    public InstrList procEntryExit2(InstrList body) {
    	return append(body, new InstrList(new OPER("", null, new TempList(ZERO,
    	new TempList(SP, new TempList(RA, calleeSaves)))), null));
    	}

    public InstrList procEntryExit3(InstrList body) {
    	
    	body = new InstrList(new OPER("subu $sp, $sp, " + (-offset),
    	new TempList(SP, null), new TempList(SP, null)), body);
    	
    	body = new InstrList(new OPER(name.toString() + ":", null, null), body);
    	
    	InstrList epilogue = new InstrList(new OPER("jr $ra", null,
    	new TempList(RA, null)), null);

    	epilogue = new InstrList(new OPER("addu $sp, $sp, " + (-offset),
    	new TempList(SP, null), new TempList(SP, null)), epilogue);
    	body = append(body, epilogue);
    	return body;
    	}
    
    public MipsFrame() {
    }

    public MipsFrame(Label n, BoolList f) {
        name = n;
        formals = allocFormals(0, f);

    }
    
    public int wordSize() {
        return wordSize;
    }

    private Frame.AccessList allocFormals(int offset, BoolList formals) {
        if (formals == null) {
            return null;
        }
        Frame.Access a;
        if (formals.head) {
            a = new InFrame(this,offset);
        } else {
            a = new InReg();
        }
        return new Frame.AccessList(a, allocFormals(offset + wordSize, formals.tail));
    }

    public Temp FP() {
        return FP;
    }

    public Temp RV() {
        return V0;
    }

    @Override
    public Temp SP() {
        return SP;
    }
    
    @Override
    public Temp RA() {
        return RA;
    }
    
    public Tree.Exp externalCall(String func, Tree.ExpList args) {
        String u = func.intern();
        Label l = (Label) labels.get(u);
        if (l == null) {
            l = new Label(u);
            labels.put(u, l);
        }
        return new CALL(new Tree.NAME(l), args);
    } 

    public String string(Label label, String value) {
    	String ret = label.toString() + ":\n";
    	ret = ret + ".word " + value.length() + "\n";
    	ret = ret + ".asciiz \"" + value + "\"";
    	return ret;
    }
    
    
    private static final HashMap<Temp, String> tempMap = new HashMap<Temp, String>();
    static {
        tempMap.put(ZERO, "$0");
        tempMap.put(AT, "$at");
        tempMap.put(V0, "$v0");
        tempMap.put(V1, "$v1");
        tempMap.put(A0, "$a0");
        tempMap.put(A1, "$a1");
        tempMap.put(A2, "$a2");
        tempMap.put(A3, "$a3");
        tempMap.put(T0, "$t0");
        tempMap.put(T1, "$t1");
        tempMap.put(T2, "$t2");
        tempMap.put(T3, "$t3");
        tempMap.put(T4, "$t4");
        tempMap.put(T5, "$t5");
        tempMap.put(T6, "$t6");
        tempMap.put(T7, "$t7");
        tempMap.put(S0, "$s0");
        tempMap.put(S1, "$s1");
        tempMap.put(S2, "$s2");
        tempMap.put(S3, "$s3");
        tempMap.put(S4, "$s4");
        tempMap.put(S5, "$s5");
        tempMap.put(S6, "$s6");
        tempMap.put(S7, "$s7");
        tempMap.put(T8, "$t8");
        tempMap.put(T9, "$t9");
        tempMap.put(K0, "$k0");
        tempMap.put(K1, "$k1");
        tempMap.put(GP, "$gp");
        tempMap.put(SP, "$sp");
        tempMap.put(FP, "$fp"); 
        tempMap.put(RA, "$ra");
    }

    public String tempMap(Temp temp) {
        if (temp == null) {
            System.out.print("haha");
        }
        if (tempMap.containsKey(temp)) {
            return (String) tempMap.get(temp);
        } else {
            return null;
        }
    }

    static TempList L(Temp h, TempList t) {
        return new TempList(h, t);
    }

    
    static TempList L(Temp h) {
        return new TempList(h, null);
    }
    

    
    static TempList L(TempList a, TempList b) {
        return new TempList(a, b);
    }
    
    

    static InstrList append(InstrList a, InstrList b) {
        return new InstrList(a, b);
    }

    static TempList append(TempList a, TempList b) {
        return new TempList(a, b);
    }

 
    
    public InstrList codegen(Stm stm) {
        return (new Codegen(this)).codegen(stm);
    }

  
    public TempList colors() {
        TempList colors = null;
       
        colors = append(colors, tempSaves);
      
        return colors;
    }

    public TempList registers() {
        TempList registers = null;
        registers = append(registers, argRegs);
        registers = append(registers, calleeSaves);
        registers = append(registers, callerSaves);
        //registers = append(registers, specialRegs);
        return registers;
    }
}
