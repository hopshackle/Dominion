package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.SimProperties;

import org.junit.*;

public class ChoosingCardTypesInAGame {

	private DominionGame game;
	private Player firstPlayer;

	@Before
	public void setup() {
		SimProperties.setProperty("DominionCardSetup", "NONE");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		firstPlayer = game.getCurrentPlayer();
		firstPlayer.setState(Player.State.PURCHASING);
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
			firstPlayer.drawTopCardFromDeckInto(CardSink.HAND);
		assertEquals(firstPlayer.getNumberOfTypeInHand(CardType.COPPER), 7);
		game.removeCardType(CardType.DUCHY);
		game.addCardType(CardType.VILLAGE, 10);
		assertTrue(CardTypeAugment.takeCard(CardType.VILLAGE).isChooseable(firstPlayer));
		assertFalse(CardTypeAugment.takeCard(CardType.DUCHY).isChooseable(firstPlayer));
		assertTrue(CardTypeAugment.takeCard(CardType.COPPER).isChooseable(firstPlayer));
		assertTrue(CardTypeAugment.takeCard(CardType.PROVINCE).isChooseable(firstPlayer));
		assertTrue(CardTypeAugment.takeCard(CardType.ESTATE).isChooseable(firstPlayer));
		assertTrue(CardTypeAugment.takeCard(CardType.SILVER).isChooseable(firstPlayer));
	}

	@Test
	public void exhaustedCardTypesAreNotChooseable() {
		for (int loop = 0; loop < 5; loop++)
			firstPlayer.drawTopCardFromDeckInto(CardSink.HAND);
		assertEquals(firstPlayer.getNumberOfTypeInHand(CardType.COPPER), 7);
		assertEquals(game.getNumberOfCardsRemaining(CardType.ESTATE), 12);
		for (int loop = 0; loop < 12; loop++) {
			System.out.println(loop);
			assertTrue(CardTypeAugment.takeCard(CardType.ESTATE).isChooseable(firstPlayer));
			firstPlayer.takeCardFromSupply(CardType.ESTATE, CardSink.DISCARD);
		}
		assertEquals(game.getNumberOfCardsRemaining(CardType.ESTATE), 0);
		assertFalse(CardTypeAugment.takeCard(CardType.ESTATE).isChooseable(firstPlayer));

	}
	
	@Test
	public void possibleToNotBuyACard() {
		assertTrue(CardTypeAugment.takeCard(CardType.NONE).isChooseable(firstPlayer));
		assertEquals(firstPlayer.totalNumberOfCards(), 10);
		firstPlayer.takeCardFromSupply(CardType.NONE, CardSink.DISCARD);
		firstPlayer.takeCardFromSupply(CardType.NONE, CardSink.HAND);
		assertEquals(firstPlayer.totalNumberOfCards(), 10);
		assertTrue(game.availableCardsToPurchase().contains(CardType.NONE));
	}
}
