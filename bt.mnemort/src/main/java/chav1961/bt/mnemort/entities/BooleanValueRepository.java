package chav1961.bt.mnemort.entities;

import java.io.IOException;

import chav1961.bt.mnemort.entities.BasicEntity.FieldNamesCollection;
import chav1961.bt.mnemort.entities.interfaces.BooleanSubscribableChangedListener;
import chav1961.bt.mnemort.entities.interfaces.BooleanSubscribableChangedListener.ChangedEventType;
import chav1961.bt.mnemort.entities.interfaces.BooleanSubscribableChangedListener.ChangedValueType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class BooleanValueRepository extends AbstractValueRepository implements JsonSerializable<BooleanValueRepository> {
	public static final String	F_CURRENT = "current";
	
	private static FieldNamesCollection	fieldsCollection = new FieldNamesCollection(F_CURRENT); 
	
	private final LightWeightListenerList<BooleanSubscribableChangedListener>	listeners = new LightWeightListenerList<>(BooleanSubscribableChangedListener.class); 
	private boolean	current = false;
	
	public BooleanValueRepository() {
	}
	
	
	@Override
	public void fromJson(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Json parser can't be null");
		}
		else {
			final FieldNamesCollection	coll = fieldsCollection.newInstance();
			boolean		_current = false;
			
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
loop:			for(JsonStaxParserLexType item : parser) {
					switch (item) {
						case NAME 		:
							BasicEntity.testDuplicate(parser, parser.name(), coll);
							switch (parser.name()) {
								case F_CURRENT	:
									_current = BasicEntity.checkAndExtractBoolean(parser);
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
					current = _current;
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
			printer.startObject().name(F_CURRENT).value(current).endObject();
		}
	}

	public void addSubscribableChangedListener(final BooleanSubscribableChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeSubscribableChangedListener(final BooleanSubscribableChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
	}

	public boolean getCurrent() {
		return current;
	}


	public void setCurrent(final boolean current) {
		final boolean	oldValue = this.current; 
		
		this.current = current;
		listeners.fireEvent((l)->{
			try{
				l.process(ChangedEventType.CHANGED, ChangedValueType.CURRENT, oldValue, current);
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
