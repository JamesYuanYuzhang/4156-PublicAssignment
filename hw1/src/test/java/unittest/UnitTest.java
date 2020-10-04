package unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import models.GameBoard;
import models.Message;
import models.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class UnitTest {
  
  private GameBoard gameBoard; 
  
  /**
   * Initialize a gameBoard.
   */
  @BeforeEach
  public void init() {
    gameBoard = new GameBoard();
  }
  
  /**
   * Test the condition that start the game normally.
   */
  @Test
  public void testStartGameNormal() {
    boolean flag;
    flag = gameBoard.startGame('X');
    assertEquals('X', gameBoard.getPlayer1().getType());
    assertEquals(true, flag);
    flag = gameBoard.startGame('O');
    assertEquals(true, flag);
    assertEquals('O', gameBoard.getPlayer1().getType());
  }
  
  /**
   * Test the condition that start the game with wrong type.
   */
  @Test
  public void testStartGameWrong() {
    boolean flag;
    flag = gameBoard.startGame('1');
    assertEquals(false, flag);
  }
  
  /**
   * Test the condition that join the game normally.
   */
  @Test
  public void testJoinGame() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    assertEquals('O', gameBoard.getPlayer2().getType());
    gameBoard = new GameBoard();
    gameBoard.startGame('O');
    gameBoard.joinGame();
    assertEquals('X', gameBoard.getPlayer2().getType());
  }
  
  /**
   * Test the condition that move normally.
   */
  @Test
  public void testMoveNormal() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    assertEquals(100, gameBoard.move(0, 0, 1).getCode());
    assertEquals('X', gameBoard.getBoardState()[0][0]);
  }
  
  /**
   * Test the condition that move out of bound.
   */
  @Test
  public void testMoveOutOfBound() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    char [][] boardState = gameBoard.getBoardState();
    int rows = boardState.length;
    int cols = boardState[0].length;
    assertEquals(201, gameBoard.move(0, cols, 1).getCode());
    assertEquals(201, gameBoard.move(rows, 0, 1).getCode());
    assertEquals(201, gameBoard.move(0, -1, 1).getCode());
    assertEquals(201, gameBoard.move(-1, 0, 1).getCode());
  }
  
  /**
   * Test the condition that one player moves twice .
   */
  @Test
  public void testMoveTwice() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    gameBoard.move(0, 0, 1);
    assertEquals(302, gameBoard.move(0, 1, 1).getCode());
  }
  
  /**
   * Test the condition that move to the position which has been used.
   */
  @Test
  public void testMoveUsedPosition() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    gameBoard.move(0, 0, 1);
    assertEquals(202, gameBoard.move(0, 0, 2).getCode());
  }
  
  /**
   * Test the condition that move before all player are present.
   */
  @Test
  public void testMoveBeforeAllPlayersPresent() {
    assertEquals(301, gameBoard.move(0, 0, 1).getCode());
    assertEquals(301, gameBoard.move(0, 0, 2).getCode());
    gameBoard.startGame('X');
    assertEquals(301, gameBoard.move(0, 0, 1).getCode());
    assertEquals(301, gameBoard.move(0, 0, 2).getCode());
  }
  
  /**
   * Test the condition that win by occupying a column with the same valid symbol.
   */
  @Test
  public void testWinBycol() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    int rows = gameBoard.getBoardState().length;
    for (int i = 0; i < rows - 1; i++) {
      gameBoard.move(i, 0, 1);
      gameBoard.move(i, 1, 2);
    }
    gameBoard.move(rows - 1, 0, 1);
    assertEquals(401, gameBoard.move(rows - 1, 1, 2).getCode());
    assertEquals(1, gameBoard.getWinner());
  }
  
  /**
   * Test the condition that win by occupying a row with the same valid symbol.
   */
  @Test
  public void testWinByRow() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    int cols = gameBoard.getBoardState()[0].length;
    for (int j = 0; j < cols - 1; j++) {
      gameBoard.move(0, j, 1);
      gameBoard.move(1, j, 2);
    }
    gameBoard.move(0, cols - 1, 1);
    assertEquals(401, gameBoard.move(1, cols - 1, 2).getCode());
    assertEquals(1, gameBoard.getWinner());
  }
  
  /**
   * Test the condition that win by occupying the diagonal line with the same valid symbol.
   */
  @Test
  public void testWinByDiagonal() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    int n = gameBoard.getBoardState().length - 1;
    for (int i = 0; i < n; i++) {
      gameBoard.move(i, n - i, 1);
      if (i + i != n) {
        gameBoard.move(i, i, 2);
      } else {
        gameBoard.move(i, i + 1, 2);
      }
    }
    gameBoard.move(n, 0, 1);
    assertEquals(1, gameBoard.getWinner());
  }
  
  /**
   * Test the condition that win by occupying the reverse diagonal line with the same valid symbol.
   */
  @Test
  public void testWinByReverseDiagonal() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    int n = gameBoard.getBoardState().length - 1;
    for (int i = 0; i < n; i++) {
      gameBoard.move(i, i, 1);
      if (i != n - i) {
        gameBoard.move(i, n - i, 2);
      } else {
        gameBoard.move(i + 1, n - i, 2);
      }
    }
    gameBoard.move(n, n, 1);
    assertEquals(1, gameBoard.getWinner());
  }
  
  /**
   * Test the condition that win after occupy the whole gameBoard.
   */
  @Test
  public void testWinAtLastTime() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    gameBoard.move(2, 0, 1);
    gameBoard.move(0, 1, 2);
    gameBoard.move(1, 1, 1);
    gameBoard.move(1, 0, 2);
    gameBoard.move(1, 2, 1);
    gameBoard.move(0, 0, 2);
    gameBoard.move(2, 1, 1);
    gameBoard.move(2, 2, 2);
    gameBoard.move(0, 2, 1);
    assertEquals(401, gameBoard.move(1, 2, 2).getCode());
    assertEquals(1, gameBoard.getWinner());
    assertEquals(false, gameBoard.getIsDraw());
  }
  
  /**
   * Test the condition that whether player can continue move after the game is drawed.
   */
  @Test
  public void testAfterDraw() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    gameBoard.setIsDraw(true);
    //Code 402 stands for the condition that the game is draw.
    //And check that there is no winner now.
    char [][] boardState = gameBoard.getBoardState();
    int rows = boardState.length;
    int cols = boardState[0].length;
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        assertEquals(402, gameBoard.move(i, j, 1).getCode());
        assertEquals(402, gameBoard.move(i, j, 2).getCode());
      }
    }
    assertEquals(true, gameBoard.getIsDraw());
  }
  
  /**
   * Test the condition that whether players can make the game draw.
   */
  @Test
  public void testTryDraw() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    gameBoard.move(0, 0, 1);
    gameBoard.move(1, 0, 2);
    gameBoard.move(0, 1, 1);
    gameBoard.move(1, 1, 2);
    gameBoard.move(2, 0, 1);
    gameBoard.move(2, 1, 2);
    gameBoard.move(1, 2, 1);
    gameBoard.move(0, 2, 2);
    gameBoard.move(2, 2, 1);
    assertEquals(0, gameBoard.getWinner());
    assertEquals(true, gameBoard.getIsDraw());
  }
  
  /**
   * Test create unknown message.
   */
  @Test
  public void testUnknownMessage() {
    new Message(1000);
  }
  
  /**
   * Test the function of CheckUsedPosition.
   */
  @Test
  public void testCheckUsedPosition() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    gameBoard.move(0, 0, 1);
    gameBoard.move(1, 0, 2);
    assertEquals(2, gameBoard.checkUsedPosition());
  }
  
  /**
   * Test the function of tryMove.
   */
  @Test
  public void testTryMove() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    gameBoard.tryMove(new Move(gameBoard.getPlayer1(), 0, 0));
    assertEquals('X', gameBoard.getBoardState()[0][0]);
  }
  
  /**
   * Test the function of updateState.
   */
  @Test
  public void testUpdateState() {
    gameBoard.startGame('X');
    gameBoard.joinGame();
    int n = gameBoard.getBoardState().length;
    char[][] boardState = new char[n][n];
    for (int i = 0; i < n; i++) {
      boardState[0][i] = 'O';
    }
    gameBoard.setBoardState(boardState);
    gameBoard.updateState();
    assertEquals(2, gameBoard.getWinner());   
  }
}
