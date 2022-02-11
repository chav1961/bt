package chav1961.bt.clipper;

import chav1961.bt.clipper.inner.AbstractBuiltinClipperFunction;
import chav1961.bt.clipper.inner.ImmutableClipperValue;
import chav1961.bt.clipper.inner.interfaces.ClipperBuiltinFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperCodeRepository;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.vm.ConstantPool;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class ClipperRuntime {
	public static final ClipperValue			NULL = new ImmutableClipperValue(ClipperType.C_Void,null);
	public static final SyntaxTreeInterface<?>	DICTIONARY = new AndOrTree<>();	
	public static final AbstractBuiltinClipperFunction[]	BUILTINS = new AbstractBuiltinClipperFunction[0]; 
	
	private final ClipperCodeRepository	repo;
	
	public ClipperRuntime(final ClipperCodeRepository repo) {
		this.repo = repo;
	}
	
	public ClipperCodeRepository getCodeRepository() {
		return repo;
	}
	
	public ConstantPool getConstantPool() {
		return null;
	}
	
	public ClipperBuiltinFunction[] getBuiltins() {
		return BUILTINS;
	}
	
	public SyntaxTreeInterface<ClipperBuiltinFunction> getBuiltinsTree() {
		return null;
	}
	
	public final SyntaxTreeInterface<?>	getDistionary() {
		return null;
	}
}
