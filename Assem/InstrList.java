package Assem;



public class InstrList {
  public Instr head;
  public InstrList tail;
  public InstrList(Instr h, InstrList t) {
    head=h; tail=t;
  }
  public InstrList(InstrList h, InstrList t) 
  {
	  InstrList inst=null;
	   while (h!=null)
	   {
		   inst=new InstrList(h.head,inst);
		   h=h.tail;
	   }
	   tail=t;
	   while (inst!=null)
	   {
		   tail=new InstrList(inst.head,tail);
		   inst=inst.tail;
	   }
	   head=tail.head;
	   tail=tail.tail;
  }

  public InstrList reverse() {
	InstrList temp = null;
	InstrList now = this;
	while (now != null) {
		temp = new InstrList(now.head, temp);
		now = now.tail;
	}
	return temp;
  }
}
