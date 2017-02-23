package Tree;

//Represents a temporary, which, in the abstract machine is similar to a register in a real machine.
//However, the abstract machine has an infinite number of temporaries.
public class TEMP extends Exp {
  public Temp.Temp temp;
  public TEMP(Temp.Temp t) {temp=t;}
  public ExpList kids() {return null;}
  public Exp build(ExpList kids) {return this;}
}

