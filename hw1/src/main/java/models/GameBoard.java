package models;

public class GameBoard {

  private Player p1;

  private Player p2;

  private boolean gameStarted;

  private int turn;

  private char[][] boardState;

  private int winner;

  private boolean isDraw;
  
  /**
   * The constructor.
   */
  public GameBoard() {
    this.gameStarted = false;
    this.turn = 1;
    boardState = new char[3][3];
    winner = 0;
    isDraw = false;
  }
  
  
  public Player getPlayer1() {
    return this.p1;
  }

  public Player getPlayer2() {
    return this.p2;
  }  

  public boolean getGamestarted() {
    return this.gameStarted;
  }  
  
  public int getTurn() {
    return this.turn;
  }
  
  public char[][] getBoardState() {
    return this.boardState;
  }
  
  public int  getWinner() {
    return this.winner;
  }
  
  public boolean  getIsDraw() {
    return this.isDraw;
  }
  
  public void setPlayer1(Player p1) {
    this.p1 = p1;
  }
  
  public void setPlayer2(Player p2) {
    this.p2 = p2;
  }
  
  public void setGamestarted(boolean gamestarted) {
    this.gameStarted = gamestarted;
  }  

  public void setTurn(int turn) {
    this.turn = turn;
  }
  
  public void setBoardState(char[][] boardState) {
    this.boardState = boardState;
  }
  
  public void setWinner(int winner) {
    this.winner = winner;
  }
  
  public void setIsDraw(boolean isDraw) {
    this.isDraw = isDraw;
  }
  
  /**
   * When player 2 joins the game, start the game.
   */
  public void joinGame() {
    char type = ('X' == this.getPlayer1().getType()) ? 'O' : 'X';
    this.setPlayer2(new Player(type, 2));
    this.setGamestarted(true);
  }
  
  /**
   * Process the request of move.
   * @param i which row
   * @param j which column
   * @param playerId who want to move
   * @return the result of this request
   */
  public Message move(int i, int j, int playerId) {
    Player player = (1 == playerId) ? this.getPlayer1() : this.getPlayer2();
    Move move = new Move(player, i, j);
    int code = tryMove(move);
    return new Message(code);
  }
  
  /**
   * Try to move at the specified position.
   * @param move Describe which player wants to move to which position.
   * @return The code for message.
   */
  private int tryMove(Move move) {
    if (null == this || null == this.getPlayer1() || null == this.getPlayer2()) {
      return 301;
    }
    if (0 !=  this.getWinner()) {
      return 401;
    }
    if (this.getIsDraw()) {
      return 402;
    }
    int i = move.getMoveX();
    int j = move.getMoveY();
    if (i >= 3 || i < 0 || j >= 3 || j < 0) {
      return 201;
    }
    if (this.getIsDraw() || '\u0000' != this.getBoardState()[i][j]) {
      return 202;
    }
    if (move.getPlayer().getId() != this.getTurn()) {
      return 302;
    }

    this.getBoardState()[i][j] = move.getPlayer().getType();
    this.setTurn((this.getTurn() % 2) + 1);
    updateState();
    return 100;
  }
  
  /**
   * Determined if one of the player has won this game.
   */
  private void updateState() {
    final char[][] boardState = this.getBoardState();
    for (int i = 0; i < 3; i++) {
      if (boardState[0][i] == boardState[1][i] && boardState[0][i] == boardState[2][i]) {
        int playerId = (boardState[0][i] == this.getPlayer1().getType()) ? 1 : 2;
        if ('\u0000' != boardState[0][i]) {
          this.setWinner(playerId);
          break;
        }
      }
      if (boardState[i][0] == boardState[i][1] && boardState[i][0] == boardState[i][2]) {
        int playerId = (boardState[i][0] == this.getPlayer1().getType()) ? 1 : 2;
        if ('\u0000' != boardState[i][0]) {
          this.setWinner(playerId);
          break;
        }
      }
    }
    if (boardState[0][0] == boardState[1][1] && boardState[0][0] == boardState[2][2]) {
      int playerId = (boardState[0][0] == this.getPlayer1().getType()) ? 1 : 2;
      if ('\u0000' != boardState[0][0]) {
        this.setWinner(playerId);
      }
    }
    if (boardState[2][0] == boardState[1][1] && boardState[2][0] == boardState[0][2]) {
      int playerId = (boardState[2][0] == this.getPlayer1().getType()) ? 1 : 2;
      if ('\u0000' != boardState[2][0]) {
        this.setWinner(playerId);
      }
    }
    if (9 == checkUsedPosition() && 0 == this.getWinner()) {
      this.setIsDraw(true);
    }
  }

  /**
   * Count how many positions have been used.
   * @return The counter of used position.
   */
  private int checkUsedPosition() {
    int count = 0;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (this.boardState[i][j] != '\u0000') {
          count += 1;
        }
      }
    }
    return count;
  }
  
}
