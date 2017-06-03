package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.basecards.*;
import hopshackle.simulation.*;

import java.util.*;

import javafx.scene.control.Cell;
import org.junit.*;

public class CloneTests {

	public DominionGame game;
	TestDominionDecider woodcutterDecider, workshopDecider, remodelDecider, defaultPurchaseDecider, cellarDecider;
	private HashMap<CardType, Double> purchasePreferences = new HashMap<CardType, Double>();
	private HashMap<CardType, Double> handPreferences = new HashMap<CardType, Double>();
	private ArrayList<CardValuationVariables> variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
	private HardCodedActionDecider hardCodedActionDecider = new HardCodedActionDecider(variablesToUse);

	@Before
	public void setUp() throws Exception {
		DeciderProperties localProp = SimProperties.getDeciderProperties("GLOBAL");
		localProp.setProperty("DominionCardSetup", "FirstGame");
		localProp.setProperty("DeciderType", "NN");
		localProp.setProperty("Temperature", "0.0");
		game = new DominionGame(new DeciderGenerator(new GameSetup(), localProp), "Test",  false);
		woodcutterDecider = TestDominionDecider.getExample(CardType.WOODCUTTER);
		workshopDecider = TestDominionDecider.getExample(CardType.WORKSHOP);
		remodelDecider = TestDominionDecider.getExample(CardType.REMODEL);
		cellarDecider = TestDominionDecider.getExample(CardType.CELLAR);

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
		for (Player p : game.getAllPlayers()) p.setDecider(new DominionDeciderContainer(defaultPurchaseDecider, hardCodedActionDecider));
	}

	@Test
	public void cloneGame() {
		// I want to clone a game, and then run the game through to completion, and confirm the original is unaffected
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();
		game.nextPlayersTurn();

		List<List<CardType>> hand = new ArrayList<List<CardType>>();
		List<List<CardType>> deck = new ArrayList<List<CardType>>();
		List<List<CardType>> discard = new ArrayList<List<CardType>>();
		int[] deckSize = new int[4];
		int[] discardSize = new int[4];
		for (int i = 1; i <=4; i++) {
			Player player = game.getPlayer(i);
			assertEquals(player.getPositionSummaryCopy().getNumberOfCardsRemaining(CardType.PROVINCE), 12.0, 0.001);
			hand.add(player.getCopyOfHand());
			discard.add(player.getCopyOfDiscard());
			deck.add(player.getCopyOfDeck());
			deckSize[i-1] = player.getDeckSize();
			discardSize[i-1] = player.getDiscardSize();
		}
		Map<CardType, Integer> initialCardsAvailable = new HashMap<CardType, Integer>();
		for (CardType ct : game.availableCardsToPurchase()) {
			initialCardsAvailable.put(ct, game.getNumberOfCardsRemaining(ct));
		}
		DominionGame clonedGame = game.clone(game.getCurrentPlayer());
		int perspectivePlayer = game.getCurrentPlayerNumber();
		assertEquals(game.getCurrentPlayerNumber(), clonedGame.getCurrentPlayerNumber());

		assertEquals(clonedGame.turnNumber(), 3);
		assertEquals(game.turnNumber(), 3);

		for (int i = 1; i <=4; i++) {
			Player player = game.getPlayer(i);
			Player cloned = clonedGame.getPlayer(i);
			assertEquals(cloned.getPositionSummaryCopy().getNumberOfCardsRemaining(CardType.PROVINCE), 12.0, 0.001);
			// Test that players are different (i.e. we must clone these as well)
			assertFalse(player == cloned);
			// Test that perspective player's hand and discard are still the same
			List<CardType> clonedHand = cloned.getCopyOfHand();
			List<CardType> clonedDiscard = cloned.getCopyOfDiscard();
			List<CardType> clonedDeck = cloned.getCopyOfDeck();
			// Test that players discards are still the same
			assertEquals(discardSize[i-1], cloned.getDiscardSize());
			// Test that players' decks are still the same
			assertEquals(deckSize[i-1], cloned.getDeckSize());
			if (i == perspectivePlayer) {
				for (int j = 0; j < hand.get(i-1).size(); j++) {
					assertTrue(hand.get(i-1).get(j) == clonedHand.get(j));
				}	
				for (int j = 0; j < discard.get(i-1).size(); j++) {
					assertTrue(discard.get(i-1).get(j) == clonedDiscard.get(j));
				}	
				boolean identical = true;
				for (int j = 0; j < deck.get(i-1).size(); j++) {
					if (deck.get(i-1).get(j) != clonedDeck.get(j)) identical = false;
				}
				assertFalse(identical);
			} else {
				boolean identical = true;
				for (int j = 0; j < hand.get(i-1).size(); j++) {
					if (hand.get(i-1).get(j) != clonedHand.get(j)) identical = false;
				}
				assertFalse(identical);
				for (int j = 0; j < discard.get(i-1).size(); j++) {
					assertTrue(discard.get(i-1).get(j) == clonedDiscard.get(j));
				}	
				identical = true;
				for (int j = 0; j < deck.get(i-1).size(); j++) {
					if (deck.get(i-1).get(j) != clonedDeck.get(j)) identical = false;
				}
				assertFalse(identical);
			}
		}

		clonedGame.playGame();
		assertTrue(clonedGame.gameOver());
		assertFalse(game.gameOver());
		assertEquals(game.getPlayerInOrdinalPosition(1), 0);

		// Test we still have all the cards on the table
		for (CardType ct : initialCardsAvailable.keySet()) {
			assertEquals((int)initialCardsAvailable.get(ct), game.getNumberOfCardsRemaining(ct));
		}
		// And that the players still have exactly the same decks, hands and discards
		for (int i = 1; i <=4; i++) {
			Player player = game.getPlayer(i);
			List<CardType> finalHand = player.getCopyOfHand();
			List<CardType> finalDiscard = player.getCopyOfDiscard();
			List<CardType> finalDeck = player.getCopyOfDeck();
			// Test that players discards are still the same
			assertEquals(discardSize[i-1], player.getDiscardSize());
			// Test that players' decks are still the same
			assertEquals(deckSize[i-1], player.getDeckSize());
			for (int j = 0; j < hand.get(i-1).size(); j++) {
				assertTrue(hand.get(i-1).get(j) == finalHand.get(j));
			}
			for (int j = 0; j < discard.get(i-1).size(); j++) {
				assertTrue(discard.get(i-1).get(j) == finalDiscard.get(j));
			}	
			for (int j = 0; j < deck.get(i-1).size(); j++) {
				assertTrue(deck.get(i-1).get(j) == finalDeck.get(j));
			}
		}
	}

	@Test
	public void cloneGameAfterPlayingBeforeBuying() {
		Player p1 = game.getCurrentPlayer();
		assertEquals(game.getCurrentPlayerNumber(), 1);
		p1.insertCardDirectlyIntoHand(new Card(CardType.WOODCUTTER));
		assertEquals(p1.getCopyOfHand().size(), 6);
		p1.setDecider(woodcutterDecider);
		int oldBudget = p1.getBudget();
		game.oneAction();
		// Player has now played Woodcutter, leaving +1 Buy, and +2 purchasing power

		DominionGame clonedGame = game.clone(p1);
		assertEquals(clonedGame.getCurrentPlayerNumber(), 1);

		Player newPlayer1 = clonedGame.getPlayer(1);
		assertEquals(newPlayer1.getBudget(), oldBudget+2);
		assertEquals(newPlayer1.getBuys(), 2);
		assertEquals(newPlayer1.getCopyOfHand().size(), 5);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void cloneGameWhilePlaying() {
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(workshopDecider);
		p1.insertCardDirectlyIntoHand(new Card(CardType.WORKSHOP));
		p1.decide();
		Action<Player> nextAction = (Action<Player>) p1.getNextAction();
		assertTrue(nextAction.getType().getEnum() == CardType.WORKSHOP);

		nextAction.start();

		DominionGame clonedGame = game.clone(p1);
		assertEquals(clonedGame.getCurrentPlayerNumber(), 1);

		Player newPlayer1 = clonedGame.getPlayer(1);
		Action<Player> nextClonedAction = (Action<Player>) newPlayer1.getNextAction();
		assertTrue(nextClonedAction != null);
		assertTrue(nextClonedAction.getActor() == newPlayer1);
		assertTrue(nextAction.getType().getEnum() == CardType.WORKSHOP);
		assertTrue(nextClonedAction != nextAction);

		// clone a second time
		clonedGame = game.clone(p1);
		newPlayer1 = clonedGame.getPlayer(1);
		nextClonedAction = (Action<Player>) newPlayer1.getNextAction();
		assertTrue(nextClonedAction != null);
		assertTrue(nextClonedAction.getActor() == newPlayer1);
		assertTrue(nextAction.getType().getEnum() == CardType.WORKSHOP);
		assertTrue(nextClonedAction != nextAction);
	}
	
	@Test
	public void cloneOfAttackCard() {
		// I want to create an attack card, get it to mid-execution, and then clone it to a new Game
		Player p1 = game.getCurrentPlayer();
		DominionGame clonedGame = game.clone(p1);

		AttackCard militia = (AttackCard) CardFactory.instantiateCard(CardType.MILITIA);
		List<ActionEnum<Player>> defence = militia.takeAction(p1);
		assertFalse(defence.isEmpty());
		assertTrue(militia.nextActor() == game.getPlayer(2));
		DominionAction followOn = militia.followUpAction();	
		assertTrue(followOn != null);
		
		AttackCard clonedMilitia = militia.clone(clonedGame);
		assertTrue(clonedMilitia.nextActor() == clonedGame.getPlayer(2));
		assertTrue(clonedMilitia.nextActor() != militia.nextActor());
		DominionAction clonedFollowOn = clonedMilitia.followUpAction();	
		assertTrue(clonedFollowOn != null);
		assertTrue(clonedFollowOn.getActor() == clonedGame.getPlayer(1));
	}
	
	@Test
	public void cloneOfAttackCardAsPartOfGameClone() {
		Player p1 = game.getCurrentPlayer();
		AttackCard militia = (AttackCard) CardFactory.instantiateCard(CardType.MILITIA);
		p1.insertCardDirectlyIntoHand(militia);
		game.oneAction(false, true);

		DominionGame clonedGame = game.clone(p1);
		Player newPlayer = clonedGame.getPlayer(1);
		Card newCard = newPlayer.getCardLastPlayed();
		assertTrue(newCard instanceof Militia);
		AttackCard clonedMilitia = (AttackCard) newCard;
		assertTrue(clonedMilitia.nextActor() == clonedGame.getPlayer(2));
		assertTrue(clonedMilitia.nextActor() != militia.nextActor());
		DominionAction clonedFollowOn = clonedMilitia.followUpAction();	
		assertTrue(clonedFollowOn != null);
		assertTrue(clonedFollowOn.getActor() == clonedGame.getPlayer(1));

	}

	@Test
	public void cloneDuringMiddleOfRemodel() {
		Player p1 = game.getCurrentPlayer();
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.REMODEL));
		p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.ESTATE));
		p1.setDecider(new DominionDeciderContainer(defaultPurchaseDecider, remodelDecider));
		int estatesInHand = p1.getNumberOfTypeInHand(CardType.ESTATE);
		assertEquals(p1.getNumberOfTypeInHand(CardType.ESTATE), estatesInHand);
		game.oneAction(false, true); //PLAY card
		game.oneAction(false, true);
		// at this point we have just trashed a card, hopefully an ESTATE
		assertEquals(p1.getHandSize(), 5);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(p1.getDeckSize(), 5);
		assertEquals(p1.getNumberOfTypeInHand(CardType.ESTATE), estatesInHand-1);
		assertEquals(p1.getOneOffBudget(), 2);
		PositionSummary prePS = p1.getPositionSummaryCopy();
		assertEquals(prePS.getOneOffBudget(), 2);
		assertEquals(prePS.getNumberInHand(CardType.ESTATE), estatesInHand-1);
		assertEquals(prePS.getPercentageInDiscard(), 0.0, 0.001);
		assertEquals(prePS.getHandSize(), 5);

		DominionGame clonedGame = game.clone(p1);
		p1 = clonedGame.getCurrentPlayer();
		assertEquals(p1.getHandSize(), 5);
		assertEquals(p1.getDiscardSize(), 0);
		assertEquals(p1.getDeckSize(), 5);
		assertEquals(p1.getNumberOfTypeInHand(CardType.ESTATE), estatesInHand-1);
		assertEquals(p1.getOneOffBudget(), 2);
		prePS = p1.getPositionSummaryCopy();
		assertEquals(prePS.getOneOffBudget(), 2);
		assertEquals(prePS.getNumberInHand(CardType.ESTATE), estatesInHand-1);
		assertEquals(prePS.getPercentageInDiscard(), 0.0, 0.001);
		assertEquals(prePS.getHandSize(), 5);

		p1.takeActions();
		// and we should now have used that to buy a SILVER
		assertEquals(p1.getHandSize(), 5);
		assertEquals(p1.getDiscardSize(), 1);
		assertEquals(p1.getDeckSize(), 5);
		assertEquals(p1.getNumberOfTypeInHand(CardType.ESTATE), estatesInHand-1);
		assertEquals(p1.getOneOffBudget(), 0);
		assertEquals(p1.getNumberOfTypeTotal(CardType.SILVER), 1);

		PositionSummary postPS = p1.getPositionSummaryCopy();
		assertEquals(postPS.getOneOffBudget(), 0);
		assertEquals(postPS.getNumberInHand(CardType.ESTATE), estatesInHand-1);
		assertEquals(postPS.getPercentageInDiscard(), 1.0/12.0, 0.001);
		assertEquals(postPS.getHandSize(), 5);
	}

	@Test
	public void cloneInMiddleOfCellar() {
		Player p1 = game.getCurrentPlayer();
		p1.setDecider(new DominionDeciderContainer(defaultPurchaseDecider, cellarDecider));
		Player initialPlayer = p1;
		DominionGame initialGame = game;
		p1.insertCardDirectlyIntoHand(new Cellar());
		p1.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		p1.insertCardDirectlyIntoHand(new Card(CardType.ESTATE));
		int estatesInHand = p1.getNumberOfTypeInHand(CardType.ESTATE);
		assertEquals(p1.getHandSize(), 8);
		for (int i = 0; i <= estatesInHand; i++) {
			// i = 0 is Play CELLAR
			if (i == 2) { // so after discarding one estate
				// clone game
				game = game.clone(p1);
				p1 = game.getPlayer(1);
				Card inPlay = p1.getCardLastPlayed();
				assertTrue(inPlay instanceof Cellar);
			}
			game.oneAction(true, true);
			assertEquals(p1.getHandSize(), 7 - i);
			assertEquals(p1.getDiscardSize(), i);
			assertEquals(p1.getDeckSize(), 5);
			assertEquals(p1.getOneOffBudget(), i);
			assertEquals(CardValuationVariables.PERCENTAGE_DISCARD.getValue(p1), i / 13.0, 0.001);
		}
		game.oneAction(true, true);	// will draw up to full hand
		assertEquals(p1.getHandSize(), 7);
		assertEquals(p1.getDiscardSize(), estatesInHand);
		assertEquals(p1.getDeckSize(), 5 - estatesInHand);
	}

	@Test
	public void cloneInMiddleOfMoneyLender() {
		Moneylender moneylender = new Moneylender();
		Player p1 = game.getCurrentPlayer();
		purchasePreferences.put(CardType.COPPER, -0.50);
		p1.insertCardDirectlyIntoHand(moneylender);
		PositionSummary preps = p1.getPositionSummaryCopy();
		int copper = p1.getNumberOfTypeInHand(CardType.COPPER);
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);

		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
		game.oneAction(true, true); // Plays MONEYLENDER
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
		assertEquals(p1.getBudget(), copper);
		assertEquals(preps.getBudget(), copper);

		game = game.clone(p1);
		p1 = game.getPlayer(1);
		PositionSummary postps = p1.getPositionSummaryCopy();
		moneylender = (Moneylender) p1.getCardLastPlayed();

		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 7);
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
		assertEquals(p1.getBudget(), copper);
		assertEquals(postps.getBudget(), copper);

		game.oneAction(false, true); // Trashes COPPER, and moves to Buy Phase
		assertEquals(p1.getNumberOfTypeTotal(CardType.COPPER), 6);
		assertEquals(moneylender.getAdditionalPurchasePower(), 3);
		assertEquals(p1.getBudget(), copper + 2);

		p1.buyCards();
		assertEquals(moneylender.getAdditionalPurchasePower(), 3);
		p1.tidyUp();
		assertEquals(moneylender.getAdditionalPurchasePower(), 0);
	}

	@Test
	public void cloneInMiddleOfChapel() {
		Player p1 = game.getCurrentPlayer();
		p1.insertCardDirectlyIntoHand(new Chapel());
		p1.insertCardDirectlyIntoHand(new Moat());
		for (int i = 0; i < 5; i++)
			p1.insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.CURSE));
		assertEquals(p1.getNumberOfTypeTotal(CardType.CURSE), 5);
		for (int i = 0; i < 2; i++) {
			game.oneAction(true, true);
			// this should play Chapel, and trash one CURSE
		}
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getNumberOfTypeInHand(CardType.CURSE), 4);
		assertEquals(p1.getActionsLeft(), 0);
		DominionGame newGame = game.clone(p1);
		p1 = newGame.getCurrentPlayer();
		assertEquals(p1.getActionsLeft(), 0);
		assertEquals(p1.getHandSize(), 10);
		assertEquals(p1.getNumberOfTypeInHand(CardType.CURSE), 4);
		newGame.oneAction();
		assertEquals(p1.getNumberOfTypeInHand(CardType.MOAT), 1);
		assertEquals(p1.getNumberOfTypeInHand(CardType.CURSE), 1);
		assertEquals(p1.getNumberOfTypeTotal(CardType.CURSE), 1);
		assertEquals(p1.getHandSize(), 7);
		assertEquals(p1.getDiscardSize(), 0);
	}

}
