package birdbrain.finchandHummingbirdServer;

import com.birdbraintechnologies.Finch;

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
	public boolean getConnected() {
		return isConnected;
	}
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
							Thread.sleep(12);
							temperature = finch.getTemperature();
							Thread.sleep(12);
							obstacles = finch.getObstacleSensors();
							Thread.sleep(12);
							lights = finch.getLightSensors();
							Thread.sleep(12); 
							
						}
						catch(NullPointerException ex) {
							accelerations = null;
							temperature = null;
							obstacles = null;
							lights = null;
							isConnected = false;
						}
						if (accelerations == null || obstacles == null || lights == null) {
							isConnected = false;
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
		//FinchController finchChecker; // Checks if a Finch is present
		if(finch == null || !finch.isConnected()) {
            finch = new Finch(); // Try to connect to Finch without blocking
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
            }
            
            if(finch.isConnected()) {
            	finch.stopWheels();
            	finch.setLED(0,0,0);
                isConnected = true;
                sensorLoop = new Thread(new SensorLoop()); // Start reading sensors
                sensorLoop.start();
                return true;
            }
		}
		else if(finch.isConnected()) {
			return true;
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
            Thread.sleep(500);
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
				int lastSlash = setter.lastIndexOf('/');
				int secondToLastSlash = setter.lastIndexOf('/', lastSlash-1);
				int leftSpeed;
				int rightSpeed;
				
				try {
					leftSpeed = (int)(Double.parseDouble(setter.substring(secondToLastSlash+1,lastSlash))*2.55);
					rightSpeed = (int)(Double.parseDouble(setter.substring(lastSlash+1))*2.55);
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
				int lastSlash = setter.lastIndexOf('/');
				int secondToLastSlash = setter.lastIndexOf('/', lastSlash-1);
				
				int frequency;
				int duration;
				
				try {
					frequency = (int)(Double.parseDouble(setter.substring(secondToLastSlash+1,lastSlash)));
					duration = (int)(Double.parseDouble(setter.substring(lastSlash+1)))-10; // to prevent collisions 
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
				int [] slashes = new int[3];
				slashes[2] = setter.lastIndexOf('/');
				slashes[1] = setter.lastIndexOf('/', slashes[2]-1);
				slashes[0] = setter.lastIndexOf('/', slashes[1]-1);
				int redLED = 0;
				int greenLED = 0;
				int blueLED = 0;
				
				try {
					redLED = (int)(Double.parseDouble(setter.substring(slashes[0]+1,slashes[1]))*2.55);
					greenLED = (int)(Double.parseDouble(setter.substring(slashes[1]+1,slashes[2]))*2.55);
					blueLED = (int)(Double.parseDouble(setter.substring(slashes[2]+1))*2.55);
				}
				// You've just sent a non-number, so the "set" did not work 
				catch(NumberFormatException e) {
					return false;
				}
				
				if(redLED > 255)
					redLED = 255;
				if(redLED < 0)
					redLED = 0;
				
				if(greenLED > 255)
					greenLED = 255;
				if(greenLED < 0)
					greenLED = 0;
				
				if(blueLED > 255)
					blueLED = 255;
				if(blueLED < 0)
					blueLED = 0;
			
				finch.setLED(redLED, greenLED, blueLED);
				
				return true;
			}
			return false;
		}
		
	}
	
}
