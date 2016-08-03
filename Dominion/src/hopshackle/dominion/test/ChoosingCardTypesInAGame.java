package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.simulation.SimProperties;

import org.junit.*;

public class ChoosingCardTypesInAGame {

	private Game game;
	private Player firstPlayer;

	@Before
	public void setup() {
		SimProperties.setProperty("DominionCardSetup", "NONE");
		game = new Game(new RunGame("Test", 1, new DeciderGenerator(new GameSetup(), 1, 1, 0, 0)), false);
		firstPlayer = game.getCurrentPlayer();
	}

	@Test
	public void cardTypesCanBeAddedToAGame() {
		assertFalse(game.availableCardsToPurchase().contains(CardType.VILLAGE));
		assertTrue(game.availableCardsToPurchase().contains(CardType.GOLD));
		assertEquals(game.availableCardsToPurchase().size(), 8);
		game.addCardType(CardType.VILLAGE, 10);
		assertTrue(game.availableCardsToPurchase().contains(CardType.VILLAGE));
		assertTrue(game.availableCardsToPurchase().contains(CardType.GOLD));
		assertEquals(game.availableCardsToPurchase().size(), 9);

		int currentCards = game.getNumberOfCardsRemaining(CardType.GOLD);
		game.addCardType(CardType.GOLD, 10);
		assertEquals(game.getNumberOfCardsRemaining(CardType.GOLD) - currentCards, 10);
		assertTrue(game.availableCardsToPurchase().contains(CardType.GOLD));
	}

	@Test
	public void cardTypesCanBeRemovedFromAGame() {
		assertFalse(game.availableCardsToPurchase().contains(CardType.VILLAGE));
		assertTrue(game.availableCardsToPurchase().contains(CardType.CURSE));
		assertEquals(game.availableCardsToPurchase().size(), 8);
		game.removeCardType(CardType.VILLAGE);
		assertEquals(game.availableCardsToPurchase().size(), 8);
		assertFalse(game.availableCardsToPurchase().contains(CardType.VILLAGE));
		game.removeCardType(CardType.CURSE);
		assertEquals(game.availableCardsToPurchase().size(), 7);
		assertFalse(game.availableCardsToPurchase().contains(CardType.CURSE));
		assertEquals(game.getNumberOfCardsRemaining(CardType.CURSE), 0);
	}

	@Test
	public void onlyAffordableCardTypesInTheGameAreChooseable() {
		for (int loop = 0; loop < 5; loop++)
			firstPlayer.drawTopCardFromDeckIntoHand();
		assertEquals(firstPlayer.getNumberOfTypeInHand(CardType.COPPER), 7);
		game.removeCardType(CardType.DUCHY);
		game.addCardType(CardType.VILLAGE, 10);
		assertTrue(CardType.VILLAGE.isChooseable(firstPlayer));
		assertFalse(CardType.DUCHY.isChooseable(firstPlayer));
		assertTrue(CardType.COPPER.isChooseable(firstPlayer));
		assertTrue(CardType.PROVINCE.isChooseable(firstPlayer));
		assertTrue(CardType.ESTATE.isChooseable(firstPlayer));
		assertTrue(CardType.SILVER.isChooseable(firstPlayer));
	}

	@Test
	public void exhaustedCardTypesAreNotChooseable() {
		for (int loop = 0; loop < 5; loop++)
			firstPlayer.drawTopCardFromDeckIntoHand();
		assertEquals(firstPlayer.getNumberOfTypeInHand(CardType.COPPER), 7);
		for (int loop = 0; loop < 12; loop++) {
			assertTrue(CardType.ESTATE.isChooseable(firstPlayer));
			firstPlayer.takeCardFromSupplyIntoDiscard(CardType.ESTATE);
		}
		assertEquals(game.getNumberOfCardsRemaining(CardType.ESTATE), 0);
		assertFalse(CardType.ESTATE.isChooseable(firstPlayer));
	}
	
	@Test
	public void possibleToNotBuyACard() {
		assertTrue(CardType.NONE.isChooseable(firstPlayer));
		assertEquals(firstPlayer.totalNumberOfCards(), 10);
		firstPlayer.takeCardFromSupplyIntoDiscard(CardType.NONE);
		firstPlayer.takeCardFromSupplyIntoDiscard(CardType.NONE);
		assertEquals(firstPlayer.totalNumberOfCards(), 10);
		assertTrue(game.availableCardsToPurchase().contains(CardType.NONE));
	}
}
