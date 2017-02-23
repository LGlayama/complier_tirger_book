package Translate;
import Temp.Label;


class Ex extends Exp {
  Tree.Exp exp;
  Ex(Tree.Exp e) {
    exp = e;
  }
  
  Tree.Exp unEx() {
    return exp;
  }
  
  Tree.Stm unNx() {
    return new Tree.EXPSTM(exp);
  }
  
  Tree.Stm unCx(Label t, Label f) {
    // if the EXP is a constant, emit JUMP statement.
    if (exp instanceof Tree.CONST) {
      Tree.CONST c = (Tree.CONST)exp;
      if (c.value == 0)
        return new Tree.JUMP(f);
      else
        return new Tree.JUMP(t);
    }
    return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);
  }

}
