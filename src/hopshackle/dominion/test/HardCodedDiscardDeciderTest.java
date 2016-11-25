package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.simulation.*;

import java.util.*;

import org.junit.*;

public class HardCodedDiscardDeciderTest {

	public DominionGame game;
	public Player p1, p2, p3, p4;
	TestDominionDecider militiaDecider;
	LookaheadDecider<Player> discardDecider;
	ArrayList<CardValuationVariables> variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
	ArrayList<CardType> actionsToUse = new ArrayList<CardType>(EnumSet.allOf(CardType.class));
	DominionStateFactory stateFactory = new DominionStateFactory(HopshackleUtilities.convertList(variablesToUse));
	
	@Before
	public void setup() {
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		p1 = game.getCurrentPlayer();
		p2 = game.getPlayer(2);
		p3 = game.getPlayer(3);
		p4 = game.getPlayer(4);
		discardDecider = new HardCodedDiscardDecider(stateFactory, actionsToUse);
		p1.setDecider(discardDecider);
		p2.setDecider(discardDecider);
		p3.setDecider(discardDecider);
		p4.setDecider(discardDecider);
		militiaDecider = TestDominionDecider.getExample(CardType.MILITIA);
	}
	
	@Test
	public void hardCodedDiscardDeciderDiscardsVictoryCardsBeforeCopperBeforeActionCards() {
		p1.setDecider(militiaDecider);
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		for (int i = 0; i<5; i++) {
			p2.discard(CardType.COPPER);
			p2.discard(CardType.ESTATE);
			p3.discard(CardType.COPPER);
			p3.discard(CardType.ESTATE);
			p4.discard(CardType.COPPER);
			p4.discard(CardType.ESTATE);
		}	// removes all cards from hand
		p2.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p2.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p2.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p2.insertCardDirectlyIntoHand(new Card(CardType.COPPER));
		p2.insertCardDirectlyIntoHand(new Card(CardType.COPPER));
		
		p3.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p3.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
		p3.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		p3.insertCardDirectlyIntoHand(new Card(CardType.COPPER));
		p3.insertCardDirectlyIntoHand(new Card(CardType.COPPER));
		
		p4.insertCardDirectlyIntoHand(new Card(CardType.COPPER));
		p4.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.MILITIA));
		p4.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
		p4.insertCardDirectlyIntoHand(new Card(CardType.GOLD));
		p4.insertCardDirectlyIntoHand(new Card(CardType.GOLD));
		
		assertEquals(p2.getHandSize(), 5);
		assertEquals(p3.getHandSize(), 5);
		assertEquals(p4.getHandSize(), 5);
		p1.takeActions();
		assertEquals(p2.getHandSize(), 3);
		assertEquals(p3.getHandSize(), 3);
		assertEquals(p4.getHandSize(), 3);
		
		assertEquals(p2.getNumberOfTypeInHand(CardType.ESTATE), 1);
		assertEquals(p2.getNumberOfTypeInHand(CardType.COPPER), 2);
		
		assertEquals(p3.getNumberOfTypeInHand(CardType.ESTATE), 0);
		assertEquals(p3.getNumberOfTypeInHand(CardType.COPPER), 1);	
		
		assertEquals(p4.getNumberOfTypeInHand(CardType.MILITIA), 0);
		assertEquals(p4.getNumberOfTypeInHand(CardType.COPPER), 0);
	}
	
	@Test
	public void crossWithReturnsTheSameDecider() {
		HardCodedDiscardDecider discardDecider2 = new HardCodedDiscardDecider(stateFactory, actionsToUse);
		Decider<Player> baby = discardDecider.crossWith(discardDecider2);
		assertTrue(baby instanceof HardCodedDiscardDecider);
		assertTrue(baby == discardDecider);
	}
}
