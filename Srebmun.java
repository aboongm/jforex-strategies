package com.dukascopy.visualforex.mrlfx;

import java.util.*;
import com.dukascopy.api.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import java.lang.reflect.*;
import java.math.BigDecimal;


/*
 * Created by VisualJForex Generator, version 1.65
 * Date: 06.02.2015 16:25
 */
public class Srebmun implements IStrategy {

	private CopyOnWriteArrayList<TradeEventAction> tradeEventActions = new CopyOnWriteArrayList<TradeEventAction>();
	private static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";
	private IEngine engine;
	private IConsole console;
	private IHistory history;
	private IContext context;
	private IIndicators indicators;
	private IUserInterface userInterface;

	@Configurable("defaultSlippage:")
	public int defaultSlippage = 5;
	@Configurable("defaultTakeProfit:")
	public int defaultTakeProfit = 60;
	@Configurable("defaultPeriod:")
	public Period defaultPeriod = Period.ONE_MIN;
	@Configurable("defaultTradeAmount:")
	public double defaultTradeAmount = 10.0;
	@Configurable("defaultStopLoss:")
	public int defaultStopLoss = 60;
	@Configurable("defaultInstrument:")
	public Instrument defaultInstrument = Instrument.EURAUD;

	private String AccountCurrency = "";
	private double Leverage;
	private Tick LastTick =  null ;
	private int _Period34 = 34;
	private String AccountId = "";
	private double Equity;
	private Candle candle18 =  null ;
	private double UseofLeverage;
	private Candle candle14 =  null ;
	private Candle candle15 =  null ;
	private List<IOrder> PendingPositions =  null ;
	private Candle candle16 =  null ;
	private Candle candle17 =  null ;
	private int _Shift2 = 2;
	private int _Shift1 = 1;
	private double _EMA34_0;
	private int _Shift0 = 0;
	private double _EMA34_1;
	private List<IOrder> AllPositions =  null ;
	private int _OpenPositionAmt = 0;
	private int OverWeekendEndLeverage;
	private int MarginCutLevel;
	private Candle LastAskCandle =  null ;
	private int _Shift3 = 3;
	private boolean GlobalAccount;
	private List<IOrder> OpenPositions =  null ;
	private Period _ChartH1 = Period.ONE_HOUR;
	private IMessage LastTradeEvent =  null ;
	private Candle LastBidCandle =  null ;


	public void onStart(IContext context) throws JFException {
		this.engine = context.getEngine();
		this.console = context.getConsole();
		this.history = context.getHistory();
		this.context = context;
		this.indicators = context.getIndicators();
		this.userInterface = context.getUserInterface();

		subscriptionInstrumentCheck(defaultInstrument);

		ITick lastITick = context.getHistory().getLastTick(defaultInstrument);
		LastTick = new Tick(lastITick, defaultInstrument);

		IBar bidBar = context.getHistory().getBar(defaultInstrument, defaultPeriod, OfferSide.BID, 1);
		IBar askBar = context.getHistory().getBar(defaultInstrument, defaultPeriod, OfferSide.ASK, 1);
		LastAskCandle = new Candle(askBar, defaultPeriod, defaultInstrument, OfferSide.ASK);
		LastBidCandle = new Candle(bidBar, defaultPeriod, defaultInstrument, OfferSide.BID);

		if (indicators.getIndicator("EMA") == null) {
			indicators.registerDownloadableIndicator("1324","EMA");
		}
		if (indicators.getIndicator("EMA") == null) {
			indicators.registerDownloadableIndicator("1324","EMA");
		}
		subscriptionInstrumentCheck(Instrument.fromString("EUR/AUD"));

	}

	public void onAccount(IAccount account) throws JFException {
		AccountCurrency = account.getCurrency().toString();
		Leverage = account.getLeverage();
		AccountId= account.getAccountId();
		Equity = account.getEquity();
		UseofLeverage = account.getUseOfLeverage();
		OverWeekendEndLeverage = account.getOverWeekEndLeverage();
		MarginCutLevel = account.getMarginCutLevel();
		GlobalAccount = account.isGlobal();
	}

	private void updateVariables(Instrument instrument) {
		try {
			AllPositions = engine.getOrders(instrument);
	        List<IOrder> listMarket = new ArrayList<IOrder>();
	        for (IOrder order: AllPositions) {
	            if (order.getState().equals(IOrder.State.FILLED)){
	                listMarket.add(order);
	            }
	        }
	        List<IOrder> listPending = new ArrayList<IOrder>();
	        for (IOrder order: AllPositions) {
	            if (order.getState().equals(IOrder.State.OPENED)){
	                listPending.add(order);
	            }
	        }
			OpenPositions = listMarket;
			PendingPositions = listPending;
		} catch(JFException e) {
			e.printStackTrace();
		}
	}

	public void onMessage(IMessage message) throws JFException {
		if (message.getOrder() != null) {
			updateVariables(message.getOrder().getInstrument());
			LastTradeEvent = message;
			for (TradeEventAction event :  tradeEventActions) {
				IOrder order = message.getOrder();
				if (order != null && event != null && message.getType().equals(event.getMessageType())&& order.getLabel().equals(event.getPositionLabel())) {
					Method method;
					try {
						method = this.getClass().getDeclaredMethod(event.getNextBlockId(), Integer.class);
						method.invoke(this, new Integer[] {event.getFlowId()});
					} catch (SecurityException e) {
							e.printStackTrace();
					} catch (NoSuchMethodException e) {
						  e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} 
					tradeEventActions.remove(event); 
				}
			}   
		}
	}

	public void onStop() throws JFException {
	}

	public void onTick(Instrument instrument, ITick tick) throws JFException {
			LastTick = new Tick(tick, instrument);
		updateVariables(instrument);


	}

	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
		LastAskCandle = new Candle(askBar, period, instrument, OfferSide.ASK);
		LastBidCandle = new Candle(bidBar, period, instrument, OfferSide.BID);
		updateVariables(instrument);
			If_block_10(1);

	}

    public void subscriptionInstrumentCheck(Instrument instrument) {
		try {
		      if (!context.getSubscribedInstruments().contains(instrument)) {
		          Set<Instrument> instruments = new HashSet<Instrument>();
		          instruments.add(instrument);
		          context.setSubscribedInstruments(instruments, true);
		          Thread.sleep(100);
		      }
		  } catch (InterruptedException e) {
		      e.printStackTrace();
		  }
		}

    public double round(double price, Instrument instrument) {
		BigDecimal big = new BigDecimal("" + price); 
		big = big.setScale(instrument.getPipScale() + 1, BigDecimal.ROUND_HALF_UP); 
		return big.doubleValue(); 
	}

    public ITick getLastTick(Instrument instrument) {
		try { 
			return (context.getHistory().getTick(instrument, 0)); 
		} catch (JFException e) { 
			 e.printStackTrace();  
		 } 
		 return null; 
	}

	private  void If_block_10(Integer flow) {
		Period argument_1 = defaultPeriod;
		Period argument_2 = LastAskCandle.getPeriod();
		if (!argument_1.equals(argument_2)) {
		}
		else if (argument_1.equals(argument_2)) {
			If_block_13(flow);
		}
	}

	private void EMA_block_11(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		Period argument_2 = _ChartH1;
		int argument_3 = _Shift0;
		int argument_4 = _Period34;
		OfferSide[] offerside = new OfferSide[1];
		IIndicators.AppliedPrice[] appliedPrice = new IIndicators.AppliedPrice[1];
		offerside[0] = OfferSide.BID;
		appliedPrice[0] = IIndicators.AppliedPrice.TYPICAL_PRICE;
		Object[] params = new Object[1];
		params[0] = _Period34;
		try {
			subscriptionInstrumentCheck(argument_1);
			long time = context.getHistory().getBar(argument_1, argument_2, OfferSide.BID, argument_3).getTime();
			Object[] indicatorResult = context.getIndicators().calculateIndicator(argument_1, argument_2, offerside,
					"EMA", appliedPrice, params, Filter.WEEKENDS, 1, time, 0);
			if ((new Double(((double [])indicatorResult[0])[0])) == null) {
				this._EMA34_0 = Double.NaN;
			} else { 
				this._EMA34_0 = (((double [])indicatorResult[0])[0]);
			} 
		} catch (JFException e) {
			e.printStackTrace();
		}
		EMA_block_12(flow);
	}

	private void EMA_block_12(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		Period argument_2 = _ChartH1;
		int argument_3 = _Shift1;
		int argument_4 = _Period34;
		OfferSide[] offerside = new OfferSide[1];
		IIndicators.AppliedPrice[] appliedPrice = new IIndicators.AppliedPrice[1];
		offerside[0] = OfferSide.BID;
		appliedPrice[0] = IIndicators.AppliedPrice.TYPICAL_PRICE;
		Object[] params = new Object[1];
		params[0] = _Period34;
		try {
			subscriptionInstrumentCheck(argument_1);
			long time = context.getHistory().getBar(argument_1, argument_2, OfferSide.BID, argument_3).getTime();
			Object[] indicatorResult = context.getIndicators().calculateIndicator(argument_1, argument_2, offerside,
					"EMA", appliedPrice, params, Filter.WEEKENDS, 1, time, 0);
			if ((new Double(((double [])indicatorResult[0])[0])) == null) {
				this._EMA34_1 = Double.NaN;
			} else { 
				this._EMA34_1 = (((double [])indicatorResult[0])[0]);
			} 
		} catch (JFException e) {
			e.printStackTrace();
		}
		GetHistoricalCandle_block_14(flow);
	}

	private  void If_block_13(Integer flow) {
		int argument_1 = OpenPositions.size();
		int argument_2 = _OpenPositionAmt;
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
		}
		else if (argument_1== argument_2) {
			EMA_block_11(flow);
		}
	}

	private  void GetHistoricalCandle_block_14(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		Period argument_2 = _ChartH1;
		OfferSide argument_3 = OfferSide.BID;
		int argument_4 = _Shift1;
			subscriptionInstrumentCheck(argument_1);
 		
        try {
			IBar tempBar = history.getBar(argument_1, argument_2, argument_3, argument_4); 
			candle14 = new Candle(tempBar, argument_2, argument_1, argument_3); 
        } catch (JFException e) {
            e.printStackTrace();
        }
		GetHistoricalCandle_block_17(flow);
	}

	private  void GetHistoricalCandle_block_17(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		Period argument_2 = _ChartH1;
		OfferSide argument_3 = OfferSide.BID;
		int argument_4 = _Shift2;
			subscriptionInstrumentCheck(argument_1);
 		
        try {
			IBar tempBar = history.getBar(argument_1, argument_2, argument_3, argument_4); 
			candle17 = new Candle(tempBar, argument_2, argument_1, argument_3); 
        } catch (JFException e) {
            e.printStackTrace();
        }
		GetHistoricalCandle_block_18(flow);
	}

	private  void GetHistoricalCandle_block_18(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		Period argument_2 = _ChartH1;
		OfferSide argument_3 = OfferSide.BID;
		int argument_4 = _Shift3;
			subscriptionInstrumentCheck(argument_1);
 		
        try {
			IBar tempBar = history.getBar(argument_1, argument_2, argument_3, argument_4); 
			candle18 = new Candle(tempBar, argument_2, argument_1, argument_3); 
        } catch (JFException e) {
            e.printStackTrace();
        }
		If_block_19(flow);
	}

	private  void If_block_19(Integer flow) {
		double argument_1 = LastAskCandle.getOpen();
		double argument_2 = _EMA34_0;
		if (argument_1< argument_2) {
			If_block_20(flow);
		}
		else if (argument_1> argument_2) {
			If_block_24(flow);
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void If_block_20(Integer flow) {
		double argument_1 = candle14.getHigh();
		double argument_2 = _EMA34_1;
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
			If_block_21(flow);
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void If_block_21(Integer flow) {
		double argument_1 = candle17.getHigh();
		double argument_2 = candle18.getHigh();
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
			If_block_22(flow);
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void If_block_22(Integer flow) {
		double argument_1 = candle14.getLow();
		double argument_2 = candle17.getLow();
		if (argument_1< argument_2) {
			OpenatMarket_block_23(flow);
		}
		else if (argument_1> argument_2) {
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void OpenatMarket_block_23(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		double argument_2 = defaultTradeAmount;
		int argument_3 = defaultSlippage;
		int argument_4 = defaultStopLoss;
		int argument_5 = defaultTakeProfit;
		String argument_6 = "";
		ITick tick = getLastTick(argument_1);

		IEngine.OrderCommand command = IEngine.OrderCommand.SELL;

		double stopLoss = tick.getAsk() + argument_1.getPipValue() * argument_4;
		double takeProfit = round(tick.getAsk() - argument_1.getPipValue() * argument_5, argument_1);
		
        try {
            String label = getLabel();           
            IOrder order = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
		        } catch (JFException e) {
            e.printStackTrace();
        }
	}

	private  void If_block_24(Integer flow) {
		double argument_1 = candle14.getLow();
		double argument_2 = _EMA34_1;
		if (argument_1< argument_2) {
			If_block_25(flow);
		}
		else if (argument_1> argument_2) {
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void If_block_25(Integer flow) {
		double argument_1 = candle17.getLow();
		double argument_2 = candle18.getLow();
		if (argument_1< argument_2) {
			If_block_26(flow);
		}
		else if (argument_1> argument_2) {
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void If_block_26(Integer flow) {
		double argument_1 = candle14.getHigh();
		double argument_2 = candle17.getHigh();
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
			OpenatMarket_block_27(flow);
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void OpenatMarket_block_27(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		double argument_2 = defaultTradeAmount;
		int argument_3 = defaultSlippage;
		int argument_4 = defaultStopLoss;
		int argument_5 = defaultTakeProfit;
		String argument_6 = "";
		ITick tick = getLastTick(argument_1);

		IEngine.OrderCommand command = IEngine.OrderCommand.BUY;

		double stopLoss = tick.getBid() - argument_1.getPipValue() * argument_4;
		double takeProfit = round(tick.getBid() + argument_1.getPipValue() * argument_5, argument_1);
		
        try {
            String label = getLabel();           
            IOrder order = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
		        } catch (JFException e) {
            e.printStackTrace();
        }
	}

class Candle  {

    IBar bar;
    Period period;
    Instrument instrument;
    OfferSide offerSide;

    public Candle(IBar bar, Period period, Instrument instrument, OfferSide offerSide) {
        this.bar = bar;
        this.period = period;
        this.instrument = instrument;
        this.offerSide = offerSide;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public OfferSide getOfferSide() {
        return offerSide;
    }

    public void setOfferSide(OfferSide offerSide) {
        this.offerSide = offerSide;
    }

    public IBar getBar() {
        return bar;
    }

    public void setBar(IBar bar) {
        this.bar = bar;
    }

    public long getTime() {
        return bar.getTime();
    }

    public double getOpen() {
        return bar.getOpen();
    }

    public double getClose() {
        return bar.getClose();
    }

    public double getLow() {
        return bar.getLow();
    }

    public double getHigh() {
        return bar.getHigh();
    }

    public double getVolume() {
        return bar.getVolume();
    }
}
class Tick {

    private ITick tick;
    private Instrument instrument;

    public Tick(ITick tick, Instrument instrument){
        this.instrument = instrument;
        this.tick = tick;
    }

    public Instrument getInstrument(){
       return  instrument;
    }

    public double getAsk(){
       return  tick.getAsk();
    }

    public double getBid(){
       return  tick.getBid();
    }

    public double getAskVolume(){
       return  tick.getAskVolume();
    }

    public double getBidVolume(){
        return tick.getBidVolume();
    }

   public long getTime(){
       return  tick.getTime();
    }

   public ITick getTick(){
       return  tick;
    }
}

public class AssertException extends RuntimeException {

    public AssertException(Object primary, Object compared) {
        super("Primary object : " + primary.toString() + " is different from " + compared.toString());
    }
}
    protected String getLabel() {
        String label;
        label = "IVF" + getCurrentTime(LastTick.getTime()) + generateRandom(10000) + generateRandom(10000);
        return label;
    }

    private String getCurrentTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(time);
    }

    private static String generateRandom(int n) {
        int randomNumber = (int) (Math.random() * n);
        String answer = "" + randomNumber;
        if (answer.length() > 3) {
            answer = answer.substring(0, 4);
        }
        return answer;
    }

    class TradeEventAction {
		private IMessage.Type messageType;
		private String nextBlockId = "";
		private String positionLabel = "";
		private int flowId = 0;

        public IMessage.Type getMessageType() {
            return messageType;
        }

        public void setMessageType(IMessage.Type messageType) {
            this.messageType = messageType;
        }

        public String getNextBlockId() {
            return nextBlockId;
        }

        public void setNextBlockId(String nextBlockId) {
            this.nextBlockId = nextBlockId;
        }
        public String getPositionLabel() {
            return positionLabel;
       }

        public void setPositionLabel(String positionLabel) {
            this.positionLabel = positionLabel;
        }
        public int getFlowId() {
            return flowId;
        }
        public void setFlowId(int flowId) {
            this.flowId = flowId;
        }
    }
}