package Flowgraph;

import java.util.Dictionary;
import java.util.Hashtable;

import Assem.Instr;
import Assem.InstrList;
import Assem.LABEL;
import Assem.Targets;
import Graph.Node;
import Graph.NodeList;
import Temp.Label;
import Temp.LabelList;
import Temp.TempList;

public class AssemFlowGraph extends FlowGraph {
	java.util.Map<Node, Instr> represent=new java.util.HashMap<Node, Instr>() ;
	
//根据汇编指令创建流图
public AssemFlowGraph(InstrList instrs) {
Dictionary<Label, Node> labels = new Hashtable<Label, Node>(); //标号表，表示哪些节点是Label指令
//添加结点和标号
for (InstrList i = instrs; i != null; i = i.tail) {
Node node = newNode();
represent.put(node, i.head);
if (i.head instanceof LABEL)
labels.put(((LABEL) i.head).label, node);
}
//添加边
for (NodeList node = nodes(); node != null; node = node.tail) {
Targets next = instr(node.head).jumps(); //跳转标号表
if (next == null) { //没有跳转,则当前指令和下一指令连一条边
if (node.tail != null) addEdge(node.head, node.tail.head);
} else //否则当前指令和所有跳转标号连边
for (LabelList l = next.labels; l != null; l = l.tail)
addEdge(node.head, (Node) labels.get(l.head));
}
}
//返回某结点表示的汇编指令
public Instr instr(Node n) {return (Instr) represent.get(n);}
//返回某结点定义的变量
public TempList def(Node node) {return instr(node).def();}
//返回某结点使用的变量
public TempList use(Node node) {return instr(node).use();}
//是否是move 指令
public boolean isMove(Node node) {
Instr instr = instr(node);
return instr.assem.startsWith("move");
}
}