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

import java.io.*;
import java.net.*;
import java.util.*;


public class ChatServer {
	private static List<ChatRoom> openChats;
	private static Map<String, ClientObserver> userObservers;
	private static final String separator = Character.toString((char) 31);
	private static final String nameSeparator = Character.toString((char) 29);

	public ChatServer() {
		openChats = new ArrayList<ChatRoom>();
		userObservers = new HashMap<String, ClientObserver>();
	}

	public void setUpNetworking() throws Exception {
		@SuppressWarnings("resource") 
		ServerSocket serverSock = new ServerSocket(4242); 
		while (true) { 
			Socket clientSocket = serverSock.accept();
			System.out.println("Received connection " + clientSocket);
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			Thread t = new Thread(new ClientHandler(clientSocket, writer)); 
			t.start(); 
		}
	}


	class ClientHandler implements Runnable {
		private BufferedReader reader;
		private ClientObserver writer; 

		public ClientHandler(Socket clientSocket, ClientObserver writer) { 
			Socket sock = clientSocket;
			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
				this.writer = writer;
			} 
			catch (IOException e) { 
				e.printStackTrace();
			}
		}
		
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					String[] array = message.split(separator);
					
					// input is a message with a chat room ID attached to it
					if(isNumeric(array[0])) {
						openChats.get(0).sendMessage(message);
					}
					
					// a user should send in its username when it is first created
					else if(array[0].equals("NEWUSER")) {
						System.out.println("hi");
						String user = array[1];
						userObservers.put(user, this.writer);
						System.out.println(this.writer);
						System.out.println("hello");

					}
					
					// create a chat room when user requests to chat with others
					// users are separated by '|' when received
					else if(array[0].equals("NEWCHAT")) {
						ChatRoom newChat = new ChatRoom();
						openChats.add(newChat);
						newChat.setID(openChats.indexOf(newChat));
						newChat.addUsers(array);
						newChat.sendMessage("" + Integer.toString(newChat.getID()) + separator + "CONSOLE" + separator + "This is a new chat, send a message!" );
					}
					
					else if(array[0].equals("GETONLINE")) {
						
						System.out.println("ok");
						Set<String> keys = 	userObservers.keySet();
						String names = "";
						
						// create String with all online user names
						for(String user: keys) {
							names += user + nameSeparator;
						}
						
						// return back GETONLINE string with user names separated by nameSeparator
						names = ("GETONLINE" + separator + array[1] + separator + names);
						
						// use chat room observer pattern to send message
						String[] tempArray = names.split(separator);
						ChatRoom temp = new ChatRoom();
						temp.addUsers(tempArray);
						temp.sendMessage(names);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/** OBTAINED FROM http://stackoverflow.com/a/14206789
		 * 
		 * @param str The String to be parsed
		 * @return true if string is numeric, false otherwise	
		 */
		public boolean isNumeric(String str) {  
			try {  
				double d = Double.parseDouble(str);  
			}  
			catch(NumberFormatException nfe) {  
				return false;  
			}  
			return true;  
		}
	}

	class ChatRoom extends Observable {
		private int ID;
		
		public void setID(int ID) {
			this.ID = ID;
		}

		public int getID() {
			return ID;
		}
		
		public void addUsers(String[] array) {
			addObserver(userObservers.get(array[1]));
			String[] otherUsers = array[2].split(nameSeparator);
			System.out.println(Arrays.toString(otherUsers));
			for(int i = 0; i < otherUsers.length; i++) {
				addObserver(userObservers.get(otherUsers[i]));
			}
		}
		
		public void sendMessage(String message) {
			setChanged();
			notifyObservers(message);
		}
	}
	


	public static void main(String[] args) {
		try {
			new ChatServer().setUpNetworking();
		} catch (Exception e) { e.printStackTrace(); }
	}

}
