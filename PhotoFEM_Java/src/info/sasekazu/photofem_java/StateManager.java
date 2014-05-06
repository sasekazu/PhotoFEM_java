package info.sasekazu.photofem_java;

import javax.swing.JLabel;


public class StateManager {

	public enum State {DRAW_OUTLINE, GENERATE_MESH, CALC_PHYSICS};
	private State state;
	private JLabel tv;
	private final static String MESSAGE_DRAW_OUTLINE = "DRAW_OUTLINE";
	private final static String MESSAGE_GENERATE_MESH= "GENERATE_MESH";
	private final static String MESSAGE_CALC_PHYSICS = "CALC_PHYSICS";
	
	public StateManager(JLabel textview) {
		tv = textview;
		state = State.DRAW_OUTLINE;
	}

	public State getState() {
		return state;
	}

	// set state and update message text
	public void setState(State state) {
		this.state = state;
		switch(state){
			case DRAW_OUTLINE:
				tv.setText(MESSAGE_DRAW_OUTLINE);
				break;
			case GENERATE_MESH:
				tv.setText(MESSAGE_GENERATE_MESH);
				break;
			case CALC_PHYSICS:
				tv.setText(MESSAGE_CALC_PHYSICS);
				break;
			default:
				break;
		}
	}
}
