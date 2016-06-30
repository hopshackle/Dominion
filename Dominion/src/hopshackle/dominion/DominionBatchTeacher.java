package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class DominionBatchTeacher {

	private String teachingStrategy = SimProperties.getProperty("DominionTeachingStrategy", "AllPlayers");
	private boolean allParticipantsLearnFromWinner = teachingStrategy.equals("SelfAndWinner");
	private boolean participantsLearnfromOwnExperienceOnly = teachingStrategy.equals("SelfOnly");
	private boolean learnFromAllPlayersEqually = teachingStrategy.equals("AllPlayers");
	private boolean singleBrain = SimProperties.getProperty("DominionSingleBrain", "false").equals("true");
	private boolean gameOverVariableInUse = SimProperties.getProperty("DominionGameOverTracking", "false").equals("true");
	private Game game;
	private Map<Integer, List<ExperienceRecord<Player>>> experienceRecords;
	private Map<Integer, List<double[]>> gameDurationRecords;

	public DominionBatchTeacher(Game game) {
		experienceRecords = new HashMap<Integer, List<ExperienceRecord<Player>>>();
		gameDurationRecords = new HashMap<Integer, List<double[]>>();
		this.game = game;
		for (int i = 1; i < 5; i++) {
			experienceRecords.put(i, new ArrayList<ExperienceRecord<Player>>());
			gameDurationRecords.put(i, new ArrayList<double[]>());
		}
//		for (Player p : game.getPlayers()) {
//			p.getPurchaseDecider().setTeacher(this);
//		}
	}

	public boolean registerDecision(Player decider, ExperienceRecord<Player> decision) {
		int playerNumber = game.getPlayerNumber(decider);
		if (playerNumber > 0) {
			experienceRecords.get(playerNumber).add(decision);
			NeuralComputer gameEndBrain = decider.getGameEndBrain();
			if (gameOverVariableInUse) {
				int currentTurn = game.turnNumber();
				double[] gameEndInputs = gameEndBrain.getInputs(decider.getPositionSummaryCopy());
				List<double[]> playerRecord = gameDurationRecords.get(playerNumber);
				if (playerRecord.size() < currentTurn)		// i.e. we haven't recorded data for this turn yet
					playerRecord.add(gameEndInputs);		// to keep things simple, we just record the data from the first purchase each turn
			}
		}
		return true;
	}

	public List<ExperienceRecord<Player>> getExperienceRecords(Player decider) {
		int playerNumber = game.getPlayerNumber(decider);
		if (playerNumber > 0)
			return experienceRecords.get(playerNumber);
		return null;
	}

	public void gameOver() {
		if (gameOverVariableInUse)
			processDataOnGameDuration();
		updateExperienceRecordsWithFinalResult();
	}

	public void trainPlayers() {
		Player[] players = game.getPlayers();
		for (int n = 1; n < 5; n++) {
			if (participantsLearnfromOwnExperienceOnly) {
				useExperienceToTeach(players[n-1], n);
			} else if (learnFromAllPlayersEqually) {
				useExperienceToTeach(players[n-1], 0);
				if (singleBrain) break;	// just do this for the first 'player' as they all use the same brain
			} else if (allParticipantsLearnFromWinner) {
				useExperienceToTeach(players[n-1], n);	// first learn from self
				for (int p : game.getWinningPlayers()) {
					if (n == p) {
						// this is a winning player
						for (int m = 0; m <4; m++) {
							if (m != n-1) {
								useExperienceToTeach(players[m], n);
								if (singleBrain) break;	// just do this for the first 'player' as they all use the same brain
							}
						}
					} 
				}
			}
		}

	}

	private void processDataOnGameDuration() {
		int totalTurns = game.turnNumber();
		Player lastPlayer = game.getCurrentPlayer();
		int lastPlayerNumber = game.getPlayerNumber(lastPlayer);
		int totalPlayerTurns = (totalTurns-1) * 4 + lastPlayerNumber; 
		List<double[]> inputs = new ArrayList<double[]>();
		List<Double> actuals = new ArrayList<Double>();
		for (int n = 0; n < 4; n++) {
			List<double[]> experienceForPlayer = gameDurationRecords.get(n+1);
			inputs.addAll(experienceForPlayer);
			if (inputs.size() == 0)
				return;
			for (int loop = 0; loop<experienceForPlayer.size(); loop++) {
				actuals.add(((double)(loop * 4 + n)) / (double)totalPlayerTurns);
			}
		}
		double[] a = new double[actuals.size()];
		int count = 0;
		for (Double d : actuals) {
			a[count] = d;
			count++;
		}
		if (inputs.size() != a.length || a.length != totalPlayerTurns)
			throw new AssertionError(String.format("Mismatch between inputs (%d), actuals (%d) and turns (%d)", inputs.size(), a.length, totalPlayerTurns));
		lastPlayer.getGameEndBrain().train(inputs, a);
	}

	private void updateExperienceRecordsWithFinalResult() {
		Player[] players = game.getPlayers();
		for (int n = 0; n < 4; n++) {
			Player currentPlayer = players[n];
			double finalResult = currentPlayer.getScore();
			List<ExperienceRecord<Player>> data = experienceRecords.get(n+1);
			Collections.reverse(data);
			PositionSummary nextState = currentPlayer.getPositionSummaryCopy();
			ExperienceRecord<Player> lastER = null;
			for (ExperienceRecord<Player> er : data) {
				DominionExperienceRecord der = (DominionExperienceRecord) er;
				der.updateWithResults(finalResult, nextState);
				if (lastER != null) {
					der.updateNextActions(lastER);
				} else {
					der.setIsFinal();
				}
				lastER = der;
				finalResult = 0;
				nextState = der.getStartPS();
			}
			// i.e. we chain backwards through Experience Records
			// with the reward of the final score being ascribed *only* to the final record (which is also marked as the end of run)
		}
	}

	private void useExperienceToTeach(Player playerToTeach, int playerNumberOfData) {
		List<ExperienceRecord<Player>> data = new ArrayList<ExperienceRecord<Player>>();
		if (playerNumberOfData == 0) {
			for (int i = 1; i < 5; i++)
				data.addAll(experienceRecords.get(i));
		} else 
			data = experienceRecords.get(playerNumberOfData);
		DominionPositionDecider decider = playerToTeach.getPurchaseDecider();
		decider.learnFromBatch(data, 100.0);
	}

	public List<ExperienceRecord<Player>> getAllExperienceRecords() {
		List<ExperienceRecord<Player>> allER = new ArrayList<ExperienceRecord<Player>>();
		for (List<ExperienceRecord<Player>> erList : experienceRecords.values()) {
			allER.addAll(erList);
		}
		return allER;
	}

}
