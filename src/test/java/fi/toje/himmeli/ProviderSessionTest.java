package fi.toje.himmeli;

import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.junit.Assert;
import org.junit.Test;

import fi.toje.himmeli.jmslibrary.ProviderSession;

public class ProviderSessionTest {

	@Test
	public void convertDeliveryModePersistent() throws Exception {
		int dm = ProviderSession.convertDeliveryMode("PERSISTENT");
		Assert.assertEquals(dm, DeliveryMode.PERSISTENT);
	}
	
	@Test
	public void convertDeliveryModeNonPersistent() throws Exception {
		int dm = ProviderSession.convertDeliveryMode("NON_PERSISTENT");
		Assert.assertEquals(dm, DeliveryMode.NON_PERSISTENT);
	}
	
	@Test(expected=Exception.class)
	public void convertDeliveryModeWrong() throws Exception {
		ProviderSession.convertDeliveryMode("WRONG");
	}
	
	@Test
	public void convertDeliveryModePersistentInt() throws Exception {
		String dm = ProviderSession.convertDeliveryMode(DeliveryMode.PERSISTENT);
		Assert.assertEquals(dm, "PERSISTENT");
	}
	
	@Test
	public void convertDeliveryModeNonPersistentInt() throws Exception {
		String dm = ProviderSession.convertDeliveryMode(DeliveryMode.NON_PERSISTENT);
		Assert.assertEquals(dm, "NON_PERSISTENT");
	}
	
	@Test(expected=Exception.class)
	public void convertDeliveryModeWrongInt() throws Exception {
		ProviderSession.convertDeliveryMode(123);
	}
	
	@Test
	public void convertType0() throws Exception {
		int t = ProviderSession.convertType("0");
		Assert.assertEquals(t, Session.SESSION_TRANSACTED);
	}
	
	@Test
	public void convertType1() throws Exception {
		int t = ProviderSession.convertType("1");
		Assert.assertEquals(t, Session.AUTO_ACKNOWLEDGE);
	}
	
	@Test
	public void convertType2() throws Exception {
		int t = ProviderSession.convertType("2");
		Assert.assertEquals(t, Session.CLIENT_ACKNOWLEDGE);
	}
	
	@Test
	public void convertType3() throws Exception {
		int t = ProviderSession.convertType("3");
		Assert.assertEquals(t, Session.DUPS_OK_ACKNOWLEDGE);
	}
	
	@Test(expected=Exception.class)
	public void convertType4() throws Exception {
		ProviderSession.convertType("4");
	}
	
	@Test
	public void convertTypeSessionTransacted() throws Exception {
		int t = ProviderSession.convertType(ProviderSession.SESSION_TRANSACTED);
		Assert.assertEquals(t, Session.SESSION_TRANSACTED);
	}
	
	@Test
	public void convertTypeAutoAcnowledge() throws Exception {
		int t = ProviderSession.convertType(ProviderSession.AUTO_ACKNOWLEDGE);
		Assert.assertEquals(t, Session.AUTO_ACKNOWLEDGE);
	}
	
	@Test
	public void convertTypeClientAcknowledge() throws Exception {
		int t = ProviderSession.convertType(ProviderSession.CLIENT_ACKNOWLEDGE);
		Assert.assertEquals(t, Session.CLIENT_ACKNOWLEDGE);
	}
	
	@Test
	public void convertTypeDupsOkAcknowledge() throws Exception {
		int t = ProviderSession.convertType(ProviderSession.DUPS_OK_ACKNOWLEDGE);
		Assert.assertEquals(t, Session.DUPS_OK_ACKNOWLEDGE);
	}
}
