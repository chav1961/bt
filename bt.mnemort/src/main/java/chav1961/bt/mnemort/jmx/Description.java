package chav1961.bt.mnemort.jmx;

import java.lang.annotation.ElementType;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@interface Description {
	String value();
}
