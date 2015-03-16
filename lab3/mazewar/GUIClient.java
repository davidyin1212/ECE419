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

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * An implementation of {@link LocalClient} that is controlled by the keyboard
 * of the computer on which the game is being run.  
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: GUIClient.java 343 2004-01-24 03:43:45Z geoffw $
 */

public class GUIClient extends LocalClient implements KeyListener {

		private ClientCommManager communicationManager;
        /**
         * Create a GUI controlled {@link LocalClient}.  
         */
        public GUIClient(String name, ClientCommManager ccm) {
                super(name);
                communicationManager = ccm;
        }
        
        /**
         * Handle a key press.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyPressed(KeyEvent e) {
        	
        	GameMessage msg = new GameMessage();
                // If the user pressed Q, invoke the cleanup code and quit. 
                if((e.getKeyChar() == 'q') || (e.getKeyChar() == 'Q')) {
                        Mazewar.quit();
                        return;
                // Up-arrow moves forward.
                } else if(e.getKeyCode() == KeyEvent.VK_UP) {
                        //forward();
                		msg.messageType = GameMessage.GAME_MESSAGE_TYPE_MOVE_PLAYER_FORWARD;
                // Down-arrow moves backward.
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                        //backup();
                	msg.messageType = GameMessage.GAME_MESSAGE_TYPE_MOVE_PLAYER_BACKWARD;
                // Left-arrow turns left.
                } else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
//                        turnLeft();
                	msg.messageType = GameMessage.GAME_MESSAGE_TYPE_TURN_LEFT;
                // Right-arrow turns right.
                } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        //turnRight();
                	msg.messageType = GameMessage.GAME_MESSAGE_TYPE_TURN_RIGHT;
                // Spacebar fires.
                } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                        //fire();
                	msg.messageType = GameMessage.GAME_MESSAGE_TYPE_FIRE;
                }
                communicationManager.registerLocalEvent(msg);
        }
        
        /**
         * Handle a key release. Not needed by {@link GUIClient}.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyReleased(KeyEvent e) {
        }
        
        /**
         * Handle a key being typed. Not needed by {@link GUIClient}.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyTyped(KeyEvent e) {
        }

}
