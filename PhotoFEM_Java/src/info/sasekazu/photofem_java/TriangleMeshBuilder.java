package info.sasekazu.photofem_java;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.ConformingDelaunayTriangulationBuilder;

public class TriangleMeshBuilder {

	private float minlen;
	private float[][] vtx;
	private int[][] idx;
	
	public TriangleMeshBuilder(float minlen) {
		this.minlen = minlen;
	}


	public void buildMesh(Outline outline) {
		
		ConformingDelaunayTriangulationBuilder cdtb = new ConformingDelaunayTriangulationBuilder();
		// get constraint line (edges of the outline)
		GeometryCollection lines = outline.getLineGeometries();
		cdtb.setConstraints(lines);
		// get inner points of the outline
		GeometryCollection sites = outline.getInnerPoints(minlen, minlen);
		cdtb.setSites(sites);
		// generate mesh
		MultiLineString mls = (MultiLineString)cdtb.getEdges(new GeometryFactory());
		// get vtx
		Coordinate[] coords = mls.getCoordinates();
		vtx = new float[coords.length][2];
		for(int i=0; i<coords.length; i++){
			vtx[i][0] = (float)coords[i].x;
			vtx[i][1] = (float)coords[i].y;
		}
		
		// get idx
		GeometryCollection tris = (GeometryCollection)cdtb.getTriangles(new GeometryFactory());
		Polygon tri;
		ArrayList<int[]> idxList = new ArrayList<int[]>();
		int idxs[];
		for(int i=0; i<tris.getNumGeometries(); i++){
			tri = (Polygon)tris.getGeometryN(i);
			// check if the triangle is within the outline
			if(outline.getPolygon().contains(tri.getCentroid())){
				idxs = new int[]{0,0,0};
				// get vertex number
				for(int j=0; j<3; j++){
					for(int k=0; k<coords.length; k++){
						if(coords[k].distance(tri.getCoordinates()[j]) < 0.1*minlen){
							idxs[j] = k;
							break;
						}
					}
				}
				idxList.add(idxs);
			}
		}
		idx = idxList.toArray(new int[idxList.size()][3]);
		
	}

	// getter
	
	public float[][] getVertices() {
		return vtx;
	}

	public int[][] getIndices() {
		return idx;
	}
	

}
