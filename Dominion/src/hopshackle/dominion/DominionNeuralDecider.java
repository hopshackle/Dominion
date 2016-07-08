package hopshackle.dominion;

import hopshackle.simulation.*;

import java.io.*;
import java.util.*;

import org.encog.neural.data.basic.*;
import org.encog.neural.networks.*;
import org.encog.neural.networks.structure.NeuralStructure;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.quick.QuickPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

public class DominionNeuralDecider extends LookaheadDecider<Player, PositionSummary> {

	protected BasicNetwork stateEvaluationBrain;
	protected static double momentum = SimProperties.getPropertyAsDouble("DominionLearningMomentum", "0.0");
	protected static int learningIterations = Integer.valueOf(SimProperties.getProperty("DominionLearningIterations", "1"));
	private static boolean applyTemperatureToLearning = SimProperties.getProperty("DominionAnnealLearning", "false").equals("true");
	private static boolean learnWithValidation = SimProperties.getProperty("DominionLearnUntilValidationError", "false").equals("true");
	private static int maxLearningIterations = Integer.valueOf(SimProperties.getProperty("DominionMaxLearningIterations", "1"));
	private static boolean logTrainingErrors = SimProperties.getProperty("DominionLogTrainingErrors", "false").equals("true");
	private static String propagationType = SimProperties.getProperty("DominionPropagationType", "back");
	private boolean ableToLearn = true;
	private DominionLookaheadFunction lookahead = new DominionLookaheadFunction();
	public static String newline = System.getProperty("line.separator");

	public DominionNeuralDecider(LookaheadFunction<Player, PositionSummary> lookahead, List<CardType> actions, List<CardValuationVariables> variables) {
		super(lookahead, HopshackleUtilities.convertList(actions), HopshackleUtilities.convertList(variables));
		stateEvaluationBrain = NeuralDecider.initialiseBrain(variableSet);
		localDebug = false;
	}

	public DominionNeuralDecider(DominionNeuralDecider parent, int mutations) {
		super(parent.lookahead, HopshackleUtilities.convertList(parent.actionSet), HopshackleUtilities.convertList(HopshackleUtilities.cloneList(parent.variableSet)));
		GameSetup gs = new GameSetup();
		for (int i = 0; i < mutations; i++) {
			int numberOfInputs = variableSet.size();
			if (Math.random() > (numberOfInputs - 5) * 0.1) {
				// then add a new one
				List<CardValuationVariables> allVar = gs.getDeckVariables();
				boolean variableFound = false;
				do {
					int roll = Dice.roll(1, allVar.size()) -1;
					GeneticVariable<Player> choice = allVar.get(roll);
					if (!variableSet.contains(choice)) {
						variableFound = true;
						variableSet.add(choice);
					}
				} while (!variableFound);
			} else {
				int roll = Dice.roll(1, numberOfInputs) -1;
				variableSet.remove(roll);
			}
		}		
		stateEvaluationBrain = NeuralDecider.initialiseBrain(variableSet);
	}

	@SuppressWarnings("unchecked")
	public <V extends GeneticVariable<Player>> List<V> combineAndMutateInputs(List<V> list, int mutations) {
		Set<V> retValue1 = new HashSet<V>();
		for (GeneticVariable<Player> gv : variableSet) {
			if (Math.random() > 0.50)
				retValue1.add((V) gv);
		}
		for (GeneticVariable<Player> gv : list) {
			if (Math.random() > 0.50)
				retValue1.add((V) gv);
		}
		List<V> retValue = new ArrayList<V>();
		for (GeneticVariable<Player> gv : retValue1) 
			retValue.add((V) gv);
		GameSetup gs = new GameSetup();
		for (int i = 0; i < mutations; i++) {
			int numberOfInputs = retValue.size();
			if (Math.random() > (numberOfInputs - 5) * 0.1) {
				// then add a new one
				List<CardValuationVariables> allVar = gs.getDeckVariables();
				boolean variableFound = false;
				do {
					int roll = Dice.roll(1, allVar.size()) -1;
					V choice = (V) allVar.get(roll);
					if (!retValue.contains(choice)) {
						variableFound = true;
						retValue.add(choice);
					}
				} while (!variableFound);
			} else {
				int roll = Dice.roll(1, numberOfInputs) -1;
				retValue.remove(roll);
			}
		}
		return retValue;
	}

	public void mutateWeights(int mutations) {
		NeuralStructure structure = stateEvaluationBrain.getStructure();
		if (stateEvaluationBrain.getLayerCount() != 3) 
			throw new AssertionError("Hard-coded assumption that NN has just one hidden layer.");
		int totalWeights = structure.calculateSize();
		int inputNeurons = stateEvaluationBrain.getLayerNeuronCount(1);
		int hiddenNeurons = stateEvaluationBrain.getLayerTotalNeuronCount(2);
		int outputNeurons = stateEvaluationBrain.getLayerTotalNeuronCount(3);
		totalWeights = inputNeurons * (hiddenNeurons - 1) + hiddenNeurons * outputNeurons;
		// -1 in the above is for the bias neuron in the hidden layer, which has no connections from input layer

		for (int i = 0; i < mutations; i++) {
			int weightToChange = Dice.roll(1, totalWeights);
			int fromLayer = 1;
			int toNeuronCount = hiddenNeurons - 1;
			if (weightToChange > inputNeurons * (hiddenNeurons - 1)) {
				fromLayer = 2;
				weightToChange -= inputNeurons * (hiddenNeurons - 1);
				toNeuronCount = outputNeurons;
			}
			int fromNeuron = weightToChange / toNeuronCount + 1;
			int toNeuron = weightToChange % toNeuronCount + 1;

			stateEvaluationBrain.addWeight(fromLayer, fromNeuron, toNeuron, Math.random() / 10.0);
		}
	}

	@Override
	public <V extends GeneticVariable<Player>> void setVariables(List<V> variables) {
		super.setVariables(variables);
		stateEvaluationBrain = NeuralDecider.initialiseBrain(variableSet);
	}

	private double value(double[] state) {
		BasicNeuralData inputData = new BasicNeuralData(state);
		double value = stateEvaluationBrain.compute(inputData).getData()[0];
		return value;
	}

	@Override
	public double value(PositionSummary ps) {
		double[] rawData = getState(ps, variableSet);
		return value(rawData);
	}

	@Override
	public List<ActionEnum<Player>> getChooseableOptions(Player player, Agent contextAgent) {
		DominionBuyingDecision dpd = new DominionBuyingDecision(player, player.getBudget(), player.getBuys());
		List<ActionEnum<Player>> retValue = dpd.getPossiblePurchasesAsActionEnum();
		return retValue;
	}

	protected double teach(BasicNeuralDataSet trainingData) {
		double trainingError = 0.0;
		double temperature = SimProperties.getPropertyAsDouble("Temperature", "1.0");
		double updatedLearningCoefficient = alpha;
		if (applyTemperatureToLearning)
			updatedLearningCoefficient *= temperature;
		Propagation trainer = null;
		switch (propagationType) {
		case "back":
			trainer = new Backpropagation(stateEvaluationBrain, trainingData, updatedLearningCoefficient, momentum);
			break;
		case "quick":
			trainer = new QuickPropagation(stateEvaluationBrain, trainingData, updatedLearningCoefficient);
			break;
		case "resilient":
			trainer = new ResilientPropagation(stateEvaluationBrain, trainingData);
			break;
		default:
			throw new AssertionError(propagationType + " is not a known type. Must be back/quick/resilient.");
		}

		trainer.iteration(learningIterations);
		trainer.finishTraining();
		if (lambda > 0.00)
			applyLambda();
		return trainingError;
	}

	private void applyLambda() {
		double temperature = SimProperties.getPropertyAsDouble("Temperature", "1.0");
		double updatedLambda = lambda;
		if (applyTemperatureToLearning)
			updatedLambda *= temperature;
		for (int layerNumber = 0; layerNumber < stateEvaluationBrain.getLayerCount()-1; layerNumber++) {	
			for (int fromNeuron = 0; fromNeuron < stateEvaluationBrain.getLayerTotalNeuronCount(layerNumber); fromNeuron++) {
				for (int toNeuron = 0; toNeuron < stateEvaluationBrain.getLayerNeuronCount(layerNumber+1); toNeuron++) {
					// The last Neuron in each layer is a bias neuron, which has no incoming connections
					if (stateEvaluationBrain.isConnected(layerNumber, fromNeuron, toNeuron)) {
						double currentWeight = stateEvaluationBrain.getWeight(layerNumber, fromNeuron, toNeuron);
						stateEvaluationBrain.setWeight(layerNumber, fromNeuron, toNeuron, currentWeight * (1.0 - updatedLambda));
					} else {
						System.out.println(String.format("No Connection from layer %d, %d -> %d",layerNumber, fromNeuron, toNeuron));
					}
				}
			}
		}
	}

	@Override
	public Decider<Player> crossWith(Decider<Player> otherDecider) {
		if (otherDecider == null) {
			DominionNeuralDecider retValue = new DominionNeuralDecider(this, 0);
			retValue.stateEvaluationBrain = (BasicNetwork) stateEvaluationBrain.clone();
			return retValue;
		}
		if (!(otherDecider instanceof DominionNeuralDecider))
			return super.crossWith(otherDecider);
		if (this.actionSet.size() != otherDecider.getActions().size())
			return super.crossWith(otherDecider);

		List<GeneticVariable<Player>> newInputs = combineAndMutateInputs(otherDecider.getVariables(), 4);
		DominionNeuralDecider retValue = new DominionNeuralDecider(lookahead, HopshackleUtilities.convertList(actionSet), HopshackleUtilities.convertList(newInputs));
		retValue.setName(this.toString());
		return retValue;
	}

	public void saveToFile(String descriptor) {
		String directory = SimProperties.getProperty("BaseDirectory", "C:");
		directory = directory + "\\DominionBrains\\";
		File saveFile = new File(directory + descriptor + "_" + name.substring(0, 4) + ".brain");

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));

			oos.writeObject(actionSet);
			oos.writeObject(variableSet);
			oos.writeObject(stateEvaluationBrain);

			oos.close();

		} catch (IOException e) {
			logger.severe("Error saving brain: " + e.toString());
			for ( StackTraceElement s : e.getStackTrace()) {
				logger.info(s.toString());
			}
		} 
	}

	@SuppressWarnings("unchecked")
	public static DominionNeuralDecider createDPSDecider(File saveFile) {
		DominionNeuralDecider retValue = null;
		LookaheadFunction<Player, PositionSummary> lookahead = new DominionLookaheadFunction();
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile));

			ArrayList<CardType> actionSet = (ArrayList<CardType>) ois.readObject();
			ArrayList<CardValuationVariables> variableSet = (ArrayList<CardValuationVariables>) ois.readObject();
			retValue = new DominionNeuralDecider(lookahead, actionSet, variableSet);

			BasicNetwork stateBrain = (BasicNetwork) ois.readObject();

			ois.close();
			retValue.stateEvaluationBrain = stateBrain;
			String name = saveFile.getName();
			String end = ".brain";
			name = name.substring(0, name.indexOf(end));
			retValue.setName(name);

		} catch (Exception e) {
			logger.severe("Error reading brain: " + e.toString());
			for ( StackTraceElement s : e.getStackTrace()) {
				logger.info(s.toString());
			}
		}

		return retValue;
	}

	@Override
	public void learnFromBatch(ExperienceRecord<Player>[] expArray, double maxResult) {
		if (alpha < 0.000001 || !ableToLearn)
			return;	// no learning to take place
		
		int inputLength = stateEvaluationBrain.getInputCount();
		double[][] batchOutputData = new double[expArray.length][1];
		double[][] batchInputData = new double[expArray.length][inputLength];

		int count = 0;
		for (ExperienceRecord<Player> exp : expArray) {
			double[] expData = preprocessExperienceRecord(exp, maxResult);
			batchOutputData[count][0] = expData[0];
			for (int n = 0; n < inputLength; n++)
				batchInputData[count][n] = expData[n+1];

			count++;
		}

		if (learnWithValidation) {
			double[][] validationOutputData = new double[expArray.length / 5][1];
			double[][] validationInputData = new double[expArray.length / 5][inputLength];

			double[][] batchOutputData2 = new double[expArray.length - expArray.length / 5][1];
			double[][] batchInputData2 = new double[expArray.length - expArray.length / 5][inputLength];

			int valCount = 0;
			for (int i = 0; i < expArray.length; i++) {
				if (i % 5 == 4) {
					validationInputData[valCount] = batchInputData[i];
					validationOutputData[valCount] = batchOutputData[i];
					valCount++;
				} else {
					batchInputData2[i - valCount] = batchInputData[i];
					batchOutputData2[i - valCount] = batchOutputData[i];
				}
			}

			BasicNeuralDataSet trainingData = new BasicNeuralDataSet(batchInputData2, batchOutputData2);
			BasicNeuralDataSet validationData = new BasicNeuralDataSet(validationInputData, validationOutputData);
			BasicNetwork brainCopy = (BasicNetwork) stateEvaluationBrain.clone();
			double startingError = stateEvaluationBrain.calculateError(validationData);
			double valError = 1.00;
			int iteration = 1;
			boolean terminateLearning = false;
			double lastTrainingError = 0.0;
			double trainingError = 0.0;
			do {
				lastTrainingError = trainingError;
				trainingError = teach(trainingData);
				double newValError = stateEvaluationBrain.calculateError(validationData);
				//			System.out.println(String.format("Iteration %d on %s has validation error of %.5f and training error of %.5f (starting validation error %.5f)", iteration, this.toString(), newValError, trainingError, startingError));
				if (newValError >= valError || iteration > maxLearningIterations) {
					terminateLearning = true;
					stateEvaluationBrain = brainCopy;
					if (logTrainingErrors)
						System.out.println(String.format("%d iterations on %s has validation error of %.5f and training error of %.5f (starting validation error %.5f)", iteration-1, this.toString(), valError, lastTrainingError, startingError));
				} else {
					brainCopy = (BasicNetwork) stateEvaluationBrain.clone();
					valError = newValError;
				}
				iteration++;
			} while (!terminateLearning);			

		} else {
			BasicNeuralDataSet trainingData = new BasicNeuralDataSet(batchInputData, batchOutputData);
			double error = teach(trainingData);
			if (logTrainingErrors)
				System.out.println(String.format("%s has training error of %.4f", this.toString(), error));
		}
	}

	private double[] preprocessExperienceRecord(ExperienceRecord<Player> exp, double maxResult) {
		// returns an array, with first element being the output value (result), and then all subsequent elements being the input values
		double endValue = value(exp.getEndState()) * maxResult; // projection
		double finalValue = gamma * endValue;
		if (exp.isInFinalState()) {
			finalValue = exp.getEndScore();
			endValue = exp.getEndScore();
		}
		if (finalValue > maxResult) {
			finalValue = maxResult;
		}
		if (finalValue < 0.0) finalValue = 0.0;

		double[] retValue = new double[stateEvaluationBrain.getInputCount() + 1];

		retValue[0] = finalValue/maxResult;	
		double[] subLoop = exp.getFeatureTrace();	
		for (int n=0; n<subLoop.length; n++) {
			retValue[n+1] = subLoop[n];
		}

		if (localDebug) {
			String message = String.format("Learning:\t%-20sEndScore: %.2f, End State Valuation: %.2f, Inferred Start Value: %.2f, EndGame: %s", 
					exp.getActionTaken(), exp.getEndScore(), endValue, finalValue, exp.isInFinalState());
			log(message);
			exp.getAgent().log(message);
			double[] startState = exp.getStartState();
			double[] endState = exp.getEndState();
			double[] featureTrace = exp.getFeatureTrace();
			StringBuffer logMessage = new StringBuffer("StartState -> EndState (FeatureTrace) :" + newline);
			for (int i = 0; i < startState.length; i++) {
				if (startState[i] != 0.0 || endState[i] != 0.0 || Math.abs(featureTrace[i]) >= 0.01)
					logMessage.append(String.format("\t%.2f -> %.2f (%.2f) %s %s", startState[i], endState[i], featureTrace[i], variableSet.get(i).toString(), newline));
			}
			message = logMessage.toString();
			log(message);
			exp.getAgent().log(message);
		}

		return retValue;
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
		if (alpha < 0.000001 || !ableToLearn)
			return;	// no learning to take place

		double[] expData = preprocessExperienceRecord(exp, maxResult);
		double [][] outputValues = new double[1][1];
		double[][] inputValues = new double[1][expData.length-1];

		outputValues[0][0] = expData[0];
		for (int n = 0; n < expData.length-1; n++)
			inputValues[0][n] = expData[n+1];

		BasicNeuralDataSet trainingData = new BasicNeuralDataSet(inputValues, outputValues);
		teach(trainingData);
	}

	public void setLearning(boolean b) {
		ableToLearn = b;
	}
	public boolean getLearning() {
		return ableToLearn;
	}
}