package chav1961.bt.clipper.inner.functions;

import chav1961.bt.clipper.inner.interfaces.ClipperFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperSyntaxEntity;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class InternalUtils {
	@FunctionalInterface
	interface FunctionGetter {
		ClipperFunction get(long id);
	}
	
	public static void prepare(final SyntaxTreeInterface<ClipperSyntaxEntity> tree) {
		if (tree == null) {
			throw new NullPointerException("Tree can't be null");
		}
		else {
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
			placeSpecial(tree, "__MClear", null);
			placeSpecial(tree, "__MXRelease", null);
			placeSpecial(tree, "__MRelease", null);
			placeSpecial(tree, "__MRestore", null);
			placeSpecial(tree, "__MSave", null);
			placeSpecial(tree, "FErase", null);
			placeSpecial(tree, "FRename", null);
			placeSpecial(tree, "__CopyFile", null);
			placeSpecial(tree, "__Dir", null);
			placeSpecial(tree, "__TypeFile", null);
			placeSpecial(tree, "__Quit", null);
			placeSpecial(tree, "__Run", null);
			placeSpecial(tree, "dbSelectArea", null);
			placeSpecial(tree, "dbCloseArea", null);
			placeSpecial(tree, "dbUseArea", null);
			placeSpecial(tree, "dbSetIndex", null);
			placeSpecial(tree, "dbAppend", null);
			placeSpecial(tree, "__dbPack", null);
			placeSpecial(tree, "__dbZap", null);
			placeSpecial(tree, "dbUnlock", null);
			placeSpecial(tree, "dbUnlockAll", null);
			placeSpecial(tree, "dbCommitAll", null);
			placeSpecial(tree, "dbGoto", null);
			placeSpecial(tree, "dbGoTop", null);
			placeSpecial(tree, "dbGoBottom", null);
			placeSpecial(tree, "dbSkip", null);
			placeSpecial(tree, "dbSeek", null);
			placeSpecial(tree, "__dbContinue", null);
			placeSpecial(tree, "__dbLocate", null);
			placeSpecial(tree, "dbClearRel", null);
			placeSpecial(tree, "dbSetRelation", null);
			placeSpecial(tree, "dbClearFilter", null);
			placeSpecial(tree, "dbSetFilter", null);
			placeSpecial(tree, "DBEval", null);
			placeSpecial(tree, "dbDelete", null);
			placeSpecial(tree, "dbRecall", null);
			placeSpecial(tree, "__dbCreate", null);
			placeSpecial(tree, "__dbCopyXStruct", null);
			placeSpecial(tree, "__dbCopyStruct", null);
			placeSpecial(tree, "__dbDelim", null);
			placeSpecial(tree, "__dbSDF", null);
			placeSpecial(tree, "__dbCopy", null);
			placeSpecial(tree, "__dbApp", null);
			placeSpecial(tree, "__dbSort", null);
			placeSpecial(tree, "__dbTotal", null);
			placeSpecial(tree, "__dbUpdate", null);
			placeSpecial(tree, "__dbJoin", null);
			placeSpecial(tree, "__dbList", null);
			placeSpecial(tree, "__ReportForm", null);
			placeSpecial(tree, "__LabelForm", null);
			placeSpecial(tree, "dbCloseArea", null);
			placeSpecial(tree, "dbCloseAll", null);
			placeSpecial(tree, "__SetFormat", null);
			placeSpecial(tree, "dbClearIndex", null);
			placeSpecial(tree, "dbCreateIndex", null);			
			placeSpecial(tree, "ordCondSet", null);
			placeSpecial(tree, "ordCreate", null);
			placeSpecial(tree, "ordDestroy", null);
			placeSpecial(tree, "ordListRebuild", null);
			placeSpecial(tree, "ordListAdd", null);
			placeSpecial(tree, "ordSetFocus", null);
			placeSpecial(tree, "OutStd", null);
						  
		}
	}

	static void placeBuiltin(final SyntaxTreeInterface<ClipperSyntaxEntity> tree, final String name, final FunctionGetter item) {
		final long			id = tree.placeName(name, null);
		final ClipperValue	val = item.get(id); 
		
		tree.setCargo(id, new ClipperSyntaxEntity() {
			@Override public SyntaxEntityType getSyntaxEntityType() {return SyntaxEntityType.BUILTIN;}
			@Override public String getSyntaxEntityName() {return name;}
			@Override public ClipperValue getValueAssociated() {return val;}
		});
	}

	static void placeSpecial(final SyntaxTreeInterface<ClipperSyntaxEntity> tree, final String name, final FunctionGetter item) {
		final long			id = tree.placeName(name, null);
		final ClipperValue	val = item.get(id); 
		
		tree.setCargo(id, new ClipperSyntaxEntity() {
			@Override public SyntaxEntityType getSyntaxEntityType() {return SyntaxEntityType.BUILTIN;}
			@Override public String getSyntaxEntityName() {return name;}
			@Override public ClipperValue getValueAssociated() {return val;}
		});
	}

}
