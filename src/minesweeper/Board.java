/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.util.Random;

/**
 * TODO: Specification
 */
public class Board {


    // TODO: Abstraction function, rep invariant, rep exposure, thread safety
    
    // TODO: Specify, test, and implement in problem 2

    private final Square[][] board;
    private final int rows;
    private final int cols;
    private final int bombNums;

    public Board(int x, int y) {
        rows = y;
        cols = x;
        bombNums = (int)(x * y * 0.25);
        board = new Square[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Square();
            }
        }
    }

    public void setBombs(boolean[][] mines) {
        for (int i = 0; i < mines.length; i++) {
            for (int j = 0; j < mines[0].length; j++) {
                board[i][j].hasBomb = mines[i][j];
            }
        }
        bombCount();
    }

    public void setBombs() {
        spreadBombs();
        bombCount();
    }

    private void spreadBombs() {
        Random random = new Random();
        int bombPlaced = 0;
        while (bombPlaced < bombNums) {
            int x = random.nextInt(cols);
            int y = random.nextInt(rows);
            if (!board[y][x].hasBomb) {
                board[y][x].hasBomb = true;
                bombPlaced++;
            }
        }
    }

    void bombCount() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j].countNums = count(j, i);
            }
        }
    }

    private int count(int x, int y) {
        int nums = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i < 0 || i >= cols || j < 0 || j >= rows) {
                    continue;
                }
                if (board[j][i].hasBomb) {
                    nums++;
                }
            }
        }
        return nums;
    }

    public boolean touched(int x, int y) {
        return board[y][x].state == SquareState.DUG;
    }

    public boolean flagged(int x, int y) {
        return board[y][x].state == SquareState.FLAGGED;
    }

    public void setFlag(int x, int y) {
        if (board[y][x].state == SquareState.UNTOUCHED) {
            board[y][x].state = SquareState.FLAGGED;
        }
    }

    public void removeFlag(int x, int y) {
        if (board[y][x].state == SquareState.FLAGGED) {
            board[y][x].state = SquareState.UNTOUCHED;
        }
    }

    public void dig(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows || board[y][x].state == SquareState.DUG) {
            return;
        }
        board[y][x].state = SquareState.DUG;
        if (board[y][x].hasBomb) {
            board[y][x].hasBomb = false;
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (i < 0 || i >= cols || j < 0 || j >= rows) {
                        continue;
                    }
                    board[j][i].countNums = count(i, j);
                }
            }
            return;        
        }
    // 如果周围没有炸弹，递归挖开相邻格子
    if (board[y][x].countNums == 0) {
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // 跳过当前格子
                if (i == x && j == y) {
                    continue;
                }
                // 递归挖开相邻的未触碰格子
                dig(i, j);
            }
        }
    }
}


    public boolean isBombAt(int x, int y) {
        return board[y][x].hasBomb;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Square square = board[i][j];
                if (square.state == SquareState.UNTOUCHED) {
                    sb.append('-');
                } else if (square.state == SquareState.FLAGGED) {
                    sb.append('F');
                } else if (square.state == SquareState.DUG) {
                    if (square.countNums == 0) {
                        sb.append(' ');
                    } else {
                        sb.append(square.countNums);
                    }
                }

                // 在每个字符后添加空格，但最后一个字符后不添加
                if (j < cols - 1) {
                    sb.append(' ');
                }
            }
            // 每行末尾添加换行符
            if (i < rows - 1) {
                sb.append('\r');
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public int getRows() {
        return rows;
    }

    public Square getSquare(int j, int i) {
        return board[i][j];
    }

    public int getCols() {
        return cols;
    }
    public static void printBoard(Board board) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Board.Square square = board.getSquare(j, i);
                if (square.state == Board.SquareState.UNTOUCHED) {
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

    enum SquareState {
        UNTOUCHED,
        FLAGGED,
        DUG
    }

    class Square {
        SquareState state;
        boolean hasBomb;
        int countNums;

        private Square() {
            state = SquareState.UNTOUCHED;
            hasBomb = false;
            countNums = 0;
        }
    }

}
