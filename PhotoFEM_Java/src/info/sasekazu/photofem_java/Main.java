package info.sasekazu.photofem_java;

import info.sasekazu.photofem_java.StateManager.State;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Coordinate;


public class Main extends JFrame {

	private static final long serialVersionUID = -1623952337148823353L;
	StateManager stateManager;
	private Outline outline = new Outline(30);
	private FEM fem;

	// UI components
	JButton resetButton;
	JButton meshButton;
	JButton dispButton;
	JLabel message;
	WorldView wv;
	
	public Main(){
		this.setSize(1000,1000);
		this.setTitle("FEM");
		

		// Reset button
		resetButton = new JButton();
		resetButton.setText("Reset");
		resetButton.addActionListener(new resetActionAdapter());
		
		// Mesh button
		meshButton = new JButton();
		meshButton.setText("Mesh");
		meshButton.addActionListener(new meshActionAdapter());
		
		// Disp button
		dispButton = new JButton();
		dispButton.setText("Disp");
		dispButton.addActionListener(new dispActionAdapter());
		
		// Message box
		message = new JLabel();
		message.setText("Message Box");
		
		// world view
		wv = new WorldView();
		wv.setBackground(Color.white);
		wv.setOutline(outline);
		wv.addMouseListener(new MyMouseAdapter());
		wv.addMouseMotionListener(new MyMouseMotionAdapter());
		
		// Initialize StateManager
		stateManager = new StateManager(message);
		wv.setStateManager(stateManager);
		
		setLayout();

		this.setVisible(true);
	}
	
	// layout
	void setLayout(){
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		// horizontal
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		hGroup.addGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(resetButton)
						.addComponent(meshButton)
						.addComponent(dispButton)
						)
				.addComponent(message)
				.addComponent(wv));
		layout.setHorizontalGroup(hGroup);
		
		// vertical
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup()
				.addComponent(resetButton)
				.addComponent(meshButton)
				.addComponent(dispButton)
				)
				.addComponent(message)
				.addComponent(wv);
		layout.setVerticalGroup(vGroup);
		
		this.add(panel);
	}
	
	// events
	
	class MyMouseAdapter extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e) {
			outline.add(new Coordinate(e.getX(), e.getY()));
			wv.repaint();
			super.mouseClicked(e);
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			outline.newClosedCurve();		}
	}
	
	class MyMouseMotionAdapter extends MouseMotionAdapter{
		@Override
		public void mouseDragged(MouseEvent e) {
			outline.add(new Coordinate(e.getX(), e.getY()));
			wv.repaint();
			super.mouseDragged(e);
		}
		
	}
	
	class resetActionAdapter implements ActionListener{
		public void actionPerformed(ActionEvent e){
			stateManager.setState(State.DRAW_OUTLINE);
			wv.reset();
		}
	}

	class meshActionAdapter implements ActionListener{
		public void actionPerformed(ActionEvent e){
			stateManager.setState(State.GENERATE_MESH);
			TriangleMeshBuilder meshBuilder = new TriangleMeshBuilder((float)(wv.getOutline().getMinlen()*1.3));
			meshBuilder.buildMesh(outline);
			wv.setVertices(meshBuilder.getVertices());
			wv.setIndices(meshBuilder.getIndices());
			stateManager.setState(State.CALC_PHYSICS);
			wv.reflesh();
			// make fem
			fem = new FEM(meshBuilder.getVertices(), meshBuilder.getIndices(), 1000.0, 0.4, 1.0, 0.01);
			fem.setBoundary(0);
			fem.calcDeformation();
			wv.setVertices(fem.getPos());
			wv.reflesh();
		}
	}
	
	class dispActionAdapter implements ActionListener{
		float disp = 0;
		public void actionPerformed(ActionEvent e){
			// make fem
			disp += 1;
			System.out.println("disp " + disp);
			fem.setBoundary(disp);
			fem.calcDeformation();
			wv.setVertices(fem.getPos());
			wv.reflesh();
		}
	}
	
	public static void main(String[] args) {
		new Main();
	}
	
	
}
