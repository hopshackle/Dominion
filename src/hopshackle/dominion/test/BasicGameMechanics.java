package hopshackle.dominion.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import org.junit.*;

public class BasicGameMechanics {

	public DominionGame game;
	TestDominionDecider copperDecider, woodcutterDecider, workshopDecider;
	private ArrayList<CardValuationVariables> variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
	private HardCodedActionDecider hardCodedActionDecider = new HardCodedActionDecider(variablesToUse);

	@Before
	public void setUp() throws Exception {
		DeciderProperties localProp = SimProperties.getDeciderProperties("GLOBAL");
		localProp.setProperty("DominionCardSetup", "FirstGame");
		localProp.setProperty("DeciderType", "NN");
		localProp.setProperty("RandomDeciderMaxChance", "0.0");
		localProp.setProperty("RandomDeciderMinChance", "0.0");
		game = new DominionGame(new DeciderGenerator("", new GameSetup(), localProp), "Test",  false);
		copperDecider = TestDominionDecider.getExample(CardType.COPPER);
		woodcutterDecider = TestDominionDecider.getExample(CardType.WOODCUTTER);
		workshopDecider = TestDominionDecider.getExample(CardType.WORKSHOP);
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
	public void playerPlaysCardThenBuysOne() {
		Player player1 = game.getCurrentPlayer();
		player1.setDecider(new DominionDeciderContainer(copperDecider, workshopDecider));
		player1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WORKSHOP));
		game.nextPlayersTurn();
		Action<?> lastAction =  player1.getActionPlan().getLastAction();
		assertTrue(lastAction != null);
		assertEquals(player1.getAllCards().size(),13);

		game.nextPlayersTurn();
		assertTrue(player1.getActionPlan().getLastAction() == lastAction);
		assertEquals(player1.getAllCards().size(),13);
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
		p1.takeCardFromSupply(CardType.COPPER, CardSink.DISCARD);
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
		assertTrue(players.get(0).isTakingActions());
		for (int i = 1; i<4; i++) {
			assertFalse(players.get(i).isTakingActions());
		}
		players.get(0).takeActions();		// calls updateGameStatus
		assertEquals(players.get(0).getBuys(), 1);
		assertFalse(players.get(0).isTakingActions());
		assertFalse(players.get(1).isTakingActions());
		players.get(0).buyCards(true);			// calls updateGameStatus, moves to next player
		assertEquals(players.get(0).getBuys(), 1);
		assertFalse(players.get(0).isTakingActions());
		assertTrue(players.get(1).isTakingActions());
		game.nextPlayersTurn();
		assertEquals(players.get(0).getBuys(), 1);
		assertFalse(players.get(0).isTakingActions());
		assertFalse(players.get(1).isTakingActions());
		assertTrue(players.get(2).isTakingActions());
	}

	@Test
	public void nextActionTakesIntoAccountCurrentState() {
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(new DominionDeciderContainer(copperDecider, woodcutterDecider));
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		assertEquals(p1.getCopyOfHand().size(), 7);
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		game.nextPlayersTurn();
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 9);
	}

	@Test
	public void nextActionTakesIntoAccountCurrentStateAfterCloning() {
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(new DominionDeciderContainer(copperDecider, woodcutterDecider));
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
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(new DominionDeciderContainer(copperDecider, woodcutterDecider));
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
	public void discardOptionsTakeAccountOfMaximum() {
		List<List<CardType>> discards = game.getCurrentPlayer().getPossibleDiscardsFromHand(0, 2);
		for (List<CardType> discard : discards) {
			assertTrue(discard.size() < 3);
		}
	}
	@Test
	public void discardOptionsTakeAccountOfMinimum() {
		List<List<CardType>> discards = game.getCurrentPlayer().getPossibleDiscardsFromHand(2, 4);
		for (List<CardType> discard : discards) {
			assertTrue(discard.size() > 1);
		}
	}
	@Test
	public void discardOptionsWorksWithSameMinMax() {
		game.getCurrentPlayer().insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.ESTATE));
		game.getCurrentPlayer().insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.ESTATE));
		List<List<CardType>> discards = game.getCurrentPlayer().getPossibleDiscardsFromHand(2, 2);
		for (List<CardType> discard : discards) {
			assertEquals(discard.size(), 2);
		}
		assertEquals(discards.size(), 3);
	}
	@Test
	public void lastCardPlayedIsReturnedCorrectly() {
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(hardCodedActionDecider);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.VILLAGE));
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WOODCUTTER));
		p1.takeActions();
		assertTrue(p1.getCardLastPlayed().getType() == CardType.WOODCUTTER);
		

	}
}
