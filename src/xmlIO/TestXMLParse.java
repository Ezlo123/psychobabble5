package xmlIO;

import libraryClasses.Competition;
import libraryClasses.Team;


public class TestXMLParse {

	public static void main(String[] args) {
//		Competition competition = XMLParser.readCompetition("players Database by team with empty standings.xml", "competition-scheme.xml");
		
		Competition competition = XMLParser.readCompetition("competitionDatabase_v2.xml", "competition-scheme.xml");
		
//		System.out.println(competition.toString());
		
		for(Team t : competition.getLibrary().getLibrary()) {
			t.setFirst11AsCurrentTeam();
		}
		
		for(int i = 0; i < 38; i++) {
			competition.playRound();
		}
		
		System.out.println(competition.standingsToString());
		
//		XMLParser.writeCompetition("competitionDatabase_v2.xml", competition);
		
	}
}


