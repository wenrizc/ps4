package minesweeper;

public class BoardVisualizer {

    public static void printBoard(Board board) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Board.Square square = board.getSquare(j, i);
                if (square.state == Board.SquareState.UNTOUCHED) {
                    if (square.hasBomb) {
                        System.out.print("F");
                        continue;
                    }
                    System.out.print("-");
                } else if (square.state == Board.SquareState.FLAGGED) {
                    System.out.print("F");
                } else if (square.state == Board.SquareState.DUG) {
                    if (square.hasBomb) {
                        System.out.print("B");
                    } else {
                        System.out.print(square.countNums);
                    }
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Board board = new Board(10, 10);
        printBoard(board);
    }
}