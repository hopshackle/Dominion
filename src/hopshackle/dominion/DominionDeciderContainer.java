package hopshackle.dominion;

import hopshackle.simulation.*;

public class DominionDeciderContainer {
	
	protected Decider<Player> purchase, action;
	
	public void setPurchaseDecider(Decider<Player> decider) {
		purchase = decider;
	}
	
	public void setActionDecider(Decider<Player> decider) {
		action = decider;
	}
	
	public Decider<Player> getDecider(Player player) {
		switch (player.getPlayerState()) {
		case PURCHASING:
			return purchase;
		case PLAYING:
			return action;
		case WAITING:
			return purchase;
		}
		return purchase;
	}

}
