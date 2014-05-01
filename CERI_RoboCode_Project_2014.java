package CERI;
import robocode.*;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;


// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * CERI_RoboCode_Project_2014 - a robot by (your name here)
 */
public class CERI_RoboCode_Project_2014 extends TeamRobot
{
	//Les points de vie perdus sous les coups adverses
	private double totalDamageTakenByBullet = 0;
	//Le comportement qu'adoptera le robot au niveau de ses déplacements
	private movementBehavior myMovementBehavior;
	//Le comportement qu'adoptera le robot au niveau de sa façon d'utiliser son radar
	private radarBehavior myRadarBehavior = radarBehavior.track;
	//L'identité du leader. Elle n'est pas static car les robots mort doivent à nouveau apprendre qui est le leader
	private String leadersIdentity = null;
	//Identité du bot que nous traquons
	private String tracked = null;
	//Identité du deuxieme bot que nous traquons si on est en mode "outnumbered" sur le radar
	private String tracked2 = null;
	
	//Les variables static sont conservées d'un round sur l'autre
	// Indique si le robot est leader
	private static boolean leader = false;
	/* Cette donnée est utile pour le leader uniquement. C'est un seul objet encapsulant toutes les données utiles pour le leader
	 * afin qu'il soit plus facile de les transmettre d'un leader à un autre à la mort du leader.
	 * Donnée comprises : 
	 * - variable en rapport avec la stratégie utilisée
	 * - toutes les infos disponibles sur tout les adversaires
	 * - position et vie des alliés
	 */
	private static LeaderData myLeaderData;
	
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
		
		//Au début, on balaye en attendant d'avoir trouvé un ennemi à traquer
		myRadarBehavior = radarBehavior.track;
		
		/* Only one robot has 200 energy, the leader, the others have 100 (since we do not use any droïd)
		 * However, it only is the leader on the first round. At the beginning of any other round, the leader
		 * is the one that was leader on the previous round.
		 */
		if(getEnergy() == 200 && getRoundNum()==0)
		{
			//Nous sommes donc dans une partie du code où seul le leader (initial) va
			leader = true;
			myLeaderData = new LeaderData();
		}
		
		if(leader == true)
		{
			leadersIdentity = getName();
			//We should have 5 allies and 5 ennemies, but a generic code is always better, isn't it?
			String[] allies = getTeammates();
			//Dans les alliés, on se compte soit même
			myLeaderData.setTeammatesAlive(allies.length + 1);
			//Les ennemis sont tout les autres robots
			myLeaderData.setEnnemiesAlive(getOthers() - (myLeaderData.getTeammatesAlive()-1));
			
			//On initialise le field monitor
			myLeaderData.newRound();
			
			
			//On va renseigner à tout nos alliés ce qu'on attend d'eux au niveau des mouvements
			try 
			{
				broadcastMessage(movementBehavior.shooter);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			assignNewBaiter();
		}
		
		//Robot main loop
		while(true)
		{
			//Chaque tour, le leader doit envoyer ses données aux autres robots au cas où il meurt
			if(leader)
			{
				try 
				{
					broadcastMessage(myLeaderData);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
				//On met à jour nos informations personnelles dans le field monitor
				Point maPosition = new Point(this.getWidth(), this.getHeight());
				RobotData freshData = new RobotData(getName(), getEnergy(), maPosition, getHeading(), getVelocity(), getTime());
				freshData.setIsAlly(true);
				myLeaderData.getFm().updateData(freshData);
			}
			else
			{
				//Au tout premier "tour" (!= round), l'identité du leader n'est pas encore connue (le temps qu'on recoive le message nous indiquant son identité)
				if(leadersIdentity != null)
				{
					//Les autres robots doivent nous donner leurs informations personelles pour qu'on les mette dans le fieldMonitor
					Point maPosition = new Point(this.getWidth(), this.getHeight());
					RobotData freshData = new RobotData(getName(), getEnergy(), maPosition, getHeading(), getVelocity(), getTime());
					freshData.setIsAlly(true);
					try 
					{
						sendMessage(leadersIdentity, freshData);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
			//Gestion des mouvements
			if(myMovementBehavior == movementBehavior.shooter)
			{
				shooter_routine();
			}
			else if(myMovementBehavior == movementBehavior.bait)
			{
				bait_routine();
			}
			else
			{
				runaway_routine();
			}
			
			//Gestion radar
			if(myRadarBehavior == radarBehavior.swipe)
			{
				swipe_routine();
			}
			else if(myRadarBehavior == radarBehavior.track)
			{
				track_routine();
			}
			else
			{
				outnumbered_routine();
			}
			
			//Gestion des tirs
			//Si le canon est froid
			if(this.getGunHeat() == 0)
			{
				int index = myLeaderData.acquireTarget();
				RobotData enemy = myLeaderData.getFm().getRobots().get(index);
				ShootInstruction instruction = myLeaderData.useNeuroph(this.getName(), enemy, this.getTime());
				double CANON_MAX_ANGLE = 20;
				
				double tailleAngle = Math.abs(instruction.getGunDirection()-this.getGunHeading());
				//Si on peut atteindre le bon angle ce tour ci
				if(Math.min(360-tailleAngle, tailleAngle) <= CANON_MAX_ANGLE)
				{
					//On tourne le canon dans le bon angle
					if(tailleAngle <= CANON_MAX_ANGLE)
						this.turnGunRight(instruction.getGunDirection() - this.getGunHeading());
					else
					{
						double value = (instruction.getGunDirection() - this.getGunHeading())-360;
						while(Math.abs(value) > CANON_MAX_ANGLE && value < 0)
							value = value + 360;
						if(value > 20)
							System.out.println("ERREUR : le canon ne devrait pas être tourné de plus de 20 degres en un tour.(Erreur dans l'algorithme)");
						this.turnGunRight(value);
					}
					double puissanceTir = instruction.getPuissance();
					Bullet myBullet = this.fireBullet(puissanceTir);
				}
				//Si on ne peut pas atteindre l'angle demandé, on essaye de s'en rapprocher un maximum
				else
				{
					if((tailleAngle < 180 && instruction.getGunDirection()-this.getGunHeading() < 0) || tailleAngle >= 180 && instruction.getGunDirection()-this.getGunHeading() < 0)
					{
						this.turnGunLeft(CANON_MAX_ANGLE);
					}
					else
					{
						this.turnGunLeft(CANON_MAX_ANGLE);
					}
				}
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) 
	{
		if(tracked != null)
		{
			if(e.getName().equals(tracked))
				System.out.println("Trouve1 : "+tracked);
			else
				System.out.println("Cherche1 : "+tracked);
		}
		if(tracked2 != null)
		{
			if(e.getName().equals(tracked2))
				System.out.println("Trouve2 : "+tracked2);
			else
				System.out.println("Cherche2 : "+tracked2);
		}
		
		boolean isFoe = true;
		//Vérifier si le robot scanné est un ennemi
		for (int i=0; i<getTeammates().length; i++)
			if(e.getName().equals(getTeammates()[i]))
				isFoe = false;
		
		if(isFoe)
		{
			//Mise à jour des données sur le robot scanné
			Point maPosition = new Point(this.getWidth(), this.getHeight());
			double d = e.getDistance();
			double b = e.getBearing();
			Point positionEnnemie = new Point(d,b,maPosition);
			
			RobotData freshData = new RobotData(e.getName(), e.getEnergy(), positionEnnemie, e.getHeading(), e.getVelocity(), this.getTime());
			
			//On verifie si on avait deja des infos sur l'ennemi. On veut notamment savoir si l'ennemi est deja tracké
			String followedBy = null;
			for(int i=0; i<myLeaderData.getFm().getRobots().size(); i++)
			{
				if(myLeaderData.getFm().getRobots().get(i).getName().equals(e.getName()))
				{
					followedBy = myLeaderData.getFm().getRobots().get(i).getTrackedBy();
				}
			}
			
			//Si le robot n'est traqué par personne et qu'il nous manquait un robot a traquer, on commence à le traquer
			if(followedBy == null)
			{
				if(myRadarBehavior != radarBehavior.swipe && tracked == null)
				{
					tracked = e.getName();
					freshData.setTrackedByIfNull(getName());
				}
				else if(myRadarBehavior == radarBehavior.outnumbered && tracked2 == null && !e.getName().equals(tracked))
				{
					tracked2 = e.getName();
					freshData.setTrackedByIfNull(getName());
				}
			}
			
			//Si on est en mode outnumbered et qu'on a traqué l'un des robots que l'on a pris pour cible, on doit aller chercher l'autre robot
			if(myRadarBehavior == radarBehavior.outnumbered && ((tracked != null && tracked.equals(e.getName())) || (tracked2!=null && tracked2.equals(e.getName()))))
			{
				if(e.getName().equals(tracked))
				{
					if(!reversed && ((seenTarget1>1 && seenTarget2 < 4 && seenTarget2>0) || seenTarget2 == 0))
					{
						reversed = true;
						swipeDirection *=-1;
					}
					seenTarget1=0;
				}
				else if(e.getName().equals(tracked2))
				{
					if(!reversed && ((seenTarget2>1 && seenTarget1 < 4 && seenTarget1>0) || seenTarget1 == 0))
					{
						reversed = true;
						swipeDirection *=-1;
					}
					seenTarget2=0;
				}
				
				//La cible a pu etre acquise par 2 robots en même temps, on libere la cible si un autre robot l'a prise en même temps (et l'a enregistrée après nous
				if(followedBy!=null && !followedBy.equals(getName()) && (tracked != null && tracked.equals(e.getName())))
				{
					tracked = null;
				}
				else if(followedBy!=null && !followedBy.equals(getName()) && (tracked2 != null && tracked2.equals(e.getName())))
				{
					tracked2 = null;
				}
			}
			
			if(myRadarBehavior == radarBehavior.track && tracked!=null && tracked.equals(e.getName()))
			{
				//On regarde si notre trajectoire de scan vient de depasser par la gauche ou par la droite notre cible
				//par la droite
				double direction;
				if(turnRadarValue > 0)
					direction = -1;
				//par la gauche
				else
					direction = 1;
				//De combien de degres on a depasse la cible du scan
				double surplus = Math.abs(e.getBearing() - (this.getRadarHeading()-this.getHeading()));
				//Marge d'erreur qu'on s'autorise car le robot adverse bouge
				double margeErreur = 10;
				turnRadarValue = Math.min(surplus+margeErreur, 45)*direction;
				
				//La cible a pu etre acquise par 2 robots en même temps, on libere la cible si un autre robot l'a prise en même temps
				if(followedBy!=null && !followedBy.equals(getName()))
				{
					tracked = null;
					turnRadarValue = 45;
				}
			}
							
			//Si c'est le leader qui a scanné, on peut enregistrer immédiatement dans le field monitor
			if(leader)
			{
				int index = myLeaderData.getFm().updateData(freshData);
				//Activation du radar tracking si le robot était en recherche d'ennemi à tracker et que l'ennemi n'était pas tracké
				if(tracked==null && myRadarBehavior == radarBehavior.track)
				{
					myLeaderData.getFm().getRobots().get(index).setTrackedByIfNull(getName());
				}
			}
			//Sinon, il faut envoyer l'information au leader
			else
			{
				try 
				{
					sendMessage(leadersIdentity, freshData);
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * onScannedRobot: What to do when any other Robot dies
	 */
	public void onRobotDeath(RobotDeathEvent e)
	{
		//On enregistre que le robot n'a plus de point de vie
		myLeaderData.getFm().updateData(new RobotData(e.getName(), 0, null, (double)0, (double)0, 0));
		
		boolean isFoe = true;
		//Vérifier si le robot mort est un ennemi
		for (int i=0; i<getTeammates().length; i++)
			if(e.getName().equals(getTeammates()[i]))
				isFoe = false;
		
		//Si c'est un ennemi, on compte un ennemi en moins, si c'est un allié, il se comptera lui même en moins dans onDeath
		if(isFoe)
		{
			//Verifier s'il n'est pas l'ennemi qu'on prennait en chasse
			if(tracked!=null && tracked.equals(e.getName()))
				tracked = null;
			if(tracked2!=null && tracked2.equals(e.getName()))
				tracked2 = null;
			
			//Quoi qu'il en soit, ça fait un ennemi en moins
			myLeaderData.setEnnemiesAlive(myLeaderData.getEnnemiesAlive() - 1);
		}
		else
		{
			myLeaderData.setTeammatesAlive(myLeaderData.getTeammatesAlive() - 1);
			if(e.getName().equals(leadersIdentity))
			{
				leadersIdentity = myLeaderData.getFm().lookForNewLeader();
				System.out.println("Remplacant : "+leadersIdentity);
				if(leadersIdentity.equals(getName()))
				{
					leader = true;
					System.out.println("Je suis le nouveau leader.");
				}
			}
		}
		
		//Ces operations là ne sont mené que par le leader (y compris si le leader vient de mourrir car le nouveau leader a deja été élu)
		if(leader)
		{
			//On rétablis la formation par defaut avant d'opérer des changements dessus : tout le monde en traqueur
			try 
			{
				broadcastMessage(radarBehavior.track);
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
			myRadarBehavior = radarBehavior.track;
			
			//Si on est TRES réduit en effectif par rapport à l'adversaire, tout le monde passe en mode swipe
			if(myLeaderData.getEnnemiesAlive() > 2*myLeaderData.getTeammatesAlive())
			{
				try 
				{
					broadcastMessage(radarBehavior.swipe);
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
				myRadarBehavior = radarBehavior.swipe;
			}
			
			//Si on a un petit désavantage numérique, certains robots doivent passer en radar 1 pour 2 (outnumbered)
			else if(myLeaderData.getEnnemiesAlive() > myLeaderData.getTeammatesAlive())
			{
				int outnumberedBy = myLeaderData.getEnnemiesAlive() - myLeaderData.getTeammatesAlive();
				if(outnumberedBy == myLeaderData.getTeammatesAlive())
				{
					try 
					{
						broadcastMessage(radarBehavior.outnumbered);
					} 
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
					myRadarBehavior = radarBehavior.outnumbered;
				}
				else
				{
					ArrayList<String> allies = myLeaderData.getFm().findAlliesAlive();
					for(int i=0; i<outnumberedBy; i++)
					{
						try 
						{
							sendMessage(allies.get(i), radarBehavior.outnumbered);
						} 
						catch (IOException e1) 
						{
							e1.printStackTrace();
						}
					}
				}
			}
			//On met à jour la priorité accordée à l'allié vaincu
			//Et on met à null le trackedBy des robots trackés par l'allié mort
			if(!isFoe)
			{
				Integer num = myLeaderData.getFm().getRobotDataByName(e.getName());
				if(num != null)
				{
					myLeaderData.getFm().getRobots().get(num).setTargeted(myLeaderData.getTeammatesAlive()+myLeaderData.getFm().getRobots().get(num).getTargeted());
				}
				ArrayList<Integer> indexes = myLeaderData.getFm().getRobotDataByTracker(e.getName());
				for(int i=0; i<indexes.size(); i++)
				{
					myLeaderData.getFm().getRobots().get(indexes.get(i)).resetTrackedBy();
					System.out.println(myLeaderData.getFm().getRobots().get(indexes.get(i)).getName()+" n'est plus suivit");
				}
			}
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) 
	{
		//Si ce n'est pas un tir allié qui nous a touché, qu'on applique une strategie avec un bait etqu'un allié a ete touché
		if(myLeaderData.isStratWorked() && ! isTeammate(e.getName()) && myMovementBehavior == movementBehavior.shooter )
		{
			//On calcul les dégats reçuts
			double damageTaken = 4 * e.getPower() + 2 * Math.max( e.getPower()-1, 0);
			totalDamageTakenByBullet += damageTaken;
			
			//Afin de signaler qu'on commence à recevoir trop de dégat, afin de changer de stratégie si trop de robot signalent ce probleme (appat inutile)
			if(totalDamageTakenByBullet > 30 && totalDamageTakenByBullet - damageTaken < 30)
			{
				myLeaderData.setDamagedShooter(myLeaderData.getDamagedShooter() + 1);
				if(myLeaderData.getDamagedShooter() > 3)
				{
					myLeaderData.setStratWorked(false);
					myLeaderData.setNbBaitFailed(myLeaderData.getNbBaitFailed() + 1);
				}
			}
			
			//Ou signaler qu'on prend personnellement bien trop de dégat (i.e. l'appat est mal choisit)
			if(totalDamageTakenByBullet > 50 && totalDamageTakenByBullet - damageTaken < 50)
			{
				myLeaderData.setStratWorked(false);
				myLeaderData.setNbWrongBaiter(myLeaderData.getNbWrongBaiter() + 1);
				myMovementBehavior = movementBehavior.runaway;
			}
		}
		//Tout tank ayant moins de 30 points de vie passe en mode fuite jusqu'à la fin du combat
		if(getEnergy() < 30)
		{
			//Ceci veut dire que si le tank était un baiter, il doit le signaler au leader
			if(myMovementBehavior == movementBehavior.bait && !leader)
			{
				try 
				{
					sendMessage(leadersIdentity, "Baiter : low energy");
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
			//Si il est lui même le leader, il nomme immediatement un autre robot comme baiter
			else if(myMovementBehavior == movementBehavior.bait && leader)
			{
				assignNewBaiter();
			}
			myMovementBehavior = movementBehavior.runaway;
		}
	}
	
	/**
	 * onScannedRobot: What to do when you receive a message
	 */
	public void onMessageReceived(MessageEvent e) 
	{
		//Si le leader nous demande de changer notre comportement au niveau des mouvements, on s'execute
		if (e.getMessage() instanceof movementBehavior) 
		{
			myMovementBehavior = (movementBehavior) e.getMessage();
			System.out.println("Message recus : changement de mode de deplacement ("+myMovementBehavior.toString()+")");
		}
		//Si le leader nous demande de changer notre comportement au niveau du radar, on s'execute
		if (e.getMessage() instanceof radarBehavior && (radarBehavior) e.getMessage() != myRadarBehavior) 
		{
			myRadarBehavior = (radarBehavior) e.getMessage();
			System.out.println("Message recus : changement de mode du radar ("+myRadarBehavior.toString()+")");
			turnRadarValue = 45;
		}
		//Si on nous envoie des infos sur un robot, on les enregistre dans le field monitor
		if(e.getMessage() instanceof RobotData && leader)
		{
			//System.out.println("Message recus : mise a jour des donnees sur un robot ennemi");
			myLeaderData.getFm().updateData((RobotData) e.getMessage());
		}
		//Chaque tour, tout les robots doivent recevoir les données du leader
		if(e.getMessage() instanceof LeaderData)
		{
			//System.out.println("Message recus : backup des donnees du leader");
			leadersIdentity = e.getSender();
			myLeaderData = (LeaderData) e.getMessage();
		}
		//Si on reçoit un String, on verifie ce qu'il contient
		if(e.getMessage() instanceof String)
		{
			String message = (String) e.getMessage();
			//Si le baiter signale qu'il quitte son post
			if(message.equals("Baiter : low energy"))
			{
				assignNewBaiter();
				System.out.println("Le baiter a pris sa retraite");
			}
		}
	}
	
	/*
	 * fonction qui contiendra la routine d'un tank shooter afin de rendre plus propre
	 * le code et plus facile à maintenir par les responsables de cette partie
	 * cette routine sera appelée dans la boucle de la fonction run
	 */
	public void shooter_routine() {
		//code à remplacer par le vrai
		/*ahead(100);
		back(100); */
	}
	
	/*
	 * fonction qui contiendra la routine d'un tank bait afin de rendre plus propre
	 * le code et plus facile à maintenir par les responsables de cette partie
	 * cette routine sera appelée dans la boucle de la fonction run
	 */
	public void bait_routine() {
		//code à remplacer par le vrai
		/*setTurnRight(10000);
		setMaxVelocity(5);
		ahead(10000);*/
	}
	
	/*
	 * fonction qui contiendra la routine d'un tank runaway afin de rendre plus propre
	 * le code et plus facile à maintenir par les responsables de cette partie
	 * cette routine sera appelée dans la boucle de la fonction run
	 */
	public void runaway_routine() {
		//code à remplacer par le vrai
		/*ahead(100);
		back(100);*/
	}
	
	/*
	 * fonction qui définit le comportement du radar initial : tourner en boucle jusqu'à trouver un
	 * ennemi non tracké, mis aussi le comportement du radar lorsqu'on est vraiment moins nombreux
	 * que les ennemis (ils sont plus de 2 fois plus nombreux que nous)
	 */
	public void swipe_routine()
	{
		//45° est le maximum
		turnRadarRight(45);
	}
	
	/*
	 * fonction qui sert à tracker un ennemi avec le radar, le but étant d'avoir des informations sur
	 * lui en continue (il ne devrait y avoir AUCUN tour où on ne reçoit pas d'informations sur lui)
	 */
	//Variable utile pour track_routine UNIQUEMENT. Calculée au moment où le robot traqué est trouvé.
	private double turnRadarValue = 45;
	public void track_routine()
	{
		turnRadarRight(turnRadarValue);
	}
	
	/*
	 * fonction qui sert à ce que le radar face des allés retours entre 2 cibles en continue. L'allé
	 * retour doit être le plus court possible afin de perdre le moins d'informations possible.
	 */
	// Variables utiles pour outnumbered_routine() UNIQUEMENT (elles sont aussi utilisées dans onScanned ceci dit)
	//Indique si on tourne vers la droite (1) ou la gauche (-1)
	private int swipeDirection = 1;
	//Indique l'ecart, en degré, qu'il semble y avoir entre les 2 cibles traquées. Si superieur à 180, il vaut mieux tourner dans l'autre sens
	private int seenTarget1 = 0;
	private int seenTarget2 = 0;
	private boolean reversed = false;
	public void outnumbered_routine()
	{
		turnRadarRight(45*swipeDirection);
		seenTarget1+=1;
		seenTarget2+=1;
		reversed = false;
	}
	
	/*
	 * Fonction qui sert à definir un new baiter
	 */
	public void assignNewBaiter()
	{
		//Seul le leader doit utiliser cette fonction
		if(!leader)
			return;
		
		String bestBaiter;
		
		//On verifie que le meilleur appat est toujours en mesure d'exercer ses fonctions, sinon, on choisit un autre
		//robot en mesure de remplir le rôle (en evitant de prendre le leader SI POSSIBLE
		bestBaiter = myLeaderData.getFm().lookForNewBaiter();
		//Vrai au premier tour seulement
		if(bestBaiter == null)
			bestBaiter = getTeammates()[getTeammates().length-1];
		System.out.println("Le meilleur baiter est : "+bestBaiter);
		
		//Le meilleur appat devient l'appat (même si cela veut dire que le leade est le nouvel appat)
		if(bestBaiter != getName() && bestBaiter != null)
		{
			try 
			{
				sendMessage(bestBaiter, movementBehavior.bait);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
			myMovementBehavior = movementBehavior.bait;
	}
	
	//Normalement, ce code n'est plus utile depuis que les données alliées et ennemis sont dans le field monitor
	/*//Sert à connaître son numéro de bot. Ce code fonctionne uniquement parce que tout les robots sont une instance de la même classe(code fournit par RoboCode)
	public int getBotNumber(String name) 
	{
		String n = "0";
		int low = name.indexOf("(")+1; 
		int hi = name.lastIndexOf(")");
		if (low >= 0 && hi >=0) 
		{ 
			n = name.substring(low, hi); 
		}
		return Integer.parseInt(n)-1;
	}
	
	//Sert à recréer le nom d'un robot à partir de son numéro. Ce code fonctionne uniquement parce que tout les robots sont une instance de la même classe
	public String getNameOfBotI(int i)
	{
		String name = "";
		int low = getName().indexOf("(")+1; 
		int hi = getName().lastIndexOf(")");
		if (low >= 0 && hi >=0) 
		{ 
			String beginning = getName().substring(0, low);
			String ending = getName().substring(hi);
			name = beginning+i+ending;
		}
		return name;
	}*/
}
