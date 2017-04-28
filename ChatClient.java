/* CHAT ROOM ChatClient.java
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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.ListIterator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ChatClient extends Application {

	private String name;
	private BufferedReader reader;
	private PrintWriter writer;
	private static final String separator = Character.toString((char) 31);
	private static final String nameSeparator = Character.toString((char) 29);
	private Stage home = null;
	private HashMap<Integer, ChatWindow> chatWindows = new HashMap<Integer, ChatWindow>();
	private VBox open;


	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpNetworking();
		home = primaryStage;

		primaryStage.setTitle("Chat Room");
		open = new VBox();

		Label title = new Label("Welcome to Chat Room!");
		title.setFont(Font.font("Bradley Hand ITC", 30));

		Label instruction = new Label("Please enter a username:");
		TextField username = new TextField();
		username.setPromptText("Username");

		Label instruction2 = new Label("Please enter a password:");
		TextField password = new TextField();
		password.setPromptText("Password");

		Button createAcc = new Button("Create Account");
		createAcc.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				String user = username.getText();
				String pwd = password.getText();

				if (!user.equals("") && !pwd.equals("")) {
					name = user;

					writer.println("NEWUSER" + separator + user + separator + pwd);
					writer.flush();

					writer.println("GETONLINE" + separator + name);
					writer.flush();
				}	
			}

		});

		Button login = new Button("Login");
		login.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				String user = username.getText();
				String pwd = password.getText();

				if (!user.equals("") && !pwd.equals("")) {
					name = user;

					writer.println("LOGIN" + separator + user + separator + pwd);
					writer.flush();

					writer.println("GETONLINE" + separator + name);
					writer.flush();
				}	
			}

		});

		open.getChildren().addAll(title, instruction, username , password, createAcc, login);

		Scene scene = new Scene(open, 300, 300);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void setUpNetworking() throws Exception {
		System.out.println("Setting up networking");

		@SuppressWarnings("resource") 

		Socket sock = new Socket("127.0.0.1", 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		try
		{
			writer = new PrintWriter(sock.getOutputStream());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("networking established"); 
		Thread readerThread = new Thread(new IncomingReader()); 
		readerThread.start();
	}

	public void userExists(String[] message) { 


		for(ListIterator<Node> iterator = open.getChildren().listIterator(); iterator.hasNext();) {
			Node currentNode = iterator.next();
			if (currentNode instanceof Label && ((Label)currentNode).getText().contains("ERROR")) {
				iterator.remove();
			}
		}

		String error = "ERROR: Username already exists/is online. Enter another username.";
		Label notif = new Label();
		notif.setText(error);
		notif.setWrapText(true);
		open.getChildren().addAll(notif);


	}

	public void wrongPass(String[] message) { 


		for(ListIterator<Node> iterator = open.getChildren().listIterator(); iterator.hasNext();) {
			Node currentNode = iterator.next();
			if (currentNode instanceof Label && ((Label)currentNode).getText().contains("ERROR")) {
				iterator.remove();
			}
		}

		String error = "ERROR: Invalid Password. Please Retry.";
		Label notif = new Label();
		notif.setText(error);
		notif.setWrapText(true);
		open.getChildren().addAll(notif);


	}

	public void loggedIn(String[] available) {

		//close old stage
		double x = home.getX();
		double y = home.getY();
		home.close();
		
		home = new Stage();
		home.setX(x);
		home.setY(y);
		
		VBox chat = new VBox();
		Label welcome = new Label("Welcome, " + name + "!");

		Label people = new Label("Who would you like to chat with?");
		chat.getChildren().addAll(welcome, people);

		CheckBox[] online = new CheckBox[available.length-1];
		int j = 0;
		for (int i = 0; i < available.length; i++) {
			if(!(available[i].equals(name))) {
				online[j] = new CheckBox(available[i]);
				chat.getChildren().add(online[j]);
				j++;
			}
		}

		Button done = new Button("Start Chat");
		done.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				String names = "";
				System.out.println(online.length);
				for(int i = 0; i < online.length; i++) {
					if (online[i].isSelected())
						names += online[i].getText() + nameSeparator;
				}
				
				if (names.equals("")) {
					return;
				}

				String message = "NEWCHAT" + separator + name + separator + names;
				writer.println(message);
				writer.flush();

			}});




		chat.getChildren().add(done);
		Scene realScene = new Scene(chat, 300, 300);
		home.setScene(realScene);
		home.show();
	}


	/**
	 * Creates new ChatWindow
	 * @param message
	 */
	public void startChat(String[] message) {
		int ID = Integer.parseInt(message[0]);
		ChatWindow newChat = new ChatWindow(ID);
		chatWindows.put(ID, newChat);
		newChat.setTitle(message);
		newChat.updateChat(message);
	}

	class ChatWindow extends Application {

		private int ID;
		private Stage chat;
		private ScrollPane sPane;
		private GridPane convo;
		private int messageNo = 0;

		public ChatWindow(int num) {



			ID = num;
			this.chat = new Stage();
			chat.setTitle("Chat Window");

			convo = new GridPane();

			sPane = new ScrollPane();
			sPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			sPane.setHbarPolicy(ScrollBarPolicy.NEVER);




			TextField text = new TextField();
			text.setPromptText("Enter message");
			Button send = new Button("Send");
			send.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					String s = text.getText();
					if (s != null) {
						sendMessage(s);
						text.clear();
						text.setPromptText("Enter message");
					}
				}

			});

			// send message on pressing 
			text.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent keyEvent) {
					if (keyEvent.getCode() == KeyCode.ENTER)  {
						String s = text.getText();
						if (s != null) {
							sendMessage(s);
							text.clear();
							text.setPromptText("Enter message");
						}
					}
				}
			});

			VBox box = new VBox();
			GridPane screen = new GridPane();
			screen.getRowConstraints().add(new RowConstraints(270));
			screen.getRowConstraints().add(new RowConstraints(30));
			screen.getColumnConstraints().add(new ColumnConstraints(250));
			screen.getColumnConstraints().add(new ColumnConstraints(50));
			screen.add(sPane, 0, 0);
			screen.add(text,0,1);
			screen.add(send, 1, 1);


			sPane.setContent(convo);
			box.getChildren().addAll(screen);
			Scene scene = new Scene(box, 300, 300);
			sPane.setMinViewportWidth(scene.getWidth()-15);
			chat.setScene(scene);
			chat.show();
		}

		public void setTitle(String[] message) {
			String sentMessage = message[2];
			sentMessage = sentMessage.substring(28, sentMessage.length());
			System.out.println(sentMessage);
			chat.setTitle(sentMessage);
		}

		@Override
		public void start(Stage arg0) throws Exception {
			//nothing
		}

		public int getID() {
			return ID;
		}

		public void updateChat(String[] message) {
			Label text = new Label();
			text.setText(message[1] + ": " + message[2]);
			text.setWrapText(true);
			
			// if you are the user that send the message - set stuff to differentiate here
			if(message[1].equals(name)) {
				//text.setAlignment(Pos.CENTER_RIGHT);
				//text.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
				//text.setTextFill(Color.WHITE);
			}
			text.setMaxWidth(sPane.getWidth());
			text.setBorder(null);
			convo.add(text, 1, messageNo);
			messageNo++;


			sPane.setVvalue(1.0);


		}

		public void sendMessage(String message) {
			writer.println(ID + separator + name + separator + message);
			writer.flush();
		}




	}

	class IncomingReader implements Runnable {

		@Override
		public void run() {
			String incoming;
			try {
				while ((incoming = reader.readLine()) != null) {

					String[] message = incoming.split(separator);


					if (message[0].equals("GETONLINE")) {

						Platform.runLater(new Runnable() {
							@Override
							public void run() { 
								loggedIn(message[2].split(nameSeparator));
							}
						});

					}

					else if (isNumeric(message[0])) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() { 

								int ID = Integer.parseInt(message[0]);
								if (chatWindows.containsKey(ID)) {
									chatWindows.get(ID).updateChat(message);
								}

								else {
									startChat(message);
								}	
							}
						});

					}
					else if (message[0].equals("USEREXISTS")) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() { 
								userExists(message);
							}
						});
					}

					else if (message[0].equals("WRONGPASS")) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() { 
								wrongPass(message);
							}
						});
					}


				}
			} catch (IOException ex) { ex.printStackTrace(); }			
		}
	}

	private static boolean isNumeric(String str) {  
		try {  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe) {  
			return false;  
		}  
		return true;  
	}

	public static void main(String[] args) {
		try {
			launch(args);
		} catch (Exception e) { e.printStackTrace(); }
	}



}
