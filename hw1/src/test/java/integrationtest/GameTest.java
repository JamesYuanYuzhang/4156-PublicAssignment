package integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import controllers.PlayGame;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import models.GameBoard;
import models.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;



@TestMethodOrder(OrderAnnotation.class) 
public class GameTest {
    
  /**
  * Runs only once before the testing starts.
  */
  @BeforeAll
  public static void init() {
    // Start Server
    PlayGame.main(null);
    System.out.println("Before All");
  }
  
  /**
  * This method starts a new game before every test run. It will run every time before a test.
  */
  @BeforeEach
  public void startNewGame() {
    // Test if server is running. You need to have an endpoint /
    // If you do not wish to have this end point, it is okay to not have anything in this method.
    HttpResponse<String> response = Unirest.get("http://localhost:8080/").asString();
    int restStatus = response.getStatus();
    assertEquals(restStatus, 200);
    System.out.println("Before Each");
  }
  
  /**
  * This is a test case to evaluate the newGame endpoint.
  */
  @Test
  public void newGameTest() {
    // Create HTTP request and get response
    HttpResponse<String> response = Unirest.get("http://localhost:8080/newgame").asString();
    int restStatus = response.getStatus();
    
    // Check assert statement (New Game has started)
    assertEquals(restStatus, 200);
    System.out.println("Test New Game");
  }
  
  /**
  * This is a test case to evaluate the startGame endpoint.
  */
  @Test
  public void startGameTest() {
      
    // Create a POST request to startGame endpoint and get the body
    // Remember to use asString() only once for an endpoint call. 
    // Every time you call asString(), a new request will be sent to the endpoint. 
    // Call it once and then use the data in the object.
    Unirest.get("http://localhost:8080/newgame").asString();
    HttpResponse<String> response = Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    String responseBody = response.getBody();
    
    // --------------------------- JSONObject Parsing ----------------------------------
    
    System.out.println("Start Game Response: " + responseBody);
    
    // Parse the response to JSON object
    JSONObject jsonObject = new JSONObject(responseBody);

    // Check if game started after player 1 joins: Game should not start at this point
    assertEquals(false, jsonObject.get("gameStarted"));
    
    // ---------------------------- GSON Parsing -------------------------
    
    // GSON use to parse data to object
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    Player player1 = gameBoard.getPlayer1();
    
    // Check if player type is correct
    assertEquals('X', player1.getType());
    
    System.out.println("Test Start Game");
  }
  
  /**
   * This is the test case to evaluate the startGame endpoint with invalid type.
   */
  @Test
  public void startGameWithInvalidType() {
    Unirest.get("http://localhost:8080/newgame").asString();
    HttpResponse<String> response = Unirest.post("http://localhost:8080/startgame").body("type=P").asString();
    assertEquals(200, response.getStatus());
    assertEquals("This type is invalid.", response.getBody());
  }
  
  
  /**
   * This is the test case to evaluate the boardStatus endpoint which return the current gameBoard.
   */
  @Test
  public void testBoardStatus() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    HttpResponse<String> response = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    JSONObject jsonObject = new JSONObject(response.getBody());
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    assertEquals('X', gameBoard.getPlayer1().getType());
    assertEquals(false, gameBoard.getGamestarted());
  }
  
  /**
   * This is the test case to evaluate the joinGame endpoint.
   */
  @Test
  public void joinTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    HttpResponse<String> response1 = Unirest.get("http://localhost:8080/joingame").asString();
    assertEquals(response1.getStatus(), 200);
    HttpResponse<String> response2 = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    JSONObject jsonObject = new JSONObject(response2.getBody());
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    assertEquals('O', gameBoard.getPlayer2().getType());
    assertEquals(true, gameBoard.getGamestarted());
  }
  
  /**
   * This is the test case to evaluate the move endpoint.
   */
  @Test
  public void moveNormalTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> response1 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    assertEquals(200, response1.getStatus());
    //Check if the move is valid or not.
    assertEquals(100, new JSONObject(response1.getBody()).get("code"));
    
    //Check if the move has been done in the gameBoard.
    HttpResponse<String> response2 = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    JSONObject jsonObject = new JSONObject(response2.getBody());
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    assertEquals('X', gameBoard.getBoardState()[0][0]);
  }
  

  /**
   * This is the test case that a player want to move before all players are present.
   */
  @Test
  public void moveBeforeAllPlayerPresentTest() {
    // The code 301 of message stands for the condition that not all players are present.
    // When no player is present.
    Unirest.get("http://localhost:8080/newgame").asString();
    HttpResponse<String> response1 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    assertEquals(200, response1.getStatus());
    assertEquals(301, new JSONObject(response1.getBody()).get("code"));
    
    HttpResponse<String> response2 = Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    assertEquals(200, response2.getStatus());
    assertEquals(301, new JSONObject(response2.getBody()).get("code"));
    
    // When only player 1 is present.
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    
    HttpResponse<String> response3 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    assertEquals(200, response3.getStatus());
    assertEquals(301, new JSONObject(response3.getBody()).get("code"));
    
    HttpResponse<String> response4 = Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    assertEquals(200, response4.getStatus());
    assertEquals(301, new JSONObject(response4.getBody()).get("code"));
  }
  

  /**
   * This is the test case that a player want to move twice consecutively.
   */
  @Test
  public void moveTwiceTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> response1 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    // Show the first move is valid.
    assertEquals(100, new JSONObject(response1.getBody()).get("code"));
    
    HttpResponse<String> response2 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    // The message code 302 stands for the condition that a player want to move twice consecutively.
    assertEquals(302, new JSONObject(response2.getBody()).get("code"));
  }
  
  /**
   * This is the test case that a player want to move to the position which has been used.
   */
  @Test
  public void moveUsedPositionTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> response1 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    // Show the first move is valid.
    assertEquals(100, new JSONObject(response1.getBody()).get("code"));
    
    HttpResponse<String> response2 = Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    // The message code 202 stands for the condition that a player want to move to used position.
    assertEquals(202, new JSONObject(response2.getBody()).get("code"));
  }
  
  
  
  /**
   * This is the test case that a player want to move to the position which is out of bound.
   */
  @Test
  public void moveOutOfBoundTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> response1 = Unirest.post("http://localhost:8080/move/1").body("x=100&y=0").asString();
    // The message code 201 shows that the move is out of bound.
    assertEquals(201, new JSONObject(response1.getBody()).get("code"));
    
    HttpResponse<String> response2 = Unirest.post("http://localhost:8080/move/1").body("x=-1&y=0").asString();
    // The message code 201 shows that the move is out of bound.
    assertEquals(201, new JSONObject(response2.getBody()).get("code"));
    
    HttpResponse<String> response3 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=100").asString();
    // The message code 201 shows that the move is out of bound.
    assertEquals(201, new JSONObject(response3.getBody()).get("code"));
    
    HttpResponse<String> response4 = Unirest.post("http://localhost:8080/move/1").body("x=0&y=-1").asString();
    // The message code 201 shows that the move is out of bound.
    assertEquals(201, new JSONObject(response4.getBody()).get("code"));
    
    HttpResponse<String> response5 = Unirest.post("http://localhost:8080/move/1").body("x=?&y=?").asString();
    // The message code 201 shows that the move is out of bound.
    assertEquals(201, new JSONObject(response5.getBody()).get("code"));
    
    HttpResponse<String> response6 = Unirest.post("http://localhost:8080/move/1").body("x=1&y=?").asString();
    // The message code 201 shows that the move is out of bound.
    assertEquals(201, new JSONObject(response6.getBody()).get("code"));
  }
  
  /**
   * This is the test case that a player can win by occupy one row with same symbol.
   */
  @Test
  public void winByRowTest() {
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
  }
  
  /**
   * This is the test case that a player can win by occupy one column with same symbol.
   */
  @Test
  public void winByColumnTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> res = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    int n = gameBoard.getBoardState().length;
    for (int i = 0; i < n; i++) {
      Unirest.post("http://localhost:8080/move/1").body(String.format("x=%d&y=0", i)).asString();
      Unirest.post("http://localhost:8080/move/2").body(String.format("x=%d&y=1", i)).asString();
    }
    res = Unirest.get("http://localhost:8080/boardstatus").asString();
    gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    assertEquals(1, gameBoard.getWinner());
  }
  
  /**
   * This is the test case that a player can win by occupy the diagonal line with same symbol.
   */
  @Test
  public void winByDiagonalTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> res = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    int n = gameBoard.getBoardState().length;
    for (int i = 0; i < n; i++) {
      Unirest.post("http://localhost:8080/move/1").body(String.format("x=%d&y=%d", i, n - i - 1)).asString();
      if (i + i != n - 1) {
        Unirest.post("http://localhost:8080/move/2").body(String.format("x=%d&y=%d", i, i)).asString();
      } else {
        Unirest.post("http://localhost:8080/move/2").body(String.format("x=%d&y=%d", i, i + 1)).asString();
      }
    }
    res = Unirest.get("http://localhost:8080/boardstatus").asString();
    gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    assertEquals(1, gameBoard.getWinner());
  }
  
  
  /**
   * This is the test case that a player can win by occupy the diagonal line with same symbol.
   */
  @Test
  public void winByReverseDiagonalTest() {
    Unirest.get("http://localhost:8080/newgame").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    HttpResponse<String> res = Unirest.get("http://localhost:8080/boardstatus").asString();
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    int n = gameBoard.getBoardState().length;
    for (int i = 0; i < n; i++) {
      Unirest.post("http://localhost:8080/move/1").body(String.format("x=%d&y=%d", i, i)).asString();
      if (i != n - i - 1) {
        Unirest.post("http://localhost:8080/move/2").body(String.format("x=%d&y=%d", i, n - i - 1)).asString();
      } else {
        Unirest.post("http://localhost:8080/move/2").body(String.format("x=%d&y=%d", i + 1, n - i - 1)).asString();
      }
    }
    res = Unirest.get("http://localhost:8080/boardstatus").asString();
    gameBoard = gson.fromJson(new JSONObject(res.getBody()).toString(), GameBoard.class);
    assertEquals(1, gameBoard.getWinner());
  }
  
  
  /**
   * This is the test case that the game will draw when all the positions have been used.
   */
  @Test
  public void drawTest() {
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
  }
  
  /**
  * This will run every time after a test has finished.
  */
  @AfterEach
  public void finishGame() {
    System.out.println("After Each");
  }
  
  /**
   * This method runs only once after all the test cases have been executed.
   */
  @AfterAll
  public static void close() {
    // Stop Server
    PlayGame.stop();
    System.out.println("After All");
  }
}
