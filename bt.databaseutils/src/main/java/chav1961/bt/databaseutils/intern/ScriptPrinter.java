package chav1961.bt.databaseutils.intern;

import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.FieldFormat;
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
			
			pw.println("create user "+upperName+" with password '"+scheme+"';");
			pw.println();
			pw.println("create schema "+upperName+" authorization "+upperName+";");
			pw.println();

			mdi.walkDown((mode, app, ui, node)->{
				if (mode == NodeEnterMode.ENTER) {
					try {
						if (node.getType() == UniqueIdContainer.class) {
							final String	sequence = node.getName().toUpperCase();
							
							pw.println("create sequence "+upperName+"."+sequence+";");
							pw.println();
							return ContinueMode.CONTINUE; 
						}
						else if (node.getType() == TableContainer.class) {
							final String	table = node.getName().toUpperCase();
							
							pw.println("create table "+upperName+"."+table+" (");
							pw.enter();
							mdi.walkDown((innerMode, innerApp, innerUi, innerNode)->{
								if (innerMode == NodeEnterMode.ENTER && innerNode.getType() != TableContainer.class) {
									try {
										pw.println(innerNode.getName().toUpperCase()+" "+toColumnType(innerNode.getApplicationPath())+toNotNull(innerNode.getFormatAssociated())+",");
									} catch (PrintingException e) {
										return ContinueMode.STOP;
									}
								}
								return ContinueMode.CONTINUE;
							}, ui);
							pw.print("primary key (");
							mdi.walkDown((innerMode, innerApp, innerUi, innerNode)->{
								if (innerMode == NodeEnterMode.ENTER && innerNode.getType() != TableContainer.class) {
									try {
										final Hashtable<String, String[]>	parms = URIUtils.parseQuery(innerNode.getApplicationPath());
										
										if (parms.containsKey("pkSeq")) {
											if (!"1".equals(parms.get("pkSeq")[0])) {
												pw.print(',');
											}
											pw.print(innerNode.getName().toUpperCase());
										}
									} catch (PrintingException e) {
										return ContinueMode.STOP;
									}
								}
								return ContinueMode.CONTINUE;
							}, ui);
							pw.println(")");
							pw.leave();
							pw.println(");");
							pw.println();
							pw.println("grant select, insert, update, delete on "+upperName+"."+table+" to "+upperName+";");
							pw.println();
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

	private String toColumnType(final URI appId) {
		return URIUtils
		.parseQuery(appId)
		.get("type")
		[0];
	}

	private String toNotNull(final FieldFormat formatAssociated) {
		return formatAssociated != null && formatAssociated.isMandatory() ? " not null " : "";
	}

}
