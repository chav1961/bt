package chav1961.bt.model.interfaces;

public interface SecuredId<Type,SessionId> {
	String encode(Type content, SessionId salt);
	Type decode(String content, SessionId salt);
}
