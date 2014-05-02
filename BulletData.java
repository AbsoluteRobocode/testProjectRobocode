package CERI;

import robocode.Bullet;

public class BulletData implements java.io.Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Bullet myBullet;
	private RobotData cible;
	//ATTENTION : ceci n'est pas l'angle dans lequel la balle a été tiré. C'est l'angle dans lequel était l'adversaire par rapport au robot
	private double angleInitial;
	//De même, c'est la distance qui sépare la cible du tireur. Pas celle qui separe la bullet de sa cible
	private double distanceInitiale;
	private long turnWithoutInfo;
	
	public BulletData(Bullet myBullet, RobotData cible, double angleInitial, double distanceInitiale, long turnWithoutInfo)
	{
		this.myBullet = myBullet;
		this.cible = cible;
		this.angleInitial = angleInitial;
		this.distanceInitiale = distanceInitiale;
		this.setTurnWithoutInfo(turnWithoutInfo);
	}
	
	public Bullet getMyBullet() 
	{
		return myBullet;
	}
	
	public RobotData getCible() 
	{
		return cible;
	}
	
	public double getAngleInitial() 
	{
		return angleInitial;
	}
	
	public double getDistanceInitiale()
	{
		return distanceInitiale;
	}

	public long getTurnWithoutInfo() {
		return turnWithoutInfo;
	}

	public void setTurnWithoutInfo(long turnWithoutInfo) {
		this.turnWithoutInfo = turnWithoutInfo;
	}
}
