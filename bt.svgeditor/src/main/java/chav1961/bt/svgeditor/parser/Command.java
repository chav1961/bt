package chav1961.bt.svgeditor.parser;

import java.util.function.BiConsumer;

import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class Command {
	public static enum CommandType {
		NEW_ENTITY,
		TRANSFORM_ENTITY,
		REMOVE_ENTITY,
		PROPERTIES_CHANGE,
		SELECTION
	}
	
	public static interface CommandConsumer {
		void accept(CommandLineParser parser, SVGCanvas canvas, Command command, Object... parameters) throws CommandLineParametersException, CalculationException; 
	}
	
	// <Descriptor>	::= <Name>[<Parameter>...]
	// <Name>		::= <Letter>[<LetterOrDigit>...]
	// <Parameter>	::= <Name>':'<Type>['='[<DefaultValue>]]
	// <TypeName>	::= {'int'|'real'|'point'|'rect'|'text'|'color'|'enum('<EnumRef>')'}
	// <Type>		::= {<int>|<real>|<point>|<rect>|<text>|<color>|<enumRef>}
	// <int>		::= {'+'|'-'}<digit>[<digit>...]
	// <real>		::= {'+'|'-'}<int>['.'<int>]
	// <point>		::= <int>','<int>
	// <rect>		::= <int>','<int>','<int>','<int>
	// <text>		::= '"'<chars><escapes>'"'
	// <color>		::= {<Name>|'#"<int>}
	// <enumRef>	::= <Name>
	// Example:
	//	line from:point to:point color:color=green
	
	private final CommandType	type;
	private final String		commandName;
	private final String		descriptor;
	private final String		helpPrefix;
	private final String		helpReference;
	final CommandConsumer		consumer;
	final Object[]				param;
	
	public Command(final CommandType type, final String commandName, final String descriptor, final String helpPrefix, final String helpReference, final CommandConsumer processor, final Object... parameters) {
		if (type == null) {
			throw new NullPointerException("Commnad type can't be null");
		}
		else if (Utils.checkEmptyOrNullString(commandName)) {
			throw new IllegalArgumentException("Command name can be neither null nor empty");
		}
		else if (Utils.checkEmptyOrNullString(descriptor)) {
			throw new IllegalArgumentException("Command descriptor can be neither null nor empty");
		}
		else if (Utils.checkEmptyOrNullString(helpPrefix)) {
			throw new IllegalArgumentException("Help prefix can be neither null nor empty");
		}
		else if (Utils.checkEmptyOrNullString(helpReference)) {
			throw new IllegalArgumentException("Help reference can be neither null nor empty");
		}
		else if (processor == null) {
			throw new NullPointerException("Processor can't be null");
		}
		else if (parameters == null || parameters.length == 0 || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Command parameters is null, empty or contains nulls inside");
		}
		else {
			this.type = type;
			this.commandName = commandName;
			this.descriptor = descriptor;
			this.helpPrefix = helpPrefix;
			this.helpReference = helpReference;
			this.consumer = processor;
			this.param = parameters;
		}
	}
	
	public CommandType getType() {
		return type;
	}

	public String getCommandName() {
		return commandName;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public String getHelpPrefix() {
		return helpPrefix;
	}

	public String getHelpReference() {
		return helpReference;
	}

	
}
