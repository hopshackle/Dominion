package hopshackle.dominion.test;

import static org.junit.Assert.*;

import java.util.Stack;

import hopshackle.dominion.*;
import hopshackle.simulation.Action;
import hopshackle.simulation.Decider;
import hopshackle.simulation.SimProperties;

import org.junit.*;

public class ActionStackTest {

	private Player player;
	private TestGame game, newGame;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "NONE");
		game = new TestGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		player = game.getCurrentPlayer();
	}

	@Test
	public void actionStackIsClonedProperly() {
		CardTypeAugment actionEnum = CardTypeAugment.discardCard(CardType.COPPER);
		DominionAction action = new DominionAction(player, actionEnum);
		game.addToStack(action);
		newGame = game.clone(player);
		assertEquals(game.getActionStack().size(), 1);
		assertEquals(newGame.getActionStack().size(), 1);
		assertFalse(newGame.getActionStack().pop() == action);
	}
	
	@Test
	public void cellarFollowUpActionNotAddedToQueueUntilNeeded() {
		player.setState(Player.State.PLAYING);
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CELLAR));
		Action<Player> da = new DominionAction(player, CardTypeAugment.playCard(CardType.CELLAR));
		assertEquals(game.getActionStack().size(), 0);
		assertTrue(player.getActionPlan().getNextAction() == null);
		da.getFollowOnAction();
		assertEquals(game.getActionStack().size(), 0);
		assertTrue(player.getActionPlan().getNextAction() == null);
		da.addToAllPlans();
		assertEquals(game.getActionStack().size(), 0);
		assertTrue(player.getActionPlan().getNextAction() == da);
		da.start();
		da.run();
		assertEquals(game.getActionStack().size(), 0);
		assertTrue(player.getActionPlan().getNextAction() == null);
	}
}

class TestGame extends DominionGame {

	public TestGame(DeciderGenerator deciderGen, String name, boolean paceSetters) {
		super(deciderGen, name, paceSetters);
	}
	public TestGame(TestGame master) {
		super(master);
	}
	
	public Stack<Action<Player>> getActionStack() {
		return actionStack;
	}
	public void addToStack(Action<Player> a) {
		actionStack.add(a);
	}
	@Override
	public TestGame clone(Player p) {
		// cloning ignores the perspective player, and any shuffling
		// to take account of information set
		return new TestGame(this);
	}

}
