package com.icegreen.greenmail.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Test;

public class PropertiesBasedGreenMailConfigurationBuilderTest {

    @Test
    public void testBuildForSingleUser() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder.GREENMAIL_USERS,
                "foo1:pwd1@bar.com");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);

        assertNotNull(config);
        assertEquals(1, config.getUsersToCreate().size());
        assertEquals(new UserBean("foo1@bar.com", "foo1", "pwd1"), config.getUsersToCreate().get(0));
        assertNull(config.getMailsinkUser());
        assertFalse(config.hasMailsinkUser());
    }

    @Test
    public void testBuildForListOfUsers() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder.GREENMAIL_USERS,
                "foo1:pwd1@bar.com,foo2:pwd2,foo3:pwd3@bar3.com");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);

        assertNotNull(config);
        assertEquals(3, config.getUsersToCreate().size());
        assertEquals(new UserBean("foo1@bar.com", "foo1", "pwd1"), config.getUsersToCreate().get(0));
        assertEquals(new UserBean("foo2", "foo2", "pwd2"), config.getUsersToCreate().get(1));
        assertEquals(new UserBean("foo3@bar3.com", "foo3", "pwd3"), config.getUsersToCreate().get(2));
        assertNull(config.getMailsinkUser());
        assertFalse(config.hasMailsinkUser());
    }

    @Test
    public void testBuildWithAuthenticationDisabledSetting() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder.GREENMAIL_AUTH_DISABLED, "");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);

        assertNotNull(config);
        assertTrue(config.isAuthenticationDisabled());
        assertNull(config.getMailsinkUser());
        assertFalse(config.hasMailsinkUser());
    }

    @Test
    public void testBuildWithMailsinkUserWithDomain() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder.GREENMAIL_MAILSINK_USER, "foo1:pwd1@bar.com");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);

        assertNotNull(config);
        assertNotNull(config.getMailsinkUser());
        assertTrue(config.hasMailsinkUser());
        assertEquals(new UserBean("foo1@bar.com", "foo1", "pwd1"), config.getMailsinkUser());
        assertTrue(config.keepMailsinkInOriginalMailboxes());
    }

    @Test
    public void testBuildWithMailsinkUserWithoutDomain() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder.GREENMAIL_MAILSINK_USER, "foo1:pwd1");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);
        assertNotNull(config);
        assertNotNull(config.getMailsinkUser());
        assertTrue(config.hasMailsinkUser());
        assertEquals(new UserBean("foo1", "foo1", "pwd1"), config.getMailsinkUser());
        assertTrue(config.keepMailsinkInOriginalMailboxes());
    }

    @Test
    public void testBuildWithMailsinkPropertyFlag() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder
                .GREENMAIL_MAILSINK_KEEP_IN_ORIG_MBOX, "false");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);
        assertNotNull(config);
        assertFalse(config.keepMailsinkInOriginalMailboxes());
        assertNull(config.getMailsinkUser());
        assertFalse(config.hasMailsinkUser());
    }

    @Test
    public void testBuildWithNonDefaultStoreImplClass() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder.GREENMAIL_MAIL_STORE_IMPL_CLASS, "my.class");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);
        assertNotNull(config);
        assertEquals("my.class", config.getStoreClassImplementation());
    }

    @Test
    public void testBuildWithFileStoreRootDir() {
        Properties props = createPropertiesFor(PropertiesBasedGreenMailConfigurationBuilder.GREENMAIL_FILESTORE_ROOT_DIR,
                "/tmp/gugus");
        GreenMailConfiguration config = new PropertiesBasedGreenMailConfigurationBuilder().build(props);
        assertNotNull(config);
        assertEquals("/tmp/gugus", config.getFileStoreRootDirectory());
    }

    @Test
    public void testParseUserWithDomain() {
        String[] result= PropertiesBasedGreenMailConfigurationBuilder.parseUser("logon:password@domain.com");
        assertEquals(3, result.length);
        assertEquals("logon", result[0]);
        assertEquals("password", result[1]);
        assertEquals("domain.com", result[2]);
    }

    @Test
    public void testParseUserWithoutDomain() {
        String[] result= PropertiesBasedGreenMailConfigurationBuilder.parseUser("bill:gates");
        assertEquals(2, result.length);
        assertEquals("bill", result[0]);
        assertEquals("gates", result[1]);
    }

    private Properties createPropertiesFor(String key, String value) {
        Properties props = new Properties();
        props.setProperty(key, value);
        return props;
    }
}
