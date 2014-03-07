package CERI;

import java.awt.*;

/**
 * RobotColors - A serializable class to send movement behavior expected from teammates.
 *
 */
public enum movementBehavior implements java.io.Serializable 
{
	bait("appat"),
	shooter("tireur"),
	runaway("fuyard");
   
	private String name = "";
   
	movementBehavior(String name)
	{
		this.name = name;
	}
   
	public String toString()
	{
		return name;
	}
}
