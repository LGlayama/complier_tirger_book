package Translate;
import Temp.Temp;
import Temp.Label;
import Tree.*;

abstract class Cx extends Exp {
    Tree.Exp unEx() {
        Temp r = new Temp();
        Label t = new Label();
        Label f = new Label();
        return new ESEQ(
                new SEQ(new MOVE(new TEMP(r), new CONST(1)),
                        new SEQ(unCx(t, f),
                                new SEQ(new LABEL(f),
                                        new SEQ(new MOVE(new TEMP(r), new Tree.CONST(0)),
                                                new LABEL(t))))),
                new TEMP(r));
    }
    
    Stm unNx() {
    	return new EXPSTM(unEx());
    } 
    
    abstract Stm unCx(Label t, Label f); 
}

