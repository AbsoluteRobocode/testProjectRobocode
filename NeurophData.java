package CERI;

public class NeurophData implements java.io.Serializable 
{
	private static final long serialVersionUID = 1L;
	private double distance;
	private double angle;
	private double headingEnemy;
	private double enemyVelocity;
	private long turn;
	private double bulletHeading;
	private double bulletPower;
	private boolean touche;
	
	public NeurophData(double distance, double angle, double headingEnemy, double enemyVelocity, long turn, double bulletHeading, double bulletPower, boolean touche)
	{
		this.distance = distance;
		this.angle = angle;
		this.headingEnemy = headingEnemy;
		this.enemyVelocity = enemyVelocity;
		this.turn = turn;
		this.bulletHeading = bulletHeading;
		this.bulletPower = bulletPower;
		this.touche = touche;
	}
	
	public double getDistance() 
	{
		return distance;
	}
	public void setDistance(double distance) 
	{
		this.distance = distance;
	}
	public double getAngle() 
	{
		return angle;
	}
	public void setAngle(double angle) 
	{
		this.angle = angle;
	}
	public double getHeadingEnemy() 
	{
		return headingEnemy;
	}
	public void setHeadingEnemy(double headingEnemy) 
	{
		this.headingEnemy = headingEnemy;
	}
	public double getEnemyVelocity() 
	{
		return enemyVelocity;
	}
	public void setEnemyVelocity(double enemyVelocity) 
	{
		this.enemyVelocity = enemyVelocity;
	}
	public long getTurn() 
	{
		return turn;
	}
	public void setTurn(long turn) 
	{
		this.turn = turn;
	}

	public double getBulletHeading() {
		return bulletHeading;
	}

	public void setBulletHeading(double bulletHeading) {
		this.bulletHeading = bulletHeading;
	}

	public double getBulletPower() {
		return bulletPower;
	}

	public void setBulletPower(double bulletPower) {
		this.bulletPower = bulletPower;
	}

	public boolean isTouche() {
		return touche;
	}

	public void setTouche(boolean touche) {
		this.touche = touche;
	}
}
