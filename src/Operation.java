public class Operation {
	OperationType t;
	Point nextPoint;
	final double eps = 4;

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
			System.out.println("drone: " + p.toString() + ", expected: " + nextPoint.toString());
			if (p.orient > nextPoint.orient + eps/2 || p.orient < nextPoint.orient -eps/2) {
				return true;
			}
			return false;
		}
		return false;
	}
	@Override
	public String toString() {
		return "(" + t + ", " + nextPoint.toString() + ")";
	}

}