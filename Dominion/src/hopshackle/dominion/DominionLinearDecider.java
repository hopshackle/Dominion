package hopshackle.dominion;

import hopshackle.simulation.*;

import java.io.*;
import java.util.*;

public class DominionLinearDecider extends LookaheadDecider<Player, PositionSummary> {

	private double[] weights;
	public static String newline = System.getProperty("line.separator");

	public DominionLinearDecider(LookaheadFunction<Player, PositionSummary> lookahead, List<ActionEnum<Player>> actions, List<GeneticVariable<Player>> variables) {
		super(lookahead, actions, variables);
		weights = new double[variables.size()];
		for (int i = 0; i < weights.length; i++)
			weights[i] = 1.0;
		localDebug = false;
	}

	@Override
	public double value(PositionSummary ps) {
		double[] rawData = getState(ps, variableSet);
		return value(rawData);
	}

	private double value(double[] inputs) {
		if (inputs.length != variableSet.size())
			throw new AssertionError("Inputs in valueState not the correct length: " + inputs.length + " instead of " + variableSet.size());

		double retValue = 0.0;
		for (int i = 0; i < variableSet.size(); i++) 
			retValue += inputs[i] * weights[i];
		return retValue;
	}

	public void updateWeight(GeneticVariable<Player> input, double delta) {
		int index = variableSet.indexOf(input);
		weights[index] += delta;
	}

	public double getWeightOf(GeneticVariable<Player> input) {
		return weights[variableSet.indexOf(input)];
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
		double[] startState = exp.getStartState();
		double[] endState = exp.getEndState();
		double[] featureTrace = exp.getFeatureTrace();
		double predictedValue = value(startState);
		double actualValue = value(endState);
		double delta = exp.getReward() + gamma * actualValue - predictedValue;
		if (localDebug) {
			String message = String.format("Learning:\t%-15sReward: %.2f, NextValue: %.2f, Predicted: %.2f, Delta: %.4f", 
					exp.getActionTaken(), exp.getReward(), actualValue, predictedValue, delta);
			log(message);
			exp.getAgent().log(message);
			StringBuffer logMessage = new StringBuffer("StartState -> EndState (FeatureTrace) :" + newline);
			for (int i = 0; i < startState.length; i++) {
				if (startState[i] != 0.0 || endState[i] != 0.0 || Math.abs(featureTrace[i]) >= 0.01)
					logMessage.append(String.format("\t%.2f -> %.2f (%.2f) %s %s", startState[i], endState[i], featureTrace[i], variableSet.get(i).toString(), newline));
			}
			message = logMessage.toString();
			log(message);
			exp.getAgent().log(message);
		}
		for (int i = 0; i < startState.length; i++) {
			double value = featureTrace[i];
			if (value == 0.0) continue;
			GeneticVariable<Player> input = variableSet.get(i);
			double weightChange = value * delta * alpha;
			if (localDebug) {
				String message = String.format("\t\t%-15s Value: %.2f, WeightChange: %.4f, Current Weight: %.2f", input.toString(), value, weightChange, 
					getWeightOf(input));
				log(message);
				exp.getAgent().log(message);
			}
			updateWeight(input, weightChange);
		}
	}

	public void saveToFile(String descriptor) {
		String directory = SimProperties.getProperty("BaseDirectory", "C:");
		directory = directory + "\\DominionBrains\\";
		File saveFile = new File(directory + descriptor + "_" + name.substring(0, 4) + ".txt");

		try {
			FileWriter outputWriter = new FileWriter(saveFile);
			for (GeneticVariable<Player> gv : variableSet) {
				outputWriter.write(String.format(("%-20s: %.2f"), gv.toString(), getWeightOf(gv)));
				outputWriter.write(newline);
			}
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("Error saving brain: " + e.toString());
			for ( StackTraceElement s : e.getStackTrace()) {
				logger.info(s.toString());
			}
		} 
	}

}
