package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;

import java.util.HashMap;

import org.junit.*;

public class ScoringTest {

	private DominionGame game;
	private Player p1, p2, p3, p4;

	@Before
	public void setup() {
		game = new DominionGame(new DeciderGenerator(new GameSetup(), 1, 1, 0, 0), "Test",  false);
		p1 = game.getPlayer(1);
		p2 = game.getPlayer(2);
		p3 = game.getPlayer(3);
		p4 = game.getPlayer(4);
		game.addCardType(CardType.GARDENS, 12);
	}

	@Test
	public void basicVictoryCards() {
		assertEquals(p1.getScore(), 3, 0.001);
		p1.takeCardFromSupply(CardType.DUCHY, CardSink.DISCARD);
		assertEquals(p1.getScore(), 6, 0.001);
		p1.takeCardFromSupply(CardType.PROVINCE, CardSink.DISCARD);
		assertEquals(p1.getScore(), 12, 0.001);
		p1.takeCardFromSupply(CardType.CURSE, CardSink.DISCARD);
		assertEquals(p1.getScore(), 11, 0.001);
	}

	@Test
	public void winningPlayerDecidedCorrectly() {
		HashMap<CardType, Double> values = new HashMap<CardType, Double>();
		values.put(CardType.PROVINCE, 1.0);
		values.put(CardType.GOLD, 0.75);
		values.put(CardType.SILVER, 0.50);
		TestDominionDecider newDecider = new TestDominionDecider(values, new HashMap<CardType, Double>());
		for (int p=0; p<4; p++) {
			game.getAllPlayers().get(p).setDecider(newDecider);
			for (int i = 0; i < p; i++) {
				game.getPlayer(p+1).insertCardDirectlyIntoHand(CardFactory.instantiateCard(CardType.GOLD));
			}
		}
		game.playGame();
		Player[] players = game.getAllPlayers().toArray(new Player[1]);
		double[] scores = new double[4];
		for (int n = 0; n < 4; n++) {
			scores[n] = players[n].getScore();
		}
		double winningScore = 0.0;
		for (int n = 1; n <= 4; n++) {
			assertTrue(game.getPlayerInOrdinalPosition(n) >= 0);
			assertTrue(game.getPlayerInOrdinalPosition(n) < 5);
		}
		for (int n = 1; n <= 4; n++) {
			int position = game.getOrdinalPosition(n);
			assertTrue(game.getPlayerInOrdinalPosition(position) != 0);
			if (position == 1) {
				assertEquals(scores[n-1] - players[n-1].totalVictoryValue(), 50, 0.1);
				winningScore = scores[n-1];
				scores[n-1] = -100;
			}
		}
		for (int n = 0; n<4; n++) {
			assertTrue(scores[n] < winningScore);
		}
	}

	@Test
	public void winningPlayersDecidedCorrectlyInDraw() {
		for (int i = 0; i < 6; i++) {
			p2.takeCardFromSupply(CardType.PROVINCE, CardSink.DISCARD);
			p3.takeCardFromSupply(CardType.PROVINCE, CardSink.DISCARD);
		}
		game.playGame();
		assertEquals(game.getPlayerInOrdinalPosition(1), 3);
		assertEquals(game.getOrdinalPosition(2), 1);
		assertEquals(game.getOrdinalPosition(3), 1);
		assertEquals(game.getPlayerInOrdinalPosition(2), 0);
	}

	@Test
	public void gardensInDeckCorrectlyValued() {
		assertEquals(p4.getScore(), 3.0, 0.1);
		p4.insertCardDirectlyIntoHand(new Card(CardType.GARDENS));
		assertEquals(p4.getScore(), 4.0, 0.1);
		for (int i = 0; i < 8; i++) {
			p4.takeCardFromSupply(CardType.COPPER, CardSink.DISCARD);
			assertEquals(p4.getScore(), 4.0, 0.1);
		}
		for (int i = 0; i < 4; i++) {
			p4.takeCardFromSupply(CardType.COPPER, CardSink.DISCARD);
			assertEquals(p4.getScore(), 5.0, 0.1);
		}
		p4.takeCardFromSupply(CardType.GARDENS, CardSink.DISCARD);
		assertEquals(p4.getScore(), 7.0, 0.1);
	}
}
