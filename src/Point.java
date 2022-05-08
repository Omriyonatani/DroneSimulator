import java.text.DecimalFormat;

public class Point {
	public double x;
	public double y;
	public double orient;


	public Point(double x,double y, double orient) {
		this.x = x;
		this.y = y;
		this.orient = orient;
	}


	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
		this.orient = p.orient;
	}

	public Point() {
		x = 0;
		y = 0;
		orient = 0;
	}


	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.###");
		
		return "(" + df.format(x) + "," + df.format(y) + "," + df.format(orient) + ")";
	}

}
