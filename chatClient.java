/* CHAT ROOM chatClient.java
 * EE422C Project 7 submission by
 * Samuel Zhang
 * shz96
 * 16225
 * Grace Zhuang
 * gpz68
 * 16215
 * Slip days used: <0>
 * Spring 2017
 */

package assignment7;

import java.io.*; 
import java.util.Observable;
import java.util.Observer;

public class chatClient {
	
	private String name;
	// JavaFX
	private BufferedReader reader;
	private PrintWriter writer;
	
	public void run() {
		initFX();
		setUpNetworking();
	}
	
	public void initFX() {
		// Java FX
	}
	
	public void setUpNetworking() {
		// set up network connections
	}
	
	public void newChat() {
		// create new JavaFX chat window
	}
	
	public void startChat() {
		newChat();
		// add protocol and send to writer
		// receive back ID
	}
	
	public class chatWindow {
		private int ID;
		// JavaFX
		
		public chatWindow(int num) {
			ID = num;
		}
		
		public int getID() {
			return ID;
		}
		
		public void updateChat() {
			// parse message, update chat
		}
		
		public void sendMessage() {
			// add protocol, send message
		}
		
	}
	
	public class clientObserver implements Observer {

		@Override
		public void update(Observable arg0, Object arg1) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class incomingReader implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}
