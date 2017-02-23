package Translate;
import Absyn.OpExp;
import Temp.Label;
import Tree.CJUMP;
import Tree.Stm;

public class RelCx extends Cx {
  int oper = 0;
  Exp left = null;
  Exp right = null;
  public RelCx(int oper, Exp left, Exp right) {
    switch (oper) {
      case Absyn.OpExp.EQ: //=
        this.oper = CJUMP.EQ;
        break;
      case Absyn.OpExp.NE: //!=
        this.oper = CJUMP.NE;
        break;
      case OpExp.GE:
        this.oper = CJUMP.GE;
        break;
        case OpExp.GT:
          this.oper = CJUMP.GT;
          break;
      case OpExp.LE:
        this.oper = CJUMP.LE;
        break;
      case OpExp.LT:
        this.oper = CJUMP.LT;
        break;
      default:
        System.err.println("Translate.Relcx() is called using a undefined oper.");
    }
    this.left = left;
    this.right = right;
  }
//先把左右都翻译成由返回值的Ex
//再用CJUMP 处理跳转
  public Stm unCx(Label t, Label f) {
    return new CJUMP(oper, left.unEx(), right.unEx(), t, f);
  }
}
