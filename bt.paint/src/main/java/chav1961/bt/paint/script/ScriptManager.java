package chav1961.bt.paint.script;

import javax.swing.JToolBar;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;

public class ScriptManager implements AutoCloseable {
	public static enum Mode {
		ORDINAL, RECORDING, PLAYING
	}
	
	
	public ScriptManager(final Localizer localizer, final LoggerFacade logger, final JToolBar control) {
		
	}

	public ScriptOwner<ScriptNodeType> getRecorder() {
		return null;
	}

	public ScriptOwner<ScriptNodeType> getPlayer() {
		return null;
	}
	
	public Mode getMode() {
		return null;
	}
	
	public void setMode(final Mode mode) {
		
	}
	

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		
	}
}
