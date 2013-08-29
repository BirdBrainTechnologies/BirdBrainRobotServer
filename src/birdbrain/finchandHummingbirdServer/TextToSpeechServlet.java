package birdbrain.finchandHummingbirdServer;

import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.http.*;

import edu.cmu.ri.createlab.audio.AudioHelper;
import edu.cmu.ri.createlab.speech.Mouth;

/**
 * Text to speech servlet - simply servlet that exposes text to speech capability on the computer to the localhost server.
 * Uses freeTTS as the underlying library for TTS.
 * @author Tom Lauwers
 *
 */
 
public class TextToSpeechServlet extends HttpServlet
{   

  private static final long serialVersionUID = -6232170414050004611L;

  
  public TextToSpeechServlet() {
      // Set system properties to point to the freeTTS directory for saySomething support
      System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");	  
  }
  
  // Helper function to make sure the speech is speakable
  // If so, returns a byte stream for speech
  private final byte[] getSpeech(final String whatToSay)
     {
     if (whatToSay != null && whatToSay.length() > 0)
        {
        final Mouth mouth = Mouth.getInstance();

        if (mouth != null)
           {
           return mouth.getSpeech(whatToSay);
           }
        }
     return null;
     }
  
  // The actual function that does the speaking - just uses the playClip method from
  // CREATE lab's library of audio helper functions
  private final void speak(final String whatToSay)
     {
     final byte[] speechAudio = getSpeech(whatToSay);
     if (speechAudio != null)
        {
        AudioHelper.playClip(speechAudio);
        }
     }
  
  /* Chain for speech URLs:
   *
   * sayThis (say this is a string of indeterminate length that you need to say
   * 
   */
  
  @Override
  // doGet for Text to speech requests
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
  {
	  String urlPath = req.getPathInfo(); // get the path, which in this case is what the user wants to have spoken
	  
	  speak(urlPath.substring(1)); // say the path minus the leading slash
  }
}