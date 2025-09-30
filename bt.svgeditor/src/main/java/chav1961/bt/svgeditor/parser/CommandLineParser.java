package chav1961.bt.svgeditor.parser;

import chav1961.bt.svgeditor.parser.Command.CommanType;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.CharUtils.Optional;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.SyntaxException;

/*
 * <Command> ::= {<SelectCommand>|<LineCommand>|<MoveCommand|<RotateCommand>|<ScaleCommand}
 * 
 * <SelectCommand> ::= 'select' {'+'|'-'} {'n'|'none'|'a'|'all'|'w'<Window>|'window'<Window>|'c'<Window>|'cross'<Window>}
 * 
 * <LineCommand> ::= 'line' <FromPoint> ['to'] <ToPoint>
 * <FromPoint> ::= {<AbsolutePoint>|'last'|'l']}
 * <ToPoint> ::= {<AbsolutePoint>|<RelativePoint>}
 *
 * <Window> ::= <AbsolutePoint> {<AbsolutePoint>|<RelativePoint>} 
 * <AbsolutePoint> ::= {<int>','<int>}
 * <RelativePoint> ::= {'@'<int>',''@'<int>}
 */
public class CommandLineParser {
	static final Command[]		COMMANDS = {
										new Command(CommanType.NEW_ENTITY, "line %1:point %2:point", "1", "1",
												(c,v)->{}, 
												ArgumentType.signedInt, ',', ArgumentType.signedInt, 
												new Optional("to"),
												ArgumentType.signedInt, ',', ArgumentType.signedInt 
												)
									};

	private static final char	EOF = '\uFFFF';
	
	private final Command[]	commands;
	
	public CommandLineParser() {
		this(COMMANDS);
	}

	public CommandLineParser(final Command... commands) {
		// TODO Auto-generated constructor stub
		if (commands == null || commands.length == 0 || Utils.checkArrayContent4Nulls(commands) >= 0) {
			throw new IllegalArgumentException("Commands is null, empty or contains nulls inside");
		}
		else {
			this.commands = commands;
		}
	}
	
	public void parse(final CharSequence descriptor) throws CommandLineParametersException {
		// TODO Auto-generated method stub
		final char[]	temp = CharUtils.terminateAndConvert2CharArray(descriptor, EOF);
		
		for(Command item : commands) {
			try{
				if (CharUtils.tryExtract(temp, 0, item.param) == temp.length) {
					final Object[]	values = new Object[100];
					
					CharUtils.extract(temp, 0, values, item.param);
					item.consumer.accept(item, values);
					return;
				}
			} catch (SyntaxException e) {
			}
		}
		throw new CommandLineParametersException("Unknown command");
	}
}
