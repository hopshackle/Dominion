package hopshackle.dominion;

import java.util.ArrayList;
import java.util.List;

import hopshackle.simulation.*;

public enum CardValuationVariables implements GeneticVariable<Player> {
    DECK_SIZE,
    PROVINCES_BOUGHT,
    DUCHIES_BOUGHT,
    MOST_DEPLETED_PILE,
    SECOND_DEPLETED_PILE,
    THIRD_DEPLETED_PILE,
    VICTORY_POINTS,
    VICTORY_MARGIN,
    WEALTH_DENSITY,
    VICTORY_DENSITY,
    PERCENT_VICTORY,
    PERCENT_ACTION,
    PERCENT_TREASURE,
    COPPER_IN_HAND,
    COPPER_PERCENT,
    SILVER_IN_HAND,
    SILVER_PERCENT,
    GOLD_IN_HAND,
    GOLD_PERCENT,
    ESTATES_IN_HAND,
    DUCHIES_IN_HAND,
    PROVINCES_IN_HAND,
    PROVINCES_TOTAL,
    CURSES_IN_HAND,
    ADVENTURER_PERCENT,
    ADVENTURERS_IN_HAND,
    ADVENTURER_IN_PLAY,
    BUREAUCRAT_PERCENT,
    BUREAUCRATS_IN_HAND,
    BUREAUCRATS_BOUGHT,
    BUREAUCRATS_PLAYED,
    CELLAR_PERCENT,
    CELLARS_IN_HAND,
    CELLAR_IN_PLAY,
    CHAPEL_PERCENT,
    CHAPELS_IN_HAND,
    CHAPEL_IN_PLAY,
    CHANCELLOR_PERCENT,
    CHANCELLORS_IN_HAND,
    COUNCIL_ROOM_PERCENT,
    COUNCIL_ROOMS_IN_HAND,
    COUNCIL_ROOMS_BOUGHT,
    COUNCIL_ROOMS_PLAYED,
    FEAST_PERCENT,
    FEASTS_IN_HAND,
    FEASTS_PLAYED,
    FESTIVAL_PERCENT,
    FESTIVALS_IN_HAND,
    GARDENS_PERCENT,
    LABORATORY_PERCENT,
    LABORATORIES_IN_HAND,
    LIBRARY_PERCENT,
    LIBRARIES_IN_HAND,
    MARKET_PERCENT,
    MARKETS_IN_HAND,
    MILITIA_PERCENT,
    MILITIA_IN_HAND,
    MILITIA_BOUGHT,
    MILITIA_PLAYED,
    MINE_PERCENT,
    MINES_IN_HAND,
    MOAT_PERCENT,
    MOATS_IN_HAND,
    MOATS_BOUGHT,
    MONEYLENDER_PERCENT,
    MONEYLENDERS_IN_HAND,
    MONEYLENDER_IN_PLAY,
    REMODEL_PERCENT,
    REMODELS_IN_HAND,
    REMODEL_IN_PLAY,
    THRONE_ROOM_PERCENT,
    THRONE_ROOMS_IN_HAND,
    THRONE_ROOM_IN_PLAY,
    SMITHY_PERCENT,
    SMITHIES_IN_HAND,
    SPY_PERCENT,
    SPIES_IN_HAND,
    SPIES_BOUGHT,
    SPIES_PLAYED,
    SPY_IN_PLAY,
    THIEF_PERCENT,
    THIEVES_IN_HAND,
    THIEVES_BOUGHT,
    THIEVES_PLAYED,
    THIEF_IN_PLAY,
    VILLAGE_PERCENT,
    VILLAGES_IN_HAND,
    WITCH_PERCENT,
    WITCHES_IN_HAND,
    WITCHES_BOUGHT,
    WOODCUTTER_PERCENT,
    WOODCUTTERS_IN_HAND,
    WORKSHOP_PERCENT,
    WORKSHOPS_IN_HAND,
    WORKSHOP_IN_PLAY,
    PERCENTAGE_DISCARD,
    TURNS,
    CURSE_PERCENT,
    ESTATE_PERCENT,
    DUCHY_PERCENT,
    BUY_PHASE,
    BUYS,
    ACTIONS,
    BUDGET,
    HAND_SIZE,
    UNKNOWN_IN_HAND,
    ACTION_CARDS_PLAYED,
    PLAYER_NUMBER,
    CURRENT_FEATURE;

    public double getValue(Player a) {
        if (a instanceof Player) {
            return getValue(((Player) a).getPositionSummaryCopy());
        } else {
            return 0.0;
        }
    }

    public double getValue(PositionSummary ps) {
        switch (this) {
            case DECK_SIZE:
                double totalCards = ps.totalNumberOfCards();
                return ((totalCards - 10.0) / 20.0);
            case PROVINCES_BOUGHT:
                return (12.0 - ps.getNumberOfCardsRemaining(CardType.PROVINCE)) / 12.0;
            case DUCHIES_BOUGHT:
                return (12.0 - ps.getNumberOfCardsRemaining(CardType.DUCHY)) / 12.0;
            case VICTORY_MARGIN:
                return (ps.getVictoryMargin() / 10.0);
            case VICTORY_POINTS:
                return ps.getVictoryDensity() * ps.totalNumberOfCards() / 50.0;
            case WEALTH_DENSITY:
                return (ps.getWealthDensity()) / 2.0;
            case VICTORY_DENSITY:
                return (ps.getVictoryDensity()) / 2.0;
            case PERCENT_VICTORY:
                return ps.getPercentVictory() * 2.0;
            case PERCENT_ACTION:
                return ps.getPercentAction() * 2.0;
            case PERCENT_TREASURE:
                return (1.00 - ps.getPercentAction() - ps.getPercentVictory()) * 2.0;
            case MOST_DEPLETED_PILE:
                return 1.0 - ps.getPercentageDepleted()[0];
            case SECOND_DEPLETED_PILE:
                return 1.0 - ps.getPercentageDepleted()[1];
            case THIRD_DEPLETED_PILE:
                return 1.0 - ps.getPercentageDepleted()[2];
            case PERCENTAGE_DISCARD:
                return ps.getPercentageInDiscard();
            case TURNS:
                return ps.getTurns() / 40.0;
            case MARKET_PERCENT:
                return ps.getPercent(CardType.MARKET) * 5.0;
            case MARKETS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.MARKET) / 5.0;
            case MILITIA_PERCENT:
                return ps.getPercent(CardType.MILITIA) * 5.0;
            case MILITIA_IN_HAND:
                return (double) ps.getNumberInHand(CardType.MILITIA) / 5.0;
            case MILITIA_BOUGHT:
                return (10.0 - ps.getNumberOfCardsRemaining(CardType.MILITIA)) / 10.0;
            case MILITIA_PLAYED:
                return Math.min(ps.getNumberPlayed(CardType.MILITIA), 1);
            case MINE_PERCENT:
                return ps.getPercent(CardType.MINE) * 5.0;
            case MINES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.MINE) / 5.0;
            case MOAT_PERCENT:
                return ps.getPercent(CardType.MOAT) * 5.0;
            case MOATS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.MOAT) / 5.0;
            case MOATS_BOUGHT:
                return (10.0 - ps.getNumberOfCardsRemaining(CardType.MOAT)) / 10.0;
            case REMODEL_PERCENT:
                return ps.getPercent(CardType.REMODEL) * 5.0;
            case REMODELS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.REMODEL) / 5.0;
            case REMODEL_IN_PLAY:
                return isCardInPlay(ps, CardType.REMODEL);
            case SMITHY_PERCENT:
                return ps.getPercent(CardType.SMITHY) * 5.0;
            case SMITHIES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.SMITHY) / 5.0;
            case VILLAGE_PERCENT:
                return ps.getPercent(CardType.VILLAGE) * 5.0;
            case VILLAGES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.VILLAGE) / 5.0;
            case WOODCUTTER_PERCENT:
                return ps.getPercent(CardType.WOODCUTTER) * 5.0;
            case WOODCUTTERS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.WOODCUTTER) / 5.0;
            case WORKSHOP_PERCENT:
                return ps.getPercent(CardType.WORKSHOP) * 5.0;
            case WORKSHOPS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.WORKSHOP) / 5.0;
            case WORKSHOP_IN_PLAY:
                return isCardInPlay(ps, CardType.WORKSHOP);
            case CELLAR_PERCENT:
                return ps.getPercent(CardType.CELLAR) * 5.0;
            case CELLARS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.CELLAR) / 5.0;
            case CELLAR_IN_PLAY:
                return isCardInPlay(ps, CardType.CELLAR);
            case BUREAUCRAT_PERCENT:
                return ps.getPercent(CardType.BUREAUCRAT) * 5.0;
            case BUREAUCRATS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.BUREAUCRAT) / 5.0;
            case BUREAUCRATS_BOUGHT:
                return (10.0 - ps.getNumberOfCardsRemaining(CardType.BUREAUCRAT)) / 10.0;
            case BUREAUCRATS_PLAYED:
                return ps.getNumberPlayed(CardType.BUREAUCRAT) / 2.0;
            case FESTIVAL_PERCENT:
                return ps.getPercent(CardType.FESTIVAL) * 5.0;
            case FESTIVALS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.FESTIVAL) / 5.0;
            case LIBRARY_PERCENT:
                return ps.getPercent(CardType.LIBRARY) * 5.0;
            case LIBRARIES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.LIBRARY) / 5.0;
            case THRONE_ROOM_PERCENT:
                return ps.getPercent(CardType.THRONE_ROOM) * 5.0;
            case THRONE_ROOMS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.THRONE_ROOM) / 5.0;
            case THRONE_ROOM_IN_PLAY:
                return isCardInPlay(ps, CardType.THRONE_ROOM);
            case COPPER_IN_HAND:
                return (double) ps.getNumberInHand(CardType.COPPER) / 5.0;
            case COPPER_PERCENT:
                return ps.getPercent(CardType.COPPER) * 2.0;
            case SILVER_IN_HAND:
                return (double) ps.getNumberInHand(CardType.SILVER) / 5.0;
            case SILVER_PERCENT:
                return ps.getPercent(CardType.SILVER) * 2.0;
            case GOLD_IN_HAND:
                return (double) ps.getNumberInHand(CardType.GOLD) / 5.0;
            case GOLD_PERCENT:
                return ps.getPercent(CardType.GOLD) * 2.0;
            case CURSE_PERCENT:
                return ps.getPercent(CardType.CURSE) * 2.0;
            case ESTATE_PERCENT:
                return ps.getPercent(CardType.ESTATE) * 2.0;
            case DUCHY_PERCENT:
                return ps.getPercent(CardType.DUCHY) * 2.0;
            case GARDENS_PERCENT:
                return ps.getPercent(CardType.GARDENS) * 2.0;
            case ESTATES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.ESTATE) / 5.0;
            case DUCHIES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.DUCHY) / 5.0;
            case PROVINCES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.PROVINCE) / 5.0;
            case PROVINCES_TOTAL:
                return (double) ps.getNumberOfCardsTotal(CardType.PROVINCE) / 6.0;
            case CURSES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.CURSE) / 5.0;
            case SPY_PERCENT:
                return ps.getPercent(CardType.SPY) * 5.0;
            case SPIES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.SPY) / 5.0;
            case SPIES_BOUGHT:
                return (10.0 - ps.getNumberOfCardsRemaining(CardType.SPY)) / 10.0;
            case SPIES_PLAYED:
                return ps.getNumberPlayed(CardType.SPY) / 2.0;
            case SPY_IN_PLAY:
                return isCardInPlay(ps, CardType.SPY);
            case THIEF_PERCENT:
                return ps.getPercent(CardType.THIEF) * 5.0;
            case THIEVES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.THIEF) / 5.0;
            case THIEVES_BOUGHT:
                return (10.0 - ps.getNumberOfCardsRemaining(CardType.THIEF)) / 10.0;
            case THIEF_IN_PLAY:
                return isCardInPlay(ps, CardType.THIEF);
            case THIEVES_PLAYED:
                return ps.getNumberPlayed(CardType.THIEF) / 2.0;
            case CHANCELLOR_PERCENT:
                return ps.getPercent(CardType.CHANCELLOR) * 5.0;
            case CHANCELLORS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.CHANCELLOR) / 5.0;
            case COUNCIL_ROOM_PERCENT:
                return ps.getPercent(CardType.COUNCIL_ROOM) * 5.0;
            case COUNCIL_ROOMS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.COUNCIL_ROOM) / 5.0;
            case COUNCIL_ROOMS_BOUGHT:
                return (10.0 - ps.getNumberOfCardsRemaining(CardType.COUNCIL_ROOM)) / 10.0;
            case COUNCIL_ROOMS_PLAYED:
                return ps.getNumberPlayed(CardType.COUNCIL_ROOM) / 2.0;
            case ADVENTURER_PERCENT:
                return ps.getPercent(CardType.ADVENTURER) * 5.0;
            case ADVENTURERS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.ADVENTURER) / 5.0;
            case ADVENTURER_IN_PLAY:
                return isCardInPlay(ps, CardType.ADVENTURER);
            case CHAPEL_PERCENT:
                return ps.getPercent(CardType.CHAPEL) * 5.0;
            case CHAPELS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.CHAPEL) / 5.0;
            case CHAPEL_IN_PLAY:
                return isCardInPlay(ps, CardType.CHAPEL);
            case FEAST_PERCENT:
                return ps.getPercent(CardType.FEAST) * 5.0;
            case FEASTS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.FEAST) / 5.0;
            case FEASTS_PLAYED:
                return ps.getNumberPlayed(CardType.FEAST) / 2.0;
            case LABORATORY_PERCENT:
                return ps.getPercent(CardType.FEAST) * 5.0;
            case LABORATORIES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.FEAST) / 5.0;
            case MONEYLENDER_PERCENT:
                return ps.getPercent(CardType.FEAST) * 5.0;
            case MONEYLENDERS_IN_HAND:
                return (double) ps.getNumberInHand(CardType.FEAST) / 5.0;
            case MONEYLENDER_IN_PLAY:
                return isCardInPlay(ps, CardType.MONEYLENDER);
            case WITCH_PERCENT:
                return ps.getPercent(CardType.WITCH) * 5.0;
            case WITCHES_IN_HAND:
                return (double) ps.getNumberInHand(CardType.WITCH) / 5.0;
            case WITCHES_BOUGHT:
                return (10.0 - ps.getNumberOfCardsRemaining(CardType.WITCH)) / 10.0;
            case UNKNOWN_IN_HAND:
                return ps.getNumberInHand(CardType.UNKNOWN) / 5.0;
            case ACTIONS:
                return ps.getActions() / 5.0;
            case BUY_PHASE:
                if (ps.getPlayerState() == Player.State.PURCHASING)
                    return 1.0;
                return 0.0;
            case BUYS:
                return ps.getBuys() / 3.0;
            case HAND_SIZE:
                return ps.getHandSize() / 10.0;
            case BUDGET:
                return ps.getBudget() / 16.0;
            case ACTION_CARDS_PLAYED:
                return ps.getNumberPlayed() / 5.0;
            case PLAYER_NUMBER:
                return ps.getPlayer().getActorRef();
            case CURRENT_FEATURE:
                return ps.getOneOffBudget() / 8.0;
        }
        return 0.0;
    }

    @Override
    public boolean unitaryRange() {
        return true;
    }

    public String getDescriptor() {
        return "DOM1";
    }

    public CardType relatedCardType() {
        switch (this) {
            case ADVENTURER_PERCENT:
            case ADVENTURERS_IN_HAND:
            case ADVENTURER_IN_PLAY:
                return CardType.ADVENTURER;
            case BUREAUCRATS_IN_HAND:
            case BUREAUCRAT_PERCENT:
            case BUREAUCRATS_BOUGHT:
            case BUREAUCRATS_PLAYED:
                return CardType.BUREAUCRAT;
            case CELLARS_IN_HAND:
            case CELLAR_PERCENT:
            case CELLAR_IN_PLAY:
                return CardType.CELLAR;
            case CHAPEL_PERCENT:
            case CHAPELS_IN_HAND:
            case CHAPEL_IN_PLAY:
                return CardType.CHAPEL;
            case CHANCELLOR_PERCENT:
            case CHANCELLORS_IN_HAND:
                return CardType.CHANCELLOR;
            case COUNCIL_ROOM_PERCENT:
            case COUNCIL_ROOMS_IN_HAND:
            case COUNCIL_ROOMS_BOUGHT:
            case COUNCIL_ROOMS_PLAYED:
                return CardType.COUNCIL_ROOM;
            case FEASTS_IN_HAND:
            case FEAST_PERCENT:
            case FEASTS_PLAYED:
                return CardType.FEAST;
            case FESTIVALS_IN_HAND:
            case FESTIVAL_PERCENT:
                return CardType.FESTIVAL;
            case GARDENS_PERCENT:
                return CardType.GARDENS;
            case LABORATORIES_IN_HAND:
            case LABORATORY_PERCENT:
                return CardType.LABORATORY;
            case LIBRARIES_IN_HAND:
            case LIBRARY_PERCENT:
                return CardType.LIBRARY;
            case MARKETS_IN_HAND:
            case MARKET_PERCENT:
                return CardType.MARKET;
            case MILITIA_IN_HAND:
            case MILITIA_PERCENT:
            case MILITIA_BOUGHT:
            case MILITIA_PLAYED:
                return CardType.MILITIA;
            case MINES_IN_HAND:
            case MINE_PERCENT:
                return CardType.MINE;
            case MOATS_IN_HAND:
            case MOAT_PERCENT:
            case MOATS_BOUGHT:
                return CardType.MOAT;
            case MONEYLENDERS_IN_HAND:
            case MONEYLENDER_PERCENT:
            case MONEYLENDER_IN_PLAY:
                return CardType.MONEYLENDER;
            case REMODELS_IN_HAND:
            case REMODEL_PERCENT:
            case REMODEL_IN_PLAY:
                return CardType.REMODEL;
            case SMITHIES_IN_HAND:
            case SMITHY_PERCENT:
                return CardType.SMITHY;
            case SPIES_IN_HAND:
            case SPIES_BOUGHT:
            case SPY_PERCENT:
            case SPIES_PLAYED:
            case SPY_IN_PLAY:
                return CardType.SPY;
            case THIEF_PERCENT:
            case THIEVES_IN_HAND:
            case THIEVES_BOUGHT:
            case THIEVES_PLAYED:
            case THIEF_IN_PLAY:
                return CardType.THIEF;
            case THRONE_ROOMS_IN_HAND:
            case THRONE_ROOM_PERCENT:
            case THRONE_ROOM_IN_PLAY:
                return CardType.THRONE_ROOM;
            case VILLAGES_IN_HAND:
            case VILLAGE_PERCENT:
                return CardType.VILLAGE;
            case WOODCUTTERS_IN_HAND:
            case WOODCUTTER_PERCENT:
                return CardType.WOODCUTTER;
            case WORKSHOPS_IN_HAND:
            case WORKSHOP_PERCENT:
            case WORKSHOP_IN_PLAY:
                return CardType.WORKSHOP;
            case WITCH_PERCENT:
            case WITCHES_BOUGHT:
            case WITCHES_IN_HAND:
                return CardType.WITCH;
            default:
                break;
        }
        return null;
    }

    public boolean isHandVariable() {
        switch (this) {
            case ADVENTURERS_IN_HAND:
            case ADVENTURER_IN_PLAY:
            case BUREAUCRATS_IN_HAND:
            case BUREAUCRATS_PLAYED:
            case CELLARS_IN_HAND:
            case CELLAR_IN_PLAY:
            case CHAPELS_IN_HAND:
            case CHAPEL_IN_PLAY:
            case CHANCELLORS_IN_HAND:
            case COUNCIL_ROOMS_IN_HAND:
            case COUNCIL_ROOMS_PLAYED:
            case COPPER_IN_HAND:
            case CURSES_IN_HAND:
            case DUCHIES_IN_HAND:
            case ESTATES_IN_HAND:
            case FEASTS_IN_HAND:
            case FEASTS_PLAYED:
            case FESTIVALS_IN_HAND:
            case GOLD_IN_HAND:
            case LABORATORIES_IN_HAND:
            case LIBRARIES_IN_HAND:
            case MARKETS_IN_HAND:
            case MILITIA_IN_HAND:
            case MILITIA_PLAYED:
            case MINES_IN_HAND:
            case MOATS_IN_HAND:
            case MONEYLENDERS_IN_HAND:
            case MONEYLENDER_IN_PLAY:
            case PROVINCES_IN_HAND:
            case REMODELS_IN_HAND:
            case REMODEL_IN_PLAY:
            case SILVER_IN_HAND:
            case SMITHIES_IN_HAND:
            case SPIES_IN_HAND:
            case SPIES_PLAYED:
            case SPY_IN_PLAY:
            case THIEVES_IN_HAND:
            case THIEVES_PLAYED:
            case THIEF_IN_PLAY:
            case THRONE_ROOMS_IN_HAND:
            case THRONE_ROOM_IN_PLAY:
            case VILLAGES_IN_HAND:
            case WOODCUTTERS_IN_HAND:
            case WORKSHOPS_IN_HAND:
            case WORKSHOP_IN_PLAY:
            case WITCHES_IN_HAND:
            case UNKNOWN_IN_HAND:
            case HAND_SIZE:
            case BUYS:
            case ACTIONS:
            case BUDGET:
            case ACTION_CARDS_PLAYED:
                return true;
            default:
                return false;

        }
    }

    private double isCardInPlay(PositionSummary ps, CardType card) {
        if (ps.getCardInPlay() == card) return 1.0;
        return 0.0;
    }

    public static List<GeneticVariable<Player>> toGenVar(List<CardValuationVariables> variablesToUseForActions) {
        List<GeneticVariable<Player>> retValue = new ArrayList<GeneticVariable<Player>>();
        for (CardValuationVariables cvv : variablesToUseForActions) {
            retValue.add((GeneticVariable<Player>) cvv);
        }
        return retValue;
    }
}
