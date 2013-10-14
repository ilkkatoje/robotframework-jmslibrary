package fi.toje.himmeli;

import javax.jms.Session;

import junit.framework.Assert;

import org.junit.Test;

import fi.toje.himmeli.jmslibrary.BrokerConnection;
import fi.toje.himmeli.jmslibrary.BrokerSession;

public class BrokerConnectionTest {

	@Test
	public void convertType0() throws Exception {
		int t = BrokerConnection.convertType("0");
		Assert.assertEquals(t, Session.SESSION_TRANSACTED);
	}
	
	@Test
	public void convertType1() throws Exception {
		int t = BrokerConnection.convertType("1");
		Assert.assertEquals(t, Session.AUTO_ACKNOWLEDGE);
	}
	
	@Test
	public void convertType2() throws Exception {
		int t = BrokerConnection.convertType("2");
		Assert.assertEquals(t, Session.CLIENT_ACKNOWLEDGE);
	}
	
	@Test
	public void convertType3() throws Exception {
		int t = BrokerConnection.convertType("3");
		Assert.assertEquals(t, Session.DUPS_OK_ACKNOWLEDGE);
	}
	
	@Test(expected=Exception.class)
	public void convertType4() throws Exception {
		int t = BrokerConnection.convertType("4");
	}
	
	@Test
	public void convertTypeSessionTransacted() throws Exception {
		int t = BrokerConnection.convertType(BrokerSession.SESSION_TRANSACTED);
		Assert.assertEquals(t, Session.SESSION_TRANSACTED);
	}
	
	@Test
	public void convertTypeAutoAcnowledge() throws Exception {
		int t = BrokerConnection.convertType(BrokerSession.AUTO_ACKNOWLEDGE);
		Assert.assertEquals(t, Session.AUTO_ACKNOWLEDGE);
	}
	
	@Test
	public void convertTypeClientAcknowledge() throws Exception {
		int t = BrokerConnection.convertType(BrokerSession.CLIENT_ACKNOWLEDGE);
		Assert.assertEquals(t, Session.CLIENT_ACKNOWLEDGE);
	}
	
	@Test
	public void convertTypeDupsOkAcknowledge() throws Exception {
		int t = BrokerConnection.convertType(BrokerSession.DUPS_OK_ACKNOWLEDGE);
		Assert.assertEquals(t, Session.DUPS_OK_ACKNOWLEDGE);
	}
}
