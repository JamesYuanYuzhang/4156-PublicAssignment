package controllers;

import com.google.gson.Gson;
import io.javalin.Javalin;
import java.io.IOException;
import java.sql.Connection;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import models.GameBoard;
import models.Message;
import models.Move;
import models.Player;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.MyDatabase;


public class PlayGame {

  private static final int PORT_NUMBER = 8080;

  private static Javalin app;
  
  // The gameBoard for current game.
  private static GameBoard gameBoard;
  
  // Transform object to JSON file and vice versa.
  private static Gson gson = new Gson();
  
  // The logger to record exception information.
  private static final Logger logger = LoggerFactory.getLogger(PlayGame.class);
  
  // The regular expression pattern for extracting the information of move request.
  private static Pattern p = Pattern.compile("[-]*\\d+");

  // The database which used to store the state.
  private static MyDatabase db = new MyDatabase();
  
  private static Connection conn = db.createConnection();
  
  private static Lock lock = new ReentrantLock();
  
  /** Main method of the application.
   * @param args Command line arguments
   */
  public static void main(final String[] args) {
    lock.lock();
    try {
      db.tryCreateTable(conn, "move");
      db.tryCreateTable(conn, "gameBoard");
      gameBoard = db.recoverFromDatabase(conn);
      db.commit(conn);
    } finally {
      lock.unlock();
    }
    System.out.println(gson.toJson(gameBoard));
    
    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);
    
    // test whether server is online.
    app.get("/", ctx -> {
      ctx.result("server is online.");
    });
    
    // New game
    app.get("/newgame", ctx -> {
      lock.lock();
      try {
        db.cleanTable(conn);
        gameBoard = new GameBoard();
        db.commit(conn);
      } finally {
        lock.unlock();
      }
      
      System.out.println(gson.toJson(gameBoard));
      ctx.redirect("tictactoe.html");
    });
    
    //Start game
    app.post("/startgame", ctx -> {
      gameBoard.init();
      char type = (ctx.body().charAt(5));
      String result;
      if (gameBoard.startGame(type)) {
        result = gson.toJson(gameBoard);
      } else {
        result = "This type is invalid.";
      }
      lock.lock();
      try {
        db.updateGameBoard(conn, gameBoard);
        db.commit(conn);
      } finally {
        lock.unlock();
      }
      ctx.result(result);
    });
    
    //Show current status of gameBoard
    app.get("/boardstatus", ctx -> {
      ctx.result(gson.toJson(gameBoard));
    });
    
    //Another player join the game.
    app.get("/joingame", ctx -> {
      gameBoard.joinGame();
      
      lock.lock();
      try {
        db.updateGameBoard(conn, gameBoard);
        db.commit(conn);
      } finally {
        lock.unlock();
      }
      ctx.redirect("/tictactoe.html?p=2");
      sendGameBoardToAllPlayers(gson.toJson(gameBoard));
    });
    
    //Player ask to move.
    app.post("/move/:playerId", ctx -> {
      int i = -1;
      int j = -1;
      Matcher matcher = p.matcher(ctx.body());
      if (matcher.find()) {
        i = Integer.parseInt(matcher.group());
      }
      if (matcher.find()) {
        j = Integer.parseInt(matcher.group());
      }
      int playerId = Integer.parseInt(ctx.pathParam("playerId"));
      Player player = (1 == playerId) ? gameBoard.getPlayer1() : gameBoard.getPlayer2();
      Message message = gameBoard.move(i, j, playerId);
      if (100 == message.getCode()) {
        lock.lock();
        try {
          db.addMove(conn, new Move(player, i, j));
          db.updateGameBoard(conn, gameBoard);
          //app.stop();
          db.commit(conn);
        } finally {
          lock.unlock();
        }
        sendGameBoardToAllPlayers(gson.toJson(gameBoard));
      }
      ctx.result(gson.toJson(message));
      
    });

    // Web sockets - DO NOT DELETE or CHANGE
    app.ws("/gameboard", new UiWebSocket());
  }

  /** Send message to all players.
   * @param gameBoardJson gameBoard JSON
   * @throws IOException webSocket message send IO Exception
   */
  private static void sendGameBoardToAllPlayers(final String gameBoardJson) {
    Queue<Session> sessions = UiWebSocket.getSessions();
    for (Session sessionPlayer : sessions) {
      try {
        sessionPlayer.getRemote().sendString(gameBoardJson);
        System.out.println("1");
      } catch (IOException e) {
        // Add logger here
        logger.error(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public static void stop() {
    app.stop();
  }
  

  public static MyDatabase getDb() {
    return db;
  }
  
  public static Connection getConnection() {
    return conn;
  }
  
  public static Lock getLock() {
    return lock;
  }
}
