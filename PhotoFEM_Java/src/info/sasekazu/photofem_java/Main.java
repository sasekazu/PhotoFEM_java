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
	private Outline outline = new Outline(10);

	// UI components
	JButton resetButton;
	JButton meshButton;
	JLabel message;
	WorldView wv;
	
	public Main(){
		this.setSize(700,600);
		this.setTitle("FEM");
		

		// Reset button
		resetButton = new JButton();
		resetButton.setText("Reset");
		resetButton.addActionListener(new resetActionAdapter());
		
		// Mesh button
		meshButton = new JButton();
		meshButton.setText("Mesh");
		meshButton.addActionListener(new meshActionAdapter());
		
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
						)
				.addComponent(message)
				.addComponent(wv));
		layout.setHorizontalGroup(hGroup);
		
		// vertical
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup()
				.addComponent(resetButton)
				.addComponent(meshButton)
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
		}
	}
	
	public static void main(String[] args) {
		new Main();
	}
	
	
}
