import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

public class tictacGUI extends JFrame{

    private static final String TITLE = "Jose Murders Puppies w/ Pleasure";
    private static final int WIDTH = 450;
    private static final int HEIGHT = 600;

    private Timer t = new Timer();
    private Container content;
    private JLabel result;

    private JButton[] cells;
    private JButton passButton;
    private JButton playButton;
    private JButton refreshButton;
    private JButton quitButton;

    private JButton choosen;

    String gameBoard;

    private boolean noughts;
    private boolean madeMove;
    private boolean isOver;
    private boolean gameBoardSet;

    public static PlayerNode node;

    public tictacGUI(PlayerNode node){
	setTitle(TITLE);
	setSize(WIDTH, HEIGHT);
	setDefaultCloseOperation(EXIT_ON_CLOSE);

	this.node = node;

	content = getContentPane();
	content.setLayout(new GridLayout(2,1));

	Container board = new Container();
	board.setLayout(new GridLayout(3,3));
	board.setSize(450,450);

	Container buttons = new Container();
	buttons.setLayout(new GridLayout(1,4));

	result=new JLabel("", SwingConstants.CENTER);

	cells = new JButton[9];
	for(int i=0; i<9; i++) {
	    cells[i] = new JButton("");
	    cells[i].setEnabled(false);
	    cells[i].addActionListener(new java.awt.event.ActionListener() {
		    public void actionPerformed(ActionEvent e) {

			JButton pressed=(JButton)(e.getSource());

			if(madeMove && (pressed == choosen)) {
			    madeMove = false;
			    choosen = null;
			    pressed.setText(""+' ');
			} else if(!madeMove && noughts) {
			    madeMove = true;
			    choosen = pressed;
			    pressed.setText("O");
			} else if(!madeMove && !noughts){
			    madeMove = true;
			    choosen = pressed;
			    pressed.setText("X");
			}
		    }
		}
		);
	}

	//Create init and exit buttons and handlers
	passButton = new JButton("PASS");
	passButton.setEnabled(false);
	passButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    passMove();
		}
	    }
	    );

	playButton = new JButton("PLAY");
	playButton.setEnabled(false);
	playButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    playMove();
		}
	    }
	    );

	refreshButton = new JButton("REFRESH");
	refreshButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    refresh();
		}
	    }
	    );

	quitButton = new JButton("QUIT");
	quitButton.setEnabled(true);
	quitButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    quitGame();
		}
	    }
	    );
	
	for(int i=0; i<9; i++) {
	    board.add(cells[i]);
	}

	buttons.add(result);
	buttons.add(quitButton);
	buttons.add(refreshButton);
	buttons.add(passButton);
	buttons.add(playButton);

	content.add(board);
	content.add(buttons);

	madeMove = false;
	choosen = null;
	setVisible(true);
	gameBoardSet = false;

	waitForBoard();
    }

    private void waitForBoard(){
	//making a timer each time it is called. needs fix.
	t.schedule(new TimerTask() {
		@Override
		    public void run() {
		    if(tictacGUI.node.isPlaying() && !gameBoardSet){
			getBoard(tictacGUI.node.getGameBoard());
		    } else if (!tictacGUI.node.inGame()) {
			getBoard(tictacGUI.node.getGameBoard());
		    }
		}
	    }, 0, 1000);
    }

    private void getBoard(String gameBoard) {
	this.gameBoard = gameBoard;

	char[] array = gameBoard.toCharArray();
	System.out.println("getBoard: "+array[0]);

	if(array[1] == 'O') {
	    noughts = true;
	    result.setText("Noughts Move");
	} else {
	    noughts = false;
	    result.setText("Crosses Move");
	}

	if(array[0] == 'X') {
	    result.setText("Crosses Win!");
	    passButton.setEnabled(false);
	    playButton.setEnabled(false);
	    t.cancel();
	    t.purge();
	    isOver = true;
	}else if(array[0] == 'O'){
	    result.setText("Noughts Win!");
	    passButton.setEnabled(false);
	    playButton.setEnabled(false);
	    t.cancel();
	    t.purge();
	    isOver = true;
	}else if(array[0] == 'T'){
	    result.setText("Tie");
	    passButton.setEnabled(false);
	    playButton.setEnabled(false);
	    t.cancel();
	    t.purge();
	    isOver = true;
	} else if (node.isPlaying()){
	    passButton.setEnabled(true);
	    playButton.setEnabled(true);
	    gameBoardSet = true;
	    isOver = false;
	}
	
	for(int i=0; i<9; i++) {
	    char ch = ' ';
	    if(array[i+2] == 'X') {
		ch = 'X';
	    } else if(array[i+2] == 'O') {
		ch = 'O';
	    } else if (!isOver && node.isPlaying()){
		cells[i].setEnabled(true);
	    }
	    cells[i].setText(""+ch);
	}
  
    }

    private void refreshBoard(String gameBoard){
	this.gameBoard = gameBoard;

	char[] array = gameBoard.toCharArray();
	System.out.println("getBoard: "+array[0]);

	if(array[1] == 'O') {
	    noughts = true;
	    result.setText("Noughts Move");
	} else {
	    noughts = false;
	    result.setText("Crosses Move");
	}

	passButton.setEnabled(false);
	playButton.setEnabled(false);
	refreshButton.setEnabled(true);
	gameBoardSet = true;
	
	for(int i=0; i<9; i++) {
	    char ch = ' ';
	    if(array[i+2] == 'X') {
		ch = 'X';
	    } else if(array[i+2] == 'O') {
		ch = 'O';
	    } 
	    cells[i].setEnabled(false);
	    cells[i].setText(""+ch);
	}
  
    }
    private void playMove() {
	String sendBoard = "";
	int count = 0;
	if(madeMove) {
	    for(int i=0; i<9; i++) {
		String str = cells[i].getText();
		if(!(str.equals("O") || str.equals("X"))) {
		    sendBoard += "-";
		} else {
		    sendBoard += str;
		    count++;
		}
	    }
	    
	    String winner = checkWinner(sendBoard);
	    if(winner.equals("-") && count == 9){
		winner = "T";
	    }

	    //pass doesnt change x or o
	    if(noughts){
		sendBoard = "X" + sendBoard;
	    } else {
		sendBoard = "O" + sendBoard;
	    }

	    sendBoard = winner + sendBoard;

	    System.out.println("playMove: "+sendBoard);
	    
	    //new
	    if(!winner.equals("-")) {
		node.sendTurn(sendBoard);
		node.sendResult(sendBoard);
		endTurn();
	    }else{
		node.sendTurn(sendBoard);
		endTurn();
		waitForBoard();
		refreshButton.setEnabled(true);
	    }
	    madeMove = false;
	} else {
	    JOptionPane.showMessageDialog(this, "You need to make a move to play it.");
	}
    }

    private void passMove() {
	System.out.println(gameBoard);
	node.sendTurn(gameBoard);
	endTurn();
	waitForBoard();
    }

    private void refresh() {
	if(!node.isPlaying()){
	    gameBoard = node.getGameBoard();
	    getBoard(gameBoard);
	}
    }

    private void endTurn() {
	for(int i = 0; i < 9; i++){
	    cells[i].setEnabled(false);
	}
	playButton.setEnabled(false);
	passButton.setEnabled(false);
	refreshButton.setEnabled(true);
	gameBoardSet = false;
    } 

    public String checkWinner(String game) {
	char[] array = game.toCharArray();
	if(array[0] == array[1] && array[1] == array[2] && array[0] != '-') {
	    return ""+array[0];
	} else if(array[3] == array[4] && array[4] == array[5] && array[3] != '-') {
	    return ""+array[3];
	} else if(array[6] == array[7] && array[7] == array[8] && array[6] != '-') {
	    return ""+array[6];
	} else if(array[0] == array[3] && array[3] == array[6] && array[0] != '-'){
	    return ""+array[0];
	} else if(array[1] == array[4] && array[4] == array[7] && array[1] != '-') {
	    return ""+array[1];
	} else if(array[2] == array[5] && array[5] == array[8] && array[2] != '-') {
	    return ""+array[2];
	} else if(array[0] == array[4] && array[4] == array[8] && array[0] != '-') {
	    return ""+array[0];
	} else if(array[2] == array[4] && array[4] == array[6] && array[2] != '-') {
	    return ""+array[2];
	} else {
		return "-";
	}
    }
    
    private void quitGame(){
	if(node.quitGame()) {
	    this.setVisible(false);
	    t.cancel();
	    t.purge();
	    this.dispose();
	}
    }
    
    /*public static void main(String[] args) {
    //Create TicTacToe object
    tictacGUI gui=new tictacGUI();
    }*/
}
