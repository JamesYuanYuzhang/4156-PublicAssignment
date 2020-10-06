package utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import models.GameBoard;
import models.Move;
import models.Player;


public class MyDatabase {
  /**
   * Create new connection.
   * @return Connection
   */
  public Connection createConnection() {
    Connection conn = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:4156.db");
      conn.setAutoCommit(false);
    } catch (Exception e) {
      System.out.println("Failed to create connection.");
      System.out.println(e.getMessage());
    }
    //System.out.println("The connection has been created successfully.");
    return conn;
  }
  
  /**
   * Create new table if the table not exists.
   */
  public void tryCreateTable(Connection conn, String tableName) {
    Statement stat = null;
    ResultSet rs = null;
    try {
      stat = conn.createStatement();
      String sql;
      if ("move".equals(tableName)) {
        sql = "create table if not exists move(playerId int, playerType char(4), x int, y int);";
        stat.executeUpdate(sql);
      } else {
        sql = "create table if not exists gameBoard(" + "gameStarted boolean, "
                + "turn int, " + "winner int, " + "isDraw boolean, "
                + "player1 char(4), " + "player2 char(4)" + ");";
        stat.executeUpdate(sql);
        rs = stat.executeQuery("select count(*) from gameBoard;");
        rs.next();
        if (0 == rs.getInt(1)) {
          stat.executeUpdate("insert into gameBoard values(false, 1, 0, false, null, null);");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (null != stat) {
          stat.close();
        }   
      } catch (SQLException e) {
        e.printStackTrace();
      }
      try {
        if (null != rs) {
          rs.close();
        }   
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * insert a record.
   * @param move move information
   * @return
   */
  public void addMove(Connection conn, Move move) {
    Statement stat = null;
    try {
      stat = conn.createStatement();
      int id = move.getPlayer().getId();
      char type = move.getPlayer().getType();
      int x = move.getMoveX();
      int y = move.getMoveY();
      String sql = String.format("insert into move values(%d, '%s', %d, %d);", id, type, x, y);
      stat.executeUpdate(sql);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
    } finally {
      try {
        if (null != stat) {
          stat.close();
        }   
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * commit the current transaction and close the connection.
   * @param conn Connection
   */
  public void commit(Connection conn) {
    try {
      conn.commit();
      System.out.println("Transaction commit successfully");
    } catch (Exception e) {
      try {
        System.out.println(e.getMessage());
        conn.rollback();
        System.out.println("Transaction rolled back successfully");
        conn.setAutoCommit(true);
        conn.close();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    } 
  }
  
  /**
   * recover from the database.
   */
  public GameBoard recoverFromDatabase(Connection conn) {
    Statement stat = null;
    ResultSet rs1 = null;
    ResultSet rs2 = null;
    GameBoard gameBoard = new GameBoard();
    try {
      stat = conn.createStatement();
      String sql = "Select * from gameBoard;";
      rs1 = stat.executeQuery(sql);
      while (rs1.next()) {
        gameBoard.setGamestarted(rs1.getBoolean("gameStarted"));
        gameBoard.setTurn(rs1.getInt("turn"));
        gameBoard.setWinner(rs1.getInt("winner"));
        gameBoard.setIsDraw(rs1.getBoolean("isDraw"));
        if (null != rs1.getString("player1")) {
          gameBoard.setPlayer1(new Player(rs1.getString("player1").charAt(0), 1));
        }
        if (null != rs1.getString("player2")) {
          gameBoard.setPlayer2(new Player(rs1.getString("player2").charAt(0), 2));
        }
      }
      sql = "select * from move;";
      rs2 = stat.executeQuery(sql);
      char[][] boardState = new char[3][3];
      while (rs2.next()) {
        int id = rs2.getInt("playerId");
        char type = rs2.getString("playerType").charAt(0);
        if (null == gameBoard.getPlayer1()) {
          if (1 == id) {
            gameBoard.setPlayer1(new Player(type, id));
            gameBoard.setPlayer2(new Player(('X' == type ? 'O' : 'X'), 2));
          } else {
            gameBoard.setPlayer2(new Player(type, id));
            gameBoard.setPlayer1(new Player(('X' == type ? 'O' : 'X'), 1));
          }
        }
        int x = rs2.getInt("x");
        int y = rs2.getInt("y");
        boardState[x][y] = type;
      }
      gameBoard.setBoardState(boardState);
      gameBoard.updateState();
      System.out.println("recover from DataBase successfully.");
      
    } catch (Exception e) {
      System.out.println("Failed to recover from DataBase.");
      e.printStackTrace();
      System.out.println(e.getMessage());
    } finally {
      try {
        if (null != stat) {
          stat.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      try {
        if (null != rs1) {
          rs1.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      try {
        if (null != rs2) {
          rs2.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return gameBoard;
  }
  
  /**
   * Clear the table.
   * @param conn Connection
   */
  public void cleanTable(Connection conn) {
    Statement stat = null;
    try {
      stat = conn.createStatement();
      stat.executeUpdate("delete from gameBoard;");
      stat.executeUpdate("delete from move;");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (null != stat) {
          stat.close();
        }   
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * update the state of gameBoard in the database.
   * @param conn Connection 
   * @param gameBoard GameBoard
   */
  public void updateGameBoard(Connection conn, GameBoard gameBoard) {
    Statement stat = null;
    try {
      stat = conn.createStatement();
      stat.executeUpdate("delete from gameBoard;");
      String sql = String.format("insert into gameBoard values (%s, %d, %d, %s, %s, %s);", 
          gameBoard.getGamestarted(), gameBoard.getTurn(), 
          gameBoard.getWinner(), gameBoard.getIsDraw(), 
          (null == gameBoard.getPlayer1() ? null : "'" + gameBoard.getPlayer1().getType() + "'"), 
          (null == gameBoard.getPlayer2() ? null : "'" + gameBoard.getPlayer2().getType() + "'"));
      stat.executeUpdate(sql);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (null != stat) {
          stat.close();
        }   
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * insert into gameBoard.
   * @param conn Connections
   * @param gameBoard gameBoard
   */
  public void insertGameBoard(Connection conn, GameBoard gameBoard) {
    Statement stat = null;
    try {
      stat = conn.createStatement();
      String sql = "insert into gameBoard values "
          + "gameStarted = " + gameBoard.getGamestarted()
          + "turn = " + gameBoard.getTurn()
          + "winner = " + gameBoard.getWinner()
          + "isDraw = " + gameBoard.getIsDraw();
      stat.executeUpdate(sql);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (null != stat) {
          stat.close();
        }   
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
}
