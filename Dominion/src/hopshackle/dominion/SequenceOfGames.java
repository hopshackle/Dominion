package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class SequenceOfGames extends World {

	private long count = 0;
	private int maximum = 0;
	private int finalScoring = 0;
	private DeciderGenerator deciderGenerator;
	private static boolean extraLastK = SimProperties.getProperty("DominionLastThousandForScoring", "true").equals("true");

	public SequenceOfGames(String name, int number, DeciderGenerator dg) {
		super(null, name, number);
		if (dg == null) {
			dg = new DeciderGenerator(new GameSetup(), 1, 0, 0, 0);
		}
		deciderGenerator = dg;
		maximum = number;
		if (extraLastK) finalScoring = 1000;
		this.setCalendar(new FastCalendar(0));
		DatabaseAccessUtility databaseUtility = new DatabaseAccessUtility();
		setDatabaseAccessUtility(databaseUtility);
		Thread t = new Thread(databaseUtility);
		t.start();
	}

	public void runNextGameWithLearning() {
		Game game = new Game(this);
		DominionBatchTeacher teacher = new DominionBatchTeacher(game);
		runGame(game);
		teacher.gameOver();
		teacher.trainPlayers();
	}

	public void runNextGameWithoutLearning() {
		Game game = new Game(this);
		runGame(game);
	}

	public List<ExperienceRecord> runAllGamesAndReturnExperience(int number) {
		List<ExperienceRecord> allER = new ArrayList<ExperienceRecord>();
		for (int i = 0; i < number; i++) {
			allER.addAll(runOneGameAndReturnExperience());
		}
		return allER;
	}

	public List<ExperienceRecord> runOneGameAndReturnExperience() {
		Game game = new Game(this);
		DominionBatchTeacher teacher = new DominionBatchTeacher(game);
		runGame(game);
		teacher.gameOver();
		List<ExperienceRecord> allER = teacher.getAllExperienceRecords();
		return allER;
	}

	private void runGame(Game game) {
		count++;
		setCurrentTime(count);
		maintenance();

		game.playGame();

		Player[] players = game.getPlayers();
		for (int p : game.getWinningPlayers()) {
			players[p-1].log("Wins Game!");
			if (deciderGenerator != null)
				deciderGenerator.reportVictory(players[p-1]);
		}

		if (maximum + finalScoring > count)
			return;

		worldDeath();
	}

	public boolean finishedRun() {
		return (maximum + finalScoring <= count);
	}

	public boolean finishedLearningRun() {
		return (maximum <= count);
	}

	public void worldDeath() {
		super.worldDeath();
		updateDatabase("EXIT");
	}

	public DeciderGenerator getDeciderDenerator() {
		return deciderGenerator;
	}
}
