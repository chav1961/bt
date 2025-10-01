package chav1961.bt.svgeditor.parser;

import java.util.function.BiConsumer;

import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public abstract class AbstractCommandProcessor {
	
	public abstract void execute(SVGCanvas canvas) throws CalculationException;
	
	
	protected static class Content<T> {
		private final Class<T>	awaitedClass;
		private final T			awaitedValue;
		private BiConsumer<Content,T>	consumer;
		
		protected Content(final Class<T> awaitedClass, final BiConsumer<Content, T> consumer) {
			this(awaitedClass, null, consumer);
		}
		
		protected Content(final Class<T> awaitedClass, final T awaitedValue, final BiConsumer<Content, T> consumer) {
			this.awaitedClass = awaitedClass;
			this.awaitedValue = awaitedValue;
			this.consumer = consumer;
		}
	}
	
	protected boolean testContent(final Object[] parameters, final Content<?>... desc) {
		if (parameters.length < desc.length) {
			return false;
		}
		else {
			for(int index = 0, maxIndex = desc.length; index < maxIndex; index++) {
				if (!desc[index].awaitedClass.isInstance(parameters[index]) 
					|| desc[index].awaitedValue != null && !desc[index].awaitedValue.equals(parameters[index])) {
					return false;
				}
			}
			return true;
		}
	}

	protected void extractContent(final Object[] parameters, final Content... desc) {
		for(int index = 0, maxIndex = desc.length; index < maxIndex; index++) {
			if (desc[index].awaitedClass.isInstance(parameters[index]) && 
				(desc[index].awaitedValue == null || desc[index].awaitedValue.equals(parameters[index]))) {
				desc[index].consumer.accept(desc[index], parameters[index]);
			}
		}
	}

	protected void prepareProcessor(final Object[] parms, final Content<?>[]... content) throws CommandLineParametersException {
		for(Content<?>[] item : content) {
			if (testContent(parms, item)) {
				extractContent(parms, item);
				return;
			}
		}
		throw new CommandLineParametersException("");
	}
}
