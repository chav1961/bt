package chav1961.bt.databaseutils.intern;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.databaseutils.interfaces.PartManagerInterface;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.TableContainer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.char2char.CodePrintStreamWrapper;

public class OrmGenerator {
	public static void processModel(final ContentMetadataInterface dbModel, final PartManagerInterface pmi, final String root) {
		if (dbModel == null) {
			throw new NullPointerException("DB model can't be null");
		}
		else if (pmi == null) {
			throw new NullPointerException("Print manager can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root package string can't be null");
		}
		else {
			printEntity(dbModel, pmi, root);
		}
	}

	static void printEntity(final ContentMetadataInterface dbModel, final PartManagerInterface pmi, final String root) {
		dbModel.walkDown((mode, applicationPath, uiPath, node)->{
			if (mode == NodeEnterMode.ENTER && node.getType() == TableContainer.class) {
				try(final OutputStream  os = pmi.getStream(root+"/entities/"+node.getName()+".java");
					final PrintStream	ps = new PrintStream(os, false, PureLibSettings.DEFAULT_CONTENT_ENCODING);
					final CodePrintStreamWrapper cs = new CodePrintStreamWrapper(ps)) {
					final List<ContentNodeMetadata> keys = new ArrayList<>();
					
					for(ContentNodeMetadata item : node) {
						if (URIUtils.parseQuery(item.getApplicationPath()).containsKey("pkSeq")) {
							keys.add(item);
						}
					}
					new EntityPrinter(node, keys.toArray(new ContentNodeMetadata[keys.size()]), root+"/entities").print(cs);
				} catch (IOException | PrintingException e) {
					e.printStackTrace();
				}
				return ContinueMode.SKIP_CHILDREN;
			}
			return ContinueMode.CONTINUE;
		}, dbModel.getRoot().getUIPath());
	}

	static void printMetaModel(final ContentMetadataInterface dbModel, final PartManagerInterface pmi, final String root) {
		dbModel.walkDown((mode, applicationPath, uiPath, node)->{
			if (mode == NodeEnterMode.ENTER && node.getType() == TableContainer.class) {
				try(final OutputStream  os = pmi.getStream(root+"/metamodel/"+node.getName()+"_.java");
					final PrintStream	ps = new PrintStream(os, false, PureLibSettings.DEFAULT_CONTENT_ENCODING);
					final CodePrintStreamWrapper cs = new CodePrintStreamWrapper(ps)) {
					final List<ContentNodeMetadata> keys = new ArrayList<>();
					
					for(ContentNodeMetadata item : node) {
						if (URIUtils.parseQuery(item.getApplicationPath()).containsKey("pkSeq")) {
							keys.add(item);
						}
					}
					new MetaModelPrinter(node, keys.toArray(new ContentNodeMetadata[keys.size()]), root+"/metamodel").print(cs);
				} catch (IOException | PrintingException e) {
					e.printStackTrace();
				}
				return ContinueMode.SKIP_CHILDREN;
			}
			return ContinueMode.CONTINUE;
		}, dbModel.getRoot().getUIPath());
	}
}
