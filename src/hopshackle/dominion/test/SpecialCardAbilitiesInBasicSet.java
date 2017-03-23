package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.basecards.*;
import hopshackle.simulation.*;

import java.util.*;

import org.junit.*;

public class SpecialCardAbilitiesInBasicSet {

	public DominionGame game;
	public Player p1, p2, p3, p4;
	private TestDominionDecider remodelDecider, defaultPurchaseDecider;
	private ArrayList<CardValuationVariables> variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
	private HardCodedActionDecider hardCodedActionDecider = new HardCodedActionDecider(variablesToUse);
	private HashMap<CardType, Double> purchasePreferences = new HashMap<CardType, Double>();
	private HashMap<CardType, Double> handPreferences = new HashMap<CardType, Double>();

	@Before
	public void setup() {
		DeciderProperties localProp = SimProperties.getDeciderProperties("GLOBAL");
		localProp.setProperty("DominionCardChanges", "THRONE_ROOM,BUREAUCRAT,LIBRARY,ADVENTURER,THIEF,SPY,FESTIVAL");
		SimProperties.setProperty("Temperature", "0.0");
		localProp.setProperty("DominionCardSetup", "FirstGame");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), localProp), "Test",  false);
		p1 = game.getPlayer(1);
		p2 = game.getPlayer(2);
		p3 = game.getPlayer(3);
		p4 = game.getPlayer(4);
		for (int n=0; n<5; n++)
			p1.drawTopCardFromDeckInto(CardSink.HAND);	// so p1 always has 7 copper and 3 estates
		remodelDecider = TestDominionDecider.getExample(CardType.REMODEL);

		purchasePreferences.put(CardType.COPPER, 0.09);
		purchasePreferences.put(CardType.ESTATE, 0.09);
		purchasePreferences.put(CardType.SILVER, 0.30);
		purchasePreferences.put(CardType.GOLD, 0.50);
		purchasePreferences.put(CardType.PROVINCE, 2.0);
		purchasePreferences.put(CardType.CURSE, -1.0);
		purchasePreferences.put(CardType.MOAT, -0.25);

		handPreferences.put(CardType.ESTATE, -0.05);
		handPreferences.put(CardType.PROVINCE, -0.05);
		handPreferences.put(CardType.COPPER, 0.01);

		defaultPurchaseDecider = new TestDominionDecider(purchasePreferences, handPreferences);

		DominionDeciderContainer ddc = new DominionDeciderContainer(defaultPurchaseDecider, hardCodedActionDecider);
		p1.setDecider(ddc);
		p2.setDecider(ddc);
		p3.setDecider(ddc);
		p4.setDecider(ddc);
	}

	@Test
	public void cellarUpdatesDiscardPile() {
		p2.insertCardDirectlyIntoHand(new Cellar());
		p2.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		int estatesInHand = p2.getNumberOfTypeInHand(CardType.ESTATE);
		game.nextPlayersTurn();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 6);
		assertEquals(p2.getDiscardSize(), estatesInHand);	// CELLAR is still in revealed cards
		assertEquals(p2.getDeckSize(), 5 - estatesInHand);
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(p2), (estatesInHand)/12.0, 0.001);
		p2.tidyUp();		// will exhaust deck
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(p2), 0.0, 0.001);
	}

	@Test
	public void cellarDiscardsAllVictoryCards() {
		for (int n=0; n<5; n++)
			p1.takeCardFromSupply(CardType.COPPER, CardSink.DISCARD);
		p1.insertCardDirectlyIntoHand(new Cellar());
		p1.takeActions();
		// We should discard 3 estate, and then reshuffle discard pile to create new Deck
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(p1.getDeckSize(), 5);
		assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(p1), 0.0, 0.001);
	}

	@Test
	public void militiaForcesAllOtherPlayersToDiscardDownToThree() {
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p3.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p3.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		assertEquals(p1.getHandSize(), 11);
		assertEquals(p2.getHandSize(), 4);
		assertEquals(p3.getHandSize(), 3);
		assertEquals(p4.getHandSize(), 5);
		p1.takeActions();
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p2.getHandSize(), 3);
		assertEquals(p3.getHandSize(), 3);
		assertEquals(p4.getHandSize(), 3);
		assertEquals(p2.getDiscardSize(), 2);
		assertEquals(p3.getDiscardSize(), 2);
		assertEquals(p4.getDiscardSize(), 2);
		assertEquals(p1.getDiscardSize(), 0);
	}

	@Test
	public void moatProtectsAgainstMilitia() {
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MOAT));
		assertEquals(p1.getHandSize(), 11);
		assertEquals(p2.getHandSize(), 6);
		assertEquals(p3.getHandSize(), 5);
		p1.takeActions();
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p2.getHandSize(), 6);
		assertEquals(p3.getHandSize(), 3);
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p3.getDiscardSize(), 2);
	}

	@Test
	public void discardingDueToMilitiaTakesDiscardDeciderIntoAccount() {
		// discardDecider will preferentially discard Estates, and then Copper
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		int[] copperBefore = new int[4];
		int[] copperAfter = new int[4];
		int[] estatesAfter = new int[4];
		int[] estatesBefore = new int[4];
		for (int n=2; n<=4; n++){
			copperBefore[n-1] = game.getPlayer(n).getNumberOfTypeInHand(CardType.COPPER);
			estatesBefore[n-1] = game.getPlayer(n).getNumberOfTypeInHand(CardType.ESTATE);
		}
		p1.takeActions();
		for (int n=2; n<=4; n++){
			copperAfter[n-1] = game.getPlayer(n).getNumberOfTypeInHand(CardType.COPPER);
			estatesAfter[n-1] = game.getPlayer(n).getNumberOfTypeInHand(CardType.ESTATE);
		}
		for (int n=1; n<4; n++) {
			switch (copperBefore[n]) {
			case 5:
				assertEquals(copperAfter[n], 3);
				assertEquals(estatesAfter[n], 0);
				break;
			case 4:
				assertEquals(copperAfter[n], 3);
				assertEquals(estatesAfter[n], 0);
				break;
			case 3:
				assertEquals(copperAfter[n], 3);
				assertEquals(estatesAfter[n], 0);
				break;
			case 2:
				assertEquals(copperAfter[n], 2);
				assertEquals(estatesAfter[n], 1);
				break;
			}
		}
	}

	@Test
	public void mineTrashesSilverPreferentially() {
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MINE));
		p2.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
		int copperBefore = p2.getNumberOfTypeInHand(CardType.COPPER);
		game.nextPlayersTurn();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 6);
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.SILVER), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.GOLD), 1);
		assertEquals(p2.getNumberOfTypeInHand(CardType.COPPER), copperBefore);
	}

	@Test
	public void mineTrashesCopperOtherwise() {
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MINE));
		int copperBefore = p2.getNumberOfTypeInHand(CardType.COPPER);
		game.nextPlayersTurn();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5);
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.SILVER), 1);
		assertEquals(p2.getNumberOfTypeInHand(CardType.GOLD), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.COPPER), copperBefore - 1);
	}

	@Test
	public void mineHasNoEffectIfNoTreasure() {
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MINE));
		int copperBefore = p2.getNumberOfTypeInHand(CardType.COPPER);
		for (int n=0; n<copperBefore; n++)
			p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.TRASH);
		game.nextPlayersTurn();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5 - copperBefore);
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.SILVER), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.GOLD), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.COPPER), 0);
	}

	@Test
	public void remodelTrashesACardAndGainsANewCard() {
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		p1.takeActions();
		assertEquals(p1.getHandSize(), 9);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getDeckSize(), 0);
	}

	@Test
	public void remodelIsForcedToTrashOncePlayedEvenIfSuboptimal() {
		// defaultPurchaseDecider values Copper at 0.10, Estates at 0.09 and Silver at 0.30
		// With only Copper in hand after discarding Estates, the remodel choice will be:
		// COPPER into ESTATE
		// However this will lower the value of the hand overall.
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		int estatesDiscarded = 0;
		if (p2.getNumberOfTypeInHand(CardType.ESTATE) > 0) {
			do {
				p2.moveCard(CardType.ESTATE, CardSink.HAND, CardSink.DISCARD);
				estatesDiscarded++;
			} while (p2.getNumberOfTypeInHand(CardType.ESTATE) > 0);
		}
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5 - estatesDiscarded - 1);
		assertEquals(p2.getDiscardSize(), estatesDiscarded + 1);
		assertEquals(p2.getDeckSize(), 5);
	}

	@Test
	public void remodelTakesPurchaseDeciderIntoAccount() {
		// Following on from above test, we now have an ESTATE, so this will be happily remodelled into a SILVER
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		p2.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5);
		assertEquals(p2.getDiscardSize(), 1);
		assertEquals(p2.getDeckSize(), 5);
		assertEquals(p2.totalTreasureValue(), 9);
		assertEquals(p2.totalVictoryValue(), 3);
		game.nextPlayersTurn();
		// Following on from above test, we should now Remodel a SMITHY into a GOLD
		p3.setDecider(new DominionDeciderContainer(defaultPurchaseDecider, remodelDecider));
		p3.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		p3.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p3.insertCardDirectlyIntoHand(new Card(CardType.SMITHY));
		p3.takeActions();
		assertEquals(p3.getHandSize(), 6);
		assertEquals(p3.getDiscardSize(), 1);
		assertEquals(p3.getDeckSize(), 5);
		assertEquals(p3.totalTreasureValue(), 10);
		assertEquals(p3.totalVictoryValue(), 4);
		assertFalse(p3.getCopyOfHand().contains(CardType.SMITHY));
	}

	@Test
	public void workshopBuysUpToCostOfFour() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WORKSHOP));
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5);
		assertTrue(p2.getAllCards().contains(CardType.WORKSHOP));
		assertEquals(p2.getDiscardSize(), 1);
		assertEquals(p2.totalTreasureValue(), 9); // buys silver given budget of four
		p2.buyCards();
		assertEquals(p2.getHandSize(), 5);
		assertEquals(p2.getDiscardSize(), 2);
	}

	@Test
	public void workshopDoesNotUseTreasureInHand() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WORKSHOP));
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.GOLD));
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.GOLD));
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.GOLD));
		assertEquals(p2.getNumberOfTypeTotal(CardType.SILVER), 0);
		p2.takeActions();
		assertEquals(p2.getHandSize(), 8);
		assertTrue(p2.getAllCards().contains(CardType.WORKSHOP));
		assertEquals(p2.getDiscardSize(), 1);
		assertEquals(p2.getNumberOfTypeTotal(CardType.SILVER), 1); // buys silver given budget of four
		p2.buyCards();
		assertEquals(p2.getHandSize(), 8);
		assertEquals(p2.getDiscardSize(), 2);
	}

	@Test
	public void bureaucratPutsSilverOnOwnDeckAndVictoryCardsOnOpponentsAndIsCounteredByMoat() {
		// need a player with bureaucrat, and at least one other player with no victory cards
		// check that top card of Deck is then Silver
		// or victory card respectively (and that their hand sizes have reduced by one)
		p2.insertCardDirectlyIntoHand(new Bureaucrat());
		p3.insertCardDirectlyIntoHand(new Card(CardType.DUCHY));

		int p4_estates = p4.getNumberOfTypeInHand(CardType.ESTATE);
		for (int i = 0; i < p4_estates; i++) {
			p4.moveCard(CardType.ESTATE, CardSink.HAND, CardSink.DISCARD);
		}
		int p3_estates = p3.getNumberOfTypeInHand(CardType.ESTATE);
		for (int i = 0; i < p3_estates; i++) {
			p3.moveCard(CardType.ESTATE, CardSink.HAND, CardSink.DISCARD);
		}
		game.nextPlayersTurn();
		p1.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p1.insertCardDirectlyIntoHand(new Moat());
		int[] handSize = getAllHandSizes();
		int[] deckSize = getAllDeckSizes();
		p2.takeActions();
		assertEquals(handSize[0] - p1.getHandSize(), 0);
		assertEquals(handSize[1] - p2.getHandSize(), 1);
		assertEquals(handSize[2] - p3.getHandSize(), 1);
		assertEquals(handSize[3] - p4.getHandSize(), 0);
		assertEquals(deckSize[0] - p1.getDeckSize(), 0);
		assertEquals(deckSize[1] - p2.getDeckSize(), -1);
		assertEquals(deckSize[2] - p3.getDeckSize(), -1);
		assertEquals(deckSize[3] - p4.getDeckSize(), 0);
		assertEquals(p3.getNumberOfTypeInHand(CardType.DUCHY), 0);
		p1.drawTopCardFromDeckInto(CardSink.HAND);
		p2.drawTopCardFromDeckInto(CardSink.HAND);
		p3.drawTopCardFromDeckInto(CardSink.HAND);
		assertEquals(p2.getNumberOfTypeInHand(CardType.SILVER), 1);
		assertEquals(p3.getNumberOfTypeInHand(CardType.DUCHY), 1);
	}

	@Test
	public void libraryWhenAllCardsDrawnAreNotActionCards() {
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.insertCardDirectlyIntoHand(new Library());
		int startingDeckSize = p2.getDeckSize();
		game.nextPlayersTurn();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 7);
		assertEquals(p2.getDeckSize() - startingDeckSize, -3);
	}

	@Test
	public void libraryWhenActionCardsAreInThoseDrawn() {
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.insertCardDirectlyIntoHand(new Library());
		p2.moveCard(CardType.MILITIA, CardSink.SUPPLY, CardSink.DECK);
		p2.moveCard(CardType.MILITIA, CardSink.SUPPLY, CardSink.DECK);
		p2.moveCard(CardType.BUREAUCRAT, CardSink.SUPPLY, CardSink.DECK);
		handPreferences.put(CardType.MILITIA, -2.0);
		handPreferences.put(CardType.BUREAUCRAT, -2.0);
		int startingDeckSize = p2.getDeckSize();
		int startingDiscardSize = p2.getDiscardSize();
		game.nextPlayersTurn();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 7);
		assertEquals(p2.getDeckSize() - startingDeckSize, -6);
		assertEquals(p2.getDiscardSize() - startingDiscardSize, 3);
	}

	@Test
	public void withVillageAndLibraryPlayVillageFirstAndKeepAnActionCard() {
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.insertCardDirectlyIntoHand(new Library());
		p2.insertCardDirectlyIntoHand(new Card(CardType.VILLAGE));
		p2.moveCard(CardType.MILITIA, CardSink.SUPPLY, CardSink.DECK);
		p2.moveCard(CardType.BUREAUCRAT, CardSink.SUPPLY, CardSink.DECK);
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DECK);		// to be picked up by Village
		int startingDeckSize = p2.getDeckSize();
		int startingDiscardSize = p2.getDiscardSize();
		game.nextPlayersTurn();
		handPreferences.put(CardType.MILITIA, -2.0);
		handPreferences.put(CardType.BUREAUCRAT, 2.0);
		p2.takeActions();
		// Village played first	- picks up Copper, hand size back to 5
		// then Library - hand size to 7, with 4 cards picked up, and of these the Militia discarded
		// then Bureaucrat - hand size to 6, SILVER added to deck
		assertEquals(p2.getHandSize(), 6);
		assertEquals(p2.getDeckSize() - startingDeckSize, -4);	// one from Village, four from Library, -1 from Bureaucrat
		assertEquals(p2.getDiscardSize() - startingDiscardSize, 1); // the militia card
		assertEquals(p2.getNumberOfTypeInDeck(CardType.SILVER), 1);		// to prove that the Bureaucrat was played
	}
	
	@Test
	public void libraryUpdatesPositionSummary() {
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.insertCardDirectlyIntoHand(new Library());
		p2.moveCard(CardType.MILITIA, CardSink.SUPPLY, CardSink.DECK);
		p2.refreshPositionSummary();
		int copper = p2.getNumberOfTypeInHand(CardType.COPPER);
		PositionSummary initial = p2.getPositionSummaryCopy();
		assertEquals(initial.getPercent(CardType.MILITIA), 1.0 / 12.0, 0.001);
		assertEquals(initial.getPercent(CardType.LIBRARY), 1.0 / 12.0, 0.001);
		assertEquals(initial.getBuys(), 1);
		assertEquals(initial.getBudget(), copper);
		assertEquals(initial.getPercentAction(), 2.0 / 12.0, 0.001);
		game.nextPlayersTurn();
		game.oneAction(false, true);
		PositionSummary ps = p2.getPositionSummaryCopy();
		assertEquals(ps.getPercent(CardType.MILITIA), 1.0 / 12.0, 0.001);
		assertEquals(ps.getPercent(CardType.LIBRARY), 1.0 / 12.0, 0.001);
		assertEquals(ps.getBuys(), 1);
		assertEquals(ps.getPercentAction(), 2.0 / 12.0, 0.001);
		assertEquals(ps.getBudget(), copper);
	}

	@Test
	public void throneRoomDoublesUpBasicCardInHandI() {
		game.nextPlayersTurn();
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.insertCardDirectlyIntoHand(new ThroneRoom());
		p2.insertCardDirectlyIntoHand(new Card(CardType.SMITHY));
		p2.takeActions();
		assertEquals(p2.getHandSize(), 9);	// Throne Room and Smithy Discarded from original 5, and then 6 picked up
	}

	@Test
	public void throneRoomDoublesUpBasicCardInHandII() {
		game.nextPlayersTurn();
		ThroneRoom throneRoom = new ThroneRoom();
		p2.moveCard(CardType.COPPER, CardSink.SUPPLY, CardSink.DISCARD);
		p2.moveCard(CardType.COPPER, CardSink.SUPPLY, CardSink.DISCARD);
		p2.moveCard(CardType.COPPER, CardSink.SUPPLY, CardSink.DISCARD);
		p2.insertCardDirectlyIntoHand(throneRoom);
		p2.insertCardDirectlyIntoHand(new Card(CardType.FESTIVAL));
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		assertEquals(p2.getHandSize(), 12);
		p2.takeActions();
		assertEquals(p2.getHandSize(), 14); // Starts with 11. ThroneRoom and Festival removed for -2; four Moats then played, giving net +4
		assertEquals(p2.getBuys(), 3);
		assertEquals(p2.getBudget(), 14);	// so we have all treasure in original deck, plus one copper, +4 from doubled Festival

		p2.tidyUp();
		assertEquals(p2.getHandSize(), 5); 
		assertEquals(p2.getBuys(), 1);
	}

	@Test
	public void throneRoomDoublesUpComplicatedCardInHand() {
		game.nextPlayersTurn();
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p3.insertCardDirectlyIntoHand(new Card(CardType.DUCHY));
		p3.insertCardDirectlyIntoHand(new Card(CardType.DUCHY));
		int victimStartingHand = p3.getHandSize();
		int victimStartingDeck = p3.getDeckSize();
		p2.insertCardDirectlyIntoHand(new ThroneRoom());
		p2.insertCardDirectlyIntoHand(new Bureaucrat());
		p2.takeActions();
		assertEquals(p2.getNumberOfTypeInDeck(CardType.SILVER), 2);
		assertEquals(p3.getHandSize() - victimStartingHand, -2);
		assertEquals(p3.getDeckSize() - victimStartingDeck, 2);
	}

	@Test
	public void throneRoomDoublesUpSingleActionCardGivenChoice() {
		game.nextPlayersTurn();
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		p2.insertCardDirectlyIntoHand(new ThroneRoom());
		p2.insertCardDirectlyIntoHand(new Card(CardType.SMITHY));
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.takeActions();
		assertEquals(p2.getHandSize(), 10);	// Throne Room and Smithy Discarded from original 6, and then 6 picked up. Moat is ignored
		assertEquals(p2.getNumberOfTypeInHand(CardType.MOAT), 1);
	}

	@Test
	public void throneRoomWithNoActionCardPlayer() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(new ThroneRoom());
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5);
		assertEquals(p2.getCardsInPlay().getSize(), 1);
	}

	@Test
	public void chancellorMovesDiscardIntoDeckIfMoreThanSixProvincesLeft() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CHANCELLOR));
		p2.takeActions();
		assertEquals(p2.getDeckSize(), 0);
		assertEquals(p2.getDiscardSize(), 5);

		for (int i=0; i<6; i++)
			game.drawCard(CardType.PROVINCE);

		game.nextPlayersTurn();
		p3.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CHANCELLOR));
		p3.takeActions();
		assertEquals(p3.getDeckSize(), 5);
		assertEquals(p3.getDiscardSize(), 0);
	}

	@Test
	public void councilRoomCausesOtherPlayersToDrawOneCardEach() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.COUNCIL_ROOM));
		int[] handSizes = getAllHandSizes();
		p2.takeActions();
		assertEquals(p2.getDeckSize(), 1);
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p2.getHandSize(), 9);

		assertEquals(p1.getHandSize() - handSizes[0], 1);	// because p1 already has whole of deck in hand
		assertEquals(p3.getHandSize() - handSizes[2], 1);
		assertEquals(p4.getHandSize() - handSizes[3], 1);
	}

	@Test
	public void spyDiscardsVictoryAndCopperOnSelfAndComplementOnOthers() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.SPY));
		p2.putCardOnTopOfDeck(CardType.SILVER);
		p2.putCardOnTopOfDeck(CardType.COPPER);		// to be drawn by Spy, leaving Silver on top
		p3.putCardOnTopOfDeck(CardType.BUREAUCRAT);
		p4.putCardOnTopOfDeck(CardType.DUCHY);
		p1.putCardOnTopOfDeck(CardType.COPPER);

		p2.takeActions();
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p2.getDeckSize(), 6);
		assertEquals(p2.getHandSize(), 6);
		assertEquals(p3.getDiscardSize(), 1);
		assertEquals(p3.getDeckSize(), 5);
		assertEquals(p3.getHandSize(), 5);
		assertEquals(p4.getDiscardSize(), 0);
		assertEquals(p4.getDeckSize(), 6);
		assertEquals(p4.getHandSize(), 5);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(p1.getDeckSize(), 7);
		assertEquals(p1.getHandSize(), 5);
	}

	@Test
	public void thiefTakesHighestValueTreasureCardIfNotCopper() {
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.THIEF));
		purchasePreferences.put(CardType.COPPER, -0.06);
		p2.setDecider(new DominionDeciderContainer(defaultPurchaseDecider, hardCodedActionDecider));
		p3.putCardOnTopOfDeck(CardType.COPPER);
		p3.putCardOnTopOfDeck(CardType.COPPER);
		p4.putCardOnTopOfDeck(CardType.ESTATE);
		p4.putCardOnTopOfDeck(CardType.ESTATE);
		game.nextPlayersTurn();
		p1.putCardOnTopOfDeck(CardType.SILVER);
		p1.putCardOnTopOfDeck(CardType.COPPER);
		assertEquals(p1.totalTreasureValue(), 13);	// buys silver on own turn
		p2.takeActions();

		assertEquals(p1.getDeckSize(), 6);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p2.getDeckSize(), 5);
		assertEquals(p2.getDiscardSize(), 1);
		assertEquals(p3.getDeckSize(), 5);
		assertEquals(p3.getDiscardSize(), 1);
		assertEquals(p4.getDeckSize(), 5);
		assertEquals(p4.getDiscardSize(), 2);

		assertEquals(p2.totalTreasureValue(), 9);
		assertEquals(p2.totalVictoryValue(), 3);
		assertEquals(p1.totalTreasureValue(), 11);	// buys silver on own turn
		assertEquals(p3.totalTreasureValue(), 8);
		assertEquals(p4.totalTreasureValue(), 7);
		assertEquals(p4.totalVictoryValue(), 5);
	}

	@Test
	public void adventurerTakesTwoTreasuresAndDiscardsRemainder() {
		List<Player> players = game.getAllPlayers();
		for (int i = 0; i < 4; i++) {
			Player p = players.get(i);
			p.insertCardDirectlyIntoHand(new Adventurer());
			do {
				p.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
			} while (p.getBudget() > 0);
			int handSize = p.getHandSize();
			p.takeActions();
			assertEquals(p.getHandSize(), handSize + 1);	// minus Adventurer, plus two Copper
			assertEquals(p.getBudget(), 2);
			assertEquals(p.totalTreasureValue(), 7);	// unchanged
			assertEquals(p.getDiscardSize(), 10 - p.getDeckSize() - p.getHandSize());
			game.nextPlayersTurn();
		}
	}

	@Test
	public void adventurerStopsWhenAllCardsDrawn() {
		p1.insertCardDirectlyIntoHand(new Adventurer());
		p1.takeCardFromSupply(CardType.VILLAGE, CardSink.DISCARD);
		p1.takeCardFromSupply(CardType.SILVER, CardSink.DISCARD);
		p1.takeActions();
		assertEquals(p1.getHandSize(), 11);	// minus Adventurer, plus one Silver
		assertEquals(p1.getBudget(), 9);	// silver, plius starting copper
		assertEquals(p1.totalTreasureValue(), 9);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getDeckSize(), 0);
	}

	@Test
	public void chapelTrashesAllFourWhenAdvantageous() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(new Chapel());
		p2.insertCardDirectlyIntoHand(new Moat());
		for (int i = 0; i < 5; i++)
			p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CURSE));
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 5);
		p2.takeActions();
		assertEquals(p2.getNumberOfTypeInHand(CardType.MOAT), 1);
		assertEquals(p2.getNumberOfTypeInHand(CardType.CURSE), 1);
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p2.getHandSize(), 7);
		assertEquals(p2.getDiscardSize(), 0);
	}

	@Test
	public void chapelTrashesLessThanFourIfAdvantageous() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(new Chapel());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CURSE));
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p2.getHandSize(), 8);
		p2.takeActions();
		assertEquals(p2.getNumberOfTypeInHand(CardType.MOAT), 0);
		assertEquals(p2.getNumberOfTypeInHand(CardType.CURSE), 0);
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 0);
		assertEquals(p2.getNumberOfTypeTotal(CardType.CHAPEL), 1);
		assertEquals(p2.getHandSize(), 5);
		assertEquals(p2.getDiscardSize(), 0);
	}

	@Test
	public void feastGainsCardAndTrashesItself() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(new Feast());
		assertEquals(p2.getNumberOfTypeTotal(CardType.FEAST), 1);

		p2.takeActions();
		assertEquals(p2.getNumberOfTypeTotal(CardType.FEAST), 0);
		assertEquals(p2.getNumberOfTypeTotal(CardType.SILVER), 1);
		assertEquals(p2.getDiscardSize(), 1);
		assertEquals(p2.totalNumberOfCards(), 11);
	}

	@Test
	public void feastGainsCardUpToFive() {
		game.nextPlayersTurn();
		HashMap<CardType, Double> purchasePreferences = new HashMap<CardType, Double>();
		purchasePreferences.put(CardType.COPPER, 0.10);
		purchasePreferences.put(CardType.ESTATE, 0.09);
		purchasePreferences.put(CardType.SILVER, 0.30);
		purchasePreferences.put(CardType.GOLD, 0.50);
		purchasePreferences.put(CardType.PROVINCE, 2.0);
		purchasePreferences.put(CardType.CURSE, -1.0);
		purchasePreferences.put(CardType.MOAT, -0.25);
		purchasePreferences.put(CardType.MINE, 0.40);
		defaultPurchaseDecider = new TestDominionDecider(purchasePreferences, handPreferences);
		p2.setDecider(new DominionDeciderContainer(defaultPurchaseDecider, hardCodedActionDecider));
		p2.insertCardDirectlyIntoHand(new Feast());
		assertEquals(p2.getNumberOfTypeTotal(CardType.FEAST), 1);

		p2.takeActions();
		assertEquals(p2.getNumberOfTypeTotal(CardType.FEAST), 0);
		assertEquals(p2.getNumberOfTypeTotal(CardType.MINE), 1);
		assertEquals(p2.getNumberOfTypeTotal(CardType.SILVER), 0);
		assertEquals(p2.getDiscardSize(), 1);
	}

	@Test
	public void throneRoomOnFeastGainsTwoCardButTrashesOnce() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(new Feast());
		p2.insertCardDirectlyIntoHand(new ThroneRoom());
		assertEquals(p2.getNumberOfTypeTotal(CardType.FEAST), 1);

		p2.takeActions();
		assertEquals(p2.getNumberOfTypeTotal(CardType.FEAST), 0);
		assertEquals(p2.getNumberOfTypeTotal(CardType.SILVER), 2);
		assertEquals(p2.getDiscardSize(), 2);
		assertEquals(p2.totalNumberOfCards(), 13);
	}

	@Test
	public void moneylenderTrashesACopperWhenAdvantageous() {
		game.nextPlayersTurn();
		Moneylender moneylender = new Moneylender();
		purchasePreferences.put(CardType.COPPER, -0.50);
		p2.insertCardDirectlyIntoHand(moneylender);
		int copper = p2.getNumberOfTypeInHand(CardType.COPPER);
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 7);

		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
		p2.takeActions();
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 6);
		assertEquals(moneylender.getAdditionalPurchasePower(), 3);
		assertEquals(p2.getBudget(), copper + 2);

		p2.buyCards();
		assertEquals(moneylender.getAdditionalPurchasePower(), 3);
		p2.tidyUp();
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
	}

	@Test
	public void moneylenderDoesNotTrashACopperWhenAdvantageous() {
		game.nextPlayersTurn();
		Moneylender moneylender = new Moneylender();
		purchasePreferences.put(CardType.COPPER, 0.05);
		p2.insertCardDirectlyIntoHand(moneylender);
		int copper = p2.getNumberOfTypeInHand(CardType.COPPER);
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 7);
		for (int i = 0; i < copper; i++)
			p2.moveCard(CardType.COPPER, CardSink.HAND, CardSink.DISCARD);
		for (int i = 0; i < 3; i++)
			p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.COPPER));
		p2.takeActions();
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
		assertEquals(p2.getBudget(), 3);	// no bonus from Moneylender
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 10);	// 3 inserted directly
	}

	@Test
	public void witchDrawsTwoCardsAndPutsCurseInOtherPlayersHands() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(new Witch());
		p2.takeActions();
		assertEquals(p2.getCopyOfHand().size(), 7);
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 0);
		assertEquals(p1.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p3.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p3.getDiscardSize(), 1);
		assertEquals(p4.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p4.getDiscardSize(), 1);
		assertEquals(p2.getGame().getNumberOfCardsRemaining(CardType.CURSE), 27);
	}

	@Test
	public void witchAffectsOnlyPlayerToLeftIfJustOneCurse() {
		game.nextPlayersTurn();
		DominionGame game = p2.getGame();
		game.removeCardType(CardType.CURSE);
		game.addCardType(CardType.CURSE, 1);
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.WITCH));
		p2.takeActions();
		assertEquals(p2.getCopyOfHand().size(), 7);
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 0);
		assertEquals(p1.getNumberOfTypeTotal(CardType.CURSE), 0);
		assertEquals(p3.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p3.getDiscardSize(), 1);
		assertEquals(p4.getNumberOfTypeTotal(CardType.CURSE), 0);
		assertEquals(p4.getDiscardSize(), 0);
		assertEquals(p2.getGame().getNumberOfCardsRemaining(CardType.CURSE), 0);
	}

	@Test
	public void moatDefendsAgainstWitch() {
		game.nextPlayersTurn();
		p2.insertCardDirectlyIntoHand(new Witch());
		p4.insertCardDirectlyIntoHand(new Moat());
		p2.takeActions();
		assertEquals(p2.getCopyOfHand().size(), 7);
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 0);
		assertEquals(p1.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p3.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p3.getDiscardSize(), 1);
		assertEquals(p4.getNumberOfTypeTotal(CardType.CURSE), 0);
		assertEquals(p4.getDiscardSize(), 0);
		assertEquals(p2.getGame().getNumberOfCardsRemaining(CardType.CURSE), 28);
	}

	private int[] getAllDeckSizes() {
		int[] retValue = new int[4];
		retValue[0] = p1.getDeckSize();
		retValue[1] = p2.getDeckSize();
		retValue[2] = p3.getDeckSize();
		retValue[3] = p4.getDeckSize();
		return retValue;
	}

	private int[] getAllHandSizes() {
		int[] retValue = new int[4];
		retValue[0] = p1.getHandSize();
		retValue[1] = p2.getHandSize();
		retValue[2] = p3.getHandSize();
		retValue[3] = p4.getHandSize();
		return retValue;
	}
}
