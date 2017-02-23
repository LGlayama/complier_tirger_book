package Regalloc;

import Assem.InstrList;
import Flowgraph.AssemFlowGraph;
import Frame.Frame;
import Temp.Temp;
import Temp.TempMap;

// why should they both implement TempMap??????
public class RegAlloc implements TempMap {

    /*private regCompare regCom = new regCompare();

    private java.io.PrintStream oStream;

    private boolean verbose;

    private Frame frame;

    private InstrList instrs;*/
    
	//private List<Temp> regs = new ArrayList<Temp>(32);
	
    public AssemFlowGraph flowGraph;
    public Liveness interGraph;
    private Color color;

    public RegAlloc(Frame f, InstrList instrs) {
    	flowGraph = new AssemFlowGraph(instrs);//���ݻ��ָ��������ͼ
    	interGraph=new Liveness(flowGraph);//���Է���,����ͼ
    	color = new Color(interGraph, f, f.registers());//��ɫ������Ĵ���
    	}

    public String tempMap(Temp temp) {
        return color.tempMap(temp);
    }

  

}