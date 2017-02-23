package Translate;

import Temp.Label;
import Tree.*;


public class WhileExp extends Exp {
    Exp test = null; //测试条件
    Exp body = null; //循环体
    Label done = null; //完成出口
    WhileExp(Exp test, Exp body, Label done) {
        this.test = test;
        this.body = body;
        this.done = done;
    }
  //whlie 没有返回值
    Tree.Exp unEx() {
        System.err.println("WhileExp.unEx()");
        return null;
    }

  //LABEL BEGIN:
  //if (test) goto T else goto DONE
  //LABEL T:
  //body
  //goto BEGIN
  //LABEL DONE:
    Stm unNx() {
        Label begin = new Label();
        Label t = new Label();
        return new SEQ(new LABEL(begin),
                new SEQ(test.unCx(t, done),
                        new SEQ(new LABEL(t),
                                new SEQ(body.unNx(),
                                        new SEQ(new JUMP(begin),
                                                new LABEL(done))))));
    }
    
  //while 只有一个出口
    Stm unCx(Label t, Label f) {
        System.err.println("WhileExp.unCx()");
        return null;
    }
}