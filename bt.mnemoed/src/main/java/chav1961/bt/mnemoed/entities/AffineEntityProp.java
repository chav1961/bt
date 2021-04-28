package chav1961.bt.mnemoed.entities;

import java.awt.geom.AffineTransform;

abstract class AffineEntityProp extends EntityProp {
	AffineEntityProp() {
	}
	
	public abstract AffineTransform getAffineTransform();
}
