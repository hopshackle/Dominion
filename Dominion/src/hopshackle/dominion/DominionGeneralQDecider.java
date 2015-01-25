package hopshackle.dominion;

import hopshackle.simulation.*;

import java.io.*;
import java.util.*;

public class DominionGeneralQDecider extends QDecider implements DominionPositionDecider {

	private DominionLookaheadFunction lookahead = new DominionLookaheadFunction();
	private double[] weights;
	public static String newline = System.getProperty("line.separator");

	public DominionGeneralQDecider(List<? extends ActionEnum> actions, List<GeneticVariable> variables) {
		super(actions, variables);
		weights = new double[variables.size()];
		for (int i = 0; i < weights.length; i++)
			weights[i] = 1.0;
		localDebug = false;
	}

	@Override
	public double valuePosition(PositionSummary ps) {
		double[] rawData = lookahead.convertPositionSummaryToAttributes(ps, variableSet);
		return valueOption(null, rawData);
	}

	@Override
	public double valueOption(ActionEnum option, Agent decidingAgent, Agent contextAgent) {
		double[] nextInputs = lookahead.oneStepLookahead(decidingAgent, option, variableSet);		
		return valueOption(null, nextInputs);
	}

	@Override
	public double valueOption(ActionEnum option, double[] inputs) {
		if (inputs.length != variableSet.size())
			throw new AssertionError("Inputs in valueState not the correct length: " + inputs.length + " instead of " + variableSet.size());

		double retValue = 0.0;
		for (int i = 0; i < variableSet.size(); i++) 
			retValue += inputs[i] * weights[i];
		return retValue;
	}

	public void updateWeight(GeneticVariable input, ActionEnum option, double delta) {
		int index = variableSet.indexOf(input);
		weights[index] += delta;
	}

	public double getWeightOf(GeneticVariable input) {
		return weights[variableSet.indexOf(input)];
	}

	@Override
	protected ExperienceRecord getExperienceRecord(Agent decidingAgent, Agent contextAgent, ActionEnum option) {
		Player player = (Player) decidingAgent;
		DominionExperienceRecord output = new DominionExperienceRecord(player.getPositionSummaryCopy(), option, getChooseableOptions(decidingAgent, contextAgent));
		return output;
	}

	@Override
	public void learnFrom(ExperienceRecord er, double maxResult) {
		if (!(er instanceof DominionExperienceRecord))
			throw new AssertionError("Vanilla ExperienceRecord in Dominion game");
		DominionExperienceRecord exp = (DominionExperienceRecord) er;
		PositionSummary startPS = exp.getStartPS();
		startPS.drawCard(exp.getActionTaken());	// i.e. what would have been predicted
		double predictedValue = valuePosition(startPS);
		ActionEnum nextAction = getBestActionFrom(exp.getPossibleActionsFromEndState(), exp.getEndPS());
		String nextActionName = "NULL";
		if (nextAction != null)
			nextActionName = nextAction.toString();
		double bestNextActionValue = valueOfBestAction(exp);
		double delta = exp.getReward() + gamma * bestNextActionValue - predictedValue;
		double[] startState = lookahead.convertPositionSummaryToAttributes(startPS, variableSet);

		if (localDebug) {
			log(String.format("Learning:\t%-15sReward: %.2f, NextValue: %.2f, Predicted: %.2f, Delta: %.4f, NextAction: %s", 
					exp.getActionTaken(), exp.getReward(), bestNextActionValue, predictedValue, delta, nextActionName));
			StringBuffer logMessage = new StringBuffer("Start state: ");
			double[] state = exp.getStartState();
			for (int i = 0; i < state.length; i++) 
				logMessage.append(String.format(" [%.2f] ", state[i]));
			log(logMessage.toString());
			logMessage = new StringBuffer("End state:   ");
			state = exp.getEndState();
			for (int i = 0; i < state.length; i++) 
				logMessage.append(String.format(" [%.2f] ", state[i]));
			log(logMessage.toString());
		}

		for (int i = 0; i < variableSet.size(); i++) {
			double value = startState[i];
			GeneticVariable input = variableSet.get(i);
			double weightChange = value * delta * alpha;
			if (localDebug) log(String.format("\t\t%-15s Value: %.2f, WeightChange: %.4f, Current Weight: %.2f", input.toString(), value, weightChange, 
					getWeightOf(input)));
			updateWeight(input, null, value * delta * alpha);
		}
	}

	@Override 
	protected double valueOfBestAction(ExperienceRecord exp) {
		if (exp.isInFinalState()) 
			return 0.0;
		if (!(exp instanceof DominionExperienceRecord))
			throw new AssertionError("Vanilla ExperienceRecord in Dominion game");
		DominionExperienceRecord domExp = (DominionExperienceRecord) exp; 
		CardType bestAction = getBestActionFrom(domExp.getPossibleActionsFromEndState(), domExp.getEndPS());
		PositionSummary ps = domExp.getEndPS();
		ps.drawCard(bestAction);
		return valuePosition(ps);
	}

	protected CardType getBestActionFrom(List<ActionEnum> possActions, PositionSummary ps) {
		double bestValue = Double.NEGATIVE_INFINITY;
		CardType bestAction = null;
		for (ActionEnum o : possActions) {
			CardType option = (CardType) o;
			ps.drawCard(option);
			double value = valuePosition(ps);
			if (value > bestValue) {
				bestValue = value;
				bestAction = option;
			}
		}
		return bestAction;
	}

	public void saveToFile(String descriptor) {
		String directory = SimProperties.getProperty("BaseDirectory", "C:");
		directory = directory + "\\DominionBrains\\";
		File saveFile = new File(directory + descriptor + "_" + name.substring(0, 4) + ".txt");

		try {
			FileWriter outputWriter = new FileWriter(saveFile);
			for (GeneticVariable gv : variableSet) {
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

	@Override
	protected ActionEnum getBestActionFrom(List<ActionEnum> possActions, double[] state) {
		throw new AssertionError("Invalid function call for a Dominion game.");
	}
	
	@Override
	public List<CardType> buyingDecision(Player player, int budget, int buys) {
		List<CardType> retValue = (new DominionBuyingDecision(player, budget, buys)).getBestPurchase();
		for (CardType purchase : retValue)
			learnFromDecision(player, player, purchase);	// not ideal, but much simpler. Just record each decision as if it was distinct (in increasing order of cost)
		return retValue;
	}
}
