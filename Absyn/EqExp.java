package Absyn;




public class EqExp extends Exp {
	public Exp left;
	public Exp right;
	public int oper;
	public EqExp(int p, Exp l, Exp r, int o){
		pos=p;
		left=l;
		right=r;
		oper=o;
	}

}
