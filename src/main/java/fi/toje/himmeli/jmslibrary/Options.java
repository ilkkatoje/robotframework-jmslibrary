package fi.toje.himmeli.jmslibrary;

import javax.jms.DeliveryMode;
import javax.jms.Session;

public class Options {

	public static final String DEFAULT_CONNECTION_FACTORY_LOOKUP_NAME = "ConnectionFactory";
	public static final String SETTINGS_KW_CONNECTION_FACTORY_LOOKUP_NAME = "connection_factory_name";
	public static final String SETTINGS_KW_CONNECT = "connect";
	public static final String SETTINGS_KW_CLIENT_ID = "client_id";
	public static final String SETTINGS_KW_START_CONNECTION = "start";
	public static final String SETTINGS_KW_TRANSACTED = "transacted";
	public static final String SETTINGS_KW_TYPE = "type";
	public static final String SETTINGS_KW_USERNAME = "username";
	public static final String SETTINGS_KW_PASSWORD = "password";
	
	public static final String AUTO_ACKNOWLEDGE = "AUTO_ACKNOWLEDGE";
	public static final String CLIENT_ACKNOWLEDGE = "CLIENT_ACKNOWLEDGE";
	public static final String DUPS_OK_ACKNOWLEDGE = "DUPS_OK_ACKNOWLEDGE";
	public static final String SESSION_TRANSACTED = "SESSION_TRANSACTED";
	
	public static final String DELIVERY_MODE_PERSISTENT = "PERSISTENT";
	public static final String DELIVERY_MODE_NON_PERSISTENT = "NON_PERSISTENT";
	
	public static final String DESTINATION_TYPE_QUEUE = "queue";
	public static final String DESTINATION_TYPE_TOPIC = "topic";
	
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
				throw new Exception("Invalid delivery mode.");
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
			throw new Exception("Invalid delivery mode.");
		}
		
		return dm;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static int convertType(String type) throws Exception {
		int t = 0;
		if (SESSION_TRANSACTED.equals(type) || String.valueOf(Session.SESSION_TRANSACTED).equals(type)) {
			t = Session.SESSION_TRANSACTED;
		}
		else if (CLIENT_ACKNOWLEDGE.equals(type) || String.valueOf(Session.CLIENT_ACKNOWLEDGE).equals(type)) {
			t = Session.CLIENT_ACKNOWLEDGE;
		}
		else if (DUPS_OK_ACKNOWLEDGE.equals(type) || String.valueOf(Session.DUPS_OK_ACKNOWLEDGE).equals(type)) {
			t = Session.DUPS_OK_ACKNOWLEDGE;
		}
		else if (AUTO_ACKNOWLEDGE.equals(type) || String.valueOf(Session.AUTO_ACKNOWLEDGE).equals(type)) {
			t = Session.AUTO_ACKNOWLEDGE;
		}
		else {
			throw new Exception("Invalid type: " + type + ".");
		}
		
		return t;
	}
}
