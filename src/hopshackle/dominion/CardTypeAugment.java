package hopshackle.dominion;

import hopshackle.simulation.*;

public class CardTypeAugment implements ActionEnum<Player> {

    private static final long serialVersionUID = 1L;

    public enum CardSink {
        HAND, DISCARD, DECK, REVEALED, SUPPLY, TRASH;
    }

    /*
     * BUY is distinguished from MOVE in that it implies that money is spent, and a Buy slot is used
     */
    public enum ChangeType {
        MOVE, PLAY, ENTHRONE, REMODEL, CELLAR, CHANCELLOR, SPY, MONEYLENDER, BUY, NOCHANGE;
    }

    public CardType card;
    public CardSink from, to;
    public ChangeType type;
    public int target;

    public static CardTypeAugment playCard(CardType card) {
        return new CardTypeAugment(card, CardSink.HAND, CardSink.REVEALED, ChangeType.PLAY);
    }

    public static CardTypeAugment buyCard(CardType card) {
        return new CardTypeAugment(card, CardSink.SUPPLY, CardSink.DISCARD, ChangeType.BUY);
    }

    public static CardTypeAugment takeCard(CardType card) {
        return new CardTypeAugment(card, CardSink.SUPPLY, CardSink.DISCARD, ChangeType.MOVE);
    }

    public static CardTypeAugment moveCard(CardType card, CardSink from, CardSink to) {
        return new CardTypeAugment(card, from, to, ChangeType.MOVE);
    }

    public static CardTypeAugment trashCard(CardType card, CardSink from) {
        return new CardTypeAugment(card, from, CardSink.TRASH, ChangeType.MOVE);
    }

    public static CardTypeAugment discardCard(CardType card) {
        return new CardTypeAugment(card, CardSink.HAND, CardSink.DISCARD, ChangeType.MOVE);
    }

    public static CardTypeAugment drawCard() {
        return new CardTypeAugment(CardType.UNKNOWN, CardSink.DECK, CardSink.HAND, ChangeType.MOVE);
    }

    public CardTypeAugment(CardType card, CardSink from, CardSink to, ChangeType type) {
        this(card, from, to, type, 0);
    }

    public CardTypeAugment(CardType card, CardSink from, CardSink to, ChangeType type, int target) {
        this.card = card;
        this.from = from;
        this.to = to;
        this.type = type;
        this.target = target;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CardTypeAugment) {
            CardTypeAugment otherCTA = (CardTypeAugment) other;
            if (otherCTA.card == this.card && otherCTA.from == this.from && otherCTA.to == this.to && otherCTA.type == this.type)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return type.hashCode() * 17 + from.hashCode() * 5 + to.hashCode() * 953 + target + card.hashCode() * 6619;
    }

    @Override
    public String toString() {
        if (type == ChangeType.BUY) {
            return "Buys " + card.toString();
        } else if (type == ChangeType.PLAY) {
            return "Plays " + card.toString();
        } else if (type == ChangeType.ENTHRONE) {
            return "Enthrones " + card.toString();
        } else if (type == ChangeType.CHANCELLOR) {
            return "Reshuffles Discard into Deck";
        } else if (type == ChangeType.SPY) {
            return "Moves " + card.toString() + " to opponent " + to.toString();
        } else {
            if (from == CardSink.HAND && to == CardSink.DISCARD)
                return "Discards " + card.toString();
            if (from == CardSink.HAND && to == CardSink.TRASH)
                return "Trashes " + card.toString();
            if (from == CardSink.SUPPLY && to == CardSink.DISCARD)
                return "Gains " + card.toString();
            return "Moves " + card.toString() + " from " + from.toString() + " to " + to.toString();
        }
    }

    @Override
    public boolean isChooseable(Player p) {
        return true;
        // this is now not used, as possibleActions are determined via DominionGame
        // this is a dummy stub
    }

    @Override
    public Action<Player> getAction(Player p) {
        return new DominionAction(p, this);
    }

    @Override
    public String getChromosomeDesc() {
        return "DOM1";
    }

    @Override
    public Enum<CardType> getEnum() {
        return this.card;
    }

}


