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
					// users are separated by '|' when received
					else if(array[0].equals("NEWCHAT")) {
						ChatRoom newChat = new ChatRoom();
						openChats.add(newChat);
						newChat.setID(openChats.indexOf(newChat));
						newChat.addUsers(array);

						String userString = "";
						String[] users = array[2].split(nameSeparator);
						for(int i = 0; i < users.length; i++) {
							userString += users[i] + ", ";
						}
						userString += array[1];

						newChat.sendMessage("" + Integer.toString(newChat.getID()) + separator + "CONSOLE" + separator + "This is a new chat between: " + userString);
					}

					// a user should send in its user name when it is first created
					else if(array[0].equals("NEWUSER")) {
						String user = array[1];
						String pwd = array[2];

						boolean userExists = false;
						Scanner inFile = new Scanner(new FileReader(fileName));
						while(inFile.hasNext()) {
							String line = inFile.nextLine();
							if (line.toUpperCase().contains(("***" + array[1]).toUpperCase())) {
								userExists = true;
							}
						}

						inFile.close();

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
							System.out.println(this.writer);
							addedNewUsers = true;

							PrintWriter out = new PrintWriter(new FileWriter(fileName, true));
							out.println("***" + user + "###" + pwd);
							out.close();
						}


					}

					else if(array[0].equals("LOGIN")) {


						String user = array[1];
						String pwd = array[2];
						if(userObservers.containsKey(user)) {
							System.out.println("found key?");
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
								System.out.println(this.writer);
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

					else if(array[0].equals("LOGOUT")) {
						System.out.println(array[1]);
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

	class ChatRoom extends Observable {
		private int ID;

		public void setID(int ID) {
			this.ID = ID;
		}

		public int getID() {
			return ID;
		}

		public void addUsers(String[] array) {
			if(userObservers.containsKey(array[1])) {
				addObserver(userObservers.get(array[1]));
			}
			if(array.length > 2){
				String[] otherUsers = array[2].split(nameSeparator);
				for(int i = 0; i < otherUsers.length; i++) {
					addObserver(userObservers.get(otherUsers[i]));
				}
			}
		}

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
