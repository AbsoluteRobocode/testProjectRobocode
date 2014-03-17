package CERI;
import robocode.*;
import java.awt.Color;
import java.io.IOException;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * CERI_RoboCode_Project_2014 - a robot by (your name here)
 */
public class CERI_RoboCode_Project_2014 extends TeamRobot
{
	//Si le robot est leader
	private boolean leader = false;
	//La cible que doivent avoir tout les robots
	private String target;
	//Les points de vie perdus sous les coups adverses
	private double totalDamageTakenByBullet = 0;
	//Le comportement qu'adoptera le robot au niveau de ses déplacements
	private movementBehavior myMovementBehavior;
	
	//Les variables static sont partagées entre tout les robots et sont conservées d'un round sur l'autre
	//Nombre de fois où il a semblé que l'appat n'ai servit à rien
	private static int nbBaitFailed = 0;
	//Nombre de fois où il a semblé qu'un autre robot aurait dû être l'appat
	private static int nbWrongBaiter = 0;
	//Robot le plus disposé à devenir un appat
	private static String bestBaiter = null;
	
	//Certaines variables statiques doivent donc être réinitialisées à chaque tour par le leader
	//L'identité du leader
	private static String leadersIdentity;
	//Le nombre de tireurs ayant pris plus de dégâts que prévu durant ce tour
	private static int damagedShooter = 0;
	//Indique si la stratégie a fonctionné durant ce tour
	private static boolean stratWorked = true;
	//Nombre d'alliés encore en vie
	private static int teammatesAlive = 5;
	
	
	/**
	 * run: CERI_RoboCode_Project_2014's default behavior
	 */
	public void run() 
	{
		setBodyColor(Color.black);
		setGunColor(Color.darkGray);
		setRadarColor(Color.lightGray);
		setBulletColor(Color.darkGray);
		setScanColor(Color.green);
		
		//Only one robot has 200 energy, the others have 100 (since we do not use any droïd)
		if(getEnergy() == 200)
		{
			leader = true;
			leadersIdentity = getName();
			//We have 4 allies
			String[] allies = getTeammates();
			try 
			{
				//Si on a pas encore découvert de "meilleur appat" on prend un allié au hasard
				if(bestBaiter == null)
					bestBaiter = allies[0];
				broadcastMessage(movementBehavior.shooter);
				if(bestBaiter == getName())
				{
					myMovementBehavior = movementBehavior.shooter;
					sendMessage(bestBaiter, movementBehavior.bait);
				}
				else
					myMovementBehavior = movementBehavior.bait;
			} 
			catch (IOException ignored) 
			{}
		}
		
		//Robot main loop
		while(true)
		{
			if(myMovementBehavior == movementBehavior.shooter)
			{
				shooter_routine();
			}
			else if (myMovementBehavior == movementBehavior.bait)
			{
				bait_routine();
			}
			else
			{
				runaway_routine();
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) 
	{
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) 
	{
		//Si ce n'est pas un tir allié qui nous a touché
		if(stratWorked && ! isTeammate(e.getName()) && myMovementBehavior == movementBehavior.shooter )
		{
			double damageTaken = 4 * e.getPower() + 2 * Math.max( e.getPower()-1, 0);
			totalDamageTakenByBullet += damageTaken;
			
			if(totalDamageTakenByBullet > 30 && totalDamageTakenByBullet - damageTaken < 30)
			{
				damagedShooter ++;
				if(damagedShooter > 3)
				{
					stratWorked = false;
					nbBaitFailed ++;
				}
			}
			
			if(totalDamageTakenByBullet > 50 && totalDamageTakenByBullet - damageTaken < 50)
			{
				stratWorked = false;
				nbWrongBaiter ++;
				myMovementBehavior = movementBehavior.runaway;
				if(teammatesAlive == 5);
					bestBaiter = getName();
			}
		}
	}
	
	/**
	 * onScannedRobot: What to do when you receive a message
	 */
	public void onMessageReceived(MessageEvent e) 
	{
		if (e.getMessage() instanceof movementBehavior) 
		{
			myMovementBehavior = (movementBehavior) e.getMessage();
			System.out.println("Je suis un " + myMovementBehavior);
		}
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) 
	{
	}
	
	/*
	 * fonction qui contiendra la routine d'un tank shooter afin de rendre plus propre
	 * le code et plus facil à maintenir par les responsables de cette partie
	 * cette routine sera appelée dans la boucle de la fonction run
	 */
	public void shooter_routine() {
		//code à remplacer par le vrai
		ahead(100);
		back(100);
	}
	
	/*
	 * fonction qui contiendra la routine d'un tank bait afin de rendre plus propre
	 * le code et plus facil à maintenir par les responsables de cette partie
	 * cette routine sera appelée dans la boucle de la fonction run
	 */
	public void bait_routine() {
		//code à remplacer par le vrai
		setTurnRight(10000);
		setMaxVelocity(5);
		ahead(10000);
	}
	
	/*
	 * fonction qui contiendra la routine d'un tank runaway afin de rendre plus propre
	 * le code et plus facil à maintenir par les responsables de cette partie
	 * cette routine sera appelée dans la boucle de la fonction run
	 */
	public void runaway_routine() {
		//code à remplacer par le vrai
		ahead(100);
		back(100);
	}
}
