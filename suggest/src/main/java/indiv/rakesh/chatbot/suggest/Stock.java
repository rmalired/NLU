/**
 * 
 */
package indiv.rakesh.chatbot.suggest;

/**
 * @author rakesh.malireddy
 *
 */
public class Stock {
	
	
	
	private final String init="analysis of stoc";

	/**
	 * 
	 */
	public Stock() {
		
	}
	
	private String tick;
	private double closePrice;	
	private String companyName;
	// indicates 5sma is greater than 20 sma
	private boolean shortTerm;
	//indicates 15 sma is greater than 40 sma
	private boolean mediumTerm;
	//indicates 50 sma is greater than 200 sma
	private boolean longTerm;
	//indicates whether stock close prices is above or below 50 sma
	private boolean stockOver50SMA;
	//indicates whether stock close price is above or below 20 sma
	private boolean stockOver20SMA;
	//intermediate ema
	private boolean expMediumTerm;
	//CCCIndicator
	private long cciIndicatorValue;
	//RSI2 indicator
	private long rsi2Value;
	//RSI2 trend
	private boolean rsi2Trend;
	//Linear regression
	private long slope;
	//linear regression trend
	private String slopetrend;
	//macd value
	private long macdValue;
	// macd trend
	private String macdTrend;
	//ADX - slope
	private String adxslope;
	//ADX -14 day value
	private long adxValue;
	//ADX positive
	private boolean adxTrend;
	//Stores the trailing indicator values
	private TrailingIndicators trailingIndicators;
	
	public String getTick() {
		return tick;
	}
	public void setTick(String tick) {
		this.tick = tick;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public boolean isShortTerm() {
		shortTerm = (trailingIndicators != null && trailingIndicators.getSma5() > trailingIndicators.getSma20())? true: false;		
		return shortTerm;
	}
	public void setShortTerm(boolean shortTerm) {
		this.shortTerm = shortTerm;
	}
	public boolean isMediumTerm() {
		return mediumTerm;
	}
	public void setMediumTerm(boolean mediumTerm) {
		this.mediumTerm = mediumTerm;
	}
	public boolean isLongTerm() {
		return longTerm;
	}
	public void setLongTerm(boolean longTerm) {
		this.longTerm = longTerm;
	}
	public boolean isStockOver50SMA() {
		return stockOver50SMA;
	}
	public void setStockOver50SMA(boolean stockOver50SMA) {
		this.stockOver50SMA = stockOver50SMA;
	}
	public boolean isExpMediumTerm() {
		return expMediumTerm;
	}
	public void setExpMediumTerm(boolean expMediumTerm) {
		this.expMediumTerm = expMediumTerm;
	}
	public long getCciIndicatorValue() {
		return cciIndicatorValue;
	}
	public void setCciIndicatorValue(long cciIndicatorValue) {
		this.cciIndicatorValue = cciIndicatorValue;
	}
	public long getRsi2Value() {
		return rsi2Value;
	}
	public void setRsi2Value(long rsi2Value) {
		this.rsi2Value = rsi2Value;
	}
	public long getSlope() {
		return slope;
	}
	public void setSlope(long slope) {
		this.slope = slope;
	}
	public String getSlopetrend() {
		return slopetrend;
	}
	public void setSlopetrend(String slopetrend) {
		this.slopetrend = slopetrend;
	}
	public long getMacdValue() {
		return macdValue;
	}
	public void setMacdValue(long macdValue) {
		this.macdValue = macdValue;
	}
	public String getMacdTrend() {
		return macdTrend;
	}
	public void setMacdTrend(String macdTrend) {
		this.macdTrend = macdTrend;
	}
	public String getAdxslope() {
		return adxslope;
	}
	public void setAdxslope(String adxslope) {
		this.adxslope = adxslope;
	}
	public long getAdxValue() {
		return adxValue;
	}
	public void setAdxValue(long adxValue) {
		this.adxValue = adxValue;
	}
	public boolean isAdxTrend() {
		return adxTrend;
	}
	public void setAdxTrend(boolean adxTrend) {
		this.adxTrend = adxTrend;
	}	
	public boolean isStockOver20SMA() {
		return stockOver20SMA;
	}
	public void setStockOver20SMA(boolean stockOver20SMA) {
		this.stockOver20SMA = stockOver20SMA;
	}
	public boolean isRsi2Trend() {
		return rsi2Trend;
	}
	public void setRsi2Trend(boolean rsi2Trend) {
		this.rsi2Trend = rsi2Trend;
	}
	public double getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}	
	public TrailingIndicators getTrailingIndicators() {
		return trailingIndicators;
	}
	public void setTrailingIndicators(TrailingIndicators trailingIndicators) {
		this.trailingIndicators = trailingIndicators;
	}
	
	
	public String shortTermAnalysis(){
		
		String rsi2Indicator ="Normal";
		rsi2Indicator =(rsi2Value > 80)? "Overbought": "Oversold";
		String rsi2IndicatorTrend = "Nuetral";
		rsi2IndicatorTrend =(rsi2Trend)? "improving" : "Declining";
		String priceAboveBelow = "above";
		if(trailingIndicators != null){
			priceAboveBelow = (closePrice>trailingIndicators.getSma20())?"above":"below";	
		}
		String mvgTrend = "Nuetral";
		mvgTrend = (isShortTerm())? "Upward": "Downward";
		
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("The short term moving average for ");
		sb.append(tick + " is ");
		sb.append(mvgTrend);
		sb.append("and the closing price is ");
		sb.append(priceAboveBelow);
		sb.append("20 day average.");
		
		
		/*sb.append("The short term RSI is indicating an ");
		sb.append(rsi2Indicator+" condition and the rsi trend is ");
		sb.append(rsi2IndicatorTrend);*/
		
		
		return sb.toString();
	}
	
	public String mediumTermAnalysis(){
		return "Not Implemented Yet";
	}
	
	public String longTermAnalysis(){
		return "Not Implemented Yet";
	}
	
	

}
