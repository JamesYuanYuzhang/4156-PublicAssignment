package integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import controllers.PlayGame;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.locks.Lock;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import models.GameBoard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;





public class RobustnessTest {
  
  /**
   * initialize.
   */
  @BeforeEach
  public void init() {
    // Start Server
    System.out.println("Before Each");
    PlayGame.main(null);
    
  }
  
  /**
   * Every time a new game starts, the database table(s) must be cleaned.
   */
  @Test
  public void test1() {
    Connection conn = PlayGame.getConnection();
    Lock lock = PlayGame.getLock();
    
    boolean flag = true;
    try {
      lock.lock();
      Statement stat = conn.createStatement();
      stat.executeUpdate("insert into move values(1, 'X', 0, 0);");
      stat.close();
      conn.commit();
      lock.unlock();
      
      HttpResponse<String> response = Unirest.get("http://localhost:8080/newgame").asString();
      int restStatus = response.getStatus();
      // Check assert statement (New Game has started)
      assertEquals(restStatus, 200);
      
      lock.lock();
      stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select count(*) from move;");
      rs.next();
      assertEquals(rs.getInt(1), 0);
      rs = stat.executeQuery("select count(*) from gameBoard;");
      rs.next();
      assertEquals(rs.getInt(1), 0);
      conn.commit();
      lock.unlock();
    } catch (Exception e) {
      flag = false;
    }
    assertEquals(flag, true);
  }
  
  /**
   * If the application crashes after a move, 
   * the application must reboot with the game board's last move.
   */
  @Test
  public void test2() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> response1 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    assertEquals(200, response1.getStatus());
    //Check if the move is valid or not.
    assertEquals(100, new JSONObject(response1.getBody()).get("code"));
    //simulate the crash.
    PlayGame.stop();
    
    //check if the state is recovered.
    PlayGame.main(null);
    HttpResponse<String> response2 = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    JSONObject jsonObject = new JSONObject(response2.getBody());
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    assertEquals('X', gameBoard.getBoardState()[0][0]);
  }
  

  /**
   * If the application crashes after the game was a draw, but no new game started, 
   * the application must reboot to show the draw game board.
   */
  @Test
  public void test3() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=1").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=1").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=2").asString();
    HttpResponse<String> res = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    assertEquals(true, gameBoard.getIsDraw());
    assertEquals(0, gameBoard.getWinner());
    
    PlayGame.stop();
    //check if the state is recovered.
    PlayGame.main(null);
    HttpResponse<String> response2 = Unirest.get("http://localhost:8080/boardstatus").asString();
    JSONObject jsonObject = new JSONObject(response2.getBody());
    gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    System.out.println(jsonObject);
    assertEquals(true, gameBoard.getIsDraw());
  }
  
  /**
   * If the application crashes after the game has ended with a winner, 
   * but no new game started, the application must reboot to show the corresponding game board.
   */
  @Test
  public void test4() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> res = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    int n = gameBoard.getBoardState().length;
    for (int j = 0; j < n; j++) {
      Unirest.post("http://localhost:8080/move/1").body("x=0&y=" + String.valueOf(j)).asString();
      Unirest.post("http://localhost:8080/move/2").body("x=1&y=" + String.valueOf(j)).asString();
    }
    res = Unirest.get("http://localhost:8080/boardstatus").asString();
    gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    assertEquals(1, gameBoard.getWinner());
    
    PlayGame.stop();
    //check if the state is recovered.
    PlayGame.main(null);
    HttpResponse<String> response2 = Unirest.get("http://localhost:8080/boardstatus").asString();
    JSONObject jsonObject = new JSONObject(response2.getBody());
    gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    System.out.println(jsonObject);
    assertEquals(1, gameBoard.getWinner());
  }
  
  /**
   * If player 1 had started a game and the application crashed, 
   * then the application must be able to reboot with player 1 as part of the game board.
   */
  @Test
  public void test5() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    
    HttpResponse<String> response = Unirest.get("http://localhost:8080/boardstatus").asString();
    JSONObject jsonObject = new JSONObject(response.getBody());
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    System.out.println(jsonObject);
    assertEquals('X', gameBoard.getPlayer1().getType());
    PlayGame.stop();

    //check if the state is recovered.
    PlayGame.main(null);
    HttpResponse<String> response2 = Unirest.get("http://localhost:8080/boardstatus").asString();
    jsonObject = new JSONObject(response2.getBody());
    gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    System.out.println(jsonObject);
    assertEquals('X', gameBoard.getPlayer1().getType());
  }
  
  /**
   * If player 2 had joined a game and the application crashed, 
   * then the application must be able to reboot with player 2 
   * as part of the game board with the corresponding game board status.
   */
  @Test 
  public void test6() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    PlayGame.stop();
    //check if the state is recovered.
    PlayGame.main(null);
    HttpResponse<String> response = Unirest.get("http://localhost:8080/boardstatus").asString();
    JSONObject jsonObject = new JSONObject(response.getBody());
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    System.out.println(jsonObject);
    assertEquals('X', gameBoard.getPlayer1().getType());
    assertEquals('O', gameBoard.getPlayer2().getType());
  }

  /**
   * If a new game was created and the application crashed, 
   * it should reboot to a new game board.
   */
  @Test
  public void test7() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.get("http://localhost:8080/newgame").asString();
    PlayGame.stop();
    //check if the state is recovered.
    PlayGame.main(null);
    HttpResponse<String> response = Unirest.get("http://localhost:8080/boardstatus").asString();
    JSONObject jsonObject = new JSONObject(response.getBody());
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    System.out.println(jsonObject);
    assertEquals(false, gameBoard.getGamestarted());
    assertEquals(null, gameBoard.getPlayer1());
    assertEquals(null, gameBoard.getPlayer2());
  }
  
  /**
   * If the application crashed after a player made an invalid move, 
   * the application must reboot with the last valid move state.
   */
  @Test
  public void test8() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=4&y=0").asString();
    PlayGame.stop();
    //check if the state is recovered.
    PlayGame.main(null);
    HttpResponse<String> response = Unirest.get("http://localhost:8080/boardstatus").asString();
    JSONObject jsonObject = new JSONObject(response.getBody());
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    System.out.println(jsonObject);
    assertEquals('X', gameBoard.getBoardState()[0][0]);
  }
  
  /**
   * If the game crashes between a request to an 
   * endpoint and dispatch of the application's response, 
   * the data should NOT get persisted in the database.
   */
  @Test
  public void test9() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    //Add app.stop() in the URL entry for move and then we can see the
    //result is not stored.
  }
  
  /**
   * This method runs after each test.
   */
  @AfterEach
  public void close() {
    // Stop Server
    PlayGame.stop();
    System.out.println("After Each");
  }
}
