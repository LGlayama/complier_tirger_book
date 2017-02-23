package Mips;

import Temp.Temp;

class InReg extends Frame.Access {
  Temp temp;
  public InReg() {
    temp = new Temp();
  }

  public Tree.Exp exp(Tree.Exp fp) {
    return new Tree.TEMP(temp);
  }
  
  public Tree.Exp expFromStack(Tree.Exp stackPtr) {
	  return new Tree.TEMP(temp);
  }

  /*public String toString() {
    return temp.toString();
  }*/
}
