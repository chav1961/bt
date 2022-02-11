package chav1961.bt.clipper.inner.compiler;

import chav1961.bt.clipper.inner.interfaces.ClipperValue;

public class Lexema {
	public static enum LexType {
		EOF,
		OpenB, CloseB, OpenBB, CloseBB, OpenFB, CloseFB, OpenXB,
		Dot, Colon, Semicolon, Div,
		Oper, Const, Name, Function, Builtin
	}
	
	public static enum OperType {
		Add, Sub, Mul, Div, Mod,
		EQ, NE, GT, GE, LT, LE,
	}
	
	private final int			row, col;
	private final LexType		type;
	private final OperType		opType;
	private final long			ref;
	private final ClipperValue	constValue;
	
	protected Lexema(final int row, final int col, final LexType type) {
		this.row = row;
		this.col = col;
		this.type = type;
		this.opType = null;
		this.ref = Long.MAX_VALUE;
		this.constValue = null;
	}

	protected Lexema(final int row, final int col, final LexType type, final long ref) {
		this.row = row;
		this.col = col;
		this.type = type;
		this.opType = null;
		this.ref = ref;
		this.constValue = null;
	}
	
	protected Lexema(final int row, final int col, final OperType type) {
		this.row = row;
		this.col = col;
		this.type = LexType.Oper;
		this.opType = type;
		this.ref = Long.MAX_VALUE;
		this.constValue = null;
	}

	protected Lexema(final int row, final int col, final ClipperValue value) {
		this.row = row;
		this.col = col;
		this.type = LexType.Const;
		this.opType = null;
		this.ref = Long.MAX_VALUE;
		this.constValue = value;
	}
	
	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	public LexType getType() {
		return type;
	}
	
	public OperType getOperType() {
		return opType;
	}
	
	public long getRef() {
		return ref;
	}
	
	public ClipperValue getValue() {
		return constValue;
	}

	@Override
	public String toString() {
		return "Lexema [row=" + row + ", col=" + col + ", type=" + type + ", opType=" + opType + ", ref=" + ref + ", constValue=" + constValue + "]";
	}
}
