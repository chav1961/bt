package chav1961.bt.model.interfaces;

public interface SessionDescriptor<SessionId> {
	public enum CRUD {
		CREATE, READ, UPDATE, DELETE
	}
	SessionId getSessionId();
	boolean canBeProcessed(Object item, CRUD operation);
}
