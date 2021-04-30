package chav1961.bt.mnemoed.entities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import chav1961.bt.mnemoed.interfaces.JsonSerialzable;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public abstract class EntityProp implements JsonSerialzable {
	private static final Map<Class<? extends ValueSource>, ArgType>	ARG_TYPES = new HashMap<>();
	private static final String	PLACE_HOLDER = "placeHolder";
	
	protected enum ArgType {
		PrimitiveConstant,
		PrimitiveSubscribable,
		PrimitiveExpression,
		ObjectConstant,
		ObjectSubscribable,
		ObjectExpression;
	}

	EntityProp() {
	}
	
	protected static ArgType getArgType(final Class<? extends ValueSource> clazz) {
		if (clazz == null) {
			throw new NullPointerException("Class to get type for can't be null"); 
		}
		else {
			return ARG_TYPES.get(clazz);
		}
	}
	
	protected static <T extends ValueSource> T createValueSourceByArgType(final String valueSourceType) {
		if (valueSourceType == null) {
			throw new NullPointerException("Value source type can't be null"); 
		}
		else {
			switch (ArgType.valueOf(valueSourceType)) {
				case ObjectConstant			: return (T) new ObjectConstantValueSource<String>(PLACE_HOLDER);
				case ObjectExpression		: return (T) new ObjectExpressionValueSource(PLACE_HOLDER);
				case ObjectSubscribable		: return (T) new ObjectSubscribableValueSource(PLACE_HOLDER);
				case PrimitiveConstant		: return (T) new PrimitiveConstantValueSource(CompilerUtils.CLASSTYPE_LONG,0);
				case PrimitiveExpression	: return (T) new PrimitiveExpressionValueSource(PLACE_HOLDER);
				case PrimitiveSubscribable	: return (T) new PrimitiveSubscribableValueSource(PLACE_HOLDER);
				default : throw new UnsupportedOperationException("Argument type ["+valueSourceType+"] is not supported yet");
			}
		}
	}
	
	protected PrimitiveValueSource parsePrimitiveValueSource(final JsonStaxParser parser) throws IOException, SyntaxException {
		if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
			if (parser.next() == JsonStaxParserLexType.NAME) {
				final PrimitiveValueSource	pvs = createValueSourceByArgType(parser.name());
				
				if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER && parser.next() == JsonStaxParserLexType.START_OBJECT) {
					pvs.download(parser);
				}
				if (parser.current() == JsonStaxParserLexType.END_OBJECT) {
					parser.next();
					return pvs;
				}
				else {
					throw new SyntaxException(parser.row(), parser.col(), "'}' is missing");
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "name awaiting");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "'{' is missing");
		}
	}

	protected ObjectValueSource parseObjectValueSource(final JsonStaxParser parser) throws IOException, SyntaxException {
		if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
			if (parser.next() == JsonStaxParserLexType.NAME) {
				final ObjectValueSource	ovs = createValueSourceByArgType(parser.name());
				
				if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER && parser.next() == JsonStaxParserLexType.START_OBJECT) {
					ovs.download(parser);
				}
				if (parser.current() == JsonStaxParserLexType.END_OBJECT) {
					parser.next();
					return ovs;
				}
				else {
					throw new SyntaxException(parser.row(), parser.col(), "'}' is missing");
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "name awaiting");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "'{' is missing");
		}
	}
}
