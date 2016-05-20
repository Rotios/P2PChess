import javax.swing.JButton;

public class TileButton extends JButton{
  private int column;
  private int row;

  public TileButton(int column, int row){
    this.column = column;
    this.row = row;
  }

  int getColumn() {
    return column;
  }

  int getRow() {
    return row;
  }
}
