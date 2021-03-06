package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.simulation.*;

import org.junit.Before;
import org.junit.Test;

public class LearningAndExperience {
	
	private DominionGame game;
	private DominionStateFactory stateFactory;
	private DeciderGenerator dg;
	DeciderProperties localProp;
	
	@Before
	public void setUp() throws Exception {
		localProp = SimProperties.getDeciderProperties("GLOBAL");
		localProp.setProperty("DominionCardSetup", "FirstGame");
		localProp.setProperty("DominionHardCodedActionDecider", "false");
		dg = new DeciderGenerator("", new GameSetup(), localProp);
		game = new DominionGame(dg, "Test", false);
		stateFactory = new DominionStateFactory(dg.getDecider(false).getVariables());
	}

	@Test
	public void playingCardsDoesNotGenerateExperienceRecordsWithFilter() {
		EventFilter purchaseEventFilter = new EventFilter() {
			@Override
			public boolean ignore(AgentEvent event) {
				DominionAction action = (DominionAction) event.getAction();
				if (action == null || !action.isAction())
					return false;
				return true;
			}
		};
		ExperienceRecordCollector<Player> erc = new ExperienceRecordCollector<Player>(new StandardERFactory<Player>(localProp), purchaseEventFilter);
		OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>();
		for (Player p : game.getAllPlayers()) {
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
		ExperienceRecordCollector<Player> erc = new ExperienceRecordCollector<Player>(new StandardERFactory<Player>(localProp));
		OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>();
		for (Player p : game.getAllPlayers()) {
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
		PositionSummary updatedps = ps.apply(CardTypeAugment.takeCard(CardType.MILITIA));
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
		PositionSummary updatedps = ps.apply(CardTypeAugment.takeCard(CardType.MILITIA));
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
