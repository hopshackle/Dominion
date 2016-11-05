package hopshackle.dominion.test;

import static org.junit.Assert.assertEquals;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.simulation.SimProperties;

import org.junit.*;

public class BuysActionsAndPurchasePowerFromCards {

	public DominionGame game;
	public Player p1;
	TestDominionDecider villageDecider, marketDecider, smithyDecider, woodcutterDecider;
	TestDominionDecider copperDecider, silverDecider, provinceDecider, generalPurchaseDecider;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		SimProperties.setProperty("Temperature", "0.0");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		p1 = game.getCurrentPlayer();
		for (int n=0; n<5; n++)
			p1.drawTopCardFromDeckIntoHand();	// so p1 always has 7 copper and 3 estates
		villageDecider = TestDominionDecider.getExample(CardType.VILLAGE);
		marketDecider =  TestDominionDecider.getExample(CardType.MARKET);
		smithyDecider =  TestDominionDecider.getExample(CardType.SMITHY);
		woodcutterDecider =  TestDominionDecider.getExample(CardType.WOODCUTTER);
		copperDecider =  TestDominionDecider.getExample(CardType.COPPER);
		silverDecider =  TestDominionDecider.getExample(CardType.SILVER);
		provinceDecider =  TestDominionDecider.getExample(CardType.PROVINCE);

		HashMap<CardType, Double> values = new HashMap<CardType, Double>();
		values.put(CardType.COPPER, 0.5);
		values.put(CardType.SILVER, 1.5);
		values.put(CardType.GOLD, 5.0);
		values.put(CardType.SMITHY, 4.0);
		values.put(CardType.CURSE, -1.0);
		generalPurchaseDecider = new TestDominionDecider(values);
	}

	@Test
	public void normallyOnlyOneCardIsPurchasable() {
		p1.setDecider(copperDecider);
		p1.setActionDecider(villageDecider);
		assertEquals(p1.totalTreasureValue(), 7);
		p1.insertCardDirectlyIntoHand(new Card(CardType.VILLAGE));
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.totalTreasureValue(), 8);
	}

	@Test
	public void ableToGetBuysAndPurchasePowerFromDeck() {
		Deck deck = new Deck();
		deck.addCard(CardFactory.instantiateCard(CardType.VILLAGE));
		deck.addCard(CardFactory.instantiateCard(CardType.MARKET));
		deck.addCard(CardFactory.instantiateCard(CardType.WOODCUTTER));
		deck.addCard(new Card(CardType.COPPER));
		deck.addCard(new Card(CardType.COPPER));
		deck.addCard(CardFactory.instantiateCard(CardType.VILLAGE));
		deck.addCard(CardFactory.instantiateCard(CardType.SMITHY));
		assertEquals(deck.getAdditionalBuys(), 2);
		assertEquals(deck.getAdditionalPurchasePower(), 3);
		deck.addCard(CardFactory.instantiateCard(CardType.MILITIA));
		assertEquals(deck.getAdditionalBuys(), 2);
		assertEquals(deck.getAdditionalPurchasePower(), 5);
	}

	@Test
	public void actionWithAdditionalPurchasePowerIsUsedInPurchase() {
		p1.setDecider(provinceDecider);
		p1.setActionDecider(woodcutterDecider);
		assertEquals(p1.totalTreasureValue(), 7);
		assertEquals(p1.totalVictoryValue(), 3);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WOODCUTTER));
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.totalTreasureValue(), 7);
		assertEquals(p1.totalVictoryValue(), 9);
	}

	@Test
	public void actionWithAdditionalPurchasePowerThatIsNotPlayedCannotBeUsedInPurchase() {
		p1.setDecider(provinceDecider);
		p1.setActionDecider(villageDecider);
		assertEquals(p1.totalTreasureValue(), 7);
		assertEquals(p1.totalVictoryValue(), 3);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WOODCUTTER));
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 11);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(p1.totalTreasureValue(), 7);
		assertEquals(p1.totalVictoryValue(), 3);
	}

	@Test
	public void actionWithAdditionalBuyIsUsedInPurchase() {
		p1.setDecider(silverDecider);
		p1.setActionDecider(marketDecider);
		assertEquals(p1.totalTreasureValue(), 7);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MARKET));
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getDiscardSize(), 2);
		assertEquals(p1.totalTreasureValue(), 11);
	}


	@Test
	public void actionsWithAdditionalBuyNotUsedIfNothingAvailable() {
		p1.setDecider(silverDecider);
		p1.setActionDecider(villageDecider);
		assertEquals(p1.totalTreasureValue(), 7);
		assertEquals(p1.getBudget(), 7);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MARKET));
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 11);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getBudget(), 7);
		assertEquals(p1.totalTreasureValue(), 9);
	}

	@Test
	public void cardsAreDrawnCorrectlyWhenActionPlayed() {
		p1.setDecider(silverDecider);
		p1.setActionDecider(smithyDecider);
		for (int n=0; n<5; n++)
			p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.SMITHY));
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 13);
		assertEquals(p1.getDiscardSize(), 1);
	}

	@Test
	public void multipleActionsAreTakenWhereAllowed() {
		p1.setDecider(silverDecider);
		p1.setActionDecider(villageDecider);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.VILLAGE));
		p1.takeCardFromSupplyIntoDiscard(CardType.VILLAGE);
		p1.takeCardFromSupplyIntoDiscard(CardType.VILLAGE);
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getDeckSize(), 0);
	}

	@Test
	public void surplusActionsAreWasted() {
		p1.setDecider(silverDecider);
		p1.setActionDecider(villageDecider);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.VILLAGE));
		p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 11);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getDeckSize(), 1);
	}

	@Test
	public void actionCardsInHandAreNotUsedIfNoActionsLeft() {
		p1.setDecider(silverDecider);
		p1.setActionDecider(smithyDecider);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.SMITHY));
		p1.takeCardFromSupplyIntoDiscard(CardType.SMITHY);
		p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		p1.takeActions();
		p1.buyCards();
		assertEquals(p1.getHandSize(), 13);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getDeckSize(), 0);
	}

	@Test
	public void twoBuysAreProcessedAsExpectedI() {
		p1.setDecider(generalPurchaseDecider);
		p1.setActionDecider(woodcutterDecider);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WOODCUTTER));
		p1.discard(CardType.COPPER); // leaves six left
		p1.discard(CardType.COPPER); // leaves five left
		p1.discard(CardType.COPPER); // leaves four left
		p1.takeActions();
		p1.buyCards();
		// should buy one Gold and one Copper
		assertEquals(p1.getHandSize(), 7);
		assertEquals(p1.getDiscardSize(), 5);
		assertEquals(p1.getDeckSize(), 0);
		assertEquals(p1.totalNumberOfCards(), 13);
		assertEquals(p1.totalTreasureValue(), 11);
	}

	@Test
	public void twoBuysAreProcessedAsExpectedII() {
		p1.setDecider(generalPurchaseDecider);
		p1.setActionDecider(woodcutterDecider);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WOODCUTTER));
		p1.discard(CardType.COPPER); // leaves six left
		p1.takeActions();
		p1.buyCards();
		// should buy two Smithies
		assertEquals(p1.getHandSize(), 9);
		assertEquals(p1.getDiscardSize(), 3);
		assertEquals(p1.getDeckSize(), 0);
		assertEquals(p1.totalNumberOfCards(), 13);
		assertEquals(p1.totalTreasureValue(), 7);
		assertEquals(p1.getNumberOfTypeTotal(CardType.SMITHY), 2);
	}

	@Test
	public void threeBuysWithSufficientTopCards() {
		p1.setDecider(generalPurchaseDecider);
		DominionBuyingDecision dpd = new DominionBuyingDecision(p1, 12, 3);
		List<CardType> purchase = dpd.getBestPurchase();
		assertEquals(purchase.size(), 3);
		int gold = 0;
		int smithy = 0;
		int silver = 0;
		int copper = 0;
		for (CardType card : purchase) {
			if (card == CardType.GOLD) gold++;
			if (card == CardType.SILVER) silver++;
			if (card == CardType.SMITHY) smithy++;
			if (card == CardType.COPPER) copper++;
		}
		assertEquals(gold, 0);
		assertEquals(smithy, 3);
		assertEquals(copper, 0);
		assertEquals(silver, 0);
	}

	@Test
	public void threeBuysWithInsufficientTopCards() {
		p1.setDecider(generalPurchaseDecider);
		for (int i = 0; i < 9; i++)
			game.drawCard(CardType.SMITHY);
		assertEquals(game.getNumberOfCardsRemaining(CardType.SMITHY), 1);
		DominionBuyingDecision dpd = new DominionBuyingDecision(p1, 12, 3);
		List<CardType> purchase = dpd.getBestPurchase();
		assertEquals(purchase.size(), 3);
		int gold = 0;
		int smithy = 0;
		int silver = 0;
		int copper = 0;
		for (CardType card : purchase) {
			if (card == CardType.GOLD) gold++;
			if (card == CardType.SILVER) silver++;
			if (card == CardType.SMITHY) smithy++;
			if (card == CardType.COPPER) copper++;
		}
		assertEquals(gold, 2);
		assertEquals(smithy, 0);
		assertEquals(copper, 1);
		assertEquals(silver, 0);
	}

}
