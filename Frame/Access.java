package Frame;



public abstract class Access {
	public abstract Tree.Exp exp(Tree.Exp framePtr); //以fp 为起始地址返回变量
	public abstract Tree.Exp expFromStack(Tree.Exp stackPtr); //以sp 为起始地址返回变量
}
