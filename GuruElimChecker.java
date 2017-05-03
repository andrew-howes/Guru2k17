import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Arrays;


public class GuruElimChecker {

	static int[] values; 
	static String[] entrants;
	static int[] scores;
	static int[] scenarioScores;
	static ArrayList<String[]> allPicks;
	static String[] scenarioResults;
	static String[] results;
	static String[][] possibleResults;
	//static File neighbors;
	static String[] closeEntries;
	static int nextMatch;
	static int checkIndex;
	static int[] wrongMatches;
	static String winningScenario;
	
	
	public GuruElimChecker(int[] INvalues,String[] INentrants, ArrayList<String[]> INallPicks, String[] INresults, String[][] INpossibleResults,
			int INnextMatch)
	{
		values = INvalues; 
		entrants = INentrants;
		allPicks = INallPicks;
		

		results = INresults;
		possibleResults = INpossibleResults;
		//static File neighbors;
		nextMatch = INnextMatch;
	}
	
	
	public static void main(String[] args) {
		populateValues();
		nextMatch = 0;
		allPicks = new ArrayList<String[]>();
		try {
	        File inFile = new File(args[0]);
	        String player = "";
	        if(args.length <= 1)
	        	checkIndex = 0;
	        else
	        	player = args[1];
	        
	        
	        //neighbors = new File("neighbors.txt");
	        
	        
	        BufferedReader in = new BufferedReader(new FileReader(inFile));
	        String line;
	        ArrayList<String> players = new ArrayList<String>();
	        int count = 0;
	        while ((line = in.readLine()) != null) {
	            String[] picks = line.split(", ", -1);
	            if(picks[0].equals("ACTUAL"))
	            {
	            	processResults(picks);
	            }else if(picks[0].equals("POSSIBLE"))
	            {
	            	processPossibleResults(picks);
	            }else{
	            	players.add(picks[0]);
	            	if(picks[0].equals(player))
	            	{
	            		checkIndex = count;
	            	}
	            	processPlayer(picks);
	            	count++;
	            }
	        }
	        entrants = new String[count];
	        players.toArray(entrants);
	        in.close();
	    } catch (IOException e) {
	        System.out.println("File Read Error: " + e.getMessage());
	    }
		//scores = calculateScores(results);
		System.out.println("Current Match: " + nextMatch);
		
		if(checkIndex == 0)
		{
			checkAllPlayers();
		}else
		{
			checkPlayer();
		}
		
		//outputClosestBrackets();
//		if(args.length <= 1)
//			checkNext(1,"");
//		else
//			checkNext(Integer.parseInt(args[1]),"");
		/*
		calculateScenarios("");*/
	}
	
	public static void checkAllPlayers()
	{
		for(int i = 0; i < entrants.length; i++)
		{
			checkIndex = i;
			checkPlayer();
		}
	}
	
	
	public static void checkPlayer()
	{
		scenarioResults = new String[127];
		ArrayList<Integer> differences = new ArrayList<Integer>();
		//set scenarioResults to current result or player's bracket when not impossible
		for(int i=0; i < 127; i++)
		{
			if(i < nextMatch){
				scenarioResults[i] = results[i];
			}else{
				if(isValid(allPicks.get(checkIndex)[i],i)){
					scenarioResults[i] = allPicks.get(checkIndex)[i];
				}else{
					scenarioResults[i] = "";
					differences.add(i);
				}
			}
		}
		if(differences.size() == 0)
		{
			if(outputScenarioWinner("any combination+"))
			{
				System.out.print("\t"+entrants[checkIndex]+" is ALIVE");
			}else{
				System.out.print("\t"+entrants[checkIndex]+" is DEAD");
			}
		}else{
			//find later round matches to iterate through, where the player is wrong
			wrongMatches = new int[differences.size()];
			
			
			for(int i = 0; i < wrongMatches.length; i++)
			{
				wrongMatches[i] = differences.get(i).intValue();
			}
			
			//recurse through results, checking from left-most first. When you reach the end of the list of matches, check scores
			boolean isAlive = checkPlayerHelper(0,"");
			
			//if player is the winner, end execution, else print scenario and winners
			if(isAlive)
			{
				System.out.print("\t"+entrants[checkIndex]+" is ALIVE");
			}else{
				System.out.print("\t"+entrants[checkIndex]+" is DEAD");
			}
		}
		System.out.println();
	}
	
	public static boolean checkPlayerHelper(int i, String scenario)
	{
		boolean result = false;
		if(i >= wrongMatches.length)
		{
			return outputScenarioWinner(scenario);
		}
		String[] possibles = getPlayerPossibles(wrongMatches[i]);
		
		for(String poss : possibles)
		{
			scenarioResults[wrongMatches[i]] = poss;
			result = checkPlayerHelper(i+1, scenario+poss+"+");
			if(result)
				break;
		}
			//possibleResults[nextMatch] = possibles;
		//if player is the winner, end execution, else print scenario and winners
		return result;
	}
	
	public static boolean outputScenarioWinner(String scene)
	{
		boolean result = false;
		scores = calculateScores(scenarioResults);
		int maxscore = scores[0];
		for(int i = 1; i < scores.length; i++)
		{
			if(scores[i] > maxscore)
				maxscore = scores[i];
		}
		scene = scene.substring(0,scene.length()-1);
		System.out.print("Winner(s) for " + scene +": ");
		for(int j = 0; j < scores.length; j++)
		{
			if(scores[j]==maxscore){
				if(j == checkIndex){
					result = true;
					winningScenario = scene;
				}
				System.out.print(entrants[j]+" ");
			}
		}
		System.out.println();
		return result;
	}
	

	
	
//	public static void checkNext(int i, String filename)
//	{
//		String[] possibles = getPossibles(nextMatch);
//		for(String poss : possibles)
//		{
//			possibleResults[nextMatch] = new String[1];
//			possibleResults[nextMatch][0] = poss;
//			results[nextMatch] = poss;
//			scores = calculateScores(results);
//			if(i <= 1)
//			{
//				neighbors = new File(filename+poss+".txt");
//				//outputClosestBrackets();
//			}else{
//				nextMatch++;
//				checkNext(i-1, filename+poss+"+");
//				nextMatch--;
//			}
//		}
//		possibleResults[nextMatch] = possibles;
//		
//	}
	
	public static String[] getPlayerPossibles(int match)
	{
		String[] result;
		int start;
		ArrayList<String> temp = new ArrayList<String>();
		if(match < 96)
		{
			start = (match-64)*2;
		}else if(match < 112)
		{
			start = (match-96)*2+64;
		}else if(match < 120)
		{
			start = (match-112)*2+96;
		}else if(match < 124)
		{
			start = (match-120)*2+112;
		}else if(match < 126)
		{
			start = (match-124)*2+120;
		}else
		{
			start = 124;
		}
		for(int i = start; i < start+2; i++)
		{
			temp.add(scenarioResults[i]);
		}
		result = temp.toArray(new String[temp.size()]);
		
		return result;
	}
	
	
	public static String[] getPossibles(int match)
	{
		String[] result;
		int start;
		if(!possibleResults[match][0].equals(""))
			return possibleResults[match];
		ArrayList<String> temp = new ArrayList<String>();
		if(match < 96)
		{
			start = (match-64)*2;
		}else if(match < 112)
		{
			start = (match-96)*2+64;
		}else if(match < 120)
		{
			start = (match-112)*2+96;
		}else if(match < 124)
		{
			start = (match-120)*2+112;
		}else if(match < 126)
		{
			start = (match-124)*2+120;
		}else
		{
			start = 124;
		}
		for(int i = start; i < start+2; i++)
		{
			if(i < nextMatch)
			{
				temp.add(results[i]);
			}else{
				for(int j = 0; j < possibleResults[i].length; j++)
				{
					temp.add(possibleResults[i][j]);
				}
			}
		}
		result = temp.toArray(new String[temp.size()]);
		
		return result;
	}
	
	public static void populateValues()
	{
		values = new int[127];
		for(int i = 0; i < 127; i++)
		{
			if(i < 64)
				values[i] = 1;
			else if (i < 96)
				values[i] = 2;
			else if (i < 112)
				values[i] = 4;
			else if (i < 120)
				values[i] = 8;
			else if (i < 124)
				values[i] = 16;
			else if (i < 126)
				values[i] = 32;
			else
				values[i] = 64;
		}
	}
	

	

	

	public static boolean isValid(String pick, int matchNum)
	{
		if(matchNum < 64)
		{
			if(matchNum < nextMatch)
			{
				return results[matchNum].equals(pick);
			}
			
			for(int i = 0; i < possibleResults[matchNum].length; i++)
			{
				if(possibleResults[matchNum][i].equals(pick))
					return true;
			}
			return false;
		}else if(matchNum < 96)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-64)*2) ||
						isValid(pick, (matchNum-64)*2+1);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 112)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-96)*2+64) ||
						isValid(pick, (matchNum-96)*2+65);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 120)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-112)*2+96) ||
						isValid(pick, (matchNum-112)*2+97);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 124)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-120)*2+112) ||
						isValid(pick, (matchNum-120)*2+113);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 126)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-124)*2+120) ||
						isValid(pick, (matchNum-124)*2+121);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else
		{
			return isValid(pick, 124)||isValid(pick,125);
		}
	}
	
	
	public static boolean isPlayerPickValid(String pick, int matchNum)
	{
		if(matchNum < 64)
		{
			if(matchNum < nextMatch)
			{
				return results[matchNum].equals(pick);
			}else{
				return scenarioResults[matchNum].equals(pick);
			}

		}else if(matchNum < 96)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-64)*2) ||
						isPlayerPickValid(pick, (matchNum-64)*2+1);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 112)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-96)*2+64) ||
						isPlayerPickValid(pick, (matchNum-96)*2+65);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 120)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-112)*2+96) ||
						isPlayerPickValid(pick, (matchNum-112)*2+97);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 124)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-120)*2+112) ||
						isPlayerPickValid(pick, (matchNum-120)*2+113);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 126)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-124)*2+120) ||
						isPlayerPickValid(pick, (matchNum-124)*2+121);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else
		{
			return isPlayerPickValid(pick, 124)||isPlayerPickValid(pick,125);
		}
	}

	
	public static void processPossibleResults(String[] possible)
	{
		possibleResults = new String[127][0];
		String[] parts;
		for(int i = 0; i < 127; i++)
		{
			parts = possible[i+1].split("; ");
			possibleResults[i] = parts;
		}
	}
	
	public static void processResults(String[] picks)
	{
		results = new String[127];
		for(int i = 1; i < picks.length; i++)
		{
			results[i-1] = picks[i];
			if(picks[i].equals("") && nextMatch == 0)
				nextMatch = i-1;
		}
	}
	
	public static void processPlayer(String[] picks)
	{
		String[] playerPicks = new String[picks.length-1];
		for(int i = 1; i < picks.length-1; i++)
		{
			playerPicks[i-1] = picks[i];
		}
		playerPicks[playerPicks.length-1] = 
				picks[picks.length-1].substring(0,picks[picks.length-1].indexOf(';'));
		allPicks.add(playerPicks);
	}
	
	public static int[] calculateScores(String[] results)
	{
		int[] scores = new int[entrants.length];
		//results = checkResults(preResults);
		for(int i = 0; i < results.length; i++)
		{
			if(!results[i].equals(""))
			{
				//for each player
				for(int j = 0; j < entrants.length; j++)
				{
					//if the player's pick for the match is equal to the result
					if(allPicks.get(j)[i].equals(results[i]))
					{
						//increase their points by the value of the match
						scores[j] += values[i];
					}
				}
			}else{
				break;
			}
		}
		return scores;
	}
}
