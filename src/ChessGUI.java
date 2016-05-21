import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;

public class ChessGUI {
    private Timer t = new Timer();
    private final JPanel gui = new JPanel(new BorderLayout(3, 3));
    private TileButton[][] chessBoardSquares = new TileButton[8][8];
    private String[][] gameBoard = new String[8][8];
    private TileHandler[][] tileHandlers = new TileHandler[8][8];
    private Image[][] chessPieceImages = new Image[2][6];
    private JPanel chessBoard;
    private JLabel message = new JLabel("This is chess.");
    private static final String COLS = "ABCDEFGH";


    public static final int PAWN = 0, BISHOP = 1, KNIGHT = 2, ROOK = 3, QUEEN = 4, KING = 5;
    public static final int[] STARTING_ROW = { ROOK, KNIGHT, BISHOP, KING, QUEEN, BISHOP, KNIGHT, ROOK };
    public static final int BLACK = 1, WHITE = 0;
    private static final String initialBoard =
	"br.bn.bb.bq.bk.bb.bn.br.bp.bp.bp.bp.bp.bp.bp.bp.................................wp.wp.wp.wp.wp.wp.wp.wp.wr.wn.wb.wq.wk.wb.wn.wr";  
  
    private boolean madeMove = false;
    private TileButton prev = null, current = null;
    private Color col;
    private ImageIcon icon1, icon2;
    private boolean isOver = false;
    private boolean gameBoardSet = false;
    private boolean isPlaying = false;

    private JFrame f;

    private Board cBoard;
    private String color;

    private static PlayerNode node;
    private String host;

    public ChessGUI(PlayerNode node, String host) {
	this.node = node;
	this.host = host;
	cBoard = new Board();
	initializeGui();
	getBoard(node.getGameBoard(host));
	f = new JFrame("\"Jose Kills Puppies in a Brutal Manner\" -"+host);
	f.setDefaultLookAndFeelDecorated(false);
	f.add(gui);
	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	f.setLocationByPlatform(true);
	f.pack();
	f.setMinimumSize(f.getSize());
	f.setVisible(true);
	waitForBoard();
    }

    public final void initializeGui() {
	try {
	    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	} catch(Exception e) {}

	// create the images for the chess pieces
	createImages();

	// set up the main GUI
	gui.setBorder(new EmptyBorder(5, 5, 5, 5));
	JToolBar tools = new JToolBar();
	tools.setFloatable(false);
	gui.add(tools, BorderLayout.PAGE_START);

	Action undoAction = new AbstractAction("Undo") {

		@Override
		    public void actionPerformed(ActionEvent e) {
		    undoMove();
		}
	    };
	tools.add(undoAction);
	tools.addSeparator();

	Action playAction = new AbstractAction("Play") {

		@Override
		    public void actionPerformed(ActionEvent e) {
		    playMove();
		}
	    };
	tools.add(playAction);

	tools.addSeparator();

	Action passAction = new AbstractAction("Pass") {

		@Override
		    public void actionPerformed(ActionEvent e) {
		    passMove();
		}
	    };
	tools.add(passAction);

	tools.addSeparator();
	Action quitAction = new AbstractAction("Quit") {

		@Override
		    public void actionPerformed(ActionEvent e) {
		    quitGame();
		}
	    };
	tools.add(quitAction);

	tools.addSeparator();
	tools.add(message);

	chessBoard = new JPanel(new GridLayout(0, 9)) {

		/**
		 * Override the preferred size to return the largest it can, in
		 * a square shape.  Must (must, must) be added to a GridBagLayout
		 * as the only component (it uses the parent as a guide to size)
		 * with no GridBagConstaint (so it is centered).
		 */
		@Override
		    public final Dimension getPreferredSize() {
		    Dimension d = super.getPreferredSize();
		    Dimension prefSize = null;
		    Component c = getParent();
		    if (c == null) {
			prefSize = new Dimension(
						 (int)d.getWidth(),(int)d.getHeight());
		    } else if (c!=null &&
			       c.getWidth()>d.getWidth() &&
			       c.getHeight()>d.getHeight()) {
			prefSize = c.getSize();
		    } else {
			prefSize = d;
		    }
		    int w = (int) prefSize.getWidth();
		    int h = (int) prefSize.getHeight();
		    // the smaller of the two sizes
		    int s = (w>h ? h : w);
		    return new Dimension(s,s);
		}
	    };
	chessBoard.setBorder(new CompoundBorder(
						new EmptyBorder(8,8,8,8),
						new LineBorder(Color.BLACK)
						));
	// Set the BG to be ochre
	Color ochre = new Color(204,119,34);
	chessBoard.setBackground(ochre);
	JPanel boardConstrain = new JPanel(new GridBagLayout());
	boardConstrain.setBackground(ochre);
	boardConstrain.add(chessBoard);
	gui.add(boardConstrain);

	// create the chess board squares
	Insets buttonMargin = new Insets(0, 0, 0, 0);
	for (int ii = 0; ii < chessBoardSquares.length; ii++) {
	    for (int jj = 0; jj < chessBoardSquares[ii].length; jj++) {
		TileButton b = new TileButton(ii, 7-jj);
		tileHandlers[jj][ii]=new TileHandler();
		b.addActionListener(tileHandlers[jj][ii]);
		b.setMargin(buttonMargin);
		// our chess pieces are 64x64 px in size, so we'll
		// 'fill this in' using a transparent icon..
		ImageIcon icon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
		b.setIcon(icon);
		if ((jj % 2 == 1 && ii % 2 == 1) || (jj % 2 == 0 && ii % 2 == 0)) {
		    b.setBackground(Color.WHITE);
		} else {
		    b.setBackground(Color.darkGray);
		}
		chessBoardSquares[ii][jj] = b;
	    }
	}

	/*
	 * fill the chess board
	 */
	chessBoard.add(new JLabel(""));
	// fill the top row
	for (int ii = 0; ii < 8; ii++) {
	    chessBoard.add(
			   new JLabel(COLS.substring(ii, ii + 1), SwingConstants.CENTER));
	}
	// fill the black non-pawn piece row
	for (int ii = 0; ii < 8; ii++) {
	    for (int jj = 0; jj < 8; jj++) {
		switch (jj) {
		case 0:
		    chessBoard.add(new JLabel("" + (9-(ii + 1)), SwingConstants.CENTER));
		default:
		    chessBoard.add(chessBoardSquares[jj][ii]);
		}
	    }
	}
    }

    private final void createImages() {
	try {
	    BufferedImage bi = ImageIO.read(getClass().getResource("resources/ChessPieces.png"));
	    for (int ii = 0; ii < 2; ii++) {
		for (int jj = 0; jj < 6; jj++) {
		    chessPieceImages[ii][jj] = bi.getSubimage(jj * 64, ii * 64, 64, 64);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public void getBoard(String strBoard){
	String[] pieces = strBoard.split("\\.");

	if(pieces[0].equals("white")){
	    isOver = true;
	    t.cancel();
	    t.purge();
	    message.setText("White Wins!");
	}
	else if(pieces[0].equals("black")){
	    isOver = true;
	    t.cancel();
	    t.purge();
	    message.setText("Black Wins!");
	}

	if(pieces[1].equals("white")){
	    color = "white";
	    message.setText("White's Move!");
	}
	else if(pieces[1].equals("black")){
	    color = "black";
	    message.setText("Black's Move!");
	}

	setBoard(Arrays.copyOfRange(pieces, 2, pieces.length));
    }

	private void clearBoard(){
		for(int i = 0; i < 8; i++){
		for(int j = 0; j < 8; j++){
		chessBoardSquares[j][i].setIcon(new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)));
}
}
	}

    private void setBoard(String[] pieces){
	cBoard.makeBoard(pieces);
	int index = 0;
	try{
	    for(int i = 0; i < 8; i++){
		for(int j = 0; j < 8; j++){
		    String piece = pieces[index];
		    gameBoard[Math.abs(i-7)][j] = piece;
		    int race = -1;
		    int rank = -1;

		    switch(piece) {
		    case "wp":
			race = WHITE;
			rank = PAWN;
			break;
		    case "bp":
			race = BLACK;
			rank = PAWN;
			break;
		    case "wr":
			race = WHITE;
			rank = ROOK;
			break;
		    case "br":
			race = BLACK;
			rank = ROOK;
			break;
		    case "wn":
			race = WHITE;
			rank = KNIGHT;
			break;
		    case "bn":
			race = BLACK;
			rank = KNIGHT;
			break;
		    case "wb":
			race = WHITE;
			rank = BISHOP;
			break;
		    case "bb":
			race = BLACK;
			rank = BISHOP;
			break;
		    case "wq":
			race = WHITE;
			rank = QUEEN;
			break;
		    case "bq":
			race = BLACK;
			rank = QUEEN;
			break;
		    case "wk":
			race = WHITE;
			rank = KING;
			break;
		    case "bk":
			race = BLACK;
			rank = KING;
			break;
		    default:
			gameBoard[i][j] = "";
		    }

		    if(rank != -1 && race != -1)
			chessBoardSquares[j][i].setIcon(new ImageIcon(chessPieceImages[race][rank]));

		    index++;
		}
	    }
	} catch(Exception e){}
    }

    private void playMove() {
        if(!madeMove || !node.isPlaying(host)) return;

        String sendBoard = "";
        int column1, column2, row1, row2;

        column1 = prev.getColumn();
        row1 = prev.getRow();
        column2 = current.getColumn();
        row2 = current.getRow();

        String moved = gameBoard[row1][column1];
        gameBoard[row2][column2] = moved;
        gameBoard[row1][column1] = "";

        String move = intToChar(column1)+(row1+1)+" "+intToChar(column2)+(row2+1);

        try{
	    cBoard.performMove(move, color, true);
	    //System.out.println(move);
        } catch(IOException e) {
	    //System.out.println(e);
        }

        Piece[][] oldBoard = cBoard.board.clone();

        if(cBoard.isInCheckMate(colorToggle(color))){
	    sendBoard = color;
        }

        cBoard.board = oldBoard;

        color = colorToggle(color);

        for(int i = 0; i < 8; i++){
	    for(int j = 0; j < 8; j++){
		sendBoard += "." + gameBoard[Math.abs(i-7)][j];
	    }
        }

        sendBoard = "." + color + sendBoard;

        System.out.println(sendBoard);
        System.out.println(cBoard);

	node.sendTurn(sendBoard, host);

	gameBoardSet = false;
        madeMove = false;
        prev = null;
    }

    private String colorToggle(String col){
        if(color.equals("white")){
            return "black";
        }

        return "white";
    }

    private String intToChar(int i) {
        String c = "";

        switch(i) {
	case 0:
            c = "a";
            break;
	case 1:
            c = "b";
            break;
	case 2:
            c = "c";
            break;
	case 3:
            c = "d";
            break;
	case 4:
            c = "e";
            break;
	case 5:
            c = "f";
            break;
	case 6:
            c = "g";
            break;
	case 7:
            c = "h";
            break;
        }

        return c;
    }

    private void undoMove() {
        if(!madeMove || !node.isPlaying(host)) return;

        prev.setIcon(icon1);
        prev = null;
        current.setIcon(icon2);
        madeMove = false;
    }

    private class TileHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
	    TileButton pressed = (TileButton)(e.getSource());

	    //System.out.println(gameBoard[pressed.getRow()][pressed.getColumn()]);

	    if(madeMove || !node.isPlaying(host)){
		return;
	    }
	    if(prev == null && gameBoard[pressed.getRow()][pressed.getColumn()].equals("")){
		return;
	    }
	    if(prev == null && color.equals("white") && gameBoard[pressed.getRow()][pressed.getColumn()].startsWith("b")){
		return;
	    }
	    if(prev == null && color.equals("black") && gameBoard[pressed.getRow()][pressed.getColumn()].startsWith("w")){
		return;
	    }

	    if(prev == null && !gameBoard[pressed.getRow()][pressed.getColumn()].equals("")){
		prev = pressed;
		col = pressed.getBackground();
		icon1 = (ImageIcon) pressed.getIcon();
		pressed.setBackground(Color.red);
	    } else if(prev == pressed){
		prev = null;
		pressed.setBackground(col);
	    } else {
		int[] array = {prev.getColumn(), prev.getRow()+1, pressed.getColumn(), pressed.getRow()+1};
		//System.out.println(array[0]+" "+array[1]+" "+array[2]+" "+array[3]);

		String move = intToChar(prev.getColumn())+(prev.getRow()+1)+" "+intToChar(pressed.getColumn())+(pressed.getRow()+1);
		//System.out.println(move);

		boolean test = true;
		try{
		    cBoard.performMove(move, color, false);
		} catch(IOException io){
		    test = false;
		}

		System.out.println(""+test);

		if(test){
		    prev.setBackground(col);
		    prev.setIcon(new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)));
		    current = pressed;
		    icon2 = (ImageIcon) pressed.getIcon();
		    pressed.setIcon(icon1);
		    madeMove = true;
		}
	    }

        }
    }

    private void refresh() {
	if(!isOver){
		clearBoard();
	    String game = node.getGameBoard(host);
	    getBoard(game);
	    gameBoardSet = true;
	} 
    }

    private void waitForBoard(){
	t.schedule(new TimerTask() {
		@Override
		    public void run() {
		    isPlaying = node.isPlaying(host);
		    if(isPlaying && !gameBoardSet){
			JOptionPane.showMessageDialog(f, "It's your turn!");
			refresh();
		    } else if (!isPlaying){ 
			System.out.println("refreshing");
			refresh();
		    }
		}
	    }, 0, 1000000);
    }

    private void passMove() {
	String sendBoard = "";

	if(cBoard.isInCheckMate(colorToggle(color))){
	    sendBoard = color;
        }

        for(int i = 0; i < 8; i++){
	    for(int j = 0; j < 8; j++){
		sendBoard += "." + gameBoard[Math.abs(i-7)][j];
	    }
        }

        sendBoard = "." + color + sendBoard;

	node.sendTurn(sendBoard, host);
    }

    private void quitGame(){
	if(node.quitGame(host)) {
	    f.setVisible(false);
	    t.cancel();
	    t.purge();
	    f.dispose();
	}
    }
}
