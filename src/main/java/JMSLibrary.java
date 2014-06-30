import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import fi.toje.himmeli.jmslibrary.ProviderConnection;
import fi.toje.himmeli.jmslibrary.ProviderSession;
import fi.toje.himmeli.jmslibrary.Options;

/**
 * Robot Framework library for testing JMS applications.
 * 
 * Set the library and JMS provider jars into classpath and start testing.
 * 
 * Library uses one connection which has one session. Session includes one
 * message producer and one message consumer.
 * 
 * Default receive timeout is 100 ms.
 * 
 * = Example with ActiveMQ =
 * 
 * | *** Settings ***
 * | Library         JMSLibrary  ${INITIAL_CONTEXT_FACTORY}  ${PROVIDER_URL}
 * | Suite Setup     Connect And Start
 * | Suite Teardown  Close Connection
 * | 
 * | *** Variables ***
 * | ${INITIAL_CONTEXT_FACTORY}  org.apache.activemq.jndi.ActiveMQInitialContextFactory
 * | ${PROVIDER_URL}             tcp://localhost:61616?jms.useAsyncSend=false
 * | ${QUEUE}                    QUEUE.JMSLIBRARY.TEST
 * | ${TOPIC}                    TOPIC.JMSLIBRARY.TEST
 * | ${TEXT}                     Hello world!
 * | 
 * | *** Test Cases ***
 * | Queue Send and Receive TextMessage
 * |     [Setup]  Clear Queue Once  ${QUEUE}
 * |     Create Text Message  ${TEXT}
 * |     Send To Queue  ${QUEUE}
 * |     Receive Once From Queue  ${QUEUE}  
 * |     ${body}=  Get Text
 * |     Should Be Equal  ${body}  ${TEXT}
 * | 
 * | Topic Send and Receive TextMessage
 * |     [Setup]  Init Topic Consumer  ${TOPIC}
 * |     Create Text Message  ${TEXT}
 * |     Send To Topic  ${TOPIC}
 * |     Receive
 * |     ${body}=  Get Text
 * |     Should Be Equal  ${body}  ${TEXT}
 * |     [Teardown]  Close Consumer
 */
public class JMSLibrary {

	public static final String ROBOT_LIBRARY_SCOPE = "TEST SUITE";
	public static final String ROBOT_LIBRARY_VERSION = "1.0.0-beta.3-SNAPSHOT";
	
	private InitialContext initialContext;
	private ConnectionFactory connectionFactory;
	private ProviderConnection providerConnection;
	
	/**
	 * Settings for selecting JMS provider. Default JNDI connection factory
	 * look up string is 'ConnectionFactory'.
	 * 
	 * Optional settings:
	 * - _connection_factory_name_:  lookup name for connection factory
	 * 
	 * Examples:
	 * | Library | JMSLibrary | org.apache.activemq.jndi.ActiveMQInitialContextFactory | tcp://localhost:61616?jms.useAsyncSend=false |
	 * | Library | JMSLibrary | com.sun.jndi.fscontext.RefFSContextFactory | file:/C:/JNDI-Directory | connection_factory_name=myCF |
	 */
	public JMSLibrary(String initialContextFactory, String providerUrl, Map<String, String> settings) throws NamingException {
		Properties env = new Properties( );
		env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		env.put(Context.PROVIDER_URL, providerUrl);
		
		initialContext = new InitialContext(env);
		connectionFactory = (ConnectionFactory)initialContext.lookup(getConnectionFactoryLookupName(settings));
	}
	
	private String getConnectionFactoryLookupName(Map<String, String> settings) {
		String lookupName = Options.DEFAULT_CONNECTION_FACTORY_LOOKUP_NAME;
		if (settings.containsKey(Options.SETTINGS_KW_CONNECTION_FACTORY_LOOKUP_NAME)) {
			lookupName = settings.get(Options.SETTINGS_KW_CONNECTION_FACTORY_LOOKUP_NAME);
		}
		
		return lookupName;
	}
	
	/**
	 * Connects to provider. Does not initialize session or start connection.
	 */
	public void connect() throws Exception {
		connect(null, null);
	}
	
	/**
	 * Connects to provider. Does not initialize session or start connection.
	 */
	public void connect(String username, String password) throws Exception {
		if (providerConnection != null) {
			throw new Exception("Connection exists");
		}
		Connection connection;
		if (username != null) {
			connection = connectionFactory.createConnection(username, password);
		}
		else {
			connection = connectionFactory.createConnection();
		}
		
		providerConnection = new ProviderConnection(connection);
	}
	
	/**
	 * Connects to provider. Initializes default session and starts the
	 * connection.
	 */
	public void connectAndStart() throws Exception {
		connect();
		initSession();
		start();
	}
	
	/**
	 * Connects to provider. Initializes default session and starts the
	 * connection.
	 */
	public void connectAndStart(String username, String password) throws Exception {
		connect(username, password);
		initSession();
		start();
	}
	
	/**
	 * Sets clientId. Must be used right after connect, refer JMS specs.
	 */
	public void setClientId(String clientId) throws JMSException {
		providerConnection.setClientId(clientId);
		System.out.println("Client id '" + clientId + "' set.");
	}
	
	/**
	 * Returns clientId.
	 */
	public String getClientId() throws JMSException {
		return providerConnection.getClientId();
	}
	
	/**
	 * (Re)initializes session with default attributes (false,
	 * AUTO_ACKNOWLEDGE). Initializes also default producer.
	 */
	public void initSession() throws Exception {
		initSession(false, Options.AUTO_ACKNOWLEDGE);
	}
	
	/**
	 * (Re)initializes session. Initializes also default producer.
	 * 
	 * Arguments:
	 * - _transacted_: true or false
	 * - _type_: AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE or SESSION_TRANSACTED
	 */
	public void initSession(boolean transacted, String type) throws Exception {
		providerConnection.initSession(transacted, Options.convertType(type));
	}
	
	/**
	 * Starts connection.
	 */
	public void start() throws JMSException {
		providerConnection.start();
	}
	
	/**
	 * Stops connection.
	 */
	public void stop() throws JMSException {
		providerConnection.stop();
	}
	
	/**
	 * Closes provider connection. Closes all resources (session, producer and
	 * consumer).
	 */
	public void closeConnection() throws Exception {
		providerConnection.close();
		providerConnection = null;
	}
	
	/**
	 * Commits all messages in the session.
	 */
	public void commit() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.commit();
	}
	
	/**
	 * Rolls back messages in the session.
	 */
	public void rollback() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.rollback();
	}
	
	/**
	 * Acknowledges all consumed messages of the session. Used in
	 * CLIENT_ACKNOWLEDGE mode. Must be used before the consumer of the received
	 * message is closed or otherwise acknowledgement won't be sent.
	 */
	public void acknowledge() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.acknowledge();
	}
	
	/**
	 * Creates TextMessage. Additional properties can be set after creation.
	 */
	public void createTextMessage(String text) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.createTextMessage(text);
	}
	
	/**
	 * Creates BytesMessage from file. Additional properties can be set after
	 * creation.
	 * 
	 * Argument:
	 * - _file_: source file name
	 */
	public void createBytesMessageFromFile(String file) throws JMSException, IOException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.createBytesMessage(file);
	}
	
	/**
	 * Creates BytesMessage from text.
	 * 
	 * Argument:
	 * - _text_: text which is encoded to bytes
	 * - _charset_: target character set
	 */
	public void createBytesMessage(String text, String charset) throws JMSException, IOException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.createBytesMessage(text, charset);
	}
	
	/**
	 * Sets JMSType of message.
	 */
	public void setJMSType(String type) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.setJmsType(type);
	}
	
	/**
	 * Returns JMSType of message.
	 */
	public String getJMSType() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		return ps.getType();
	}
	
	/**
	 * Returns JMSPriority of message.
	 */
	public int getJMSPriority() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getJmsPriority();
	}
	
	/**
	 * Sets JMSCorrelationID for message.
	 */
	public void setJMSCorrelationId(String correlationId) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.setJmsCorrelationId(correlationId);
	}
	
	/**
	 * Returns JMSCorrelationID of message.
	 */
	public String getJMSCorrelationId() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		return ps.getJmsCorrelationId();
	}
	
	/**
	 * Sets JMSReplyTo queue for message.
	 */
	public void setJMSReplyToQueue(String queue) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.setJmsReplyToQueue(queue);
	}
	
	/**
	 * JMSReplyTo queue of message.
	 * 
	 * Returns queue if it was set and was type of queue, otherwise (not set or
	 * is topic) None.
	 */
	public String getJMSReplyToQueue() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getReplyToQueue();
	}
	
	/**
	 * Sets JMSReplyTo topic for message.
	 */
	public void setJMSReplyToTopic(String topic) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.setJmsReplyToTopic(topic);
	}
	
	/**
	 * JMSReplyTo value of message.
	 * 
	 * Returns topic if it was set and was type of topic, otherwise (not set or
	 * is queue) None.
	 */
	public String getJMSReplyToTopic() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getReplyToTopic();
	}
	
	/**
	 * Sets time to live for the producer.
	 * 
	 * Argument:
	 * - _timeToLive_: time to live in milliseconds
	 */
	public void setProducerTimeToLive(long timeToLive) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.getProducer().setTimeToLive(timeToLive);
	}
	
	/**
	 * Gets time to live of the producer.
	 * 
	 */
	public long getProducerTimeToLive() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getProducer().getTimeToLive();
	}
	
	/**
	 * JMSExpiration of message.
	 * 
	 * Returns expiration of message
	 */
	public long getJMSExpiration() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		return ps.getJmsExpiration();
	}
	
	/**
	 * Sets delivery mode for the producer.
	 * 
	 * Argument:
	 * - _deliveryMode_: PERSISTENT or NON_PERSISTENT
	 */
	public void setProducerDeliveryMode(String deliveryMode) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.getProducer().setDeliveryMode(Options.convertDeliveryMode(deliveryMode));
	}
	
	/**
	 * Returns priority of the producer.
	 */
	public int getProducerPriority() throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getProducer().getPriority();
	}
	
	/**
	 * Sets priority for the producer.
	 * 
	 * Argument:
	 * - _priority_: 0-9
	 */
	public void setProducerPriority(int priority) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.getProducer().setPriority(priority);
	}
	
	/**
	 * Returns delivery mode of the producer: PERSISTENT or NON_PERSISTENT.
	 */
	public String getProducerDeliveryMode() throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return Options.convertDeliveryMode(ps.getProducerDeliveryMode());
	}
	
	/**
	 * Returns delivery mode of message: PERSISTENT or NON_PERSISTENT.
	 */
	public String getJMSDeliveryMode() throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return Options.convertDeliveryMode(ps.getJmsDeliveryMode());
	}
	
	/**
	 * JMSRedelivered of received message.
	 * 
	 * Returns true if message was redelivered.
	 */
	public boolean getJMSRedelivered() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getJmsRedelivered();
	}
	
	/**
	 * Sets string property for message.
	 */
	public void setStringProperty(String name, String value) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.setStringProperty(name, value);
		System.out.println(name + "=" + value);
	}
	
	/**
	 * Returns string property of message.
	 * 
	 * Arguments:
	 * - _name_: name of the property
	 */
	public String getStringProperty(String name) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		String value = ps.getStringProperty(name);
		System.out.println(name + "=" + value);
		
		return value;
	}
	
	/**
	 * JMSMessageID of the message.
	 * 
	 * Returns message id.
	 */
	public String getJMSMessageId() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		String id = ps.getJmsMessageId();
		System.out.println("MessageId=" + id);
		
		return id;
	}
	
	/**
	 * Receives using the consumer. The message is set to internal message
	 * object and its body and properties can be accessed via methods.
	 * 
	 * `Init Queue Consumer`, `Init Topic Consumer` or `Init Durable Subscriber`
	 * must have been called before this.
	 * 
	 * Fails if message is not available.
	 */
	public void receive() throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.receive();
	}
	
	/**
	 * Similar as plain Receive but with additional timeout argument.
	 * 
	 * Argument:
	 * - _timeout_: receive timeout in milliseconds
	 */
	public void receive(long timeout) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.receive(timeout);
	}
	
	/**
	 * Sends message to queue. The message must have been created beforehand
	 * using one of the create message methods. Message id can be accessed after
	 * sending.
	 */
	public void sendToQueue(String queue) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.sendToQueue(queue);
	}
	
	/**
	 * Receives message from queue. Local MessageConsumer is created on the fly
	 * and closed after receiving. Acknowledges or commits depending on the
	 * session configuration. This can be called without preceding `Init
	 * Queue Consumer` keyword and there is no need to close the consumer with
	 * `Close Consumer` keyword. The message is set to internal message object
	 * and its body and properties can be accessed via methods.
	 * 
	 * Fails if message is not available.
	 */
	public void receiveOnceFromQueue(String queue) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.receiveOnceFromQueue(queue);
	}
	
	/**
	 * Similar as Receive Once From Queue but with additional timeout argument.
	 * 
	 * Arguments:
	 * - _queue_: name of the queue
	 * - _timeout_: receive timeout in milliseconds
	 */
	public void receiveOnceFromQueue(String queue, long timeout) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.receiveOnceFromQueue(queue, timeout);
	}
	
	/**
	 * Sends message to topic. The message must have been created beforehand
	 * using one of the create message methods. Message id can be accessed after
	 * sending.
	 */
	public void sendToTopic(String topic) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.sendToTopic(topic);
	}
	
	/**
	 * (Re)initializes the consumer as queue receiver. Previous consumer is
	 * closed before. Receive can be called after.
	 * 
	 * Argument:
	 * - _queue_: name of the queue
	 */
	public void initQueueConsumer(String queue) throws JMSException {
		initQueueConsumer(queue, false);
	}
	
	/**
	 * (Re)initializes the consumer as queue receiver. Previous consumer is
	 * closed before. Receive can be called after.
	 * 
	 * Arguments:
	 * - _queue_: name of the queue
	 * - _clear_: true, clears the destination after initialization.
	 */
	public void initQueueConsumer(String queue, boolean clear) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.initializeQueueConsumer(queue);
		if (clear) {
			int c = ps.clear();
			System.out.println("Consumer initialized for " + queue + " and " + c 
					+ " messages consumed.");
		}
		else {
			System.out.println("Consumer initialized for " + queue + ".");
		}
	}
	
	/**
	 * (Re)initializes the consumer as topic subscriber. Previous consumer is
	 * closed before. Receive can be called after.
	 * 
	 * Argument:
	 * - _topic_: name of the topic
	 */
	public void initTopicConsumer(String topic) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.initializeTopicConsumer(topic);
		System.out.println("Consumer initialized for " + topic + ".");
	}
	
	/**
	 * Closes the consumer. Possible durable subscription will still remain.
	 */
	public void closeConsumer() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.closeConsumer();
	}
	
	/**
	 * (Re)initializes the consumer as durable topic subscriber. Previous
	 * consumer is closed before. Receive can be called after.
	 * 
	 * Arguments:
	 * - _topic_: topic name
	 * - _name_: subscription name
	 */
	public void initDurableSubscriber(String topic, String name) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.initializeDurableSubscriber(topic, name);
	}
	
	/**
	 * Unsubscribes a durable subscription.
	 * 
	 * Argument:
	 * - _name_: subscription name
	 */
	public void unsubscribe(String name) throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.unsubscribe(name);
	}
	
	/**
	 * (Re)initializes producer with default settings, refer JMS specs.
	 */
	public void initProducer() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.initProducer();
	}
	
	/**
	 * (Re)initializes producer with the arguments.
	 * 
	 * Arguments:
	 * - _deliveryMode_: PERSISTENT or NON_PERSISTENT
	 * - _priority_: 0-9
	 * - _timeToLive_: milliseconds
	 */
	public void initProducer(String deliveryMode, int priority, long timeToLive) throws NumberFormatException, Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.initProducer(Options.convertDeliveryMode(deliveryMode), priority, timeToLive);
	}
	
	/**
	 * Returns the body of TextMessage.
	 */
	public String getText() throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getText();
	}
	
	/**
	 * Returns the body of BytesMessage as String.
	 * 
	 * Argument:
	 * - _charset_: character set of the binary body
	 */
	public String getBytesAsString(String charset) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		
		return ps.getBytesAsString(charset);
	}
	
	/**
	 * Writes body of BytesMessage into file. Overwrites if the file exists.
	 * 
	 * Arguments:
	 * - _file_: target file name
	 */
	public void writeBytesToFile(String file) throws JMSException, IOException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.writeBytes(file, false);
	}
	
	/**
	 * Writes body of BytesMessage into file.
	 * 
	 * Arguments:
	 * - _file_: target file name
	 * - _append_: true or false
	 */
	public void writeBytesToFile(String file, boolean append) throws JMSException, IOException {
		ProviderSession ps = providerConnection.getProviderSession();
		ps.writeBytes(file, append);
	}
	
	/**
	 * Returns queue depth. Implemented using QueueBrowser.
	 */
	public int queueDepth(String queue) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		int depth = ps.queueDepth(queue);
		System.out.println(queue + " depth is " + depth);
		
		return depth;
	}
	
	/**
	 * Clears the queue by reading all available messages. Acknowledges or
	 * commits depending on the session configuration.
	 * 
	 * Local MessageConsumer is created on the fly and closed after receiving.
	 * No need to call `Init Queue Consumer` before.
	 * 
	 * Note that all the messages might not be consumed if there is a consumer
	 * initialized for the same queue. This is because there might be a prefetch
	 * option at the provider.
	 * 
	 * Returns message count that was consumed from the queue.
	 */
	public int clearQueueOnce(String queue) throws Exception {
		ProviderSession ps = providerConnection.getProviderSession();
		int count = ps.clearQueue(queue);
		System.out.println(count + " messages consumed from " + queue + ".");
		
		return count;
	}
	
	/**
	 * Clears the destination of the consumer by reading all
	 * available messages. Acknowledges or commits depending on the
	 * session configuration.
	 * 
	 * `Init Queue Consumer`, `Init Topic Consumer` or `Init Durable Subscriber`
	 * must have been called before.
	 * 
	 * Returns message count that was consumed.
	 */
	public int clear() throws JMSException {
		ProviderSession ps = providerConnection.getProviderSession();
		int count = ps.clear();
		System.out.println(count + " consumed. ");
		
		return count;
	}
}
