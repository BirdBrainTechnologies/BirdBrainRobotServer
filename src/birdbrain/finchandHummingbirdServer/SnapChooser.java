package birdbrain.finchandHummingbirdServer;

import java.awt.Desktop;
import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.Color;

import javax.swing.JButton;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang.SystemUtils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTextField;
import javax.swing.JTextArea;

public class SnapChooser {

	private JFrame frame;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton rdbtnSnapLevel;
	private JRadioButton rdbtnSnapLevel_1;
	private JRadioButton rdbtnSnapLevel_2;
	private JRadioButton rdbtnRegularSnap;
	/**
	 * Create the application.
	 */
	public SnapChooser(boolean cloud) {
		initialize(cloud);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(final boolean cloud) {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		frame.getContentPane().setLayout(null);

		
		rdbtnSnapLevel = new JRadioButton("Level 1, Simple Blocks");
		rdbtnSnapLevel.setToolTipText("Blocks are iconic, great for preliterate students!");
		rdbtnSnapLevel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnSnapLevel.setForeground(Color.WHITE);
		rdbtnSnapLevel.setContentAreaFilled(false);
		rdbtnSnapLevel.setBounds(20, 47, 266, 23);
		frame.getContentPane().add(rdbtnSnapLevel);
		
		rdbtnSnapLevel_1 = new JRadioButton("Level 2, Blocks with Parameters");
		rdbtnSnapLevel_1.setToolTipText("Adds numbers from 0 to 10 to level 1 blocks");
		rdbtnSnapLevel_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnSnapLevel_1.setForeground(Color.WHITE);
		rdbtnSnapLevel_1.setContentAreaFilled(false);
		rdbtnSnapLevel_1.setBounds(20, 73, 266, 23);
		frame.getContentPane().add(rdbtnSnapLevel_1);
		
		rdbtnSnapLevel_2 = new JRadioButton("Level 3, Parameters and Time");
		rdbtnSnapLevel_2.setToolTipText("Provides more control over timing of blocks than level 2, and adds sensors");
		rdbtnSnapLevel_2.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnSnapLevel_2.setForeground(Color.WHITE);
		rdbtnSnapLevel_2.setContentAreaFilled(false);
		rdbtnSnapLevel_2.setBounds(20, 99, 255, 23);
		frame.getContentPane().add(rdbtnSnapLevel_2);
		
		rdbtnRegularSnap = new JRadioButton("Level 4, Regular Snap!");
		rdbtnRegularSnap.setToolTipText("Appropriate for ages 10 and up, powerful enough to use at a high school or undergraduate level!");
		rdbtnRegularSnap.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnRegularSnap.setForeground(Color.WHITE);
		rdbtnRegularSnap.setSelected(true);
		rdbtnRegularSnap.setContentAreaFilled(false);
		rdbtnRegularSnap.setBounds(20, 125, 266, 23);
		frame.getContentPane().add(rdbtnRegularSnap);
		frame.setBounds(100, 100, 301, 251);
		
		
		
		JButton btnOpenSnap = new JButton("");
		btnOpenSnap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String urlToOpen = "http://localhost:22179/SnapOffline/snap.html"; // The local URL for Snap!
				
				if(rdbtnRegularSnap.isSelected()) {
					if(cloud)
						urlToOpen = "http://bit.ly/finchstart";
					else
						urlToOpen += "#open:http://localhost:22179/FinchSnapBlocks.xml";
				}
				else if(rdbtnSnapLevel.isSelected()) {
					if(cloud)
						urlToOpen = "http://bit.ly/finchlevel1";
					else
						urlToOpen += "#open:http://localhost:22179/FinchLevel1.xml";
				}
				else if(rdbtnSnapLevel_1.isSelected()) {
					if(cloud)
						urlToOpen = "http://bit.ly/finchlevel2";
					else
						urlToOpen += "#open:http://localhost:22179/FinchLevel2.xml";
				}
				else if(rdbtnSnapLevel_2.isSelected()) {
					if(cloud)
						urlToOpen = "http://bit.ly/finchlevel3";
					else
						urlToOpen += "#open:http://localhost:22179/FinchLevel3.xml";
				}
	  		  
			  try{
	      	// The URL - set in startServer
	    		// Runs different command to open Snap in Chrome depending on OS (Windows, Mac, Linux supported)
	            if(SystemUtils.IS_OS_WINDOWS){
	          	  final String arch = System.getProperty("sun.arch.data.model","");
	          	  File chrome= new File(System.getenv("PROGRAMFILES")+"/Google/Chrome/Application/chrome.exe"); 
	          	  if("64".equals(arch) && !chrome.exists())
	          		  chrome = new File(System.getenv("PROGRAMFILES(X86)")+"/Google/Chrome/Application/chrome.exe");
	          	  File chromeFallback = new File(System.getenv("USERPROFILE")+"/AppData/Local/Google/Chrome/Application/chrome.exe");
	          	  if(chrome.exists() || chromeFallback.exists()){
	          		  String[] chromePath = {"cmd","/k","start","chrome",urlToOpen};
	          		  Runtime.getRuntime().exec(chromePath);
	          	  }
	          	  else {
	          		  throw new IOException("Cannot find Google Chrome on Windows");
	          	  }
	            }
	            else if(SystemUtils.IS_OS_MAC_OSX) {
	                File chrome = new File("/Applications/Google Chrome.app");
	                if(chrome.exists()) {
	                    String[] chromePath = {"/usr/bin/open","-a","/Applications/Google Chrome.app",urlToOpen};
	                    Runtime.getRuntime().exec(chromePath);
	                }
	                else {
	                    //Error message if Chrome not found
	                    throw new IOException("Cannot find Google Chrome on Mac");
	                }
	            }
	            else if(SystemUtils.IS_OS_LINUX) {
	          	  File chrome = new File("/usr/bin/google-chrome");
	          	  File chromium = new File("/usr/bin/chromium-browser");
	          	  if(chrome.exists()){
	                    String[] chromePath = {"google-chrome",urlToOpen};
	                    Runtime.getRuntime().exec(chromePath);
	          	  }
	          	  else if(chromium.exists()){
	          		  String[] chromePath = {"chromium-browser",urlToOpen};
	                    Runtime.getRuntime().exec(chromePath);
	          	  }
	          	  else {
	          		//Error message if Chrome not found
	                    throw new IOException("Cannot find Google Chrome or Chromium on Linux");
	          	  }
	            }
	            else {
	                throw new IOException("Incompatible Operating System");
	            }
	         
	          }
		      catch ( IOException er )
		          {
		      		//Try the default browser if Chrome doesn't work
		          	System.out.println("Error Opening Google Chrome. Now trying default browser...");
		          	final Desktop dt;
		          	if ( Desktop.isDesktopSupported() && (dt = Desktop.getDesktop()).isSupported(Desktop.Action.BROWSE))
		              {
		                  try{
		                  	// The URL - set in startServer
		                      dt.browse( new URI( urlToOpen ) );
		                      // Set the status message to something like "Opening Snap! with x blocks loaded"
		                  }
		                  catch(Exception e1){
		                  	e1.printStackTrace();
		                  }
		              }
		          	else{
		          	}
		          }
			  
			  frame.setVisible(false);
			}	
		});
		
		buttonGroup.add(rdbtnSnapLevel);
		buttonGroup.add(rdbtnSnapLevel_1);
		buttonGroup.add(rdbtnSnapLevel_2);
		buttonGroup.add(rdbtnRegularSnap);
		  
		btnOpenSnap.setContentAreaFilled(false);
		btnOpenSnap.setBorderPainted(false);
		btnOpenSnap.setSelectedIcon(new ImageIcon(SnapChooser.class.getResource("/OpenButton2Clicked.png")));
		btnOpenSnap.setBackground(Color.DARK_GRAY);
		btnOpenSnap.setIcon(new ImageIcon(SnapChooser.class.getResource("/OpenSnap.png")));
		btnOpenSnap.setBounds(0, 155, 286, 49);
		frame.getContentPane().add(btnOpenSnap);
		
		JTextArea txtrPleaseSelectThe = new JTextArea();
		txtrPleaseSelectThe.setText("Please select a programming level:");
		txtrPleaseSelectThe.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtrPleaseSelectThe.setForeground(Color.WHITE);
		txtrPleaseSelectThe.setBackground(Color.DARK_GRAY);
		txtrPleaseSelectThe.setBounds(10, 11, 265, 37);
		frame.getContentPane().add(txtrPleaseSelectThe);
		frame.setVisible(true);
	}
}
