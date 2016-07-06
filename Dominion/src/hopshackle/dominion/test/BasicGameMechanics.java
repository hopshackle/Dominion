package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.simulation.SimProperties;

import org.junit.*;

public class BasicGameMechanics {

	public Game game;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		game = new Game(new RunGame("Test", 1, new DeciderGenerator(new GameSetup(), 1, 1, 0, 0)));
	}

	@Test
	public void newGameHasFourPlayer() {
		assertEquals(game.getPlayers().length, 4);
		for (int n = 0; n < 4; n++) {
			assertTrue(game.getPlayers()[n] != null && game.getPlayers()[n] instanceof Player);
		}
	}

	@Test
	public void newPlayerHasHandOfFiveCardsAndDeckOfFiveCards() {
		for (int n = 0; n < 4; n++) {
			Player p = game.getPlayers()[n];
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
			Player p = game.getPlayers()[n];
			assertEquals(p.getNumberOfTypeTotal(CardType.COPPER), 7);
			assertEquals(p.getNumberOfTypeTotal(CardType.ESTATE), 3);
		}
	}

	@Test
	public void playersTakeTurnsInSequence() {
		Player[] players = game.getPlayers();
		assertTrue(players[0] == game.getCurrentPlayer());
		assertEquals(game.turnNumber(), 0);
		game.nextPlayersTurn();
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
		Player[] players = game.getPlayers();
		for (int i = 0; i<4; i++) {
		assertFalse(players[i].isTakingActions());
		}
		players[0].takeActions();
		assertTrue(players[0].isTakingActions());
		players[0].buyCards();
		assertFalse(players[0].isTakingActions());
	}
	
}
