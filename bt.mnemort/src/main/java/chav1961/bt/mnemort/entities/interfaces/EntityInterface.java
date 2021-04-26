package chav1961.bt.mnemort.entities.interfaces;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public interface EntityInterface {
	public interface EntityComponentInterface {
		public enum ComponentType {
			PATH,
			TEXT,
			IMAGE
		}
		
		ComponentType getComponentType();
		Point getAnchor();
		Dimension getSize();
		AffineTransform getTransform();
	}
	
	public interface WalkerCallback {
		ContinueMode walkDown(NodeEnterMode mode, EntityInterface node) throws ContentException;
	}
	
	EntityComponentInterface[] getComponents();	
	ContinueMode walkDown(WalkerCallback callback) throws ContentException;
}
