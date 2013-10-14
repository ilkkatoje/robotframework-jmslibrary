package fi.toje.himmeli;

import javax.jms.DeliveryMode;

import org.junit.Assert;
import org.junit.Test;

import fi.toje.himmeli.jmslibrary.BrokerSession;

public class BrokerSessionTest {

	@Test
	public void convertDeliveryModePersistent() throws Exception {
		int dm = BrokerSession.convertDeliveryMode("PERSISTENT");
		Assert.assertEquals(dm, DeliveryMode.PERSISTENT);
	}
	
	@Test
	public void convertDeliveryModeNonPersistent() throws Exception {
		int dm = BrokerSession.convertDeliveryMode("NON_PERSISTENT");
		Assert.assertEquals(dm, DeliveryMode.NON_PERSISTENT);
	}
	
	@Test(expected=Exception.class)
	public void convertDeliveryModeWrong() throws Exception {
		int dm = BrokerSession.convertDeliveryMode("WRONG");
	}
	
	@Test
	public void convertDeliveryModePersistentInt() throws Exception {
		String dm = BrokerSession.convertDeliveryMode(DeliveryMode.PERSISTENT);
		Assert.assertEquals(dm, "PERSISTENT");
	}
	
	@Test
	public void convertDeliveryModeNonPersistentInt() throws Exception {
		String dm = BrokerSession.convertDeliveryMode(DeliveryMode.NON_PERSISTENT);
		Assert.assertEquals(dm, "NON_PERSISTENT");
	}
	
	@Test(expected=Exception.class)
	public void convertDeliveryModeWrongInt() throws Exception {
		String dm = BrokerSession.convertDeliveryMode(123);
	}
}
