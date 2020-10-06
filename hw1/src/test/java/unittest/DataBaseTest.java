package unittest;

import com.google.gson.Gson;
import java.sql.Connection;
import models.GameBoard;
import models.Move;
import models.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.MyDatabase;



public class DataBaseTest {
  private static MyDatabase db;
  private static Connection conn;
  
  @BeforeAll
  public static void createConnectionTest() {
    db = new MyDatabase();
    conn = db.createConnection();
  }
  
  @BeforeEach
  public void createTableTest() {
    db.tryCreateTable(conn, "move");
    db.tryCreateTable(conn, "gameBoard");
  }
  
  /**
   * Clean table.
   */
  @AfterEach
  public void cleanTableTest() {
    db.cleanTable(conn);
    db.commit(conn);
  }
  
  @Test
  public void addMoveTest() {
    db.addMove(conn, new Move(new Player('X', 1), 0, 0));
    db.commit(conn);
  }
  
  @Test
  public void updateGameBoardTest() {
    GameBoard gameBoard = new GameBoard();
    gameBoard.init();
    db.updateGameBoard(conn, gameBoard);
    db.commit(conn);
  }
  
  @Test
  public void recoveryFromDatabaseTest() {
    GameBoard gameBoard = db.recoverFromDatabase(conn);
    db.commit(conn);
    Gson gson = new Gson();
    System.out.println(gson.toJson(gameBoard));
  }
  
}
