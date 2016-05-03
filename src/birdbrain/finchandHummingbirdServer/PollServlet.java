package birdbrain.finchandHummingbirdServer;

import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Poll servlet, exposes Finch/Hummingbird sensor values over a localhost server
 * @author Tom
 *
 */
 
public class PollServlet extends HttpServlet
{   
	private static final long serialVersionUID = -3319961402649730446L;

	// FinchServletWrapper wraps a standard Finch object to make it easier to manage
	private FinchServletWrapper finch = null;	
	// HummingbirdServletWrapper wraps a standard Hummingbird object to make it easier to manage
	private HummingbirdServletWrapper hummingbird = null;		
	
	private boolean isFinchConnected=false; // Tracks if we've connected to a real Finch
	private boolean isHummingbirdConnected=false; // Tracks if we've connected to a real Hummingbird
  
	private boolean finchProblemReport; // sets if we need to report a Finch problem
	private boolean hummingbirdProblemReport; // sets if we need to report a Hummingbird problem
	
  // If we've been provided with a Finch object, that means the main server has already verified the connection for us, so it's connected!
  public PollServlet(FinchServletWrapper finchFromServer, HummingbirdServletWrapper hummingbirdFromServer) {
	  finch = finchFromServer;
	  hummingbird = hummingbirdFromServer;
  }
  
  public void setFinchConnectionState(boolean state) {
	  isFinchConnected = state;
  }
  public void setHummingbirdConnectionState(boolean state) {
	  isHummingbirdConnected = state;
  }  
  
  public void setFinchProblemReport(boolean state) {
	  finchProblemReport = state;
  }
  public void setHummingbirdProblemReport(boolean state) {
	  hummingbirdProblemReport = state;
  } 
  
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
  {
	  String endofline = System.getProperty("line.separator");
	  // If you instantiated without sending a FinchServletWrapper object
	  if(isFinchConnected) {
		  int[] lights = finch.getLightSensors();
		  boolean[] obstacles = finch.getObstacle();
		  double[] accels = finch.getAcceleration();
		  Double temperature = finch.getTemperature();
		  
		  if(accels != null && obstacles != null && lights != null && temperature != null)
		  {
			  
			  String finchOrientation = "In_Between";	  
				
		     if (accels[0] < -0.8 && accels[0] > -1.5 && accels[1] > -0.3 && accels[1] < 0.3 && accels[2] > -0.3 && accels[2] < 0.3)
	            {
	        	 finchOrientation = "Beak_Up";
	            }
	        
	         if (accels[0] < 1.5 && accels[0] > 0.8 && accels[1] > -0.3 && accels[1] < 0.3 && accels[2] > -0.3 && accels[2] < 0.3)
	            {
	        	 finchOrientation = "Beak_Down";
	            }
	       
	         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > 0.65 && accels[2] < 1.5)
	            {
	        	 finchOrientation = "Level";
	            }
	         
	         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > -1.5 && accels[2] < -0.65)
	            {
	        	 finchOrientation = "Upside_Down";
	            }
	       
	         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > 0.7 && accels[1] < 1.5 && accels[2] > -0.5 && accels[2] < 0.5)
	            {
	        	 finchOrientation = "Left_Wing_Down";
	            }
	
	         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -1.5 && accels[1] < -0.7 && accels[2] > -0.5 && accels[2] < 0.5)
	            {
	            finchOrientation = "Right_Wing_Down";
	            }
			         
	          response.getWriter().print("temperature " + Math.floor(temperature*100)/100 + endofline);
			  response.getWriter().print("leftLight " + (int)(lights[0]*100/255) + endofline);
			  response.getWriter().print("rightLight " + (int)(lights[1]*100/255) + endofline);
			  response.getWriter().print("leftObstacle " +   obstacles[0] + endofline);
			  response.getWriter().print("rightObstacle " +  obstacles[1] + endofline);
			  response.getWriter().print("orientation " + finchOrientation + endofline);
			  response.getWriter().print("XAcceleration " + Math.floor(100*accels[0])/100 + endofline);
			  response.getWriter().print("YAcceleration " + Math.floor(100*accels[1])/100 + endofline);
			  response.getWriter().print("ZAcceleration " + Math.floor(100*accels[2])/100 + endofline);
		  	}
		  else {
			  response.getWriter().print("_problem Finch sensors not responding");
		  }
	  }
	  else if(finchProblemReport && !isHummingbirdConnected){
		  response.getWriter().print("_problem Finch not connected" + endofline);  
	  }
	  if(isHummingbirdConnected)
	  {
		  int[] sensors = hummingbird.getSensors();
		  if(sensors != null)
		  {
			  for(int i = 1; i < 5; i++)
			  {
				  response.getWriter().print("voltage/" + i + " " + Math.floor(100*(double)sensors[i-1]/51)/100 + endofline);
				  response.getWriter().print("raw/" + i + " " + (int)((double)sensors[i-1]/2.55) + endofline);
			  }
		  }
		  else 
		  {
			  response.getWriter().print("_problem Hummingbird sensors not responding");
		  }
		  for(int i = 1; i < 5; i++)
		  {
			  Integer distance = hummingbird.getDistanceAtPort(i);
			  if(distance != null)
			  {
				  response.getWriter().print("distance/" + i + " " + distance + endofline);
			  }
			  else {
				  response.getWriter().print("_problem Hummingbird sensors not responding");  
			  }
		  }
		  for(int i = 1; i < 5; i++)
		  {
			  Integer sound = hummingbird.getSoundValue(i);
			  if(sound != null)
			  {
				  response.getWriter().print("sound/" + i + " " + sound + endofline);
			  }
			  else {
				  response.getWriter().print("_problem Hummingbird sensors not responding");  
			  }
		  }
		  for(int i = 1; i < 5; i++)
		  {
			  Double temperature = hummingbird.getTemperatureAtPort(i);
			  if(temperature != null)
			  {
				  response.getWriter().print("temperature/" + i + " " + temperature + endofline);
			  }
			  else {
				  response.getWriter().print("_problem Hummingbird sensors not responding");  
			  }
		  }		  
	  }
	  else if(hummingbirdProblemReport && !isFinchConnected){
		  response.getWriter().print("_problem Hummingbird not connected");  
	  }
  }
}