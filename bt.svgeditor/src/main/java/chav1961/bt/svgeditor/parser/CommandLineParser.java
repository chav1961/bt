package chav1961.bt.svgeditor.parser;


import java.util.Arrays;

import chav1961.bt.svgeditor.parser.Command.CommandType;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.CharUtils.Optional;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.CharUtils.Choise;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
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
	private static final Command[]	COMMANDS = {
										new Command(CommandType.MENU, "help", 
												"help", "1", "1",
												(parser,canvas,command,parameters)->{
													new HelpProcessor(parameters).execute(canvas);
												}, new Mark(1)),
										new Command(CommandType.MENU, "new", 
												"new", "1", "1",
												(parser,canvas,command,parameters)->{
													new MenuItemProcessor("action:/newImage").execute(canvas);
												}, new Mark(1)),
										new Command(CommandType.MENU, "q[uit]", 
												"q[uit]", "1", "1",
												(parser,canvas,command,parameters)->{
													new MenuItemProcessor("action:/exit").execute(canvas);
												}, new Mark(1)),
										new Command(CommandType.MENU, "u[ndo]", 
												"u[ndo]", "1", "1",
												(parser,canvas,command,parameters)->{
													new MenuItemProcessor("action:/undo").execute(canvas);
												}, new Mark(1)),
										new Command(CommandType.MENU, "r[edo]", 
												"r[edo]", "1", "1",
												(parser,canvas,command,parameters)->{
													new MenuItemProcessor("action:/redo").execute(canvas);
												}, new Mark(1)),
										new Command(CommandType.MENU, "set", 
												"set [%1:string = %2:string]", "1", "1",
												(parser,canvas,command,parameters)->{
													new SetProcessor("action:/settings", parameters).execute(canvas);
												}, 
												new Optional(ArgumentType.dottedName, '=', ArgumentType.raw) 
												),
										new Command(CommandType.NEW_ENTITY, "l[ine]", 
												"l[ine] %1:point [to] [@]%2:point", "1", "1",
												(parser,canvas,command,parameters)->{
													new LineProcessor(parameters).execute(canvas);
												}, 
												ArgumentType.signedInt, ',', ArgumentType.signedInt, 
												new Optional("to"),
												new Optional("@", new Mark(1)),
												ArgumentType.signedInt, ',', ArgumentType.signedInt 
												),
										new Command(CommandType.TRANSFORM_ENTITY, "m[ove]", 
												"m[ove] {a[ll]|l[ast]|sel[ected]} %1:point [to] [@]%2:point\"", "1", "1",
												(parser,canvas,command,parameters)->{
													new MoveProcessor(parameters).execute(canvas);
												}, 
												new Choise(
													new Object[] {
														"all", new Mark(1)	
													},
													new Object[] {
														"a", new Mark(1)	
													},
													new Object[] {
														"last", new Mark(2)	
													},
													new Object[] {
														"l", new Mark(2)	
													},
													new Object[] {
														"selected", new Mark(3)	
													},
													new Object[] {
														"sel", new Mark(3)	
													}
												),
												ArgumentType.signedInt, ',', ArgumentType.signedInt, 
												new Optional("to"),
												new Optional("@", new Mark(4)),
												ArgumentType.signedInt, ',', ArgumentType.signedInt 
												),
										new Command(CommandType.REMOVE_ENTITY, "del[ete]", 
												"del[ete] {a[ll]|l[ast]|sel[ected]}", "1", "1",
												(parser,canvas,command,parameters)->{
													new DeleteProcessor(parameters).execute(canvas);
												}, 
												new Choise(
													new Object[] {
														"all", new Mark(1)	
													},
													new Object[] {
														"a", new Mark(1)	
													},
													new Object[] {
														"last", new Mark(2)	
													},
													new Object[] {
														"l", new Mark(2)	
													},
													new Object[] {
														"selected", new Mark(3)	
													},
													new Object[] {
														"sel", new Mark(3)	
													}
												)
												),
										new Command(CommandType.TRANSFORM_ENTITY, "copy", 
												"copy {a[ll]|l[ast]|sel[ected]}", "1", "1",
												(parser,canvas,command,parameters)->{
													new CopyProcessor(false, parameters).execute(canvas);
												}, 
												new Choise(
													new Object[] {
														"all", new Mark(1)	
													},
													new Object[] {
														"a", new Mark(1)	
													},
													new Object[] {
														"last", new Mark(2)	
													},
													new Object[] {
														"l", new Mark(2)	
													},
													new Object[] {
														"selected", new Mark(3)	
													},
													new Object[] {
														"sel", new Mark(3)	
													}
												)
												),
										new Command(CommandType.REMOVE_ENTITY, "cut", 
												"cut {a[ll]|l[ast]|sel[ected]}", "1", "1",
												(parser,canvas,command,parameters)->{
													new CopyProcessor(true, parameters).execute(canvas);
												}, 
												new Choise(
													new Object[] {
														"all", new Mark(1)	
													},
													new Object[] {
														"a", new Mark(1)	
													},
													new Object[] {
														"last", new Mark(2)	
													},
													new Object[] {
														"l", new Mark(2)	
													},
													new Object[] {
														"selected", new Mark(3)	
													},
													new Object[] {
														"sel", new Mark(3)	
													}
												)
												),
										new Command(CommandType.NEW_ENTITY, "paste", 
												"paste [%1:point]", "1", "1",
												(parser,canvas,command,parameters)->{
													new PasteProcessor(parameters).execute(canvas);
												}, 
												new Optional(ArgumentType.signedInt, ',', ArgumentType.signedInt),
												new Mark(1)),
										new Command(CommandType.SELECTION, "sel[ect]", 
												"sel[ect] {n[one]|a[ll]|{[+]|[-]}{w[indow] %1:point [@] %2:point|c[rossing] %1:point [@] %2:point|l[ast]|at %1:point [%2:int]}", "1", "1",
												(parser,canvas,command,parameters)->{
													new SelectionProcessor(parameters).execute(canvas);
												}, 
												new Choise(
													new Object[] {
														"none", new Mark(1)	
													},
													new Object[] {
														"n", new Mark(1)	
													},
													new Object[] {
														"all", new Mark(2)	
													},
													new Object[] {
														"a", new Mark(2)	
													},
													new Object[] {
														new Choise(
															new Object[] {"+", new Mark(3)},
															new Object[] {"-", new Mark(4)},
															new Object[] {new Mark(5)}
														),
														new Choise(
															new Object[] {
																new Choise(
																	"window", "w" 
																),
																new Mark(6),
																ArgumentType.signedInt, ',', ArgumentType.signedInt,
																new Optional("@", new Mark(10)),
																ArgumentType.signedInt, ',', ArgumentType.signedInt
															},
															new Object[] {
																new Choise(
																	"crossing", "c" 
																),
																new Mark(7),
																ArgumentType.signedInt, ',', ArgumentType.signedInt,
																new Optional("@", new Mark(10)),
																ArgumentType.signedInt, ',', ArgumentType.signedInt
															},
															new Object[] {
																new Choise(
																	"last", "l"
																),
																new Mark(8),
															},
															new Object[] {
																"at",
																new Mark(9),
																ArgumentType.signedInt, ',', ArgumentType.signedInt,
																new Optional(ArgumentType.signedInt)
															}
														)
													}
												)
											)
										};

	private static final char	EOF = '\uFFFF';
	
	private final Command[]	commands;
	
	public CommandLineParser() {
		this(COMMANDS);
	}

	public CommandLineParser(final Command... commands) {
		if (commands == null || commands.length == 0 || Utils.checkArrayContent4Nulls(commands) >= 0) {
			throw new IllegalArgumentException("Commands is null, empty or contains nulls inside");
		}
		else {
			this.commands = commands;
		}
	}
	
	public void parse(final CharSequence descriptor, final SVGCanvas canvas) throws CommandLineParametersException, CalculationException {
		final char[]	temp = CharUtils.terminateAndConvert2CharArray(descriptor, EOF);
		final int[]		nameLocation = new int[2];
		final int		start = CharUtils.parseName(temp, CharUtils.skipBlank(temp, 0, false), nameLocation);
		final String	name = new String(temp, nameLocation[0], nameLocation[1] - nameLocation[0] + 1);
		
		for(Command item : commands) {
			try{
				if (namesCompared(name, item.getCommandName()) 
						&& CharUtils.tryExtract(temp, start, item.param) >= 0) {
					try {
						final Object[]	values = new Object[100];
						
						CharUtils.extract(temp, start, values, item.param);						
						item.consumer.accept(this, canvas, item, values);
						return;
					} catch (SyntaxException e) {
						throw new CalculationException(e);
					}
				}
			} catch (SyntaxException e) {
			}
		}
		throw new CommandLineParametersException("Unknown command ["+name+"] or illegal command arguments");
	}

	private boolean namesCompared(final String command, final String template) {
		if (template.contains("[") && template.contains("]")) {
			return command.equalsIgnoreCase(template.substring(0, template.indexOf('[')))
				|| command.equalsIgnoreCase(template.replace("[","").replace("]",""));
		}
		else {
			return command.equalsIgnoreCase(template);
		}
	}
}
