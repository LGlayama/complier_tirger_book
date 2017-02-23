/*package Regalloc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import Graph.Graph;
import Graph.Node;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempList;


public class Liveness extends InterferenceGraph {
	//flowgraph
	Flowgraph.FlowGraph flowGraph;
	
	Hashtable<Temp, TempNode> temp2node = new Hashtable<Temp, TempNode>();
	
	MoveList movelist = null;
	
	//node 和 nodeinfo 的映射
	java.util.HashMap<Node, NodeInfo> nodeinfo =new java.util.HashMap<Node, NodeInfo>();
	java.util.HashMap<Node, TempList> liveMap =new java.util.HashMap<Node, TempList>();
	
	public Liveness(Flowgraph.FlowGraph flowGraph) {
    	this.flowGraph = flowGraph;
    	initNodeInfo(); //初始化
    	calculateLiveness(); //计算活性变量
    	buildGraph(); //生成干扰图
    	}
	
    public void initNodeInfo(){
		NodeList node=flowGraph.nodes();
		TempList use=null;
		TempList def=null;
		//遍历流图所有指令
		for(;node != null; node=node.tail) {
			//关联node和nodeinfo
			NodeInfo info=new NodeInfo();
			nodeinfo.put(node.head, info );
			
			//四个set初始进null
			//info.in.add(null);
			//info.out.add(null);
			//info.use.add(null);
			//info.def.add(null);
			use=flowGraph.use(node.head);
			def=flowGraph.def(node.head);
			
			for(;use != null; use=use.tail) {
				info.use.add(use.head);
			}
			for(;def != null; def=def.tail) {
				info.def.add(def.head);
			}

		}
	}
	
    
    void calculateLiveness() {
    	boolean done = false;
    	do {
    	done = true;
    	for(NodeList node=flowGraph.nodes();node != null; node=node.tail) {
    	//遍历流图所有指令
    		NodeInfo inf = (NodeInfo) nodeinfo.get(node.head);//更新前的活性信息
    		//等式1
    		Set<Temp> in1 = new HashSet<Temp>(inf.out);
    		in1.removeAll(inf.def);
    		in1.addAll(inf.use);
    		if (!in1.equals(inf.in)) done = false; //测试是否完成
    		inf.in = in1; //更新 in
    		//等式2
    		Set<Temp> out1 = new HashSet<Temp>();
    		for (NodeList succ = node.head.succ(); succ != null; succ =
    		succ.tail) {
    		NodeInfo i = (NodeInfo) nodeinfo.get(succ.head);
    		out1.addAll(i.in);
    		}
    		if (!out1.equals(inf.out)) done = false; //测试是否完成
    		inf.out = out1; //更新 out
    		}
    		} while (!done);
    		//生成liveMap
    		for (NodeList node = flowGraph.nodes();node != null;node = node.tail) {
    		TempList list = null;
    		//得到活性信息中活跃变量的迭代器
    		Iterator<Temp> i = ((NodeInfo) nodeinfo.get(node.head)).out.iterator();
    		while (i.hasNext()) list = new TempList((Temp) i.next(), list);
    		if (list != null) liveMap.put(node.head, list);
    		}
    }

    
    void buildGraph() {
    	Set<Temp> temps = new HashSet<Temp>(); //包含流图中所有有关的变量(使用的和定义的)
    	//生成temps
    	for (NodeList node = flowGraph.nodes(); node != null; node = node.tail)
    	{
    		for (TempList t = flowGraph.use(node.head); t != null; t = t.tail)
    			temps.add(t.head);
    		for (TempList t = flowGraph.def(node.head); t != null; t = t.tail)
    			temps.add(t.head);
    	}
    	//生成 tnode
    	Iterator<Temp> i = temps.iterator();
    	while (i.hasNext()) 
    		newNode((Temp) i.next());
    	//生成干扰图
    	for (NodeList node = flowGraph.nodes(); node != null; node = node.tail)
    		//遍历流图中每一条指令
    		for (TempList t = flowGraph.def(node.head); t != null; t = t.tail)
    			//遍历指令中定义的所有变量
    			for (TempList t1 = (TempList) liveMap.get(node.head); t1 != null;
    					t1 = t1.tail)
    				//遍历指令的所有活跃变量
    				//应用以上规则a,b,尝试添加边
    				if (t.head != t1.head //防止自回路
    				&& !(flowGraph.isMove(node.head)
    						&& flowGraph.use(node.head).head == t1.head)) {
    					addEdge(tnode(t.head), tnode(t1.head));
    					addEdge(tnode(t1.head), tnode(t.head)); //无向图,双向加边
    				}
    }
    
    public Node newNode(Temp t) {
        TempNode n = new TempNode(this, t);
        temp2node.put(t, n);
        return n;
    }
    
    
    public Node tnode(Temp temp) {
        Node n = (Node) temp2node.get(temp);
        if (n == null) // make new nodes on demand.
        {
            return newNode(temp);
        } else {
            return n;
        }
    }
    
	public MoveList moves() {
        return movelist;
}

	public Temp gtemp(Node node) {
		if (!(node instanceof TempNode)) {
			throw new Error("Node " + node.toString()
                	+ " not a member of graph.");
		} else {
			return ((TempNode) node).temp;
		}
	}
}
    
   
    class TempNode extends Node {

        Temp temp;

        TempNode(Graph g, Temp t) {
            super(g);
            temp = t;
        }

    public String toString() {
        return temp.toString(); // +"("+super.toString()+")";
        }

    }

   
        public int hashCode() {
            return temp.hashCode();
        }

    private void print(String heading, TempSet[] t) {
        System.err.print(heading);
        for (int i = 0; i < t.length; i++) {
            System.err.println("\t" + i + ":" + t[i].toString());
        }
    }

	
    boolean debug = false;

    private Map liveIntervals;

    



    Hashtable nodeuses = new Hashtable();

    // Utility class for describing sets of register temporary operands.
    static class TempSet {

        HashSet set; // contains Temp

        TempSet() {
            set = new HashSet();
        }

        TempSet(TempList t) {
            set = new HashSet<Temp>();
            if (t == null) {
                return;
            }
            for (TempList temp = t; temp != null; temp = temp.tail) {
                add(temp.head);
            }
        }

        void add(Temp t) { // only add operands that are Temp's
            set.add(t);
        }

        public boolean equals(Object os) {
            return (os instanceof TempSet) && (((TempSet) os).set.equals(set)); // expensive!
        }

        void diff(TempSet os) {
            for (Iterator it = os.set.iterator(); it.hasNext();) {
                set.remove(it.next());
            }
        }

        void union(TempSet os) {
            for (Iterator it = os.set.iterator(); it.hasNext();) {
                set.add(it.next());
            }
        }

        TempSet copy() {
            TempSet s = new TempSet();
            s.set = (HashSet) set.clone();
            return s;
        }

        public String toString() {
            String r = "{ ";
            for (Iterator it = set.iterator(); it.hasNext();) {
                r += it.next() + " ";
            }
            r += "}";
            return r;
        }

        Iterator iterator() {
            return set.iterator();
        }
    }

    // Utility class for describing lists of integers
    static class IndexList {

        List list;

        IndexList() {
            list = new ArrayList();
        }

        void add(int i) {
            list.add(new Integer(i));
        }

        int get(int p) {
            return ((Integer) list.get(p)).intValue();
        }

        int size() {
            return list.size();
        }

        public String toString() {
            String r = "[";
            if (size() > 0) {
                r += get(0);
                for (int i = 1; i < size(); i++) {
                    r += "," + get(i);
                }
            }
            r += "]";
            return r;
        }

        public void print() {
            System.out.println(toString());
        }
    }

    private Node[] l2a(NodeList nl) {
        int n = 0;
        for (NodeList p = nl; p != null; p = p.tail) {
            n++;
        }

        Node[] a = new Node[n];
        int i = 0;
        for (NodeList p = nl; p != null; p = p.tail, i++) {
            a[i] = p.head;
        }
        return a;
    }

    private int indexOf(Node n, Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            if (n == nodes[i]) {
                return i;
            }
        }
        return -1;
    }



   
    static class Interval {

        int start;

        int end;

        Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    // calculate live interval for each temporary in body
    public Map calculateLiveIntervals(TempSet[] liveOut) {
        liveIntervals = new HashMap(); // keys are Temp; values are Interval
        for (int i = 0; i < liveOut.length; i++) {
            Iterator<Temp> it = liveOut[i].iterator();
            while (it.hasNext()) {
                Temp t = it.next();
                Interval n = (Interval) (liveIntervals.get(t));
                if (n == null) {
                    n = new Interval(i, i + 1);
                    liveIntervals.put(t, n);
                } else {
                    n.end = i + 1;
                }
            }
        }
        return liveIntervals;
    }

    private void addInterferenceEdges(AssemFlowGraph flow,
            Node flownode, Temp deftemp, Temp livetemp) {
        TempList s = flow.use(flownode);
        // if this is a move instruction, maybe we don't want to add this edge
        if (flow.isMove(flownode)) // see if this livetemp is part of the source of this move
        {
            for (TempList t = s; t != null; t = t.tail) // if so, don't add an edge
            {
                if (t.head == livetemp) {
                    return;
                }
            }
        }

        // tests okay, add the edge.
        addEdge(tnode(deftemp), tnode(livetemp));
        addEdge(tnode(livetemp), tnode(deftemp));
    }

    public String toString() {
        String result = "";
        Set lis = liveIntervals.entrySet();
        for (Iterator it = lis.iterator(); it.hasNext();) {
            Map.Entry me = (Map.Entry) (it.next());
            Temp t = (Temp) (me.getKey());
            Liveness.Interval n = (Liveness.Interval) (me.getValue());
            result = result + "" + t + "\t[" + n.start + "," + n.end + "]\n";
        }
        return result;
    }
*/
    package Regalloc;


    import java.util.HashMap;
    import java.util.HashSet;
    import java.util.Hashtable;
    import java.util.Iterator;
    import java.util.Map;
    import java.util.Set;

    import Flowgraph.AssemFlowGraph;
    import Flowgraph.FlowGraph;
    import Graph.Node;
    import Graph.NodeList;
    import Temp.Temp;
    import Temp.TempList;

    public class Liveness extends InterferenceGraph {
    	AssemFlowGraph flowGraph=null;
    	MoveList movelist = null;
    	private Hashtable<Node, TempList> liveMap = new Hashtable<Node, TempList>(); 
    	private Hashtable<Node,NodeInfo> node2nodeInfo = new Hashtable<Node,NodeInfo>();
    	HashMap <Temp,Node> temp2node = new HashMap<Temp,Node>();
    	private Hashtable<Node, Temp> node2temp = new Hashtable<Node, Temp>();
    	public Liveness(AssemFlowGraph flowGraph) {   
    		this.flowGraph = flowGraph;   
    		initNodeInfo(); 
    		calculateLiveness();
    		buildGraph(); 
    	}

    	void initNodeInfo(){
    		for(NodeList node=flowGraph.nodes();node != null; node=node.tail){
    			NodeInfo a = new NodeInfo(node.head);
    			node2nodeInfo.put(node.head, a);
    		}
    	}
    	void calculateLiveness() {
    		boolean done = false;
    		while (!done)
    		 {
    			done = true;
    			for(NodeList node=flowGraph.nodes();node != null; node=node.tail) {
    				NodeInfo inf = node2nodeInfo.get(node.head);
    				Set<Temp> in1 = new HashSet<Temp>();
    				in1.addAll(inf.out);
    				in1.removeAll(inf.def);
    				in1.addAll(inf.use);
    				if (!in1.equals(inf.in)) done = false; 
    				node2nodeInfo.get(node.head).in = in1;

    				Set <Temp>out1 = new HashSet<Temp>();
    				for (NodeList succ = node.head.succ(); succ != null; succ =succ.tail) {
    					NodeInfo i = (NodeInfo)node2nodeInfo.get(succ.head);
    					out1.addAll(i.in);
    				}
    				if (!out1.equals(inf.out)) done = false; 
    				node2nodeInfo.get(node.head).out = out1;
    			}
    		}
    		for (NodeList node = flowGraph.nodes();node != null;node = node.tail) {
    			TempList list = null;

    			for (Iterator i =((NodeInfo)node2nodeInfo.get(node.head)).out.iterator(); i.hasNext(); )
    				list = new TempList((Temp) i.next(), list);
    			if (list != null) liveMap.put(node.head,list);
    		}
    	}
    	void buildGraph() {
    		Set temps = new HashSet();

    		for (NodeList node = flowGraph.nodes(); node != null; node = node.tail){
    			for (TempList t = flowGraph.use(node.head); t != null; t = t.tail)
    				temps.add(t.head);
    			for (TempList t = flowGraph.def(node.head); t != null; t = t.tail)
    				temps.add(t.head);
    		}

    		Iterator i = temps.iterator();
    		while (i.hasNext()) add( newNode(),(Temp) i.next());

    		for (NodeList node = flowGraph.nodes(); node != null; node = node.tail){

    			for (TempList t = flowGraph.def(node.head); t != null; t = t.tail){

    				for (TempList t1 = (TempList) liveMap.get(node.head); t1 != null;t1 = t1.tail){
    					if (t.head != t1.head && !flowGraph.isMove(node.head)){
    						 addEdge(tnode(t.head), tnode(t1.head));            
    						 addEdge(tnode(t1.head), tnode(t.head)); 
    					} 
    					if (t.head != t1.head && flowGraph.isMove(node.head) && flowGraph.use(node.head).head != t1.head) {
    						addEdge(tnode(t.head), tnode(t1.head));                 
    						addEdge(tnode(t1.head), tnode(t.head)); 
    					}
    				}
    			}
    		}
    	}
    	void add(Node node, Temp temp)
    	{

    	temp2node.put(temp, node);
    	node2temp.put(node, temp);
    	}
    	@Override
    	public Node tnode(Temp temp) {
    		Node n = (Node) temp2node.get(temp);
    		return n;
    	}
    	
    	@Override
    	public Temp gtemp(Node node) {
    		return (Temp) node2temp.get(node);
    	}
    	@Override
    	public MoveList moves() {
    		return movelist;
    	}
    }







  




