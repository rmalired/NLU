/**
 * 
 */
package indiv.rakesh.chatbot.suggest;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * @author rakesh.malireddy
 *
 */
public final class StockSuggestSpeechletRequestHandler extends SpeechletRequestStreamHandler {
	
	// amzn1.ask.skill.2d25240b-dba5-4c56-93ee-b233ad8b992e
	
	 private static final Set<String> supportedApplicationIds = new HashSet<String>();
	    static {
	        /*
	         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
	         * Alexa Skill and put the relevant Application Ids in this Set.
	         */
	        supportedApplicationIds.add("amzn1.ask.skill.2d25240b-dba5-4c56-93ee-b233ad8b992e");
	    }

	    public StockSuggestSpeechletRequestHandler() {
	        super(new StockSuggestSpeechlet(), supportedApplicationIds);
	    }

	/**
	 * @param speechlet
	 * @param supportedApplicationIds
	 */
	public StockSuggestSpeechletRequestHandler(SpeechletV2 speechlet, Set<String> supportedApplicationIds) {
		super(speechlet, supportedApplicationIds);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param speechlet
	 * @param supportedApplicationIds
	 */
	public StockSuggestSpeechletRequestHandler(Speechlet speechlet, Set<String> supportedApplicationIds) {
		super(speechlet, supportedApplicationIds);
		// TODO Auto-generated constructor stub
	}

}
