//(c) 2016 John Freeman
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class menuGUI extends JFrame{

    //Specifications about how the GUI is displayed
    private static final String TITLE = "Jose Really Hates Puppies";
    private static final int WIDTH = 500;
    private static final int HEIGHT = 600;

    //Container to hold everything that needs to be displayed
    private Container content;

    private static PlayerNode node;

    private JLabel info;

    private JButton startButton;
    private JButton randButton;
    private JButton refreshButton;
    private JButton logoutButton;

    private JTextArea games;
    private Highlighter highlighter;
    private HighlightPainter painter;

    String user;
    String port;
    String master;
    String myIp;

    //Constructor
    public menuGUI(String user, String port, String master){
	this.user = user;
	this.port = port;
	this.master = master;
	node = new PlayerNode(user, port, master);
	myIp = node.getIP();
	init();
    }

    public menuGUI(String user, String port){
	this.user = user;
	this.port = port;
	this.master = "localhost";
	node = new PlayerNode(user, port);
	myIp = node.getIP();
	init();
    }

    private void init() {
	//Sets dimensions of the window
	setTitle(TITLE);
	setSize(WIDTH, HEIGHT);
	setDefaultCloseOperation(EXIT_ON_CLOSE);

	//Initialize Content Pane
	content = getContentPane();
	content.setLayout(new BorderLayout());

	info = new JLabel("<html>UserName: "+user+"<br>Port: "+port+"<br>My IP: "+ myIp + "<br>Master: "+master+"</html>");
	content.add(info, BorderLayout.NORTH);

	Container buttons = new Container();
	buttons.setLayout(new GridLayout(4,1));

	startButton = new JButton("Start Game");
	startButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    startGame();
		}
	    });
	buttons.add(startButton);

	randButton = new JButton("Random Game");
	randButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    connectRandomGame();
		}
	    });
	buttons.add(randButton);

	refreshButton = new JButton("Refresh");
	refreshButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    refresh();
		}
	    });
	buttons.add(refreshButton);

	logoutButton = new JButton("Logout");
	logoutButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    logout();
		}
	    });
	buttons.add(logoutButton);

	content.add(buttons, BorderLayout.EAST);

	JScrollPane scrollPane;

	games = new JTextArea(20, 25);
	scrollPane = new JScrollPane(games);
	games.setEditable(false);
	content.add(scrollPane, BorderLayout.WEST);

	highlighter = games.getHighlighter();
	painter = new DefaultHighlighter.DefaultHighlightPainter(Color.lightGray);
	refresh();
	//set window visible
	setVisible(true);
    }

    private void startGame() {
	if(!node.isHost()){
	    node.startGame();
	    new tictacGUI(node, user);
	    System.out.println("In menu Start -" + node.getHosts());
	    // Host stuff for a game
	}
    }

    private void connectRandomGame() {
	try{
	    refresh();
	    String hostIP = node.getRandomHostIP();
	    String hostName = node.getUserName(hostIP);
	    if (hostName != user){
		if (node.connectToHost(hostName, hostIP)){
		    new tictacGUI(node, hostName);
		}
	    } else System.out.println("Reconnect");
	} catch (Exception e) {
	    System.out.println("No New IP's Found");
	}
    }

    private void refresh() {
	node.refresh();
	// Get hosts as a string and get rid of curly braces
	String hosts = node.getHosts().toString();
	hosts = hosts.substring(1);
	hosts = hosts.substring(0, hosts.length() - 1);
	
	if(hosts.contains(", ")) 
	    hosts = hosts.replace(", ","\n");
	if(hosts.contains("=")) 
	    hosts = hosts.replace("=","\t");
	
	hosts = "host\tIP\n" + hosts;
	games.setText(hosts);
    }

    private void logout() {
	node.logout();
	System.exit(1);
    }

}
