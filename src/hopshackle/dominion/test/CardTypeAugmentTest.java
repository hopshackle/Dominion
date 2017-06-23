package hopshackle.dominion.test;

import static org.junit.Assert.*;
import hopshackle.dominion.*;
import java.util.*;
import org.junit.*;

/**
 * Created by james on 23/06/2017.
 */
public class CardTypeAugmentTest {

    @Test
    public void cardTypeAugmentEquality() {
        CardTypeAugment buyWoodcutter1 = CardTypeAugment.buyCard(CardType.WOODCUTTER);
        CardTypeAugment buyMilitia1 = CardTypeAugment.buyCard(CardType.MILITIA);
        CardTypeAugment discardWoodcutter1 = CardTypeAugment.discardCard(CardType.WOODCUTTER);
        CardTypeAugment playWoodcutter1 = CardTypeAugment.playCard(CardType.WOODCUTTER);
        CardTypeAugment buyWoodcutter2 = CardTypeAugment.buyCard(CardType.WOODCUTTER);
        CardTypeAugment discardWoodcutter2 = CardTypeAugment.discardCard(CardType.WOODCUTTER);
        CardTypeAugment playWoodcutter2 = CardTypeAugment.playCard(CardType.WOODCUTTER);

        assertTrue(buyWoodcutter1.equals(buyWoodcutter2));
        assertTrue(discardWoodcutter1.equals(discardWoodcutter2));
        assertTrue(playWoodcutter1.equals(playWoodcutter2));

        assertFalse(buyWoodcutter1.equals(buyMilitia1));
        assertFalse(discardWoodcutter1.equals(buyWoodcutter1));
        assertFalse(playWoodcutter1.equals(buyWoodcutter2));
    }

    @Test
    public void cardTypeAugmentHashCode() {
        CardTypeAugment buyWoodcutter1 = CardTypeAugment.buyCard(CardType.WOODCUTTER);
        CardTypeAugment buyMilitia1 = CardTypeAugment.buyCard(CardType.MILITIA);
        CardTypeAugment discardWoodcutter1 = CardTypeAugment.discardCard(CardType.WOODCUTTER);
        CardTypeAugment playWoodcutter1 = CardTypeAugment.playCard(CardType.WOODCUTTER);
        CardTypeAugment buyWoodcutter2 = CardTypeAugment.buyCard(CardType.WOODCUTTER);
        CardTypeAugment discardWoodcutter2 = CardTypeAugment.discardCard(CardType.WOODCUTTER);
        CardTypeAugment playWoodcutter2 = CardTypeAugment.playCard(CardType.WOODCUTTER);

        assertEquals(buyWoodcutter1.hashCode(), buyWoodcutter2.hashCode());
        assertEquals(discardWoodcutter1.hashCode(), discardWoodcutter2.hashCode());
        assertEquals(playWoodcutter1.hashCode(), playWoodcutter2.hashCode());

        assertNotEquals(buyWoodcutter1.hashCode(), buyMilitia1.hashCode());
        assertNotEquals(discardWoodcutter1.hashCode(), buyWoodcutter1.hashCode());
        assertNotEquals(playWoodcutter1.hashCode(), buyWoodcutter2.hashCode());
    }

    @Test
    public void cardTypeListEquality() {
        List<CardType> list1 = new ArrayList<CardType>();
        list1.add(CardType.COPPER);
        list1.add(CardType.CELLAR);
        List<CardType> list2 = new ArrayList<CardType>();
        list2.add(CardType.COPPER);
        list2.add(CardType.CELLAR);
        List<CardType> list3 = new ArrayList<CardType>();
        list3.add(CardType.CELLAR);
        list3.add(CardType.COPPER);
        List<CardType> list4 = new ArrayList<CardType>();
        list4.add(CardType.MILITIA);
        list4.add(CardType.CELLAR);
        CardTypeList oneBuy = new CardTypeList(list1, CardTypeAugment.ChangeType.BUY);
        CardTypeList oneDiscard = new CardTypeList(list1, CardTypeAugment.ChangeType.MOVE);
        CardTypeList two = new CardTypeList(list2, CardTypeAugment.ChangeType.BUY);
        CardTypeList three = new CardTypeList(list3, CardTypeAugment.ChangeType.BUY);
        CardTypeList four = new CardTypeList(list4, CardTypeAugment.ChangeType.BUY);

        assertTrue(oneBuy.equals(two));
        assertFalse(oneBuy.equals(oneDiscard));
        assertTrue(oneBuy.equals(three));
        assertTrue(two.equals(three));
        assertFalse(two.equals(four));
    }

    @Test
    public void cardTypeListHashCode() {
        List<CardType> list1 = new ArrayList<CardType>();
        list1.add(CardType.COPPER);
        list1.add(CardType.CELLAR);
        List<CardType> list2 = new ArrayList<CardType>();
        list2.add(CardType.COPPER);
        list2.add(CardType.CELLAR);
        List<CardType> list3 = new ArrayList<CardType>();
        list3.add(CardType.CELLAR);
        list3.add(CardType.COPPER);
        List<CardType> list4 = new ArrayList<CardType>();
        list4.add(CardType.MILITIA);
        list4.add(CardType.CELLAR);
        CardTypeList oneBuy = new CardTypeList(list1, CardTypeAugment.ChangeType.BUY);
        CardTypeList oneDiscard = new CardTypeList(list1, CardTypeAugment.ChangeType.MOVE);
        CardTypeList two = new CardTypeList(list2, CardTypeAugment.ChangeType.BUY);
        CardTypeList three = new CardTypeList(list3, CardTypeAugment.ChangeType.BUY);
        CardTypeList four = new CardTypeList(list4, CardTypeAugment.ChangeType.BUY);

        assertEquals(oneBuy.hashCode(), two.hashCode());
        assertNotEquals(oneBuy.hashCode(), oneDiscard.hashCode());
        assertEquals(oneBuy.hashCode(), three.hashCode());
        assertEquals(two.hashCode(),three.hashCode());
        assertNotEquals(two.hashCode(), four.hashCode());
    }

    @Test
    public void cardTypeListEqualityAndHashForMine() {
        List<CardTypeAugment> mineSilver1 = new ArrayList<CardTypeAugment>();
        mineSilver1.add(CardTypeAugment.moveCard(CardType.GOLD, CardTypeAugment.CardSink.SUPPLY, CardTypeAugment.CardSink.HAND));
        mineSilver1.add(CardTypeAugment.trashCard(CardType.SILVER, CardTypeAugment.CardSink.HAND));

        List<CardTypeAugment> mineSilver2 = new ArrayList<CardTypeAugment>();
        mineSilver2.add(CardTypeAugment.moveCard(CardType.GOLD, CardTypeAugment.CardSink.SUPPLY, CardTypeAugment.CardSink.HAND));
        mineSilver2.add(CardTypeAugment.trashCard(CardType.SILVER, CardTypeAugment.CardSink.HAND));

        List<CardTypeAugment> mineCopper = new ArrayList<CardTypeAugment>();
        mineCopper.add(CardTypeAugment.moveCard(CardType.SILVER, CardTypeAugment.CardSink.SUPPLY, CardTypeAugment.CardSink.HAND));
        mineCopper.add(CardTypeAugment.trashCard(CardType.COPPER, CardTypeAugment.CardSink.HAND));

        assertTrue(mineSilver1.equals(mineSilver2));
        assertEquals(mineSilver1.hashCode(), mineSilver2.hashCode());
        assertFalse(mineSilver1.equals(mineCopper));
        assertNotEquals(mineSilver1.hashCode(), mineCopper.hashCode());
    }
}
