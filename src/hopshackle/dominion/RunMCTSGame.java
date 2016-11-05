package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class RunMCTSGame {

	public static void main(String[] args) {
		
		GameSetup gamesetup = new GameSetup();
		// GeneticProperties.txt will need to specify that we have 100% BigMoneyPacesetters
		// Then I can manually override the decider on player 1 to be MCTS
		DeciderGenerator dg = new DeciderGenerator(gamesetup, 1, 0, 0, 0);
		
		DominionGame game = new DominionGame(dg, "MCTSSingleGame", false);
		Player firstPlayer = game.getPlayer(1);
		List<CardValuationVariables> varList = gamesetup.getDeckVariables();
		List<CardType> actionsToUse = gamesetup.getCardTypes();
		DominionStateFactory stateFactory = new DominionStateFactory(HopshackleUtilities.convertList(varList));
		MCTSMasterDecider mctsDecider = new MCTSMasterDecider<Player>(stateFactory, actionsToUse, dg.bigMoney, dg.bigMoney);
	//	firstPlayer.setPositionDecider(mctsDecider);
		game.nextPlayersTurn();
	}
	
}
