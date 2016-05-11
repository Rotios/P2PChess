//(c) 2015 John Freeman
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class p2pGUI extends JFrame {

    //Specifications about how the GUI is displayed
    private static final String TITLE = "Jose Hates Puppies";
    private static final int WIDTH = 350;
    private static final int HEIGHT = 200;

    //Container to hold everything that needs to be displayed
    private Container content;

    private JLabel nickLabel;
    private JLabel portLabel;
    private JLabel ipLabel;

    private JTextField nickField;
    private JTextField portField;
    private JTextField ipField;

    private JButton connectButton;
    private JButton networkButton;

    private PlayerNode node;

    //Constructor
    public p2pGUI(){
	//Sets dimensions of the window
	setTitle(TITLE);
	setSize(WIDTH, HEIGHT);
	setDefaultCloseOperation(EXIT_ON_CLOSE);

	//Initialize Content Pane
	content = getContentPane();
	content.setLayout(new GridLayout(4,2));

	//Initalize search field with a listener
    nickLabel = new JLabel();
    nickLabel.setText("Nickname: ");
    content.add(nickLabel);
	nickField = new JTextField(20);
	content.add(nickField);

    portLabel = new JLabel();
    portLabel.setText("Port #: ");
    content.add(portLabel);
    portField = new JTextField(10);
    content.add(portField);

    ipLabel = new JLabel();
    ipLabel.setText("IP Address: ");
    content.add(ipLabel);
    ipField = new JTextField(20);
    content.add(ipField);

	networkButton = new JButton("Start Network");
        networkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNetwork();
            }
        });
	content.add(networkButton);

    connectButton = new JButton("Connect");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectToNetwork();
            }
        });
        content.add(connectButton);

	//set window visible
	setVisible(true);
    }

    private void startNetwork() {
        String warning = "";

        int nickLen = nickField.getText().length();
        if(nickLen < 4 || nickLen > 15)
            warning += "Nickname must be between 4 and 15 characters.\n";

        String port = portField.getText();

        try {
            int portNum = Integer.parseInt(port);
            if(portNum < 8000 || portNum > 9999)
                warning += "Port # must be a number between 8000 - 9999.\n";
        } catch(NumberFormatException e) {
            warning += "Port # must be a number between 8000 - 9999.\n";
        }

        if(!warning.equals(""))
            JOptionPane.showMessageDialog(this, warning);
        else {
          menuGUI menu = new menuGUI(nickField.getText(), port, "You");
          this.setVisible(false);
        }
    }

    private void connectToNetwork() {
        String warning = "";

        int nickLen = nickField.getText().length();
        if(nickLen < 4 || nickLen > 15)
            warning += "Nickname must be between 4 and 15 characters.\n";

        String port = portField.getText();

        try {
            int portNum = Integer.parseInt(port);
            if(portNum < 8000 || portNum > 9999)
                warning += "Port # must be a number between 8000 - 9999.\n";
        } catch(NumberFormatException e) {
            warning += "Port # must be a number between 8000 - 9999.\n";
        }

	// if()
    }

    public static void main(String[] args) {
        p2pGUI gui = new p2pGUI();
    }
}
