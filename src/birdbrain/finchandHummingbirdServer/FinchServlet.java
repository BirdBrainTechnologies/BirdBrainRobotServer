package birdbrain.finchandHummingbirdServer;

import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Finch servlet, exposes Finch functionality over a localhost server
 * @author Tom
 *
 */
 
public class FinchServlet extends HttpServlet
{   
	private static final long serialVersionUID = -3319961402649730446L;

	// FinchServletWrapper wraps a standard Finch object to make it easier to manage
	private FinchServletWrapper finch = null;	
		
	private boolean isConnected=false; // Tracks if we've connected to a real Finch
 
  // If we've instantiated this way, we are not connected
 // public FinchServlet() {
//	  isConnected = false;
 // }
  
  // If we've been provided with a Finch object, that means the main server has already verified the connection for us, so it's connected!
  public FinchServlet(FinchServletWrapper wrapFromServer) {
	  finch = wrapFromServer;
//	  isConnected = true;
  }
  
  public void setConnectionState(boolean state) {
	  isConnected = state;
  }
   
  /* Chain for finch URLs:
   *
   * out/motor/left/right
   * out/buzzer/frequency/duration
   * out/led/red/green/blue
   * 
   * 
   * in/lights
   * in/lightLeft
   * in/lightRight
   * in/obstacles
   * in/obstacleLeft
   * in/obstacleRight
   * in/accelerations
   * in/accelerationX
   * in/accelerationY
   * in/accelerationZ
   * in/temperature
   * in/lastTappedTime
   * in/lastShakenTime
   *
   */
  
  @Override
  // doGet for Finch requests
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
  {
	  String urlPath = req.getPathInfo();
	  
	  // If you instantiated without sending a FinchServletWrapper object
	  if(!isConnected) {
		  response.getWriter().print("Finch not connected");
	  }
	  // Else, parse the URL to figure out what to do
	  else {
		  // If in, then we're looking for a sensor
		  if(urlPath.substring(1,3).equals("in")) {
			  // Print both light sensors, divide by 2.55 to get 0 to 100
			  if(urlPath.substring(4).equals("lights")) {
				  int[] lights = finch.getLightSensors();
				  if(lights == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print((int)(lights[0]/2.55)+ " " + (int)(lights[1]/2.55));
				  }
			  }
			  // Print the left light sensor
			  else if(urlPath.substring(4).equals("lightLeft")) {
				  int[] lights = finch.getLightSensors();
				  if(lights == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print((int)(lights[0]/2.55));
				  }
			  }
			  // Right light sensor
			  else if(urlPath.substring(4).equals("lightRight")) {
				  int[] lights = finch.getLightSensors();
				  if(lights == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print((int)(lights[1]/2.55));
				  }
			  }
			  // X, Y, and Z accelerations
			  else if(urlPath.substring(4).equals("accelerations")) {
				  double[] accel = finch.getAcceleration();
				  if(accel == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  // Use math.floor to truncate to 2 decimal points
					  response.getWriter().print(Math.floor(100*accel[0])/100 + " " + Math.floor(100*accel[1])/100 + " " + Math.floor(100*accel[2])/100);
				  }
			  }
			  // Just X Acceleration
			  else if(urlPath.substring(4).equals("accelerationX")) {
				  double[] accel = finch.getAcceleration();
				  if(accel == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print(Math.floor(100*accel[0])/100);
				  }
			  }
			  // Just Y Acceleration
			  else if(urlPath.substring(4).equals("accelerationY")) {
				  double[] accel = finch.getAcceleration();
				  if(accel == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print(Math.floor(100*accel[1])/100);
				  }
			  }
			  // Just Z Acceleration
			  else if(urlPath.substring(4).equals("accelerationZ")) {
				  double[] accel = finch.getAcceleration();
				  if(accel == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print(Math.floor(100*accel[2])/100);
				  }
			  }
			  // Analyze the accelerometer data to return an orientation
			  else if(urlPath.substring(4).equals("orientation")) {
				  double[] accels = finch.getAcceleration();
				  String finchOrientation = "In Between";	  
	
				  if(accels == null) {
					  response.getWriter().print("null");
				  }
				  else {
			         if (accels[0] < -0.8 && accels[0] > -1.5 && accels[1] > -0.3 && accels[1] < 0.3 && accels[2] > -0.3 && accels[2] < 0.3)
			            {
			        	 finchOrientation = "Beak Up";
			            }
			        
			         if (accels[0] < 1.5 && accels[0] > 0.8 && accels[1] > -0.3 && accels[1] < 0.3 && accels[2] > -0.3 && accels[2] < 0.3)
			            {
			        	 finchOrientation = "Beak Down";
			            }
			       
			         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > 0.65 && accels[2] < 1.5)
			            {
			        	 finchOrientation = "Level";
			            }
			         
			         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > -1.5 && accels[2] < -0.65)
			            {
			        	 finchOrientation = "Upside Down";
			            }
			       
			         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > 0.7 && accels[1] < 1.5 && accels[2] > -0.5 && accels[2] < 0.5)
			            {
			        	 finchOrientation = "Left Wing Down";
			            }
		
			         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -1.5 && accels[1] < -0.7 && accels[2] > -0.5 && accels[2] < 0.5)
			            {
			            finchOrientation = "Right Wing Down";
			            }
				        response.getWriter().print(finchOrientation);
				  }
			  }
			  // Print both obstacle sensors
			  else if(urlPath.substring(4).equals("obstacles")) {
				  boolean[] obstacles = finch.getObstacle();
				  if(obstacles == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print(obstacles[0] + " " + obstacles[1]);
				  }
			  }
			  // Just left sensor
			  else if(urlPath.substring(4).equals("obstacleLeft")) {
				  boolean[] obstacles = finch.getObstacle();
				  if(obstacles == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print(obstacles[0]);
				  }
			  }
			  // Just right sensor
			  else if(urlPath.substring(4).equals("obstacleRight")) {
				  boolean[] obstacles = finch.getObstacle();
				  if(obstacles == null) {
					  response.getWriter().print("null");
				  }
				  else {
					  response.getWriter().print(obstacles[1]);
				  }
			  }
			  // Print temperature, use math.floor to get to two decimal points
			  else if(urlPath.substring(4).equals("temperature")) {
				  response.getWriter().print(Math.floor(finch.getTemperature()*100)/100);
				 
			  }
			  else if(urlPath.substring(4).equals("lastTappedTime")) {
				  response.getWriter().print(finch.getLastTappedTime());
			  }
			  else if(urlPath.substring(4).equals("lastShakenTime")) {
				  response.getWriter().print(finch.getLastShakenTime());
			  }
			  // If the Finch is active and you wrote "in" but the remainder is garbage, send an error message
			  else {
				  response.getWriter().print("Wrong sensor request"); 
			  }
		  }
		  // setOutput parses which specific output is desired inside the FinchWrapper
		  else if(urlPath.substring(1,4).equals("out")) {
			  if(finch.setOutput(urlPath.substring(5))) {
				  response.getWriter().print("Output set");
			  }
			  else {
				  response.getWriter().print("Wrong output setting request"); 
			  }
		  }
		  // If you wrote finch as part of the url but otherwise the request makes no sense, you get an error
		  else {
			  response.getWriter().print("Wrong Finch request"); 
			  
		  }
	  }
  }
}