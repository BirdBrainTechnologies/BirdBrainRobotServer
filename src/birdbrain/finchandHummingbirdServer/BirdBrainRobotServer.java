package birdbrain.finchandHummingbirdServer;

import java.awt.EventQueue;
import java.awt.Desktop;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.awt.Color;

import javax.swing.JButton;

import java.awt.Font;

import javax.swing.SwingConstants;
import javax.usb.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;

import java.awt.Toolkit;

import jssc.SerialPortList;

/**
 * BirdBrain Robot Server - main class for exposing Finch and Hummingbird functionality over a localhost server.
 * Primarily used to connect robots to Snap! right now, but may have other uses in the future.
 * @author Tom
 *
 */

public class BirdBrainRobotServer {

	private JFrame frmBirdbrainRobotServer; // The JFrame for the GUI end of the application
	private JLabel lblFinchpic; // Finch connected/not connected image
	private JLabel lblHummingbirdpic; // Hummingbird connected/not connected image
	private JLabel lblHelperText;  // Text which displays status messages and warnings in the GUI
	private Server server = null; // Our server object - it's up here so we can stop/destroy it when the window closes
	private FinchServletWrapper finch; // Wrapper of the Finch object
	private HummingbirdServletWrapper hummingbird; // Wrapper of the Hummingbird object
	private boolean finchConnected; // Tracks if we have a connected Finch
	private boolean hummingbirdConnected; // Tracks if we have a connected Hummingbird
	private FinchServlet finchServlet;
	private HummingbirdServlet hummingbirdServlet;
	private ResetServlet resetServlet;
	private PollServlet pollServlet;
	private String urlToOpenRemote = "http://bit.ly/"; // The URL to open when you click "Open Snap!". Depending on what's connected, this gets altered
	private String urlToOpenLocal = "http://localhost:22179/SnapOffline/snap.html"; // The local URL for Snap!
	private String urlToOpen;
	private String statusMessage; // The message we want to set the helper text to
	private CheckConnections connector;
	
	private JCheckBox chckbxOpenSnapLocally; // Checkbox for open button
	private JButton btnOpenSnap; // Open Snap! button contained
	private JButton btnOpenScratch;
	
	private SnapChooser chooser;
	
	private String[] ports;
	
	/**
	 * Launch the application.
	 */

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BirdBrainRobotServer window = new BirdBrainRobotServer();
					window.frmBirdbrainRobotServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public BirdBrainRobotServer() {
		initialize(); // Initialize GUI
		startServer(); // Start Jetty Server
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBirdbrainRobotServer = new JFrame();
		frmBirdbrainRobotServer.setIconImage(Toolkit.getDefaultToolkit().getImage(BirdBrainRobotServer.class.getResource("/LightBulbKnockedOut.png")));
		frmBirdbrainRobotServer.addWindowListener(new WindowAdapter() {
			@Override
			// If the window is closing, disconnect the Finch and Hummingbird, stop and destroy the server, and then exit
			public void windowClosing(WindowEvent arg0) {
				frmBirdbrainRobotServer.setVisible(false);
								
				if(connector != null) {
					connector.stop(); //stop the connector thread
				}
				try {
					Thread.sleep(500); 
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(server != null) {
					if(hummingbirdConnected) {
                    	hummingbird.disConnect();
                    }
					if(finchConnected) {
                		finch.disConnect();
                    }
                    try {
	               		server.stop();
	               	}
	               	catch(Exception ex)
	               	{
	               		System.out.println("Error when stopping jetty server: " + ex.getMessage());
	               	}
	           		//server.destroy();
	                System.exit(0);    
               }				
			}
		});
		frmBirdbrainRobotServer.getContentPane().setBackground(Color.DARK_GRAY);
		frmBirdbrainRobotServer.setTitle("BirdBrain Robot Server");
		frmBirdbrainRobotServer.setBackground(Color.DARK_GRAY);
		frmBirdbrainRobotServer.setBounds(100, 100, 450, 450);
		frmBirdbrainRobotServer.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmBirdbrainRobotServer.getContentPane().setLayout(null);
		
		lblFinchpic = new JLabel("");
		lblFinchpic.setBounds(10, 7, 233, 200);
		lblFinchpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/FinchNotConnected.png")));
		frmBirdbrainRobotServer.getContentPane().add(lblFinchpic);
		
		lblHummingbirdpic = new JLabel("");
		lblHummingbirdpic.setBounds(224, 7, 200, 200);
		lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdNotConnectedDuo.png")));
		frmBirdbrainRobotServer.getContentPane().add(lblHummingbirdpic);
		
		btnOpenSnap = new JButton("");
		btnOpenSnap.setFocusable(false);
		btnOpenSnap.setRolloverIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/OpenButton2Clicked.png")));
		btnOpenSnap.setDefaultCapable(false);
		btnOpenSnap.setBorder(null);
		// Open Snap should open Google Chrome
		btnOpenSnap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
          
              
              // Based on what's connected, load the appropriate blocks library by setting the URL
      		  // Notify the user through the status message
      		  if(finchConnected && hummingbirdConnected) {
      			  if(chckbxOpenSnapLocally.isSelected()) {
      				  urlToOpen = urlToOpenLocal + "#open:http://localhost:22179/FinchHummingbirdSnapBlocks.xml";
      			  }
      			  else {
      				  urlToOpen = urlToOpenRemote + "finchhummingbirdstart";
      			  }
      			  statusMessage = "Opening Snap! with Finch and Hummingbird blocks loaded...";
      		  }
      		  else if(finchConnected) {
      			  //urlToOpen += "#open:http://localhost:22179/FinchSnapBlocks.xml";
      			  chooser = new SnapChooser(!chckbxOpenSnapLocally.isSelected());
      			  statusMessage = "Opening Snap! with Finch blocks loaded...";
      		  }
      		  else if(hummingbirdConnected) {
      			if(chckbxOpenSnapLocally.isSelected()) {
    				  urlToOpen = urlToOpenLocal + "#open:http://localhost:22179/HummingbirdSnapBlocks.xml";
    			  }
    			  else {
    				  urlToOpen = urlToOpenRemote + "hummingbirdstart";
    			  }
      			  statusMessage = "Opening Snap! with Hummingbird blocks loaded...";
      		  }
      		  else
      		  {
      			  lblHelperText.setText("<html>No Finch or Hummingbird detected.<br> Plug in a robot and restart this program to check again");
      			  statusMessage = "Opening Snap! with no robot blocks loaded...";
      			  if(chckbxOpenSnapLocally.isSelected()) {
    				  urlToOpen = urlToOpenLocal + "#open:http://localhost:22179/SayThisBlock.xml";
    			  }
    			  else {
    				  urlToOpen = urlToOpenRemote + "SnapStart";
    			  }
      		  }
	          if(!finchConnected  || hummingbirdConnected)         		  
	          {
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
                // Set the status message to something like "Opening Snap! with x blocks loaded"
                lblHelperText.setText(statusMessage);
                }
            catch ( IOException e )
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
                            lblHelperText.setText(statusMessage);
	                    }
	                    catch(Exception e1){
	                    	lblHelperText.setText("Error opening browser.");
	                    	e1.printStackTrace();
	                    }
	                }
	            	else{
	            		lblHelperText.setText("Error opening browser.");
	            	}
                }
			}
			}
		});
		btnOpenSnap.setContentAreaFilled(false);
		btnOpenSnap.setBorderPainted(false);
		btnOpenSnap.setBounds(100, 218, 233, 49);
		btnOpenSnap.setToolTipText("Opens Snap! in Google Chrome or your default browser");
		btnOpenSnap.setSelectedIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/OpenButton2Clicked.png")));
		btnOpenSnap.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/OpenSnap.png")));
		frmBirdbrainRobotServer.getContentPane().add(btnOpenSnap);
		
		lblHelperText = new JLabel("");
		lblHelperText.setForeground(Color.WHITE);
		lblHelperText.setBackground(Color.DARK_GRAY);
		lblHelperText.setVerticalAlignment(SwingConstants.TOP);
		lblHelperText.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblHelperText.setBounds(10, 360, 414, 80);
		frmBirdbrainRobotServer.getContentPane().add(lblHelperText);
		
		chckbxOpenSnapLocally = new JCheckBox("Open Snap! locally (no cloud storage)");
		chckbxOpenSnapLocally.setFocusable(false);
		chckbxOpenSnapLocally.setFont(new Font("Tahoma", Font.PLAIN, 14));
		chckbxOpenSnapLocally.setForeground(Color.WHITE);
		chckbxOpenSnapLocally.setBackground(Color.DARK_GRAY);
		chckbxOpenSnapLocally.setBounds(110, 274, 272, 23);
		frmBirdbrainRobotServer.getContentPane().add(chckbxOpenSnapLocally);
		if(SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX) { //hide Scratch button on Linux
			btnOpenScratch = new JButton("");
			btnOpenScratch.setFocusable(false);
			btnOpenScratch.setRolloverIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/OpenScratchButtonClicked.png")));
			btnOpenScratch.setDefaultCapable(false);
			btnOpenScratch.setBorder(null);
			btnOpenScratch.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg){
					try{
						if(SystemUtils.IS_OS_WINDOWS){
		              	  final String arch = System.getProperty("sun.arch.data.model","");
		              	  File scratch= new File(System.getenv("PROGRAMFILES")+"/Scratch 2/Scratch 2.exe"); 
		              	  if("64".equals(arch) && !scratch.exists())
		              		  scratch = new File(System.getenv("PROGRAMFILES(X86)")+"/Scratch 2/Scratch 2.exe");
		              	  if(scratch.exists()){
		              		  
		              		  if(finchConnected && hummingbirdConnected) {
		              			String[] scratchPath = {"cmd","/c","start","ScratchStarters/FinchHummingbirdStart.sb2"};
		              			Runtime.getRuntime().exec(scratchPath);
		              		  }
		              		  else if(finchConnected)
		              		  {
		              			String[] scratchPath = {"cmd","/c","start","ScratchStarters/FinchStart.sb2"};
		              			Runtime.getRuntime().exec(scratchPath);
		              		  }
		              		  else if(hummingbirdConnected)
		              		  {
		              			String[] scratchPath = {"cmd","/c","start","ScratchStarters/HummingbirdStart.sb2"}; 
		              			Runtime.getRuntime().exec(scratchPath);
		              		  }
		              		  else 
		              		  {
		              			String[] scratchPath = {"cmd","/c","start","Scratch 2","/D",scratch.getParentFile().getPath(),"Scratch 2"};
		              			Runtime.getRuntime().exec(scratchPath);
		              		  }
		              		  
		              	  }
		              	  else {
		              		  throw new IOException("Cannot find Scratch on Windows. Is it installed?");
		              	  }
		                }
		                else if(SystemUtils.IS_OS_MAC_OSX) {
		                    File scratch = new File("/Applications/Scratch 2.app");
		                    String path = new File("").getAbsolutePath();
		                    if(scratch.exists()) {
		                    	 if(finchConnected && hummingbirdConnected) {
		                    		path = path + "/ScratchStarters/FinchHummingbirdStart.sb2";
		                    		String[] scratchPath = {"/usr/bin/open","-a","/Applications/Scratch 2.app", "--args", path};
			              			Runtime.getRuntime().exec(scratchPath);
			              		  }
			              		  else if(finchConnected)
			              		  {
			              			path = path + "/ScratchStarters/FinchStart.sb2";
		                    		String[] scratchPath = {"/usr/bin/open","-a","/Applications/Scratch 2.app", "--args", path};
				              		Runtime.getRuntime().exec(scratchPath);
			              		  }
			              		  else if(hummingbirdConnected)
			              		  {
			              			path = path + "/ScratchStarters/HummingbirdStart.sb2";
		                    		String[] scratchPath = {"/usr/bin/open","-a","/Applications/Scratch 2.app", "--args", path};
				              		Runtime.getRuntime().exec(scratchPath);
			              		  }
			              		  else 
			              		  {
			              			String[] scratchPath = {"/usr/bin/open","-a","/Applications/Scratch 2.app"};
			                        Runtime.getRuntime().exec(scratchPath);
			              		  }
		                    	
		                    }
		                    else {
		                        //Error message if Scratch not found
		                        throw new IOException("Cannot find Scratch on Mac. Is it installed?");
		                    }
		                }
						lblHelperText.setText("Opening Scratch 2.0...");
					}
					catch(IOException e){
						lblHelperText.setText(e.getMessage());
                        System.out.println( e.getMessage() );
					}
				}
			});
			btnOpenScratch.setContentAreaFilled(false);
			btnOpenScratch.setBorderPainted(false);
			btnOpenScratch.setBounds(100, 304, 233, 49);
			btnOpenScratch.setToolTipText("Opens Scratch 2.0 offline");
			btnOpenScratch.setSelectedIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/OpenScratchButtonClicked.png")));
			btnOpenScratch.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/OpenScratchButton.png")));
			frmBirdbrainRobotServer.getContentPane().add(btnOpenScratch);
		}
		frmBirdbrainRobotServer.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{btnOpenSnap}));
	}
	
	public class CheckConnections implements Runnable {
	//Class that dynamically checks if Finch and Hummingbird are connected
		//Used to make sure the image isn't updated every iteration of the loop
		private boolean isFinch = false;
		private boolean isHumm = false;
		private boolean arduino = false;
		private boolean toRun = true;
		public void stop() {
			toRun = false;
		}
		
		public void run() {

			ports = SerialPortList.getPortNames();  
			while(toRun) {
				finchConnected = finch.connect();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				hummingbirdConnected = hummingbird.connect();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(finch.getConnected() && isFinch == false) {
					finchServlet.setConnectionState(true);
					resetServlet.setFinchConnectionState(true);
					pollServlet.setFinchConnectionState(true);
					lblFinchpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/FinchConnected.png")));
					isFinch = true;
				}
				else if (!finch.getConnected() && isFinch == true) {
					finchServlet.setConnectionState(false);
					resetServlet.setFinchConnectionState(false);
					pollServlet.setFinchConnectionState(false);
					pollServlet.setFinchProblemReport(true);
					lblFinchpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/FinchNotConnected.png")));
					isFinch = false;
				}
			    if (hummingbird.getConnected() && isHumm == false) {
			    	hummingbirdServlet.setConnectionState(true);
					resetServlet.setHummingbirdConnectionState(true);
					pollServlet.setHummingbirdConnectionState(true);
					if(hummingbird.isDuo()) {
						lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdDuoConnected.png")));
					}
					else if(hummingbird.isDuo() == false) {
						lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdV1Connected.png")));	
					}
					isHumm = true;
				}
				else if (!hummingbird.getConnected()) {
					if(isHumm == true)
					{
						hummingbirdServlet.setConnectionState(false);
						resetServlet.setHummingbirdConnectionState(false);
						pollServlet.setHummingbirdConnectionState(false);
						pollServlet.setHummingbirdProblemReport(true);
						lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdNotConnectedDuo.png")));
						isHumm = false;
					}
					
					try{
						  arduino = deviceFound((short) 0x2354, (short) 0x2333,UsbHostManager.getUsbServices().getRootUsbHub())
								         || deviceFound((short) 0x2341, (short) 0x8036,UsbHostManager.getUsbServices().getRootUsbHub());
						  String[] newPorts;
						  
						  if(arduino){
							  lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdArduinoMode.png")));
						  }else if(!(Arrays.equals(ports, SerialPortList.getPortNames()))){//test for different set of serial ports
							  ports = SerialPortList.getPortNames();
							  lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdNotConnectedDuo.png")));
							  boolean found = false;
		                      for(int i = 0;i<5;i++){ //check for bootloader presence a few times since it sometimes takes a second to show up
		                          if(deviceFound((short)0x2341,(short)0x0036,UsbHostManager.getUsbServices().getRootUsbHub())) { //Arduino Leonardo bootloader VID & PID - backwards compatibility with beta units
		                              found = true;
		                              break;
		                          }
		                          else if(deviceFound((short)0x2354,(short)0x2444,UsbHostManager.getUsbServices().getRootUsbHub())) { //Hummingbird Duo bootloader VID & PID
		                              found = true;
		                              System.out.println("Found the duo");
		                              break;
		                          }
		                          Thread.sleep(1000);
		                      }
		                      newPorts = SerialPortList.getPortNames(); //refresh list of serial ports
		                      if(newPorts.length >= ports.length && found) { //check for new serial port or different serial port
		                          String comport = "";
		                          if(ports.length==0){ //different lists of ports and first list is empty
		                              if(newPorts.length > 0) //prevent any weird array out of bounds errors that might show up
		                                  comport = newPorts[0]; //new serial port must be bootloader
		                          }
		                          else{
		                              for(String newPort : newPorts){
		                            	  System.out.println("New port " + newPort);
		                            	  for(int i = 0; i < ports.length; i++)
		                            	  {
		                            		  System.out.println("Old port " + i + " " + ports[i]);
		                            	  }
		                                  if (!Arrays.asList(ports).contains(newPort)) { //find changed or new Serial port
		                                      comport = newPort; //bootloader serial port found
		                                      break;
		                                  }
		                              }
		                          }
		                          String firmwareFile = "";
		                          try {
		                              URL url = new URL("http://www.hummingbirdkit.com/sites/default/files/HummingbirdV2.hex");
		                              File file = new File("HummingbirdV2.hex");
		                              FileUtils.copyURLToFile(url,file,2000,2000);
		                              firmwareFile = file.getPath();
		                          }catch(Exception e){
		                              System.err.println("Error downloading Hummingbird firmware. Trying offline version.");
		                              firmwareFile = "HummingbirdV2.hex";
		                          }
		                          System.out.println("Comport is " + comport + " And firmware is " + firmwareFile);
		                           if(!comport.equals("") && !firmwareFile.equals("")) {
		                                lblHelperText.setText("Reset Detected. Trying to revert to Hummingbird mode.");
		                                Process p;
		                                String error = "";
		                                try {
		                                    String avrdude = "avrdude";
		                                    String avrconf ="avrdude.conf";
		                                    if(SystemUtils.IS_OS_LINUX){
		                                        final String arch = System.getProperty("sun.arch.data.model","");
		                                        if(arch.equals("64"))
		                                            avrdude = "./avrdude64";
		                                        else
		                                            avrdude = "./avrdude";
		                                    }
		                                    else if(SystemUtils.IS_OS_MAC_OSX){
		                                        avrdude = "./avrdude_mac";
		                                        avrconf = "./avrdude_mac.conf";
		                                    }
		                                    String[] command={avrdude, "-p", "atmega32u4", "-P", comport, "-c", "avr109", "-C", avrconf, "-b", "9600", "-U", "flash:w:" + firmwareFile+":i"};
		                                    //run avrdude
		                                    p = Runtime.getRuntime().exec(command);
		                                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		                                    String currentLine;
		                                    while ((currentLine = reader.readLine()) != null) { //Blocking read from avrdude error stream
		                                        error +=currentLine+"\n";
		                                    }
		                                } catch (Exception e) { //problem running avrdude
		                                    System.err.println("Error running avrdude");
		                                    e.printStackTrace();
		                                }
		                                
		                                if(error.indexOf("done.  Thank you.")==-1){ //Check for lack of success message
		                                    System.err.println(error);
		                                    JOptionPane.showMessageDialog(null,"Error uploading firmware. Please try again."+error);
		                                }
		                                lblHelperText.setText("");
		                            }
		                      }
						  }else{
							  lblHelperText.setText("");
						  }
					  }catch (UsbException usbEx) {
		                 //   lblHelperText.setText("Status: Error with USB connection");
		                    usbEx.printStackTrace();
		                } catch (InterruptedException iEx) {
		                    iEx.printStackTrace();
		                }
				
				}
				
			}
			
		}
		
	}
	
	private void startServer() {
		  // Start a new server on port 22179
		  server = new Server(22179);

		  // Create a new WebAppContext
		  WebAppContext context = new WebAppContext();
		  context.setContextPath("/"); // Set the context path to localhost:22179/
		  // Use config from our web.xml - this is critical as it allows us to enable cross-domain filtering so that snap.berkeley.edu can talk with our server
		  // Make sure to check out web.xml to see how to enable filtering
		  context.setDescriptor("WebContent/WEB-INF/web.xml"); 
		  context.setResourceBase("WebContent/"); // WebContent contains our blocks.xml files

		  context.setParentLoaderPriority(true); // This is required but I don't know why. Thanks internet for examples without explanation
		  
		  // Use the context as our sole handler for the server
		  server.setHandler(context);
		  
		  finch = new FinchServletWrapper();
		  hummingbird = new HummingbirdServletWrapper();
		  
		  finchConnected = finch.connect(); 
		  hummingbirdConnected = hummingbird.connect();
		  finchServlet = new FinchServlet(finch);
		  
		  // If our Finch connected, pass the finch wrapper object to the servlet and update the GUI
		  if(finchConnected) {
			//  finchServlet = new FinchServlet(finch);
			  finchServlet.setConnectionState(true);
			  lblFinchpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/FinchConnected.png")));
		  }
		  // Else don't pass the object, so this servlet just prints "Finch not connected"
		  else {
			  finchServlet.setConnectionState(false);
			 // finchServlet = new FinchServlet();	  
		  }

		  hummingbirdServlet = new HummingbirdServlet(hummingbird);
		  // Ditto for Hummingbird
		  if(hummingbirdConnected) {
			  hummingbirdServlet.setConnectionState(true);
			  if(hummingbird.isDuo()) {
					lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdDuoConnected.png")));
				}
				else if(hummingbird.isDuo() == false) {
					lblHummingbirdpic.setIcon(new ImageIcon(BirdBrainRobotServer.class.getResource("/HummingbirdV1Connected.png")));	
				}
		  }
		  else {
			  hummingbirdServlet.setConnectionState(false);
			  //hummingbirdServlet = new HummingbirdServlet();	  
		  }
		  // Check if Snap! website is available
		  try {
			  URL url = new URL(urlToOpenRemote);
			  URLConnection conn = url.openConnection();  
			  conn.setConnectTimeout(3000);  
			  conn.setReadTimeout(3000);  
			  conn.getInputStream();
		  }
		  catch(Exception e) {
			  // If there's no connection, check the local checkbox and then disable it
			  chckbxOpenSnapLocally.setSelected(true);	
			  chckbxOpenSnapLocally.setEnabled(false);
		  }
		  
		  // Create Reset and Poll servlets as required by Scratch 2.0
		  resetServlet = new ResetServlet(finch, hummingbird);
		  resetServlet.setFinchConnectionState(finchConnected);
		  resetServlet.setHummingbirdConnectionState(hummingbirdConnected);
		  
		  pollServlet = new PollServlet(finch, hummingbird);
		  pollServlet.setFinchConnectionState(finchConnected);
		  pollServlet.setHummingbirdConnectionState(hummingbirdConnected);
		  
		  pollServlet.setFinchProblemReport(finchConnected);
		  pollServlet.setHummingbirdProblemReport(hummingbirdConnected);
		  
		  //Class and thread that handle dynamic checking
		  connector = new CheckConnections(); 
		  Thread checker = new Thread(connector);
		  checker.start();
		  try {
			Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		  
		  // Add Finch, Hummingbird, and Speech servlets
		  context.addServlet(new ServletHolder(finchServlet), "/finch/*");
		  context.addServlet(new ServletHolder(hummingbirdServlet), "/hummingbird/*");
		  context.addServlet(new ServletHolder(new TextToSpeechServlet()), "/speak/*");
		  context.addServlet(new ServletHolder(resetServlet), "/reset_all");
		  context.addServlet(new ServletHolder(pollServlet), "/poll");
		  
		  try {
		   server.start(); // Finally, start out server
		   // No need to server.join - we shut the server down when the window closes and no servlet can shut it down.
		  } catch (Exception e) {
		   e.printStackTrace();
		  }

	}
	
	public boolean deviceFound(short vid, short pid, UsbHub hub) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) { //iterate through all USB devices
            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor();
            
            if ((descriptor.idVendor() == vid && descriptor.idProduct() == pid) || //matching device VID & PID
                    (device.isUsbHub() && deviceFound(vid, pid,(UsbHub) device))) //if device is hub, search devices in hub
                return true;
        }
        return false; //return false if no devices found
    }
}
