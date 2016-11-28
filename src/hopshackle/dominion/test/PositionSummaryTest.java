package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import org.junit.*;

public class PositionSummaryTest {
	
	DominionGame game;
	PositionSummary p1, p2, p3, p4;
	
	@Before
	public void setUp() {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		game.getCurrentPlayer().takeCardFromSupplyIntoDiscard(CardType.VILLAGE);
		game.getCurrentPlayer().takeCardFromSupplyIntoDiscard(CardType.GOLD);
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
		game.getCurrentPlayer().trashCardFromDiscard(CardType.GOLD);
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
		game.getCurrentPlayer().trashCardFromHand(CardType.COPPER);
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
		game.getCurrentPlayer().trashCardFromRevealed(CardType.MILITIA);
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
}
