/**
 * 
 */
package indiv.rakesh.chatbot.suggest;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

/**
 * @author rakesh.malireddy
 *
 */
public class StockSuggestSpeechlet implements Speechlet {
	
	 private static final Logger log = LoggerFactory.getLogger(StockSuggestSpeechlet.class);
	 
	 private final String Equity = "equity";
	 private final String RequestedEquity = "RequestedEquity";
	 private final String Tick = "tick";
	 private final String Term = "term";
	 private final String RequestedTerm = "RequestedTerm";


	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		
		 log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
	                session.getSessionId());
		 
		 Intent intent = request.getIntent();
		 String intentName = (intent != null) ? intent.getName() : null;
		 if("GetStockPrice".equalsIgnoreCase(intentName)){
			return getStockResponse(intent,session);
		 }else if("GetStockInfo".equalsIgnoreCase(intentName)){
			return getStockAnalysis(intent, session);
		 }
		 else if("AMAZON.HelpIntent".equals(intentName)){
			 return getHelpResponse();
		 }else{
			 throw new SpeechletException("Invalid Intent");
		 }
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
	}

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		 log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
	                session.getSessionId());
	        // any cleanup logic goes here need to figure out what can be terminated
		
	}

	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initalization goes here  need to figure out what can be initialized.
		
	}
	
    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to TED, you can request for price of stock in Dow Jones Industrial 30 and check for investment opportunities";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the getstockprice intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getStockResponse(Intent intent, final Session session) {
    	
    	Slot slot = intent.getSlot("tick");   
    	
    	String speechText = "Unable to retrieve the amount of the requested stock, may be ask with a ticker symbol instead of full name";
    	log.info("trying to retrieve slot value");   	
    	
    	if(slot != null && slot.getValue()!= null){
    		
    		
    		
    		String tick = slot.getValue().toUpperCase();
    		log.info("tick value : "+tick);
    		
    		List<Stock> stocks = getStockInfo(tick, 4);
    		
    		if(stocks != null && stocks.size()>0){
    			Stock lastInfo = stocks.get(0);
    			speechText = "The price of the "+tick+" is "+lastInfo.getClosePrice()+ " US $";
    		}
    	}

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("StockTicker");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }
    
    private List<Stock> getStockInfo(String stock, int size){
    	
    	List<Stock> stockItems = new ArrayList<>();
    	Item item = null;
    	
    	if(size ==0){
    		//default to 4 days
    		size = 4;
    	}
    	
    	stock = stock.toUpperCase();
    	
    	log.info("stock value : "+stock);
    	
		// do a dynamodb lookup based on the ticker
		 AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
	        		.withRegion(Regions.US_EAST_1).build();
	        DynamoDB dynamoDB = new DynamoDB(client);
	        
	        Table table = dynamoDB.getTable("StockPrice");	        
			LocalDate ldate = LocalDate.now();    			
			LocalDate _4daysback = ldate.minus(Period.ofDays(size));	
			ZonedDateTime _4daysZdt = _4daysback.atStartOfDay(ZoneId.of("America/Chicago"));
			
	        QuerySpec spec = new QuerySpec()
	        	    .withKeyConditionExpression("Tick = :v_tick and cdate >= :v_date")        	    
	        	    .withValueMap(new ValueMap()
	        	        .withString(":v_tick", stock)
	        	        .withNumber(":v_date", _4daysZdt.toEpochSecond()))        	      
	        	    .withConsistentRead(true)
	        	    .withScanIndexForward(false);
	        
	        ItemCollection<QueryOutcome> items = table.query(spec);
	        Iterator<Item> iterator = items.iterator();
	        
	        while(iterator.hasNext()){
	        	item = iterator.next();
	        	//map item to stock
	        	//AdjClose, Close, High, Low, Open, Volume
	        	Stock _stock = new Stock(); // need to do a builder pattern
	        	
	        	_stock.setClosePrice(item.getNumber("AdjClose").doubleValue());
	        	_stock.setTick(item.getString("Tick"));
	        	TrailingIndicators tIndicator = new TrailingIndicators();
	        	tIndicator.setEma26((item.hasAttribute("EMA_26"))?item.getNumber("EMA_26").doubleValue():null);
	        	tIndicator.setEma9((item.hasAttribute("EMA_9"))?item.getNumber("EMA_9").doubleValue():null);
	        	tIndicator.setSma5((item.hasAttribute("SMA_5"))?item.getNumber("SMA_5").doubleValue():null);
	        	tIndicator.setSma20((item.hasAttribute("SMA_20"))?item.getNumber("SMA_20").doubleValue():null);
	        	tIndicator.setSma30((item.hasAttribute("SMA_30"))?item.getNumber("SMA_30").doubleValue():null);
	        	tIndicator.setSma50((item.hasAttribute("SMA_50"))?item.getNumber("SMA_50").doubleValue():null);
	        	tIndicator.setSma60((item.hasAttribute("SMA_60"))?item.getNumber("SMA_60").doubleValue():null);
	        	tIndicator.setSma200((item.hasAttribute("SMA_200"))?item.getNumber("SMA_200").doubleValue():null);
	        	_stock.setTrailingIndicators(tIndicator);
	        	
	        	stockItems.add(_stock);
	        }    	
    	return stockItems;
    	
    	
    }

    /**
     *  Creates a {@code SpeechletResponse} for the getstockinfo intent
     * 
     * @param intent
     * @param session
     * @return
     */
    private SpeechletResponse getStockAnalysis(Intent intent, final Session session){
    	
    	Slot equitySlot  = intent.getSlot("equity");
    	Slot termSlot = intent.getSlot("term");
    	
    	/*
    	 * if equity slot exists then check for term-slot.
    	 * if term slot does exists then call the lambda api to get the details
    	 * else prompt the user for term slot and pass the equity key in session response
    	 * 
    	 * if only term slot exists in input check for equity slot in session, if equity exists in session
    	 * call the back end api, else prompt the user for equity 
    	 */
    	
    	if(equitySlot != null && equitySlot.getValue() != null){
    		return handleEquityDialogRequest(intent, session);
    	}else if(termSlot != null && termSlot.getValue() != null){
    		return handleTermDialogRequest(intent, session);
    	}
    	
    	
    	return new SpeechletResponse();
    	
    
    }
    
    
    private SpeechletResponse handleTermDialogRequest(final Intent intent, final Session session){
    	String term = intent.getSlot(Term).getValue();
    	
    	int termSize = 10;
    	String speechText ="";
    	
    	if(session.getAttributes().containsKey(RequestedEquity)){
    		String requestedEquity = ((String)session.getAttribute(RequestedEquity)).toUpperCase();
    		
    		//invoke common method
    		
    		List<Stock> stockHistory = getStockInfo(requestedEquity, termSize);
    		if(stockHistory != null && stockHistory.size()>0){
    			speechText = stockHistory.get(0).shortTermAnalysis();
    		}
    		
    		 // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("TED");
            card.setContent(speechText);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            // Create reprompt
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);
            
    		
    	}else{
    		//set equity in session and re-prompt for term
    		session.setAttribute(RequestedTerm, term);
    		String speechOutput = "What is the stock looking for";
    		String repromptText = "What is the stock looking for";
    		
    		return newAskResponse(speechOutput, repromptText);  		
    		
    	}
    }
    
    
    
    private SpeechletResponse handleEquityDialogRequest(final Intent intent, final Session session){
    	
    	String equity = intent.getSlot(Equity).getValue();
    	
    	int termSize = 10;
    	String speechText ="";
    	
    	if(session.getAttributes().containsKey(RequestedTerm)){
    		String requestedTerm = (String)session.getAttribute(RequestedTerm);
    		List<Stock> stockHistory = getStockInfo(equity, termSize);
    		if(stockHistory != null && stockHistory.size()>0){
    			speechText = stockHistory.get(0).shortTermAnalysis();
    		}
    		
    		 // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("TED");
            card.setContent(speechText);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            // Create reprompt
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);
    	}else{
    		//set equity in session and re-prompt for term
    		session.setAttribute(RequestedEquity, equity);
    		String speechOutput = "What is the investment horizon looking for such as short term  or intermediate or long term";
    		String repromptText = "What is the investment horizon looking for such as short term  or intermediate or long term";
    		
    		return newAskResponse(speechOutput, repromptText);  		
    		
    	}
    	
    }
    
    
    
    
    
    
    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say hello to me!";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
    
    /**
     * Wrapper for creating the Ask response from the input strings with
     * plain text output and reprompt speeches.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        return newAskResponse(stringOutput, false, repromptText, false);
    }

    /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param isOutputSsml
     *            whether the output text is of type SSML
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml
     *            whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
            String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

}
