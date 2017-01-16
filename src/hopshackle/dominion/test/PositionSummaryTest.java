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
		game.getCurrentPlayer().takeCardFromSupply(CardType.VILLAGE, CardSink.DISCARD);
		game.getCurrentPlayer().takeCardFromSupply(CardType.GOLD, CardSink.DISCARD);
		//	game.getCurrentPlayer().setState(Player.State.PLAYING);
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
		game.getCurrentPlayer().moveCard(CardType.GOLD, CardSink.DISCARD, CardSink.TRASH);
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
		game.getCurrentPlayer().moveCard(CardType.COPPER, CardSink.HAND, CardSink.TRASH);
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getPercentageInDiscard(), 2.0/11.0, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 6);
		int newCopper = p1.getNumberInHand(CardType.COPPER);
		assertEquals(copper - newCopper, 1);
	}
	@Test
	public void trashCardFromRevealed() {
		game.getCurrentPlayer().putCardOnTopOfDeck(CardType.MILITIA);
		game.getCurrentPlayer().drawTopCardFromDeckInto(CardSink.HAND);
		game.getCurrentPlayer().takeActions();
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getNumberPlayed(CardType.MILITIA), 1);
		assertEquals(p1.getNumberOfCardsTotal(CardType.MILITIA), 1);
		assertEquals(p1.totalNumberOfCards(), 13.0, 0.0001);
		assertEquals(p1.getPercentAction(), 2.0 / 13.0, 0.001);
		game.getCurrentPlayer().moveCard(CardType.MILITIA, CardSink.REVEALED, CardSink.TRASH);
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		assertEquals(p1.getNumberPlayed(CardType.MILITIA), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.MILITIA), 0);
		assertEquals(p1.totalNumberOfCards(), 12.0, 0.0001);
		assertEquals(p1.getPercentAction(), 1.0 / 12.0, 0.001);
	}
	@Test
	public void addCardToHand() {
		assertEquals(p1.getNumberInHand(CardType.GOLD), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 1);
		assertEquals(p1.getPercentageInDiscard(), 2.0/12.0, 0.001);
		assertEquals(p1.getPercentAction(), 1.0/12.00, 0.001);
		p1.addCard(CardType.GOLD, CardSink.HAND);
		assertEquals(p1.getNumberInHand(CardType.GOLD), 1);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 2);
		assertEquals(p1.getPercentageInDiscard(), 2.0/13.0, 0.001);
		assertEquals(p1.getPercentAction(), 1.0/13.00, 0.001);

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
		ActionEnum<Player> testAction = CardTypeAugment.takeCard(CardType.CURSE);
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
		ActionEnum<Player> testAction = CardTypeAugment.trashCard(CardType.COPPER, CardSink.HAND);
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
		game.getCurrentPlayer().setState(Player.State.PLAYING);
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		p1.drawCard(CardType.WOODCUTTER, CardSink.HAND);
		assertEquals(p1.getActions(), 1);
		assertEquals(p1.getAdditionalPurchasePower(), 0);
		assertEquals(p1.getBuys(), 1);
		assertEquals(p1.getNumberInHand(CardType.WOODCUTTER), 1);
		assertEquals(p1.getNumberPlayed(CardType.WOODCUTTER), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.WOODCUTTER), 1);
		assertEquals(p1.getPercentAction(), 2.0 / 13.0, 0.001);
		ActionEnum<Player> testAction = CardTypeAugment.playCard(CardType.WOODCUTTER);
		PositionSummary updated = p1.apply(testAction);
		assertEquals(updated.getActions(), 0);
		assertEquals(updated.getAdditionalPurchasePower(), 2);
		assertEquals(updated.getBuys(), 2);
		assertEquals(updated.getNumberInHand(CardType.WOODCUTTER), 0);
		assertEquals(updated.getNumberPlayed(CardType.WOODCUTTER), 1);
		assertEquals(updated.getNumberOfCardsTotal(CardType.WOODCUTTER), 1);
		assertEquals(updated.getPercentAction(), 2.0 / 13.0, 0.001);

		assertEquals(p1.getPercentAction(), 2.0 / 13.0, 0.001);
		assertEquals(p1.getActions(), 1);
		assertEquals(p1.getAdditionalPurchasePower(), 0);
		assertEquals(p1.getBuys(), 1);
		assertEquals(p1.getNumberInHand(CardType.WOODCUTTER), 1);
		assertEquals(p1.getNumberPlayed(CardType.WOODCUTTER), 0);
		assertEquals(p1.getNumberOfCardsTotal(CardType.WOODCUTTER), 1);
	}
	@Test
	public void applicationOfCardPlayWithoutCard() {
		ActionEnum<Player> testAction = CardTypeAugment.playCard(CardType.WOODCUTTER);
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
		ActionEnum<Player> testAction = CardTypeAugment.playCard(CardType.WOODCUTTER);
		try {
			p1.apply(testAction);
			assertTrue(false);
		} catch (AssertionError err) {
			// Hurrah
		}
	}

	@Test
	public void runningAnActionIncreasesActionsLeftInPositionSummary() {
		Player player = game.getCurrentPlayer();
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CELLAR));
		Action<Player> da = new DominionAction(player, CardTypeAugment.playCard(CardType.CELLAR));

		da.addToAllPlans();
		da.start();
		da.run();
		assertEquals(player.getActionsLeft(), 1);
		assertEquals(player.getPositionSummaryCopy().getActions(), 1);
	}

	@Test
	public void playingACardUpdatesPositionSummaryWithPurchasePower() {
		Player player = game.getCurrentPlayer();
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MARKET));
		CardTypeAugment marketEnum = CardTypeAugment.playCard(CardType.MARKET);
		DominionAction action = new DominionAction(player, marketEnum );
		assertEquals(player.getAdditionalPurchasePower(), 0);
		assertEquals(player.getBuys(), 1);
		action.addToAllPlans();
		action.start();
		action.run();
		assertEquals(player.getAdditionalPurchasePower(), 1);
		assertEquals(player.getBuys(), 2);
		assertEquals(player.getPositionSummaryCopy().getAdditionalPurchasePower(), 1);
		assertEquals(player.getPositionSummaryCopy().getBuys(), 2);
	}

	@Test
	public void positionSummaryUpdatedAheadOfDefensivePlay() {
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		Player p1 = game.getCurrentPlayer();
		Player p3 = game.getPlayer(3);
		assertEquals(game.getCurrentPlayerNumber(),1);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		assertEquals(p3.getPositionSummaryCopy().getTurns(), 0.00, 0.001);
		assertEquals(p3.getPositionSummaryCopy().getActions(), 0.00, 0.001);
		p1.takeActions();
		assertEquals(p3.getPositionSummaryCopy().getTurns(), 1.0, 0.001);
		assertEquals(p3.getPositionSummaryCopy().getActions(), 0.00, 0.001);
	}

	@Test
	public void discardCardAugment() {
		CardTypeAugment action = CardTypeAugment.discardCard(CardType.COPPER);
		PositionSummary ps = game.getCurrentPlayer().getPositionSummaryCopy();
		PositionSummary newPs = ps.apply(action);
		assertEquals(ps.getHand().size(), 5);
		assertEquals(newPs.getHand().size(), 4);
		assertEquals(ps.getNumberInHand(CardType.COPPER) - newPs.getNumberInHand(CardType.COPPER), 1);
		assertEquals(ps.getNumberInHand(CardType.ESTATE) - newPs.getNumberInHand(CardType.ESTATE), 0);
		assertEquals(ps.getNumberOfCardsTotal(CardType.COPPER) - newPs.getNumberOfCardsTotal(CardType.COPPER), 0);
		assertEquals(newPs.getPercentageInDiscard(), 3.0/12.0, 0.001);
		assertEquals(ps.getPercentageInDiscard(), 2.0/12.0, 0.001);
	}
	@Test
	public void fromHandToDeck() {
		Player p2 = game.getPlayer(2);
		PositionSummary ps = p2.getPositionSummaryCopy();
		assertEquals(ps.getHandSize(), 5);
		double startMoney = ps.getHandMoneyValue();
		assertEquals(ps.getPercentageInDiscard(), 0.00, 0.01);
		assertEquals(ps.getPercentVictory(), 0.30, 0.01);
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DECK);
		ps = p2.getPositionSummaryCopy();
		assertEquals(ps.getHandSize(), 4);
		assertEquals(ps.getHandMoneyValue() - startMoney, -1.00, 0.01);
		assertEquals(ps.getPercentageInDiscard(), 0.00, 0.01);
		assertEquals(ps.getPercentVictory(), 0.30, 0.01);
	}

	@Test
	public void fromSupplyToHand() {
		Player p2 = game.getPlayer(2);
		PositionSummary ps = p2.getPositionSummaryCopy();
		assertEquals(ps.getHandSize(), 5);
		assertEquals(ps.getPercentageDepleted()[0], 0.00, 0.001);
		assertEquals(ps.getPercentAction(), 0.00, 0.01);
		p2.moveCard(CardType.VILLAGE, CardSink.SUPPLY, CardSink.HAND);
		ps = p2.getPositionSummaryCopy();
		assertEquals(ps.getHandSize(), 6);
		assertEquals(ps.getPercentageDepleted()[0], 0.10, 0.001);
		assertEquals(ps.getPercentAction(), 1.0 / 11.0, 0.01);
	}

	@Test
	public void puttingDiscardIntoDeck() {
		Player p1 = game.getPlayer(1);
		PositionSummary ps = p1.getPositionSummaryCopy();
		assertEquals(ps.getPercentageInDiscard(), 2.0 / 12.0, 0.001);
		int cardsInDeck = p1.getDeckSize();
		for (int i = 0; i < cardsInDeck; i++) {
			p1.drawTopCardFromDeckInto(CardSink.DISCARD);
		}
		ps = p1.getPositionSummaryCopy();
		assertEquals(ps.getPercentageInDiscard(), 7.0 / 12.0, 0.001);
		p1.peekAtTopCardOfDeck();
		ps = p1.getPositionSummaryCopy();
		assertEquals(p1.getDeckSize(), 7);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(ps.getPercentageInDiscard(), 0.0 / 12.0, 0.001);
	}
}
