package chav1961.bt.databaseutils.intern;

import java.io.IOException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.TableContainer;
import chav1961.purelib.model.UniqueIdContainer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.char2char.CodePrintStreamWrapper;

public class ScriptPrinter {
	private final ContentMetadataInterface	mdi;
	private final String	scheme;
	
	public ScriptPrinter(final ContentMetadataInterface mdi, final String scheme) {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (Utils.checkEmptyOrNullString(scheme)) {
			throw new IllegalArgumentException("Scheme name can't be null or empty");
		}
		else {
			this.mdi = mdi;
			this.scheme = scheme;
		}
	}
	
	public void print(final CodePrintStreamWrapper pw) throws IOException, PrintingException {
		if (pw == null) {
			throw new NullPointerException("Print erapper can't be null");
		}
		else {
			final String	upperName = scheme.toUpperCase();
			
			pw.println("create user "+upperName+" with password '"+scheme+"'");
			pw.println("create schema "+upperName+" authorization "+upperName);

			mdi.walkDown((mode, app, ui, node)->{
				if (mode == NodeEnterMode.ENTER) {
					try {
						if (node.getType() == UniqueIdContainer.class) {
							
							return ContinueMode.CONTINUE; 
						}
						else if (node.getType() == TableContainer.class) {
							final String	table = node.getName().toUpperCase();
							
							pw.println("create table "+upperName+"."+table+" (");
							pw.enter();
							mdi.walkDown((innerMode, innerApp, innerUi, innerNode)->{
								if (innerMode == NodeEnterMode.ENTER) {
									try {
										pw.println(innerNode.getName().toUpperCase()+" "+toColumnType(innerNode.getTooltipId())+",");
									} catch (PrintingException e) {
										return ContinueMode.STOP;
									}
								}
								return ContinueMode.CONTINUE;
							}, ui);
							pw.leave();
							pw.println(")");
							pw.println("grant select, insert, update, delete on "+upperName+"."+table+" to "+upperName+";");
							return ContinueMode.SKIP_CHILDREN; 
						}
						else {
							return ContinueMode.CONTINUE; 
						}
					} catch (PrintingException e) {
						return ContinueMode.STOP; 
					}
				}
				else {
					return ContinueMode.CONTINUE; 
				}
			}, mdi.getRoot().getUIPath());
		}
	}

	private String toColumnType(String tooltipId) {
		// TODO Auto-generated method stub
		return null;
	}

}
