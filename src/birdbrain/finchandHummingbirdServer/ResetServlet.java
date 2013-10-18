package birdbrain.finchandHummingbirdServer;

import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Reset servlet, allow Scratch to turn off all motors, servos, LEDs on Finch or Hummingbird
 * @author Tom
 *
 */
 
public class ResetServlet extends HttpServlet
{   
	private static final long serialVersionUID = -3319961402649730446L;

	// FinchServletWrapper wraps a standard Finch object to make it easier to manage
	private FinchServletWrapper finch = null;
	// HummingbirdServletWrapper wraps a standard Hummingbird object to make it easier to manage
	private HummingbirdServletWrapper hummingbird = null;		
	
	private boolean isFinchConnected=false; // Tracks if we've connected to a real Finch
	private boolean isHummingbirdConnected=false; // Tracks if we've connected to a real Hummingbird
  
  // If we've been provided with a Finch object, that means the main server has already verified the connection for us, so it's connected!
  public ResetServlet(FinchServletWrapper finchFromServer, HummingbirdServletWrapper hummingbirdFromServer) {
	  finch = finchFromServer;
	  hummingbird = hummingbirdFromServer;
  }
  
  public void setFinchConnectionState(boolean state) {
	  isFinchConnected = state;
  }
  public void setHummingbirdConnectionState(boolean state) {
	  isHummingbirdConnected = state;
  }
  
  @Override
  // doGet for Reset requests
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
  {
	  if(isFinchConnected) {
		  finch.setOutput("motor/0/0");
		  finch.setOutput("buzzer/0/0");
		  finch.setOutput("led/0/0/0");
	  }
	  
	  if(isHummingbirdConnected) {
		  hummingbird.setOutput("motor/1/0");
		  hummingbird.setOutput("motor/2/0");
		  hummingbird.setOutput("vibration/1/0");
		  hummingbird.setOutput("vibration/2/0");
		  hummingbird.setOutput("led/1/0");
		  hummingbird.setOutput("led/2/0");
		  hummingbird.setOutput("led/3/0");
		  hummingbird.setOutput("led/4/0");
		  hummingbird.setOutput("triled/1/0/0/0");
		  hummingbird.setOutput("triled/2/0/0/0");
	  }
  }
}