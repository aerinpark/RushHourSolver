package rushhour;

import java.io.*;
import java.util.*;

/**
 * Modified by
 * Aerin Gilyoung Park (301228319)
 * John Patrick Alvarado (30130226)
 */
public class RushHour
{

	public final static int HORIZONTAL = 0;
	public final static int VERTICAL = 1;


	public final static int UP = 0;
	public final static int DOWN = 1;
	public final static int LEFT = 2;
	public final static int RIGHT = 3;

	public final static char[] DIRECTION = {'U', 'D', 'L', 'R'};
	public final static int SIZE = 6;

	private class Car
	{
		private char name;
		public int dir;
		public int topLeftX;
		public int topLeftY;
		public int length;


		/**
		 * @param name
		 * @param dir - HORIZONTAL or VERTICAL
		 * @param x,y - represent the top left position of the top left corner of the car
		 */
		public Car(char name, int dir, int x, int y, int len) {
			this.name = name;
			this.dir = dir;
			this.topLeftX = x;
			this.topLeftY = y;
			this.length = len;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;

			if (!(other instanceof Car))
				return false;

			return (this.name == ((Car)other).name);
		}

	}

	// 2D array representation of a game board
	char board[][];
	// HashMap of all the cars present on the board
	Map<Character, Car> cars;
	// A linked list of all the movements of the cars
	private LinkedList<String> path;

	public RushHour(String fileName) throws FileNotFoundException {

		//
		//	..CCC.
		//  ..XX..	// board[1][2] = X board[1][3] = X
		//  ..G...
		//  ..G..A
		//  ..G..A
		//  LL....
		board = new char[SIZE][SIZE];
		path = new LinkedList<>();

		int i,j;
		File file= new File(fileName);
		Scanner reader = null;
		try {
			reader = new Scanner(file);
			for (i=0; i< SIZE; i++) {
				String data = reader.nextLine();
				for (j=0; j< SIZE; j++)
					board[i][j] = data.charAt(j);
			}
		}
		catch (FileNotFoundException exception) {
			throw exception;
		}
		catch (Exception e) {
			throw new BadBoardException(e);
		}
		finally {
			if(reader!=null)
				reader.close();
		}


		// create list of cars
		cars = new HashMap<Character,Car>();
		for (i = 0; i < board.length; i++) {
			for (j = 0; j < board.length; j++) {
				if (board[i][j] != '.') {
					int dir = findDirection(i,j);
					int len = 1;
					if (dir == HORIZONTAL) {
						int k = 0;
						while (j+k < SIZE && board[i][j+k] == board[i][j])
							k++;
						len = k;
					}
					if (dir == VERTICAL) {
						int k = 0;
						while (i+k < SIZE && board[i+k][j] == board[i][j])
							k++;
						len = k;
					}
					Car newCar = new Car(board[i][j], dir, j, i, len);
					cars.putIfAbsent(newCar.name, newCar);	// only the top left corner is added
				}
			}
		}

	}

	/**
	 * Constructs a gameset
	 * Referenced to Igor's RushHour class
	 * @param board
	 */
	public RushHour(char[][] board){
		this.board = board;
		this.path = new LinkedList<>();
		int row, col;

		cars = new HashMap<Character,Car>();
		for (row = 0; row < board.length; row++) {
			for (col = 0; col < board.length; col++) {
				if (board[row][col] != '.') {
					int dir = findDirection(row, col);
					int len = 1;
					if (dir == HORIZONTAL) {
						int k = 0;
						while (col + k < SIZE && board[row][col + k] == board[row][col])
							k++;
						len = k;
					}
					if (dir == VERTICAL) {
						int k = 0;
						while (row + k < SIZE && board[row + k][col] == board[row][col])
							k++;
						len = k;
					}
					Car newCar = new Car(board[row][col], dir, col, row, len);
					cars.putIfAbsent(newCar.name, newCar);    // only the top left corner is added
				}
			}
		}
	}

	/**
	 *
	 * @return the all the movements made
	 */
	public LinkedList<String> getPath() {
		return path;
	}

	/**
	 *
	 * @param path
	 */
	public void setPath(LinkedList<String> path) {
		this.path = path;
	}

	/**
	 * updates the list of movements made by cars on the board
	 * if the same car (ex. A) makes consecutive moves
	 * to the same direction, increase the distance,
	 * instead of writing another movement
	 * ex. A moving up twice should be AU2 not AU1 AU1
	 * @param carName
	 * @param dir
	 * @param dist
	 */
	public void updatePath(char carName, int dir, int dist){
		String currPath = new String();
		if(!path.isEmpty()) {
			String prevPath = path.removeLast();
			if (prevPath.charAt(0) == carName && prevPath.charAt(1) == DIRECTION[dir]) {
				String temp = prevPath.replaceAll("[^0-9]", "");
				int traveled = Integer.parseInt(temp);
				traveled++;
				currPath = Character.toString(carName)+Character.toString(DIRECTION[dir])+Integer.toString(traveled);
				path.addLast(currPath);
			} else {
				path.addLast(prevPath);
				currPath = Character.toString(carName)+Character.toString(DIRECTION[dir])+Integer.toString(dist);
				path.addLast(currPath);
			}
		}else {
			currPath = Character.toString(carName)+Character.toString(DIRECTION[dir])+Integer.toString(dist);
			path.addLast(currPath);
		}
	}

	/**
	 * @param carName
	 * @param direction
	 * @param dist
	 * Moves car with the given name for length steps in the given direction
	 * @throws IllegalMoveException if the move is illegal
	 */
	public void makeMove(char carName, int direction, int dist) throws IllegalMoveException {
		Car car = cars.get(carName);
		if (car.dir == HORIZONTAL && (direction == UP || direction == DOWN)) {
			throw new IllegalMoveException("car" + carName + " tried moving vertically");
		}
		if (car.dir == VERTICAL && (direction == RIGHT || direction == LEFT)) {
			throw new IllegalMoveException("car" + carName + " tried moving horizontally");
		}
		checkLegalMove(car, direction, dist);
		switch (direction) {
			case RIGHT: {

				// erase from current position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY][car.topLeftX+j] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY][car.topLeftX+dist+j] =car.name;

				car.topLeftX+=dist;
				return;
			}
			case LEFT: {
				// erase from current position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY][car.topLeftX+j] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY][car.topLeftX-dist+j] =car.name;

				car.topLeftX-=dist;
				return;
			}
			case DOWN: {
				// erase from current position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY+j][car.topLeftX] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY+dist+j][car.topLeftX] =car.name;

				car.topLeftY+=dist;
				return;
			}
			case UP: {

				// erase from current position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY+j][car.topLeftX] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					board[car.topLeftY-dist+j][car.topLeftX] =car.name;
				car.topLeftY-=dist;
				return;
			}
			default:
				throw new IllegalArgumentException("Bad direction: " + direction);
		}

	}

	/**
	 * move a single space UP, DOWN, LEFT or RIGHT
	 * Throwing Exception is not necessary
	 * since illegal movements are checked prior to this method
	 * using canUP, canDOWN, canLEFT or canRIGHT
	 * Referenced to Igor's RushHour
	 * @param gameBoard
	 * @param carName
	 * @param direction
	 * @return
	 */
	public char[][] makeMove(char[][] gameBoard, char carName, int direction){
		int dist = 1;
		Car car = cars.get(carName);
		switch (direction) {
			case RIGHT: {
				// erase from current position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY][car.topLeftX+j] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY][car.topLeftX+dist+j] =car.name;
				return gameBoard;
			}
			case LEFT: {
				// erase from current position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY][car.topLeftX+j] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY][car.topLeftX-dist+j] =car.name;
				return gameBoard;
			}
			case DOWN: {
				// erase from current position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY+j][car.topLeftX] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY+dist+j][car.topLeftX] =car.name;
				return gameBoard;
			}
			case UP: {
				// erase from current position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY+j][car.topLeftX] ='.';

				// add to new position
				for (int j = 0; j < car.length; j++)
					gameBoard[car.topLeftY-dist+j][car.topLeftX] =car.name;
				return gameBoard;
			}
		}
		return gameBoard;
	}

	/**
	 * checks if (x, y) on the board is empty ('.')
	 * @param y
	 * @param x
	 * @return
	 */
	public boolean isEmpty(int y, int x){
		if(y < 0 || y >= SIZE || x < 0 || x >= SIZE){
			return false;
		}
		return board[y][x] == '.';
	}

	/**
	 * checks if the car of interest can move one space upward
	 * @param car
	 * @param dist
	 * @return
	 */
	public boolean canUp(Car car, int dist){
		if(car.topLeftY - dist < 0){
			return false;
		}else{
			return isEmpty(car.topLeftY-dist, car.topLeftX);
		}
	}

	/**
	 * checks if the car of interest can move one space downward
	 * @param car
	 * @param dist
	 * @return
	 */
	public boolean canDown(Car car, int dist){
		if(car.topLeftY+car.length + dist -1 > SIZE){
			return false;
		}else{
			return isEmpty(car.topLeftY+dist+car.length-1, car.topLeftX);
		}
	}

	/**
	 * checks if the car of interest can move one space left
	 * @param car
	 * @param dist
	 * @return
	 */
	public boolean canLeft(Car car, int dist){
		if(car.topLeftX-dist < 0){
			return false;
		}else{
			return isEmpty(car.topLeftY, car.topLeftX-dist);
		}
	}

	/**
	 * checks if the car of interest can move one space right
	 * @param car
	 * @param dist
	 * @return
	 */
	public boolean canRight(Car car, int dist){
		if(car.topLeftX+car.length+dist -1 > SIZE){
			return false;
		}else{
			return isEmpty(car.topLeftY, car.topLeftX+dist - 1 +car.length);
		}
	}

	private void checkLegalMove(Car car, int direction, int dist) throws IllegalMoveException
	{
		switch (direction) {
			case RIGHT: {
				if (car.topLeftX + car.length + dist > SIZE)
					throw new IllegalMoveException("move " + car.name + " RIGHT " + dist + ": OUT OF BOUNDS");
				for (int j = 0; j < dist; j++) {
					if (board[car.topLeftY][car.topLeftX+car.length+j] !='.')
						throw new IllegalMoveException("move " + car.name + " RIGHT " + dist + ": "
								+ board[car.topLeftY][car.topLeftX+car.length+j] + " IN A WAY");
				}
				return;
			}
			case LEFT: {
				if (car.topLeftX - dist < 0)
					throw new IllegalMoveException("move " + car.name + " LEFT " + dist + ": OUT OF BOUNDS");
				for (int j = 0; j < dist; j++) {
					if (board[car.topLeftY][car.topLeftX-j-1] !='.')
						throw new IllegalMoveException("move " + car.name + " LEFT " + dist + ": "
								+ board[car.topLeftY][car.topLeftX-j-1] + " IN A WAY");
				}
				return;
			}
			case DOWN: {
				if (car.topLeftY + car.length + dist > SIZE)
					throw new IllegalMoveException("move " + car.name + " DOWN " + dist + ": OUT OF BOUNDS");
				for (int j = 0; j < dist; j++) {
					if (board[car.topLeftY+car.length+j][car.topLeftX] !='.')
						throw new IllegalMoveException("move " + car.name + " DOWN " + dist + ": "
								+ board[car.topLeftY][car.topLeftX+car.length+j] + " IN A WAY");
				}
				return;
			}
			case UP: {
				if (car.topLeftY - dist < 0)
					throw new IllegalMoveException("move " + car.name + " UP " + dist + ": OUT OF BOUNDS");
				for (int j = 0; j < dist; j++) {
					if (board[car.topLeftY-j-1][car.topLeftX] !='.')
						throw new IllegalMoveException("move " + car.name + " UP " + dist + ": "
								+ board[car.topLeftY-j-1][car.topLeftX] + " IN A WAY");
				}
				return;
			}
			default:
				throw new IllegalMoveException("Bad direction: " + direction);
		}
	}

	private int findDirection(int i, int j){
		if ((j>=1 && board[i][j] == board[i][j-1]) || (j<= SIZE -2 && board[i][j] == board[i][j+1]))
			return HORIZONTAL;
		else if ((i>=1 && board[i][j] == board[i-1][j]) || (i<= SIZE -2 && board[i][j] == board[i+1][j]))
			return VERTICAL;
		else
			throw new BadBoardException("board[" + i + "][" + j + "j]");

	}

	/**
	 * @return true if and only if the board is solved,
	 * i.e., the XX car is touching the right edge of the board
	 */
	public boolean isSolved() {
		Car xCar = cars.get('X');
		return (xCar.topLeftX+xCar.length == SIZE);
	}

	/**
	 *
	 * @return the current board state
	 */
	public char[][] getBoard() {
		return board;
	}

	/**
	 * shows the current state of the board
	 */
	public void showBoard(){
		for(int i=0; i<SIZE; i++){
			for(int j=0; j<SIZE; j++){
				System.out.print(board[i][j]);
			}
			System.out.println();
		}
	}

	/**
	 * Deep-copies the current state of the board
	 * @return 2D representation of the board
	 */
	private char[][] copyBoard(){
		char[][] copy = new char[SIZE][SIZE];
		for(int i=0; i<SIZE; i++){
			for(int j=0; j<SIZE; j++){
				copy[i][j] = board[i][j];
			}
		}
		return copy;
	}

	/**
	 * Deep-copies the current list of paths
	 * into a new linked list (copyPath)
	 * to prevent any changes to the original list
	 * @return copyPath
	 */
	public LinkedList<String> deepCopyPath(){
		LinkedList<String> copyPath = new LinkedList<>();
		for (String temp : path) {
			copyPath.add(temp);
		}
		return copyPath;
	}

	/**
	 * HashMap containing all the cars present on the board are scanned through
	 * Each car in the map is checked (using canUp, canDown, canLeft and/or canRight)
	 * Cars that can move move a single space towards the available space
	 * The gameboard is updated, and a newly created RushHour with the updated board
	 * is added to the linked list (possibleMoves)
	 * @return the list of RushHour that contains the possible states of boards
	 */
	public List<RushHour> possibleMoves(){
		List<RushHour> possibleMoves = new LinkedList<>();

		// Go through every entry in the Map
		for(Map.Entry<Character,Car> entry : cars.entrySet()) {
			Car currentCar = entry.getValue();
			if (currentCar.dir == VERTICAL) {
				if (canUp(currentCar, 1)) {
					char[][] updatedBoard = copyBoard();
					RushHour newSet = new RushHour(makeMove(updatedBoard, currentCar.name, UP));
					newSet.setPath(deepCopyPath());
					possibleMoves.add(newSet);
					newSet.updatePath(currentCar.name, UP, 1);
				}
				if (canDown(currentCar, 1)) {
					char[][] updatedBoard = copyBoard();
					RushHour newSet = new RushHour(makeMove(updatedBoard, currentCar.name, DOWN));
					newSet.setPath(deepCopyPath());
					possibleMoves.add(newSet);
					newSet.updatePath(currentCar.name, DOWN, 1);
				}
			} else { // when dir == HORIZONTAL
				if (canLeft(currentCar, 1)) {
					char[][] updatedBoard = copyBoard();
					RushHour newSet = new RushHour(makeMove(updatedBoard, currentCar.name, LEFT));
					newSet.setPath(deepCopyPath());
					possibleMoves.add(newSet);
					newSet.updatePath(currentCar.name, LEFT, 1);
				}
				if (canRight(currentCar, 1)) {
					char[][] updatedBoard = copyBoard();
					RushHour newSet = new RushHour(makeMove(updatedBoard, currentCar.name, RIGHT));
					newSet.setPath(deepCopyPath());
					possibleMoves.add(newSet);
					newSet.updatePath(currentCar.name, RIGHT, 1);
				}
			}
		}
		return possibleMoves;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(board);
	}
}