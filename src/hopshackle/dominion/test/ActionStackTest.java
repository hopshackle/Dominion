package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.basecards.Cellar;
import hopshackle.simulation.Decider;
import hopshackle.simulation.SimProperties;

import org.junit.*;

public class ActionStackTest {

	private TestPlayer player;
	private TestGame game, newGame;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "NONE");
		game = new TestGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		player = new TestPlayer(game, 1);
	}

	@Test
	public void actionStackIsClonedProperly() {
		newGame = new TestGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		CardTypeAugment actionEnum = CardTypeAugment.discardCard(CardType.COPPER);
		DominionAction action = new DominionAction(player, actionEnum);
		player.addActionToStack(action);
		TestPlayer clone = (TestPlayer) player.clone(newGame);

		assertEquals(clone.actionStackSize(), 1);
		assertFalse(clone.getTopActionOnStack() == action);
	}
	
	@Test
	public void actionStackAndActionQueueClonedInSynch() {
		newGame = new TestGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		CardTypeAugment actionEnum = CardTypeAugment.discardCard(CardType.COPPER);
		DominionAction action = new DominionAction(player, actionEnum);
		player.addActionToStack(action);
		player.getActionPlan().addAction(action);
		assertTrue(player.getActionPlan().getNextAction() == action);
		TestPlayer clone = (TestPlayer) player.clone(newGame);

		assertEquals(clone.actionStackSize(), 1);
		assertFalse(clone.getTopActionOnStack() == action);
		assertTrue(clone.getTopActionOnStack() == clone.getActionPlan().getNextAction());
	}
	
	@Test
	public void cellarFollowUpActionNotAddedToQueueUntilNeeded() {
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CELLAR));
		DominionAction da = new DominionAction(player, CardTypeAugment.playCard(CardType.CELLAR));
		assertEquals(player.actionStackSize(), 0);
		assertTrue(player.getActionPlan().getNextAction() == null);
		da.getFollowOnAction();
		assertEquals(player.actionStackSize(), 0);
		assertTrue(player.getActionPlan().getNextAction() == null);
		da.addToAllPlans();
		assertEquals(player.actionStackSize(), 0);
		assertTrue(player.getActionPlan().getNextAction() == da);
		da.start();
		da.run();
		assertEquals(player.actionStackSize(), 0);
		assertTrue(player.getActionPlan().getNextAction() == null);
	}

}

class TestPlayer extends Player {
	public TestPlayer(DominionGame game, int number) {
		super(game, number);
	}
	public TestPlayer(TestPlayer testPlayer, DominionGame newGame) {
		super(testPlayer, newGame);
	}
	public void addActionToStack(DominionAction action) {
		actionStack.add(action);
	}
	public DominionAction getTopActionOnStack() {
		return actionStack.peek();
	}
	@Override
	public TestPlayer clone(DominionGame newGame) {
		return new TestPlayer(this, newGame);
	}
}
class TestGame extends DominionGame {

	public TestGame(DeciderGenerator deciderGen, String name, boolean paceSetters) {
		super(deciderGen, name, paceSetters);
		for (int n = 0; n < players.length; n++) {
			Decider<Player> deciderToUse = players[n].getDecider();
			players[n] = new TestPlayer(this, n+1);
			players[n].setDecider(deciderToUse);
			players[n].setGame(this);
		}
		for (int n = 0; n < players.length; n++)
			players[n].refreshPositionSummary();
	}

}
