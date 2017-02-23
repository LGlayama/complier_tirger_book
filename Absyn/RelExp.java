package Absyn;




public class RelExp extends Exp{
	public Exp left;
	public Exp right;
	public int oper;
	
	public RelExp(int p, Exp l, Exp r, int o){
		pos=p;
		left=l;
		right=r;
		oper=o;
	}



}
