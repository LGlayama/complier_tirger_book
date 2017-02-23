package Mips;

import Assem.Instr;
import Assem.InstrList;
import Assem.OPER;
import Temp.LabelList;
import Temp.Temp;
import Temp.TempList;
import Temp.TempMap;
import Tree.BINOP;
import Tree.CALL;
import Tree.CJUMP;
import Tree.CONST;
import Tree.EXPSTM;
import Tree.Exp;
import Tree.ExpList;
import Tree.JUMP;
import Tree.LABEL;
import Tree.MEM;
import Tree.MOVE;
import Tree.NAME;
import Tree.Stm;
import Tree.StmList;
import Tree.TEMP;

public class Codegen {

    MipsFrame frame;
    

    public Codegen(MipsFrame f) {
        frame = f;
    }

    static Instr OPER(String a, TempList d, TempList s) {
        return new OPER(a, d, s);
    }

   
    private InstrList ilist = null, last = null;

    private void emit(Instr inst) {
        if (last != null) {
            last = last.tail = new InstrList(inst, null);
        } else {
            if (ilist != null) {
                throw new Error("Codegen.emit");
            }
            last = ilist = new InstrList(inst, null);
        }
    }

    InstrList codegen(Stm s) {
        munchStm(s);
        InstrList l = ilist;
        ilist = last = null;
        return l;
    }

    static Instr MOVE(String a, Temp d, Temp s) {
        return new Assem.MOVE(a, d, s);
    }

    static TempList L(Temp h) {
        return new TempList(h, null);
    }

    static TempList L(Temp h, TempList t) {
        return new TempList(h, t);
    }

    public void munchStms(StmList slist) {
        StmList list = slist;
        for (; list != null; list = list.tail) {
            munchStm(list.head);
        }
    }

    void munchStm(Stm s) {
        if (s instanceof MOVE) {
            munchStm((MOVE) s);
        } else if (s instanceof EXPSTM) {
            munchStm((EXPSTM) s);
        } else if (s instanceof JUMP) {
            munchStm((JUMP) s);
        } else if (s instanceof CJUMP) {
            munchStm((CJUMP) s);
        } else if (s instanceof LABEL) {
            munchStm((LABEL) s);
        } else {
            throw new Error("Codegen.munchStm");
        }
    }

    public void munchStm(CJUMP j) {
    	String oper = null;
    	switch (j.relop) {
    		case CJUMP.EQ:
    			oper = "beq";
    			break;
    		case CJUMP.NE:
    			oper = "bne";
    			break;
    		case CJUMP.GT:
    			oper = "bgt";
    			break;
    		case CJUMP.GE:
    			oper = "bge";
    			break;
    		case CJUMP.LT:
    			oper = "blt";
    			break;
    		case CJUMP.LE:
    			oper = "ble";
    			break;
    	}
    	Temp t1 = munchExp(j.left);
    	Temp t2 = munchExp(j.right);
    	emit(new OPER(oper + " `s0, `s1, `j0", null, new TempList(t1,
    	new TempList(t2, null)), new LabelList(j.iftrue, new LabelList(j.iffalse, null))));
    }
    
    public void munchStm(MOVE s) {
    	Tree.Exp dst = s.dst;
    	Tree.Exp src = s.src;
    	if (dst instanceof MEM) {
    	MEM dst1 = (MEM) dst;
    	if (dst1.exp instanceof BINOP //锟斤拷锟�1
    	&& ((BINOP) dst1.exp).binop == Tree.BINOP.PLUS
    	&& ((BINOP) dst1.exp).right instanceof CONST) {
    	Temp t1 = munchExp(src);
    	Temp t2 = munchExp(((BINOP) dst1.exp).left);
    	emit(new OPER("sw `s0, "
    			+ ((CONST) ((BINOP) dst1.exp).right).value + "(`s1)",
    			null, new TempList(t1, new TempList(t2, null))));
    	} else if (dst1.exp instanceof BINOP //锟斤拷锟�2
    			&& ((BINOP) dst1.exp).binop == Tree.BINOP.PLUS
    			&& ((BINOP) dst1.exp).left instanceof CONST) {
    			Temp t1 = munchExp(src);
    			Temp t2 = munchExp(((BINOP) dst1.exp).right);
    			emit(new OPER("sw `s0, "
    			+ ((CONST) ((BINOP) dst1.exp).left).value + "(`s1)",
    			null, new TempList(t1, new TempList(t2, null))));
    			} else if (dst1.exp instanceof CONST) { //锟斤拷锟�3
    			Temp t1 = munchExp(src);
    			emit(new OPER("sw `s0, " + ((CONST) dst1.exp).value, null,
    			new TempList(t1, null)));
    			} else { //锟斤拷锟�4
    			Temp t1 = munchExp(src);
    			Temp t2 = munchExp(dst1.exp);
    			emit(new OPER("sw `s0, (`s1)", null, new TempList(t1,
    			new TempList(t2, null))));
    			}
    			} else if (dst instanceof TEMP)
    			if (src instanceof CONST) { //锟斤拷锟�5
    			emit(new OPER("li `d0, " + ((CONST) src).value, new TempList(
    			((TEMP) dst).temp, null), null));
    			} else { //锟斤拷锟�6
    			Temp t1 = munchExp(src);
    			emit(new OPER("move `d0, `s0", new TempList(((TEMP) dst).temp,
    			null), new TempList(t1, null)));
    			}
    			}

    void munchStm(EXPSTM s) {
        munchExp(s.exp);
    }

    public void munchStm(JUMP j) {
    	emit(new OPER("j " + j.targets.head, null, null, j.targets));
    	}
 
    public void munchStm(LABEL l) {
    	emit(new Assem.LABEL(l.label.toString() + ":", l.label));
    	}

    Temp munchExp(Exp s) {
        if (s instanceof CONST) {
            return munchExp((CONST) s);
        } else if (s instanceof NAME) {
            return munchExp((NAME) s);
        } else if (s instanceof TEMP) {
            return munchExp((TEMP) s);
        } else if (s instanceof BINOP) {
            return munchExp((BINOP) s);
        } else if (s instanceof MEM) {
            return munchExp((MEM) s);
        } else if (s instanceof CALL) {
            return munchExp((CALL) s);
        } else {
            throw new Error("Codegen.munchExp");
        }
    }

    public Temp munchExp(CONST e) {
    	Temp ret = new Temp();
    	emit(new OPER("li `d0, " + e.value, new TempList(ret, null), null));
    	return ret;
    }

    public Temp munchExp(NAME t) {
    	Temp ret = new Temp();
    	emit(new OPER("la `d0, " + t.label, new TempList(ret, null), null));
    	return ret;
    }

    public Temp munchExp(TEMP t) {
    	return t.temp;
    }
    
    private static String[] BINOP = new String[10];

    static {
        BINOP[Tree.BINOP.PLUS] = "add";
        BINOP[Tree.BINOP.MINUS] = "sub";
        BINOP[Tree.BINOP.MUL] = "mulo";
        BINOP[Tree.BINOP.DIV] = "div";
        BINOP[Tree.BINOP.AND] = "and";
        BINOP[Tree.BINOP.OR] = "or";
        BINOP[Tree.BINOP.LSHIFT] = "sll";
        BINOP[Tree.BINOP.RSHIFT] = "srl";
        BINOP[Tree.BINOP.ARSHIFT] = "sra";
        BINOP[Tree.BINOP.XOR] = "xor";
    }

   /* private static int shift(int i) {
        int shift = 0;
        if ((i >= 2) && ((i & (i - 1)) == 0)) {
            while (i > 1) {
                shift += 1;
                i >>= 1;
            }
        }
        return shift;
    }*/

    public Temp munchExp(BINOP e) {
    	Temp ret = new Temp();
    	String oper = null;
    	switch (e.binop) {
    		case Tree.BINOP.PLUS:
    			oper = "add";
    			break;
    		case Tree.BINOP.MINUS:
    			oper = "sub";
    			break;
    		case Tree.BINOP.MUL:
    			oper = "mul";
    			break;
    		case Tree.BINOP.DIV:
    			oper = "div";
    			break;
    	}
    	if (e.right instanceof CONST) { //锟斤拷锟�1
    	Temp t1 = munchExp(e.left);
    	emit(new OPER(oper + " `d0, `s0, " + ((CONST) e.right).value,
    	new TempList(ret, null), new TempList(t1, null)));
    	} else if (e.left instanceof CONST) { //锟斤拷锟�2
    		Temp t1 = munchExp(e.right);
    		emit(new OPER(oper + " `d0, `s0, " + ((CONST) e.left).value,
    		new TempList(ret, null), new TempList(t1, null)));
    		} else { //锟斤拷锟�3
    			Temp t1 = munchExp(e.left);
    			Temp t2 = munchExp(e.right);
    			emit(new OPER(oper + " `d0, `s0, `s1", new TempList(ret, null),new TempList(t1, new TempList(t2, null))));
    		}
    		return ret;
    }

    public Temp munchExp(MEM e) {
    	Temp ret = new Temp();
    	if (e.exp instanceof BINOP && ((BINOP) e.exp).binop == Tree.BINOP.PLUS
    	&& ((BINOP) e.exp).right instanceof CONST) {
    	Temp t1 = munchExp(((BINOP) e.exp).left);
    	emit(new OPER("lw `d0, " + ((CONST) ((BINOP) e.exp).right).value
    	+ "(`s0)", new TempList(ret, null), new TempList(t1, null)));
    	//锟斤拷锟�1
    	} else if (e.exp instanceof BINOP && ((BINOP) e.exp).binop == Tree.BINOP.PLUS
    	&& ((BINOP) e.exp).left instanceof CONST) {
    		Temp t1 = munchExp(((BINOP) e.exp).right);
    		emit(new OPER("lw `d0, " + ((CONST) ((BINOP) e.exp).left).value
    		+ "(`s0)", new TempList(ret, null), new TempList(t1, null)));
    		//锟斤拷锟�2
    		} else if (e.exp instanceof CONST) {
    		emit(new OPER("lw `d0, " + ((CONST) e.exp).value, new TempList(ret,
    		null), null)); //锟斤拷锟�3
    		} else {
    		Temp t1 = munchExp(e.exp);
    		emit(new OPER("lw `d0, (`s0)", new TempList(ret, null),
    		new TempList(t1, null))); //锟斤拷锟�4
    		}
    		return ret;
    }
    	

    public Temp munchExp(CALL c) {
    	TempList list = null;
    	int i = 0;
    	for (ExpList a = c.args; a != null; a = a.tail, ++i) {
    	Temp t = null;
    	if (a.head instanceof CONST)
    	emit(new OPER("li $a" + i + ", " + ((CONST) a.head).value,
    	null, null));
    	else {
    	t = munchExp(a.head);
    	emit(new OPER("move $a" + i + ", `s0", null, new TempList(t,
    	null)));
    	}
    	if (t != null)
    	list = new TempList(t, list);
    	}
    	emit(new OPER("jal " + ((NAME) c.func).label, MipsFrame.calldefs, list));
    	return MipsFrame.V0;
    }

    /*private TempList munchArgs(int i, ExpList args) {
        if (args == null) {
            return null;
        }
        Temp src = munchExp(args.head);
        if (i > frame.maxArgs) {
            frame.maxArgs = i;
        }
        switch (i) {
            case 0:
                emit(MOVE("move `d0,`s0", frame.A0, src));
                break;
            case 1:
                emit(MOVE("move `d0,`s0", frame.A1, src));
                break;
            case 2:
                emit(MOVE("move `d0,`s0", frame.A2, src));
                break;
            case 3:
                emit(MOVE("move `d0,`s0", frame.A3, src));
                break;
            default:
                emit(OPER("sw `s0" + (i - 1) * frame.wordSize() + "(`s1)", null,
                        L(src, L(frame.SP))));
                break;
        }
        return L(src, munchArgs(i + 1, args.tail));
    }*/

    /*public InstrList getBareResult() {
        return ilist.reverse();
    }*/

    public String format(InstrList is, TempMap f) {
        String s = "";
        s = is.head.toString() + "";
        System.out.println(is.head);
        System.out.println(is.head.format(f));
        if (is.tail == null) {
            return s + is.head.format(f);
        } else {
            return s + is.head.format(f) + "" + format(is.tail, f);
        }
    }
}

