import java.awt.*;

import javax.swing.JComponent;


public class Painter extends JComponent{
	AutoDrone algo;
	
	public Painter(AutoDrone algo) {
		this.algo = algo;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		algo.paint(g);
	}
}
