package hopshackle.dominion;

import java.util.List;
import hopshackle.simulation.*;

public class RunMCTSGame {

	public static void main(String[] args) {
		
		GameSetup gamesetup = new GameSetup();
		// GeneticProperties.txt will need to specify that we have 100% BigMoneyPacesetters
		// Then I can manually override the decider on player 1 to be MCTS
		DeciderGenerator dg = new DeciderGenerator(gamesetup, 1, 0, 0, 0);
		
		DominionGame game = new DominionGame(dg, "MCTSSingleGame", true);
		Player firstPlayer = game.getPlayer(1);
		List<CardValuationVariables> varList = gamesetup.getDeckVariables();
		MCTSMasterDecider<Player> mctsDecider = new MCTSMasterDominion(varList, dg.completeHeuristic, dg.completeHeuristic);
		firstPlayer.setDecider(mctsDecider);
		game.playGame();
	}
	
}
