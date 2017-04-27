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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ChatClient extends Application {

	private String name;
	private BufferedReader reader;
	private PrintWriter writer;
	private static final String separator = Character.toString((char) 31);
	private static final String nameSeparator = Character.toString((char) 29);
	private Stage home = null;


	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpNetworking();
		home = new Stage();

		primaryStage.setTitle("Chat Room");
		VBox open = new VBox();

		Label title = new Label("Welcome to Chat Room!");
		title.setFont(Font.font("Bradley Hand ITC", 30));

		Label instruction = new Label("Please enter a username:");
		TextField username = new TextField();
		username.setPromptText("Username");
		Button done = new Button("Create Account");
		done.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				String s = username.getText();
				if (s != null) { //FIX THIS!!!!!
					name = s;

					writer.println("NEWUSER" + separator + s);
					writer.flush();


					writer.println("GETONLINE" + separator + name);
					writer.flush();

					primaryStage.close();
				}	
			}

		});

		open.getChildren().addAll(title, instruction, username, done);

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

	public void loggedIn(String[] available) {
		VBox chat = new VBox();
		Label welcome = new Label("Welcome, " + name + "!");

		Label people = new Label("Who would you like to chat with?");

		CheckBox[] online = new CheckBox[available.length];
		for (int i = 0; i < available.length; i++) {
			online[i] = new CheckBox(available[i]);
			chat.getChildren().add(online[i]);
		}


		chat.getChildren().addAll(welcome, people);
		Scene realScene = new Scene(chat, 300, 300);
		home.setScene(realScene);
		home.show();
	}


	public void startChat() {
		// writer.println("NEWCHAT" + separator + name + );
		// receive back ID
	}

	class ChatWindow {

		private int ID;

		public ChatWindow(int num) {
			ID = num;

			Stage chat = new Stage();
			chat.setTitle("Chat Window");

			ScrollPane convo = new ScrollPane();
			convo.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			TextField text = new TextField();
			text.setPromptText("Enter message");
			Button send = new Button("Send");
			send.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					String s = send.getText();
					if (s != null) { //FIX THIS!!!!!!
						sendMessage(s);
					}
				}

			});
			VBox box = new VBox();
			box.getChildren().addAll(convo, text, send);
			Scene scene = new Scene(box, 300, 300);
			chat.setScene(scene);
			chat.show();
		}

		public int getID() {
			return ID;
		}

		public void updateChat() {
			// parse message, update chat
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

				}
			} catch (IOException ex) { ex.printStackTrace(); }			
		}
	}

	public static void main(String[] args) {
		try {
			launch(args);
		} catch (Exception e) { e.printStackTrace(); }
	}

}
