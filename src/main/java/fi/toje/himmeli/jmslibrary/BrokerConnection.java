package fi.toje.himmeli.jmslibrary;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

public class BrokerConnection {
	
	private Connection connection;
	private BrokerSession brokerSession;
	private boolean isStarted = false;
	
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
	 * Starts the connection. Also sets internal variable which is used 
	 * when initializing sessions (start or not to start connection).
	 * 
	 * @throws JMSException
	 */
	public void start() throws JMSException {
		if (!isStarted) {
			connection.start();
			isStarted = true;
		}
	}
	
	/**
	 * Stops the connection. Also sets internal variable which is used 
	 * when initializing sessions (start or not to start connection).
	 * 
	 * @throws JMSException
	 */
	public void stop() throws JMSException {
		if (isStarted) {
			connection.stop();
			isStarted = false;
		}
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
		boolean wasStarted = isStarted;
		stop();
		if (brokerSession != null) {
			brokerSession.close();
		}
		if (wasStarted) {
			start();
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
	 * Initializes new session for connection. Closes existing producer, consumer and session if needed.
	 *  
	 * @param transacted
	 * @param type
	 * @throws JMSException 
	 */
	private void initSession(boolean transacted, int type) throws Exception {
		boolean wasStarted = isStarted;
		stop();
		if (brokerSession != null) {
			brokerSession.close();
		}
		brokerSession = new BrokerSession(connection, transacted, type);
		if (wasStarted) {
			start();
		}
	}
	
	/**
	 * Initializes new session for connection. Closes existing producer, consumer and session if needed.
	 * 
	 * @param transacted
	 * @param type AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE, SESSION_TRANSACTED
	 * @throws JMSException
	 */
	public void initSession(boolean transacted, String type) throws Exception {
		initSession(transacted, convertType(type));
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static int convertType(String type) throws Exception {
		int t = 0;
		if (BrokerSession.SESSION_TRANSACTED.equals(type) || String.valueOf(Session.SESSION_TRANSACTED).equals(type)) {
			t = Session.SESSION_TRANSACTED;
		}
		else if (BrokerSession.CLIENT_ACKNOWLEDGE.equals(type) || String.valueOf(Session.CLIENT_ACKNOWLEDGE).equals(type)) {
			t = Session.CLIENT_ACKNOWLEDGE;
		}
		else if (BrokerSession.DUPS_OK_ACKNOWLEDGE.equals(type) || String.valueOf(Session.DUPS_OK_ACKNOWLEDGE).equals(type)) {
			t = Session.DUPS_OK_ACKNOWLEDGE;
		}
		else if (BrokerSession.AUTO_ACKNOWLEDGE.equals(type) || String.valueOf(Session.AUTO_ACKNOWLEDGE).equals(type)) {
			t = Session.AUTO_ACKNOWLEDGE;
		}
		else {
			throw new Exception("Invalid type: " + type);
		}
		
		return t;
	}
}
