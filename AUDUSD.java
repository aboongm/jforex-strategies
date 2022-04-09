package jforex;

import java.util.Set;
import com.dukascopy.api.*;
import java.util.HashSet;

public class AUDUSD implements IStrategy {
    private IEngine engine;
    
    private IHistory history;
    private IContext context;
    private IConsole console;
    private IUserInterface userInterface;  
    private IIndicators indicators;
    private double amount; 
    private IBar prevBar;  
    private IOrder order;
    private static final int PREVIOUS = 1;
    private static final int LAST = 0;
    
    @Configurable(value="Instrument value") public Instrument currencyInstrument = Instrument.AUDUSD;    
    @Configurable("Amount") public double startingAmount = 1;
    @Configurable(value="Period value") public Period currencyPeriod = Period.TEN_MINS;
    @Configurable(value="Offer Side value", obligatory=true) public OfferSide currencyOfferSide = OfferSide.BID;    
    @Configurable("SMA time period") public int maTimePeriod = 80;    
    @Configurable("RSI Time Priod") public int rsiTimePeriod = 3;
    
    @Configurable("Stop loss in pips") public int stopLossPips = 22;
    @Configurable("Take profit in pips") public int takeProfitPips = 22;
    
    @Configurable("Break event pips") public double breakEventPips = 15; 
         
    
    
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.context = context;
        this.indicators = context.getIndicators();
        this.userInterface = context.getUserInterface();
        
        //subscribe to your choice of instrument:
        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(currencyInstrument);                     
        context.setSubscribedInstruments(instruments, true);
    }

    public void onAccount(IAccount account) throws JFException {
    }
       
    // messages related with orders from IMessages are print to strategy's output
    public void onMessage(IMessage message) throws JFException {        
        if(message.getOrder() != null) 
            console.getOut().println("order: " + message.getOrder().getLabel() + " || message content: " + message.getContent());
    }

    public void onStop() throws JFException {
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {   
        if (instrument != currencyInstrument) {
            return;
        }
        if (order != null && engine.getOrders().contains(order) && order.getProfitLossInPips() >= breakEventPips) {
            order.setStopLossPrice(order.getOpenPrice());
        }
    }
    
       
    // implement our trading logic here.
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {     
        
        if (!instrument.equals(currencyInstrument) || !period.equals(currencyPeriod)) {
            return; 
        }                 
                
        IEngine.OrderCommand myOrderCommand = null;
        int candleAfter = 0, candleBefore = 2;
        
        // get SMA values of the last two completed bars
        prevBar = (currencyOfferSide == OfferSide.BID ? bidBar : askBar);
        long currBarTime = prevBar.getTime();
        double sma[] = indicators.sma(instrument, period, currencyOfferSide, IIndicators.AppliedPrice.CLOSE, 
                maTimePeriod, Filter.NO_FILTER, candleBefore, currBarTime, candleAfter);
        double rsi3 = indicators.rsi(instrument, period, currencyOfferSide,
                IIndicators.AppliedPrice.CLOSE, rsiTimePeriod, 1);
              
        console.getOut().format("Bar SMA Values: Second-to-last = %.5f; Last Completed = %.5f", sma[LAST], sma[PREVIOUS]).println();        
        if (sma[PREVIOUS] > sma[LAST] && rsi3 < 15) {
            console.getOut().println("SMA in up-trend and RSI below 15"); 
            myOrderCommand = IEngine.OrderCommand.BUY;
        } else if (sma[PREVIOUS] < sma[LAST] && rsi3 > 85){
            console.getOut().println("SMA in down-trend and RSI above 85"); 
            myOrderCommand = IEngine.OrderCommand.SELL;
        } else {
            return;
        }
       
       
        // check the orders and decide to close the order if exist or submit new order if not
        order = engine.getOrder("MyOrder");                       
        if(order != null && engine.getOrders().contains(order) && order.getOrderCommand() != myOrderCommand){
            order.close();
            order.waitForUpdate(IOrder.State.CLOSED);             
            console.getOut().println("Order " + order.getLabel() + " is closed");            
        } 
                
        if (order == null || !engine.getOrders().contains(order)) {     
                        
            double lastTickBid = history.getLastTick(currencyInstrument).getBid();
            double lastTickAsk = history.getLastTick(currencyInstrument).getAsk();
            double stopLossValueForLong = currencyInstrument.getPipValue() * stopLossPips;
            double stopLossValueForShort = currencyInstrument.getPipValue() * takeProfitPips;
            double stopLossPrice = myOrderCommand.isLong() ? (lastTickBid - stopLossValueForLong) : (lastTickAsk + stopLossValueForLong);
            double takeProfitPrice = myOrderCommand.isLong() ? (lastTickBid + stopLossValueForShort) : (lastTickAsk - stopLossValueForShort);
                                                                  
            //engine.submitOrder("MyOrder", instrument, myOrderCommand, startingAmount);  
            engine.submitOrder("MyOrder", instrument, myOrderCommand, startingAmount, 0, 1, stopLossPrice, takeProfitPrice);                               
        }
    }    
}
