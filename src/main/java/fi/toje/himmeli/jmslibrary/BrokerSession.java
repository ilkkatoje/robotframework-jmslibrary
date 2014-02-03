package fi.toje.himmeli.jmslibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
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

public class BrokerSession {

	public static final String AUTO_ACKNOWLEDGE = "AUTO_ACKNOWLEDGE";
	public static final String CLIENT_ACKNOWLEDGE = "CLIENT_ACKNOWLEDGE";
	public static final String DUPS_OK_ACKNOWLEDGE = "DUPS_OK_ACKNOWLEDGE";
	public static final String SESSION_TRANSACTED = "SESSION_TRANSACTED";
	
	public static final String DELIVERY_MODE_PERSISTENT = "PERSISTENT";
	public static final String DELIVERY_MODE_NON_PERSISTENT = "NON_PERSISTENT";
	
	private static final int DEFAULT_BUFFER = 8192;
	private static final long DEFAULT_RECEIVE_TIMEOUT = 100;
	
	private Session session;
	private MessageProducer producer;
	private long timeToLive = 0;
	private int deliveryMode = DeliveryMode.PERSISTENT;
	private MessageConsumer topicConsumer;
	private HashMap<String, Queue> queues;
	private HashMap<String, Topic> topics;
	private Message message;
	
	public BrokerSession(Session session) throws JMSException {
		this.session = session;
		queues = new HashMap<String, Queue>();
		topics = new HashMap<String, Topic>();
	}
	
	public Session getSession() {
		return session;
	}
	
	public void close() throws JMSException {
		if (producer != null) {
			producer.close();
		}
		if (topicConsumer != null) {
			topicConsumer.close();
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
	
	/**
	 * Writes BytesMessage's body into file.
	 * 
	 * @param file
	 * @throws IOException 
	 * @throws JMSException 
	 */
	public void writeBytes(String file) throws JMSException, IOException {
		FileOutputStream fos = new FileOutputStream(new File(file));
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
	
	public void setType(String type) throws JMSException {
		message.setJMSType(type);
	}
	
	public String getType() throws JMSException {
		return message.getJMSType();
	}
	
	public boolean getJmsRedelivered() throws JMSException {
		return message.getJMSRedelivered();
	}
	
	public void setCorrelationId(String correlationId) throws JMSException {
		message.setJMSCorrelationID(correlationId);
	}
	
	public String getCorrelationId() throws JMSException {
		return message.getJMSCorrelationID();
	}
	
	public void setReplyToQueue(String queue) throws JMSException {
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
	
	/**
	 * 
	 * @param topic
	 * @throws JMSException
	 */
	public void setReplyToTopic(String topic) throws JMSException {
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
	 * @param timeToLive
	 * @throws JMSException
	 */
	public void setProducerTimeToLive(long timeToLive) throws JMSException {
		this.timeToLive = timeToLive;
		if (producer != null) { 
			producer.setTimeToLive(timeToLive);
		}
	}
	
	/**
	 * 
	 * @param deliveryMode, either string value or numeric value
	 * @throws Exception
	 */
	public void setProducerDeliveryMode(String deliveryMode) throws Exception {
		this.deliveryMode = convertDeliveryMode(deliveryMode);
		if (producer != null) {
			producer.setDeliveryMode(this.deliveryMode);
		}
	}
	
	/**
	 * Gets String representation or delivery mode.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getProducerDeliveryMode() throws Exception {
		int d;
		if (producer != null) {
			d = producer.getDeliveryMode();
		}
		else {
			d = deliveryMode;
		}
		
		return convertDeliveryMode(d);
	}
	
	/**
	 * Get delivery mode of message.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getDeliveryMode() throws Exception {
		return convertDeliveryMode(message.getJMSDeliveryMode());
	}
	
	/**
	 * 
	 * @param delivery
	 * @return
	 * @throws Exception
	 */
	public static String convertDeliveryMode(int delivery) throws Exception {
		String dm;
		switch(delivery) {
			case DeliveryMode.PERSISTENT:
				dm = DELIVERY_MODE_PERSISTENT;
				break;
			case DeliveryMode.NON_PERSISTENT:
				dm = DELIVERY_MODE_NON_PERSISTENT;
				break;
			default:
				throw new Exception("Invalid delivery mode");
		}
		
		return dm;
	}
	
	/**
	 * 
	 * @param delivery
	 * @return
	 * @throws Exception
	 */
	public static int convertDeliveryMode(String deliveryMode) throws Exception {
		int dm;
		if (DELIVERY_MODE_PERSISTENT.equals(deliveryMode) || String.valueOf(DeliveryMode.PERSISTENT).equals(deliveryMode)) {
			dm = DeliveryMode.PERSISTENT;
		}
		else if (DELIVERY_MODE_NON_PERSISTENT.equals(deliveryMode) || String.valueOf(DeliveryMode.NON_PERSISTENT).equals(deliveryMode)) {
			dm = DeliveryMode.NON_PERSISTENT;
		}
		else {
			throw new Exception("Invalid delivery mode");
		}
		
		return dm;
	}
	
	/**
	 * Expiration of received message.
	 * 
	 * @return
	 * @throws JMSException
	 */
	public long getExpiration() throws JMSException {
		return message.getJMSExpiration();
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @throws JMSException
	 */
	public void setStringProperty(String name, String value) throws JMSException {
		message.setStringProperty(name, value);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws JMSException
	 */
	public String getStringProperty(String name) throws JMSException {
		return message.getStringProperty(name);
	}
	
	/**
	 * 
	 * @return
	 * @throws JMSException
	 */
	public String getMessageId() throws JMSException {
		return message.getJMSMessageID();
	}
	
	/**
	 * 
	 * @param queue
	 * @throws Exception
	 */
	public void sendToQueue(String queue) throws Exception {
		Queue q = getQueue(queue);
		if (producer == null) {
			producer = getProducer();
		}
		
		producer.send(q, message);
	}
	
	/**
	 * Receives message from queue. Uses existing queueConsumer if available and having the same destination.
	 * 
	 * @param queue
	 * @throws Exception if no message available
	 */
	public void receiveFromQueue(String queue) throws Exception {
		receiveFromQueue(queue, DEFAULT_RECEIVE_TIMEOUT);
	}
	
	/**
	 * Receives message from queue. Uses existing queueConsumer if available and having the same destination.
	 * 
	 * @param queue
	 * @param timeout
	 * @throws Exception
	 */
	public void receiveFromQueue(String queue, long timeout) throws Exception {
		message = null;
		MessageConsumer queueConsumer = session.createConsumer(getQueue(queue));
		message = queueConsumer.receive(timeout);
		queueConsumer.close();
		if (message == null) {
			throw new Exception("No message available");
		}
	}
	
	/**
	 * 
	 * @param topic
	 * @throws Exception
	 */
	public void sendToTopic(String topic) throws Exception {
		Topic t = getTopic(topic);
		if (producer == null) {
			producer = getProducer();
		}
		
		producer.send(t, message);
	}
	
	/**
	 * Subscribes to topic. Closes (unsubscribes) previous consumer if existed.
	 * 
	 * @param topic
	 * @throws JMSException
	 */
	public void subscribe(String topic) throws JMSException {
		Topic t = getTopic(topic);
		if (topicConsumer != null) {
			topicConsumer.close();
		}
		topicConsumer = session.createConsumer(t);
	}
	
	/**
	 * Unsubscribes topic. Just closes the consumer. Can be used also with durable subscription.
	 * 
	 * @throws JMSException
	 */
	public void unsubscribe() throws JMSException {
		topicConsumer.close();
	}
	
	/**
	 * @param topic
	 * @param name
	 * @throws JMSException
	 */
	public void subscribeDurable(String topic, String name) throws JMSException {
		Topic t = this.getTopic(topic);
		if (topicConsumer != null) {
			topicConsumer.close();
		}
		topicConsumer = session.createDurableSubscriber(t, name);
	}
	
	/**
	 * Unsubscribes durable topic subscription. Closes topic consumer as well.
	 * 
	 * @param name
	 * @throws JMSException
	 */
	public void unsubscribeDurable(String name) throws JMSException {
		topicConsumer.close();
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
		message = topicConsumer.receive(timeout);
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
	
	/**
	 * 
	 * @throws JMSException
	 */
	public void commit() throws JMSException {
		session.commit();
	}
	
	/**
	 * 
	 * @throws JMSException
	 */
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
	 * Clears the queue by reading all available messages. Also acknowledges or commits depending on the configuration.
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
	 * Clears topic. Topic can be non-durable or durable. Subscribe (Durable) must have been called before.
	 * 
	 * @return
	 * @throws JMSException
	 */
	public int clearTopic() throws JMSException {
		int count = 0;
		Message lastMessage = null;
		do {
			lastMessage = topicConsumer.receive(DEFAULT_RECEIVE_TIMEOUT);
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
	 * Gets message producer. Sets producer attributes.
	 * 
	 * @return
	 * @throws JMSException
	 */
	private MessageProducer getProducer() throws JMSException {
		if (producer == null) {
			producer = session.createProducer(null);
			producer.setTimeToLive(timeToLive);
			producer.setDeliveryMode(deliveryMode);
		}
		
		return producer;
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
