package models;

public class Message {

  private boolean moveValidity;

  private int code;

  private String message;
  
  /**
   * The constructor.
   * @param code The code which shows the result of the move.
   */
  public Message(int code) {
    //The code with 2 as prefix cover the exception for wrong position.
    //The code with 3 as prefix cover the exception for wrong order for player.
    this.code = code;
    this.moveValidity = false;
    if (100 == code) {
      this.message = "";
      this.moveValidity = true;
    } else if (201 == code) {
      this.message = "The position is out of bound.";
    } else if (202 == code) {
      this.message = "The position has been used.";
    } else if (301 == code) {
      this.message = "Another player is absent.";
    } else if (302 == code) {
      this.message = "It is not your turn.";
    } else if (401 == code) {
      this.message = "The game has been over.";
    } else if (402 == code) {
      this.message = "The game is draw.";
    }
  }
  
  public boolean getMoveValidity() {
    return this.moveValidity;
  }
  
  public int getCode() {
    return this.code;
  }
  
  public String getMessage() {
    return this.message;
  }
  
  public void setMoveValidity(boolean moveValidity) {
    this.moveValidity = moveValidity;
  }
  
  public void setCode(int code) {
    this.code = code;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }

}
