/*package Regalloc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import Assem.Instr;
import Graph.Node;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempList;
import Temp.TempMap;



public class Color implements TempMap {
	
	// the stack
	LinkedList<Node> selectStack = new LinkedList<Node>();
	TempMap initcoloring;
	
	private void pushNode(Node n) {
		selectStack.addFirst(n);
	}

	private Node popNode() {
		return selectStack.removeFirst();
	}
	
	private boolean isInStack(Node n) {
		return selectStack.contains(n);
	}
	
	public Map<Temp, Temp> map= new HashMap<Temp, Temp>();
	
	public Color(InterferenceGraph interGraph, TempMap init, TempList regs) {
		initcoloring=init;
		int number = 0;
		//遍历每个临时变量结点
		for (NodeList node=interGraph.nodes(); node != null;node = node.tail) {
			++number;
			//得到结点所对应的临时变量temp
			Temp temp = interGraph.gtemp(node.head);
			//如果temp 已经被分配了寄存器
			if (init.tempMap(temp) != null) {
				--number;
				pushNode(node.head); //将改结点压入堆栈
				map.put(temp, temp); //放入分配列表map 中，它们的寄存器就是本身了
				//删除从该结点出发的所有边
				for (NodeList adj = node.head.succ(); adj != null; adj = adj.tail)
					interGraph.rmEdge(node.head, adj.head);
			}
		}
		//剩下是number 个还没有被分配寄存器的
		for (int i = 0; i < number; ++i) {
			Node node = null;
			int max = -1;
			//再次遍历每个临时变量结点
			for (NodeList n = interGraph.nodes(); n != null; n = n.tail)
				if (init.tempMap(interGraph.gtemp(n.head)) == null
				&& !isInStack(n.head)) { //没有被分配寄存器且不在堆栈中
					int num = n.head.outDegree(); //出度
					if (max < num && num < regs.size()) {
						//找到一个度最大的且小于寄存器数目的结点
						max = num;
						node = n.head;
					}
				}
			if (node == null) { //度大于等于寄存器数目，溢出
				System.err.println("Color.color() : register spills.");
				break;
			}
			//否则继续推入堆栈并移去从不在堆栈中的结点指向该结点的所有边
			pushNode(node);
			for (NodeList adj = node.pred(); adj != null; adj = adj.tail)
				if (!isInStack(adj.head))
					interGraph.rmEdge(adj.head, node);
		}
		//接下来开始分配那number 个没有分配寄存器的临时变量，它们处在栈顶
		for (int i = 0; i < number; ++i) {
			Node node = popNode(); //弹出一个
			Set<Temp> available = new HashSet<Temp>();//可供分配寄存器列表
			TempList aregs=regs;
			while(aregs!=null){
				available.add(aregs.head);
				aregs=aregs.tail;
			}
			for (NodeList adj = node.succ(); adj != null; adj = adj.tail) {
				//从可供分配寄存器列表中移除该结点指向的某个结点所代表的寄存器
				available.remove(map.get(interGraph.gtemp(adj.head)));
			}
			//取剩下的一个作为寄存器
			Temp reg = (Temp) available.iterator().next();
			//加入寄存器分配表
			map.put(interGraph.gtemp(node), reg);
		}
	}


	public String tempMap(Temp t) {
		if (t == null) {
			throw new Error("Attempted to color map a null");
		}
		if (!map.containsKey(t)) {
			throw new Error("No color for the temp " + t);
		}
		return initcoloring.tempMap(map.get(t));
	}

	
	
	
	/*private regCompare regCom = new regCompare();

	boolean verbose;

	PrintStream oStream;
	InterferenceGraph interGraph;


	// precolored: the set of precolored nodes (frame's registers)
	Set<Temp> precolored;

	Set<Node> virtualRegs = new HashSet<Node>();

	// colors: the set of nodes available for coloring
	Set<Temp> colors;

	// the node info structure
	private Map<Node, NodeInfo> infoMap = new HashMap<Node, NodeInfo>();

	private NodeInfo nodeInfo(Node n) {
		if (infoMap.get(n) == null) {
			throw new Error("Node " + n + " not mapped to any info!");
		}
		return infoMap.get(n);
	}



	// the simplify worklist
	LinkedList<Node> simplifyWorkList = new LinkedList<Node>();

	private void addSimplifyCandidate(Node n) {
		simplifyWorkList.add(n);
		nodeInfo(n).setSimplifyMember();
	}

	private Node removeSimplifyCandidate() {
		Node r = simplifyWorkList.removeFirst();
		nodeInfo(r).setStackMember();
		return r;
	}

	// the spilled temps
	List<Temp> spills = new ArrayList<Temp>();

	// the spill worklist
	LinkedList<Node> spillWorkList = new LinkedList<Node>();

	private void addSpillCandidate(Node n) {
		spillWorkList.add(n);
		nodeInfo(n).setSpillMember();
	}

	private Node removeSpillCandidate() {
		Node r = spillWorkList.removeFirst();
		addSimplifyCandidate(r);
		return r;
	}

	// the move worklist
	MoveList workListMoves;

	// map each temp to a machine register; for precoloreds, it's the identity
	// mapping

	private TempMap initialColoring;

	

	private int numColors;

	public String tempMap(Temp t) {
		if (t == null) {
			throw new Error("Attempted to color map a null");
		}
		if (!coloring.containsKey(t)) {
			throw new Error("No color for the temp " + t);
		}
		return initialColoring.tempMap(coloring.get(t));
	}

	private void initialize(TempMap initial) {
		initialColoring = initial;
		numColors = colors.size();

		for (Temp register : precolored) {
			coloring.put(register, register);
		}
		// Idea: don't modify the interference graph structure at all. Instead,
		// keep the following data:
		// adjacent: mapping of each node to its "remaining" neighbors, i.e.
		// those in graph not on stack (or coalesced, later)
		// virtualRegs: set of all temps (nodes?) used in the program that are
		// not precolored
		// simplifyWorklist: list of low-degree (non-move) nodes
		// ( no freezeWorklist, spillWorklist, spilledNodes, coalescedNodes )
		// Additionally: a mapping from nodes to enum value indicating which of
		// these disjoint sets they are in
		// Later, initialize also: movelist, alias
		// Put the insignificant-degree nodes into simplifyWorklist
		// Now we should have the invariants:
		// 1.
		// u in simplifyWorklist ( U freezeWorklist U spillWorklist )
		// implies
		// degree(u) = cardinality of
		// adjList(u) intersect
		// (precolored U simplifyWorklist ( U freezeWorklist U spillWorklist ) )

		// 2. Either u has been selected for spilling, or
		// u in simplifyWorklist
		// implies
		// degree(u) < K & moveList[u] intersect (activeMoves U worklistMoves) =
		// {}

		// 3.
		// u in freezeWorkList
		// implies
		// degree(u) < K & moveList[u] intersect (activeMoves U worklistMoves)
		// != {}

		// 4.
		// u in spillWorklist implies degree(u) >= K
		for (NodeList nodes = ig.nodes(); nodes != null; nodes = nodes.tail) {			
			Node node = nodes.head;
			NodeInfo info = new NodeInfo();
			infoMap.put(node, info);
			info.setNotOnStack();
			info.setNeighbors(node.adj());
			if (!precolored.contains(ig.gtemp(node))) { // a "virtual" register
				virtualRegs.add(node);
				info.setVirtual();
				info.setDegree(info.adj.size());
				if (info.degree < numColors) {
					addSimplifyCandidate(node);
				} else {
					addSpillCandidate(node);
				}
			} else { // a machine register
				info.setPrecolored();
				info.setColor(ig.gtemp(node));
				info.setDegree(Integer.MAX_VALUE);
			}
		}
		// set up the move worklist and mark the nodes move-related
		for (MoveList ms = ig.moves(); ms != null; ms = ms.tail) {
			nodeInfo(ms.src).addMove(ms.src, ms.dst);
			nodeInfo(ms.dst).addMove(ms.src, ms.dst);
		}
		workListMoves = ig.moves();
	}

	private Set<Node> adjacent(Node n) {
		Set<Node> result = new HashSet<Node>();
		for (Node m : nodeInfo(n).adj) {
			if (!nodeInfo(m).isOnStack() && !nodeInfo(m).isCoalesced()) {
				result.add(m);
			}
		}
		return result;
	}

	private void decrementDegree(Node n) {
		NodeInfo info = nodeInfo(n);
		int d = info.degree;
		info.setDegree(d - 1);
		if (d == numColors) {
			spillWorkList.remove(n);
			addSimplifyCandidate(n);
		}
	}

	private void simplify() {
		Node n = removeSimplifyCandidate();
		push(n);
		for (Node neighbor : adjacent(n)) {
			decrementDegree(neighbor);
		}
	}

	private void selectSpill() {
		removeSpillCandidate();
	}

	// For coalescing
	private Node getAlias(Node n) {
		return n;
	}

	private void assignColors() {

		while (!selectStack.isEmpty()) {
			Node n = pop();
			if (verbose) {
				oStream.println("Coloring node " + n + " for temp "
						+ ig.gtemp(n));
			}
			TreeSet<Temp> okColors = new TreeSet<Temp>(regCom);
			okColors.addAll(colors);
			Temp color;
			for (NodeList neighbors = n.adj(); neighbors != null; neighbors = neighbors.tail) {
				Node trueName = getAlias(neighbors.head);
				if ((color = nodeInfo(trueName).color) != null) {
					okColors.remove(color);
					if (verbose) {
						oStream.println("Neighbor has color " + color);
					}
				}
			}
			if (okColors.isEmpty()) {
				if (verbose) {
					oStream.println("Spilling node " + n + " for temp "
							+ ig.gtemp(n));
				}
				spills.add(ig.gtemp(n));
			} else {
				color = okColors.first();
				nodeInfo(n).setColor(color);
				coloring.put(ig.gtemp(n), color);
				if (verbose) {
					oStream.println("Coloring node " + n + " for temp "
							+ ig.gtemp(n) + " with color " + color);
				}
			}
			// if coalescing, color all the coalesced nodes too
		}
		if (verbose) {
			oStream.println("Coloring complete");
			// for ( Node n : virtualRegs ) {
			// oStream.println("Virtual register: "+ig.gtemp(n)+", machine
			// register: "+coloring.get(ig.gtemp(n)));
			// }
		}
	}

	private String wlToString(List<Node> wl) {
		String result = "[ ";
		for (Node n : wl) {
			result += ig.gtemp(n) + " ";
		}
		return result + "]";
	}

	private void coalesce() {
		throw new Error("Coalescing not implemented");
	}

	
	/*public Color(InterferenceGraph ig, TempMap initial, List<Temp> registers,
			Set<Temp> colors, boolean verbose, PrintStream oStream) {
		this.verbose = verbose;
		this.oStream = oStream;
		this.ig = ig;
		precolored = new TreeSet<Temp>(regCom);
		precolored.addAll(registers);
		if (verbose)
			System.out.println(precolored.toString());
		this.colors = colors;

		initialize(initial);
		do {
			if (verbose) {
				oStream.println("Simplify iteration: ");
				oStream.println(" simplify worklist = "
						+ wlToString(simplifyWorkList));
				oStream.println(" spill worklist = "
						+ wlToString(spillWorkList));
			}
			if (!simplifyWorkList.isEmpty())
				simplify();
			// else if ( ! worklistMoves.isEmpty() )
			// coalesce();
			// else if ( ! freezeWorklist.isEmpty() )
			// freeze();
			else if (!spillWorkList.isEmpty())
				selectSpill();
		} while ((!simplifyWorkList.isEmpty()) // || (!
												// workListmoves.isEmpty())
				// || (! freezeWorkList.isEmpty())
				|| (!spillWorkList.isEmpty()));
		assignColors();
	}*/
package Regalloc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import Frame.Frame;
import Graph.Node;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempList;
import Temp.TempMap;


public class Color implements TempMap {
	Stack <Node>stack = new Stack();
	private Map<Temp, Temp> coloring = new HashMap<Temp, Temp>();
	InterferenceGraph interGraph;
	private TempMap init;
	public Color(InterferenceGraph ig,Frame f,TempList regs) {
		this.interGraph=ig;
		this.init=f;
		
	 	int number = 0;   
	 	for (NodeList node=interGraph.nodes(); node != null;node = node.tail) {    
	 		++number;    
	 		Temp temp = interGraph.gtemp(node.head);       
	 		if (init.tempMap(temp) != null) {    
	 		 --number;     
	 		 stack.push(node.head);     
	 		 coloring.put(temp, temp);    
	 		 for (NodeList adj = node.head.succ(); adj != null; adj = adj.tail) 
				interGraph.rmEdge(node.head, adj.head);  
			}  
		}   
		for (int i = 0; i < number; ++i) {    
			Node node = null;    
			int max = -1;       
			for (NodeList n = interGraph.nodes(); n != null; n = n.tail){    
				if (init.tempMap(interGraph.gtemp(n.head)) == null && stack.search(n.head)==-1) { 
				    int num = n.head.outDegree();
				    if (max < num && num < regs.size()) {           
				    	max = num;       
				    	node = n.head;      
				    }     
				} 
			}   
			if (node == null) {     
				System.err.println("Color.color() : register spills.");     
				break;    
		}      
			stack.push(node);    
			for (NodeList adj = node.pred(); adj != null; adj = adj.tail){    
				if (stack.search(adj.head)==-1)      
					interGraph.rmEdge(adj.head, node);   
			} 
		}    
		for (int i = 0; i < number; ++i) {    
			Node node1 = stack.pop();     
			Set<Temp> available = new HashSet<Temp>();
			TempList aregs=regs;
			while(aregs!=null){
				available.add(aregs.head);
				aregs=aregs.tail;
			}   
			for (NodeList adj = node1.succ(); adj != null; adj = adj.tail) {    
				available.remove(coloring.get(interGraph.gtemp(adj.head)));    
			}     
			Temp reg = (Temp) available.iterator().next();    
			coloring.put(interGraph.gtemp(node1), reg);  
			 
		}  
}
	@Override
	public String tempMap(Temp t) {
		if (t == null) {
			throw new Error("Attempted to color map a null");
		}
		return init.tempMap(coloring.get(t));
	}
}


