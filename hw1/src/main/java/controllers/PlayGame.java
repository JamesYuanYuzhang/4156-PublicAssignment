package controllers;

import com.google.gson.Gson;
import io.javalin.Javalin;
import java.io.IOException;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import models.GameBoard;
import models.Message;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

  /** Main method of the application.
   * @param args Command line arguments
   */
  public static void main(final String[] args) {
    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);
    
    // test whether server is online.
    app.get("/", ctx -> {
      ctx.result("server is online.");
    });
    
    // New game
    app.get("/newgame", ctx -> {
      gameBoard = new GameBoard();
      ctx.redirect("tictactoe.html");
    });
    
    //Start game
    app.post("/startgame", ctx -> {
      char type = (ctx.body().charAt(5));
      if (gameBoard.startGame(type)) {
        ctx.result(gson.toJson(gameBoard));
      } else {
        ctx.result("This type is invalid.");
      }
    });
    
    //Show current status of gameBoard
    app.get("/boardstatus", ctx -> {
      ctx.result(gson.toJson(gameBoard));
    });
    
    //Another player join the game.
    app.get("/joingame", ctx -> {
      gameBoard.joinGame();
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
      Message message = gameBoard.move(i, j, playerId);
      ctx.result(gson.toJson(message));
      sendGameBoardToAllPlayers(gson.toJson(gameBoard));
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

}
