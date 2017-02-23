package Main;
import Semant.Semant;
import Temp.DefaultMap;

import java.io.FileNotFoundException;

import ErrorMsg.ErrorMsg;
import Parse.Parse;
import Parse.sym;


class Main {

 static Frame.Frame frame = new Mips.MipsFrame();

 static void prStmList(Tree.Print print, Tree.StmList stms) {
     for(Tree.StmList l = stms; l!=null; l=l.tail)
	print.prStm(l.head);
 }

 static Assem.InstrList codegen(Frame.Frame f, Tree.StmList stms) {
  	Assem.InstrList first=null, last=null;
	    for(Tree.StmList s=stms; s!=null; s=s.tail) {
	       Assem.InstrList i = f.codegen(s.head);
	       if (last==null) {first=last=i;}
	       else {while (last.tail!=null) last=last.tail;
		     last=last.tail=i;
		    }
	    }
	    return first;
	}
		

 static void emitProc(java.io.PrintStream out, Translate.ProcFrag f, java.io.PrintStream IR) throws FileNotFoundException {
     
     
     Tree.Print print = new Tree.Print(IR);
     Tree.StmList stms = Canon.Canon.linearize(f.body);
     Canon.BasicBlocks b = new Canon.BasicBlocks(stms);
     IR.println("#IR Tree ");
     Tree.StmList traced = (new Canon.TraceSchedule(b)).stms;
     prStmList(print,traced);
     
    
     Assem.InstrList instrs= codegen(f.frame,traced);
     instrs = frame.procEntryExit2(instrs);
     Regalloc.RegAlloc regAlloc = new Regalloc.RegAlloc(f.frame, instrs);
   
     instrs = f.frame.procEntryExit3(instrs);
     Temp.TempMap tempmap = new Temp.CombineMap(f.frame,regAlloc);
     
     out.println(".text");
     for (Assem.InstrList p = instrs; p != null; p = p.tail)
     out.println(p.head.format(tempmap));
 
 }

 public static void main(String args[]) throws java.io.IOException {
	 System.out.println("Parsing...");
	 ErrorMsg errorMsg = new ErrorMsg(args[0]);
	 Parse parse = new Parse(errorMsg);
	 Absyn.Exp absyn = parse.parse(args[0]);
	 System.out.println("Parse Tree:");
	 new Absyn.Print(System.out).prExp(absyn, 0);
	 System.out.print("\nParse Complete.");	
	 System.out.println("Translating...");
	 java.io.PrintStream out = new java.io.PrintStream(new java.io.FileOutputStream(args[0] + ".s"));
	 java.io.PrintStream IR = new java.io.PrintStream(new java.io.FileOutputStream(args[0]+".IR"));
	 Frame.Frame frame= new Mips.MipsFrame();
	 Translate.Translate translate = new Translate.Translate(frame);
	 Semant semant = new Semant(translate,parse.errorMsg);
	 Translate.Frag frags = semant.transProg(absyn);
	 System.out.print("\nTranslation Complete.");
	 out.println(".globl main");
	 for(Translate.Frag f = frags; f!=null; f=f.next)
       if (f instanceof Translate.ProcFrag)
          emitProc(out, (Translate.ProcFrag)f, IR);
       else if (f instanceof Translate.DataFrag)
          out.println("\n.data\n"+((Translate.DataFrag)f).data);

	 java.io.BufferedReader br = new java.io.BufferedReader(new
			 java.io.InputStreamReader(new java.io.FileInputStream("E:\\compiler_project\\mycode\\tiger_compiler\\src\\Main\\runtime.s")));
	 String data = null;
	 while((data = br.readLine())!=null)
	 {
		out.println(data);
	 }
	 out.close();
 }

}




