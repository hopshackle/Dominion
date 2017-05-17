package hopshackle.dominion.test;

import static org.junit.Assert.*;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;
import org.junit.*;

import java.util.*;

public class PositionSummaryApplyTests {


    DominionGame game;
    Player p1, p2, p3, p4;
    private TestDominionDecider remodelDecider = TestDominionDecider.getExample(CardType.REMODEL);

    @Before
    public void setUp() {
        DeciderProperties localProp = SimProperties.getDeciderProperties("GLOBAL");
        localProp.setProperty("DominionCardSetup", "FirstGame");
        localProp.setProperty("DeciderType", "NNL");
        localProp.setProperty("Temperature", "0.0");
        game = new DominionGame(new DeciderGenerator(new GameSetup(), localProp), "Test", false);
        //	game.getCurrentPlayer().setState(Player.State.PLAYING);
        p1 = game.getCurrentPlayer();
    }

    @Test
    public void playSmithy() {
        p1.takeCardFromSupply(CardType.SMITHY, CardSink.HAND);
        PositionSummary start = p1.getPositionSummaryCopy();
        assertEquals(start.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(start.getNumberInHand(CardType.SMITHY), 1);
        assertEquals(start.getNumberPlayed(), 0);
        assertEquals(start.getHandSize(), 6);
        int startBudget = p1.getBudget();
        assertTrue(start.getCardInPlay().equals(CardType.NONE));

        PositionSummary post = start.apply(CardTypeAugment.playCard(CardType.SMITHY));
        assertEquals(post.getNumberInHand(CardType.UNKNOWN), 3);
        assertEquals(post.getNumberInHand(CardType.SMITHY), 0);
        assertEquals(post.getNumberPlayed(), 1);
        assertTrue(post.getCardInPlay().equals(CardType.SMITHY));
        assertEquals(post.getHandSize(), 8);
        assertEquals(post.getBudget(), startBudget);

        assertEquals(start.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(start.getNumberInHand(CardType.SMITHY), 1);
        assertEquals(start.getNumberPlayed(), 0);
        assertEquals(start.getHandSize(), 6);
        assertEquals(start.getBudget(), startBudget);
        assertTrue(start.getCardInPlay().equals(CardType.NONE));
    }

    @Test
    public void playMilitia() {
        p1.takeCardFromSupply(CardType.MILITIA, CardSink.HAND);
        PositionSummary start = p1.getPositionSummaryCopy();
        assertTrue(start.getPlayerState() == Player.State.PLAYING);
        assertEquals(start.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(start.getNumberInHand(CardType.MILITIA), 1);
        assertEquals(start.getNumberPlayed(), 0);
        assertEquals(start.getHandSize(), 6);
        int startBudget = p1.getBudget();

        PositionSummary post = start.apply(CardTypeAugment.playCard(CardType.MILITIA));
        assertTrue(post.getPlayerState() == Player.State.PLAYING);
        assertEquals(post.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(post.getNumberInHand(CardType.MILITIA), 0);
        assertEquals(post.getNumberPlayed(), 1);
        assertTrue(post.getCardInPlay().equals(CardType.MILITIA));
        assertEquals(post.getHandSize(), 5);
        assertEquals(post.getBudget(), startBudget +2);

        assertEquals(start.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(start.getNumberInHand(CardType.MILITIA), 1);
        assertEquals(start.getNumberPlayed(), 0);
        assertEquals(start.getHandSize(), 6);
        assertEquals(start.getBudget(), startBudget);
    }

    @Test
    public void playRemodel() {
        p1.takeCardFromSupply(CardType.REMODEL, CardSink.HAND);
        PositionSummary start = p1.getPositionSummaryCopy();
        assertEquals(start.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(start.getNumberInHand(CardType.REMODEL), 1);
        assertEquals(start.getNumberPlayed(), 0);
        assertEquals(start.getHandSize(), 6);
        assertEquals(start.getActions(), 1);
        int startBudget = p1.getBudget();

        PositionSummary post = start.apply(CardTypeAugment.playCard(CardType.REMODEL));
        assertEquals(post.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(post.getNumberInHand(CardType.REMODEL), 0);
        assertTrue(post.getCardInPlay().equals(CardType.REMODEL));
        assertEquals(post.getNumberPlayed(), 1);
        assertEquals(post.getHandSize(), 5);
        assertEquals(post.getActions(), 0);
        assertEquals(post.getBudget(), startBudget);

        assertEquals(start.getNumberInHand(CardType.UNKNOWN), 0);
        assertEquals(start.getNumberInHand(CardType.REMODEL), 1);
        assertEquals(start.getNumberPlayed(), 0);
        assertEquals(start.getHandSize(), 6);
        assertEquals(start.getBudget(), startBudget);
    }

    @Test
    public void playNone() {
        PositionSummary start = p1.getPositionSummaryCopy();
        assertEquals(start.getActions(), 1);
        assertTrue(start.getPlayerState() == Player.State.PLAYING);
        PositionSummary playNone = start.apply(CardTypeAugment.playCard(CardType.NONE));
        assertEquals(playNone.getActions(), 0);
        assertTrue(playNone.getPlayerState() == Player.State.PLAYING);
    }

    @Test
    public void cardInPlay() {
        /*
        This needs to be set when we instantiate a PS based on the last revealed card IFF we are still making
        play decisions for the card. Which we can't 100% check, but we can assume this if state is PLAY.
        A PS is always reset when we change state.
         */
        p1.takeCardFromSupply(CardType.REMODEL, CardSink.HAND);
        p1.setDecider(remodelDecider);
        PositionSummary start = p1.getPositionSummaryCopy();
        assertTrue(start.getCardInPlay() == CardType.NONE);
        game.oneAction(true, true);
        p1.refreshPositionSummary();
        PositionSummary later = p1.getPositionSummaryCopy();
 //       assertTrue(p1.getPlayerState() == Player.State.PLAYING);
        assertEquals(later.getNumberPlayed(), 1);
        assertEquals(later.getNumberPlayed(CardType.REMODEL), 1);
        assertTrue(later.getCardInPlay() == CardType.REMODEL);
        game.oneAction(false, false);
        assertTrue(p1.getPlayerState() == Player.State.PURCHASING);
        later = p1.getPositionSummaryCopy();
        assertTrue(later.getCardInPlay() == CardType.NONE);
    }
}
