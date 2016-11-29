package hopshackle.dominion.test;

import static org.junit.Assert.assertEquals;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.SimProperties;

import org.junit.*;

public class CardValuationVariablesTest {
	
	DominionGame game;
	PositionSummary p1, p2, p3, p4;
	
	@Before
	public void setUp() {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		recalculate();
	}
	
	private void recalculate() {
		for (int n = 0; n<4; n++)
			game.getAllPlayers().get(n).tidyUp();
		p1 = game.getAllPlayers().get(0).getPositionSummaryCopy();
		p2 = game.getAllPlayers().get(1).getPositionSummaryCopy();
		p3 = game.getAllPlayers().get(2).getPositionSummaryCopy();
		p4 = game.getAllPlayers().get(3).getPositionSummaryCopy();
	}

	@Test
	public void deckSize() {
		assertEquals(CardValuationVariables.DECK_SIZE.getValue(p1), 0.00, 0.0001);
		p1.addCard(CardType.COPPER, CardSink.DECK);
		assertEquals(CardValuationVariables.DECK_SIZE.getValue(p1), 0.05, 0.0001);
	}
	
	@Test
	public void provincesRemaining() {
		assertEquals(CardValuationVariables.PROVINCES_BOUGHT.getValue(p1), 0.00, 0.0001);
		assertEquals(CardValuationVariables.PROVINCES_BOUGHT.getValue(p2), 0.00, 0.0001);
		game.drawCard(CardType.PROVINCE);
		recalculate();
		assertEquals(CardValuationVariables.PROVINCES_BOUGHT.getValue(p1), (1.0/12.0), 0.0001);
		assertEquals(CardValuationVariables.PROVINCES_BOUGHT.getValue(p2), (1.0/12.0), 0.0001);
		assertEquals(CardValuationVariables.DUCHIES_BOUGHT.getValue(p1), 0.00, 0.0001);
		assertEquals(CardValuationVariables.DUCHIES_BOUGHT.getValue(p2), 0.00, 0.0001);
	}
	
	@Test
	public void duchiesRemaining() {
		assertEquals(CardValuationVariables.DUCHIES_BOUGHT.getValue(p1), 0.00, 0.0001);
		assertEquals(CardValuationVariables.DUCHIES_BOUGHT.getValue(p2), 0.00, 0.0001);
		game.drawCard(CardType.DUCHY);
		recalculate();
		assertEquals(CardValuationVariables.DUCHIES_BOUGHT.getValue(p1), (1.0/12.0), 0.0001);
		assertEquals(CardValuationVariables.DUCHIES_BOUGHT.getValue(p2), (1.0/12.0), 0.0001);
	}
	
	@Test
	public void victoryMargin() {
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p1), 0.00, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p2), 0.00, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p3), 0.00, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p4), 0.00, 0.0001);
		p1.addCard(CardType.ESTATE, CardSink.DECK);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p1), 0.1, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p2), 0.0, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p3), 0.0, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p4), 0.0, 0.0001);
		game.getAllPlayers().get(0).takeCardFromSupply(CardType.ESTATE, CardSink.DISCARD);
		recalculate();
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p1), 0.1, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p2), -.1, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p3), -.1, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p4), -.1, 0.0001);
		
		game.getAllPlayers().get(1).takeCardFromSupply(CardType.CURSE, CardSink.DISCARD);
		recalculate();
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p1), 0.1, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p2), -0.20, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p3), -.1, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p4), -.1, 0.0001);
		
		game.getPlayer(3).takeCardFromSupply(CardType.PROVINCE, CardSink.DISCARD);
		recalculate();
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p1), -.5, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p2), -.7, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p3), 0.5, 0.0001);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(p4), -.6, 0.0001);
	}
	
	@Test
	public void wealthAndVictoryDensities() {
		assertEquals(p1.getWealthDensity(), 0.70, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.30, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 7);
		assertEquals(p1.getPercent(CardType.COPPER), 0.70, 0.001);
		p1.addCard(CardType.COPPER, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 0.727, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.273, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 8);
		assertEquals(p1.getPercent(CardType.COPPER), 0.727, 0.001);
		p1.addCard(CardType.SILVER, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 0.833, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.25, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 8);
		assertEquals(p1.getPercent(CardType.COPPER), 0.667, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.SILVER), 1);
		assertEquals(p1.getPercent(CardType.SILVER), 0.083, 0.001);
		p1.addCard(CardType.GOLD, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 1.00, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.231, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.GOLD), 1);
		assertEquals(p1.getPercent(CardType.GOLD), 0.077, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.SILVER), 1);
		assertEquals(p1.getPercent(CardType.SILVER), 0.077, 0.001);
		p1.addCard(CardType.ESTATE, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 0.929, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.286, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.COPPER), 8);
		assertEquals(p1.getPercent(CardType.COPPER), 0.571, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.SILVER), 1);
		assertEquals(p1.getPercent(CardType.SILVER), 0.0714, 0.001);
		p1.addCard(CardType.DUCHY, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 0.867, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.467, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.PROVINCE), 0);
		p1.addCard(CardType.PROVINCE, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 0.813, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.813, 0.001);
		assertEquals(p1.getNumberOfCardsTotal(CardType.PROVINCE), 1);
		p1.addCard(CardType.CURSE, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 0.765, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.706, 0.001);
		game.addCardType(CardType.VILLAGE, 10);
		p1.addCard(CardType.VILLAGE, CardSink.DECK);
		assertEquals(p1.getWealthDensity(), 0.722, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.667, 0.001);
		p1.trashCard(CardType.SILVER, CardSink.DECK);
		assertEquals(p1.getNumberOfCardsTotal(CardType.SILVER), 0);
		assertEquals(p1.getPercent(CardType.SILVER), 0.0, 0.001);
		assertEquals(p1.getWealthDensity(), 0.647, 0.001);
		assertEquals(p1.getVictoryDensity(), 0.706, 0.001);
	}
	
	@Test
	public void percentVictoryActionAndTreasureCards() {
		assertEquals(CardValuationVariables.PERCENT_VICTORY.getValue(p1), 0.60, 0.001);
		assertEquals(CardValuationVariables.PERCENT_TREASURE.getValue(p1), 1.40, 0.001);
		assertEquals(CardValuationVariables.PERCENT_ACTION.getValue(p1), 0.00, 0.001);
		p1.addCard(CardType.CURSE, CardSink.DECK);
		assertEquals(CardValuationVariables.PERCENT_VICTORY.getValue(p1), 0.728, 0.001);
		assertEquals(CardValuationVariables.PERCENT_TREASURE.getValue(p1), 1.272, 0.001);
		assertEquals(CardValuationVariables.PERCENT_ACTION.getValue(p1), 0.00, 0.001);
		p1.addCard(CardType.ESTATE, CardSink.DECK);
		assertEquals(CardValuationVariables.PERCENT_VICTORY.getValue(p1), 0.834, 0.001);
		assertEquals(CardValuationVariables.PERCENT_TREASURE.getValue(p1), 1.166, 0.001);
		assertEquals(CardValuationVariables.PERCENT_ACTION.getValue(p1), 0.00, 0.001);
		p1.addCard(CardType.GOLD, CardSink.DECK);
		assertEquals(CardValuationVariables.PERCENT_VICTORY.getValue(p1), 0.77, 0.001);
		assertEquals(CardValuationVariables.PERCENT_TREASURE.getValue(p1), 1.230, 0.001);
		assertEquals(CardValuationVariables.PERCENT_ACTION.getValue(p1), 0.00, 0.001);
		p1.addCard(CardType.CELLAR, CardSink.DECK);
		assertEquals(CardValuationVariables.PERCENT_VICTORY.getValue(p1), 0.714, 0.001);
		assertEquals(CardValuationVariables.PERCENT_TREASURE.getValue(p1), 1.143, 0.001);
		assertEquals(CardValuationVariables.PERCENT_ACTION.getValue(p1), (1.0/14.0 *2.0), 0.001);
	}
	
	@Test
	public void cardsInHand() {
		p1.addCard(CardType.SILVER, CardSink.DECK);
		assertEquals(CardValuationVariables.SILVER_IN_HAND.getValue(p1), 0.0, 0.001);
		game.getPlayer(1).insertCardDirectlyIntoHand(new Card(CardType.SILVER));
		p1 = game.getPlayer(1).getPositionSummaryCopy();
		assertEquals(CardValuationVariables.SILVER_IN_HAND.getValue(p1), 0.20, 0.001);
		p1.addCard(CardType.VILLAGE, CardSink.DECK);
		assertEquals(CardValuationVariables.VILLAGES_IN_HAND.getValue(p1), 0.0, 0.001);
		game.getPlayer(1).insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.VILLAGE));
		p1 = game.getPlayer(1).getPositionSummaryCopy();
		assertEquals(CardValuationVariables.VILLAGES_IN_HAND.getValue(p1), 0.20, 0.001);
	}
	
	@Test
	public void actionCardsInDeck() {
		Player player = game.getPlayer(1);
		game.addCardType(CardType.REMODEL, 12);
		game.addCardType(CardType.MINE, 12);
		game.addCardType(CardType.MARKET, 12);
		player.takeCardFromSupply(CardType.REMODEL, CardSink.DISCARD);
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.REMODEL_PERCENT.getValue(p1), (1.0/11.0)*5.0, 0.001);
		assertEquals(CardValuationVariables.MINE_PERCENT.getValue(p1), 0.0, 0.001);
		assertEquals(CardValuationVariables.MARKET_PERCENT.getValue(p1), 0.0, 0.001);
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MINE));
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.REMODEL_PERCENT.getValue(p1), (1.0/12.0)*5.0, 0.001);
		assertEquals(CardValuationVariables.MINE_PERCENT.getValue(p1), (1.0/12.0)*5.0, 0.001);
		assertEquals(CardValuationVariables.MARKET_PERCENT.getValue(p1), 0.0, 0.001);
		player.takeCardFromSupply(CardType.REMODEL, CardSink.DISCARD);
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.REMODEL_PERCENT.getValue(p1), (2.0/13.0)*5.0, 0.001);
		assertEquals(CardValuationVariables.MINE_PERCENT.getValue(p1), (1.0/13.0)*5.0, 0.001);
		assertEquals(CardValuationVariables.MARKET_PERCENT.getValue(p1), 0.0, 0.001);
		player.trashCard(CardType.MINE, CardSink.HAND);
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.REMODEL_PERCENT.getValue(p1), (2.0/12.0)*5.0, 0.001);
		assertEquals(CardValuationVariables.MINE_PERCENT.getValue(p1), 0.0, 0.001);
		assertEquals(CardValuationVariables.MARKET_PERCENT.getValue(p1), 0.0, 0.001);
		player.takeCardFromSupply(CardType.MARKET, CardSink.DISCARD);
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.REMODEL_PERCENT.getValue(p1), (2.0/13.0)*5.0, 0.001);
		assertEquals(CardValuationVariables.MINE_PERCENT.getValue(p1), 0.0, 0.001);
		assertEquals(CardValuationVariables.MARKET_PERCENT.getValue(p1), (1.0/13.0)*5.0, 0.001);
	}
	
	@Test
	public void percentageOfPilesDepleted() {
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 1.0, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 1.0, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 1.0, 0.001);
		game.removeCardType(CardType.CELLAR);
		game.addCardType(CardType.CELLAR, 4);
		recalculate();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 1.0, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 1.0, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		game.removeCardType(CardType.MARKET);
		game.addCardType(CardType.MARKET, 5);
		recalculate();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 1.0, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.5, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		game.removeCardType(CardType.WOODCUTTER);
		game.addCardType(CardType.WOODCUTTER, 6);
		recalculate();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.6, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.5, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		game.removeCardType(CardType.PROVINCE);
		game.addCardType(CardType.PROVINCE, 1); // moves third lowest to MARKET
		recalculate();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.5, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.1, 0.001);
		game.removeCardType(CardType.PROVINCE);
		game.addCardType(CardType.PROVINCE, 6); // moves third lowest back to WOODCUTTER / PROVINCE
		recalculate();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.6, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.5, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		game.removeCardType(CardType.ESTATE);
		game.addCardType(CardType.ESTATE, 7); // no change
		recalculate();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.6, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.5, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		game.removeCardType(CardType.ESTATE);
		game.addCardType(CardType.ESTATE, 2); // moves third lowest to MARKET
		recalculate();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.5, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.2, 0.001);
	}
	
	@Test
	public void percentagePilesDepletedWorksCorrectlyWithHypotheticalSituations() {
		percentageOfPilesDepleted();
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.5, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.2, 0.001);
		recalculate();
		p1.drawCard(CardType.MARKET);
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.2, 0.001);
		p1.drawCard(CardType.ESTATE);
		assertEquals(CardValuationVariables.THIRD_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.SECOND_DEPLETED_PILE.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.MOST_DEPLETED_PILE.getValue(p1), 0.1, 0.001);
	}
	
	@Test
	public void gameVariablesInPositionSummaryCorrectlyUpdatedForHypotheticallyDrawnCard() {
		p1.drawCard(CardType.DUCHY);
		assertEquals(CardValuationVariables.DUCHIES_BOUGHT.getValue(p1), (1.0/12.0), 0.001);
	}
	
	@Test
	public void variablesInPositionSummaryCorrectlyUpdatedForHypotheticallyTrashedCard() {
		assertEquals(CardValuationVariables.PROVINCES_BOUGHT.getValue(p1), 0.00, 0.001);
		assertEquals(CardValuationVariables.VICTORY_DENSITY.getValue(p1), 3.0 / 10.0 / 2.0, 0.001);
		p1.drawCard(CardType.PROVINCE);
		assertEquals(CardValuationVariables.PROVINCES_BOUGHT.getValue(p1), (1.0/12.0), 0.001);
		assertEquals(CardValuationVariables.VICTORY_DENSITY.getValue(p1), (3.0 + 6.0) / 11.0 / 2.0, 0.001);
		p1.trashCard(CardType.PROVINCE, CardSink.DECK);
		assertEquals(CardValuationVariables.PROVINCES_BOUGHT.getValue(p1), (1.0/12.0), 0.001);
		assertEquals(CardValuationVariables.VICTORY_DENSITY.getValue(p1), 3.0 / 10.0 / 2.0, 0.001);
	}
	
	@Test
	public void percentageInDiscardBasic() {
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(p1), 0.50, 0.0001);
		p1.addCard(CardType.CELLAR, CardSink.DECK);
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(p1), (5.0/11.0), 0.0001);
	}
	
	@Test
	public void percentageInDiscardAdvanced() {
		Player player = game.getCurrentPlayer();
		player.tidyUp();	// So that they now have all ten cards in discard, and reshuffle to draw 5
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(player.getPositionSummaryCopy()), 0.0, 0.0001);
		player.takeCardFromSupply(CardType.MILITIA, CardSink.DISCARD);
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(player.getPositionSummaryCopy()), (1.0/11.0), 0.0001);
		player.discard(CardType.COPPER);
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(player.getPositionSummaryCopy()), (2.0/11.0), 0.0001);
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MARKET));
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(player.getPositionSummaryCopy()), (2.0/12.0), 0.0001);
		player.putCardFromHandOnTopOfDeck(CardType.MARKET);
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(player.getPositionSummaryCopy()), (2.0/12.0), 0.0001);
		player.putDeckIntoDiscard();
		double hand = player.getHandSize();
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(player.getPositionSummaryCopy()), (12.0 - hand)/12.0, 0.0001);
		player.drawTopCardFromDeckIntoHand();
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(player.getPositionSummaryCopy()), 0.0, 0.0001);
	}
	
	@Test
	public void basicVariablesFromCardPlayPlayedI() {
		Player player = game.getCurrentPlayer();
		player.setState(Player.State.PLAYING);
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.BUYS.getValue(p1), 0.333333, 0.001);
		assertEquals(CardValuationVariables.ACTIONS.getValue(p1), 0.2, 0.001);
		assertEquals(CardValuationVariables.PURCHASE_POWER.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.MILITIA_PLAYED.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.MILITIA_IN_HAND.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.WORKSHOPS_PLAYED.getValue(p1), 0, 0.001);
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.BUYS.getValue(p1), 0.333333, 0.001);
		assertEquals(CardValuationVariables.ACTIONS.getValue(p1), 0.2, 0.001);
		assertEquals(CardValuationVariables.PURCHASE_POWER.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.MILITIA_PLAYED.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.MILITIA_IN_HAND.getValue(p1), 0.2, 0.001);
		assertEquals(CardValuationVariables.WORKSHOPS_PLAYED.getValue(p1), 0, 0.001);
		p1 = p1.apply(new CardTypeAugment(CardType.MILITIA, CardSink.DISCARD, ChangeType.PLAY));	// This is just a lookahead and does not update the game
		assertEquals(CardValuationVariables.BUYS.getValue(p1), 0.333333, 0.001);
		assertEquals(CardValuationVariables.ACTIONS.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.PURCHASE_POWER.getValue(p1), 0.25, 0.001);
		assertEquals(CardValuationVariables.MILITIA_PLAYED.getValue(p1), 1, 0.001);
		assertEquals(CardValuationVariables.MILITIA_IN_HAND.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.WORKSHOPS_PLAYED.getValue(p1), 0, 0.001);
	}
	
	@Test
	public void basicVariablesFromCardPlayPlayedII() {
		Player player = game.getCurrentPlayer();
		player.setState(Player.State.PLAYING);
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.VILLAGE));
		player.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WOODCUTTER));
		p1 = player.getPositionSummaryCopy();
		assertEquals(CardValuationVariables.BUYS.getValue(p1), 0.333333, 0.001);
		assertEquals(CardValuationVariables.ACTIONS.getValue(p1), 0.2, 0.001);
		assertEquals(CardValuationVariables.PURCHASE_POWER.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.MILITIA_PLAYED.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.VILLAGES_IN_HAND.getValue(p1), 0.2, 0.001);
		assertEquals(CardValuationVariables.WOODCUTTERS_IN_HAND.getValue(p1), 0.2, 0.001);
		p1 = p1.apply(new CardTypeAugment(CardType.VILLAGE, CardSink.HAND, ChangeType.PLAY));	// This is just a lookahead and does not update the game
		assertEquals(CardValuationVariables.BUYS.getValue(p1), 0.333333, 0.001);
		assertEquals(CardValuationVariables.ACTIONS.getValue(p1), 0.4, 0.001);
		assertEquals(CardValuationVariables.PURCHASE_POWER.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.VILLAGES_IN_HAND.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.WOODCUTTERS_IN_HAND.getValue(p1), 0.2, 0.001);
		p1 = p1.apply(new CardTypeAugment(CardType.WOODCUTTER, CardSink.HAND, ChangeType.PLAY));	// This is just a lookahead and does not update the game
		assertEquals(CardValuationVariables.BUYS.getValue(p1), 0.66666666, 0.001);
		assertEquals(CardValuationVariables.ACTIONS.getValue(p1), 0.2, 0.001);
		assertEquals(CardValuationVariables.PURCHASE_POWER.getValue(p1), 0.25, 0.001);
		assertEquals(CardValuationVariables.VILLAGES_IN_HAND.getValue(p1), 0, 0.001);
		assertEquals(CardValuationVariables.WOODCUTTERS_IN_HAND.getValue(p1), 0, 0.001);
	}
	
	@Test
	public void turnNumber() {
		assertEquals(CardValuationVariables.TURNS.getValue(p1), 0.0, 0.0001);
		game.nextPlayersTurn();
		recalculate();
		assertEquals(CardValuationVariables.TURNS.getValue(p1), 0.0, 0.0001);
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		recalculate();
		assertEquals(CardValuationVariables.TURNS.getValue(p1), 0.0, 0.0001);
		assertEquals(CardValuationVariables.TURNS.getValue(p2), 0.0, 0.0001);
		game.nextPlayersTurn();
		recalculate();
		assertEquals(CardValuationVariables.TURNS.getValue(p1), 0.025, 0.0001);
		assertEquals(CardValuationVariables.TURNS.getValue(p2), 0.025, 0.0001);
	}
	
	@Test
	public void victoryMarginAfterCloning() {
		DominionGame clonedGame = game.clone(game.getCurrentPlayer());
		PositionSummary clonedP1 = clonedGame.getCurrentPlayer().getPositionSummaryCopy();
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(clonedP1), 0.0, 0.001);
		clonedP1.addCard(CardType.COPPER, CardSink.DECK);
		assertEquals(CardValuationVariables.VICTORY_MARGIN.getValue(clonedP1), 0.0, 0.001);

	}
	
}
