package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import java.util.*;

import org.junit.*;
public class MonteCarloTreeUpdates {
	
	DominionGame game;
	DeciderGenerator dg;
	MonteCarloTree<Player> tree;
	PositionSummary startState;
	List<ActionEnum<Player>> actionList;
	List<CardType> copperMarket ;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		SimProperties.setProperty("DominionHardCodedActionDecider", "true");
		DominionAction.refresh();
		dg = new DeciderGenerator(new GameSetup(), 1, 1, 0, 0);
		game = new DominionGame(dg, "Test", false);
		tree = new MonteCarloTree<Player>();
		startState = game.getCurrentPlayer().getPositionSummaryCopy();
		startState.setVariables(dg.getPurchaseDecider(false).getVariables());
		
		actionList = new ArrayList<ActionEnum<Player>>();
		copperMarket = new ArrayList<CardType>();
		copperMarket.add(CardType.COPPER);
		copperMarket.add(CardType.MARKET);
		actionList.add(new CardTypeList(copperMarket));
		actionList.add(CardTypeAugment.takeCard(CardType.ADVENTURER));
		actionList.add(CardTypeAugment.takeCard(CardType.COPPER));

	}
	
	@Test
	public void monteCarloTreeSetUpWithCardTypeListActions() {
		tree.insertState(startState, actionList);
		tree.updateState(startState, new CardTypeList(copperMarket), startState, 2.0);
		assertTrue(tree.containsState(startState));
		MCStatistics<Player> stats = tree.getStatisticsFor(startState);
		assertEquals(stats.getMean(new CardTypeList(copperMarket)), 2.0, 0.001);
	}
	
	@Test
	public void cardTypeListActionsReturnCorrectActionEnum() {
		ActionEnum<Player> cmActionEnum = new CardTypeList(copperMarket);
		Action<Player> cmAction = cmActionEnum.getAction(game.getCurrentPlayer());
		assertTrue(cmActionEnum.toString().equals("Gains COPPER Gains MARKET "));
		assertTrue(cmAction.getType().equals(cmActionEnum));
		assertTrue(cmAction.toString().equals("Gains COPPER Gains MARKET "));
	}
	
	@Test
	public void settingPurchaseDeciderOnClonedPlayerDoesNotAffectParent() {
		Player p1 = game.getCurrentPlayer();
		p1.takeActions();
		Decider<Player> p1Decider = p1.getDecider();
		Game<Player, CardTypeAugment> clonedGame = game.clone(p1);
		Player p2 = clonedGame.getCurrentPlayer();
		assertTrue(p2.getDecider() == p1.getDecider());
		p2.setDecider(new HardCodedDecider<Player>(CardTypeAugment.takeCard(CardType.ADVENTURER)));
		assertFalse(p2.getDecider() == p1.getDecider());
		assertTrue(p1.getDecider() == p1Decider);
	}

}
