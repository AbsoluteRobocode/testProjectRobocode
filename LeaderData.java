package CERI;

import java.util.ArrayList;

public class LeaderData implements java.io.Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//Nombre de fois où il a semblé que l'appat n'ai servit à rien
	private int nbBaitFailed = 0;
	//Nombre de fois où il a semblé qu'un autre robot aurait dû être l'appat
	private int nbWrongBaiter = 0;
	//Permet de traquer ce qu'il se passe sur le terrain
	private FieldMonitor myFieldMonitor;
	
	//Certaines variables statiques doivent donc être réinitialisées à chaque tour par le leader
	//Le nombre de tireurs ayant pris plus de dégâts que prévu durant ce tour
	private int damagedShooter = 0;
	//Indique si la stratégie a fonctionné durant ce tour
	private boolean stratWorked = true;
	//Nombre d'alliés encore en vie
	private int teammatesAlive;
	//Nombre d'ennemis encore en vie
	private int ennemiesAlive;
	
	//Les donnees utilisees par Neuroph
	private ArrayList<NeurophData> shotMemories = new ArrayList<NeurophData>();
	
	//La cible de nos robots
	private Integer target = null;
	
	private NeuroBrain cerveau;
	
	public NeuroBrain getCerveau() {
		return cerveau;
	}
	
	
	public void setCerveau( ) {
		cerveau = new NeuroBrain ( this.getShotMemories() );
	}
	
	public LeaderData()
	{
		myFieldMonitor = new FieldMonitor();
	}
	
	public FieldMonitor getFm() 
	{
		return myFieldMonitor;
	}

	public int getTeammatesAlive() {
		return teammatesAlive;
	}

	public void setTeammatesAlive(int teammatesAlive) {
		this.teammatesAlive = teammatesAlive;
	}

	public int getEnnemiesAlive() {
		return ennemiesAlive;
	}

	public void setEnnemiesAlive(int ennemiesAlive) {
		this.ennemiesAlive = ennemiesAlive;
	}

	public boolean isStratWorked() {
		return stratWorked;
	}

	public void setStratWorked(boolean stratWorked) {
		this.stratWorked = stratWorked;
	}

	public int getDamagedShooter() {
		return damagedShooter;
	}

	public void setDamagedShooter(int damagedShooter) {
		this.damagedShooter = damagedShooter;
	}

	public int getNbBaitFailed() {
		return nbBaitFailed;
	}

	public void setNbBaitFailed(int nbBaitFailed) {
		this.nbBaitFailed = nbBaitFailed;
	}

	public int getNbWrongBaiter() {
		return nbWrongBaiter;
	}

	public void setNbWrongBaiter(int nbWrongBaiter) {
		this.nbWrongBaiter = nbWrongBaiter;
	}

	public void newRound() 
	{
		damagedShooter = 0;
		stratWorked = true;
		myFieldMonitor.newRound();
		target = null;
	}
	
	//NOTE : vu que
	public Integer acquireTarget()
	{
		//Si on a déjà une cible et qu'elle est toujours en vie
		if(target!=null && myFieldMonitor.getRobots().get(target).getEnergie() > 0)
		{
			return target;
		}
		//Sinon, on cherche une nouvelle cible.
		else
		{
			target = null;
			for(int i=0; i<myFieldMonitor.getRobots().size(); i++)
			{
				if(myFieldMonitor.getRobots().get(i).getEnergie() > 0)
				{
					target = i;
					return target;
				}
			}
		}
		//Ici, target peut toujours être null
		return target;
	}
	
	public ShootInstruction neuroph(double distance, double angle, double headingEnemy, double enemyVelocity, long turn)
	{
		System.out.println("LeaderData->neuroph() n'a pas encore été implémenté.");
		//INSTRUCTION BIDON POUR TESTER !!
		
		double[] input = { distance, angle, headingEnemy, enemyVelocity, 1.0d };
		
		double[] solution = cerveau.getSolution(input);
		
		return new ShootInstruction( solution[0], solution[1] );
	}

	public ArrayList<NeurophData> getShotMemories() 
	{
		return shotMemories;
	}
}
