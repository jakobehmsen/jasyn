package jasy.interceptors.transcription;

public interface Store {
	void put(Invocation invocation);
	Object allocateId(Object obj);
}
