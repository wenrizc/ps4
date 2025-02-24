package minesweeper;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BoardTest {

    private static final int BOARD_SIZE_X = 10;
    private static final int BOARD_SIZE_Y = 10;

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testBoardConstruction() {
        Board board = new Board(BOARD_SIZE_X, BOARD_SIZE_Y);
        assertEquals(BOARD_SIZE_X * BOARD_SIZE_Y / 20, getBombCount(board));
    }

    @Test
    public void testBombDistribution() {
        Board board = new Board(BOARD_SIZE_X, BOARD_SIZE_Y);
        int expectedBombs = BOARD_SIZE_X * BOARD_SIZE_Y / 20;
        int actualBombs = countBombs(board);
        assertEquals(expectedBombs, actualBombs);
    }

    @Test
    public void testInitialSquareStates() {
        Board board = new Board(BOARD_SIZE_X, BOARD_SIZE_Y);
        for (int i = 0; i < BOARD_SIZE_Y; i++) {
            for (int j = 0; j < BOARD_SIZE_X; j++) {
                assertEquals(Board.SquareState.UNTOUCHED, getSquareState(board, j, i));
            }
        }
    }

    // Helper methods using reflection to access private members
    private int getBombCount(Board board) {
        try {
            Field bombNumsField = Board.class.getDeclaredField("bombNums");
            bombNumsField.setAccessible(true);
            return bombNumsField.getInt(board);
        } catch (Exception e) {
            fail("Could not access bombNums field");
            return -1;
        }
    }

    private int countBombs(Board board) {
        try {
            Field boardField = Board.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Object[][] squares = (Object[][]) boardField.get(board);

            int count = 0;
            for (Object[] row : squares) {
                for (Object square : row) {
                    Field hasBombField = square.getClass().getDeclaredField("hasBomb");
                    hasBombField.setAccessible(true);
                    if ((boolean) hasBombField.get(square)) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            fail("Could not count bombs");
            return -1;
        }
    }

    private Board.SquareState getSquareState(Board board, int x, int y) {
        try {
            Field boardField = Board.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Object[][] squares = (Object[][]) boardField.get(board);

            Field stateField = squares[y][x].getClass().getDeclaredField("state");
            stateField.setAccessible(true);
            return (Board.SquareState) stateField.get(squares[y][x]);
        } catch (Exception e) {
            fail("Could not get square state");
            return null;
        }
    }

    private void setSquareBomb(Board board, int x, int y, boolean hasBomb) {
        try {
            Field boardField = Board.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Object[][] squares = (Object[][]) boardField.get(board);

            Field hasBombField = squares[y][x].getClass().getDeclaredField("hasBomb");
            hasBombField.setAccessible(true);
            hasBombField.set(squares[y][x], hasBomb);
        } catch (Exception e) {
            fail("Could not set bomb");
        }
    }

    private int getSquareCount(Board board, int x, int y) {
        try {
            Field boardField = Board.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Object[][] squares = (Object[][]) boardField.get(board);

            Field countField = squares[y][x].getClass().getDeclaredField("countNums");
            countField.setAccessible(true);
            return countField.getInt(squares[y][x]);
        } catch (Exception e) {
            fail("Could not get square count");
            return -1;
        }
    }
}