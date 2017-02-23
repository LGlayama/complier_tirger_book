package Frame;

import Assem.InstrList;
import Temp.Label;
import Temp.Temp;
import Temp.TempList;
import Temp.TempMap;
import Tree.Stm;
import Util.BoolList;


public abstract class Frame implements TempMap {
	abstract public Frame newFrame(Label name, BoolList formals);//
    public Label name;
    public AccessList formals=null;
    abstract public Access allocLocal(boolean escape);//
    abstract public Tree.Exp externalCall(String func, Tree.ExpList args);
    abstract public Temp FP();//
    abstract public Temp SP();//
    abstract public Temp RA();//
    abstract public Temp RV();//
    abstract public TempList registers();
    abstract public Stm procEntryExit1(Stm body);//
    abstract public InstrList procEntryExit2(InstrList body);//
    abstract public InstrList procEntryExit3(InstrList body);//
    abstract public String string(Label lab, String lit);//
    abstract public InstrList codegen(Stm stm);//
    abstract public int wordSize();//
    abstract public TempList colors();
    /*static final Label error = new Label("error");
    public static Label getError() { return error; }

    abstract public TempList colors();
    abstract public String tempMap(Temp temp);
    abstract public String pre();
    abstract public String post();*/
}