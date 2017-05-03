import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class Guru {

	static int[] values;
	static String[] entrants;
	static int[] scores;
	static ArrayList<String[]> allPicks;
	static String[] results;
	static String[][] possibleResults;
	static File neighbors; 
	static int nextMatch;
	
	public static void main(String[] args) {
		populateValues();
		nextMatch = 0;
		allPicks = new ArrayList<String[]>();
		try {
	        File inFile = new File(args[0]);
	        
	        neighbors = new File("neighbors.txt");
	        
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
		scores = calculateScores(results);
		System.out.println("Current Match: " + nextMatch + " Remaining Brackets: " + entrants.length);
		outputClosestBrackets();
		if(args.length <= 1)
			checkNext(1,"");
		else
			checkNext(Integer.parseInt(args[1]),"");
		
		calculateScenarios("");
	}
	
	public static void checkNext(int i, String filename)
	{
		String[] possibles = getPossibles(nextMatch);
		for(String poss : possibles)
		{
			possibleResults[nextMatch] = new String[1];
			possibleResults[nextMatch][0] = poss;
			results[nextMatch] = poss;
			scores = calculateScores(results);
			if(i <= 1)
			{
				nextMatch++;
				neighbors = new File(filename+poss+".txt");
				outputClosestBrackets();
				nextMatch--;
			}else{
				nextMatch++;
				checkNext(i-1, filename+poss+"+");
				nextMatch--;
			}
		}
		possibleResults[nextMatch] = possibles;
		
	}
	
	public static void calculateScenarios(String scene)
	{
		String[] possibles = getPossibles(nextMatch);
		for(String poss : possibles)
		{
			possibleResults[nextMatch] = new String[1];
			possibleResults[nextMatch][0] = poss;
			results[nextMatch] = poss;
			scores = calculateScores(results);
			if(nextMatch == 126)
			{
				String newScene = scene+poss;
				outputWinner(newScene);
			}else{
				nextMatch++;
				calculateScenarios(scene+poss+"+");
				nextMatch--;
			}
		}
		possibleResults[nextMatch] = new String[possibles.length];
		possibleResults[nextMatch] = possibles;
	}
	
	public static void outputWinner(String scene)
	{
		int maxscore = scores[0];
		for(int i = 1; i < scores.length; i++)
		{
			if(scores[i] > maxscore)
				maxscore = scores[i];
		}
		System.out.print("Winner(s) for " + scene +": ");
		for(int j = 0; j < scores.length; j++)
		{
			if(scores[j]==maxscore)
				System.out.print(entrants[j]+" ");
		}
		System.out.println();
	}
	
	
	public static String[] getPossibles(int match)
	{
		String[] result;
		int start;
//		if(!possibleResults[match][0].equals(""))
//			return possibleResults[match];
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
	
	public static void outputClosestBrackets()
	{
		try {
			FileWriter writer = new FileWriter(neighbors);
			
			String winner = neighbors.getName();
			
			winner = winner.substring(0,winner.indexOf("."));
			if(! winner.equals("neighbors"))
				System.out.println("Elims for a "+winner+" win:");
			
			writer.write("<span class=\"nocode\">\n");
			writer.write("updated through "+results[nextMatch-1]+"'s win\n");
			int[][] comparisons;
			int minscore;
			String out;
			ArrayList<Integer> minIDs = new ArrayList<Integer>();
			int[] diffmatches;
			boolean hasPrinted = false;
			for(int player = 0; player < entrants.length; player++)
			{
				comparisons = new int[entrants.length][3];
				for(int second = 0; second < entrants.length; second++)
				{
					comparisons[second] = getDifferenceScore(player, second);
				}
				minscore = 384;
				minIDs.clear();
				for(int i = 0; i < entrants.length; i++)
				{
					if(i != player)
					{
						//if(comparisons[i][1] < minscore)
						//if((scores[i]-scores[player]) + comparisons[i][2] < minscore)
						if((comparisons[i][2]-(scores[i]-scores[player])) < 10 ||
								(scores[player]-scores[i]) + comparisons[i][2] < minscore)
						{
							if(minscore > 10)
								minIDs.clear();
							//minscore = comparisons[i][1];
							if(comparisons[i][2]-(scores[i]-scores[player]) < minscore)
								minscore = (comparisons[i][2]-(scores[i]-scores[player]));
							minIDs.add(i);
						//}else if(comparisons[i][1] == minscore)
						}else if((scores[player]-scores[i]) + comparisons[i][2] == minscore)
						{
							minIDs.add(i);
						}
					}
				}
				out = "";
				writer.write(entrants[player]+"'s closest brackets: - current score: " 
								+ scores[player] + " count: " + minIDs.size() + "\n");
				hasPrinted = false;
				for(Integer i : minIDs)
				{
					if((comparisons[i][2]-(scores[i]-scores[player]))<0 || minscore>=0)
					{
						out += "  " + entrants[i] + " -";
						out += " total difference: " + comparisons[i][1];
						out += " current deficit: "+ (scores[i]-scores[player]); 
						out += " possible gain: " + comparisons[i][2] +"\n";
						out += "    magic number: " + (comparisons[i][2]-(scores[i]-scores[player])) + "\n";
						out += "\tdifferences: ";
						diffmatches = getDifferentMatches(player,i);
						out += Arrays.toString(diffmatches)+"\n";
						if((scores[i]-scores[player]) > comparisons[i][2])
						{
							out += "Should be dead\n";
							if(!hasPrinted){
								System.out.print(entrants[player] + " by " + entrants[i]);
								hasPrinted = true;
							}else
								System.out.print(", " + entrants[i]);
						}
					}
				}
				if(hasPrinted) System.out.println();
				writer.write(out);
			}
			System.out.println();
			writer.write("</span>\n");
			writer.close();
		} catch (IOException e) {
			System.out.println("problem with output");
			System.exit(1);
		}
		//System.out.println("Done getting differences");
	}
	
	public static int[] getDifferentMatches(int first, int second)
	{
		String[] firstPicks = allPicks.get(first);
		String[] lastPicks = allPicks.get(second);
		
		ArrayList<Integer> differences = new ArrayList<Integer>();
		
		for(int i = 0; i < firstPicks.length; i++)
		{
			if(!firstPicks[i].equals(lastPicks[i]))
			{
				differences.add(i+1);
			}
		}
		int[] result = new int[differences.size()];
		for(int i = 0; i < result.length; i++)
		{
			result[i] = differences.get(i).intValue();
		}
		return result;
	}
	
	public static int[] getDifferenceScore(int first, int second)
	{
		String[] firstPicks = allPicks.get(first);
		String[] lastPicks = allPicks.get(second);
		int[] result = new int[3];
		//number of differences, point value, possible points to make up
		result[0] = result[1] = result[2] = 0;
		for(int i = 0; i < firstPicks.length; i++)
		{
			if(!firstPicks[i].equals(lastPicks[i]))
			{
				result[1] += values[i];
				result[0]++;
				if(i >= nextMatch && isValid(firstPicks[i],i))
				{
					result[2]+=values[i];
				}
			}
		}
		
		return result;
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
