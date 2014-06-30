package fi.toje.himmeli.jmslibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class ProviderSession {
	
	private static final int DEFAULT_BUFFER = 8192;
	private static final long DEFAULT_RECEIVE_TIMEOUT = 100;
	
	private Session session;
	private MessageProducer producer;
	private MessageConsumer consumer;
	private HashMap<String, Queue> queues;
	private HashMap<String, Topic> topics;
	private Message message;
	
	public ProviderSession(Session session) throws JMSException {
		this.session = session;
		queues = new HashMap<String, Queue>();
		topics = new HashMap<String, Topic>();
		initProducer();
	}
	
	public Session getSession() {
		return session;
	}
	
	public MessageProducer getProducer() {
		return producer;
	}
	
	public Message getMessage() {
		return message;
	}
	
	public void close() throws JMSException {
		if (producer != null) {
			producer.close();
		}
		if (consumer != null) {
			consumer.close();
		}
		if (session != null) {
			session.close();
		}
		if (message != null) {
			message = null;
		}
		queues.clear();
		topics.clear();
	}
	
	public void createTextMessage(String text) throws JMSException {
		message = session.createTextMessage(text);
	}
	
	/**
	 * @return text body of message
	 * @throws JMSException
	 */
	public String getText() throws JMSException {
		return ((TextMessage)message).getText();
	}
	
	public String getBytesAsString(String charset) throws JMSException, UnsupportedEncodingException {
		long length = ((BytesMessage)message).getBodyLength();
		byte[] bytes = new byte[(int)length];
		((BytesMessage)message).readBytes(bytes, (int)length);
		String text = new String(bytes, charset);
		
		return text;
	}
	
	/**
	 * Creates BytesMessage from file.
	 * 
	 * @param file
	 * @throws JMSException
	 * @throws IOException
	 */
	public void createBytesMessage(String file) throws JMSException, IOException {
		message = null;
		FileInputStream fis = new FileInputStream(new File(file));
		BytesMessage bytesMessage = session.createBytesMessage();
		byte[] bytes = new byte[DEFAULT_BUFFER];
		int c = 0;
		int bytesCount = 0;
		while ((c = fis.read(bytes)) > 0) {
			bytesCount += c;
			bytesMessage.writeBytes(bytes, 0, c);
		}
		System.out.println(bytesCount + " bytes read from " + file);
		message = bytesMessage;
		fis.close();
	}
	
	public void createBytesMessage(String text, String charset) throws JMSException, IOException {
		message = null;
		BytesMessage bytesMessage = session.createBytesMessage();
		byte[] bytes = text.getBytes(charset);
		bytesMessage.writeBytes(bytes);
		
		System.out.println(bytes.length + " wrote to message.");
		message = bytesMessage;
	}
	
	/**
	 * Writes BytesMessage's body into file.
	 * 
	 * @param file
	 * @throws IOException 
	 * @throws JMSException 
	 */
	public void writeBytes(String file, boolean append) throws JMSException, IOException {
		FileOutputStream fos = new FileOutputStream(new File(file), append);
		BytesMessage bytesMessage = (BytesMessage)message;
		byte[] bytes = new byte[DEFAULT_BUFFER];
		int c = 0;
		int bytesCount = 0;
		while ((c = bytesMessage.readBytes(bytes, DEFAULT_BUFFER)) > 0) {
			bytesCount += c;
			fos.write(bytes, 0, c);
		}
		System.out.println(bytesCount + " bytes wrote into " + file);
		fos.close();
	}
	
	public void setJmsType(String type) throws JMSException {
		message.setJMSType(type);
	}
	
	public String getType() throws JMSException {
		return message.getJMSType();
	}
	
	public boolean getJmsRedelivered() throws JMSException {
		return message.getJMSRedelivered();
	}
	
	public int getJmsPriority() throws JMSException {
		return message.getJMSPriority();
	}
	
	public void setJmsCorrelationId(String correlationId) throws JMSException {
		message.setJMSCorrelationID(correlationId);
	}
	
	public String getJmsCorrelationId() throws JMSException {
		return message.getJMSCorrelationID();
	}
	
	public void setJmsReplyToQueue(String queue) throws JMSException {
		Queue q = getQueue(queue);
		message.setJMSReplyTo(q);
	}
	
	/**
	 * Gets JMSReplyTo of message.
	 * 
	 * @return queue or null (if not set or is topic)
	 * @throws JMSException
	 */
	public String getReplyToQueue() throws JMSException {
		String ret = null;
		Destination d = message.getJMSReplyTo();
		System.out.println(d);
		if (d != null) {
			if (d instanceof Queue) {
				ret = ((Queue) d).getQueueName();
			}
			else {
				System.out.println("JMSReplyTo " + d + " is topic! Returns null.");
			}
		}
		
		return ret;
	}
	
	public void setJmsReplyToTopic(String topic) throws JMSException {
		Topic t = getTopic(topic);
		message.setJMSReplyTo(t);
	}
	
	/**
	 * Gets JMSReplyTo of message.
	 * 
	 * @return
	 * @throws JMSException
	 */
	public String getReplyToTopic() throws JMSException {
		String ret = null;
		Destination d = message.getJMSReplyTo();
		if (d != null) {
			if (d instanceof Topic) {
				ret = ((Topic) d).getTopicName();
			}
			else {
				System.out.println("JMSReplyTo " + d + " is queue! Returns null.");
			}
		}
		
		return ret;
	}
	
	/**
	 * Gets String representation or delivery mode.
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getProducerDeliveryMode() throws Exception {
		if (producer == null) {
			producer = session.createProducer(null);
		}
		
		return producer.getDeliveryMode();
	}
	
	public int getJmsDeliveryMode() throws Exception {
		return message.getJMSDeliveryMode();
	}
	
	/**
	 * Expiration of received message.
	 * 
	 * @return
	 * @throws JMSException
	 */
	public long getJmsExpiration() throws JMSException {
		return message.getJMSExpiration();
	}
	
	public void setStringProperty(String name, String value) throws JMSException {
		message.setStringProperty(name, value);
	}
	
	public String getStringProperty(String name) throws JMSException {
		return message.getStringProperty(name);
	}
	
	public String getJmsMessageId() throws JMSException {
		return message.getJMSMessageID();
	}
	
	public void sendToQueue(String queue) throws Exception {
		Queue q = getQueue(queue);
		
		producer.send(q, message);
	}
	
	public void receive() throws Exception {
		receive(DEFAULT_RECEIVE_TIMEOUT);
	}
	
	public void receive(long timeout) throws Exception {
		message = null;
		if (consumer != null) {
			message = consumer.receive(timeout);
			if (message == null) {
				throw new Exception("No message available.");
			}
		}
		else {
			throw new Exception("Consumer is not specified.");
		}
	}
	
	/**
	 * Receives message from queue. Creates consumer on the fly (does not use
	 * the ProviderSession's consumer).
	 * 
	 * @param queue
	 * @throws Exception if no message available
	 */
	public void receiveOnceFromQueue(String queue) throws Exception {
		receiveOnceFromQueue(queue, DEFAULT_RECEIVE_TIMEOUT);
	}
	
	/**
	 * Receives message from queue. Creates consumer on the fly (does not use
	 * the ProviderSession's consumer).
	 * 
	 * @param queue
	 * @param timeout
	 * @throws Exception
	 */
	public void receiveOnceFromQueue(String queue, long timeout) throws Exception {
		message = null;
		MessageConsumer queueConsumer = session.createConsumer(getQueue(queue));
		message = queueConsumer.receive(timeout);
		if (message != null) {
			if (session.getTransacted()) {
				session.commit();
			}
			else {
				if (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
					message.acknowledge();
				}
			}
		}
		queueConsumer.close();
		if (message == null) {
			throw new Exception("No message available.");
		}
	}
	
	public void sendToTopic(String topic) throws Exception {
		Topic t = getTopic(topic);
		
		producer.send(t, message);
	}
	
	/**
	 * Create queue consumer. Closes previous consumer if existed.
	 * 
	 * @param topic
	 * @throws JMSException
	 */
	public void initializeQueueConsumer(String queue) throws JMSException {
		Queue q = getQueue(queue);
		if (consumer != null) {
			consumer.close();
		}
		consumer = session.createConsumer(q);
	}
	
	/**
	 * Subscribes to topic. Closes (unsubscribes) previous consumer if existed.
	 * 
	 * @param topic
	 * @throws JMSException
	 */
	public void initializeTopicConsumer(String topic) throws JMSException {
		Topic t = getTopic(topic);
		if (consumer != null) {
			consumer.close();
		}
		consumer = session.createConsumer(t);
	}
	
	public void initProducer() throws JMSException {
		if (producer != null) {
			producer.close();
		}
		producer = session.createProducer(null);
	}
	
	public void initProducer(int deliveryMode, int priority, long timeToLive) throws JMSException {
		if (producer != null) {
			producer.close();
		}
		producer = session.createProducer(null);
		producer.setDeliveryMode(deliveryMode);
		producer.setPriority(priority);
		producer.setTimeToLive(timeToLive);
	}
	
	/**
	 * Just closes the consumer. Can be used also with
	 * durable subscription.
	 * 
	 * @throws JMSException
	 */
	public void closeConsumer() throws JMSException {
		if (consumer != null) {
			consumer.close();
		}
	}
	
	public void initializeDurableSubscriber(String topic, String name) throws JMSException {
		Topic t = this.getTopic(topic);
		if (consumer != null) {
			consumer.close();
		}
		consumer = session.createDurableSubscriber(t, name);
	}
	
	/**
	 * Unsubscribes durable topic subscription.
	 * 
	 * @param name
	 * @throws JMSException
	 */
	public void unsubscribe(String name) throws JMSException {
		session.unsubscribe(name);
	}
	
	/**
	 * Subscribe (Durable) must have been called before this.
	 * 
	 * @throws Exception
	 */
	public void receiveFromTopic() throws Exception {
		receiveFromTopic(DEFAULT_RECEIVE_TIMEOUT);
	}
	
	/**
	 * Subscribe (Durable) must have been called before this.
	 * 
	 * @param timeout
	 * @throws Exception
	 */
	public void receiveFromTopic(long timeout) throws Exception {
		message = null;
		message = consumer.receive(timeout);
		if (message == null) {
			throw new Exception("No message available");
		}
	}
	
	/**
	 * Acknowledges current message. Used if CLIENT_ACKNOWLEDGE mode in use.
	 * 
	 * @throws JMSException
	 */
	public void acknowledge() throws JMSException {
		message.acknowledge();
	}
	
	public void commit() throws JMSException {
		session.commit();
	}
	
	public void rollback() throws JMSException {
		session.rollback();
	}
	
	/**
	 * Calculates queue depth using QueueBrowser.
	 * 
	 * @param destination
	 * @return message count in queue
	 * @throws Exception
	 */
	public int queueDepth(String queue) throws Exception {
		int depth = 0;
		Queue q = getQueue(queue);
		QueueBrowser browser = session.createBrowser(q);
		Enumeration<?> e = browser.getEnumeration();
		while (e.hasMoreElements()) {
			e.nextElement();
			depth++;
		}
		browser.close();
		
		return depth;
	}
	
	/**
	 * Clears the queue by reading all available messages. Also acknowledges or
	 * commits depending on the configuration.
	 * 
	 * @param destination to be cleared
	 * @return message count that was consumed from the queue
	 * @throws JMSException
	 */
	public int clearQueue(String queue) throws JMSException {
		int count = 0;
		Message lastMessage = null;
		MessageConsumer queueConsumer = session.createConsumer(getQueue(queue));
		do {
			lastMessage = queueConsumer.receive(DEFAULT_RECEIVE_TIMEOUT);
			if (lastMessage != null) {
				count++;
				if (session.getTransacted()) {
					session.commit();
				}
				else {
					if (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
						lastMessage.acknowledge();
					}
				}
			}
		}
		while (lastMessage != null);
		
		queueConsumer.close();
		
		return count;
	}
	
	/**
	 * Clears topic. Topic can be non-durable or durable. Subscribe (Durable)
	 * must have been called before.
	 * 
	 * @return
	 * @throws JMSException
	 */
	public int clear() throws JMSException {
		int count = 0;
		Message lastMessage = null;
		do {
			lastMessage = consumer.receive(DEFAULT_RECEIVE_TIMEOUT);
			if (lastMessage != null) {
				count++;
				if (session.getTransacted()) {
					session.commit();
				}
				else {
					if (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
						lastMessage.acknowledge();
					}
				}
			}
		}
		while (lastMessage != null);
		
		return count;
	}
	
	/**
	 * Caches already created queues.
	 * 
	 * @param queue
	 * @return
	 * @throws JMSException
	 */
	private Queue getQueue(String queue) throws JMSException {
		Queue q;
		if (queues.containsKey(queue)) {
			q = queues.get(queue);
		}
		else {
			q = session.createQueue(queue);
			queues.put(queue, q);
		}
		
		return q;
	}
	
	/**
	 * Caches already created topics.
	 * 
	 * @param queue
	 * @return
	 * @throws JMSException
	 */
	private Topic getTopic(String topic) throws JMSException {
		Topic t;
		if (topics.containsKey(topic)) {
			t = topics.get(topic);
		}
		else {
			t = session.createTopic(topic);
			topics.put(topic, t);
		}
		
		return t;
	}
}
