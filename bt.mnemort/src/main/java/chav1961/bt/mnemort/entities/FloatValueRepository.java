package chav1961.bt.mnemort.entities;

import java.io.IOException;

import chav1961.bt.mnemort.entities.BasicEntity.FieldNamesCollection;
import chav1961.bt.mnemort.entities.interfaces.FloatSubscribableChangedListener;
import chav1961.bt.mnemort.entities.interfaces.FloatSubscribableChangedListener.ChangedEventType;
import chav1961.bt.mnemort.entities.interfaces.FloatSubscribableChangedListener.ChangedValueType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class FloatValueRepository extends AbstractValueRepository implements JsonSerializable<FloatValueRepository> {
	public static final String	F_MINIMUM = "min";
	public static final String	F_MAXIMUM = "max";
	public static final String	F_CURRENT = "current";
	public static final String	F_PREV_CURRENT = "prev";
	public static final String	F_W_L_DOWN = "warnLowDown";
	public static final String	F_W_L_UP = "warnLowUp";
	public static final String	F_W_H_DOWN = "warnHighDown";
	public static final String	F_W_H_UP = "warnHighUp";
	public static final String	F_W_S_DOWN = "warnSpeedDown";
	public static final String	F_W_S_UP = "warnSpeedUp";
	public static final String	F_E_L_DOWN = "errLowDown";
	public static final String	F_E_L_UP = "errLowUp";
	public static final String	F_E_H_DOWN = "errHighDown";
	public static final String	F_E_H_UP = "errHighUp";
	public static final String	F_E_S_DOWN = "errSpeedDown";
	public static final String	F_E_S_UP = "errSpeedUp";

	private static FieldNamesCollection	fieldsCollection = new FieldNamesCollection(F_MINIMUM, F_MAXIMUM, F_CURRENT, F_PREV_CURRENT, F_W_L_DOWN, F_W_L_UP, F_W_H_DOWN, F_W_H_UP, F_W_S_DOWN, F_W_S_UP, F_E_L_DOWN, F_E_L_UP, F_E_H_DOWN, F_E_H_UP, F_E_S_DOWN, F_E_S_UP);
	
	private final LightWeightListenerList<FloatSubscribableChangedListener>	listeners = new LightWeightListenerList<>(FloatSubscribableChangedListener.class); 
	private float				minimum = 0,  current = 0, previousCurrent = 0, maximum = 100;
	private float				warningLowUp = 0, warningLowDown = 0, warningHighUp = 100, warningHighDown = 100, warningSpeedUp = 0, warningSpeedDown = 0; 
	private float				errorLowUp = 0, errorLowDown = 0, errorHighUp = 100, errorHighDown = 100, errorSpeedUp = 0, errorSpeedDown = 0;
	private ChangedEventType	lastEventType = ChangedEventType.CHANGED;

	public FloatValueRepository() {
	}

	@Override
	public void fromJson(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Json parser can't be null");
		}
		else {
			final FieldNamesCollection	coll = fieldsCollection.newInstance();
			float		_minimum = 0, _current = 0, _previousCurrent = 0, _maximum = 0;
			float		_warningLowUp = 0, _warningLowDown = 0, _warningHighUp = 100, _warningHighDown = 100, _warningSpeedUp = 0, _warningSpeedDown = 0; 
			float		_errorLowUp = 0, _errorLowDown = 0, _errorHighUp = 100, _errorHighDown = 100, _errorSpeedUp = 0, _errorSpeedDown = 0;
			
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
loop:			for(JsonStaxParserLexType item : parser) {
					switch (item) {
						case NAME 		:
							BasicEntity.testDuplicate(parser, parser.name(), coll);
							switch (parser.name()) {
								case F_MINIMUM		:
									_minimum = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_MAXIMUM		:
									_maximum = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_CURRENT		:
									_current = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_PREV_CURRENT	:
									_previousCurrent = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_W_L_DOWN		:
									_warningLowDown = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_W_L_UP		:
									_warningLowUp = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_W_H_DOWN		:
									_warningHighDown = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_W_H_UP		:
									_warningHighUp = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_W_S_DOWN		:
									_warningSpeedDown = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_W_S_UP		:
									_warningSpeedUp = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_E_L_DOWN		:
									_errorLowDown = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_E_L_UP		:
									_errorLowUp = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_E_H_DOWN		:
									_errorHighDown = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_E_H_UP		:
									_errorHighUp = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_E_S_DOWN		:
									_errorSpeedDown = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_E_S_UP		:
									_errorSpeedUp = BasicEntity.checkAndExtractFloat(parser);
									break;
								default :
									throw new SyntaxException(parser.row(), parser.col(), "Unsupported name ["+parser.name()+"]");
							}
							break;
						case LIST_SPLITTER :
							break;
						case END_OBJECT	:
							break loop;
						default :
							throw new SyntaxException(parser.row(), parser.col(), "Name or '}' awaited");
					}
				}
				parser.next();
				if (coll.areSomeFieldsMissing()) {
					throw new SyntaxException(parser.row(), parser.col(), "Mandatory field(s) ["+coll.getMissingNames()+"] are missing");
				}
				else {
					minimum = _minimum;
					current = _current;  
					previousCurrent = _previousCurrent;  
					maximum = _maximum;  
					warningLowUp = _warningLowUp;  
					warningLowDown = _warningLowDown;  
					warningHighUp = _warningHighUp; 
					warningHighDown = _warningHighDown;  
					warningSpeedUp = _warningSpeedUp; 
					warningSpeedDown = _warningSpeedDown;  
					errorLowUp = _errorLowUp;
					errorLowDown = _errorLowDown;  
					errorHighUp = _errorHighUp; 
					errorHighDown = _errorHighDown; 
					errorSpeedUp = _errorSpeedUp; 
					errorSpeedDown = _errorSpeedDown;  
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "Missing '{'");
			}
		}
	}

	@Override
	public void toJson(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Json printer can't be null");
		}
		else {
			printer.startObject().name(F_MINIMUM).value(minimum).splitter()
				.name(F_MAXIMUM).value(maximum).splitter()
				.name(F_CURRENT).value(current).splitter()
				.name(F_PREV_CURRENT).value(previousCurrent).splitter()
				.name(F_W_L_DOWN).value(warningLowDown).splitter()
				.name(F_W_L_UP).value(warningLowUp).splitter()
				.name(F_W_H_DOWN).value(warningHighDown).splitter()
				.name(F_W_H_UP).value(warningHighUp).splitter()
				.name(F_W_S_DOWN).value(warningSpeedDown).splitter()
				.name(F_W_S_UP).value(warningSpeedUp).splitter()
				.name(F_E_L_DOWN).value(errorLowDown).splitter()
				.name(F_E_L_UP).value(errorLowUp).splitter()
				.name(F_E_H_DOWN).value(errorHighDown).splitter()
				.name(F_E_H_UP).value(errorHighUp).splitter()
				.name(F_E_S_DOWN).value(errorSpeedDown).splitter()
				.name(F_E_S_UP).value(errorSpeedUp).endObject();
		}
	}
	
	public void addSubscribableChangedListener(final FloatSubscribableChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeSubscribableChangedListener(final FloatSubscribableChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
	}

	public float getMinimum() {
		return minimum;
	}

	public void setMinimum(final float minimum) {
		final float	oldValue = this.minimum; 
		
		this.minimum = minimum;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.MINIMUM, oldValue, minimum);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getCurrent() {
		return current;
	}

	public void setCurrent(final float current) {
		final float				oldValue = this.current; 
		final ChangedEventType 	type;
		
		this.current = current;
		
		switch (lastEventType) {
			case CHANGED		:
				if (current > getErrorHighUp()) {
					type = ChangedEventType.HIGH_ERROR; 
				}
				else if (current > getWarningHighUp()) {
					type = ChangedEventType.HIGH_WARNING; 
				}
				else if (current < getErrorLowUp()) {
					type = ChangedEventType.LOW_ERROR; 
				}
				else if (current < getWarningLowDown()) {
					type = ChangedEventType.LOW_WARNING; 
				}
				else {
					type = ChangedEventType.CHANGED;
				}
				break;
			case HIGH_ERROR		:
				if (current > getErrorHighDown()) {
					type = ChangedEventType.HIGH_ERROR; 
				}
				else if (current > getWarningHighDown()) {
					type = ChangedEventType.HIGH_WARNING; 
				}
				else if (current < getErrorLowDown()) {
					type = ChangedEventType.LOW_ERROR; 
				}
				else if (current < getWarningLowDown()) {
					type = ChangedEventType.LOW_WARNING; 
				}
				else {
					type = ChangedEventType.CHANGED;
				}
				break;
			case HIGH_WARNING	:
				if (current > getErrorHighUp()) {
					type = ChangedEventType.HIGH_ERROR; 
				}
				else if (current > getWarningHighDown()) {
					type = ChangedEventType.HIGH_WARNING; 
				}
				else if (current < getErrorLowDown()) {
					type = ChangedEventType.LOW_ERROR; 
				}
				else if (current < getWarningLowDown()) {
					type = ChangedEventType.LOW_WARNING; 
				}
				else {
					type = ChangedEventType.CHANGED;
				}
				break;
			case LOW_ERROR		:
				if (current > getErrorHighUp()) {
					type = ChangedEventType.HIGH_ERROR; 
				}
				else if (current > getWarningHighUp()) {
					type = ChangedEventType.HIGH_WARNING; 
				}
				else if (current < getErrorLowUp()) {
					type = ChangedEventType.LOW_ERROR; 
				}
				else if (current < getWarningLowDown()) {
					type = ChangedEventType.LOW_WARNING; 
				}
				else {
					type = ChangedEventType.CHANGED;
				}
				break;
			case LOW_WARNING	:
				if (current > getErrorHighUp()) {
					type = ChangedEventType.HIGH_ERROR; 
				}
				else if (current > getWarningHighUp()) {
					type = ChangedEventType.HIGH_WARNING; 
				}
				else if (current < getErrorLowUp()) {
					type = ChangedEventType.LOW_ERROR; 
				}
				else if (current < getWarningLowDown()) {
					type = ChangedEventType.LOW_WARNING; 
				}
				else {
					type = ChangedEventType.CHANGED;
				}
				break;
			case SPEED_ERROR	:
				if (current > getErrorHighUp()) {
					type = ChangedEventType.HIGH_ERROR; 
				}
				else if (current > getWarningHighUp()) {
					type = ChangedEventType.HIGH_WARNING; 
				}
				else if (current < getErrorLowUp()) {
					type = ChangedEventType.LOW_ERROR; 
				}
				else if (current < getWarningLowDown()) {
					type = ChangedEventType.LOW_WARNING; 
				}
				else {
					type = ChangedEventType.CHANGED;
				}
				break;
			case SPEED_WARNING	:
				if (current > getErrorHighUp()) {
					type = ChangedEventType.HIGH_ERROR; 
				}
				else if (current > getWarningHighUp()) {
					type = ChangedEventType.HIGH_WARNING; 
				}
				else if (current < getErrorLowUp()) {
					type = ChangedEventType.LOW_ERROR; 
				}
				else if (current < getWarningLowDown()) {
					type = ChangedEventType.LOW_WARNING; 
				}
				else {
					type = ChangedEventType.CHANGED;
				}
				break;
			default:
				throw new UnsupportedOperationException("Event change type ["+lastEventType+"] is not supported yet");
		}
		
		listeners.fireEvent((l)->{
			try{
				l.process(type, ChangedValueType.CURRENT, oldValue, current);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getPreviousCurrent() {
		return previousCurrent;
	}

	public void setPreviousCurrent(final float previousCurrent) {
		final float	oldValue = this.previousCurrent; 
		
		this.previousCurrent = previousCurrent;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.MINIMUM, oldValue, current);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getMaximum() {
		return maximum;
	}

	public void setMaximum(final float maximum) {
		final float	oldValue = this.maximum; 
		
		this.maximum = maximum;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.MAXIMUM, oldValue, maximum);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getWarningLowUp() {
		return warningLowUp;
	}

	public void setWarningLowUp(final float warningLowUp) {
		final float	oldValue = this.warningLowUp; 
		
		this.warningLowUp = warningLowUp;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.LOW_WARNING_UP, oldValue, warningLowUp);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getWarningLowDown() {
		return warningLowDown;
	}

	public void setWarningLowDown(final float warningLowDown) {
		final float	oldValue = this.warningLowDown; 
		
		this.warningLowDown = warningLowDown;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.LOW_WARNING_DOWN, oldValue, warningLowDown);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getWarningHighUp() {
		return warningHighUp;
	}

	public void setWarningHighUp(final float warningHighUp) {
		final float	oldValue = this.warningHighUp; 
		
		this.warningHighUp = warningHighUp;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.HIGH_WARNING_UP, oldValue, warningHighUp);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getWarningHighDown() {
		return warningHighDown;
	}

	public void setWarningHighDown(final float warningHighDown) {
		final float	oldValue = this.warningHighDown; 
		
		this.warningHighDown = warningHighDown;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.HIGH_WARNING_DOWN, oldValue, warningHighDown);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getWarningSpeedUp() {
		return warningSpeedUp;
	}

	public void setWarningSpeedUp(final float warningSpeedUp) {
		final float	oldValue = this.warningSpeedUp; 
		
		this.warningSpeedUp = warningSpeedUp;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.SPEED_WARNING_UP, oldValue, warningSpeedUp);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getWarningSpeedDown() {
		return warningSpeedDown;
	}

	public void setWarningSpeedDown(float warningSpeedDown) {
		final float	oldValue = this.warningSpeedDown; 
		
		this.warningSpeedDown = warningSpeedDown;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.SPEED_WARNING_DOWN, oldValue, warningSpeedDown);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getErrorLowUp() {
		return errorLowUp;
	}

	public void setErrorLowUp(final float errorLowUp) {
		final float	oldValue = this.errorLowUp; 
		
		this.errorLowUp = errorLowUp;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.LOW_ERROR_UP, oldValue, errorLowUp);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getErrorLowDown() {
		return errorLowDown;
	}

	public void setErrorLowDown(final float errorLowDown) {
		final float	oldValue = this.errorLowDown; 
		
		this.errorLowDown = errorLowDown;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.LOW_ERROR_DOWN, oldValue, errorLowDown);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getErrorHighUp() {
		return errorHighUp;
	}

	public void setErrorHighUp(final float errorHighUp) {
		final float	oldValue = this.errorHighUp; 
		
		this.errorHighUp = errorHighUp;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.HIGH_ERROR_UP, oldValue, errorHighUp);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getErrorHighDown() {
		return errorHighDown;
	}

	public void setErrorHighDown(float errorHighDown) {
		final float	oldValue = this.errorHighDown; 
		
		this.errorHighDown = errorHighDown;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.HIGH_ERROR_DOWN, oldValue, errorHighDown);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getErrorSpeedUp() {
		return errorSpeedUp;
	}

	public void setErrorSpeedUp(float errorSpeedUp) {
		final float	oldValue = this.errorSpeedUp; 
		
		this.errorSpeedUp = errorSpeedUp;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.SPEED_ERROR_UP, oldValue, errorSpeedUp);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	public float getErrorSpeedDown() {
		return errorSpeedDown;
	}

	public void setErrorSpeedDown(final float errorSpeedDown) {
		final float	oldValue = this.errorSpeedDown; 
		
		this.errorSpeedDown = errorSpeedDown;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.SPEED_ERROR_UP, oldValue, errorSpeedDown);
			} catch (ContentException exc) {
				processError(exc);
			}
		});
	}

	@Override	
	protected void processError(final ContentException exc) {
	}

	@Override
	protected void tick() {
	}
}
