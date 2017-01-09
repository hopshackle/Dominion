package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.basecards.Militia;
import hopshackle.simulation.*;

import java.util.*;

import org.junit.*;

public class CloneTests {

	public DominionGame game;
	TestDominionDecider woodcutterDecider, workshopDecider;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		SimProperties.setProperty("DominionMCTSDeciderProportion", "0.0");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		woodcutterDecider = TestDominionDecider.getExample(CardType.WOODCUTTER);
		workshopDecider = TestDominionDecider.getExample(CardType.WORKSHOP);
	}

	@Test
	public void cloneGame() {
		// I want to clone a game, and then run the game through to completion, and confirm the original is unaffected
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();

		List<List<CardType>> hand = new ArrayList<List<CardType>>();
		List<List<CardType>> deck = new ArrayList<List<CardType>>();
		List<List<CardType>> discard = new ArrayList<List<CardType>>();
		int[] deckSize = new int[4];
		int[] discardSize = new int[4];
		for (int i = 1; i <=4; i++) {
			Player player = game.getPlayer(i);
			assertEquals(player.getPositionSummaryCopy().getNumberOfCardsRemaining(CardType.PROVINCE), 12.0, 0.001);
			hand.add(player.getCopyOfHand());
			discard.add(player.getCopyOfDiscard());
			deck.add(player.getCopyOfDeck());
			deckSize[i-1] = player.getDeckSize();
			discardSize[i-1] = player.getDiscardSize();
		}
		Map<CardType, Integer> initialCardsAvailable = new HashMap<CardType, Integer>();
		for (CardType ct : game.availableCardsToPurchase()) {
			initialCardsAvailable.put(ct, game.getNumberOfCardsRemaining(ct));
		}
		DominionGame clonedGame = game.clone(game.getCurrentPlayer());
		int perspectivePlayer = game.getCurrentPlayerNumber();
		assertEquals(game.getCurrentPlayerNumber(), clonedGame.getCurrentPlayerNumber());

		assertEquals(clonedGame.turnNumber(), 3);
		assertEquals(game.turnNumber(), 3);

		for (int i = 1; i <=4; i++) {
			Player player = game.getPlayer(i);
			Player cloned = clonedGame.getPlayer(i);
			assertEquals(cloned.getPositionSummaryCopy().getNumberOfCardsRemaining(CardType.PROVINCE), 12.0, 0.001);
			// Test that players are different (i.e. we must clone these as well)
			assertFalse(player == cloned);
			// Test that perspective player's hand and discard are still the same
			List<CardType> clonedHand = cloned.getCopyOfHand();
			List<CardType> clonedDiscard = cloned.getCopyOfDiscard();
			List<CardType> clonedDeck = cloned.getCopyOfDeck();
			// Test that players discards are still the same
			assertEquals(discardSize[i-1], cloned.getDiscardSize());
			// Test that players' decks are still the same
			assertEquals(deckSize[i-1], cloned.getDeckSize());
			if (i == perspectivePlayer) {
				for (int j = 0; j < hand.get(i-1).size(); j++) {
					assertTrue(hand.get(i-1).get(j) == clonedHand.get(j));
				}	
				for (int j = 0; j < discard.get(i-1).size(); j++) {
					assertTrue(discard.get(i-1).get(j) == clonedDiscard.get(j));
				}	
				boolean identical = true;
				for (int j = 0; j < deck.get(i-1).size(); j++) {
					if (deck.get(i-1).get(j) != clonedDeck.get(j)) identical = false;
				}
				assertFalse(identical);
			} else {
				boolean identical = true;
				for (int j = 0; j < hand.get(i-1).size(); j++) {
					if (hand.get(i-1).get(j) != clonedHand.get(j)) identical = false;
				}
				assertFalse(identical);
				for (int j = 0; j < discard.get(i-1).size(); j++) {
					assertTrue(discard.get(i-1).get(j) == clonedDiscard.get(j));
				}	
				identical = true;
				for (int j = 0; j < deck.get(i-1).size(); j++) {
					if (deck.get(i-1).get(j) != clonedDeck.get(j)) identical = false;
				}
				assertFalse(identical);
			}
		}

		clonedGame.playGame();
		assertTrue(clonedGame.gameOver());
		assertFalse(game.gameOver());
		assertEquals(game.getPlayerInOrdinalPosition(1), 0);

		// Test we still have all the cards on the table
		for (CardType ct : initialCardsAvailable.keySet()) {
			assertEquals((int)initialCardsAvailable.get(ct), game.getNumberOfCardsRemaining(ct));
		}
		// And that the players still have exactly the same decks, hands and discards
		for (int i = 1; i <=4; i++) {
			Player player = game.getPlayer(i);
			List<CardType> finalHand = player.getCopyOfHand();
			List<CardType> finalDiscard = player.getCopyOfDiscard();
			List<CardType> finalDeck = player.getCopyOfDeck();
			// Test that players discards are still the same
			assertEquals(discardSize[i-1], player.getDiscardSize());
			// Test that players' decks are still the same
			assertEquals(deckSize[i-1], player.getDeckSize());
			for (int j = 0; j < hand.get(i-1).size(); j++) {
				assertTrue(hand.get(i-1).get(j) == finalHand.get(j));
			}
			for (int j = 0; j < discard.get(i-1).size(); j++) {
				assertTrue(discard.get(i-1).get(j) == finalDiscard.get(j));
			}	
			for (int j = 0; j < deck.get(i-1).size(); j++) {
				assertTrue(deck.get(i-1).get(j) == finalDeck.get(j));
			}
		}
	}

	@Test
	public void cloneGameAfterPlayingBeforeBuying() {
		Player p1 = game.getCurrentPlayer();
		assertEquals(game.getCurrentPlayerNumber(), 1);
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		assertEquals(p1.getCopyOfHand().size(), 6);
		p1.setDecider(woodcutterDecider);
		int oldBudget = p1.getBudget();
		assertEquals(p1.getBuys(), 1);
		game.oneAction();
		// Player has now played Woodcutter, leaving +1 Buy, and +2 purchasing power

		DominionGame clonedGame = game.clone(p1);
		assertEquals(clonedGame.getCurrentPlayerNumber(), 1);

		Player newPlayer1 = clonedGame.getPlayer(1);
		assertEquals(newPlayer1.getBudget(), oldBudget+2);
		assertEquals(newPlayer1.getBuys(), 2);
		assertEquals(newPlayer1.getCopyOfHand().size(), 5);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void cloneGameWhilePlaying() {
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(workshopDecider);
		p1.insertCardDirectlyIntoHand(new Card(CardType.WORKSHOP));
		p1.decide();
		Action<Player> nextAction = (Action<Player>) p1.getNextAction();
		assertTrue(nextAction.getType().getEnum() == CardType.WORKSHOP);

		nextAction.start();

		DominionGame clonedGame = game.clone(p1);
		assertEquals(clonedGame.getCurrentPlayerNumber(), 1);

		Player newPlayer1 = clonedGame.getPlayer(1);
		Action<Player> nextClonedAction = (Action<Player>) newPlayer1.getNextAction();
		assertTrue(nextClonedAction != null);
		assertTrue(nextClonedAction.getActor() == newPlayer1);
		assertTrue(nextAction.getType().getEnum() == CardType.WORKSHOP);
		assertTrue(nextClonedAction != nextAction);

		// clone a second time
		clonedGame = game.clone(p1);
		newPlayer1 = clonedGame.getPlayer(1);
		nextClonedAction = (Action<Player>) newPlayer1.getNextAction();
		assertTrue(nextClonedAction != null);
		assertTrue(nextClonedAction.getActor() == newPlayer1);
		assertTrue(nextAction.getType().getEnum() == CardType.WORKSHOP);
		assertTrue(nextClonedAction != nextAction);
	}
	
	@Test
	public void cloneOfAttackCard() {
		// I want to create an attack card, get it to mid-execution, and then clone it to a new Game
		Player p1 = game.getCurrentPlayer();
		DominionGame clonedGame = game.clone(p1);

		AttackCard militia = (AttackCard) CardFactory.instantiateCard(CardType.MILITIA);
		List<ActionEnum<Player>> defence = militia.takeAction(p1);
		assertFalse(defence.isEmpty());
		assertTrue(militia.nextActor() == game.getPlayer(2));
		DominionAction followOn = militia.followUpAction();	
		assertTrue(followOn != null);
		
		AttackCard clonedMilitia = militia.clone(clonedGame);
		assertTrue(clonedMilitia.nextActor() == clonedGame.getPlayer(2));
		assertTrue(clonedMilitia.nextActor() != militia.nextActor());
		DominionAction clonedFollowOn = clonedMilitia.followUpAction();	
		assertTrue(clonedFollowOn != null);
		assertTrue(clonedFollowOn.getActor() == clonedGame.getPlayer(1));
	}
	
	@Test
	public void cloneOfAttackCardAsPartOfGameClone() {
		Player p1 = game.getCurrentPlayer();
		AttackCard militia = (AttackCard) CardFactory.instantiateCard(CardType.MILITIA);
		p1.insertCardDirectlyIntoHand(militia);
		game.oneAction(false, true);

		DominionGame clonedGame = game.clone(p1);
		Player newPlayer = clonedGame.getPlayer(1);
		Card newCard = newPlayer.getCardLastPlayed();
		assertTrue(newCard instanceof Militia);
		AttackCard clonedMilitia = (AttackCard) newCard;
		assertTrue(clonedMilitia.nextActor() == clonedGame.getPlayer(2));
		assertTrue(clonedMilitia.nextActor() != militia.nextActor());
		DominionAction clonedFollowOn = clonedMilitia.followUpAction();	
		assertTrue(clonedFollowOn != null);
		assertTrue(clonedFollowOn.getActor() == clonedGame.getPlayer(1));

	}

}
