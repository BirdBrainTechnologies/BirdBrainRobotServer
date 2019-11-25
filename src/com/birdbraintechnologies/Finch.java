
package com.birdbraintechnologies;

import org.hid4java.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import edu.cmu.ri.createlab.speech.Mouth;
import edu.cmu.ri.createlab.util.FileUtils;
import edu.cmu.ri.createlab.audio.AudioHelper;


/**
 * Created by Tom on 10/27/2019.
 */
public class Finch {

    private static final Integer VENDOR_ID = 0x2354;  // Finch VID
    private static final Integer PRODUCT_ID = 0x1111; // Finch PID
    private static final int PACKET_LENGTH = 8;       // Finch USB packets are 8 bytes
    public static final String SERIAL_NUMBER = null;  // No serial number
    private static final int LED_MAX = 255;           // For bounds checking
    private static final int LED_MIN = 0;
    private static final int MOTOR_MAX = 255;
    private static final int MOTOR_MIN = -255;

    private HidDevice HIDFinch;                      // Object that opens and communicates with the Finch
    private HidServices hidServices;
    private int reportCounter = 0;                   // Tracks the sensor reports to make sure we're reading the correct report



    public Finch()
    {
        // Configure to use custom specification
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        
        // The following four settings work, but it's not clear we need them
        hidServicesSpecification.setAutoShutdown(true);
       // hidServicesSpecification.setScanInterval(500);   
       // hidServicesSpecification.setPauseInterval(5000);
        hidServicesSpecification.setScanMode(ScanMode.NO_SCAN);//SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);

        // Get HID services using custom specification
        hidServices = HidManager.getHidServices(hidServicesSpecification);
        Connect();
    }
    
    // Returns true if a Finch is connected, false otherwise
    public boolean isConnected()
    {
    	if(HIDFinch != null)
    		return true;
    	else
    		return false;
    }
    
    // Returns true if a Finch successfully connected
    private boolean Connect()
    {
    	// Only connect if you aren't currently connected
    	if(!isConnected()) {
    		HIDFinch = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
    		// If hidServices returned an HIDFinch it means one is attached to the computer, so open it
	        if(HIDFinch != null) {
	            System.out.println("Connecting Finch...");
	            HIDFinch.open();
	            return true;
	        }
	        else {
	            System.out.println("No Finch detected");
	            return false;
	        }
    	}
    	else {
    		return true; // because if isConnected is true, it means that a Finch is already connected
    	}
    }

    /**
     * Sets the color of the LED in the Finch's beak using a Color object.
     *
     * @param     color is a Color object that determines the beaks color
     */
    public void setLED(final Color color)
    {
        if (color != null)
        {
            setLED(color.getRed(), color.getGreen(), color.getBlue());
        }
        else
        {
            System.out.println("Color object was null, LED could not be set");
        }
    }

    /**
     * Sets the color of the LED in the Finch's beak.  The LED can be any color that can be
     * created by mixing red, green, and blue; turning on all three colors in equal amounts results
     * in white light.  Valid ranges for the red, green, and blue elements are 0 to 255.
     *
     * @param     red sets the intensity of the red element of the LED
     * @param     green sets the intensity of the green element of the LED
     * @param     blue sets the intensity of the blue element of the LED
     */

    public void setLED(final int red, final int green, final int blue)
    {
        boolean inRange = true;
        if (red > LED_MAX)
        {
            inRange = false;
            System.out.println("Red value exceeds appropriate values (0-255), LED will not be set");
        }
        if (red < LED_MIN)
        {
            inRange = false;
            System.out.println("Red value is negative, LED will not be set");
        }

        if (green > LED_MAX)
        {
            inRange = false;
            System.out.println("Green value exceeds appropriate values (0-255), LED will not be set");
        }
        if (green < LED_MIN)
        {
            inRange = false;
            System.out.println("Green value is negative, LED will not be set");
        }

        if (blue > LED_MAX)
        {
            inRange = false;
            System.out.println("Blue value exceeds appropriate values (0-255), LED will not be set");
        }
        if (blue < LED_MIN)
        {
            inRange = false;
            System.out.println("Blue value is negative, LED will not be set");
        }

        // Only set if all three colors are 0 to 255
        if (inRange)
        {
            // Send the LED message
            byte[] message = new byte[PACKET_LENGTH];
            message[0] = 'O'; // USB: Payload 8 bytes, LED only uses four of them
            message[1] = (byte) red;
            message[2] = (byte) green;
            message[3] = (byte) blue;
            writeFinch(message);
        }
    }

    /**
     * Sets the color of the LED in the Finch's beak using a Color object for the length of time specified by duration.
     *
     * @param     color is a Color object that determines the beaks color
     * @param     duration is the length of time the color will display on the beak
     */
    public void setLED(final Color color, final int duration)
    {
        if (color != null)
        {
            setLED(color.getRed(), color.getGreen(), color.getBlue(), duration);
        }
        else
        {
            System.out.println("Color object was null, LED could not be set");
        }
    }

    /**
     * Sets the color of the LED in the Finch's beak for the length of time specified by duration.
     * The LED can be any color that can be created by mixing red, green, and blue; turning on all three colors in equal amounts results
     * in white light.  Valid ranges for the red, green, and blue elements are 0 to 255.
     *
     * @param     red sets the intensity of the red element of the LED
     * @param     green sets the intensity of the green element of the LED
     * @param     blue sets the intensity of the blue element of the LED
     * @param     duration is the length of time the color will display on the beak
     */

    public void setLED(final int red, final int green, final int blue, final int duration)
    {
        setLED(red, green, blue);
        sleep(duration);
        setLED(0, 0, 0);
    }

    /**
     * Stops both wheels.
     */
    public void stopWheels()
    {
        setWheelVelocities(0, 0);
    }

    /**
     * This method simultaneously sets the velocities of both wheels. Current valid values range from
     * -255 to 255; negative values cause a wheel to move backwards.
     *
     * @param leftVelocity The velocity at which to move the left wheel
     * @param rightVelocity The velocity at which to move the right wheel
     */
    public void setWheelVelocities(final int leftVelocity, final int rightVelocity)
    {
    	// -1 means no duration
        setWheelVelocities(leftVelocity, rightVelocity, -1);
    }

    /**
     * This method simultaneously sets the velocities of both wheels. Current valid values range from
     * -255 to 255.  If <code>timeToHold</code> is positive, this method blocks further program execution for the amount
     * of time specified by timeToHold, and then stops the wheels once time has elapsed.
     *
     * @param leftVelocity The velocity in native units at which to move the left wheel
     * @param rightVelocity The velocity in native units at which to move the right wheel
     * @param timeToHold The amount of time in milliseconds to hold the velocity for; if 0 or negative, program
     *                   execution is not blocked and the wheels are not stopped.
     */
    public void setWheelVelocities(final int leftVelocity, final int rightVelocity, final int timeToHold)
    {
        if (leftVelocity <= MOTOR_MAX &&
                leftVelocity >= MOTOR_MIN &&
                rightVelocity <= MOTOR_MAX &&
                rightVelocity >= MOTOR_MIN)
        {
            // Send the Motor message
            byte[] message = new byte[PACKET_LENGTH];
            message[0] = 'M'; // USB: Payload 8 bytes
            if(leftVelocity >= 0) {
                message[1] = 0;
                message[2] = (byte) leftVelocity;
            }
            else {
                message[1] = 1;
                message[2] = (byte)(leftVelocity * -1);
            }
            if(rightVelocity >= 0) {
                message[3] = 0;
                message[4] = (byte) rightVelocity;
            }
            else {
                message[3] = 1;
                message[4] = (byte)(rightVelocity * -1);
            }
            writeFinch(message);
            if (timeToHold > 0)
            {
                sleep(timeToHold);
                stopWheels();
            }
        }
        else
        {
            System.out.println("Velocity values out of range");
        }
    }

    /**
     * This method uses Thread.sleep to cause the currently running program to sleep for the
     * specified number of seconds.
     *
     * @param ms - the number of milliseconds to sleep for.  Valid values are all positive integers.
     */
    public void sleep(final int ms)
    {
        if (ms < 0)
        {
            System.out.println("Program sent a negative time to sleep for");
        }
        else
        {
            try
            {
                Thread.sleep(ms);
            }
            catch (InterruptedException ignored)
            {
                System.out.println("Error:  sleep was interrupted for some reason");
            }
        }
    }

    /**
     * This method returns the current X-axis acceleration value experienced by the robot.  Values for acceleration
     * range from -1.5 to +1.5g.  The X-axis is the beak-tail axis.
     *
     * @return The X-axis acceleration value
     */
      
    public double getXAcceleration()
    {
        double [] accels = getAccelerations();
        if (accels != null)
        {
            return accels[0];
        }
        System.out.println("Accelerometer not responding, check Finch connection");
        return 0.0;
    }

    /**
     * This method returns the current Y-axis acceleration value experienced by the robot.  Values for acceleration
     * range from -1.5 to +1.5g.  The Y-axis is the wheel-to-wheel axis.
     *
     * @return The Y-axis acceleration value
     */
      
    public double getYAcceleration()
    {
        double [] accels = getAccelerations();
        if (accels != null)
        {
            return accels[1];
        }
        System.out.println("Accelerometer not responding, check Finch connection");
        return 0.0;
    }

    /**
     * This method returns the current Z-axis acceleration value experienced by the robot.  Values for acceleration
     * range from -1.5 to +1.5g.  The Z-axis runs perpendicular to the Finch's circuit board.
     *
     * @return The Z-axis acceleration value
     */
     
    public double getZAcceleration()
    {
        double [] accels = getAccelerations();
        if (accels != null)
        {
            return accels[2];
        }
        System.out.println("Accelerometer not responding, check Finch connection");
        return 0.0;
    }

    /**
     * Use this method to simultaneously return the current X, Y, and Z accelerations experienced by the robot.
     * Values for acceleration can be in the range of -1.5g to +1.5g.  When the robot is on a flat surface,
     * X and Y should be close to 0g, and Z should be near +1.0g.
     *
     * @return a an array of 3 doubles containing the X, Y, and Z acceleration values
     */
      
    public double[] getAccelerations()
    {
        byte [] command = new byte [PACKET_LENGTH];
        command[0] = 'A';
        byte[] rawAccelerometers = readFinch(command);
        if (rawAccelerometers != null)
        {
            final double[] accelerations = new double[3];
            accelerations[0] = rawToGs(rawAccelerometers[1]);
            accelerations[1] = rawToGs(rawAccelerometers[2]);
            accelerations[2] = rawToGs(rawAccelerometers[3]);
            return accelerations;
        }
        System.out.println("Accelerometer not responding, check Finch connection");
        return null;
    }

    /**
     * Function to convert raw accelerometer value to g force
     */
    private double rawToGs(byte val) {
        double accel;

        if(val < 0x20) {
            accel = (double) (val & 0xFF) * 1.5 / 32;
        }
        else {
            accel = ((double)(val & 0xFF) - 64) * 1.5 / 32;
        }
            return accel;
        }

    /**
     * This method returns true if the beak is up (Finch sitting on its tail), false otherwise
     *
     * @return true if beak is pointed at ceiling
     */
    //  
    public boolean isBeakUp()
    {
        final double[] accels = getAccelerations();
        if (accels != null)
        {
            if (accels[0] < -0.7 && accels[0] > -1.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > -0.5 && accels[2] < 0.5)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns true if the beak is pointed at the floor, false otherwise
     *
     * @return true if beak is pointed at the floor
     */
    public boolean isBeakDown()
    {
        final double[] accels = getAccelerations();
        if (accels != null)
        {
            if (accels[0] < 1.5 && accels[0] > 0.7 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > -0.5 && accels[2] < 0.5)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns true if the Finch is on a flat surface
     *
     * @return true if the Finch is level
     */
    public boolean isFinchLevel()
    {
        final double[] accels = getAccelerations();
        if (accels != null)
        {
            if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > 0.65 && accels[2] < 1.5)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns true if the Finch is upside down, false otherwise
     *
     * @return true if Finch is upside down
     */
    public boolean isFinchUpsideDown()
    {
        final double[] accels = getAccelerations();
        if (accels != null)
        {
            if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > -1.5 && accels[2] < -0.65)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns true if the Finch's left wing is pointed at the ground
     *
     * @return true if Finch's left wing is down
     */
    public boolean isLeftWingDown()
    {
        final double[] accels = getAccelerations();
        if (accels != null)
        {
            if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > 0.7 && accels[1] < 1.5 && accels[2] > -0.5 && accels[2] < 0.5)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns true if the Finch's right wing is pointed at the ground
     *
     * @return true if Finch's right wing is down
     */
    public boolean isRightWingDown()
    {
        final double[] accels = getAccelerations();
        if (accels != null)
        {
            if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -1.5 && accels[1] < -0.7 && accels[2] > -0.5 && accels[2] < 0.5)
            {
                return true;
            }
        }
        return false;
    }

    /**
     *  Returns true if the Finch has been shaken since the last accelerometer read
     *
     *  @return true if the Finch was recently shaken
     */
    public boolean isShaken()
    {
        byte [] command = new byte [PACKET_LENGTH];
        command[0] = 'A';
        byte[] rawAccelerometers = readFinch(command);
        if (rawAccelerometers != null)
        {
            if((rawAccelerometers[4] & 0x80) > 0) // Bit 7 on byte 5 indicates tapped
                return true;
            else
                return false;
        }
        System.out.println("Accelerometer not responding, check Finch connection");
        return false;
    }

    /**
     *  Returns true if the Finch has been tapped since the last accelerometer read
     *
     *  @return true if the Finch was recently tapped
     */
    public boolean isTapped()
    {
        byte [] command = new byte [PACKET_LENGTH];
        command[0] = 'A';
        byte[] rawAccelerometers = readFinch(command);
        if (rawAccelerometers != null)
        {
            if((rawAccelerometers[4] & 0x20) > 0)  // Bit 5 on byte 5 indicates tapped
                return true;
            else
                return false;
        }
        System.out.println("Accelerometer not responding, check Finch connection");
        return false;
    }

    /**
     * Plays a tone over the computer speakers or headphones at a given frequency (in Hertz) for
     * a specified duration in milliseconds.  Middle C is about 262Hz.  Visit http://www.phy.mtu.edu/~suits/notefreqs.html for
     * frequencies of musical notes.
     *
     * @param frequency The frequency of the tone in Hertz
     * @param duration The time to play the tone in milliseconds
     */
      
    public void playTone(final int frequency, final int duration)
    {
        playTone(frequency, 10, duration);
    }

    /**
     * Plays a tone over the computer speakers or headphones at a given frequency (in Hertz) for
     * a specified duration in milliseconds at a specified volume.  Middle C is about 262Hz.
     * Visit http://www.phy.mtu.edu/~suits/notefreqs.html for frequencies of musical notes.
     *
     * @param frequency The frequency of the tone in Hertz
     * @param volume The volume of the tone on a 1 to 10 scale
     * @param duration The time to play the tone in milliseconds
     */
    
      
    public void playTone(final int frequency, final int volume, final int duration)
    {
    	AudioHelper.playTone(frequency, volume, duration);
    }

    /**
     * Plays a wav file over computer speakers at the specificied fileLocation path.  If you place the audio
     * file in the same path as your source, you can just specify the name of the file.
     *
     * @param     fileLocation Absolute path of the file or name of the file if located in some directory as source code
     */
    
    public void playClip(final String fileLocation)
    {
        try
        {
            final File file = new File(fileLocation);
            final byte[] rawSound = FileUtils.getFileAsBytes(file);

            AudioHelper.playClip(rawSound);
        }
        catch (IOException e)
        {
            System.out.println("Failed to play sound.");
        }
    }

    /**
     * Takes the text of 'sayThis' and synthesizes it into a sound file and plays the sound file over
     * computer speakers.  sayThis can be arbitrarily long and can include variable arguments.
     *
     * Example:
     *   myFinch.saySomething("My light sensor has a value of "+ lightSensor + " and temperature is " + tempInCelcius);
     *
     * @param     sayThis The string of text that will be spoken by the computer
     */
 
    public void saySomething(final String sayThis)
    {
        if (sayThis != null && sayThis.length() > 0)
        {
            final Mouth mouth = Mouth.getInstance();

            if (mouth != null)
            {
            	AudioHelper.playClip(mouth.getSpeech(sayThis));
            }
        }
        else
        {
            System.out.println("Given text to speak was null or empty");
        }
    }

    /**
     * Takes the text of 'sayThis' and synthesizes it into a sound file and plays the sound file over
     * computer speakers. sayThis can be arbitrarily long and can include variable arguments. The duration
     * argument allows you to delay program execution for a number of milliseconds.
     *
     * Example:
     *   myFinch.saySomething("My light sensor has a value of "+ lightSensor + " and temperature is " + tempInCelcius);
     *
     * @param     sayThis The string of text that will be spoken by the computer
     * @param     duration The time in milliseconds to halt further program execution
     */
   
    public void saySomething(final String sayThis, final int duration)
    {
        if (sayThis != null && sayThis.length() > 0)
        {
            final Mouth mouth = Mouth.getInstance();

            if (mouth != null)
            {
               mouth.getSpeech(sayThis);
               sleep(duration);
            }
        }
        else
        {
            System.out.println("Given text to speak was null or empty");
        }
    }

    /**
     * Plays a tone at the specified frequency for the specified duration on the Finch's internal buzzer.
     * Middle C is about 262Hz.
     * Visit http://www.phy.mtu.edu/~suits/notefreqs.html for frequencies of musical notes.
     * Note that this is different from playTone, which plays a tone on the computer's speakers.
     * Also note that buzz is non-blocking - so if you call two buzz methods in a row without
     * an intervening sleep, you will only hear the second buzz (it will over-write the first buzz).
     *
     * @param     frequency Frequency in Hertz of the tone to be played
     * @param     duration  Duration in milliseconds of the tone
     */
    //  
    public void buzz(final int frequency, final int duration)
    {
        // Send the buzzer message
        byte[] message = new byte[PACKET_LENGTH];
        message[0] = 'B'; // USB: Payload 8 bytes
        message[1] = (byte) (duration/256);
        message[2] = (byte) (duration%256);
        message[3] = (byte) (frequency/256);
        message[4] = (byte) (frequency%256);
        writeFinch(message);
    }

    /**
     * Plays a tone at the specified frequency for the specified duration on the Finch's internal buzzer.
     * Middle C is about 262Hz.
     * Visit http://www.phy.mtu.edu/~suits/notefreqs.html for frequencies of musical notes.
     * Note that this is different from playTone, which plays a tone on the computer's speakers.
     * Unlike the buzz method, this method will block program execution for the time specified by duration.
     *
     * @param     frequency Frequency in Hertz of the tone to be played
     * @param     duration  Duration in milliseconds of the tone
     */
    //  
    public void buzzBlocking(final int frequency, final int duration)
    {
        buzz(frequency, duration);
        sleep(duration);
    }

    /**
     * Returns the value of the left light sensor.  Valid values range from 0 to 255, with higher
     * values indicating more light is being detected by the sensor.
     *
     *
     * @return The current light level at the left light sensor
     */
    //  
    public int getLeftLightSensor()
    {
        return getLightSensor(0);
    }

    /**
     * Returns the value of the right light sensor.  Valid values range from 0 to 255, with higher
     * values indicating more light is being detected by the sensor.
     *
     *
     * @return The current light level at the right light sensor
     */
    //  
    public int getRightLightSensor()
    {
        return getLightSensor(1);
    }

    private int getLightSensor(final int id)
    {
        final int[] values = getLightSensors();
        if (values != null)
        {
            return values[id];
        }

        System.out.println("Light sensor not responding, check Finch connection");
        return 0;
    }

    /**
     * Returns a 2 integer array containing the current values of both light sensors.
     * The left sensor is the 0th array element, and the right sensor is the 1st element.
     *
     *
     * @return A 2 int array containing both light sensor readings.
     */
    //  
    public int[] getLightSensors()
    {
        byte [] command = new byte[PACKET_LENGTH];
        command[0] = 'L';
        byte[] data = readFinch(command);

        if (data == null)
        {
            System.out.println("Light sensor not responding, check Finch connection");
            return null;
        }
        else {
        	// the & 0xFF hack is make sure the integer is unsigned - without it the returned values range from -128 to 127
            int[] lightSensors = new int[2];
            lightSensors[0] = (int) (data[0] & 0xFF);
            lightSensors[1] = (int) (data[1] & 0xFF);
            return lightSensors;
        }
    }

    /**
     * Returns true if the left light sensor is greater than the value specified
     * by limit, false otherwise.
     *
     * @param limit The value the light sensor needs to exceed
     * @return whether the light sensor exceeds the value specified by limit
     */
    //  
    public boolean isLeftLightSensor(final int limit)
    {
        return (limit < getLeftLightSensor());
    }

    /**
     * Returns true if the right light sensor is greater than the value specified
     * by limit, false otherwise.
     *
     * @param limit The value the light sensor needs to exceed
     * @return true if the light sensor exceeds the value specified by limit
     */
    //  
    public boolean isRightLightSensor(final int limit)
    {
        return (limit < getRightLightSensor());
    }

    /**
     * Returns true if there is an obstruction in front of the left side of the robot.
     *
     *
     * @return Whether an obstacle exists in front of the left side of the robot.
     */
    //  
    public boolean isObstacleLeftSide()
    {
        return isObstactleDetected(0);
    }

    /**
     * Returns true if there is an obstruction in front of the right side of the robot.
     *
     *
     * @return Whether an obstacle exists in front of the right side of the robot.
     */
    //  
    public boolean isObstacleRightSide()
    {
        return isObstactleDetected(1);
    }

    private boolean isObstactleDetected(final int id)
    {
        boolean [] obstacles = getObstacleSensors();
        if(obstacles != null)
            return obstacles[id];
        return false;
    }

    /**
     * Returns true if either left or right obstacle sensor detect an obstacle.
     *
     *
     * @return Whether either obstacle sensor sees an obstacle.
     */
    //  
    public boolean isObstacle()
    {
        return isObstacleLeftSide() || isObstacleRightSide();
    }

    /**
     * Returns the value of both obstacle sensors as 2 element boolean array.
     * The left sensor is the 0th element, and the right sensor is the 1st element.
     *
     *
     * @return The values of left and right obstacle sensors in a 2 element array
     */
    //  
    public boolean[] getObstacleSensors()
    {
        byte [] command = new byte[PACKET_LENGTH];
        command[0] = 'I';
        byte[] data = readFinch(command);
        if (data == null)
        {
            System.out.println("Obstacle sensor not responding, check Finch connection");
            return null;
        }
        else {
            boolean[] obstacles = new boolean[2];
            if (data[0] > 0)
                obstacles[0] = true;
            else
                obstacles[0] = false;
            if (data[1] > 0)
                obstacles[1] = true;
            else
                obstacles[1] = false;
            return obstacles;
        }
    }

    /**
     * The current temperature reading at the temperature probe.  The value
     * returned is in Celsius.  To get Fahrenheit from Celsius, multiply the number
     * by 1.8 and then add 32.
     *
     * @return The current temperature in degrees Celsius
     */
    //  
    public double getTemperature()
    {
        byte [] command = new byte[PACKET_LENGTH];
        command[0] = 'T';
        byte[] data = readFinch(command);
        if (data == null)
        {
            System.out.println("Temperature sensor not responding, check Finch connection");
            return 0.0;
        }
        else {
            Double temperature = (double)((int)(data[0] & 0xFF) - 127) / 2.4 + 25;
            return temperature;
        }
    }

    /**
     * Returns true if the temperature is greater than the value specified
     * by limit, false otherwise.
     *
     * @param limit The value the temperature needs to exceed
     * @return true if the temperature exceeds the value specified by limit
     */
    public boolean isTemperature(final double limit)
    {
        return (limit < getTemperature());
    }
    /**
     * Command to write a command to Finch
     */
    private void writeFinch(byte[] command) {
        if (!HIDFinch.isOpen() || HIDFinch == null) {
            System.out.println("Finch not connected");
        } else {
            int val = HIDFinch.write(command, PACKET_LENGTH, (byte) 0x00);
            if (val < 0) {
                System.err.println(HIDFinch.getLastErrorMessage());
                HIDFinch = null;
            }
        }
    }

    /**
     * Command to read a sensor from Finch
     */
    private byte[] readFinch(byte[] command) {
    	// Check if the Finch connection is open
        if (!HIDFinch.isOpen() || HIDFinch == null) {
            System.out.println("Finch not connected");
        }
        else {
            // This is a hack to ensure that each return report is different from the one before. If the sensors haven't changed, the return report won't either, causing problems.
            command[7] = (byte)reportCounter;
            byte data[] = new byte[PACKET_LENGTH];
            // This method reads the returned report, or returns null if it has timed out after 10 ms
            int val = HIDFinch.read(data,10); // throw away the first read
            val = HIDFinch.write(command, PACKET_LENGTH, (byte) 0x00);
            if (val < 0) {
                System.err.println(HIDFinch.getLastErrorMessage());
            }
            // This method reads the returned report, or returns null if it has timed out after 10 ms
            val = HIDFinch.read(data,10);
            
            // If the read report does not match with the command report, then try reading again
            if(((int)(data[7] & 0xFF) != reportCounter))
            {
            	val = HIDFinch.read(data,10);
            }
            
            // If things are still wonky, try ten more times to resynchronize, then give up because infinite loops are bad
            int count = 0;
            while((int)(data[7] & 0xFF) != reportCounter && count < 10)
            {
            	val = HIDFinch.write(command, PACKET_LENGTH, (byte) 0x00);
                if (val < 0) {
                    System.err.println(HIDFinch.getLastErrorMessage());
                }
            	
            	val = HIDFinch.read(data,10);
            	count++;
            }
            
            
            reportCounter++;
            if(reportCounter > 255)
            	reportCounter = 0;
            // If val is -1, it means you lost connection, so set the HIDFinch to null, indicating connection lost
            // It is possible that the object needs to be disposed of in a cleaner way
            switch (val) {
                case -1:
                    System.err.println(HIDFinch.getLastErrorMessage());
                    HIDFinch = null; // Lost connection, make it so you can reopen the connection later
                    break;
                case 0:
                    System.err.println("No sensor data received");
                    break;
            }
            return data;
        }
        return null;
    }

    /**
     * Displays a graph of the X, Y, and Z accelerometer values.  Note that this graph
     * does not update on its own - you need to call updateAccelerometerGraph to
     * do so.
     *
     */
     /** Graphing isn't need for the BirdBrain Robot Server, so this is commented out
    public void showAccelerometerGraph()
    {
        accelerometerPlotter.addDataset(Color.RED);
        accelerometerPlotter.addDataset(Color.GREEN);
        accelerometerPlotter.addDataset(Color.BLUE);

        //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        final Component plotComponent = accelerometerPlotter.getComponent();

                        // create the main frame
                        jFrameAccel = new JFrame("Accelerometer Values");

                        // add the root panel to the JFrame
                        jFrameAccel.add(plotComponent);

                        // set various properties for the JFrame
                        jFrameAccel.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        jFrameAccel.addWindowListener(
                                new WindowAdapter()
                                {
                                    //  
                                    public void windowClosing(final WindowEvent e)
                                    {
                                        jFrameAccel.setVisible(false);
                                        jFrameAccel.dispose();
                                    }
                                });
                        jFrameAccel.setBackground(Color.WHITE);
                        jFrameAccel.setResizable(false);
                        jFrameAccel.pack();
                        jFrameAccel.setLocation(400, 200);// center the window on the screen
                        jFrameAccel.setVisible(true);
                    }
                });
    }*/

    /**
     * updates the accelerometer graph with accelerometer data specified by xVal,
     * yVal, and zVal.
     *
     * @param xVal  The X axis acceleration value
     * @param yVal  The Y axis acceleration value
     * @param zVal  The Z axis acceleration value
     */
    /*
    //  
    public void updateAccelerometerGraph(final double xVal, final double yVal, final double zVal)
    {
        accelerometerPlotter.setCurrentValues(xVal, yVal, zVal);
    }*/

    /**
     * Closes the opened Accelerometer Graph
     */
    /*
    //  
    public void closeAccelerometerGraph()
    {
        jFrameAccel.setVisible(false);
        jFrameAccel.dispose();
    }*/

    /**
     * Displays a graph of the left and right light sensor values.  Note that this graph
     * does not update on its own - you need to call updateLightSensorGraph to
     * do so.
     *
     */

    /*
    //  
    public void showLightSensorGraph()
    {
        lightPlotter.addDataset(Color.RED);
        lightPlotter.addDataset(Color.BLUE);

        //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        final Component plotComponent = lightPlotter.getComponent();

                        // create the main frame
                        jFrameLight = new JFrame("Light Sensor Values");

                        // add the root panel to the JFrame
                        jFrameLight.add(plotComponent);

                        // set various properties for the JFrame
                        jFrameLight.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        jFrameLight.addWindowListener(
                                new WindowAdapter()
                                {
                                    //  
                                    public void windowClosing(final WindowEvent e)
                                    {
                                        jFrameLight.setVisible(false);
                                        jFrameLight.dispose();
                                    }
                                });
                        jFrameLight.setBackground(Color.WHITE);
                        jFrameLight.setResizable(false);
                        jFrameLight.pack();
                        jFrameLight.setLocation(20, 200);// center the window on the screen
                        jFrameLight.setVisible(true);
                    }
                });
    }
    */
    /**
     * Updates the light sensor graph with the left and right light sensor data.
     *
     * @param leftSensor  Variable containing left light sensor value
     * @param rightSensor  Variable containing right light sensor value
     */
    /*
    //  
    public void updateLightSensorGraph(final int leftSensor, final int rightSensor)
    {
        lightPlotter.setCurrentValues(leftSensor, rightSensor);
    }*/

    /**
     * Closes the opened Light sensor Graph
     */
    /*
    //  
    public void closeLightSensorGraph()
    {
        jFrameLight.setVisible(false);
        jFrameLight.dispose();
    }*/

    /**
     * Displays a graph of the temperature value.  Note that this graph
     * does not update on its own - you need to call updateTemperatureGraph to
     * do so.
     *
     */
    /*

    //  
    public void showTemperatureGraph()
    {
        temperaturePlotter.addDataset(Color.GREEN);

        //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        final Component plotComponent = temperaturePlotter.getComponent();

                        // create the main frame
                        jFrameTemp = new JFrame("Temperature Values");

                        // add the root panel to the JFrame
                        jFrameTemp.add(plotComponent);

                        // set various properties for the JFrame
                        jFrameTemp.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        jFrameTemp.addWindowListener(
                                new WindowAdapter()
                                {
                                    //  
                                    public void windowClosing(final WindowEvent e)
                                    {
                                        jFrameTemp.setVisible(false);
                                        jFrameTemp.dispose();
                                    }
                                });
                        jFrameTemp.setBackground(Color.WHITE);
                        jFrameTemp.setResizable(false);
                        jFrameTemp.pack();
                        jFrameTemp.setLocation(780, 200);// center the window on the screen
                        jFrameTemp.setVisible(true);
                    }
                });
    }*/

    /**
     * Updates the temperature graph with the most recent temperature data.
     *
     * @param temp   variable containing a temperature value
     */
/*
    //  
    public void updateTemperatureGraph(final double temp)
    {
        temperaturePlotter.setCurrentValues(temp);
    }
*/
    /**
     * Closes the opened temperature Graph
     */
  /*  //  
    public void closeTemperatureGraph()
    {
        jFrameTemp.setVisible(false);
        jFrameTemp.dispose();
    }
*/
    /**
     * This method properly closes the connection with the Finch and resets the Finch so that
     * it is immediately ready to be controlled by subsequent programs.  Note that if this
     * method is not called at the end of the program, the Finch will continue to act on its
     * most recent command (such as drive forward) for 5 seconds before automatically timing
     * out and resetting.  This is why we recommend you always call the quit method at the end
     * of your program.
     */
    //  
    public void quit()
    {
        /* No graphs are launched, so comment this out
        if (jFrameAccel != null)
        {
            closeAccelerometerGraph();
        }
        if (jFrameLight != null)
        {
            closeLightSensorGraph();
        }
        if (jFrameTemp != null)
        {
            closeTemperatureGraph();
        }*/
        // Send the reset command
        byte[] message = new byte[PACKET_LENGTH];
        message[0] = 'R'; // Turns off motors, shuts off the Finch
        writeFinch(message);
        // Shut down and rely on auto-shutdown hook to clear HidApi resources
        hidServices.shutdown();
        HIDFinch.close();
    }
}




