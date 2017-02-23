package Semant;

import Translate.Exp;
import Types.Type;
public class ExpTy {
Exp exp; //语义表达式
Type ty; //语义类型
ExpTy(Exp exp, Type ty) {
this.exp = exp;
this.ty = ty;
}
}