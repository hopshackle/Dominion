package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import org.junit.*;

import com.sun.media.jfxmedia.events.PlayerStateEvent.PlayerState;

public class PositionSummaryTest {

	DominionGame game;
	PositionSummary p1, p2, p3, p4;

	@Before
	public void setUp() {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		game.getCurrentPlayer().takeCardFromSupply(CardType.VILLAGE, CardSink.DISCARD);
		game.getCurrentPlayer().takeCardFromSupply(CardType.GOLD, CardSink.DISCARD);
		game.getCurrentPlayer().setState(Player.State.PLAYING);
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
	}

	@Test
	public void trashCardFromDiscard() {
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 1);
		p1.trashCard(CardType.GOLD, CardSink.DISCARD);
		assertEquals(p1.getPercentageInDiscard(), 1.0/11.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 0);

		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 1);
		game.getCurrentPlayer().trashCard(CardType.GOLD, CardSink.DISCARD);
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getPercentageInDiscard(), 1.0/11.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 7);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 0);
	}
	@Test
	public void trashCardFromHand() {
		int copper = p1.getNumberInHand(CardType.COPPER);
		int estates = p1.getNumberInHand(CardType.ESTATE);
		assertEquals(copper+estates, 5);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 7);
		game.getCurrentPlayer().trashCard(CardType.COPPER, CardSink.HAND);
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getPercentageInDiscard(), 2.0/11.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 6);
		int newCopper = p1.getNumberInHand(CardType.COPPER);
		assertEquals(copper - newCopper, 1);
	}
	@Test
	public void trashCardFromRevealed() {
		game.getCurrentPlayer().putCardOnTopOfDeck(CardType.MILITIA);
		game.getCurrentPlayer().drawTopCardFromDeckIntoHand();
		game.getCurrentPlayer().takeActions();
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getNumberPlayed(CardType.MILITIA), 1);
		assertEquals(p1.getNumberOfCardsTotal(CardType.MILITIA), 1);
		assertEquals(p1.totalNumberOfCards(), 13.0, 0.0001);
		game.getCurrentPlayer().trashCard(CardType.MILITIA, CardSink.REVEALED);
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getNumberPlayed(CardType.MILITIA), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.MILITIA), 0);
		assertEquals(p1.totalNumberOfCards(), 12.0, 0.0001);
	}
	@Test
	public void addCardToHand() {
		assertEquals(p1.getNumberInHand(CardType.GOLD), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 1);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		p1.addCard(CardType.GOLD, CardSink.HAND);
		assertEquals(p1.getNumberInHand(CardType.GOLD), 1);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 2);
		assertEquals(p1.getPercentageInDiscard(), 2.0/13.0, 0.001);
	}
	@Test
	public void addCardToDiscard() {
		assertEquals(p1.getNumberInHand(CardType.GOLD), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 1);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		p1.addCard(CardType.GOLD, CardSink.DISCARD);
		assertEquals(p1.getNumberInHand(CardType.GOLD), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 2);
		assertEquals(p1.getPercentageInDiscard(), 3.0/13.0, 0.001);
	}
	@Test
	public void addCardToDeck() {
		assertEquals(p1.getNumberInHand(CardType.GOLD), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 1);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		p1.addCard(CardType.GOLD, CardSink.DECK);
		assertEquals(p1.getNumberInHand(CardType.GOLD), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 2);
		assertEquals(p1.getPercentageInDiscard(), 2.0/13.0, 0.001);
	}

	@Test
	public void applicationOfCardGain() {
		assertEquals(p1.getNumberOfCardsTotal(CardType.CURSE), 0);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		assertEquals(p1.getVictoryDensity(), 3.0/12.0, 0.001);
		ActionEnum<Player> testAction = new CardTypeAugment(CardType.CURSE, CardSink.DISCARD, ChangeType.GAIN);
		PositionSummary updated = p1.apply(testAction);
		assertEquals(updated.getNumberOfCardsTotal(CardType.CURSE), 1);
		assertEquals(updated.getPercentageInDiscard(), 3.0/13.0, 0.001);
		assertEquals(updated.getVictoryDensity(), 2.0/13.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.CURSE), 0);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		assertEquals(p1.getVictoryDensity(), 3.0/12.0, 0.001);
	}
	@Test
	public void applicationOfCardLoss() {
		int initial = p1.getNumberInHand(CardType.COPPER);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 7);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		assertEquals(p1.getWealthDensity(), 10.0/12.0, 0.001);
		ActionEnum<Player> testAction = new CardTypeAugment(CardType.COPPER, CardSink.HAND, ChangeType.LOSS);
		PositionSummary updated = p1.apply(testAction);
		assertEquals(updated.getNumberOfCardsTotal(CardType.COPPER), 6);
		assertEquals(updated.getNumberInHand(CardType.COPPER), initial-1);
		assertEquals(updated.getPercentageInDiscard(), 2.0/11.0, 0.001);
		assertEquals(updated.getWealthDensity(), 9.0/11.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 7);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		assertEquals(p1.getWealthDensity(), 10.0/12.0, 0.001);
	}
	@Test
	public void applicationOfCardPlayFromValidState() {
		p1.drawCard(CardType.WOODCUTTER, CardSink.HAND);
		assertEquals(p1.getActions(), 1);
		assertEquals(p1.getAdditionalPurchasePower(), 0);
		assertEquals(p1.getBuys(), 1);
		assertEquals(p1.getNumberInHand(CardType.WOODCUTTER), 1);
		assertEquals(p1.getNumberPlayed(CardType.WOODCUTTER), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.WOODCUTTER), 1);
		ActionEnum<Player> testAction = new CardTypeAugment(CardType.WOODCUTTER, CardSink.HAND, ChangeType.PLAY);
		PositionSummary updated = p1.apply(testAction);
		assertEquals(updated.getActions(), 0);
		assertEquals(updated.getAdditionalPurchasePower(), 2);
		assertEquals(updated.getBuys(), 2);
		assertEquals(updated.getNumberInHand(CardType.WOODCUTTER), 0);
		assertEquals(updated.getNumberPlayed(CardType.WOODCUTTER), 1);
		assertEquals(updated.getNumberOfCardsTotal(CardType.WOODCUTTER), 1);

		assertEquals(p1.getActions(), 1);
		assertEquals(p1.getAdditionalPurchasePower(), 0);
		assertEquals(p1.getBuys(), 1);
		assertEquals(p1.getNumberInHand(CardType.WOODCUTTER), 1);
		assertEquals(p1.getNumberPlayed(CardType.WOODCUTTER), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.WOODCUTTER), 1);
	}
	@Test
	public void applicationOfCardPlayWithoutCard() {
		ActionEnum<Player> testAction = new CardTypeAugment(CardType.WOODCUTTER, CardSink.HAND, ChangeType.PLAY);
		try {
			p1.apply(testAction);
			assertTrue(false);
		} catch (AssertionError err) {
			// Hurrah
		}
	}
	@Test
	public void applicationOfCardPlayFromInvalidState() {
		game.getCurrentPlayer().takeActions();
		game.getCurrentPlayer().setState(Player.State.PURCHASING);
		p1 = game.getCurrentPlayer().getPositionSummaryCopy();
		p1.drawCard(CardType.WOODCUTTER, CardSink.HAND);
		ActionEnum<Player> testAction = new CardTypeAugment(CardType.WOODCUTTER, CardSink.HAND, ChangeType.PLAY);
		try {
			p1.apply(testAction);
			assertTrue(false);
		} catch (AssertionError err) {
			// Hurrah
		}
	}
}
