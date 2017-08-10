package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.simulation.*;

import java.util.*;

import org.junit.*;
public class MonteCarloTreeUpdates {
	
	DominionGame game;
	DeciderGenerator dg;
	MonteCarloTree<Player> tree;
	PositionSummary startState;
	List<ActionEnum<Player>> actionList;
	List<CardType> copperMarket;

	@Before
	public void setUp() throws Exception {
		DeciderProperties localProp = SimProperties.getDeciderProperties("GLOBAL");
		localProp.setProperty("DominionCardSetup", "FirstGame");
		localProp.setProperty("DominionHardCodedActionDecider", "true");
		dg = new DeciderGenerator(new GameSetup(), localProp);
		game = new DominionGame(dg, "Test", false);
		tree = new MonteCarloTree<Player>(localProp);
		startState = game.getCurrentPlayer().getPositionSummaryCopy();
		startState.setVariables(dg.getDecider(false).getVariables());
		
		actionList = new ArrayList<ActionEnum<Player>>();
		copperMarket = new ArrayList<CardType>();
		copperMarket.add(CardType.COPPER);
		copperMarket.add(CardType.MARKET);
		actionList.add(new CardTypeList(copperMarket, CardTypeAugment.ChangeType.BUY));
		actionList.add(CardTypeAugment.takeCard(CardType.ADVENTURER));
		actionList.add(CardTypeAugment.takeCard(CardType.COPPER));

	}
	
	@Test
	public void monteCarloTreeSetUpWithCardTypeListActions() {
		tree.insertState(startState);
		tree.updateState(startState, new CardTypeList(copperMarket, CardTypeAugment.ChangeType.BUY), startState, 2.0);
		assertTrue(tree.containsState(startState));
		MCStatistics<Player> stats = tree.getStatisticsFor(startState);
		assertEquals(stats.getMean(new CardTypeList(copperMarket, CardTypeAugment.ChangeType.BUY), 1)[0], 2.0, 0.001);
	}
	
	@Test
	public void cardTypeListActionsReturnCorrectActionEnum() {
		ActionEnum<Player> cmActionEnum = new CardTypeList(copperMarket, CardTypeAugment.ChangeType.BUY);
		Action<Player> cmAction = cmActionEnum.getAction(game.getCurrentPlayer());
		assertTrue(cmActionEnum.toString().equals("Buys COPPER Buys MARKET "));
		assertTrue(cmAction.getType().equals(cmActionEnum));
		assertTrue(cmAction.toString().equals("Buys COPPER Buys MARKET "));
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
