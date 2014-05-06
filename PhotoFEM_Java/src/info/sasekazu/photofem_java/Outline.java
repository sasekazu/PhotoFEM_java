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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;

public class Outline {

	private ArrayList<ClosedCurve> cc;	
	private float minlen;	// Minimum distance between each vertex
	private Geometry polygon;
	
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
			// check intersection against others
			for(int i=0; i<cc.size()-1; i++){
				if(cc.get(i).getLinearRing().intersects(cc.get(cc.size()-1).getLinearRing())){
					cc.remove(cc.size()-1);
					break;
				}
			}
			// check self intersection
			if(!cc.get(cc.size()-1).getLinearRing().isRing()){
				cc.remove(cc.size()-1);
			}
			
			cc.add(new ClosedCurve(this.minlen));
			
			// update polygon
			polygon = new GeometryFactory().createPolygon(cc.get(0).getLinearRing());
			if(cc.size()>1){
				Geometry plTar;
				for(int i=1; i<cc.size()-1; i++){
					plTar = new GeometryFactory().createPolygon(cc.get(i).getLinearRing());
					polygon = polygon.symDifference(plTar);
				}
			}
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
	
	
	public GeometryCollection getLineGeometries(){
		ArrayList<LineString> linesList = new ArrayList<LineString>();
		for(int i=0; i<cc.size()-1; i++){
			LinearComponentExtracter.getLines(cc.get(i).getLinearRing(), linesList, true);
		}
		return new GeometryFactory().createGeometryCollection(linesList.toArray(new LineString[linesList.size()]));
	}

	public Geometry getPolygon() {
		return polygon;
	}

	// return collection of Point
	// which is within the input outline.
	// Points which the distance between the outline is 
	// under threshold are not included.
	public GeometryCollection getInnerPoints(float dx, float dy){
		double maxx = polygon.getEnvelopeInternal().getMaxX();
		double minx = polygon.getEnvelopeInternal().getMinX();
		double maxy = polygon.getEnvelopeInternal().getMaxY();
		double miny = polygon.getEnvelopeInternal().getMinY();
		
		ArrayList<Point> ptList = new ArrayList<Point>();
		int divx = (int)((maxx-minx)/dx)+2;
		int divy = (int)((maxy-miny)/dy)+2;
		Point pt;
		double threshold = minlen;
		boolean tooNearFlag;
		for(int i=0; i<divx; i++){
			for(int j=0; j<divy; j++){
				// generate grid points from bounding box
				pt = new GeometryFactory().createPoint(new Coordinate(minx+dx*i, miny+dy*j));
				// check whether the point is within the outline
				if(!polygon.contains(pt)){
					continue;
				}
				// check distance
				tooNearFlag = false;
				for(int k=0; k<cc.size()-1; k++){
					if(cc.get(k).getLinearRing().distance(pt)<threshold){
						tooNearFlag = true;
						break;
					}
				}
				// add point which passes all conditions
				if(!tooNearFlag){
					ptList.add(pt);
				}
			}
		}
		return new GeometryFactory().createGeometryCollection(ptList.toArray(new Point[ptList.size()]));
	}
	

}
