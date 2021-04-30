package chav1961.bt.mnemoed.entities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public final class ObjectConstantValueSource<T> extends ObjectValueSource {
	private T	value;
	
	public ObjectConstantValueSource(final T value) throws NullPointerException {
		super(ValueSourceType.REF_CONST);
		if (value == null) {
			throw new NullPointerException("Constant value can't be null"); 
		}
		else {
			this.value = value;
		}
	}
	
	public T getObjectValue() {
		return value;
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name("valType").value(value.getClass().getCanonicalName()).name("value").value(toString((Class<T>)value.getClass(),value)).endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
				JsonStaxParserLexType	lexType;
				Class					clazz = null;
				
				do {lexType = parser.next();
					if (lexType == JsonStaxParserLexType.NAME) {
						switch (parser.name()) {
							case "valType" :
								if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER && parser.next() == JsonStaxParserLexType.STRING_VALUE) {
									try{clazz = Class.forName(parser.stringValue());
									} catch (ClassNotFoundException e) {
										throw new SyntaxException(parser.row(), parser.col(), "Unknown class ["+parser.stringValue()+"] for object constant");
									}
									lexType = parser.next();
								}
								else {
									throw new SyntaxException(parser.row(), parser.col(), "Structure corruption (integer awaited)");
								}
								break;
							case "value" :
								if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER && parser.next() == JsonStaxParserLexType.STRING_VALUE) {
									if (clazz != null) {
										try{value = (T)fromString(clazz, parser.stringValue());
										} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
											throw new SyntaxException(parser.row(), parser.col(), "Error converting value ["+parser.stringValue()+"] to class ["+clazz.getCanonicalName()+"] : "+e.getLocalizedMessage());
										}
										lexType = parser.next();
									}
									else {
										throw new SyntaxException(parser.row(), parser.col(), "Structure corruption (field 'valType' must preceeded 'value')");
									}
								}
								else {
									throw new SyntaxException(parser.row(), parser.col(), "Structure corruption (integer awaited)");
								}
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "Unsupported name. Only 'valType' and 'value' are valid here");
						}
					}
					else {
						throw new SyntaxException(parser.row(), parser.col(), "field name is missing");
					}
				} while (lexType == JsonStaxParserLexType.LIST_SPLITTER);
				if (lexType == JsonStaxParserLexType.END_OBJECT) {
					parser.next();
				}
				else {
					throw new SyntaxException(parser.row(), parser.col(), "'}' is missing");
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "'{' is missing");
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ObjectConstantValueSource<T> other = (ObjectConstantValueSource<T>) obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectConstantValueSource [value=" + value + ", getSourceType()=" + getSourceType() + "]";
	}

	private static <T> String toString(final Class<T> clazz, final T instance) {
		return instance.toString();
	}
	
	private static <T> T fromString(final Class<T> clazz, final String content) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final Method	m = clazz.getMethod("valueOf", String.class);
			
		return (T)m.invoke(null, content);
	}
	
}
