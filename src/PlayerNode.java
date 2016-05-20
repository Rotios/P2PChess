import java.io.*;
import java.net.*;
import java.util.*;
import java.net.URL;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import java.lang.Thread;

public class PlayerNode{

    // By default the player is in none of the following states:
    private static boolean isMaster = false;
    //    private static boolean isHost = false;
    private static boolean inGame = false;
    private static boolean isPlaying = false;
    
    // IP's and User Info
    private static String userName = "";
    private static String portNumber = "";
    private static String myIP = "";
    private static String masterIP = "";
    
    private static String hostIP = "";
    private static String curPlayer = "";
    private static final String initialBoard = ".white.br.bn.bb.bq.bk.bb.bn.br.bp.bp.bp.bp.bp.bp.bp.bp.................................wp.wp.wp.wp.wp.wp.wp.wp.wr.wn.wb.wq.wk.wb.wn.wr";
    private static String gameBoard = ".white.br.bn.bb.bq.bk.bb.bn.br.bp.bp.bp.bp.bp.bp.bp.bp.................................wp.wp.wp.wp.wp.wp.wp.wp.wr.wn.wb.wq.wk.wb.wn.wr";
    
    // HashMaps to save known hosts and players
    private static HashMap<String, String> hosts= new HashMap<String, String>();
    private static HashMap<String, String> players= new HashMap<String, String>();
    private static HashMap<String, String> games = new HashMap<String, String>();
    
    //XMLRPC Client Vars
    private static XmlRpcClientConfigImpl config;
    private static XmlRpcClient client;
    
    //XMLRPC Server Vars
    private static XmlRpcServerConfigImpl serverConfig;
    private static XmlRpcServer xmlRpcServer;
    
    public PlayerNode(){}

    public PlayerNode(String user, String port){
	try{
	    userName = user;
	    portNumber = port;
	    myIP = getPublicIP();
	    masterIP = "localhost";
	    isMaster = true;

	    startServer();
	    startClient();
	} catch(Exception e) {System.out.println("HELLO");}
    }

    public PlayerNode(String user, String port, String master){
	try{
	    userName = user;
	    portNumber = port;
	    myIP = getPublicIP();
	    masterIP = master;
	    isMaster = false;

	    startServer();
	    startClient();
	    
	    refresh();
	    this.hosts = (HashMap<String,String>) client.execute("handler.getHosts", new String[0]);
	} catch(Exception e) {System.err.println("Exception = " + e);}
    }

    public String getCurPlayer(){
	return curPlayer;
    }

    public boolean resetState(){
	inGame = false;
	isPlaying = false;
	
	hostIP = "";
	curPlayer = "";
	return true;
    }

    // http://stackoverflow.com/questions/2939218/getting-the-external-ip-address-in-java
    public String getPublicIP(){
	try{
	    URL whatismyip = new URL("http://checkip.amazonaws.com");
	    BufferedReader in = new BufferedReader(new InputStreamReader(
									 whatismyip.openStream()));
	    
	    String ip = in.readLine(); //you get the IP as a String
	    return ip;
	} catch(Exception e) {return "";}
    }
    
    //Refresh Button
    public boolean refresh(){
	if(!isMaster){
	    try{
		//client.execute("handler.printStuff", new String[0]);
		this.hosts = (HashMap<String,String>) client.execute("handler.getHosts", new String[0]);
		
		System.out.println("In refresh = " + this.hosts);
		return true;
	    } catch(Exception e){ System.out.println("ERROR REFRESH"); return false;}
	} else {return false;}
    }
    
    //Start Button
    public boolean startGame() {
	boolean result = false;
	gameBoard = initialBoard;
	// Change the state of the node
	//	isHost = true;
	inGame = true;
	isPlaying = true;
	players.clear();
	hostIP = myIP;
	curPlayer = userName;
	addGame(userName, myIP);

	System.out.println("Started game. isMaster is " + isMaster);
	try{
	    if (isMaster) {
		this.newHost(userName,myIP);
		result = true;
	    }
	    else {
		config.setServerURL(new URL("http://" + masterIP + ":" + portNumber));
		result = (boolean) client.execute("handler.newHost", new String[] {userName, myIP});
	    }
	    this.addPlayer(userName,myIP);
	    System.out.println("In start-game = " + this.hosts);
	} catch (Exception e) {
	    return false;
	}
	return result;
    }

    public String getRandomHostIP() throws Exception{
	try{
	    HashMap<String, String> hostList = hosts;
	    if(!isMaster) {
		// Ensures that master is the current IP of the client
		config.setServerURL(new URL("http://" + masterIP + ":" + portNumber));
		
		// Get a random host from the Master's Hosts HashTable
		hostList = (HashMap<String,String>) client.execute("handler.getHosts", new String[0]);
	    }
	    
	    if(hostList.isEmpty()) throw new Exception("None Found");
	    
	    String[] keys = (String[]) hostList.keySet().toArray(new String[0]);
	    int size = keys.length;
	    if(size <= games.keySet().toArray().length) throw new Exception("None Found");
	    
	    Random rand = new Random();
	    
	    int i = rand.nextInt(keys.length);
	    String ip = hostList.get(keys[i]);
	    
	    while(games.get(ip) != null){
		i = rand.nextInt(keys.length);
		ip = hosts.get(keys[i]);
	    }
	    return ip;
		
	} catch(Exception e){throw new Exception("None Found");}
    }    

    public String getRandomPlayerIP(){
	if(players.isEmpty()) return "None Found";
	
	Random rand = new Random();
	String[] keys = (String[]) this.players.keySet().toArray(new String[0]);
	int i = rand.nextInt(keys.length);

	return this.players.get(keys[i]);
    }

    public boolean connectToHost(String hostName, String hostIP) {
	boolean result = false;
	try{	    
	    if(games.get(hostName) != null){
		return result;
	    } 
	   
	    // Connect to the random host above
	    config.setServerURL(new URL("http://" + hostIP + ":" + portNumber));
	    
	    // Tell the Host to add self to game
	    result = (boolean) client.execute("handler.addPlayer", new String[] {userName, myIP});
	    
	    //Request the Game Board
	    //gameBoard = (String) client.execute("handler.getGameBoard", new String[0]);
	    
	    // Change the state appropriately
	    isPlaying = false;
	    inGame = true;
	    addGame(hostName, hostIP);
	    result = true;
	} catch(Exception e) {
	    System.err.println("We failed in Connection to Host " + e);
	    return false;
	} 
	return result;
    }

    public boolean quitGame(String gameName) {
	try {
	    boolean check = false;
	    if (!isHost(gameName) && !isPlaying(gameName)) {
		System.out.println("Quitting Game, not host");
		config.setServerURL(new URL("http://" + games.get(gameName) + ":" + portNumber));
		client.execute("handler.removePlayer", new String[] {userName});
		games.remove(gameName);
		return true;
	    } else if (!isPlaying(gameName) || (players.keySet().toArray().length <= 1)){
		removePlayer(userName);
		if (players.keySet().toArray().length != 0) {
		    Set<String> recipients = players.keySet();
		    for(String recip: recipients){   
			config.setServerURL(new URL("http://" + players.get(recip) + 
						    ":" + portNumber));
			check = (boolean) client.execute("handler.makeHost", new Object[] {players, curPlayer, gameBoard});
			if(check) break;
		    }
		}
		if(!check){
		    gameBoard = "T" + gameBoard.substring(1);
		}

		games.remove(gameName);
		config.setServerURL(new URL("http://" + masterIP + ":" + portNumber));
		client.execute("handler.removeHost", new String[] {userName});
		
		return true;
	    }
	    return false;
	} catch (Exception e) {System.out.println("Exception at QuitGame");return false;} 
    }

    public boolean makeHost(HashMap playerList, String currentPlayer, String gameBoard) {
	try {
	    if (isHost()) {
		System.out.println("MAKING HOST");
		this.gameBoard = gameBoard;
		curPlayer = currentPlayer;
		players = playerList;
		contactAll(players, "handler.setHost", new String[] {myIP});
		config.setServerURL(new URL("http://" + masterIP + ":" + portNumber));
		client.execute("handler.newHost", new String[] {userName, myIP});
		return true;
	    } 
	    return false;
	} catch (Exception e) { System.out.println("EXception Make Host");return false;}
    }

    public boolean setHost(String hostIP){
	this.hostIP = hostIP;
	return true;
    }
    
    //Logout Button
    public boolean logout(){
	try{
	    if (isMaster){
		
		String hostIp = getRandomHostIP();
		config.setServerURL(new URL("http://" + hostIp + 
					    ":" + portNumber));
		client.execute("handler.makeMaster", new Object[] {hosts});
		
		isMaster = false;
	    }
	    
	    if(isHost()){
		quitGame(userName);
		config.setServerURL(new URL("http://" + masterIP + ":" + portNumber));
		client.execute("handler.removeHost", new String[] {userName});
	    }
	    
	    if (inGame()) {
		contactAll(games, "handler.removePlayer", new String[] {userName});
	    }

	    return true;
	} catch (Exception e) {return false;}
    }

    /*//Used while in queue for game
    public boolean waitForTurn() {
        while(!isPlaying) {}
	return isPlaying;
    }
    */

    // Sends out the turn the node made to the host.
    public boolean sendTurn(String newBoard, String gameName) {
        try{
	    if(isHost(gameName)) {
		processTurn(newBoard);
		return true;
	    } else{
		
		config.setServerURL(new URL("http://" + games.get(gameName) + ":" + portNumber));
		boolean turnMade = (boolean) client.execute("handler.processTurn", new String[] {newBoard});
		return turnMade;
	    }
	} catch(Exception e) {System.out.println("PROBLEM HERE");return false;}
    }

    public boolean processTurn(String newBoard) {
        gameBoard = newBoard;
	boolean result = false;
	try{
	    if (!newBoard.substring(0,1).equals(".")){
		curPlayer = "-";
	    }
	    else {
		String nextPlayerIP = this.getRandomPlayerIP();
		System.out.println("This guy has it = " + nextPlayerIP);
		curPlayer = getUserName(nextPlayerIP);
	    }
	}
	catch (Exception e) {System.out.println("PROBLEM PROCESSING");return false;}
	return result;
    }

    public boolean addGame(String gameName, String hostIP){
	games.put(gameName, hostIP);
	return true;
    }

    public boolean removeGame(String gameName){
	games.remove(gameName);
	return true;
    }

    //Used by host to add player to queue for game
    public boolean addPlayer(String user, String ip) {
        players.put(user, ip);
	System.out.println("The Players are: " + players);
        return true;
    }

    public boolean removePlayer(String user){
	players.remove(user);
	return true;
    }

    //Used by master node to add new host/game to list
    public boolean newHost(String name, String ip) {
	if(!isMaster) return false;
	this.hosts.put(name, ip);
	return true;
    }
    
    public boolean removeHost(String user){
	if(!isMaster) return false;
	hosts.remove(user);
	return true;
    }

    //Used to tell nodes about new master node
    public boolean newMaster(String master){
	masterIP = master;
	return true;
    }

    //Used to indicate node as new master
    public boolean makeMaster(HashMap hosts) {
	try{
	    this.hosts = hosts;
            isMaster = true;
            masterIP = myIP;
	    
	    contactAll(hosts, "handler.newMaster", new String[] {masterIP});
	} catch (Exception e) {System.out.println("FAILED to MAKE MASTER"); return false;}
	return true;
    }

    public boolean contactAll(HashMap map, String method, Object[] toSend) throws Exception{
	Set<String> recipients = map.keySet();
	if(recipients.toArray().length <= 0) return true;
	for(String recip: recipients){   
	    config.setServerURL(new URL("http://" + map.get(recip) + 
					":" + portNumber));
	    client.execute(method, toSend);
	}
	return true;
    }
    
    //Used to get known hosts of this node
    public HashMap<String,String> getHosts() {
	System.out.println("got hosts? = " + this.hosts);
	return this.hosts;
    }

    public String getGameBoard(String hostName) {
	if (!isHost(hostName)) {
	    try{
		config.setServerURL(new URL("http://" + games.get(hostName) + ":" + portNumber));
		
		//client.execute("handler.printStuff", new String[0]);
		return (String) client.execute("handler.getGameBoard", new String[]{hostName});
		
	    } catch(Exception e){ System.out.println("ERROR GetGameBoard"); return "";}
	} else return gameBoard;
    }

    //Return port number node is running on
    public String getPortNumber(){
	return portNumber;
    }

    //Return IP of the node
    public String getIP(){
	return myIP;
    }

    //Return if node is master node
    public boolean isMaster() {
	return isMaster;
    }

    public boolean isPlaying(String gameName) {
	try{
	    if(isHost(gameName)) {
		return curPlayer.equals(userName);
	    } else {
		config.setServerURL(new URL("http://" + games.get(gameName) + ":" + portNumber));
		return ((String) client.execute("handler.getCurPlayer", new String[]{})).equals(userName);
	    }
	} catch (Exception e){return false;}
    }
    
    public boolean isHost() {
	return (games.get(userName) != null);
    }

    //Return if node is hosting game
    public boolean isHost(String hostName) {
	return userName.equals(hostName);
    }

    public boolean inGame() {
	return !games.isEmpty();
    }

    public boolean inGame(String gameName){
	return (games.get(gameName) != null);
    }

    public String getUserName() {
	return userName;
    }

    public String getUserName(String IP){
	try{
	    config.setServerURL(new URL("http://" + IP + ":" + portNumber));
	    return (String) client.execute("handler.getUserName", new String[0]);
	} catch(Exception e) {return "";}
    }

    public boolean startClient(){
	try{
	    //XMLRPC Client startup
	    config = new XmlRpcClientConfigImpl();
	    client = new XmlRpcClient();
	    System.out.println("Attempting to start XML-RPC Client...");
	    config.setServerURL(new URL("http://" + masterIP  + ":" + portNumber));
	    config.setEnabledForExtensions(true);
	    client.setConfig(config);

	    System.out.println("Started Client successfully.");
	    return true;
	} catch (Exception e) {
	    //System.err.println("Failure: " + e);
	    return false;
	}
    }

    public boolean startServer(){
	try {
	    System.out.println("Attempting to start XML-RPC Server...");

	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    WebServer server = new WebServer(Integer.parseInt(portNumber));
	    xmlRpcServer = server.getXmlRpcServer();
	    serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
	    serverConfig.setEnabledForExceptions(true);
	    phm.addHandler("handler", PlayerNode.class);
	    xmlRpcServer.setHandlerMapping(phm);
	    server.start();

	    System.out.println("Started Server successfully.");
	    System.out.println("Accepting reqests. (Halt program to stop.)");
	    return true;
	} catch (Exception exception){
	    //System.err.println("JavaServer: " + exception);
	    return false;
	}
    }
}
