package hopshackle.dominion;

import hopshackle.simulation.*;
import java.io.*;
import java.util.*;

public class DominionNeuralDecider extends NeuralLookaheadDecider<Player> {

	private boolean ableToLearn = true;

	public DominionNeuralDecider(List<CardValuationVariables> variables) {
		super(new DominionStateFactory(HopshackleUtilities.convertList(variables)), 100.0);
	}

	@Override
	public void learnFromBatch(ExperienceRecord<Player>[] expArray, double maxResult) {
		if (ableToLearn) {
			super.learnFromBatch(expArray, maxResult);
		} else {
			// ignore
		}
	}
	
	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
		if (ableToLearn) {
			super.learnFrom(exp, maxResult);
		} else {
			// ignore
		}
	}

	public void setLearning(boolean b) {
		ableToLearn = b;
	}
	public boolean getLearning() {
		return ableToLearn;
	}

	public static DominionNeuralDecider createFromFile(File fileName) {
		StateFactory<Player> sf = new DominionStateFactory(new ArrayList<GeneticVariable<Player>>());
		NeuralDecider<Player> nd = NeuralDecider.createFromFile(sf, fileName, true);
		DominionNeuralDecider retValue = new DominionNeuralDecider(nd.getVariables());
		DeciderProperties propToUse = nd.getProperties();
		Properties overrideProp = SimProperties.specificProperties("Override");
		propToUse.putAll(overrideProp);
		retValue.injectProperties(nd.getProperties());
		retValue.setInternalNeuralNetwork(nd);
		retValue.setName(nd.toString());
		return retValue;
	}
}