package Absyn;




public class CalcExp extends Exp{
	public Exp left;
	public Exp right;
	public int oper;
	
	public CalcExp(int p, Exp l, Exp r, int o){
		pos=p;
		left=l;
		right=r;
		oper=o;
	}


}
