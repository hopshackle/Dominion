package hopshackle.dominion;

import hopshackle.simulation.*;

import java.io.*;
import java.util.*;

public class DominionNeuralDecider extends NeuralLookaheadDecider<Player> {

    public DominionNeuralDecider(List<CardValuationVariables> variables) {
        super(new DominionStateFactory(HopshackleUtilities.convertList(variables)), 100.0);
    }

    @Override
    public void learnFromBatch(ExperienceRecord<Player>[] expArray, double maxResult) {
        super.learnFromBatch(expArray, maxResult);
    }

    @Override
    public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
        super.learnFrom(exp, maxResult);
    }

    public static Decider<Player> createFromFile(File fileName) {
        StateFactory<Player> sf = new DominionStateFactory(new ArrayList<GeneticVariable<Player>>());
        NeuralDecider<Player> nd = NeuralDecider.createFromFile(sf, fileName, true);
        String deciderType = nd.getProperties().getProperty("DeciderType", "NNL");
        DeciderProperties propToUse = nd.getProperties();
        Properties overrideProp = SimProperties.specificProperties("Override");
        propToUse.putAll(overrideProp);
        switch (deciderType) {
            case "NN":
                return nd;
            case "NNL":
                DominionNeuralDecider retValue = new DominionNeuralDecider(nd.getVariables());
                retValue.injectProperties(nd.getProperties());
                retValue.setInternalNeuralNetwork(nd);
                retValue.setName(nd.toString());
                return retValue;
            default:
                throw new AssertionError("Unknown DeciderType in saved file " + deciderType);
        }
    }
}