package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.CardFactory;
import hopshackle.dominion.CardType;
import hopshackle.dominion.CardValuationVariables;
import hopshackle.dominion.DeciderGenerator;
import hopshackle.dominion.DominionBuyAction;
import hopshackle.dominion.DominionLookaheadFunction;
import hopshackle.dominion.DominionStateFactory;
import hopshackle.dominion.Game;
import hopshackle.dominion.GameSetup;
import hopshackle.dominion.Player;
import hopshackle.dominion.PositionSummary;
import hopshackle.dominion.RunGame;
import hopshackle.simulation.Action;
import hopshackle.simulation.AgentEvent;
import hopshackle.simulation.EventFilter;
import hopshackle.simulation.ExperienceRecordCollector;
import hopshackle.simulation.OnInstructionTeacher;
import hopshackle.simulation.SimProperties;
import hopshackle.simulation.StandardERFactory;
import hopshackle.simulation.basic.BasicAgent;

import org.junit.Before;
import org.junit.Test;

public class LearningAndExperience {
	
	private Game game;
	private DominionStateFactory stateFactory;
	private DeciderGenerator dg;
	private DominionLookaheadFunction lookahead = new DominionLookaheadFunction();
	
	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		dg = new DeciderGenerator(new GameSetup(), 1, 1, 0, 0);
		game = new Game(new RunGame("Test", 1, dg), false);
		stateFactory = new DominionStateFactory(dg.getPurchaseDecider(false).getVariables());
	}

	@Test
	public void playingCardsDoesNotGenerateExperienceRecordsWithFilter() {
		EventFilter purchaseEventFilter = new EventFilter() {
			@Override
			public boolean ignore(AgentEvent event) {
				Action<?> action = event.getAction();
				if (action == null || action instanceof DominionBuyAction)
					return false;
				return true;
			}
		};
		ExperienceRecordCollector<Player> erc = new ExperienceRecordCollector<Player>(new StandardERFactory<Player>(), purchaseEventFilter);
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
	
	@Test
	public void playingCardsDoesGenerateExperienceRecordsWithoutFilter() {
		ExperienceRecordCollector<Player> erc = new ExperienceRecordCollector<Player>(new StandardERFactory<Player>());
		OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>();
		for (Player p : game.getPlayers()) {
			erc.registerAgent(p);
		}
		teacher.registerToERStream(erc);
		Player firstPlayer = game.getCurrentPlayer();
		firstPlayer.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.SMITHY));
		assertEquals(erc.getExperienceRecords(firstPlayer).size(), 0);
		firstPlayer.takeActions();
		assertEquals(erc.getExperienceRecords(firstPlayer).size(), 1);
		firstPlayer.buyCards();
		assertEquals(erc.getExperienceRecords(firstPlayer).size(), 2);
	}
	
	@Test
	public void positionSummaryApplyReturnsUpdatedResult() {
		Player firstPlayer = game.getCurrentPlayer();
		firstPlayer.setState(Player.State.PURCHASING);
		PositionSummary ps = (PositionSummary) stateFactory.getCurrentState(firstPlayer);
		PositionSummary updatedps = ps.apply(CardType.MILITIA);
		assertFalse(ps == updatedps);
		assertEquals(CardValuationVariables.MILITIA_PERCENT.getValue(ps), 0.0, 0.001);
		assertEquals(CardValuationVariables.MILITIA_PERCENT.getValue(updatedps), 5.0/11.0 , 0.001);
		double[] psArray = ps.getAsArray();
		double[] updatedpsArray = updatedps.getAsArray();
		boolean identical = true;
		for (int i = 0; i < psArray.length; i++) {
			if (psArray[i] != updatedpsArray[i])
				identical = false;
		}
		assertFalse(identical);
	}

	@Test
	public void lookaheadApplyReturnsUpdatedResult() {
		Player firstPlayer = game.getCurrentPlayer();
		firstPlayer.setState(Player.State.PURCHASING);
		PositionSummary ps = (PositionSummary) stateFactory.getCurrentState(firstPlayer);
		PositionSummary updatedps = (PositionSummary) lookahead.apply(ps, CardType.MILITIA);
		assertFalse(ps == updatedps);
		assertEquals(CardValuationVariables.MILITIA_PERCENT.getValue(ps), 0.0, 0.001);
		assertEquals(CardValuationVariables.MILITIA_PERCENT.getValue(updatedps), 5.0/11.0 , 0.001);
		double[] psArray = ps.getAsArray();
		double[] updatedpsArray = updatedps.getAsArray();
		boolean identical = true;
		for (int i = 0; i < psArray.length; i++) {
			if (psArray[i] != updatedpsArray[i])
				identical = false;
		}
		assertFalse(identical);
	}

}
