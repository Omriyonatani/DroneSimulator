import java.text.DecimalFormat;

public class Operation {
	OperationType t;
	Point nextPoint;
	final double eps = 0.01;

	public Operation(OperationType t, Point target) {
		this.t = t;
		nextPoint = target;
	}

	public boolean isFinished(Point p) {
		if (t == OperationType.flying) {
			if ((Math.abs(p.x - nextPoint.x) <= eps) && (Math.abs(p.y - nextPoint.y) <= eps)) {
				return true;
			}
			return false;
		} else if (t == OperationType.rotating) {
			if (Math.abs(p.orient - nextPoint.orient) <= eps) {
				return true;
			}
			return false;
		}
		return false;
	}

}