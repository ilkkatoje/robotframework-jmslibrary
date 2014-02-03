package fi.toje.himmeli.jmslibrary;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

public class BrokerConnection {
	
	private Connection connection;
	private BrokerSession brokerSession;
	
	public BrokerConnection(Connection connection) throws Exception {
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
		if (brokerSession != null) {
			brokerSession.close();
		}
		connection.close();
	}
	
	public void closeSession() throws JMSException {
		if (brokerSession != null) {
			brokerSession.close();
		}
	}
	
	/**
	 * @return connection
	 */
	public Connection getConnection() {
		return connection;
	}
	
	public BrokerSession getBrokerSession() {
		return brokerSession;
	}
	
	public Session getSession() {
		return brokerSession.getSession();
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
	public void initSession(boolean transacted, String type) throws Exception {
		if (brokerSession != null) {
			brokerSession.close();
		}
		brokerSession = new BrokerSession(connection.createSession(transacted, BrokerSession.convertType(type)));
	}
}
