package Semant;

import Absyn.FieldList;
import ErrorMsg.*; 
import Translate.Level;
import Types.*;
import Util.BoolList;
import Symbol.Symbol;

public class Semant {
	private Env env;
	private Translate.Translate trans;
	private Translate.Level level = null;
	private java.util.Stack<Temp.Label> loopStack = new java.util.Stack<Temp.Label>();
	
	public Semant(Translate.Translate t, ErrorMsg err)
	{
		trans = t;
		level = new Level(t.frame);
		level = new Level(level, Symbol.symbol("main"), null);
		env = new Env(err, level);
	}
	public Translate.Frag transProg(Absyn.Exp e)
	{
		ExpTy et = transExp(e);
		if(ErrorMsg.anyErrors)
		{
			System.out.println("Find semant error, end compiling!");
			return null;
		}
		trans.procEntryExit (level, et.exp, false); 
		level = level.parent;
		return trans.getResult();
	}
	public ExpTy transVar(Absyn.Var e)
	{
		if (e instanceof Absyn.SimpleVar) return transVar((Absyn.SimpleVar)e);
		if (e instanceof Absyn.SubscriptVar) return transVar((Absyn.SubscriptVar)e);
		if (e instanceof Absyn.FieldVar) return transVar((Absyn.FieldVar)e);
		env.errorMsg.error(e.pos, "Unknow Var!");
		return null;
	}
	public ExpTy transExp(Absyn.Exp e)
	{
		if (e instanceof Absyn.IntExp) return transExp((Absyn.IntExp)e);
		if (e instanceof Absyn.StringExp) return transExp((Absyn.StringExp)e);
		if (e instanceof Absyn.NilExp) return transExp((Absyn.NilExp)e);
		if (e instanceof Absyn.VarExp) return transExp((Absyn.VarExp)e);
		if (e instanceof Absyn.OpExp) return transExp((Absyn.OpExp)e);
		if (e instanceof Absyn.AssignExp) return transExp((Absyn.AssignExp)e);
		if (e instanceof Absyn.CallExp) return transExp((Absyn.CallExp)e);
		if (e instanceof Absyn.RecordExp) return transExp((Absyn.RecordExp)e);
		if (e instanceof Absyn.ArrayExp) return transExp((Absyn.ArrayExp)e);
		if (e instanceof Absyn.IfExp) return transExp((Absyn.IfExp)e);
		if (e instanceof Absyn.WhileExp) return transExp((Absyn.WhileExp)e);
		if (e instanceof Absyn.ForExp) return transExp((Absyn.ForExp)e);
		if (e instanceof Absyn.BreakExp) return transExp((Absyn.BreakExp)e);
		if (e instanceof Absyn.LetExp) return transExp((Absyn.LetExp)e);
		if (e instanceof Absyn.SeqExp) return transExp((Absyn.SeqExp)e);
		
		return null;
	}
	public Translate.Exp transDec(Absyn.Dec e)
	{
		if (e instanceof Absyn.VarDec) return transDec((Absyn.VarDec)e);
		if (e instanceof Absyn.TypeDec) return transDec((Absyn.TypeDec)e);
		if (e instanceof Absyn.FunctionDec) return transDec((Absyn.FunctionDec)e);
		
		return null;
	}
	public Type transTy(Absyn.Ty e)
	{
		if (e instanceof Absyn.ArrayTy) return transTy((Absyn.ArrayTy)e);
		if (e instanceof Absyn.RecordTy) return transTy((Absyn.RecordTy)e);
		if (e instanceof Absyn.NameTy) return transTy((Absyn.NameTy)e);
	
		return null;
	}	
	private ExpTy transExp(Absyn.IntExp e)
	{
		return new ExpTy(trans.transIntExp(e.value), new INT());
	}
	private ExpTy transExp(Absyn.StringExp e)
	{
		return new ExpTy(trans.transStringExp(e.value), new STRING());
	}
	private ExpTy transExp(Absyn.NilExp e)
	{
		return new ExpTy(trans.transNilExp(), new NIL());
	}
	private ExpTy transExp(Absyn.VarExp e)
	{
		return transVar(e.var);
	}
	private ExpTy transExp(Absyn.OpExp e)
	{
		ExpTy el = transExp(e.left);
		ExpTy er = transExp(e.right);
		if (el == null || er == null)
		{
			env.errorMsg.error(e.pos, "Lack of operator!");
			return null;
		}
		if (e.oper == Absyn.OpExp.EQ || e.oper == Absyn.OpExp.NE) 
		{
			if (el.ty.actual() instanceof NIL && er.ty.actual() instanceof NIL)
			{
				env.errorMsg.error(e.pos, " double nil error");
				return null;
			}
			if (el.ty.actual() instanceof VOID || er.ty.actual() instanceof VOID)
			{
				env.errorMsg.error(e.pos, "void error");
				return null;
			}
			if (el.ty.actual() instanceof NIL && er.ty.actual() instanceof RECORD)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.actual() instanceof RECORD && er.ty.actual() instanceof NIL)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.coerceTo(er.ty))
			{
				if (el.ty.actual() instanceof STRING && e.oper == Absyn.OpExp.EQ)
				{
					return new ExpTy(trans.transStringRelExp(level, e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
				}
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			}

			env.errorMsg.error(e.pos, "different type error");
			return null;
		}
		if (e.oper > Absyn.OpExp.NE)
		{
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.actual() instanceof STRING && er.ty.actual() instanceof STRING)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new STRING());
			env.errorMsg.error(e.pos, "type don't support campare");
			return null;
		}
		if (e.oper < Absyn.OpExp.EQ)
		{	
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			env.errorMsg.error(e.pos, "different type");
			return null;
		}

		return new ExpTy(trans.transOpExp(e.oper, el.exp, er.exp), new INT());
	}
	private ExpTy transExp(Absyn.AssignExp e)
	{
		int pos=e.pos;
		Absyn.Var var=e.var;
		Absyn.Exp exp=e.exp;
		ExpTy er = transExp(exp);
		if (er.ty.actual() instanceof VOID)
		{
			env.errorMsg.error(pos, "error");
			return null;
		}
		if (var instanceof Absyn.SimpleVar)
		{
			Absyn.SimpleVar ev = (Absyn.SimpleVar)var;
			Entry x= (Entry)(env.vEnv.get(ev.name));
			if (x instanceof VarEntry && ((VarEntry)x).isFor)
			{
				env.errorMsg.error(pos, "error");
				return null;
			}
		}
		ExpTy vr = transVar(var);
		if (!er.ty.coerceTo(vr.ty))
		{
				env.errorMsg.error(pos, er.ty.actual().toString()+"error"+vr.ty.actual().toString()+"error");
				return null;	
		}
		return new ExpTy(trans.transAssignExp(vr.exp, er.exp), new VOID());
		
	}
	private ExpTy transExp(Absyn.CallExp e)
	{
		Object x = env.vEnv.get(e.func);
	

		Absyn.ExpList ex =e.args;
		FunEntry fe = (FunEntry)x;
		RECORD rc = fe.formals;
		while (ex != null)
		{
			if (rc == null)
			{
				env.errorMsg.error(e.pos, "error");
				return null;
			}

		
			ex = ex.tail;
			rc = rc.tail;
		}
	

		java.util.ArrayList<Translate.Exp> arrl = new java.util.ArrayList<Translate.Exp>();
		for (Absyn.ExpList i = e.args; i != null; i = i.tail)
			arrl.add(transExp(i.head).exp);
		if (x instanceof StdFuncEntry)
		{
			StdFuncEntry sf = (StdFuncEntry)x;
			return new ExpTy(trans.transStdCallExp(level, sf.label, arrl), sf.result);
		}
		return new ExpTy(trans.transCallExp(level, fe.level, fe.label, arrl), fe.result);
	}
	private ExpTy transExp(Absyn.RecordExp e)
	{
		Type t =(Type)env.tEnv.get(e.typ);
		if (t == null || !(t.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		Absyn.FieldExpList fe = e.fields;
		RECORD rc = (RECORD)(t.actual());
		if (fe == null && rc != null)
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		
		while (fe != null)
		{	
			ExpTy ie = transExp(fe.init);
		
			fe = fe.tail;
			rc = rc.tail;
		}	
		java.util.ArrayList<Translate.Exp> arrl = new java.util.ArrayList<Translate.Exp>();
		for (Absyn.FieldExpList i = e.fields; i != null; i = i.tail)
			arrl.add(transExp(i.init).exp);
		return new ExpTy(trans.transRecordExp(level, arrl), t.actual()); 
	}
	private ExpTy transExp(Absyn.ArrayExp e)
	{
		Type ty = (Type)env.tEnv.get(e.typ);
		if (ty == null || !(ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		ExpTy size = transExp(e.size);
		if (!(size.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}	

		ARRAY ar = (ARRAY)ty.actual();
		ExpTy ini = transExp(e.init);
		if (!ini.ty.coerceTo(ar.element))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		return new ExpTy(trans.transArrayExp(level, ini.exp, size.exp), new ARRAY(ar.element));			
	}
	private ExpTy transExp(Absyn.IfExp e)
	{
		
		ExpTy testET = transExp(e.test);
		ExpTy thenET = transExp(e.thenclause);
		ExpTy elseET = transExp(e.elseclause);
		
		if (e.test == null || testET == null || !(testET.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		
		if (e.elseclause == null && (!(thenET.ty.actual() instanceof VOID)))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}		
	
		if (elseET == null)
			return new ExpTy(trans.transIfThenElseExp(testET.exp, thenET.exp, trans.transNoOp()), thenET.ty);
		return new ExpTy(trans.transIfThenElseExp(testET.exp, thenET.exp, elseET.exp), thenET.ty);
	}
	private ExpTy transExp(Absyn.WhileExp e)
	{
		
		ExpTy transt = transExp(e.test);
		if (transt == null)	{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		
		if (!(transt.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error");		
			return null;
		}
		
		Temp.Label out = new Temp.Label();
		
		loopStack.push(out);
		ExpTy bdy = transExp(e.body);
		loopStack.pop();
		
		if (bdy == null){env.errorMsg.error(e.pos, "error");	return null;}
		
		if (!(bdy.ty.actual() instanceof VOID))
		{
			env.errorMsg.error(e.pos, "while error");
			return null;
		}
		
		return new ExpTy(trans.transWhileExp(transt.exp, bdy.exp, out), new VOID());
	}
	private ExpTy transExp(Absyn.ForExp e)
	{
		
		boolean flag = false;
		
		if (!(transExp(e.hi).ty.actual() instanceof INT) || !(transExp(e.var.init).ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error");
		}
		
		env.vEnv.beginScope();
		Temp.Label label = new Temp.Label();
		loopStack.push(label);
		
		Translate.Access acc = level.allocLocal(true);
		
		env.vEnv.put(e.var.name, new VarEntry(new INT(), acc, true));
		
		ExpTy body = transExp(e.body);
		
		ExpTy high = transExp(e.hi);
		
		ExpTy low = transExp(e.var.init);
		
		if (body == null)	flag = true;
		loopStack.pop();
		
		env.vEnv.endScope();
		
		
		if (flag){
			env.errorMsg.error(e.pos, "error");
			return null;
		}
	
		return new ExpTy(trans.transForExp(level, acc, low.exp, high.exp, body.exp, label), new VOID());
	}
	private ExpTy transExp(Absyn.BreakExp e)
	{
		
		if (loopStack.isEmpty())
		{
			env.errorMsg.error(e.pos, "break");
			return null;
		}
		return new ExpTy(trans.transBreakExp(loopStack.peek()), new VOID());
	}
	private ExpTy transExp(Absyn.LetExp e)
	{
		
		Translate.Exp ex = null;
		
		env.vEnv.beginScope();
		env.tEnv.beginScope();	
		ExpTy td = transDecList(e.decs);
		
		if (td != null)
			ex = td.exp;
		ExpTy tb = transExp(e.body);
		
		if (tb == null)
			ex = trans.combine2Stm(ex, null);
		else if (tb.ty.actual() instanceof VOID)
			ex = trans.combine2Stm(ex, tb.exp);
		else 
			ex = trans.combine2Exp(ex, tb.exp);
		
				
		env.tEnv.endScope();
		env.vEnv.endScope();
	
		return new ExpTy(ex, tb.ty);
	}
	private ExpTy transDecList(Absyn.DecList e)
	{
		
		Translate.Exp ex = null;
		for (Absyn.DecList i = e; i!= null; i = i.tail)
			ex = trans.combine2Stm(ex, transDec(i.head));

		return new ExpTy(ex, new VOID());
	}
	private ExpTy transExp(Absyn.SeqExp e)
	{
		
		Translate.Exp ex = null;
		for (Absyn.ExpList t = e.list; t != null; t = t.tail)
		{
			ExpTy x = transExp(t.head);

			if (t.tail == null)
			{	
				
				if (x.ty.actual() instanceof VOID)
					ex = trans.combine2Stm(ex, x.exp);
				else 
				{
					ex = trans.combine2Exp(ex, x.exp);
				}
				return new ExpTy(ex, x.ty);
			}
			ex = trans.combine2Stm(ex, x.exp);	
		}
		env.errorMsg.error(e.pos, "SeqExp has Errors!");
		return null;
	}
	private ExpTy transVar(Absyn.SimpleVar e)
	{
		
		Entry ex = (Entry)env.vEnv.get(e.name);
		
		if (ex == null || !(ex instanceof VarEntry))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		VarEntry evx = (VarEntry)ex;
		return new ExpTy(trans.transSimpleVar(evx.acc, level), evx.Ty);
	}
	private ExpTy transVar(Absyn.SubscriptVar e)
	{
		
		if (!(transExp(e.index).ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}		
		ExpTy ev = transVar(e.var);
	
		ExpTy ei = transExp(e.index);
	
		
		if (ev == null || !(ev.ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		ARRAY ae = (ARRAY)(ev.ty.actual());
		return new ExpTy(trans.transSubscriptVar(ev.exp, ei.exp), ae.element);
	}
	private ExpTy transVar(Absyn.FieldVar e)
	{
		
		ExpTy et = transVar(e.var);
		
		if (!(et.ty.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		
		RECORD rc = (RECORD)(et.ty.actual());
		int count = 1;
		while (rc != null)
		{
			if (rc.fieldName == e.field)
			{
				return new ExpTy(trans.transFieldVar(et.exp, count), rc.fieldType);
			}
			count++;
			rc = rc.tail;
		}
		env.errorMsg.error(e.pos, "error");
		return null;
	}
	private Type transTy(Absyn.NameTy e)
	{
		
		if (e == null)
			return new VOID();
		
		Type t =(Type)env.tEnv.get(e.name);
		
		if (t == null)
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		return t.actual();
	}
	private ARRAY transTy(Absyn.ArrayTy e)
	{
		Type t = (Type)env.tEnv.get(e.typ);
		
		if (t == null)
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		return new ARRAY(t);
	}
	private RECORD transTy(Absyn.RecordTy e)
	{
		RECORD rc = new RECORD(),  r = new RECORD();
		if (e == null || e.fields == null)
		{
			rc.gen(null, null, null);
			return rc;
		}
		
		Absyn.FieldList fl = e.fields;
		boolean first = true;
		while (fl != null)
		{
			
			
			rc.gen(fl.name, (Type)env.tEnv.get(fl.typ), new RECORD());
			if (first)
			{
				r = rc;
				first = false;
			}
			if (fl.tail == null)
				rc.tail = null;
			rc = rc.tail;
			fl = fl.tail;
		}		
		
		return r;
	}
	private Translate.Exp transDec(Absyn.VarDec e)
	{
		
		ExpTy et = transExp(e.init);
		
		if (et == null )	
		{
			env.errorMsg.error(e.pos,"error");
			 return null;
		}
	
		if (e.typ == null && e.init instanceof Absyn.NilExp)
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		if (e.init == null )
		{
			env.errorMsg.error(e.pos, "error");
			return null;
		}
		Translate.Access acc = level.allocLocal(true);
	
		if (e.typ != null)
		{
			env.vEnv.put(e.name, new VarEntry((Type)env.tEnv.get(e.typ.name), acc));
		}
		
		else
		{
			env.vEnv.put(e.name, new VarEntry(transExp(e.init).ty, acc));
		}
		return trans.transAssignExp(trans.transSimpleVar(acc, level), et.exp);
	}
	private Translate.Exp transDec(Absyn.TypeDec e)
	{
	
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();
		
		for (Absyn.TypeDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{ 
				env.errorMsg.error(e.pos, "error");
				return null;
			}
			hs.add(i.name);
		}

		for (Absyn.TypeDec i = e; i != null; i = i.next)
		{
			env.tEnv.put(i.name, new NAME(i.name));
			((NAME)env.tEnv.get(i.name)).bind(transTy(i.ty));
			NAME field = (NAME)env.tEnv.get(i.name);
			if(field.isLoop() == true) 
				{
				env.errorMsg.error(i.pos, "error");
				return null;
				}
		}	
	
	for (Absyn.TypeDec i = e; i != null; i = i.next)
		env.tEnv.put(i.name, transTy(i.ty));
		return trans.transNoOp();
	}
	
	private Translate.Exp transDec(Absyn.FunctionDec e)
	{
		
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();
		ExpTy et = null;
		
		for (Absyn.FunctionDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{
				env.errorMsg.error(e.pos, "error");
				return null;
			}
			if (env.stdFuncSet.contains(i.name))
			{
				env.errorMsg.error(e.pos, "error");
				return null;
			}
			
			Absyn.RecordTy rt = new Absyn.RecordTy(i.pos, i.params);
			RECORD  r = transTy(rt);
			if ( r == null){
				env.errorMsg.error(e.pos, "Record is empty!");	
				return null;
			}
			
			BoolList bl = null;
			for (FieldList f = i.params; f != null; f = f.tail)
			{
				bl = new BoolList(true, bl);
			}
			level = new Level(level, i.name, bl);
			env.vEnv.put(i.name, new FunEntry(level, new Temp.Label(i.name), r, transTy(i.result)));
			env.vEnv.beginScope();
			Translate.AccessList al = level.formals.tail;
			for (RECORD j = r; j!= null; j = j.tail)
			{
				if (j.fieldName != null)
				{
					env.vEnv.put(j.fieldName, new VarEntry(j.fieldType, al.head));
					al = al.tail;
				}
			}			
			et = transExp(i.body);
			
			
			if (et == null)
			{	env.vEnv.endScope();	
			env.errorMsg.error(e.pos, "Body is empty.");
				return null;	}
		
			if (!(et.ty.actual() instanceof VOID)) 
				trans.procEntryExit(level, et.exp, true);
			else 
				trans.procEntryExit(level, et.exp, false);
			
			env.vEnv.endScope();
			level = level.parent;
			
			hs.add(i.name);
		}
		return trans.transNoOp();
	}
}



