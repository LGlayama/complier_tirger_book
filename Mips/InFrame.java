package Mips;

class InFrame extends Frame.Access {
  private MipsFrame frame;
  int offset;
  InFrame(MipsFrame frame,int o) {
	this.frame = frame;
	this.offset = o;
  }

  public Tree.Exp exp(Tree.Exp fp) {
    return new Tree.MEM
      (new Tree.BINOP(Tree.BINOP.PLUS, fp, new Tree.CONST(offset)));
  }
  
  public Tree.Exp expFromStack(Tree.Exp stackPtr) {
	  return new Tree.MEM(new Tree.BINOP(Tree.BINOP.PLUS, stackPtr, new Tree.CONST(offset
	  - frame.offset - frame.wordSize())));
	  }

  /*public String toString() {
    Integer offset = new Integer(this.offset);
    return offset.toString();
  }*/
}
