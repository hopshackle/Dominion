package hopshackle.dominion;

import hopshackle.simulation.*;

import java.io.*;
import java.util.ArrayList;

import org.encog.neural.networks.*;

public class ViewBrain {

	public static void main(String[] args) {
		String baseDir = SimProperties.getProperty("BaseDirectory", "C:");

		File f = new File(baseDir + "\\recordedBRains\\FG123_1_DUMMY_DOM1.brain");
		NeuralDecider<Player> nd = NeuralDecider.createNeuralDecider(new DominionStateFactory(new ArrayList<GeneticVariable<Player>>()), f, 100.0);

		BasicNetwork brain = nd.getBrain();

		int count = 0;
		String[] firstRowNames = new String[brain.getInputCount()+1];
		for (GeneticVariable<Player> gv : nd.getVariables()) {
			firstRowNames[count] = gv.toString();
			count++;
		}
		firstRowNames[count] = "Threshold";

		for (int l = 0; l < brain.getLayerCount() - 1; l++) {
			System.out.println("Layer #" + (l) + ". Neurons: " + brain.getLayerNeuronCount(l));
			for (int fromNeuron = 0; fromNeuron < brain.getLayerTotalNeuronCount(l); fromNeuron++) {
				StringBuffer outputString = new StringBuffer(String.format("Row %2d:\t", fromNeuron));
				if (l == 0) {
					String neuronName = firstRowNames[fromNeuron];
					outputString = new StringBuffer(String.format("%-16s\t", neuronName));
				} 
				for (int toNeuron = 0; toNeuron < brain.getLayerNeuronCount(l+1); toNeuron++) {
					outputString.append(String.format("%+.2f\t",brain.getWeight(l, fromNeuron, toNeuron)));
				}
				System.out.println(outputString.toString());
			}

		}
	}
}
