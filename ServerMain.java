/* CHAT ROOM ChatServer.java
 * EE422C Project 7 submission by
 * Samuel Zhang
 * shz96
 * 16225
 * Grace Zhuang
 * gpz68
 * 16215
 * Slip days used: <1>
 * Spring 2017
 */

package assignment7;

import java.io.*;
import java.net.*;
import java.util.*;


public class ServerMain {
	private static List<ChatRoom> openChats;
	private static Map<String, ClientObserver> userObservers;
	private static final String separator = Character.toString((char) 31);
	private static final String nameSeparator = Character.toString((char) 29);
	private static String fileName = "C:\\ChatRoom\\users.txt";
	private boolean addedNewUsers = false;
	private ServerSocket serverSock;




	public ServerMain() throws FileNotFoundException, IOException {
		openChats = new ArrayList<ChatRoom>();
		userObservers = new HashMap<String, ClientObserver>();

		// Create one directory
		new File("C:\\ChatRoom").mkdir();

		File yourFile = new File(fileName);

		// create new file if it doesn't exist
		try {
			yourFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Set up network sockets for server and create threads for each unique client
	 * @throws Exception
	 */
	public void setUpNetworking() throws Exception {
		this.serverSock = new ServerSocket(4242); 
		while (true) { 
			Socket clientSocket = serverSock.accept();
			System.out.println("Received connection " + clientSocket);
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			Thread t = new Thread(new ClientHandler(clientSocket, writer)); 
			t.start(); 
		}
	}


	/**
	 * ClientHandler is a Runnable that constantly checks for "messages" from the clients
	 * which then get processed via a standard protocol.
	 *
	 */
	class ClientHandler implements Runnable {
		private BufferedReader reader;
		private ClientObserver writer;
		private Socket sock;

		public ClientHandler(Socket clientSocket, ClientObserver writer) {
			this.sock = clientSocket;

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
						openChats.get(Integer.parseInt(array[0])).sendMessage(message);
					}



					// create a chat room when user requests to chat with others
					// users are separated by nameSeparator when received
					else if(array[0].equals("NEWCHAT")) {
						String[] users = array[2].split(nameSeparator);

						// check to see if chat with users already exists
						String[] names = new String[users.length + 1];
						names[0] = array[1];
						for(int i = 0; i< users.length; i++) {
							names[i+1] = users[i];
						}

						boolean exist = false;
						int ID = -1;
						for(ChatRoom room: openChats) {
							if(room.users.containsAll(Arrays.asList(names)) && room.users.size() == names.length) {
								exist = true;
								ID = room.getID();
								break;
							}
						}





							if(!exist) {
								ChatRoom newChat = new ChatRoom();
								openChats.add(newChat);
								newChat.setID(openChats.indexOf(newChat));
								newChat.addUsers(array);

								String userString = "";
								for(int i = 0; i < users.length; i++) {
									userString += users[i] + ", ";
								}
								userString += array[1];

								newChat.sendMessage("" + Integer.toString(newChat.getID()) + separator + "CONSOLE" + separator + "This is a new chat between: " + userString);
							} else {
								ChatRoom chat = openChats.get(ID);
								chat.sendMessage("" + Integer.toString(chat.getID()) + separator + "CONSOLE" + separator + "Chat refresh requested.");
							}
						}

						// a user should send in its user name when it is first created
						else if(array[0].equals("NEWUSER")) {
							String user = array[1];
							String pwd = array[2];

							// check to see if user has already been created in text file
							boolean userExists = false;
							Scanner inFile = new Scanner(new FileReader(fileName));
							while(inFile.hasNext()) {
								String line = inFile.nextLine();
								if (line.toUpperCase().contains(("***" + array[1]).toUpperCase())) {
									userExists = true;
								}
							}

							inFile.close();

							// if user is online OR user already exists within text file
							if(userObservers.containsKey(user) || userExists) {
								userObservers.put("ERRORNAME", this.writer);
								String error = "USEREXISTS" + separator + "ERRORNAME" + separator +  "ERRORNAME";
								String[] tempArray = error.split(separator);
								ChatRoom temp = new ChatRoom();
								temp.addUsers(tempArray);
								temp.sendMessage(error);
								userObservers.remove("ERRORNAME");
							}
							else {
								userObservers.put(user, this.writer);
								addedNewUsers = true;

								PrintWriter out = new PrintWriter(new FileWriter(fileName, true));
								out.println("***" + user + "###" + pwd);
								out.close();
							}


						}

						// attempt to try to log into chat server
						else if(array[0].equals("LOGIN")) {


							String user = array[1];
							String pwd = array[2];
							if(userObservers.containsKey(user)) {
								userObservers.put("ERRORNAME", this.writer);
								String error = "ALREADYLOGGEDIN" + separator + "ERRORNAME" + separator +  "ERRORNAME";
								String[] tempArray = error.split(separator);
								ChatRoom temp = new ChatRoom();
								temp.addUsers(tempArray);
								temp.sendMessage(error);
								userObservers.remove("ERRORNAME");

							}
							else {
								boolean userExists = false;
								Scanner inFile = new Scanner(new FileReader(fileName));
								while(inFile.hasNext()) {
									String line = inFile.nextLine();
									if (line.toUpperCase().contains((("***") + array[1]).toUpperCase()) && line.contains("###" + pwd)) {
										userExists = true;
									}
								}

								if(userExists) {
									userObservers.put(user, this.writer);
									addedNewUsers = true;
								} else {
									userObservers.put("ERRORNAME", this.writer);
									String error = "WRONGPASS" + separator + "ERRORNAME" + separator +  "ERRORNAME";
									String[] tempArray = error.split(separator);
									ChatRoom temp = new ChatRoom();
									temp.addUsers(tempArray);
									temp.sendMessage(error);
									userObservers.remove("ERRORNAME");
								}



								inFile.close();
							}
						}

						// retrieve all online people if there has been a change in 
						// # of clients and print out change to every client
						else if(array[0].equals("GETONLINE")) {

							if(addedNewUsers) {
								addedNewUsers = false;
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

						// LOGOUT removes user from our HashMap
						else if(array[0].equals("LOGOUT")) {
							userObservers.remove(array[1]);
							addedNewUsers = true;
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

		/**
		 * ChatRoom acts as a holder of information and implements the Observable
		 * abstract class which allows us to utilize the Observer/Observable
		 * framework built into Java
		 */
		class ChatRoom extends Observable {

			public List<String> users = new ArrayList<String>();
			private int ID;

			public void setID(int ID) {
				this.ID = ID;
			}

			public int getID() {
				return ID;
			}

			/**
			 * Adds users to the observers list
			 * @param array holds a username within array[1] and a list of user
			 * 				in array[2] which is separated by nameSeparator
			 */
			public void addUsers(String[] array) {
				if(userObservers.containsKey(array[1])) {
					addObserver(userObservers.get(array[1]));
					users.add(array[1]);
				}
				if(array.length > 2){
					String[] otherUsers = array[2].split(nameSeparator);
					for(int i = 0; i < otherUsers.length; i++) {
						addObserver(userObservers.get(otherUsers[i]));
						users.add(otherUsers[i]);
					}
				}
			}

			/**
			 * uses setChanged and pushes message to all Observers
			 * @param message
			 */
			public void sendMessage(String message) {
				setChanged();
				notifyObservers(message);
			}
		}



		public static void main(String[] args) {
			try {
				new ServerMain().setUpNetworking();
			} catch (Exception e) { e.printStackTrace(); }


		}
	}
