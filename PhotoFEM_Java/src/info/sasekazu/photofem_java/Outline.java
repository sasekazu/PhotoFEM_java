/**
 * PhotoFEM_Android.java
 * this class includes 
 * 	main function,
 * 	layout design,
 * 	event handling (mouse, button)
 */

package info.sasekazu.photofem_java;


import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;

public class Outline {

	private ArrayList<ClosedCurve> cc;	
	private float minlen;	// Minimum distance between each vertex
	
	public Outline(float minlen) {
		this.minlen = minlen;
		cc = new ArrayList<ClosedCurve>();
		cc.add(new ClosedCurve(minlen));
	}
	
	public Outline(Outline obj){
		this.cc = new ArrayList<ClosedCurve>(obj.cc);
		this.minlen = obj.getMinlen();
	}
	
	public boolean add(Coordinate coord){
		return cc.get(cc.size()-1).add(coord);
	}

	// return true when new ClosedCurve is added.
	// it is only when current ClosedCurve is closed.
	public boolean newClosedCurve(){
		if(isLastCurveClosed()){
			cc.add(new ClosedCurve(this.minlen));
			return true;
		}else{
			return false;
		}
	}

	// Methods similar to ArrayList Container
	
	public Coordinate get(int closedCurveIdx, int coodIdx){
		return cc.get(closedCurveIdx).get(coodIdx);
	}
	
	public int closedCurveNum(){
		return cc.size();
	}
	
	public int coodNum(int closedCurveIdx){
		return cc.get(closedCurveIdx).size();
	}
	
	public void clear(){
		cc.clear();
		cc.add(new ClosedCurve(minlen));
	}
	
	
	// is**
	
	public boolean isLastCurveClosed(){
		return cc.get(cc.size()-1).isClosed();
	}
	
	// getter
	
	public float getMinlen() {
		return minlen;
	}

	public ArrayList<Coordinate> getVertices(){
		int closedCount;
		if(cc.get(cc.size()-1).isClosed()){
			closedCount = cc.size();
		}else{
			closedCount = cc.size()-1;
		}
		ArrayList<Coordinate> tmp = new ArrayList<Coordinate>();
		for(int i=0; i<closedCount; i++){
			tmp.addAll(cc.get(i).getVertices());
		}
		return tmp;
	}

	public ArrayList<LinearRing> getLinearRings(){
		int closedCount;
		if(cc.get(cc.size()-1).isClosed()){
			closedCount = cc.size();
		}else{
			closedCount = cc.size()-1;
		}
		ArrayList<LinearRing> tmp = new ArrayList<LinearRing>();
		for(int i=0; i<closedCount; i++){
			tmp.add(cc.get(i).getLinearRing());
		}
		return tmp;
	}

}
