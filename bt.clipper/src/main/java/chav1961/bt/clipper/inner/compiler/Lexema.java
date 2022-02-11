package chav1961.bt.clipper.inner.compiler;

import chav1961.bt.clipper.inner.interfaces.ClipperValue;

public class Lexema {
	public static enum LexType {
		EOF, EndOp, Comment, Raw,
		OpenB, CloseB, OpenBB, CloseBB, OpenFB, CloseFB, OpenXB,
		Dot, Colon, Semicolon, Div, Vert, 
		Oper, Const, Name, NameF, Builtin, Macros,
		ToConsole, ToConsole2,
		Box, Prompt, Say, Get, To,
		Accept, Append, Blank,
		Average,
		Begin, Sequence, End,
		Call, Cancel, Clear, All, Gets, Memory, Screen,
		Close, Continue, Copy, File, Structure, Extended,
		Count, Create, Declare, Delete, Dir, Display,
		Do, Case, EndCase, While, EndDo,
		Eject, Erase, Exit, External,
		Find, For, Next, Function, Return, Goto,
		If, ElseIf, Else, Endif,
		Index, On, Input, Join, With,
		Keyboard, Label, Form, List, Locate,
		Loop, Menu, Note, Pack, Parameters,
		Private, Procedure, Public, Quit,
		Read, Recall, Reindex, Release, Rename, Replace,
		Report, Restore, From, Run,
		Save, Seek, Select, Set,
		Alternate, Bell, Century, Date,
		Color, Confirm, Console, Decimals,
		Default, Deleted, Delimiters, Device,
		Escape, Exact, Filter, Fixed,
		Format, Intensity, Key, Margin,
		Message, Order, Path, Print, Printer,
		Relation, Unique, Skip, Sort, Store,
		Sum, Text, EndText, Total, Type,
		Update, Use, Wait, ZAP
	}
	
	public static enum OperType {
		Add, Sub, Mul, Power, Div, Mod,
		EQ, NE, GT, GE, LT, LE, Contains, 
		Not, And, Or
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
