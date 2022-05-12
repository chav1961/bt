package chav1961.bt.clipper.inner.functions;

import chav1961.bt.clipper.inner.interfaces.ClipperFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperSyntaxEntity;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class FunctionsRepo {
	private interface FunctionGetter {
		ClipperFunction get(long id);
	}
	
	public static void prepare(final SyntaxTreeInterface<ClipperSyntaxEntity> tree) {
		if (tree == null) {
			throw new NullPointerException("Tree can't be null");
		}
		else {
			placeBuiltin(tree, "ADEL", (id)->new ADEL_Function(ClipperType.C_Array, id));
			
			placeSpecial(tree, "_ProcReq_", null);
			placeSpecial(tree, "Set", null);
			placeSpecial(tree, "__SetCentury", null);
			placeSpecial(tree, "_DFSET", null);
			placeSpecial(tree, "SetColor", null);
			placeSpecial(tree, "SetCursor", null);
			placeSpecial(tree, "QOut", null);
			placeSpecial(tree, "QQOut", null);
			placeSpecial(tree, "__Eject", null);
			placeSpecial(tree, "__TextSave", null);
			placeSpecial(tree, "__TextRestore", null);
			placeSpecial(tree, "Scroll", null);
			placeSpecial(tree, "SetPos", null);
			placeSpecial(tree, "DispBox", null);
			placeSpecial(tree, "DevPos", null);
			placeSpecial(tree, "DevPos", null);
			placeSpecial(tree, "DevOut", null);
			placeSpecial(tree, "DevOutPict", null);
			placeSpecial(tree, "__SetFormat", null);
			placeSpecial(tree, "ReadModal", null);
			placeSpecial(tree, "ReadKill", null);
			placeSpecial(tree, "__AtPrompt", null);
			placeSpecial(tree, "__MenuTo", null);
			placeSpecial(tree, "__XSaveScreen", null);
			placeSpecial(tree, "__XRestScreen", null);
			placeSpecial(tree, "SaveScreen", null);
			placeSpecial(tree, "RestScreen", null);
			placeSpecial(tree, "__Wait", null);
			placeSpecial(tree, "__Accept", null);
			placeSpecial(tree, "__Keyboard", null);
			placeSpecial(tree, "SetKey", null);
			placeSpecial(tree, "__SetFunction", null);
			
															
		}
	}

	private static void placeBuiltin(final SyntaxTreeInterface<ClipperSyntaxEntity> tree, final String name, final FunctionGetter item) {
		final long			id = tree.placeName(name, null);
		final ClipperValue	val = item.get(id); 
		
		tree.setCargo(id, new ClipperSyntaxEntity() {
			@Override public SyntaxEntityType getSyntaxEntityType() {return SyntaxEntityType.BUILTIN;}
			@Override public String getSyntaxEntityName() {return name;}
			@Override public ClipperValue getValueAssociated() {return val;}
		});
	}

	private static void placeSpecial(final SyntaxTreeInterface<ClipperSyntaxEntity> tree, final String name, final FunctionGetter item) {
		final long			id = tree.placeName(name, null);
		final ClipperValue	val = item.get(id); 
		
		tree.setCargo(id, new ClipperSyntaxEntity() {
			@Override public SyntaxEntityType getSyntaxEntityType() {return SyntaxEntityType.BUILTIN;}
			@Override public String getSyntaxEntityName() {return name;}
			@Override public ClipperValue getValueAssociated() {return val;}
		});
	}

}
