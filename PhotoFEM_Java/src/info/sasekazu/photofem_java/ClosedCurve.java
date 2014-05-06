package info.sasekazu.photofem_java;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.math.Vector2D;

public class ClosedCurve {

	private float minlen;
	private LinearRing linearRing;
	private ArrayList<Coordinate> coords;
	private boolean closedFlag = false;

	public ClosedCurve(float minlen) {
		this.minlen = minlen;
		coords = new ArrayList<Coordinate>();
	}

	// Return 'true' when new entry has been added to the member 'coords'
	public boolean add(Coordinate coord){
		
		// First entry is added without any check
		if(coords.size()<1){
			coords.add(coord);
			return true;
		}
		
		// Return immediately
		// when it is closed
		if(closedFlag){
			return false;
		}
		
		// Add new coord
		// when the distance between last coord and new coord is more than 'minlen'
		float distFromEnd = (float)coords.get(coords.size()-1).distance(coord);	// 'distFromEnd' is used later too
		if(distFromEnd>minlen){
			
			// Divide line segment,
			// when new line is too long
			float divThreshold = minlen*2;
			if(distFromEnd>divThreshold){
				int div = (int)(distFromEnd/minlen);
				Vector2D last = new Vector2D(coord);
				Vector2D prev = new Vector2D(coords.get(coords.size()-1));
				Vector2D rel = last.add(prev.negate());
				Vector2D divRel = rel.divide((double)div);
				Vector2D newCod;
				// Add interpolative coordinates one by one
				for(int i=0; i<div; i++){
					newCod = prev.add(divRel.multiply((double)i));
					coords.add(newCod.toCoordinate());
				}
			}
			// just add new coordinate
			// when new line is less than threshold
			else{
				coords.add(coord);
			}
			
			// The curve is determined as closed
			// when new coord is near start coord
			if(coords.size()>2){
				float distFromStart = (float)coords.get(0).distance(coord);
				float connectThreshold = minlen*2;
				if(distFromStart<connectThreshold){
					closedFlag = true;
					coords.add(coords.get(0));
					CoordinateArraySequence cas = new CoordinateArraySequence(coords.toArray(new Coordinate[coords.size()]));
					linearRing = new GeometryFactory().createLinearRing(cas);
				}
			}
			return true;
		}
		// Nothing to do
		// when the new coordinate is too near the last one
		else{
			return false;
		}
	}

	
	// Methods similar to ArrayList Container
	
	public Coordinate get(int index){
		return coords.get(index);
	}
	
	public int size(){
		return coords.size();
	}
	
	public void clear(){
		closedFlag = false;
		coords.clear();
	}

	// is**
	
	public boolean isClosed(){
		return closedFlag;
	}

	// getter
	
	public ArrayList<Coordinate> getVertices(){
		return new ArrayList<Coordinate>(coords);
	}
	
	public LinearRing getLinearRing(){
		return linearRing;
	}
	
}
