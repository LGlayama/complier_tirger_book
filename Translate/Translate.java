package Translate;
import Temp.Temp;
import Temp.Label;
import Tree.*;

import java.util.ArrayList;

public class Translate {
  public Frame.Frame frame;
  private int wordSize = 4;
  private Frag frags = null;

  public Translate(Frame.Frame f) {
    frame = f;
  }
  
  public Frag getResult() {
    return frags;
  }
  
  public void addFrag(Frag frag) {
    frag.next = frags;
    frags = frag;
  }
  
  public void procEntryExit(Level level, Exp body, boolean returnValue) {
	  Stm b = null;
	  if (returnValue)

	  b = new MOVE(new TEMP(level.frame.RV()), body.unEx());
	  else
	
	  b = body.unNx();
	  b = level.frame.procEntryExit1(b);
	 
	  addFrag(new ProcFrag(b, level.frame)); 
	  }
  
  public Exp transNilExp() {
	
    return new Ex(new CONST(0));
  }
  
  public Exp transIntExp(int value) {
	
    return new Ex(new CONST(value));
  }
  
  public Exp transStringExp(String value) {
	  
    Label l = new Label(); 
    addFrag(new DataFrag(l, frame.string(l, value)));
    return new Ex(new NAME(l));
  }

  private static Tree.Exp CONST(int value) {
	
	    return new Tree.CONST(value);
	  }
	  
  public Exp Error() {
	    return new Ex(CONST(0));
	  }
	
  public Exp transArrayExp(Level home, Exp init, Exp size) {
	
	Tree.Exp alloc = home.frame.externalCall("initArray", new Tree.ExpList(size.unEx(), new Tree.ExpList(init.unEx(), null)));
    return new Ex(alloc);
  }
  
  public Exp transNoOp() {
	  
    return new Ex(new CONST(0));
  }
  
  public Exp transStringRelExp(int oper, Exp left, Exp right) {
	  
	  Tree.Exp comp = frame.externalCall("_stringCompare", new Tree.ExpList(left.unEx(),
            new Tree.ExpList(right.unEx(), null)));
    return new RelCx(oper, new Ex(comp), new Ex(new CONST(0)));
  }
  
  public Exp transStringRelExp(Level currentL, int oper, Exp left, Exp right)
	{
	
		Tree.Exp comp = currentL.frame.externalCall("stringEqual", new Tree.ExpList(left.unEx(), new Tree.ExpList(right.unEx(), null)));
		return new RelCx(oper, new Ex(comp), new Ex( new CONST(1)));
	}
  
  public Exp transCalcExp(int binOp, Exp left, Exp right) {
	  
    return new Ex(new BINOP(binOp, left.unEx(), right.unEx()));
  }
  
  public Exp transOtherRelExp(int oper, Exp left, Exp right) {
    return new RelCx(oper, left, right);
  }
  
  public Exp transAssignExp(Exp lvalue, Exp ex) {
	 
    return new Nx(new MOVE(lvalue.unEx(), ex.unEx()));
  }

  public Exp transCallExp(Level home, Level dest, Label name, ArrayList<Exp> argValue) {
	
	  Tree.ExpList args = null;
    for (int i = argValue.size() - 1; i >= 0; --i)
      args = new Tree.ExpList(((Exp) argValue.get(i)).unEx(), args);
    Level l = home;
    Tree.Exp slnk = new TEMP(l.frame.FP()); 
  
    while (dest.parent != l) {
      slnk = l.staticLink().acc.exp(slnk);
      l = l.parent;
    }
    args = new Tree.ExpList(slnk, args);
    return new Ex(new CALL(new NAME(name), args));
  }

  public Exp transVarExp(Exp ex) {
    return ex;
  }
  
  public Exp transOpExp(int oper, Exp left, Exp right)
	{	
		if (oper >= BINOP.PLUS && oper <= BINOP.DIV)
			return new Ex(new BINOP(oper, left.unEx(), right.unEx()));
		return new RelCx(oper, left, right);
	}

  public Exp transIfThenElseExp(Exp test, Exp e_then, Exp e_else) {
    return new IfExp(test, e_then, e_else);
  }

  public Exp combine2Stm(Exp e1, Exp e2) {
    if (e1 == null)
      return new Nx(e2.unNx());
    else if (e2 == null)
      return new Nx(e1.unNx());
    else
      return new Nx(new SEQ(e1.unNx(), e2.unNx()));
  }

  public Exp combine2Exp(Exp e1, Exp e2) {
    if (e1 == null)
      return new Ex(e2.unEx());
    else
      return new Ex(new ESEQ(e1.unNx(), e2.unEx()));
  }

  public Exp transRecordExp(Level home, ArrayList<Exp> field) {
    Temp addr = new Temp();
 
    Tree.Exp alloc = home.frame.externalCall("_allocRecord",
            new Tree.ExpList(new CONST((
                    field.size() == 0 ? 1 : field.size())
                    * home.frame.wordSize()), null));
    Stm init = new EXPSTM(new CONST(0));
    for (int i = field.size() - 1; i >= 0; i--) {
    	
    	Tree.Exp offset = new BINOP(BINOP.PLUS,
              new TEMP(addr), new CONST(i * home.frame.wordSize()));
    	Tree.Exp v = field.get(i).unEx();
    	init = new SEQ(new MOVE(new MEM(offset), v), init);
    }
  
    return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(addr), alloc), init), new TEMP(addr)));
  }

  public Exp transWhileExp(Exp test, Exp body, Label done) {
    return new WhileExp(test, body, done);
  }

  public Exp transForExp(Level home, Access var, Exp low, Exp high, Exp body, Label done) {
    return new ForExp(home, var, low, high, body, done);
  }

  public Exp transSimpleVar(Access access, Level home) {
	Tree.Exp res = new TEMP(home.frame.FP());
    Level l = home;
    while (l != access.home) {
      res = l.staticLink().acc.exp(res);
      l = l.parent;
    }
    return new Ex(access.acc.exp(res));
   
 }

  public Exp transSubscriptVar(Exp var, Exp idx) {
	Tree.Exp arr_addr = var.unEx(); 
	
	Tree.Exp arr_off = new BINOP(BINOP.MUL, idx.unEx(), new CONST(wordSize));
    return new Ex(new MEM(new BINOP(BINOP.PLUS, arr_addr, arr_off)));
  }

  public Exp transFieldVar(Exp var, int num) {
	Tree.Exp rec_addr = var.unEx(); 
	
	Tree.Exp rec_off = new CONST(num * wordSize);
    return new Ex(new MEM(new BINOP(BINOP.PLUS, rec_addr, rec_off)));
  }


  
  public Exp transStdCallExp(Level currentL, Label name, java.util.ArrayList<Exp> args_value)
  {
      Tree.ExpList args = null;
      for (int i = args_value.size() - 1; i >= 0; --i)
          args = new Tree.ExpList(((Exp) args_value.get(i)).unEx(), args);
      return new Ex(currentL.frame.externalCall(name.toString(), args));
  }

  public Exp transExtCallExp(Level home, Label name, ArrayList argValue) {
	  Tree.ExpList args = null;
	  for (int i = argValue.size() - 1; i >= 0; --i)
		  args = new Tree.ExpList(((Exp) argValue.get(i)).unEx(), args);
	  Level l = home;
	  Tree.Exp slnk = new TEMP(l.frame.FP());
	  args = new Tree.ExpList(slnk, args);
	  return new Ex(home.frame.externalCall("_" + name, args));
  }
		  
  public Exp transBreakExp(Label done) {
    return new Nx(new JUMP(done));
  }

  public Exp transMultiArrayExp(Level home, Exp init, Exp size) {
	Tree.Exp alloc = home.frame.externalCall("_malloc",
            new Tree.ExpList(
                    new BINOP(BINOP.MUL, size.unEx(), new CONST(frame.wordSize())),
                    null));
    Temp addr = new Temp();
    Access var = home.allocLocal(false);
    Stm initialization = (new ForExp(home, var, new Ex(new CONST(0)),
            new Ex(new BINOP(BINOP.MINUS, size.unEx(), new CONST(1))),
            new Nx(new MOVE(
                    new MEM(new BINOP(BINOP.PLUS, new TEMP(addr), new BINOP(BINOP.MUL, var.acc.exp(null), new CONST(frame.wordSize())))),
                    init.unEx())),
            new Label())).unNx();
    return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(addr), alloc), initialization), new TEMP(addr)));
  }

  public Exp transSeqExp(ExpList el, boolean isVOID) {
    if (el == null) return new Ex(new CONST(0));
    if (el.tail == null) return el.head;
    if (el.tail.tail == null)
      if (isVOID)
        return new Nx(new SEQ(el.head.unNx(), el.tail.head.unNx()));
      else
        return new Ex(new ESEQ(el.head.unNx(), el.tail.head.unEx()));
    ExpList ptr = el.tail, prev = el;
    SEQ res = null;
    for (; ptr.tail != null; ptr = ptr.tail) {
      if (res == null)
        res = new SEQ(prev.head.unNx(), ptr.head.unNx());
      else
        res = new SEQ(res, ptr.head.unNx());
    }
    if (isVOID)
      return new Nx(new SEQ(res, ptr.head.unNx()));
    else
      return new Ex(new ESEQ(res, ptr.head.unEx()));
  }

}


