package CERI;

/**
 * RobotColors - A serializable class to send movement behavior expected from teammates.
 *
 */
public enum radarBehavior implements java.io.Serializable 
{
	swipe("balayage"),
	track("traqueur"),
	outnumbered("unPourDeux");
   
	private String name = "";
   
	radarBehavior(String name)
	{
		this.name = name;
	}
   
	public String toString()
	{
		return name;
	}
}