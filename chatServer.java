/* CHAT ROOM chatServer.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import assignment7.chatClient.clientObserver;

public class chatServer {
	private ArrayList<chatRoom> openChats;
	private HashMap<String, clientObserver> info;
	
	public void setUpNetworking() {
		
	}
	
	public static void main(String[] args) {
		try {
		new chatServer().setUpNetworking();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public class chatRoom extends Observable {
		private int ID;
		
		public chatRoom(int num) {
			ID = num;
		}	
	}
	
	public class clientHandler implements Runnable {
		public void run() {
			
		}
	}
	
}
