package chav1961.bt.mnemoed.interfaces;

import chav1961.bt.mnemoed.entities.EntityProp;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public interface RuntimeInterface {
	public interface TagManipulator {
		<T> T getTagValue(final long tagId) throws ContentException;
		byte getByteTagValue(final long tagId) throws ContentException;
		int getTagQByte(final long tagId) throws ContentException;
		<T> void setTagValue(final long tagId, final T value) throws ContentException;
		void setByteTagValue(final long tagId, final byte value) throws ContentException;
		void commit() throws ContentException;
	}
	
	boolean isConstant(EntityProp prop);
	<T> T calculateValue(EntityProp prop) throws ContentException;
	byte calculateByteValue(EntityProp prop) throws ContentException;
	int parseExpression(final String expression, final int from) throws SyntaxException;
	int compileExpression(final String expression, final int from) throws SyntaxException;
	long[] getTagIds();
	long getTagId(final String tagName);
	String getTagName(final long tagId);
	TagManipulator snapshot(boolean isMutable) throws ContentException;
}
