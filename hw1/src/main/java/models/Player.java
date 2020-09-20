package models;

public class Player {

  private char type;

  private int id;
  
  public Player(char type, int id) {
    this.id = id;
    this.type = type;
  }
  
  public int getId() {
    return this.id;
  }
  
  public  char getType() {
    return this.type;
  }
  
  public void setId(int id) {
    this.id = id;
  }

  public void setType(char type) {
    this.type = type;
  }
}
