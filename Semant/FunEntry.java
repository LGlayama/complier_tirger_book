package Semant;

public class FunEntry extends Entry{
	Translate.Level level;
	Temp.Label label;
	Types.RECORD formals;
	Types.Type result;
	public FunEntry(Translate.Level l,  Temp.Label lab,Types.RECORD f,Types.Type r){
		formals=f;result=r;level=l; label=lab;}
}
