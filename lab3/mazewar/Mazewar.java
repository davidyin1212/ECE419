/*
Copyright (C) 2004 Geoffrey Alan Washburn
   
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
   
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
   
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/
  
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JOptionPane;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

/**
 * The entry point and glue code for the game.  It also contains some helpful
 * global utility methods.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

public class Mazewar extends JFrame {

		private static String namingServerHostName;
		private static int namingServerPortNo;
		
		private ClientCommManager commManager;
		
        /**
         * The default width of the {@link Maze}.
         */
        public static final int mazeWidth = 20;

        /**
         * The default height of the {@link Maze}.
         */
        public static final int mazeHeight = 10;

        /**
         * The default random seed for the {@link Maze}.
         * All implementations of the same protocol must use 
         * the same seed value, or your mazes will be different.
         */
        private final int mazeSeed = 42;

        /**
         * The {@link Maze} that the game uses.
         */
        public Maze maze = null;

        /**
         * The {@link GUIClient} for the game.
         */
        public GUIClient guiClient = null;
        
        public Vector<RemoteClient> remoteClients;

        /**
         * The panel that displays the {@link Maze}.
         */
        private OverheadMazePanel overheadPanel = null;
        
        private JScrollPane consoleScrollPane = null; 

        private JScrollPane scoreScrollPane = null;
        
        private ScoreTableModel scoreModel = null;
        /**
         * The table the displays the scores.
         */
        private JTable scoreTable = null;
        
        /** 
         * Create the textpane statically so that we can 
         * write to it globally using
         * the static consolePrint methods  
         */
        private static final JTextPane console = new JTextPane();
      
        /** 
         * Write a message to the console followed by a newline.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrintLn(String msg) {
                console.setText(console.getText()+msg+"\n");
        }
        
        /** 
         * Write a message to the console.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrint(String msg) {
                console.setText(console.getText()+msg);
        }
        
        /** 
         * Clear the console. 
         */
        public static synchronized void clearConsole() {
           console.setText("");
        }
        
        /**
         * Static method for performing cleanup before exiting the game.
         */
        public static void quit() {
                // Put any network clean-up code you might have here.
                // (inform other implementations on the network that you have 
                //  left, etc.)
                

                System.exit(0);
        }
       
        /** 
         * The place where all the pieces are put together. 
         */
        public Mazewar() {
                super("ECE419 Mazewar");
                consolePrintLn("ECE419 Mazewar started!");
                remoteClients = new Vector<RemoteClient>();
                // Create the maze
                maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
                assert(maze != null);
                
                // Create communication Manager
                commManager = new ClientCommManager(this);
                

                // Have the ScoreTableModel listen to the maze to find
                // out how to adjust scores.
                scoreModel = new ScoreTableModel();
                assert(scoreModel != null);
                maze.addMazeListener(scoreModel);
                
                
                // -----
                String prompt, name = "";
                boolean nameExists = false, joinGameSuccess = false;
                ControlMessage cm;
                do {
       
                	
                	/* Alert user if username already exists on server */
                	if (nameExists)
                		prompt = "Username(" + name + ") already exists.\nSelect new name (Empty string to quit)";
                	else
                		prompt = "Enter your name (Empty string to quit)";
                	
                	/* Prompt username*/
                    name = JOptionPane.showInputDialog(prompt);
            
                    /* On empty name quit program */
                    if((name == null) || (name.length() == 0)) {
                      Mazewar.quit();
                    }
                    
                    commManager.setClientUsername(name);
                    
                    /* Setup Message to send */
                    cm = new ControlMessage();
                    cm.messageType = ControlMessage.JOIN_GAME_REQUEST;
                    cm.myInfo = commManager.getMyInfo();
                    cm.username = name;
                    
                    
                    try {
                    	/* Send join request and process according to response */
                        Socket s = new Socket(namingServerHostName, namingServerPortNo);
        				ObjectOutputStream outStream = new ObjectOutputStream(s.getOutputStream());
                    	ObjectInputStream inStream= new ObjectInputStream(s.getInputStream());

        				outStream.writeObject(cm);
        				outStream.flush();
        				
        				Object o = inStream.readObject();
        				if (o instanceof ControlMessage) {
        					cm = (ControlMessage) o;
        					if (cm.messageType == ControlMessage.JOIN_GAME_REQUEST_FAILURE_MAX_CLIENT_REACHED) {
        						System.out.println("Game is already full - exiting game");
        						Mazewar.quit();
        					}
        					else if (cm.messageType == ControlMessage.JOIN_GAME_REQUEST_FAILURE_USERNAME_EXISTS) {
        						nameExists = true;
        					}
        					else if (cm.messageType == ControlMessage.JOIN_GAME_REQUEST_SUCCESS) {
        						joinGameSuccess = true;
        						
        						System.out.println("Successfully received Success!");
        						guiClient = new GUIClient(name, commManager);
        						commManager.initializeConnection(cm.clients);
        					}
        					else {
        						System.err.println("Error: Unhandled Message Type");
        						System.exit(1);
        					}
        				}
        				
        				else {
        					System.err.println("Error: Message received from naming service is not control message");
        					System.exit(1);
        				}
        				
        				s.close();
        				inStream.close();
        				outStream.close();
                    }
                    
	                catch (Exception e) {
	                	System.err.println("Exception while contacting naming service");
	                	e.printStackTrace();
	                	//System.exit(1);
	                }


                	
                } while (! joinGameSuccess);
                
                // You may want to put your network initialization code somewhere in
                // here.
                
                // Create the GUIClient and connect it to the KeyListener queue
                
//                maze.addClient(guiClient);
//                this.addKeyListener(guiClient);
                
                // Use braces to force constructors not to be called at the beginning of the
                // constructor.
//                {
//                        maze.addClient(new RobotClient("Norby"));
//                        maze.addClient(new RobotClient("Robbie"));
//                        maze.addClient(new RobotClient("Clango"));
//                        maze.addClient(new RobotClient("Marvin"));
//                }

                
               
                  
               
        }

        
        public void startGame() {
        	 // Create the panel that will display the maze.
            overheadPanel = new OverheadMazePanel(maze, guiClient);
            assert(overheadPanel != null);
            maze.addMazeListener(overheadPanel);
            
            // Don't allow editing the console from the GUI
            console.setEditable(false);
            console.setFocusable(false);
            console.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
           
            // Allow the console to scroll by putting it in a scrollpane
            consoleScrollPane = new JScrollPane(console);
            assert(consoleScrollPane != null);
            consoleScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Console"));
            
            // Create the score table
            scoreTable = new JTable(scoreModel);
            assert(scoreTable != null);
            scoreTable.setFocusable(false);
            scoreTable.setRowSelectionAllowed(false);

            // Allow the score table to scroll too.
            scoreScrollPane = new JScrollPane(scoreTable);
            assert(scoreScrollPane != null);
            scoreScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Scores"));
            
            // Create the layout manager
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            getContentPane().setLayout(layout);
            
            // Define the constraints on the components.
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 3.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            layout.setConstraints(overheadPanel, c);
            c.gridwidth = GridBagConstraints.RELATIVE;
            c.weightx = 2.0;
            c.weighty = 1.0;
            layout.setConstraints(consoleScrollPane, c);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            layout.setConstraints(scoreScrollPane, c);
        	
        	
        	// ------
            Vector<String> clientNames = new Vector<String>();
            clientNames.add(commManager.getMyname());
            for(ClientCommWorker peer : commManager.peers) {
            	clientNames.add(peer.username);
            }
            
            Collections.sort(clientNames);
            for (String name : clientNames) {
            	Point p = MazeImpl.getRandomPoint(ClientCommManager.randomGen);
            	if (name == guiClient.getName()) {
            		maze.addClient(guiClient, p);
            		
            	}
            	else {
            		RemoteClient remoteClient = new RemoteClient(name);
            		remoteClients.addElement(remoteClient);
            		maze.addClient(remoteClient, p);
            	}
            }
        	
        	 // Add the components
            getContentPane().add(overheadPanel);
            getContentPane().add(consoleScrollPane);
            getContentPane().add(scoreScrollPane);
            
            // Pack everything neatly.
            pack();
            this.addKeyListener(guiClient);
            // Let the magic begin.
            setVisible(true);
            overheadPanel.repaint();
            this.requestFocusInWindow();
        }
        
        public Client getClient(String username) {
        	if (guiClient.getName().equals(username)) {
        		return guiClient;
        	}
        	else {
        		for (RemoteClient rc : remoteClients) {
        			if (rc.getName().equals(username)) {
        				return rc;
        			}
        		}
        	}
        	return null;
        }
        /**
         * Entry point for the game.  
         * @param args Command-line arguments.
         */
        public static void main(String args[]) {
        	
        	if (args.length != 2) {
        		System.err.println("Usage: java Mazewar [NamingServerHostName] [NamingServerPortNumber]");
        		return;
        	}
        	namingServerHostName = args[0];
        	namingServerPortNo = Integer.parseInt(args[1]);

                /* Create the GUI */
                new Mazewar();
        }
}
