package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.simulation.*;

import java.util.*;

import org.junit.*;

public class SpecialCardAbilitiesInBasicSet {

	public Game game;
	public Player p1, p2, p3, p4;
	private TestDominionDecider remodelDecider, defaultPurchaseDecider, thiefPurchaseDecider;
	private DominionPositionDecider discardDecider;
	private ArrayList<GeneticVariable> variablesToUse = new ArrayList<GeneticVariable>(EnumSet.allOf(CardValuationVariables.class));
	private ArrayList<ActionEnum> actionsToUse = new ArrayList<ActionEnum>(EnumSet.allOf(CardType.class));
	private HardCodedActionDecider hardCodedActionDecider = new HardCodedActionDecider(actionsToUse, variablesToUse);

	@Before
	public void setup() {
		SimProperties.setProperty("Temperature", "0.0");
		SimProperties.setProperty("DominionCardSetup", "FirstGame");
		game = new Game(new SequenceOfGames("Test", 1, null));
		p1 = game.getPlayers()[0];
		p2 = game.getPlayers()[1];
		p3 = game.getPlayers()[2];
		p4 = game.getPlayers()[3];
		for (int n=0; n<5; n++)
			p1.drawTopCardFromDeckIntoHand();	// so p1 always has 7 copper and 3 estates
		remodelDecider = TestDominionDecider.getExample(CardType.REMODEL);
		discardDecider = new HardCodedDiscardDecider(actionsToUse, variablesToUse);
		p1.setDiscardDecider(discardDecider);
		p2.setDiscardDecider(discardDecider);
		p3.setDiscardDecider(discardDecider);
		p4.setDiscardDecider(discardDecider);
		p1.setActionDecider(hardCodedActionDecider);
		p2.setActionDecider(hardCodedActionDecider);
		p3.setActionDecider(hardCodedActionDecider);
		p4.setActionDecider(hardCodedActionDecider);

		HashMap<CardType, Double> purchasePreferences = new HashMap<CardType, Double>();
		purchasePreferences.put(CardType.COPPER, 0.10);
		purchasePreferences.put(CardType.ESTATE, 0.09);
		purchasePreferences.put(CardType.SILVER, 0.30);
		purchasePreferences.put(CardType.GOLD, 0.50);
		purchasePreferences.put(CardType.PROVINCE, 2.0);
		purchasePreferences.put(CardType.CURSE, -1.0);
		purchasePreferences.put(CardType.MOAT, -0.25);
		defaultPurchaseDecider = new TestDominionDecider(purchasePreferences);
		thiefPurchaseDecider = new TestThiefDominionDecider(purchasePreferences);
		
		p1.setPurchaseDecider(defaultPurchaseDecider);
		p2.setPurchaseDecider(defaultPurchaseDecider);
		p3.setPurchaseDecider(defaultPurchaseDecider);
		p4.setPurchaseDecider(defaultPurchaseDecider);
	}

	@Test
	public void cellarDiscardsAllVictoryCards() {
		for (int n=0; n<5; n++)
			p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		p1.insertCardDirectlyIntoHand(new Cellar());
		p1.takeActions();
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(p1.getDeckSize(), 5);
	}

	@Test
	public void cellarDiscardsNothingIfNoVictoryCards() {
		for (int n=0; n<5; n++)
			p1.takeCardFromSupplyIntoDiscard(CardType.COPPER);
		for (int n=0; n<3; n++)
			p1.discard(CardType.ESTATE);
		p1.insertCardDirectlyIntoHand(new Cellar());
		p1.takeActions();
		assertEquals(p1.getHandSize(), 7);
		assertEquals(p1.getDiscardSize(), 8);
		assertEquals(p1.getDeckSize(), 0);
	}

	@Test
	public void militiaForcesAllOtherPlayersToDiscardDownToThree() {
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		p2.discard(CardType.COPPER);
		p3.discard(CardType.COPPER);
		p3.discard(CardType.COPPER);
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
		for (int n=1; n<4; n++){
			copperBefore[n] = game.getPlayers()[n].getNumberOfTypeInHand(CardType.COPPER);
			estatesBefore[n] = game.getPlayers()[n].getNumberOfTypeInHand(CardType.ESTATE);
		}
		p1.takeActions();
		for (int n=1; n<4; n++){
			copperAfter[n] = game.getPlayers()[n].getNumberOfTypeInHand(CardType.COPPER);
			estatesAfter[n] = game.getPlayers()[n].getNumberOfTypeInHand(CardType.ESTATE);
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
			p2.trashCardFromHand(CardType.COPPER);
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
	public void remodelWithNothingToTrashDoesNotGainACard() {
		// defaultPurchaseDecider values Copper at 0.10, Estates at 0.09 and Silver at 0.30
		// With only Copper in hand after discarding Estates, the remodel choice will be:
		// COPPER into ESTATE
		// However this will lower the value of the hand overall, so will not be selected.

		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		boolean estatesToDiscard = true;
		int estatesDiscarded = -1;
		do {
			estatesToDiscard = p2.discard(CardType.ESTATE);
			estatesDiscarded++;
		} while (estatesToDiscard);
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5 - estatesDiscarded);
		assertEquals(p2.getDiscardSize(), estatesDiscarded);
		assertEquals(p2.getDeckSize(), 5);
	}

	@Test
	public void remodelTakesPurchaseDeciderIntoAccount() {
		// Following on from above test, we now have an ESTATE, so this will be happily remodelled into a SILVER
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		p2.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p2.takeActions();
		assertEquals(p2.getHandSize(), 5);
		assertEquals(p2.getDiscardSize(), 1);
		assertEquals(p2.getDeckSize(), 5);
		assertEquals(p2.totalTreasureValue(), 9);
		assertEquals(p2.totalVictoryValue(), 3);

		// Following on from above test, we should now Remodel a SMITHY into a GOLD
		p3.setActionDecider(remodelDecider);
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
		assertTrue(p3.isTakingActions());
	}

	@Test
	public void remodelDoesntTurnGoldIntoProvinceIfItStopsItBuyingAProvince() {
		for (CardType ct : p2.getCopyOfHand())
			p2.discard(ct);
		for (int i = 0; i < 3; i++)
			p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.GOLD));
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.ESTATE));
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		assertEquals(p2.totalTreasureValue(), 16);	// starting 7, plus 3 gold

		p2.takeActions();
		assertEquals(p2.getHandSize(), 3);	// Remodel played, Estate Remodeled to silver
		assertEquals(p2.getDiscardSize(), 6);	// Silver remodeled from Estate
		assertEquals(p2.totalTreasureValue(), 18);	// starting 7, plus Silver, plus 3 gold
		assertEquals(p2.totalVictoryValue(), 3); //minus Estate (starts with 3, one inserted into hand, one remodeled)

		for (CardType ct : p3.getCopyOfHand())
			p3.discard(ct);
		for (int i = 0; i < 4; i++)
			p3.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.GOLD));	// one extra Gold compared to above
		p3.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		p3.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.ESTATE));

		p3.takeActions();
		assertEquals(p3.getHandSize(), 4);	// Remodel played, Gold Remodeled to Province
		assertEquals(p3.getDiscardSize(), 6);	// Province remodeled from Gold
		assertEquals(p3.totalTreasureValue(), 16);	// starting 7, plus 4 gold, minus the remodeled gold
		assertEquals(p3.totalVictoryValue(), 10); // 3 starting estates, plus one inserted into hand, plus province
	}

	@Test
	public void workshopBuysUpToCostOfFour() {
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
	public void bureaucratPutsSilverOnOwnDeckAndVictoryCardsOnOpponentsAndIsCounteredByMoat() {
		// need a player with bureaucrat, and at least one other player with no victory cards
		// check that top card of Deck is then Silver
		// or victory card respectively (and that their hand sizes have reduced by one)
		p2.insertCardDirectlyIntoHand(new Bureaucrat());
		p1.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p1.insertCardDirectlyIntoHand(new Moat());
		p3.insertCardDirectlyIntoHand(new Card(CardType.DUCHY));

		int p4_estates = p4.getNumberOfTypeInHand(CardType.ESTATE);
		for (int i = 0; i < p4_estates; i++) {
			p4.discard(CardType.ESTATE);
		}
		int p3_estates = p3.getNumberOfTypeInHand(CardType.ESTATE);
		for (int i = 0; i < p3_estates; i++) {
			p3.discard(CardType.ESTATE);
		}
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
		p1.drawTopCardFromDeckIntoHand();
		p2.drawTopCardFromDeckIntoHand();
		p3.drawTopCardFromDeckIntoHand();
		assertEquals(p2.getNumberOfTypeInHand(CardType.SILVER), 1);
		assertEquals(p3.getNumberOfTypeInHand(CardType.DUCHY), 1);

	}

	@Test
	public void libraryWhenAllCardsDrawnAreNotActionCards() {
		p2.discard(CardType.COPPER);
		p2.insertCardDirectlyIntoHand(new Library());
		int startingDeckSize = p2.getDeckSize();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 7);
		assertEquals(p2.getDeckSize() - startingDeckSize, -3);
	}

	@Test
	public void libraryWhenActionCardsAreInThoseDrawn() {
		p2.discard(CardType.COPPER);
		p2.insertCardDirectlyIntoHand(new Library());
		p2.putCardFromHandOnTopOfDeck(CardType.MILITIA);
		p2.putCardFromHandOnTopOfDeck(CardType.BUREAUCRAT);
		int startingDeckSize = p2.getDeckSize();
		int startingDiscardSize = p2.getDiscardSize();
		p2.takeActions();
		assertEquals(p2.getHandSize(), 7);
		assertEquals(p2.getDeckSize() - startingDeckSize, -5);
		assertEquals(p2.getDiscardSize() - startingDiscardSize, 2);
	}

	@Test
	public void withVillageAndLibraryPlayVillageFirstAndKeepAnActionCard() {
		p2.discard(CardType.COPPER);
		p2.insertCardDirectlyIntoHand(new Library());
		p2.insertCardDirectlyIntoHand(new Card(CardType.VILLAGE));
		p2.putCardFromHandOnTopOfDeck(CardType.MILITIA);
		p2.putCardFromHandOnTopOfDeck(CardType.BUREAUCRAT);
		p2.putCardFromHandOnTopOfDeck(CardType.COPPER);		// to be picked up by Village
		int startingDeckSize = p2.getDeckSize();
		int startingDiscardSize = p2.getDiscardSize();
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
	public void throneRoomDoublesUpBasicCardInHandI() {
		p2.discard(CardType.COPPER);
		p2.discard(CardType.COPPER);
		p2.insertCardDirectlyIntoHand(new ThroneRoom());
		p2.insertCardDirectlyIntoHand(new Card(CardType.SMITHY));
		p2.takeActions();
		assertEquals(p2.getHandSize(), 9);	// Throne Room and Smithy Discarded from original 5, and then 6 picked up
	}

	@Test
	public void throneRoomDoublesUpBasicCardInHandII() {
		p2.discard(CardType.COPPER);
		p2.discard(CardType.COPPER);
		p2.putCardOnDiscard(new Card(CardType.COPPER));
		p2.putCardOnDiscard(new Card(CardType.COPPER));
		ThroneRoom throneRoom = new ThroneRoom();
		p2.insertCardDirectlyIntoHand(throneRoom);
		p2.insertCardDirectlyIntoHand(new Card(CardType.FESTIVAL));
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.takeActions();
		assertEquals(p2.getHandSize(), 12); // Starts with 10. ThroneRoom and Festival removed for -2; four Moats then played, giving net +4
		assertEquals(p2.getBuys(), 3);
		assertEquals(p2.remainingTreasureValueOfHand(), 12);	// so we have all treasure in hand, bar one copper left in deck, +4 from doubled Festival

		assertEquals(throneRoom.getAdditionalBuys(), 1);	// inheriting from Festival
		assertEquals(throneRoom.getAdditionalPurchasePower(), 2);	// inheriting from Festival
		p2.tidyUp();
		assertEquals(throneRoom.getAdditionalBuys(), 0);	// to confirm that Reset has occurred
		assertEquals(throneRoom.getAdditionalPurchasePower(), 0);	// to confirm that Reset has occurred
	}

	@Test
	public void throneRoomDoublesUpComplicatedCardInHand() {
		p2.discard(CardType.COPPER);
		p2.discard(CardType.COPPER);
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
		p2.discard(CardType.COPPER);
		p2.discard(CardType.COPPER);
		p2.insertCardDirectlyIntoHand(new ThroneRoom());
		p2.insertCardDirectlyIntoHand(new Card(CardType.SMITHY));
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.takeActions();
		assertEquals(p2.getHandSize(), 10);	// Throne Room and Smithy Discarded from original 6, and then 6 picked up. Moat is ignored
		assertEquals(p2.getNumberOfTypeInHand(CardType.MOAT), 1);
	}

	@Test
	public void chancellorMovesDiscardIntoDeckIfMoreThanSixProvincesLeft() {
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CHANCELLOR));
		p2.takeActions();
		assertEquals(p2.getDeckSize(), 0);
		assertEquals(p2.getDiscardSize(), 5);

		for (int i=0; i<6; i++)
			game.drawCard(CardType.PROVINCE);

		p3.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CHANCELLOR));
		p3.takeActions();
		assertEquals(p3.getDeckSize(), 5);
		assertEquals(p3.getDiscardSize(), 0);
	}

	@Test
	public void councilRoomCausesOtherPlayersToDrawOneCardEach() {
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.COUNCIL_ROOM));
		int[] handSizes = getAllHandSizes();
		p2.takeActions();
		assertEquals(p2.getDeckSize(), 1);
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p2.getHandSize(), 9);

		assertEquals(p1.getHandSize() - handSizes[0], 0);	// because p1 already has whole of deck in hand
		assertEquals(p3.getHandSize() - handSizes[2], 1);
		assertEquals(p4.getHandSize() - handSizes[3], 1);
	}

	@Test
	public void spyDiscardsVictoryAndCopperOnSelfAndComplementOnOthers() {
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
		assertEquals(p1.getDeckSize(), 1);
		assertEquals(p1.getHandSize(), 10);
	}

	@Test
	public void thiefTakesHighestValueTreasureCardIfNotCopper() {
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.THIEF));
		p2.setPurchaseDecider(thiefPurchaseDecider);

		p1.putCardOnTopOfDeck(CardType.SILVER);
		p1.putCardOnTopOfDeck(CardType.COPPER);
		p3.putCardOnTopOfDeck(CardType.COPPER);
		p3.putCardOnTopOfDeck(CardType.COPPER);
		p4.putCardOnTopOfDeck(CardType.ESTATE);
		p4.putCardOnTopOfDeck(CardType.ESTATE);

		p2.takeActions();

		assertEquals(p1.getDeckSize(), 0);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p2.getDeckSize(), 5);
		assertEquals(p2.getDiscardSize(), 0);
		assertEquals(p3.getDeckSize(), 5);
		assertEquals(p3.getDiscardSize(), 1);
		assertEquals(p4.getDeckSize(), 5);
		assertEquals(p4.getDiscardSize(), 2);

		assertEquals(p2.totalTreasureValue(), 7);
		assertEquals(p2.totalVictoryValue(), 3);
		assertEquals(p1.totalTreasureValue(), 8);
		assertEquals(p3.totalTreasureValue(), 8);
		assertEquals(p4.totalTreasureValue(), 7);
		assertEquals(p4.totalVictoryValue(), 5);
	}

	@Test
	public void adventurerTakesTwoTreasuresAndDiscardsRemainder() {
		Player[] players = game.getPlayers();
		for (int i = 1; i < 4; i++) {
			Player p = players[i];
			p.insertCardDirectlyIntoHand(new Adventurer());
			do {
				p.discard(CardType.COPPER);
			} while (p.remainingTreasureValueOfHand() > 0);
			int handSize = p.getHandSize();
			p.takeActions();
			assertEquals(p.getHandSize(), handSize + 1);	// minus Adventurer, plus two Copper
			assertEquals(p.remainingTreasureValueOfHand(), 2);
			assertEquals(p.totalTreasureValue(), 7);	// unchanged
			assertEquals(p.getDiscardSize(), 10 - p.getDeckSize() - p.getHandSize());
		}
	}

	@Test
	public void adventurerStopsWhenAllCardsDrawn() {
		p1.insertCardDirectlyIntoHand(new Adventurer());
		p1.takeCardFromSupplyIntoDiscard(CardType.VILLAGE);
		p1.takeCardFromSupplyIntoDiscard(CardType.SILVER);
		p1.takeActions();
		assertEquals(p1.getHandSize(), 11);	// minus Adventurer, plus one Silver
		assertEquals(p1.remainingTreasureValueOfHand(), 9);	// silver, plius starting copper
		assertEquals(p1.totalTreasureValue(), 9);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getDeckSize(), 0);
	}

	@Test
	public void chapelTrashesAllFourWhenAdvantageous() {
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
		p2.insertCardDirectlyIntoHand(new Chapel());
		p2.insertCardDirectlyIntoHand(new Moat());
		p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CURSE));
		assertEquals(p2.getNumberOfTypeTotal(CardType.CURSE), 1);
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
		HashMap<CardType, Double> purchasePreferences = new HashMap<CardType, Double>();
		purchasePreferences.put(CardType.COPPER, 0.10);
		purchasePreferences.put(CardType.ESTATE, 0.09);
		purchasePreferences.put(CardType.SILVER, 0.30);
		purchasePreferences.put(CardType.GOLD, 0.50);
		purchasePreferences.put(CardType.PROVINCE, 2.0);
		purchasePreferences.put(CardType.CURSE, -1.0);
		purchasePreferences.put(CardType.MOAT, -0.25);
		purchasePreferences.put(CardType.MINE, 0.40);
		defaultPurchaseDecider = new TestDominionDecider(purchasePreferences);
		p2.setPurchaseDecider(defaultPurchaseDecider);
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
		Moneylender moneylender = new Moneylender();
		p2.insertCardDirectlyIntoHand(moneylender);
		int copper = p2.getNumberOfTypeInHand(CardType.COPPER);
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 7);
		if (copper == 3) {
			p2.discard(CardType.COPPER);
			copper--;		// as three Copper means it's better not to trash Copper with Moneylender
		}
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
		p2.takeActions();
		assertEquals(moneylender.getAdditionalPurchasePower(), 3);
		assertEquals(p2.remainingTreasureValueOfHand(), copper + 2);
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 6);
		
		p2.buyCards();
		assertEquals(moneylender.getAdditionalPurchasePower(), 3);
		p2.tidyUp();
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
	}
	
	@Test
	public void moneylenderDoesNotTrashACopperWhenAdvantageous() {
		Moneylender moneylender = new Moneylender();
		p2.insertCardDirectlyIntoHand(moneylender);
		int copper = p2.getNumberOfTypeInHand(CardType.COPPER);
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 7);
		for (int i = 0; i < copper; i++)
			p2.discard(CardType.COPPER);
		for (int i = 0; i < 3; i++)
			p2.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.COPPER));
		p2.takeActions();
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
		assertEquals(p2.remainingTreasureValueOfHand(), 3);	// no bonus from Moneylender
		assertEquals(p2.getNumberOfTypeTotal(CardType.COPPER), 10);	// 3 inserted directly
	}
	
	@Test
	public void witchDrawsTwoCardsAndPutsCurseInOtherPlayersHands() {
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
		Game game = p2.getGame();
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
