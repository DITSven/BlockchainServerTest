package blockchainserver.mulvey.eoin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import blockchain.mulvey.eoin.Block;

public class ChainServerThread extends Thread{
	ServerSocket serverSocket;
	Socket serviceSocket;
	BufferedReader input;
	PrintWriter output;
	ArrayList<Block> blockchain;
	
	public ChainServerThread() {
		
	}
	
	public void run() {
		while (true) {
			OpenServerSocket();
			RespondToClient();
		}
	}
	
	public void OpenServerSocket() {
		try {
			serverSocket = new ServerSocket(6262);
		}
		catch(IOException e) {
			System.out.println(e);
		}
		try {
			serviceSocket = serverSocket.accept();
		}
		catch(IOException e) {
			System.out.println(e);
		}
		try {
			input = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()));
		}
		catch(IOException e) {
			System.out.println(e);
		}
		try {
			output = new PrintWriter(serviceSocket.getOutputStream(), true);
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public void CloseServerSocket() {
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
	
	public void RespondToClient() {
		try {
			String line = input.readLine();
			int blockNumber = Integer.parseInt(line);
			line = input.readLine();
			String blockHash = line;
			if(blockchain.get(blockNumber).getBlockHash() == blockHash) {
				output.println(blockchain.get(blockNumber).getPreviousHash() + "\n");
			}
			output.println("OK");
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
}
