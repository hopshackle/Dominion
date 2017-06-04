package hopshackle.dominion.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import org.junit.Before;
import org.junit.Test;

public class DominionBuyingDecisionTest {
	public DominionGame game;
	public Player p1;
	
	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		SimProperties.setProperty("Temperature", "0.0");
		game = new DominionGame(new DeciderGenerator(new GameSetup()), "Test",  false);
		p1 = game.getCurrentPlayer();
		for (int n=0; n<5; n++)
			p1.drawTopCardFromDeckInto(CardSink.HAND);	// so p1 always has 7 copper and 3 estates
	}

	@Test
	public void singleBuyIncludesRelevantCardsOnly() {
		DominionBuyingDecision dpd = new DominionBuyingDecision(p1, 1, 1);
		List<ActionEnum<Player>> allPurchases = dpd.getPossiblePurchasesAsActionEnum();
		// CURSE, COPPER, NONE
		assertEquals(allPurchases.size(), 3);
		
		dpd = new DominionBuyingDecision(p1, 3, 1);
		allPurchases = dpd.getPossiblePurchasesAsActionEnum();
		// NONE, CURSE, COPPER, MOAT, CELLAR, SILVER, ESTATE, VILLAGE, WORKSHOP, WOODCUTTER
		assertEquals(allPurchases.size(), 10);
		
	}
	
	@Test
	public void multipleBuysExcludeIdenticalPermutations() {
		DominionBuyingDecision dpd = new DominionBuyingDecision(p1, 2, 2);
		List<ActionEnum<Player>> allPurchases = dpd.getPossiblePurchasesAsActionEnum();
		// NONE, CURSE, COPPER, MOAT, CELLAR, ESTATE
		// CURSE+COPPER, CURSE+CURSE, COPPER+COPPER
		// MOAT+CURSE, MOAT+COPPER, CELLAR+CURSE, CELLAR+COPPER, ESTATE+CURSE, ESTATE+COPPER
		assertEquals(allPurchases.size(), 15);
	}
	
	@Test
	public void cardTypeListEquality() {
		CardTypeList ctl1, ctl2, ctl3, ctl4;
		List<CardType> coppercurse, cursecopper, cellarcellar;
		coppercurse = new ArrayList<CardType>();
		coppercurse.add(CardType.COPPER);
		coppercurse.add(CardType.CURSE);
		cursecopper = new ArrayList<CardType>();
		cursecopper.add(CardType.CURSE);
		cursecopper.add(CardType.COPPER);
		cellarcellar = new ArrayList<CardType>();
		cellarcellar.add(CardType.CELLAR);
		cellarcellar.add(CardType.CELLAR);

		ctl1 = new CardTypeList(coppercurse, CardTypeAugment.ChangeType.BUY);
		ctl2 = new CardTypeList(cursecopper, CardTypeAugment.ChangeType.BUY);
		ctl3 = new CardTypeList(coppercurse, CardTypeAugment.ChangeType.BUY);
		ctl4 = new CardTypeList(cellarcellar, CardTypeAugment.ChangeType.BUY);
		assertTrue(ctl1.equals(ctl2));
		assertTrue(ctl1.equals(ctl3));
		assertTrue(ctl2.equals(ctl3));
		assertFalse(ctl1.equals(ctl4));
		assertFalse(ctl4.equals(ctl2));
		assertFalse(ctl3.equals(ctl4));
	}
	

	@Test
	public void onlyAffordableAndAvailableCardTypesInTheGameAreChooseable() {
		for (int loop = 0; loop < 5; loop++)
			p1.drawTopCardFromDeckInto(CardSink.HAND);
		assertEquals(p1.getNumberOfTypeInHand(CardType.COPPER), 7);
		game.removeCardType(CardType.DUCHY);
		game.addCardType(CardType.VILLAGE, 10);
		assertEquals(p1.getBudget(), 7);
		p1.takeActions();
		List<ActionEnum<Player>> options = game.getPossibleActions(p1);
		assertTrue(options.contains(CardTypeAugment.buyCard(CardType.VILLAGE)));
		assertFalse(options.contains(CardTypeAugment.buyCard(CardType.DUCHY)));
		assertTrue(options.contains(CardTypeAugment.buyCard(CardType.COPPER)));
		assertFalse(options.contains(CardTypeAugment.buyCard(CardType.PROVINCE)));
		assertTrue(options.contains(CardTypeAugment.buyCard(CardType.ESTATE)));
		assertTrue(options.contains(CardTypeAugment.buyCard(CardType.SILVER)));
		
		for (int loop = 0; loop < 12; loop++) {
			System.out.println(loop);
			p1.takeCardFromSupply(CardType.ESTATE, CardSink.DISCARD);
		}
		options = game.getPossibleActions(p1);
		assertFalse(options.contains(CardTypeAugment.buyCard(CardType.ESTATE)));
	}

}
