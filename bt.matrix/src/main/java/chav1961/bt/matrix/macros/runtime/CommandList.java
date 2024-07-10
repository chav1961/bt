package chav1961.bt.matrix.macros.runtime;

import java.util.ArrayList;
import java.util.List;

import chav1961.bt.matrix.macros.runtime.interfaces.Command;
import chav1961.bt.matrix.macros.runtime.interfaces.CommandRepo;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.ThreadedCommandRepo;
import chav1961.purelib.basic.exceptions.CalculationException;

public class CommandList implements ThreadedCommandRepo {
	public static enum CommandType {
		OR,
		AND,
		NOT,
		EQ,
		GT,
		GE,
		LT,
		LE,
		NE,
		MUL,
		DIV,
		MOD,
		ADD,
		SUB,
		NEGATE,
		LOAD_VAR,
		STORE_VAR,
		LOAD_INDEX,
		STORE_INDEX,
		CONST_BOOLEAN,
		CONST_INT,
		CONST_REAL,
		CONST_CHAR,
		CALL,
		BREAK,
		CONTINUE,
		ERROR,
		WARNING,
		PRINT,
		JUMP_TRUE,
		JUMP_FALSE,
		JUMP,
		DUPLICATE,
		POP
	}
	
	private final List<Integer>	backwardLabels = new ArrayList<>();
	private final CommandRepo	repo = new InMemoryCommandRepo();	
	
	@Override
	public void addCommand(final CommandType type) {
	}	

	@Override
	public void addCommand(final CommandType type, final Object... parameters) {
		switch(type) {
			case ADD		:
				break;
			case AND		:
				break;
			case BREAK		:
				break;
			case CALL		:
				break;
			case CONST_BOOLEAN	:
				break;
			case CONST_CHAR	:
				break;
			case CONST_INT	:
				break;
			case CONST_REAL	:
				break;
			case CONTINUE	:
				break;
			case DIV		:
				break;
			case DUPLICATE	:
				break;
			case EQ			:
				break;
			case ERROR		:
				break;
			case GE			:
				break;
			case GT			:
				break;
			case JUMP		:
				break;
			case JUMP_FALSE	:
				break;
			case JUMP_TRUE	:
				break;
			case LE			:
				break;
			case LOAD_INDEX	:
				break;
			case LOAD_VAR	:
				break;
			case LT			:
				break;
			case MOD		:
				break;
			case MUL		:
				break;
			case NE			:
				break;
			case NEGATE		:
				break;
			case NOT		:
				break;
			case OR			:
				break;
			case POP		:
				break;
			case PRINT		:
				break;
			case STORE_INDEX:
				break;
			case STORE_VAR	:
				break;
			case SUB		:
				break;
			case WARNING	:
				break;
			default:
				break;
		}
	}
	
	@Override
	public void registerForwardLabel(final int label) {
		
	}
	
	@Override
	public void registerBackwardLabel(final int label) {
		
	}
	
	public CommandCursor getCursor(final long commandAddress) {
		return new CommandCursor(repo, commandAddress);
	}
	
	public static class CommandCursor {
		private final CommandRepo	repo;
		private long				commandAddress = 0;
		
		private CommandCursor(final CommandRepo repo, final long commandAddress) {
			this.repo = repo;
			this.commandAddress = commandAddress;			
		}
		
		public boolean executeCommand(final MacrosRuntime rt) throws CalculationException {
			Command		c;
			long		delta;
			do {
				c = repo.getCommand(commandAddress);
				delta = c.execute(rt);
				
				switch (c.getControlType()) {
					case BACKWARD_BRUNCH : case BACKWARD_CONDITIONAL : case SEQUENCE :
						commandAddress += delta; 
						break;
					case FORWARD_BRUNCH : case FORWARD_CONDITIONAL :
						break;
					default :
						throw new UnsupportedOperationException("Control type ["+c.getControlType()+"] is not supported yet");
				}
				
			} while (!c.resumeRequired(rt));
			return true;
		}
	}
}
