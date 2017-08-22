/**
 * 
 */
package indiv.rakesh.chatbot.suggest;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Directive;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.IntentRequest.DialogState;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.dialog.directives.DelegateDirective;
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
	 
	 private static Map<String,String> stockSynonms;
	 
	 static {
		 stockSynonms = new HashMap<>();
		 stockSynonms.put("APPLE", "AAPL");
		 stockSynonms.put("AMERICAN EXPRESS", "AXP");
		 stockSynonms.put("BOEING", "BA");
		 stockSynonms.put("CATERPILLAR", "CAT");
		 stockSynonms.put("CISCO", "CSCO");
		 stockSynonms.put("CHEVRON", "CVX");
		 stockSynonms.put("DUPONT", "DD");
		 stockSynonms.put("DISNEY", "DIS");
		 stockSynonms.put("GENERAL ELECTRIC", "GE");
		 stockSynonms.put("GOLDMAN SACHS", "GS");
		 stockSynonms.put("HOME DEPOT", "HD");
		 stockSynonms.put("INTERNATIONAL BUSINESS MACHINES", "IBM");
		 stockSynonms.put("INTEL", "INTC");
		 stockSynonms.put("JOHNSON N JOHNSON", "JNJ");
		 stockSynonms.put("JP MORGAN", "JPM");
		 stockSynonms.put("COCA COLA", "KO");
		 stockSynonms.put("COKE", "KO");
		 stockSynonms.put("MAC DONALD", "MCD");
		 stockSynonms.put("MC DONALD", "MCD");
		 stockSynonms.put("3M", "MMM");
		 stockSynonms.put("THREE M", "MMM");
		 stockSynonms.put("MICROSOFT", "MSFT");
		 stockSynonms.put("NIKE", "NKE");
		 stockSynonms.put("PFIZER", "PFE");
		 stockSynonms.put("PROCTOR N GAMBLE", "PG");
		 stockSynonms.put("TRAVELLERS", "TRV");
		 stockSynonms.put("UNITED HEALTH", "UNH");
		 stockSynonms.put("UNITED TECHNOLOGIES", "UTX");
		 stockSynonms.put("VISA", "V");
		 stockSynonms.put("VERIZON", "VZ");
		 stockSynonms.put("WALMART", "WMT");
		 stockSynonms.put("EXON MOBIL", "XOM"); 
		 
	 }


	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		
		 log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
	                session.getSessionId());
		 
		 Intent intent = request.getIntent();
		 String intentName = (intent != null) ? intent.getName() : null;
		 
		 DialogState dialogState = request.getDialogState()==null ? DialogState.STARTED: request.getDialogState();
		 log.info("dialogState ={}", dialogState);
		 
		 log.info("intentName ={}", intentName);
		 if("GetStockPrice".equalsIgnoreCase(intentName)){
			return getStockResponse(intent,session);
		 }else if("GetStockInfo".equalsIgnoreCase(intentName)){
			 Slot equitySlot= null;
			 Slot termSlot = null;
			 switch(dialogState) {
			 default:
			 case STARTED:
				 equitySlot = intent.getSlot("equity");
				 termSlot = intent.getSlot("term");
				 
				 if(equitySlot == null || equitySlot.getValue() == null || equitySlot.getValue().trim().isEmpty()) {
				   log.info("Dialog.ElicitSlot equity");
				   return newDelegateResponse(intent);
				 }
				 
				 if(termSlot == null || termSlot.getValue() == null || termSlot.getValue().trim().isEmpty()) {
					   log.info("Dialog.ElicitSlot term");
					   return newDelegateResponse(intent);
				}
			 case IN_PROGRESS:
				 equitySlot = intent.getSlot("equity");
				 termSlot = intent.getSlot("term");
				 
				 if(equitySlot == null || equitySlot.getValue() == null || equitySlot.getValue().trim().isEmpty()) {
				   log.info("Dialog.ElicitSlot equity");
				   return newDelegateResponse(intent);
				 }
				 
				 if(termSlot == null || termSlot.getValue() == null || termSlot.getValue().trim().isEmpty()) {
					   log.info("Dialog.ElicitSlot term");
					   return newDelegateResponse(intent);
				}
				 
			 case COMPLETED:	
				 // Assuming all slots are filled - no cross checking of slot values
				 return getStockAnalysis(intent, session);
				 
			 }			
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
    		} else {
    			speechText = "Unable to retrieve the amount of the requested stock "+ tick+" , may be ask with a ticker symbol instead of full name";
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
    	
    	stock = (stockSynonms.containsKey(stock.toUpperCase()))?stockSynonms.get(stock.toUpperCase()):stock.toUpperCase();
    	
    	
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
	        	if(item.hasAttribute("EMA_26")) {
	        		double exp26 = item.getDouble("EMA_26");
	        		tIndicator.setEma26(exp26);
	        	}
	        	if(item.hasAttribute("EMA_9")) {
	        		tIndicator.setEma9(item.getDouble("EMA_9"));
	        	}
	        	if(item.hasAttribute("SMA_5")) {
	        		tIndicator.setSma5(item.getDouble("SMA_5"));
	        	}
	        	if(item.hasAttribute("SMA_20")) {
	        		tIndicator.setSma20(item.getDouble("SMA_20"));
	        	}
	        	if(item.hasAttribute("SMA_30")) {
	        		tIndicator.setSma30(item.getDouble("SMA_30"));
	        	}
	        	if(item.hasAttribute("SMA_50")) {
	        		tIndicator.setSma50(item.getDouble("SMA_50"));
	        	}
	        	if(item.hasAttribute("SMA_60")) {
	        		tIndicator.setSma60(item.getDouble("SMA_60"));
	        	}
	        	if(item.hasAttribute("SMA_200")) {
	        		tIndicator.setSma200(item.getDouble("SMA_200"));
	        	}
	        	_stock.setTrailingIndicators(tIndicator);
	        	
	        	stockItems.add(_stock);
	        } 
	        
	        log.info("returning stock items");
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
    	if((equitySlot != null && equitySlot.getValue() != null)&& (termSlot != null && termSlot.getValue() != null) ) {
    		log.info("Both slots found");
    		return handleEquityTermDialogRequest(intent, session);
    	}else if(equitySlot != null && equitySlot.getValue() != null){
    		log.info("Equity slot found");
    		return handleEquityDialogRequest(intent, session);
    	}else if(termSlot != null && termSlot.getValue() != null){
    		log.info("Term slot found");
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
    
    
    
    private SpeechletResponse handleEquityTermDialogRequest(final Intent intent, final Session session){
    	
    	String equity = intent.getSlot(Equity).getValue();
    	
    	int termSize = 10;
    	String speechText ="The stock you requested was unable to analayze";
    	
    	
    	
    		List<Stock> stockHistory = getStockInfo(equity, termSize);
    		if(stockHistory != null && stockHistory.size()>0){
    			log.info("found more than one entry");
    			speechText = stockHistory.get(0).shortTermAnalysis();//TODO currently hardcoded to short term need to change
    			log.info(speechText);
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

            return SpeechletResponse.newTellResponse(speech, card);
    	
    	
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
        String speechText = "You can request TED to know the value of stock and check for technical anaysis. You can say what's the price of AAPLE";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Ted");
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
    
    
    /**
     * 
     * @param updatedIntent
     * @return
     */
    public static SpeechletResponse newDelegateResponse(Intent updatedIntent) {
        final SpeechletResponse response = new SpeechletResponse();
        Directive dDirective = new DelegateDirective();
        response.setNullableShouldEndSession(false);
        response.setDirectives(Arrays.asList(dDirective));
        return response;
    }
   

}
