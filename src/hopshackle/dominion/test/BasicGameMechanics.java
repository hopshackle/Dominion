package hopshackle.dominion.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.simulation.SimProperties;

import org.junit.*;

import com.sun.media.jfxmedia.events.PlayerStateEvent.PlayerState;

public class BasicGameMechanics {

	public DominionGame game;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
	}

	@Test
	public void newGameHasFourPlayer() {
		assertEquals(game.getAllPlayers().size(), 4);
		for (int n = 0; n < 4; n++) {
			assertTrue(game.getAllPlayers().get(n) != null && game.getAllPlayers().get(n) instanceof Player);
		}
	}

	@Test
	public void newPlayerHasHandOfFiveCardsAndDeckOfFiveCards() {
		for (int n = 0; n < 4; n++) {
			Player p = game.getAllPlayers().get(n);
			assertEquals(p.getHandSize(), 5);
			assertEquals(p.getDeckSize(), 5);
		}
	}
	
	@Test
	public void tenOfEachKingdomCardTwelveOfVictory() {
		assertEquals(game.getNumberOfCardsRemaining(CardType.MARKET), 10);
		assertEquals(game.getNumberOfCardsRemaining(CardType.SMITHY), 10);
		assertEquals(game.getNumberOfCardsRemaining(CardType.PROVINCE), 12);
		assertEquals(game.getNumberOfCardsRemaining(CardType.DUCHY), 12);
		assertEquals(game.getNumberOfCardsRemaining(CardType.ESTATE), 12);
	}

	@Test
	public void newPlayerHasSevenCopperAndThreeEstatesSpreadAcrossHandAndDeck() {
		for (int n = 0; n < 4; n++) {
			Player p = game.getAllPlayers().get(n);
			assertEquals(p.getNumberOfTypeTotal(CardType.COPPER), 7);
			assertEquals(p.getNumberOfTypeTotal(CardType.ESTATE), 3);
		}
	}

	@Test
	public void playersTakeTurnsInSequence() {
		Player[] players = game.getAllPlayers().toArray(new Player[1]);
		assertTrue(players[0] == game.getCurrentPlayer());
		assertEquals(game.turnNumber(), 1);
		game.nextPlayersTurn();
		assertTrue(players[1] == game.getCurrentPlayer());
		assertEquals(game.turnNumber(), 1);
		game.nextPlayersTurn();
		assertTrue(players[2] == game.getCurrentPlayer());
		assertEquals(game.turnNumber(), 1);
		game.nextPlayersTurn();
		assertTrue(players[3] == game.getCurrentPlayer());
		assertEquals(game.turnNumber(), 1);
		game.nextPlayersTurn();
		assertTrue(players[0] == game.getCurrentPlayer());
		assertEquals(game.turnNumber(), 2);
		game.nextPlayersTurn();
		assertTrue(players[1] == game.getCurrentPlayer());
		assertEquals(game.turnNumber(), 2);
		game.nextPlayersTurn();
	}

	@Test
	public void	gameEndsWhenProvincesAreEnhausted() {
		for (int n = 0; n < 12; n++) {
			assertFalse(game.gameOver());
			assertTrue(game.drawCard(CardType.PROVINCE));
		}
		assertFalse(game.drawCard(CardType.PROVINCE));
		assertTrue(game.gameOver());
	}

	@Test
	public void gameEndsWhenThreeTypesOfCardAreExhausted() {
		for (int n = 0; n < 12; n++) {
			assertTrue(game.drawCard(CardType.DUCHY));
		}
		assertFalse(game.gameOver());
		for (int n = 0; n < 30; n++) {
			assertTrue(game.drawCard(CardType.CURSE));
		}
		assertFalse(game.gameOver());
		for (int n = 0; n < 12; n++) {
			assertTrue(game.drawCard(CardType.ESTATE));
		}
		assertTrue(game.gameOver());
	}
	
	@Test
	public void notPossibleToPurchaseCardOnceTheyAreAllGone() {
		for (int n = 0; n < 12; n++) {
			assertTrue(game.availableCardsToPurchase().contains(CardType.PROVINCE));
			assertTrue(game.drawCard(CardType.PROVINCE));
		}
		assertFalse(game.availableCardsToPurchase().contains(CardType.PROVINCE));
	}
	
	@Test
	public void whenDeckIsEmptyShuffleDiscardPile() {
		Player p1 = game.getCurrentPlayer();
		p1.tidyUp();
		assertEquals(p1.getDeckSize(), 0);
		assertEquals(p1.getDiscardSize(), 5);
		assertEquals(p1.getHandSize(), 5);
		p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		assertEquals(p1.getDeckSize(), 0);
		assertEquals(p1.getDiscardSize(), 6);
		assertEquals(p1.getHandSize(), 5);
		p1.tidyUp();
		assertEquals(p1.getDeckSize(), 6);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(p1.getHandSize(), 5);
	}

	@Test
	public void phaseFlagIsSetCorrectlyAsPlayProceeds() {
		List<Player> players = game.getAllPlayers();
		for (int i = 0; i<4; i++) {
		assertTrue(players.get(i).isTakingActions());
		}
		players.get(0).takeActions();
		assertFalse(players.get(0).isTakingActions());
		players.get(0).buyCards();
		assertTrue(players.get(0).isTakingActions());
	}
	
	@Test
	public void correctDeciderIsReturnedBasedOnPhase() {
		Player firstPlayer = game.getCurrentPlayer();
		firstPlayer.setState(Player.State.PURCHASING);
		assertFalse(firstPlayer.getDecider() instanceof HardCodedActionDecider);
		firstPlayer.setState(Player.State.PLAYING);
		assertTrue(firstPlayer.getDecider() instanceof HardCodedActionDecider);
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
		assertEquals(game.getWinningPlayers().length, 0);
		
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
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		assertEquals(p1.getCopyOfHand().size(), 6);

		int oldBudget = p1.getBudget();
		assertEquals(p1.getBuys(), 1);
		p1.takeActions();
		// Player has now played Woodcutter, leaving +1 Buy, and +2 purchasing power
		
		DominionGame clonedGame = game.clone(p1);
		assertEquals(clonedGame.getCurrentPlayerNumber(), 1);
		
		Player newPlayer1 = clonedGame.getPlayer(1);
		assertEquals(newPlayer1.getBudget(), oldBudget+2);
		assertEquals(newPlayer1.getBuys(), 2);
		assertEquals(newPlayer1.getCopyOfHand().size(), 5);
	}
	
	@Test
	public void nextActionTakesIntoAccountCurrentState() {
		TestDominionDecider copperDecider = TestDominionDecider.getExample(CardType.COPPER);
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(copperDecider);
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		assertEquals(p1.getCopyOfHand().size(), 7);
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		p1.takeActions();
		game.nextPlayersTurn();
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 9);
	}
	
	@Test
	public void nextActionTakesIntoAccountCurrentStateAfterCloning() {
		TestDominionDecider copperDecider = TestDominionDecider.getExample(CardType.COPPER);
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(copperDecider);
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		p1.takeActions();
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		assertEquals(p1.getBuys(), 2);
		
		DominionGame clonedGame = game.clone(p1);
		Player newP1 = clonedGame.getCurrentPlayer();
		assertEquals(newP1.getBuys(), 2);
		assertEquals(clonedGame.getCurrentPlayerNumber(), 1);
		clonedGame.nextPlayersTurn();
		assertEquals(newP1.getNumberOfTypeTotal(CardType.COPPER), 9);
	}
	
	
	@Test
	public void nextActionTakesIntoAccountCurrentStateAfterCloningWithMCTSLikeBehaviour() {
		TestDominionDecider copperDecider = TestDominionDecider.getExample(CardType.COPPER);
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(copperDecider);
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		p1.takeActions();
		p1.setState(Player.State.PURCHASING);	// to emulate the purchasing behaviour of MCTS
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		assertEquals(p1.getBuys(), 2);
		
		DominionGame clonedGame = game.clone(p1);
		Player newP1 = clonedGame.getCurrentPlayer();
		assertEquals(newP1.getBuys(), 2);
		assertEquals(clonedGame.getCurrentPlayerNumber(), 1);
		clonedGame.nextPlayersTurn();
		assertEquals(newP1.getNumberOfTypeTotal(CardType.COPPER), 9);
	}

}
