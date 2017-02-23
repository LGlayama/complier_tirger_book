package Translate;

import java.util.Stack;
import Temp.Label;

public class LoopEnv {
	Stack <Stack <Label> > s = new Stack <Stack <Label> > ();
	public LoopEnv() {
		s.add(new Stack<Label> ());
	}
	public void beginScope() {
		s.add(new Stack<Label> ());
	}
	public void endScope() {
		s.pop();
	}
	public void newLoop() {
		s.peek().add(new Label());
	}
	public Label exitLoop() {
		return s.peek().pop();
	}
	public Label done() {
		return s.peek().peek();
	}
	public boolean inLoop() {
		return (!s.peek().isEmpty());
	}
}
