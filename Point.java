package CERI;

/**
 * Point - a serializable point class
 */
public class Point implements java.io.Serializable 
{

	private static final long serialVersionUID = 1L;

	private double x = 0.0;
	private double y = 0.0;

	public Point(double x, double y) 
	{
		this.x = x;
		this.y = y;
	}
	
	//Une autre façon de créer un point : à partir d'un Point, d'une distance et d'un angle 
	public Point(double d, double b, Point p)
	{
		x = p.x + Math.sin(b)*d;
		y = p.y + Math.cos(b)*d;
	}

	public double getX() 
	{
		return x;
	}

	public double getY() 
	{
		return y;
	}
	
	public static double getDistance(Point p1, Point p2)
	{
		double pythagoreX = p1.x-p2.x;
		double pythagoreY = p1.y-p2.y;
		double distance = Math.sqrt(pythagoreX*pythagoreX + pythagoreY*pythagoreY);
		return distance;
	}
	
	public static double getAngle(Point p1, Point p2)
	{
		
		return 1;
	}
}