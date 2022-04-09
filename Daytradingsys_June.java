package com.dukascopy.visualforex.mrlfx;

import java.util.*;
import com.dukascopy.api.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import java.lang.reflect.*;
import java.math.BigDecimal;


/*
 * Created by VisualJForex Generator, version 2.40
 * Date: 02.06.2017 01:51
 */
public class Daytradingsys_June implements IStrategy {

	private CopyOnWriteArrayList<TradeEventAction> tradeEventActions = new CopyOnWriteArrayList<TradeEventAction>();
	private static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";
	private IEngine engine;
	private IConsole console;
	private IHistory history;
	private IContext context;
	private IIndicators indicators;
	private IUserInterface userInterface;

	@Configurable("defaultTakeProfit:")
	public int defaultTakeProfit = 10;
	@Configurable("_StopL:")
	public int _StopL = 30;
	@Configurable("_MySHift2:")
	public int _MySHift2 = 2;
	@Configurable("_MyDefaultChart:")
	public Period _MyDefaultChart = Period.ONE_HOUR;
	@Configurable("_TradeStart1:")
	public double _TradeStart1 = 14.0;
	@Configurable("defaultTradeAmount:")
	public double defaultTradeAmount = 10.0;
	@Configurable("_MyTimPeriod:")
	public int _MyTimPeriod = 24;
	@Configurable("_MyShift1:")
	public int _MyShift1 = 1;
	@Configurable("_MyShift2:")
	public int _MyShift2 = 2;
	@Configurable("_TradeStart:")
	public double _TradeStart = 9.0;
	@Configurable("_ClosePositionHr:")
	public double _ClosePositionHr = 22.0;
	@Configurable("defaultInstrument:")
	public Instrument defaultInstrument = Instrument.EURUSD;
	@Configurable("defaultSlippage:")
	public int defaultSlippage = 5;
	@Configurable("defaultStopLoss:")
	public int defaultStopLoss = 10;
	@Configurable("defaultPeriod:")
	public Period defaultPeriod = Period.DAILY;

	private Tick LastTick =  null ;
	private IOrder _MySellPosition =  null ;
	private String AccountCurrency = "";
	private Candle candle35 =  null ;
	private double Leverage;
	private IOrder _MyBuyPosition =  null ;
	private IOrder _SellPosition =  null ;
	private double _MinCandlesLow3;
	private double _MinCandlesLow2;
	private int _CurrentTime;
	private double _MAXCandlesHigh3;
	private double _MAXcandlesHigh2;
	private Candle LastAskCandle =  null ;
	private Candle candle60 =  null ;
	private Candle LastBidCandle =  null ;
	private int _MyShift5 = 5;
	private String AccountId = "";
	private int _MyShift3 = 3;
	private int _MyShift4 = 4;
	private double Equity;
	private IOrder _BuyPosition =  null ;
	private Candle candle59 =  null ;
	private Candle candle58 =  null ;
	private int _CureentTIme2;
	private int OverWeekendEndLeverage;
	private Candle candle57 =  null ;
	private Candle candle12 =  null ;
	private IOrder _MyCurentPosition =  null ;
	private List<IOrder> PendingPositions =  null ;
	private int _TakeP = 30;
	private List<IOrder> OpenPositions =  null ;
	private double UseofLeverage;
	private IMessage LastTradeEvent =  null ;
	private boolean GlobalAccount;
	private double _MyPositionAmount = 0.0;
	private double _MAXCandleHigh;
	private int MarginCutLevel;
	private List<IOrder> AllPositions =  null ;
	private double _MinCandlesLow;


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

		subscriptionInstrumentCheck(Instrument.fromString("EUR/USD"));

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
			AllPositions = engine.getOrders();
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
		Period argument_1 = _MyDefaultChart;
		Period argument_2 = LastAskCandle.getPeriod();
		if (argument_1 == null && argument_2 !=null || (argument_1!= null && !argument_1.equals(argument_2))) {
		}
		else if (argument_1!= null && argument_1.equals(argument_2)) {
			If_block_11(flow);
		}
	}

	private  void If_block_11(Integer flow) {
		int argument_1 = OpenPositions.size();
		double argument_2 = _MyPositionAmount;
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
			GetTimeUnit_block_86(flow);
		}
		else if (argument_1== argument_2) {
			MultipleAction_block_77(flow);
		}
	}

	private  void If_block_13(Integer flow) {
		double argument_1 = LastAskCandle.getClose();
		double argument_2 = LastAskCandle.getOpen();
		if (argument_1< argument_2) {
			OpenatMarket_block_85(flow);
		}
		else if (argument_1> argument_2) {
			OpenatMarket_block_31(flow);
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void OpenatMarket_block_31(Integer flow) {
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
               _MySellPosition = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
		        } catch (JFException e) {
            e.printStackTrace();
        }
	}

	private  void GetTimeUnit_block_44(Integer flow) {
		long argument_1 = LastTick.getTime();
		Date date = new Date(argument_1);
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime(date);
		_CurrentTime = calendar.get(Calendar.HOUR_OF_DAY);
		If_block_45(flow);
	}

	private  void If_block_45(Integer flow) {
		int argument_1 = _CurrentTime;
		double argument_2 = _TradeStart;
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
		}
		else if (argument_1== argument_2) {
			If_block_13(flow);
		}
	}

	private  void MultipleAction_block_77(Integer flow) {
		GetTimeUnit_block_83(flow);
		GetTimeUnit_block_44(flow);
	}

	private  void If_block_79(Integer flow) {
		double argument_1 = LastAskCandle.getClose();
		double argument_2 = LastAskCandle.getOpen();
		if (argument_1< argument_2) {
			OpenatMarket_block_81(flow);
		}
		else if (argument_1> argument_2) {
			OpenatMarket_block_80(flow);
		}
		else if (argument_1== argument_2) {
		}
	}

	private  void OpenatMarket_block_80(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		double argument_2 = defaultTradeAmount;
		int argument_3 = defaultSlippage;
		int argument_4 = _StopL;
		int argument_5 = _TakeP;
		String argument_6 = "";
		ITick tick = getLastTick(argument_1);

		IEngine.OrderCommand command = IEngine.OrderCommand.SELL;

		double stopLoss = tick.getAsk() + argument_1.getPipValue() * argument_4;
		double takeProfit = round(tick.getAsk() - argument_1.getPipValue() * argument_5, argument_1);
		
           try {
               String label = getLabel();           
               _MySellPosition = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
		        } catch (JFException e) {
            e.printStackTrace();
        }
	}

	private  void OpenatMarket_block_81(Integer flow) {
		Instrument argument_1 = defaultInstrument;
		double argument_2 = defaultTradeAmount;
		int argument_3 = defaultSlippage;
		int argument_4 = _StopL;
		int argument_5 = _TakeP;
		String argument_6 = "";
		ITick tick = getLastTick(argument_1);

		IEngine.OrderCommand command = IEngine.OrderCommand.BUY;

		double stopLoss = tick.getBid() - argument_1.getPipValue() * argument_4;
		double takeProfit = round(tick.getBid() + argument_1.getPipValue() * argument_5, argument_1);
		
           try {
               String label = getLabel();           
               _MyBuyPosition = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
		        } catch (JFException e) {
            e.printStackTrace();
        }
	}

	private  void GetTimeUnit_block_83(Integer flow) {
		long argument_1 = LastTick.getTime();
		Date date = new Date(argument_1);
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime(date);
		_CureentTIme2 = calendar.get(Calendar.HOUR_OF_DAY);
		If_block_84(flow);
	}

	private  void If_block_84(Integer flow) {
		int argument_1 = _CureentTIme2;
		double argument_2 = _TradeStart1;
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
		}
		else if (argument_1== argument_2) {
			If_block_79(flow);
		}
	}

	private  void OpenatMarket_block_85(Integer flow) {
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
               _MyBuyPosition = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
		        } catch (JFException e) {
            e.printStackTrace();
        }
	}

	private  void GetTimeUnit_block_86(Integer flow) {
		long argument_1 = LastTick.getTime();
		Date date = new Date(argument_1);
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime(date);
		_CureentTIme2 = calendar.get(Calendar.HOUR_OF_DAY);
		If_block_87(flow);
	}

	private  void If_block_87(Integer flow) {
		int argument_1 = _CurrentTime;
		double argument_2 = _ClosePositionHr;
		if (argument_1< argument_2) {
		}
		else if (argument_1> argument_2) {
			MultipleAction_block_88(flow);
		}
		else if (argument_1== argument_2) {
			MultipleAction_block_88(flow);
		}
	}

	private  void MultipleAction_block_88(Integer flow) {
		CloseandCancelPosition_block_90(flow);
		CloseandCancelPosition_block_89(flow);
	}

	private  void CloseandCancelPosition_block_89(Integer flow) {
		try {
			if (_MyBuyPosition != null && (_MyBuyPosition.getState() == IOrder.State.OPENED||_MyBuyPosition.getState() == IOrder.State.FILLED)){
				_MyBuyPosition.close();
			}
		} catch (JFException e)  {
			e.printStackTrace();
		}
	}

	private  void CloseandCancelPosition_block_90(Integer flow) {
		try {
			if (_MySellPosition != null && (_MySellPosition.getState() == IOrder.State.OPENED||_MySellPosition.getState() == IOrder.State.FILLED)){
				_MySellPosition.close();
			}
		} catch (JFException e)  {
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