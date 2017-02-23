package Translate;
import Temp.Label;
import Symbol.Symbol;
import Util.BoolList;

public class Level {
  public Level parent;
  public Frame.Frame frame;		// not public!
  public AccessList formals;

  public Level(Level parent, Symbol name, BoolList fmls) {
    this.parent = parent;
    this.frame = parent.frame.newFrame(new Label(name), new BoolList(true, fmls));
    for (Frame.AccessList f = frame.formals; f != null; f = f.tail)
      this.formals = new AccessList(new Access(this, f.head),
              this.formals);
  }

/*  private AccessList allocFormals(Frame.AccessList formals) {
    if (formals == null)
      return null;
    return new AccessList(new Access(this, formals.head),
			  allocFormals(formals.tail));
  }*/
  public Access staticLink() {return formals.head;}

  public Level(Frame.Frame f) {
    frame = f;
  }

  public Label name() {
    return frame.name;
  }

  public Access allocLocal(boolean escape) {
    return new Access(this, frame.allocLocal(escape));
  }
}
