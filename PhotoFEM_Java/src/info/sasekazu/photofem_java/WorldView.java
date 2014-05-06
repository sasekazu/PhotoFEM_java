package info.sasekazu.photofem_java;

import info.sasekazu.photofem_java.StateManager.State;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Coordinate;

public class WorldView extends JPanel {
	
	private static final long serialVersionUID = -7812014352055093448L;
	GeneralPath path = new GeneralPath();
	private boolean stateInitFlag = false; 
	private StateManager stateManager;
	private Outline outline;
	
	private float[][] vtx;
	private int[][] idx;

	
	public WorldView(){
		
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		
		if(!stateInitFlag){
			return;
		}
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		State state = stateManager.getState();
		// DRAW_OUTLINE
		if(state == StateManager.State.DRAW_OUTLINE){
			// Draw vertices
			for(int i=0; i<outline.closedCurveNum(); i++){
				int cr = 3;	int cR = 2*cr;
				for(int j=0; j<outline.coodNum(i); j++){
					g2.fillOval((int)outline.get(i, j).x-cr,  (int)outline.get(i, j).y-cr, cR, cR);
				}
			}
			// Draw lines
			for(int i=0; i<outline.closedCurveNum(); i++){
				path.reset();
				if(outline.coodNum(i)>0){
					path.moveTo((float)outline.get(i, 0).x, (float)outline.get(i, 0).y);
					for(int j=1; j<outline.coodNum(i); j++){
						path.lineTo((float)outline.get(i, j).x, (float)outline.get(i, j).y);
					}
				}
				if(i==outline.closedCurveNum()-1){
					g2.setStroke(new BasicStroke(1.0f));
				}else{
					g2.setStroke(new BasicStroke(3.0f));
				}
			    g2.draw(path);
			}
		}
		// GENERATE_MESH
		else if(state == StateManager.State.GENERATE_MESH){
		}
		// CALC_PHYSICS
		else if(state == StateManager.State.CALC_PHYSICS){
			
			// Draw vertices
			int vertnum = vtx.length;
			int cr = 3; int cR = 2*cr;
			for(int i=0; i<vertnum; i++){
				g2.fillOval((int)vtx[i][0]-cr,  (int)vtx[i][1]-cr, cR, cR);
			}

			// Draw triangles
			path.reset();
			for(int i=0; i<idx.length; i++){
				path.moveTo(vtx[idx[i][0]][0], vtx[idx[i][0]][1]);
				path.lineTo(vtx[idx[i][1]][0], vtx[idx[i][1]][1]);
				path.lineTo(vtx[idx[i][2]][0], vtx[idx[i][2]][1]);
				path.lineTo(vtx[idx[i][0]][0], vtx[idx[i][0]][1]);
			}
		    g2.draw(path);
			
		}
		
	}


	public void reset(){
		outline.clear();
		repaint();
	}

	public void reflesh(){
		repaint();
	}

	// getter
	
	public ArrayList<Coordinate> getVertices(){
		return outline.getVertices();
	}
	
	public Outline getOutline(){
		return new Outline(outline);
	}
	
	// setter
	
	public void setStateManager(StateManager stateManager){
		this.stateManager = stateManager;
		this.stateManager.getState();
		this.stateInitFlag = true;	// now onDraw() goes actual draw phase
	}
	
	public void setVertices(float vert[][]){
		vtx = vert.clone();
	}
	
	public void setIndices(int indices[][]){
		this.idx = indices.clone();
	}
	
	public void setOutline(Outline outline) {
		this.outline = outline;
	}


}
