package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class GameSetup {

	private ArrayList<CardType> cardTypes = new ArrayList<CardType>();
	private ArrayList<CardValuationVariables> deckVariables = new ArrayList<CardValuationVariables>();
	private ArrayList<CardValuationVariables> handVariables = new ArrayList<CardValuationVariables>();
	private String coreVariables = SimProperties.getProperty("DominionCoreVariables", "");
	private String[] cardChanges = SimProperties.getProperty("DominionCardChanges", "").split(",");

	public GameSetup() {

		boolean firstGame = false, villageSquare = false, interaction = false, bigMoney = false, sizeDistortion = false, none = false;
		String dominionSetup = SimProperties.getProperty("DominionCardSetup", "FirstGame");
		firstGame = dominionSetup.equals("FirstGame");
		villageSquare = dominionSetup.equals("VillageSquare");
		interaction = dominionSetup.equals("Interaction");
		bigMoney = dominionSetup.equals("BigMoney");
		sizeDistortion = dominionSetup.equals("SizeDistortion");
		none = dominionSetup.equals("NONE");

		if (! (firstGame || interaction || villageSquare || bigMoney || sizeDistortion || none)) {
			System.out.println(dominionSetup + " is not a recognised Game set");
			return;
		}

		if (firstGame) {
			cardTypes.add(CardType.CELLAR);
			cardTypes.add(CardType.MARKET);
			cardTypes.add(CardType.MILITIA);
			cardTypes.add(CardType.MINE);
			cardTypes.add(CardType.MOAT);
			cardTypes.add(CardType.REMODEL);
			cardTypes.add(CardType.SMITHY);
			cardTypes.add(CardType.VILLAGE);
			cardTypes.add(CardType.WOODCUTTER);
			cardTypes.add(CardType.WORKSHOP);
		}

		if (villageSquare) {
			cardTypes.add(CardType.BUREAUCRAT);
			cardTypes.add(CardType.CELLAR);
			cardTypes.add(CardType.FESTIVAL);
			cardTypes.add(CardType.LIBRARY);
			cardTypes.add(CardType.MARKET);
			cardTypes.add(CardType.REMODEL);
			cardTypes.add(CardType.SMITHY);
			cardTypes.add(CardType.THRONE_ROOM);
			cardTypes.add(CardType.VILLAGE);
			cardTypes.add(CardType.WOODCUTTER);
		}

		if (interaction) {
			cardTypes.add(CardType.BUREAUCRAT);
			cardTypes.add(CardType.CHANCELLOR);
			cardTypes.add(CardType.COUNCIL_ROOM);
			cardTypes.add(CardType.FESTIVAL);
			cardTypes.add(CardType.LIBRARY);
			cardTypes.add(CardType.MILITIA);
			cardTypes.add(CardType.MOAT);
			cardTypes.add(CardType.SPY);
			cardTypes.add(CardType.THIEF);
			cardTypes.add(CardType.VILLAGE);
		}

		if (bigMoney) {
			cardTypes.add(CardType.ADVENTURER);
			cardTypes.add(CardType.BUREAUCRAT);
			cardTypes.add(CardType.CHANCELLOR);
			cardTypes.add(CardType.CHAPEL);
			cardTypes.add(CardType.FEAST);
			cardTypes.add(CardType.LABORATORY);
			cardTypes.add(CardType.MARKET);
			cardTypes.add(CardType.MINE);
			cardTypes.add(CardType.MONEYLENDER);
			cardTypes.add(CardType.THRONE_ROOM);
		}

		if (sizeDistortion) {
			cardTypes.add(CardType.CELLAR);
			cardTypes.add(CardType.CHAPEL);
			cardTypes.add(CardType.FEAST);
			cardTypes.add(CardType.GARDENS);
			cardTypes.add(CardType.LABORATORY);
			cardTypes.add(CardType.THIEF);
			cardTypes.add(CardType.VILLAGE);
			cardTypes.add(CardType.WITCH);
			cardTypes.add(CardType.WOODCUTTER);
			cardTypes.add(CardType.WORKSHOP);
		}

		cardTypes.add(CardType.PROVINCE);
		cardTypes.add(CardType.DUCHY);
		cardTypes.add(CardType.ESTATE);

		cardTypes.add(CardType.GOLD);
		cardTypes.add(CardType.SILVER);
		cardTypes.add(CardType.COPPER);

		cardTypes.add(CardType.CURSE);

		cardTypes.add(CardType.NONE);

		for (CardType ct : CardType.values()) {
			String card = ct.toString();
			for (String cardToSwitch : cardChanges) {
				if (cardToSwitch.equals(card))
					if (cardTypes.contains(ct))
						cardTypes.remove(ct);
					else
						cardTypes.add(ct);
			}
		}

		for (CardValuationVariables cardVariable : EnumSet.allOf(CardValuationVariables.class)) {
			if (cardTypes.contains(cardVariable.relatedCardType())) {
				if (cardVariable.isHandVariable()) {
					handVariables.add(cardVariable);
				} else {
					deckVariables.add(cardVariable);
				}
			}
		}

		String[] varsToInclude = coreVariables.split(",");
		for (String v : varsToInclude) {
			deckVariables.add(CardValuationVariables.valueOf(v));
		}
	}


	public List<CardValuationVariables> getDeckVariables() {
		return deckVariables;
	}

	public List<CardValuationVariables> getHandVariables() {
		return handVariables;
	}

	public List<CardType> getCardTypes() {
		return cardTypes;
	}



}
