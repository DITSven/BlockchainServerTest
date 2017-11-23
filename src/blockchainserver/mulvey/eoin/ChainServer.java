package blockchainserver.mulvey.eoin;

import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import blockchain.mulvey.eoin.Block;
import java.io.*;

@SuppressWarnings("serial") //Not serializing this class
public class ChainServer extends JFrame{
	//Setup variables
	JTextArea responseWindow;
	ServerSocket serverSocket;
	Socket serviceSocket;
	BufferedReader input;
	PrintWriter output;
	File chainFile;
	ArrayList<Block> blockchain;
	
	//Constructor - Takes blockchain file location as argument 
	public ChainServer(String filePath) {
		responseWindow = new JTextArea();
		add(new JScrollPane(responseWindow));
		setSize(600,300);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		chainFile = new File(filePath);
		chainFileOpen(chainFile);
		runServer();
		
	}
	
	//Runs server connection code
	public void runServer() {
		createServerSocket();
		while(true) {
			openForConnection();
			setupStreams();
			respondToClient();
			//new ChainServerThread().start(); ...For use later when migrating code to threads.
		}
	}
	
	//Open blockchain file and load into memory, attaching to ArrayList<Block> blockchain
	@SuppressWarnings("unchecked")
	public void chainFileOpen(File chainFile) {
		try(FileInputStream fileInput = new FileInputStream(chainFile)){
			ObjectInputStream objectFileInput = new ObjectInputStream(fileInput);
			blockchain = new ArrayList<Block>();
			try {
			blockchain.addAll((ArrayList<Block>) objectFileInput.readObject()); //unchecked cast, but works once file is valid.
			}
			catch(Error e) {
				System.out.println("Blockchain file invalid");
			}
			objectFileInput.close();
			fileInput.close();
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	}
	
	//Create Socket object
	public void createServerSocket() {
		try {
			serverSocket = new ServerSocket(62626, 9999);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	//Open socket for connection
	public void openForConnection() {
		try {
			serviceSocket = serverSocket.accept();
			showMessage("Connection established.\n");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	//Create input and output streams
	public void setupStreams() {
		try {
			output = new PrintWriter(serviceSocket.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()));
			showMessage("Input and output streams established.\n");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	//Receives client data and responds - in this case takes a block number and block hash, verifies they match and returns previous hash (this is to test functionality).
	public void respondToClient() {
		output.println("SERVER-START");
		showMessage("Send SERVER-START\n");
		try {
			showMessage("Starting try block\n");
		String blockNumberLine = input.readLine();
		showMessage("Block number: " + blockNumberLine + "\n");
		int blockNumber = Integer.parseInt(blockNumberLine);
		output.println("BLOCK-NUMBER-OK");
		String blockHashLine = input.readLine();
		showMessage("Block hash: " + blockHashLine + "\n");
		blockHashLine.replaceAll("\\s+", "");
		String testHash = blockchain.get(blockNumber).getBlockHash();
		testHash.replaceAll("\\s+", "");
		showMessage("Test hash: " + testHash + "\n");
		if(testHash.equals(blockHashLine)) {
			showMessage("Previous hash sent.\n");
			output.println((String)blockchain.get(blockNumber).getPreviousHash());
			
		}
		else {
			output.println((String)blockchain.get(blockNumber).getPreviousHash());
			showMessage("Failed to send previous hash.\n");
		}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		output.println("SERVER-END");
		showMessage("Send SERVER-END\n");
	}
	
	//Close streams and sockets
	public void closeServerSocket() {
		try {
			output.close();
			input.close();
			serviceSocket.close();
			serverSocket.close();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	void showMessage(final String text){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					responseWindow.append(text);
				}
			}
		);
	}

}
