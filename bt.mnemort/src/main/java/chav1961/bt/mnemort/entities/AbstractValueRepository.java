package chav1961.bt.mnemort.entities;

import chav1961.bt.mnemort.entities.interfaces.QualityByte;
import chav1961.purelib.basic.exceptions.ContentException;

abstract class AbstractValueRepository {
	private QualityByte	quality = QualityByte.NORMAL;
	
	public QualityByte getQuality() {
		return quality;
	}
	
	public void setQuality(final QualityByte quality) {
		this.quality = quality;
	}
	
	protected abstract void tick();
	protected abstract void processError(final ContentException exc);
}
