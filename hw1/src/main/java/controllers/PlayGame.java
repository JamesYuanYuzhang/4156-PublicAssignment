package controllers;

import com.google.gson.Gson;
import io.javalin.Javalin;
import java.io.IOException;
import java.util.Queue;
import models.GameBoard;
import models.Message;
import models.Move;
import models.Player;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class PlayGame {

  private static final int PORT_NUMBER = 8080;

  private static Javalin app;
  
  //The gameBoard for current game.
  private static GameBoard gameBoard;
  
  //Transform object to JSON file and vice versa.
  private static Gson gson = new Gson();
  
  //The logger to record exception information.
  private static final Logger logger = LoggerFactory.getLogger(PlayGame.class);

  /** Main method of the application.
   * @param args Command line arguments
   */
  public static void main(final String[] args) {
    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);

    // Test Echo Server
    app.post("/echo", ctx -> {
      ctx.result(ctx.body());
    });
    
    // New game
    app.get("/newgame", ctx -> {
      gameBoard = new GameBoard();
      ctx.redirect("tictactoe.html");
    });
    
    //Start game
    app.post("/startgame", ctx -> {
      char type = (ctx.body().charAt(5));
      gameBoard.setPlayer1(new Player(type, 1));
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
      int i = Character.getNumericValue(ctx.body().charAt(2));
      int j = Character.getNumericValue(ctx.body().charAt(6));
      int playerId = Integer.valueOf(ctx.pathParam("playerId"));
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
