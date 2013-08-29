package birdbrain.finchandHummingbirdServer;

import edu.cmu.ri.createlab.terk.robot.finch.DefaultFinchController;
import edu.cmu.ri.createlab.terk.robot.finch.Finch;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;

/** Finch servlet wrapper class, for use with Finch servers */
public class FinchServletWrapper {

	private Finch finch = null; // Container of standard Finch object
	private boolean isConnected = false; // Flag that is true if a finch is discovered
	// Variables for sensors
	private double[] accelerations; 
	private Double temperature;
	private boolean[] obstacles;
	private int[] lights;
	
	// We poll sensors in a separate thread to minimize the timer doGet has to wait
	private Thread sensorLoop;
	
	/* Get sensor data in a loop that runs at ~16 Hz */
	private class SensorLoop implements Runnable {
		public void run() {
    
			while(isConnected) {
				try {
					if(finch != null) 
					{
						try {
							// Each finch.get takes 8 ms, then sleep to allow other things to happen
							accelerations = finch.getAccelerations();
							temperature = finch.getTemperature();
							obstacles = finch.getObstacleSensors();
							lights = finch.getLightSensors();
							Thread.sleep(32);
						}
						catch(NullPointerException ex) {
							accelerations = null;
							temperature = null;
							obstacles = null;
							lights = null;
						}
					}
				}
				catch (InterruptedException ex) {
			        Thread.currentThread().interrupt(); // very important - causes the second interrupt to be thrown
			        break;
			     }
				
			}

		}
	}
	
	public FinchServletWrapper()
	{
			
	}
	
	public boolean connect()
	{
		FinchController finchChecker; // Checks if a Finch is present
		if(!isConnected) {
            finchChecker = DefaultFinchController.create(); // Try to connect to Finch without blocking
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
            }
            
            if(finchChecker != null) {
                finchChecker.disconnect(); // If you connected, there's a Finch; now disconnect
                isConnected = true;
                finch = new Finch(); // Now connect again with a real Finch object
                sensorLoop = new Thread(new SensorLoop()); // Start reading sensors
                sensorLoop.start();
                return true;
            }
		}
		return false;
	}
	
	public boolean disConnect()
	{
		if(!isConnected) {
			return false;
		}
		sensorLoop.interrupt(); // Kill sensor loop
		isConnected = false;
		// Wait for the sensor loop to die, 100ms should do it
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
		finch.quit(); // Quit the Finch
		//finch = null;
		return true;
	}
		
	//  The following functions just get the sensors - they don't call Finch directly, they just get the last sensor value found in the sensor loop
	public int[] getLightSensors() {
		if(!isConnected) {
			return null;
		}
		else {
			return lights;
		}
		
	}
	
	public Double getTemperature() {
		if(!isConnected) {
			return null;
		}	
		return temperature;
	}
	
	public double[] getAcceleration() {
		if(!isConnected) {
			return null;
		}
		else {
			return accelerations;
		}
	}
	
	public boolean[] getObstacle() {
		if(!isConnected) {
			return null;
		}
		else {
			return obstacles;
		}
		
	}	
	
	// Parses the Finch output string and sets it
	public boolean setOutput(String setter)
	{
		if(!isConnected) {
			return false;
		}
		else
		{
			// Sets motor, arguments are -100 to 100
			if(setter.substring(0,5).equals("motor")) {
				int secondIndexofSlash = setter.lastIndexOf('/');
				int leftSpeed;
				int rightSpeed;
				
				try {
					leftSpeed = (int)(Double.parseDouble(setter.substring(6,secondIndexofSlash))*2.55);
					rightSpeed = (int)(Double.parseDouble(setter.substring(secondIndexofSlash+1))*2.55);
				}
				// You've just sent a non-number, so the "set" did not work 
				catch(NumberFormatException e) {
					return false;
				}
				
				if(leftSpeed > 255)
					leftSpeed = 255;
				if(leftSpeed < -255)
					leftSpeed = -255;
				
				if(rightSpeed > 255)
					rightSpeed = 255;
				if(rightSpeed < -255)
					rightSpeed = -255;				
				
				finch.setWheelVelocities(leftSpeed, rightSpeed);
				return true;
			}
			// Sets buzzer
			else if(setter.substring(0,6).equals("buzzer")) {
				int secondIndexofSlash = setter.lastIndexOf('/');
				
				int frequency;
				int duration;
				
				try {
					frequency = (int)(Double.parseDouble(setter.substring(7,secondIndexofSlash)));
					duration = (int)(Double.parseDouble(setter.substring(secondIndexofSlash+1)))-10; // to prevent collisions 
				}
				// You've just sent a non-number, so the "set" did not work 
				catch(NumberFormatException e) {
					return false;
				}				
				
				if(frequency < 20)
					frequency = 20;
				if(frequency > 20000)
					frequency = 20000;
				
				if(duration < 10)
					duration = 10;
				
				finch.buzz(frequency, duration);
				return true;
			}
			// Sets LED, color intensity is 0 to 100 for R, G, and B
			else if(setter.substring(0,3).equals("led")) {
				int secondIndexofSlash = setter.indexOf('/', 4);
				int thirdIndexofSlash = setter.lastIndexOf('/');
				int redLED = 0;
				int greenLED = 0;
				int blueLED = 0;
				
				try {
					redLED = (int)(Double.parseDouble(setter.substring(4,secondIndexofSlash))*2.55);
					greenLED = (int)(Double.parseDouble(setter.substring(secondIndexofSlash+1,thirdIndexofSlash))*2.55);
					blueLED = (int)(Double.parseDouble(setter.substring(thirdIndexofSlash+1))*2.55);
				}
				// You've just sent a non-number, so the "set" did not work 
				catch(NumberFormatException e) {
					return false;
				}
				
				if(redLED > 100)
					redLED = 100;
				if(redLED < 0)
					redLED = 0;
				
				if(greenLED > 100)
					greenLED = 100;
				if(greenLED < 0)
					greenLED = 0;
				
				if(blueLED > 100)
					blueLED = 100;
				if(blueLED < 0)
					blueLED = 0;
			
				finch.setLED(redLED, greenLED, blueLED);
				
				return true;
			}
			return false;
		}
		
	}
	
}
