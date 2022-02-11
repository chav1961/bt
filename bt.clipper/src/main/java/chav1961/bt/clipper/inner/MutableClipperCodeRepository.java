package chav1961.bt.clipper.inner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.bt.clipper.inner.interfaces.ClipperCodeRepository;
import chav1961.bt.clipper.inner.interfaces.ClipperFunction;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class MutableClipperCodeRepository implements ClipperCodeRepository {
	private final SyntaxTreeInterface<ClipperFunction>	tree = new AndOrTree<>();
	
	public MutableClipperCodeRepository() {
	}

	@Override
	public Iterator<ClipperFunction> iterator() {
		final List<ClipperFunction>	result = new ArrayList<>();
		
		tree.walk((name,from,to,func)->{
			result.add(func);
			return true;
		});
		return result.iterator();
	}

	@Override
	public boolean contains(long id) {
		return getSyntaxTreeInterface().contains(id);
	}

	@Override
	public ClipperFunction get(final long id) {
		if (!contains(id)) {
			throw new IllegalArgumentException();
		}
		else {
			return getSyntaxTreeInterface().getCargo(id);
		}
	}

	@Override
	public ClipperFunction getMain() {
		return get(0);
	}

	@Override
	public int size() {
		return (int) getSyntaxTreeInterface().size();
	}

	public SyntaxTreeInterface<ClipperFunction> getSyntaxTreeInterface() {
		return tree;
	}
}
