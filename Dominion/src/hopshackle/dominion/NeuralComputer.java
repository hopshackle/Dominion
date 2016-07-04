package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;
import org.encog.neural.data.basic.*;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

public class NeuralComputer {

	protected List<GeneticVariable<Player>> variableSet;
	protected BasicNetwork gameEndBrain;
	protected static double momentum = SimProperties.getPropertyAsDouble("DominionLearningMomentum", "0.0");
	protected static double alpha = SimProperties.getPropertyAsDouble("Alpha", "0.10");
	private static boolean applyTemperatureToLearning = SimProperties.getProperty("DominionAnnealLearning", "false").equals("true");

	public NeuralComputer() {
		variableSet = new ArrayList<GeneticVariable<Player>>();
		variableSet.add(CardValuationVariables.PROVINCES_BOUGHT);
		variableSet.add(CardValuationVariables.MOST_DEPLETED_PILE);
		variableSet.add(CardValuationVariables.SECOND_DEPLETED_PILE);
		variableSet.add(CardValuationVariables.THIRD_DEPLETED_PILE);
		variableSet.add(CardValuationVariables.TURNS);
		gameEndBrain = NeuralDecider.initialiseBrain(variableSet);
	}

	public double[] getInputs(PositionSummary positionSummary) {
		double[] inputs = new double[variableSet.size()];
		int count = 0;
		for (GeneticVariable<Player> gv : variableSet) {
			inputs[count] = gv.getValue(positionSummary);
			count++;
		}
		return inputs;
	}

	public double getValue(PositionSummary positionSummary) {
		double[] inputs = getInputs(positionSummary);
		BasicNeuralData input = new BasicNeuralData(inputs);
		return gameEndBrain.compute(input).getData(0);
	}

	public void train(List<double[]> inputs, double[] actuals) {
		if (alpha < 0.000001)
			return;	// no learning to take place
		int inputLength = gameEndBrain.getInputCount();
		double[][] batchOutputData = new double[actuals.length][1];
		double[][] batchInputData = new double[actuals.length][inputLength];

		for (int count = 0; count < actuals.length; count++) {
			batchOutputData[count][0] = actuals[count];
			batchInputData[count] = inputs.get(count);
		}
		BasicNeuralDataSet trainingData = new BasicNeuralDataSet(batchInputData, batchOutputData);

		double temperature = SimProperties.getPropertyAsDouble("Temperature", "1.0");
		double updatedLearningCoefficient = alpha;
		if (applyTemperatureToLearning)
			updatedLearningCoefficient *= temperature;
		Backpropagation trainer = new Backpropagation(gameEndBrain, trainingData, updatedLearningCoefficient, momentum);
		trainer.iteration();
	}

}
