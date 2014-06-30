package fi.toje.himmeli.jmslibrary;

import javax.jms.Connection;
import javax.jms.JMSException;

public class ProviderConnection {
	
	private Connection connection;
	private ProviderSession providerSession;
	
	public ProviderConnection(Connection connection) throws Exception {
		this.connection = connection;
	}
	
	public void setClientId(String clientId) throws JMSException {
		connection.setClientID(clientId);
	}
	
	public String getClientId() throws JMSException {
		return connection.getClientID();
	}
	
	/**
	 * Starts the connection.
	 * 
	 * @throws JMSException
	 */
	public void start() throws JMSException {
		connection.start();
	}
	
	/**
	 * Stops the connection.
	 * 
	 * @throws JMSException
	 */
	public void stop() throws JMSException {
		connection.stop();
	}
	
	/**
	 * Closes connection. Also closes session.
	 * 
	 * @throws JMSException
	 */
	public void close() throws JMSException {
		stop();
		if (providerSession != null) {
			providerSession.close();
		}
		connection.close();
	}
	
	public ProviderSession getProviderSession() {
		return providerSession;
	}
	
	/**
	 * Initializes new session for connection. Closes existing producer,
	 * consumer and session if needed.
	 * 
	 * @param transacted
	 * @param type AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE,
	 * SESSION_TRANSACTED
	 * @throws JMSException
	 */
	public void initSession(boolean transacted, int type) throws Exception {
		if (providerSession != null) {
			providerSession.close();
		}
		providerSession = new ProviderSession(connection.createSession(transacted, type));
	}
}
