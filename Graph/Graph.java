package Graph;

public class Graph {

  int nodecount=0;//图中已有的节点数
  NodeList mynodes, mylast;
  public NodeList nodes() { return mynodes;} 
  //在图中加入节点，并维护在nodelist中
  public Node newNode() {
	return new Node(this);
  }
  //检查node是不是这个图里的
  void check(Node n) {
      if (n.mygraph != this)
	throw new Error("Graph.addEdge using nodes from the wrong graph");
  }
  //检查node在不在图的nodelist里边
  static boolean inList(Node a, NodeList l) {
       for(NodeList p=l; p!=null; p=p.tail)
             if (p.head==a) return true;
       return false;
  }
  //维护node的两个属性pred和succs，这两个都是nodelist，很合理hhh
  public void addEdge(Node from, Node to) {
      check(from); check(to);
      if (from.goesTo(to)) return;
      to.preds=new NodeList(from, to.preds);
      from.succs=new NodeList(to, from.succs);
  }
  //在nodelist中删除一个节点，用了递归的方法，很高端。。恩
  NodeList delete(Node a, NodeList l) {
	if (l==null) throw new Error("Graph.rmEdge: edge nonexistent");
        else if (a==l.head) return l.tail;
	else return new NodeList(l.head, delete(a, l.tail));
  }
  //图中删除一条边。。
  public void rmEdge(Node from, Node to) {
	to.preds=delete(from,to.preds);
        from.succs=delete(to,from.succs);
  }

 /**
  * Print a human-readable dump for debugging.
  */
     public void show(java.io.PrintStream out) {
	for (NodeList p=nodes(); p!=null; p=p.tail) {
	  Node n = p.head;
	  out.print(n.toString());
	  out.print(": ");
	  for(NodeList q=n.succ(); q!=null; q=q.tail) {
	     out.print(q.head.toString());
	     out.print(" ");
	  }
	  out.println();
	}
     }

}