import java.io.IOException;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import fi.toje.himmeli.jmslibrary.BrokerConnection;
import fi.toje.himmeli.jmslibrary.BrokerSession;

/**
 * Robot Framework library for testing applications utilizing JMS.
 * 
 * Set the library and chosen JMS provider into classpath and start testing.
 * 
 * Library uses one connection which has one session.
 * Session includes one message producer and one message consumer for topic. Queue consumers are created on the fly per receive. Currently, messages can be received from only one topic at time.
 * Producer specific settings (timeToLive etc.) apply within a session. Settings will be reset, if session is reinitialized.
 * 
 * Default receive timeout is 100 ms.
 * 
 * = Example with ActiveMQ =
 * 
 * | *** Settings ***
 * | Library         JMSLibrary  ${INITIAL_CONTEXT_FACTORY}  ${PROVIDER_URL}
 * | Suite Setup     Connect And Start
 * | Suite Teardown  Close
 * |
 * | *** Variables ***
 * | ${INITIAL_CONTEXT_FACTORY}  org.apache.activemq.jndi.ActiveMQInitialContextFactory
 * | ${PROVIDER_URL}             tcp://localhost:61616?jms.useAsyncSend=false
 * | ${QUEUE}                    QUEUE.JMSLIBRARY.TEST
 * | ${TOPIC}                    TOPIC.JMSLIBRARY.TEST
 * | ${BODY_TEXT}                Hello world!
 * |
 * | *** Test Cases ***
 * | Queue Send and Receive TextMessage
 * |     Create Text Message  ${BODY_TEXT}
 * |     Send To Queue  ${QUEUE}
 * |     Receive From Queue  ${QUEUE}
 * |     ${body}=  Get Text
 * |     Should Be Equal  ${BODY_TEXT}  ${body}
 * |
 * | Topic Send and Receive TextMessage
 * |     Subscribe  ${TOPIC}
 * |     Create Text Message  ${BODY_TEXT}
 * |     Send To Topic  ${TOPIC}
 * |     Receive From Topic
 * |     ${body}=  Get Text
 * |     Should Be Equal  ${BODY_TEXT}  ${body}
 * |     Unsubscribe
 * 
 */
public class JMSLibrary {

	public static final String ROBOT_LIBRARY_SCOPE = "TEST SUITE";
	public static final String ROBOT_LIBRARY_VERSION = "1.0.0-beta.2";
	public static final String DEFAULT_JNDI_CONNECTION_FACTORY_NAME = "ConnectionFactory";
	
	private InitialContext jndi;
	private ConnectionFactory connectionFactory;
	private BrokerConnection brokerConnection;
	
	/**
	 * Settings for selecting JMS provider.
	 * 
	 * Default JNDI connection factory look up string: ConnectionFactory
	 * 
	 * Example:
	 * | Library | JMSLibrary | org.apache.activemq.jndi.ActiveMQInitialContextFactory | tcp://localhost:61616?jms.useAsyncSend=false |
	 */
	public JMSLibrary(String initialContextFactory, String providerUrl) throws NamingException {
		Properties env = new Properties( );
		env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		env.put(Context.PROVIDER_URL, providerUrl);
		
		jndi = new InitialContext(env);
		connectionFactory = (ConnectionFactory)jndi.lookup("ConnectionFactory");
	}
	
	/**
	 * Connects to broker. Does not initialize session or start connection.
	 * 
	 */
	public void connect() throws Exception {
		connect(null, null);
	}
	
	/**
	 * Connects to broker. Does not initialize session or start connection.
	 * 
	 */
	public void connect(String username, String password) throws Exception {
		if (brokerConnection != null) {
			throw new Exception("Connection exists");
		}
		Connection connection;
		if (username != null) {
			connection = connectionFactory.createConnection(username, password);
		}
		else {
			connection = connectionFactory.createConnection();
		}
		
		brokerConnection = new BrokerConnection(connection);
	}
	
	/**
	 * Connects to broker. Initializes default session and starts the connection.
	 * 
	 */
	public void connectAndStart() throws Exception {
		connect();
		initializeSession();
		start();
	}
	
	/**
	 * Connects to broker. Initializes default session and starts the connection.
	 * 
	 */
	public void connectAndStart(String username, String password) throws Exception {
		connect(username, password);
		initializeSession();
		start();
	}
	
	/**
	 * Sets clientId. Must be used right after connect, refer JMS specs.
	 * 
	 */
	public void setClientId(String clientId) throws JMSException {
		brokerConnection.setClientId(clientId);
	}
	
	/**
	 * Returns clientId.
	 */
	public String getClientId() throws JMSException {
		return brokerConnection.getClientId();
	}
	
	/**
	 * (Re)initializes session with default attributes (non-transacted, AUTO_ACKNOWLEDGE).
	 * 
	 */
	public void initializeSession() throws Exception {
		initializeSession(false, BrokerSession.AUTO_ACKNOWLEDGE);
	}
	
	/**
	 * (Re)initializes session for current connection.
	 * 
	 * Arguments:
	 * - _transacted_: true or false
	 * - _type_: AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE or SESSION_TRANSACTED
	 * 
	 */
	public void initializeSession(boolean transacted, String type) throws Exception {
		brokerConnection.initSession(transacted, type);
	}
	
	/**
	 * Starts connection.
	 * 
	 */
	public void start() throws JMSException {
		brokerConnection.start();
	}
	
	/**
	 * Stops connection.
	 * 
	 */
	public void stop() throws JMSException {
		brokerConnection.stop();
	}
	
	/**
	 * Closes broker connection. Closes all resources (session, producer and consumer).
	 * 
	 */
	public void closeConnection() throws Exception {
		brokerConnection.close();
		brokerConnection = null;
	}
	
	/**
	 * Commits all messages in the session.
	 * 
	 */
	public void commit() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.commit();
	}
	
	/**
	 * Rolls back messages in the session.
	 * 
	 */
	public void rollback() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.rollback();
	}
	
	/**
	 * Acknowledges all consumed messages of the session. Used in CLIENT_ACKNOWLDGE mode.
	 * 
	 */
	public void acknowledge() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.acknowledge();
	}
	
	/**
	 * Creates TextMessage. Additional properties can be set after creation.
	 * 
	 */
	public void createTextMessage(String text) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.createTextMessage(text);
	}
	
	/**
	 * Creates BytesMessage from file. Additional properties can be set after creation.
	 * 
	 * Argument:
	 * - _file_: filepath
	 * 
	 */
	public void createBytesMessageFromFile(String file) throws JMSException, IOException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.createBytesMessage(file);
	}
	
	/**
	 * Sets JMSType of message.
	 * 
	 */
	public void setJMSType(String type) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.setType(type);
	}
	
	/**
	 * Returns JMSType of message.
	 * 
	 */
	public String getJMSType() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getType();
	}
	
	/**
	 * Sets JMSCorrelationID for message.
	 * 
	 */
	public void setJMSCorrelationId(String correlationId) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.setCorrelationId(correlationId);
	}
	
	/**
	 * Returns JMSCorrelationID of message.
	 * 
	 */
	public String getJMSCorrelationId() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getCorrelationId();
	}
	
	/**
	 * Sets JMSReplyTo queue for message.
	 * 
	 */
	public void setJMSReplyToQueue(String queue) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.setReplyToQueue(queue);
	}
	
	/**
	 * JMSReplyTo queue of message.
	 * 
	 * Returns queue if it was set and was type of queue, otherwise (not set or is topic) null 
	 * 
	 */
	public String getJMSReplyToQueue() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getReplyToQueue();
	}
	
	/**
	 * Sets JMSReplyTo topic for message.
	 * 
	 */
	public void setJMSReplyToTopic(String topic) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.setReplyToTopic(topic);
	}
	
	/**
	 * JMSReplyTo value of message.
	 * 
	 * Returns topic if it was set and was type of topic, otherwise (not set or is queue) null 
	 * 
	 */
	public String getJMSReplyToTopic() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getReplyToTopic();
	}
	
	/**
	 * Sets time to live for the producer.
	 * 
	 * Argument:
	 * - _timeToLive_: time to live in milliseconds
	 * 
	 */
	public void setProducerTimeToLive(long timeToLive) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.setProducerTimeToLive(timeToLive);
	}
	
	/**
	 * JMSExpiration of received message.
	 * 
	 * Returns expiration of message
	 * 
	 */
	public long getJMSExpiration() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getExpiration();
	}
	
	/**
	 * Sets delivery mode for the producer.
	 * 
	 * Argument:
	 * - _deliveryMode_: PERSISTENT or NON_PERSISTENT
	 * 
	 */
	public void setProducerDeliveryMode(String deliveryMode) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.setProducerDeliveryMode(deliveryMode);
	}
	
	/**
	 * Returns delivery mode of the producer: PERSISTENT or NON_PERSISTENT.
	 * 
	 */
	public String getProducerDeliveryMode() throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getProducerDeliveryMode();
	}
	
	/**
	 * Returns delivery mode of received message: PERSISTENT or NON_PERSISTENT.
	 * 
	 */
	public String getDeliveryMode() throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getDeliveryMode();
	}
	
	/**
	 * JMSRedelivered of received message.
	 * 
	 * Return true if message was redelivered
	 * 
	 */
	public boolean getJMSRedelivered() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		return bs.getJmsRedelivered();
	}
	
	/**
	 * Sets string property for message.
	 * 
	 */
	public void setStringProperty(String name, String value) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.setStringProperty(name, value);
		System.out.println(name + "=" + value);
	}
	
	/**
	 * Returns string property of message.
	 * 
	 * Arguments:
	 * - _name_: name of the property
	 * 
	 */
	public String getStringProperty(String name) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		String value = bs.getStringProperty(name);
		System.out.println(name + "=" + value);
		
		return value;
	}
	
	/**
	 * JMSMessageID
	 * 
	 * Returns message id.
	 */
	public String getJMSMessageId() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		String id = bs.getMessageId();
		System.out.println("MessageId=" + id);
		
		return id;
	}
	
	/**
	 * Sends message to queue. The message must have been created beforehand using one of the create message methods.
	 * Message id can be accessed after sending.
	 * 
	 */
	public void sendToQueue(String queue) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.sendToQueue(queue);
	}
	
	/**
	 * Receives message from queue. The message is set to internal message object and its body and
	 * properties can be accessed via methods.
	 * 
	 */
	public void receiveFromQueue(String queue) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.receiveFromQueue(queue);
	}
	
	/**
	 * Receives message from queue. The message is set to internal message object and its body and
	 * properties can be accessed via methods.
	 * 
	 * Arguments:
	 * - _queue_: name of the queue
	 * - _timeout_: receive timeout in milliseconds
	 */
	public void receiveFromQueue(String queue, long timeout) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.receiveFromQueue(queue, timeout);
	}
	
	/**
	 * Sends message to topic. The message must have been created beforehand using one of the create message methods.
	 * Message id can be accessed after sending.
	 * 
	 */
	public void sendToTopic(String topic) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.sendToTopic(topic);
	}
	
	/**
	 * Subscribes to topic. Receive From Topic can be called after.
	 * 
	 */
	public void subscribe(String topic) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.subscribe(topic);
	}
	
	/**
	 * Unsubscribes from topic and closes topic consumer. This can be used also after Subscribe Durable (Durable subscription will still remain).
	 * 
	 */
	public void unsubscribe() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.unsubscribe();
	}
	
	/**
	 * Subscribes durably to topic. Receive From Topic can be called after.
	 * 
	 * Arguments:
	 * - _topic_: topic name
	 * - _name_: subscription name
	 * 
	 */
	public void subscribeDurable(String topic, String name) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.subscribeDurable(topic, name);
	}
	
	/**
	 * Unsubscribes from topic and closes topic consumer.
	 * 
	 * Argument:
	 * - _name_: subscription name
	 */
	public void unsubscribeDurable(String name) throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.unsubscribeDurable(name);
	}
	
	/**
	 * Subscribe (Subscribe Durable) must have been called before this.
	 * 
	 */
	public void receiveFromTopic() throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.receiveFromTopic();
	}
	
	/**
	 * Subscribe (Subscribe Durable) must have been called before this.
	 * 
	 * Argument:
	 * - _timeout_: receive timeout milliseconds
	 */
	public void receiveFromTopic(long timeout) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.receiveFromTopic(timeout);
	}
	
	/**
	 * Returns the body of TextMessage.
	 * 
	 */
	public String getText() throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		
		return bs.getText();
	}
	
	/**
	 * Writes body of BytesMessage into file.
	 * 
	 */
	public void writeBytesToFile(String file) throws JMSException, IOException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		bs.writeBytes(file);
	}
	
	/**
	 * Returns queue depth. Implemented using QueueBrowser.
	 * 
	 */
	public int queueDepth(String queue) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		int depth = bs.queueDepth(queue);
		System.out.println(queue + " depth is " + depth);
		
		return depth;
	}
	
	/**
	 * Clears the queue by reading all available messages. Acknowledges or commits depending on the configuration.
	 * 
	 * Returns message count that was consumed from the queue
	 * 
	 */
	public int clearQueue(String queue) throws Exception {
		BrokerSession bs = brokerConnection.getBrokerSession();
		int count = bs.clearQueue(queue);
		System.out.println(queue + " cleared. " + count + " messages consumed.");
		
		return count;
	}
	
	/**
	 * Clears the topic by reading all available messages. Acknowledges or commits depending on the configuration.
	 * Subscription must have been done before.
	 * 
	 * Returns message count that was consumed from the topic.
	 */
	public int clearTopic() throws JMSException {
		BrokerSession bs = brokerConnection.getBrokerSession();
		int count = bs.clearTopic();
		System.out.println("Topic cleared. " + count + " messages consumed.");
		
		return count;
	}
}
