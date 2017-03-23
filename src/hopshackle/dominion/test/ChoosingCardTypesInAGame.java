package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import org.junit.*;

public class ChoosingCardTypesInAGame {

	private DominionGame game;
	private Player firstPlayer;

	@Before
	public void setup() {
		DeciderProperties localProp = SimProperties.getDeciderProperties("GLOBAL");
		localProp.setProperty("DominionCardSetup", "NONE");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), localProp), "Test",  false);
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
	public void possibleToNotBuyACard() {
		assertTrue(CardTypeAugment.takeCard(CardType.NONE).isChooseable(firstPlayer));
		assertEquals(firstPlayer.totalNumberOfCards(), 10);
		firstPlayer.takeCardFromSupply(CardType.NONE, CardSink.DISCARD);
		firstPlayer.takeCardFromSupply(CardType.NONE, CardSink.HAND);
		assertEquals(firstPlayer.totalNumberOfCards(), 10);
		assertTrue(game.availableCardsToPurchase().contains(CardType.NONE));
	}
}
