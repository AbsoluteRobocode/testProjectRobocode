package CERI;

//Classe qui sert � stocker les donn�es concernant un enemi
public class RobotData implements java.io.Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Nom du robot
	private String name;
	
	//PV restants
	private Double energie;
	
	//Position du robot (ascisse / ordonn�e)
	private Point position;
	
	//Direction (en degr�s) dans laquelle le robot va
	private Double direction;
	
	//Vitesse du robot
	private Double vitesse;
	
	//Indique si le robot est traqu� en permanence par l'un des notres
	private String trackedBy = null;
	
	//Indique la derni�re fois que le robot a �t� scann� (voir Robot.GetTime())
	private Long lastScan;
	
	//Indique si le robot est un alli�
	private boolean isAlly = false;
	
	//Indique si il semble que l'alli� est une cible importante pour l'ennemi ou non. Plus la valeur est elev�, plus plus le robot est cibl�
	private int targeted = 0;
	
	public RobotData(String name, double energie, Point position, double direction, double vitesse, long time)
	{
		this.name = name;
		this.energie = energie;
		this.position = position;
		this.direction = direction;
		this.vitesse = vitesse;
		this.lastScan = time;
	}
	
	public void update(RobotData updatedData)
	{
		energie = updatedData.energie;
		position = updatedData.getPosition();
		direction = updatedData.direction;
		vitesse = updatedData.vitesse;
		lastScan = updatedData.lastScan;
		if(updatedData.trackedBy != null)
			setTrackedByIfNull(updatedData.trackedBy);
	}
	
	public void setTrackedByIfNull(String tracker)
	{
		if(trackedBy == null)
			trackedBy = tracker;
	}
	
	public void reset()
	{
		energie = null;
		position = null;
		direction = null;
		vitesse = null;
		trackedBy = null;
		lastScan = null;
	}
	
	public void setIsAlly(boolean value)
	{
		isAlly = value;
	}
	
	public boolean getIsAlly()
	{
		return isAlly;
	}
	
	public void setTargeted(int value)
	{
		targeted = value;
	}
	
	public int getTargeted()
	{
		return targeted;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getTrackedBy()
	{
		return trackedBy;
	}
	
	public void resetTrackedBy()
	{
		trackedBy = null;
	}
	
	public double getEnergie()
	{
		return energie;
	}

	public Point getPosition() 
	{
		return position;
	}
	
	public double getDirection()
	{
		return direction;
	}
	
	public double getVitesse()
	{
		return vitesse;
	}
	
	public long getLastScan()
	{
		return lastScan;
	}
}
	