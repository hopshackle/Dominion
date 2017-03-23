package hopshackle.dominion.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.dominion.basecards.Bureaucrat;
import hopshackle.simulation.*;

import org.junit.*;

public class ActionStackTest {

	private Player player;
	private TestGame game, newGame;

	@Before
	public void setUp() throws Exception {
		DeciderProperties localProp = SimProperties.getDeciderProperties("GLOBAL");
		localProp.setProperty("DeciderType", "NN");
		localProp.setProperty("DominionCardSetup", "FirstGame");
		localProp.setProperty("DominionHardCodedActionDecider", "true");
		game = new TestGame(new DeciderGenerator(new GameSetup(), localProp), "Test",  false);
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
	public void actionStackIsClonedProperlyWithAttributes() {
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		game.oneAction(false, true);	// executes the play of the militia, but not the following defence
		newGame = game.clone(player);
		assertEquals(game.getActionStack().size(), 1);
		assertEquals(newGame.getActionStack().size(), 1);
		DominionAction clonedAction = (DominionAction) newGame.getActionStack().peek();
		DominionAction oldAction = (DominionAction) game.getActionStack().peek();
		assertTrue(oldAction.getNextActor() == game.getPlayer(2));
		assertFalse(oldAction.getNextOptions().isEmpty());
		assertTrue(oldAction.getFollowOnAction() != null);
		assertTrue(clonedAction.getNextActor() == newGame.getPlayer(2));
		assertFalse(clonedAction.getNextOptions().isEmpty());
		assertTrue(clonedAction.getFollowOnAction() != null);
		
		newGame.oneAction(false, true);		// first defence - looks at nextOptions from stack, decides and executes.
											// Then pops FollowOnAction off the stack, and executes it, putting new Follow-On
											// Action on the stack with options for P3
		assertEquals(newGame.getActionStack().size(), 1);
		Action<Player> action = newGame.getActionStack().peek();
		assertTrue(action.getNextActor() == newGame.getPlayer(3));
		assertTrue(action.getActor() == newGame.getPlayer(1));
		assertFalse(action.getNextOptions().isEmpty());
		
		game = newGame.clone(newGame.getPlayer(3));
		clonedAction = (DominionAction) game.getActionStack().peek();
		assertTrue(clonedAction.getNextActor() == game.getPlayer(3));
		assertTrue(clonedAction.getActor() == game.getPlayer(1));
		assertFalse(clonedAction.getNextOptions().isEmpty());
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
	
	
	@Test
	public void bureaucratGivesOpponentOptionWithTwoTypesOfVictoryCard() {
		Player p2 = game.getPlayer(2);
		Player p3 = game.getPlayer(3);
		p2.insertCardDirectlyIntoHand(new Bureaucrat());
		p3.insertCardDirectlyIntoHand(new Card(CardType.DUCHY));
		p3.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		assertEquals(p3.getHandSize(), 7);
		game.nextPlayersTurn();
		game.oneAction(false, true); 	// this should execute attack on p3, and leave options
		Action<Player> nextAction = game.getActionStack().get(0);
		assertEquals(nextAction.getNextOptions().size(), 2);
		assertTrue(nextAction.getNextActor() == p3);
		game.nextPlayersTurn();
		assertEquals(p3.getHandSize(), 6);
	}
	
	@Test
	public void multipleItemsOnStackPoppedUntilFollowOnActionFound() {
		Player p1 = game.getPlayer(1);
		game.addToStack(CardTypeAugment.discardCard(CardType.COPPER).getAction(p1));
		game.addToStack(CardTypeAugment.discardCard(CardType.COPPER).getAction(p1));
		assertEquals(game.getActionStack().size(),2);
		game.oneAction(false, true);
		assertEquals(game.getActionStack().size(),0);
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
