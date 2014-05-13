package CERI;

import java.util.ArrayList;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.learning.LMS;


public class NeuroBrain {
	
	/*
	 * structure même du réseau de neuronnes
	 */
	NeuralNetwork neuralNetwork;
	
	/*
	 * nombre maximum d'itération dans l'apprentissage
	 */
	public static final int maxIterations = 250000;
	
	/*
	 * jeu de données
	 */
	TrainingSet<SupervisedTrainingElement> trainingSet;
	
	
	
	public static final double maxAngle = 360;
	
	public static final double maxX = 1200;
	
	public static final double maxY = 1200;
	
	public static final double maxPower = 3;
	
	
	public NeuroBrain( ArrayList<NeurophData> datas ) {
		neuralNetwork = new MultiLayerPerceptron ( 5, 25, 2 );
		((LMS) neuralNetwork.getLearningRule()).setMaxError(0.00001);// taux d'erreur moyen accepté
        ((LMS) neuralNetwork.getLearningRule()).setLearningRate(0.7);//
        ((LMS) neuralNetwork.getLearningRule()).setMaxIterations(maxIterations);
        
        createAndSetTrainingset ( datas );
        
        neuralNetwork.learnInNewThread ( trainingSet ); // il ne faut pas bloquer le robot dans ses autres taches
	}
	
	/*
	 * cette fonction permet d'initialiser le réseau de neuronne à partir
	 * d'une liste de NeurophData
	 */
	public void createAndSetTrainingset ( ArrayList<NeurophData> datas ) {
		
		trainingSet = new TrainingSet<SupervisedTrainingElement>();
		
		double[] input = new double [ 5 ];
		double[] output = new double [ 2 ];
		
		
		for ( NeurophData i : datas ) {
			
			input [ 0 ] = i.getDistance();
			input [ 1 ] = i.getAngle();
			input [ 2 ] = i.getHeadingEnemy();
			input [ 3 ] = i.getEnemyVelocity();
			input [ 4 ] = i.isTouche() ? 1.0d : 0.0d ;
			output [ 0 ] = i.getBulletHeading();
			output [ 1 ] = i.getBulletPower();
			
			trainingSet.addElement ( new SupervisedTrainingElement  ( input, output ) ); // besoin que des entrées
		}
		
		
	}
	

	public NeuroBrain ( final String neuroNetworkFile, final String dataFile, final boolean fromFile ) {
		
		// si on veut charger à partir d'un fichier
		if ( fromFile ) {
			
			neuralNetwork = NeuralNetwork.load ( neuroNetworkFile ); // création du réseau de neuronne
		}
		else { // sinon on crée un réseau de base
			/*
			 * 5 entrée :
			 * - distance ennemi
			 * - angle vers l'ennemi
			 * - direction ennemi
			 * - vitesse ennemi
			 * - touche
			 * 
			 * 2 sorties:
			 * - angle de tir
			 * - puissance du tir
			 */
			neuralNetwork = new MultiLayerPerceptron ( 5, 25, 2 ); 
		}
		
		
		
		
		((LMS) neuralNetwork.getLearningRule()).setMaxError(0.00001);// taux d'erreur moyen accepté
        ((LMS) neuralNetwork.getLearningRule()).setLearningRate(0.7);//
        ((LMS) neuralNetwork.getLearningRule()).setMaxIterations(maxIterations);
        
        
        /*
         * chargement d'un jeu de données précréé
         */
		trainingSet = TrainingSet.createFromFile( "dataFile.txt", 5, 2, ","); // separateur = ,
        
        /*
         * on apprend la première fois pour initialiser le réseau de neuronne
         */
        neuralNetwork.learnInNewThread ( trainingSet ); // il ne faut pas bloquer le robot dans ses autres taches
	}
	
	
	
	/*
	 * mettre en pause l'apprentissage, ce qui permet
	 * d'utiliser le réseau tel quel si besoin sans garanti que
	 * l'apprentissage était fini
	 */
	public void pause () {
		neuralNetwork.pauseLearning();
	}
	
	
	
	
	/*
	 * permet de reprendre l'apprentissage
	 */
	public void resume() {
		neuralNetwork.resumeLearning();
	}
	
	
	
	
	/*
	 * ajoute une ligne dans le jeu de donnée
	 */
	public void addEntryValues ( double[] entrees, double[] sorties ) {
		trainingSet.addElement ( new SupervisedTrainingElement ( entrees, sorties ) );
	}
	
	
	
	
	/*
	 * génère une solution
	 */
	
	public double[] getSolution ( double[] input ) {
		/*
		 * donnée d'entrée de test pour la génération de solution
		 */
		TrainingSet<SupervisedTrainingElement > testSet = new TrainingSet<SupervisedTrainingElement>();
		testSet.addElement ( new SupervisedTrainingElement  ( input, null ) ); // besoin que des entrées
		
		double[] networkOutput = null;

		pause(); // on pause le training si jamais il tournait
		
		for (SupervisedTrainingElement  testElement : testSet.elements() ) {
			neuralNetwork.setInput ( testElement.getInput() ); // envoi ds données d'entrées au réseau de neuronne
			neuralNetwork.calculate(); // calcul de la solution
			networkOutput = neuralNetwork.getOutput(); // récupération des sorties générées
		}
		
		resume(); // on relance le training
		
		
		/*
		 * on oublie pas de remettre les valeurs en non-normalisé
		 */
		networkOutput[0] = networkOutput[0] * maxAngle;
		networkOutput[1] = networkOutput[1] * maxPower;
		
		
		return networkOutput;
	}
	
	
}
