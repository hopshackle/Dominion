package hopshackle.dominion;

import hopshackle.dominion.basecards.Adventurer;
import hopshackle.dominion.basecards.Bureaucrat;
import hopshackle.dominion.basecards.Cellar;
import hopshackle.dominion.basecards.Chancellor;
import hopshackle.dominion.basecards.Chapel;
import hopshackle.dominion.basecards.CouncilRoom;
import hopshackle.dominion.basecards.Feast;
import hopshackle.dominion.basecards.Library;
import hopshackle.dominion.basecards.Militia;
import hopshackle.dominion.basecards.Mine;
import hopshackle.dominion.basecards.Moat;
import hopshackle.dominion.basecards.Moneylender;
import hopshackle.dominion.basecards.Remodel;
import hopshackle.dominion.basecards.Spy;
import hopshackle.dominion.basecards.Thief;
import hopshackle.dominion.basecards.ThroneRoom;
import hopshackle.dominion.basecards.Witch;
import hopshackle.dominion.basecards.Workshop;

public class CardFactory {

	public static Card instantiateCard(CardType cardType) {
		switch (cardType) {
		case ADVENTURER:
			return new Adventurer();
		case BUREAUCRAT:
			return new Bureaucrat();
		case CELLAR:
			return new Cellar();
		case CHANCELLOR:
			return new Chancellor();
		case CHAPEL:
			return new Chapel();
		case COUNCIL_ROOM:
			return new CouncilRoom();
		case FEAST:
			return new Feast();
		case LIBRARY:
			return new Library();
		case MILITIA:
			return new Militia();
		case MINE:
			return new Mine();
		case MOAT:
			return new Moat();
		case MONEYLENDER:
			return new Moneylender();
		case REMODEL:
			return new Remodel();
		case SPY:
			return new Spy();
		case THIEF:
			return new Thief();
		case THRONE_ROOM:
			return new ThroneRoom();
		case WITCH:
			return new Witch();
		case WORKSHOP:
			return new Workshop();
		default:
			return new Card(cardType);
		}
	}
	
}
