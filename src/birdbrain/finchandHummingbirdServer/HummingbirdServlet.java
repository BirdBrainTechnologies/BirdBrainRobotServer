package birdbrain.finchandHummingbirdServer;

import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.http.*;
 
/**
 * Hummingbird Servlet - Exposes Hummingbird functionality to the outside world
 * @author Tom Lauwers
 *
 */

public class HummingbirdServlet extends HttpServlet
{   
  
	private static final long serialVersionUID = 585210767963249475L;
	
	private HummingbirdServletWrapper hummingbird = null; // Container for Hummingbird object	
	private boolean isConnected=false; // Holds Hummingbird connection state

  // If we got a wrapper object from the server class, Hummingbird was found and connected to
  public HummingbirdServlet(HummingbirdServletWrapper wrapFromServer) {
	  hummingbird = wrapFromServer;
  }
  
  public void setConnectionState(boolean state)
  {
	  isConnected = state;
  }
  
  
  /* Chain for hummingbird URLs:
   *
   * out/motor/port/speed (port 1 or 2, speed -100 to 100)
   * out/servo/port/position (port 1 to 4, position 0 to 160)
   * out/vibration/port/speed (port 1 or 2, speed 0 to 100)
   * out/led/port/intensity (port 1 to 4, intensity 0 to 100)
   * out/triled/port/R/G/B (port 1 or 2, R, G, B are intensities 0 to 100)
   * 
   * 
   * in/sensors (all four sensors, scaled to 0 to 100)
   * in/sensor/position (value at position x)
   * in/distance/position (value at position x in cm if it's a distance sensor)
   * in/temperature/position (value at position x if it's a temperature sensor)
   * in/sound/position (value at position x if it's a sound sensor)
   */
  
  @Override
  // doGet for Hummingbird requests
  
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
  {
	  String urlPath = req.getPathInfo();
	  
	  // If you instantiated without sending a HummingbirdServletWrapper object
	  if(!isConnected) {
		  response.getWriter().print("Hummingbird not connected");
	  }
	  // Else, parse the URL to figure out what to do
	  else {
		  // If in, then we're looking for a sensor
		  if(urlPath.substring(1,3).equals("in")) {
			  // Print all four sensors, divide by 2.55 to get 0 to 100
			  try {
				  if(urlPath.substring(4).equals("sensors")) {
					  int[] sensors = hummingbird.getSensors();
					  if(sensors == null) {
						  response.getWriter().print("null");
					  }
					  else {
						  response.getWriter().print((int)(sensors[0]/2.55)+ " " + (int)(sensors[1]/2.55) + " " + (int)(sensors[2]/2.55) + " " + (int)(sensors[3]/2.55));
					  }
				  }
				  else if(urlPath.substring(4,8).equals("led/")) {
					  Integer ledVal = null;
					  try {
						  	ledVal = hummingbird.getLEDValue((int)(Double.parseDouble(urlPath.substring(8))));
						  }
						  // You've just sent a non-number, so the "set" did not work 
						  catch(NumberFormatException e) {
							  response.getWriter().println("specified port not a number");
						  }
					  if(ledVal == null) 
						  response.getWriter().print("null");
					  else
						  response.getWriter().print(ledVal);
				  }
				  else if(urlPath.substring(4,10).equals("sound/")) {
					  Integer sound = null;
					  try {
						  sound = hummingbird.getSoundValue((int)(Double.parseDouble(urlPath.substring(10))));
					  }
					  // You've just sent a non-number, so the "set" did not work 
					  catch(NumberFormatException e) {
						  response.getWriter().println("specified port not a number");
					  }
					  if(sound == null) {
						  response.getWriter().print("null");
					  }
					  else {
						  response.getWriter().print(sound);
					  }
				  }
				  else if(urlPath.substring(4,10).equals("motor/")) {
					  Integer motorVal = null;
					  try {
						  motorVal = hummingbird.getMotorValue((int)(Double.parseDouble(urlPath.substring(10))));
						  }
						  // You've just sent a non-number, so the "set" did not work 
						  catch(NumberFormatException e) {
							  response.getWriter().println("specified port not a number");
						  }
					  if(motorVal == null) 
						  response.getWriter().print("null");
					  else
						  response.getWriter().print(motorVal);
				  }
				  else if(urlPath.substring(4,10).equals("servo/")) {
					  Integer servoVal = null;
					  try {
						  servoVal = hummingbird.getServoValue((int)(Double.parseDouble(urlPath.substring(10))));
						  }
						  // You've just sent a non-number, so the "set" did not work 
						  catch(NumberFormatException e) {
							  response.getWriter().println("specified port not a number");
						  }
					  if(servoVal == null) 
						  response.getWriter().print("null");
					  else
						  response.getWriter().print(servoVal);
				  }
				  else if(urlPath.substring(4,11).equals("sensor/")) {
					  Integer sensor = null;
					  
					  try {
						  sensor = hummingbird.getSensorValue((int)(Double.parseDouble(urlPath.substring(11))));
					  }
					  // You've just sent a non-number, so the "set" did not work 
					  catch(NumberFormatException e) {
						  response.getWriter().println("specified port not a number");
					  }
					  
					  if(sensor == null) {
						  response.getWriter().print("null");
					  }
					  else {
						  response.getWriter().print((int)(sensor/2.55));
					  }
				  }
				  else if(urlPath.substring(4,11).equals("triled/")) {
					  int[] triVal = null;
					  try {
						  triVal =  hummingbird.getTriLEDValue((int)(Double.parseDouble(urlPath.substring(11))));
						  }
						  // You've just sent a non-number, so the "set" did not work 
						  catch(NumberFormatException e) {
							  response.getWriter().println("specified port not a number");
						  }
					  if(triVal == null) 
						  response.getWriter().print("null");
					  else
						  response.getWriter().print(triVal[0] + " " + triVal[1] + " " + triVal[2]);
				  }
				  else if(urlPath.substring(4,13).equals("distance/")) {
					  Integer distance = null;
					  try {
						  distance = hummingbird.getDistanceAtPort((int)(Double.parseDouble(urlPath.substring(13))));
					  }
					  // You've just sent a non-number, so the "set" did not work 
					  catch(NumberFormatException e) {
						  response.getWriter().println("specified port not a number");
					  }
					  if(distance == null) {
						  response.getWriter().print("null");
					  }
					  else {
						  response.getWriter().print(distance);
					  }
				  }
				  else if(urlPath.substring(4,14).equals("vibration/")) {
					  Integer vibVal = null;
					  try {
						  vibVal = hummingbird.getVibrationValue((int)(Double.parseDouble(urlPath.substring(14))));
						  }
						  // You've just sent a non-number, so the "set" did not work 
						  catch(NumberFormatException e) {
							  response.getWriter().println("specified port not a number");
						  }
					  if(vibVal == null) 
						  response.getWriter().print("null");
					  else
						  response.getWriter().print(vibVal);
				  }
				  else if(urlPath.substring(4,16).equals("temperature/")) {
					  Double temperature = null;
					  try {
					  	temperature = hummingbird.getTemperatureAtPort((int)(Double.parseDouble(urlPath.substring(16))));
					  }
					  // You've just sent a non-number, so the "set" did not work 
					  catch(NumberFormatException e) {
						  response.getWriter().println("specified port not a number");
					  }
					  	
					  if(temperature == null) {
						  response.getWriter().print("null");
					  }
					  else {
						  response.getWriter().print(temperature);
					  }
				  }
				  // If the Hummingbird is active and you wrote in but the remainder is garbage, send an error message
				  else {
					  response.getWriter().print("Wrong sensor request"); 
				  }
			  }
			  catch(StringIndexOutOfBoundsException e)
			  {
				  response.getWriter().print("Wrong sensor request"); 
			  }
		  }
		  // setOutput parses through everything inside the HummingbirdWrapper
		  else if(urlPath.substring(1,4).equals("out")) {
			  if(hummingbird.setOutput(urlPath.substring(5))) {
				  response.getWriter().print("Output set");
			  }
			  else {
				  response.getWriter().print("Wrong output setting request"); 
			  }
		  }
		  else if (urlPath.substring(1,7).equals("change")){
			  if(hummingbird.changeOutput(urlPath.substring(8))) {
				  response.getWriter().print("Output changed");
			  }
			  else {
				  response.getWriter().print("Wrong output changer request"); 
			  }
		  }
		  // If you wrote hummingbird as part of the url, you get garbage
		  else {
			  response.getWriter().print("Wrong Hummingbird request"); 
		  }
	  }
  }
}