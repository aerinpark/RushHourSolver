package rushhour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Author:
 * Aerin Gilyoung Park (301228319)
 * John Patrick Alvarado (30130226)
 */
public class Solver
{
	/**
	 * A new RushHour game is created with inputPath
	 * Then the game is solved and a new file (with outputPath as its name) is created
	 * All the necessary car movements for solving the game are are written in the file
	 * @param inputPath
	 * @param outputPath
	 * @throws FileNotFoundException
	 */
	public static void solveFromFile(String inputPath, String outputPath) throws FileNotFoundException {
		RushHour newGame = new RushHour(inputPath);
		RushHour solvedGame = solve(newGame);
		LinkedList<String> path = solvedGame.getPath();

		File file = new File(outputPath);
		PrintWriter output = new PrintWriter(file);

		// Each movement stored in path is called and printed into the file
		for(int i = 0; i < path.size(); i++){
			output.println(path.get(i));
		}
		output.close();
	}

	/**
	 *
	 * @param game
	 * @return whether the game is solved (the redCar (ex. XX) reaches the rightmost space)
	 */
	public static boolean solved(RushHour game){
		return game.isSolved();
	}

	/**
	 * HashSet is used to keep track of the states that are already visited
	 * Queue (LinkedList) is used to store RushHour of possible movements
	 * This method uses BFS algorithm to create possible movements
	 * and traverse through them to until the solution is found
	 *
	 * @param initialBoard
	 * @return RushHour of the final board state
	 */
	public static RushHour solve(RushHour initialBoard) {
		Set<RushHour> seen = new HashSet<RushHour>();
		Queue<RushHour> queue = new LinkedList<>();
		queue.add(initialBoard);
		while (!queue.isEmpty()) {
			RushHour currentBoard = queue.poll();
			if (solved(currentBoard)) {
				return currentBoard;
			}

			List<RushHour> possibleNextState = currentBoard.possibleMoves();
			// Scan through all the elements in possibleNextState
			// and add those that have not been visited
			// mark them as seen and add to the queue
			for (int i = 0; i < possibleNextState.size(); i++) {
				if (seen.contains(possibleNextState.get(i))) {
					continue;
				} else {
					RushHour nextBoard = new RushHour(possibleNextState.get(i).getBoard());
					nextBoard.setPath(possibleNextState.get(i).getPath());
					seen.add(nextBoard);
					queue.add(nextBoard);
				}
			}
		}
		return null;
	}
}