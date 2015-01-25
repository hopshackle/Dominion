package hopshackle.dominion;

import hopshackle.simulation.AgentDAO;

public class CompetitionDAO implements AgentDAO<VariableRoundResults> {

	@Override
	public String getTableCreationSQL(String tableSuffix) {

		return  "CREATE TABLE IF NOT EXISTS DomCompetition_" + tableSuffix +
				" (round			INT			NOT NULL,"		+
				" variable			VARCHAR(30)	NOT NULL,"		+
				" percent			FLOAT		NOT NULL,"		+
				" top50Percent		FLOAT		NOT NULL"		+
				");";
	}

	@Override
	public String getTableDeletionSQL(String tableSuffix) {
		return "DROP TABLE IF EXISTS DomCompetition_" + tableSuffix + ";";
	}

	@Override
	public String getTableUpdateSQL(String tableSuffix) {
		return "INSERT INTO DomCompetition_" + tableSuffix + 
				" (round, variable, percent, top50Percent) VALUES";
	}

	@Override
	public String getValuesForAgent(VariableRoundResults competition) {

		return String.format(" (%d, '%s', %.2f, %.2f)", 
				competition.round,
				competition.variableName,
				competition.percent,
				competition.top50);

	}

}
