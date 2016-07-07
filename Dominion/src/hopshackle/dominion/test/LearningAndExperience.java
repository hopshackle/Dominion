package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.CardFactory;
import hopshackle.dominion.CardType;
import hopshackle.dominion.DeciderGenerator;
import hopshackle.dominion.Game;
import hopshackle.dominion.GameSetup;
import hopshackle.dominion.Player;
import hopshackle.dominion.RunGame;
import hopshackle.simulation.ExperienceRecordCollector;
import hopshackle.simulation.OnInstructionTeacher;
import hopshackle.simulation.SimProperties;

import org.junit.Before;
import org.junit.Test;

public class LearningAndExperience {
	
	private Game game;
	
	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		game = new Game(new RunGame("Test", 1, new DeciderGenerator(new GameSetup(), 1, 1, 0, 0)));
	}

	@Test
	public void playingCardsDoesNotYetGenerateExperienceRecords() {
		ExperienceRecordCollector<Player> erc = new ExperienceRecordCollector<Player>();
		OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>();
		for (Player p : game.getPlayers()) {
			erc.registerAgent(p);
		}
		teacher.registerToERStream(erc);
		Player firstPlayer = game.getCurrentPlayer();
		firstPlayer.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.SMITHY));
		assertEquals(erc.getExperienceRecords(firstPlayer).size(), 0);
		firstPlayer.takeActions();
		assertEquals(erc.getExperienceRecords(firstPlayer).size(), 0);
		firstPlayer.buyCards();
		assertEquals(erc.getExperienceRecords(firstPlayer).size(), 1);
	}

}
