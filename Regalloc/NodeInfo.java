package Regalloc;

import java.util.HashSet;
import java.util.Set;

import Flowgraph.FlowGraph;
import Graph.Node;
import Temp.Temp;
import Temp.TempList;


public class NodeInfo {
	
	Set<Temp> in = new HashSet<Temp>(); //��ָ��ǰ�Ļ��Ա���
	Set<Temp> out = new HashSet<Temp>(); //��ָ���Ļ��Ա��� �� ����Ծ����
	Set<Temp> use = new HashSet<Temp>(); //ĳָ��ʹ�õı��� - ��ֵ���ұ�
	Set<Temp> def = new HashSet<Temp>(); //ĳָ���ı��� - ��ֵ�����
	
	public NodeInfo(Node node){
		 for (TempList i = ((Flowgraph.FlowGraph)node.mygraph).def(node); i != null; i = i.tail)   
		 { 
			 def.add(i.head);      
		 }       
		 for (TempList i = ((Flowgraph.FlowGraph)node.mygraph).use(node); i != null; i = i.tail)       {   
			 use.add(i.head);      
			 } 
		 }
		
	}
	

	
	

