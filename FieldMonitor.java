package CERI;

import java.util.ArrayList;

//Enregistre toutes les données disponibles surle match actuel
//Toute les donnée / méthodes de cette classe sont statiques afin de conserer en mémoire les noms des ennemis
public class FieldMonitor implements java.io.Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Liste des ennemis sur lesquels nous avons des infos
	private ArrayList<RobotData> robots;
	
	//Nécessaire pour ne pas écraser les noms des ennemis enregistrés
	private boolean isFirstRound = true;
	
	//(Ré)initialise les données en vue du prochain round
	public void newRound()
	{
		if(isFirstRound)
		{
			robots = new ArrayList<RobotData>();
			isFirstRound = false;
			return;
		}
		for(int i=0; i<getRobots().size(); i++)
			getRobots().get(i).reset();
	}
	
	//Met à jour (ou créé) les données sur le robot passé en paramètre. Renvoie la position du robot dans la liste "ennemies"
	public int updateData(RobotData bot)
	{
		String name = bot.getName();
		for(int i=0; i<getRobots().size(); i++)
		{
			if(getRobots().get(i).getName().equals(name))
			{
				getRobots().get(i).update(bot);
				return i;
			}
		}
		getRobots().add(bot);
		return getRobots().size()-1;
	}
	
	public Integer getRobotDataByName(String name)
	{
		for(int i=0; i<getRobots().size(); i++)
		{
			if(getRobots().get(i).getName().equals(name))
			{
				return i;
			}
		}
		return null;
	}
	
	public ArrayList<Integer> getRobotDataByTracker(String name)
	{
		ArrayList<Integer> returnValue = new ArrayList<Integer>();
		for(int i=0; i<getRobots().size(); i++)
		{
			if(getRobots().get(i).getTrackedBy() != null && getRobots().get(i).getTrackedBy().equals(name))
			{
				returnValue.add(i);
			}
		}
		return returnValue;
	}
	
	public String lookForNewLeader()
	{
		Double maxHP = null;
		int bestLeader = 0;
		for(int i=0; i<getRobots().size(); i++)
		{
			//Le meilleur leader est l'allié ayant le plus de vie
			if(getRobots().get(i).getIsAlly() && (maxHP == null || maxHP < getRobots().get(i).getEnergie()))
			{
				maxHP = getRobots().get(i).getEnergie();
				bestLeader = i;
			}
		}
		return getRobots().get(bestLeader).getName();
	}
	
	public String lookForNewBaiter()
	{
		int priorityValue = 0;
		String baiter = null;
		for(int i=0; i<getRobots().size(); i++)
		{
			if(getRobots().get(i).getIsAlly() && getRobots().get(i).getTargeted()>=priorityValue && getRobots().get(i).getEnergie() > 30)
			{
				priorityValue = getRobots().get(i).getTargeted();
				baiter = getRobots().get(i).getName();
			}
		}
		return baiter;
	}
	
	public ArrayList<String> findAlliesAlive()
	{
		ArrayList<String> alliesAlive = new ArrayList<String>();
		for(int i=0; i<getRobots().size(); i++)
		{
			if(getRobots().get(i).getIsAlly() && getRobots().get(i).getEnergie() > 0)
			{
				alliesAlive.add(getRobots().get(i).getName());
			}
		}
		return alliesAlive;
	}

	public ArrayList<RobotData> getRobots() 
	{
		return robots;
	}
}