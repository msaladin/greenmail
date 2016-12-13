package com.icegreen.greenmail.configuration;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates GreenMailConfiguration from properties.
 * <p/>
 * Example usage:
 * GreenMailConfiguration config = new PropertyBasedGreenMailConfigurationBuilder().build(System.getProperties());
 * <ul>
 * <li>greenmail.users :  List of comma separated users of format login:pwd[@domain][,login:pwd[@domain]]
 * <p>Example: user1:pwd1@localhost,user2:pwd2@0.0.0.0</p>
 * <p>Note: domain part must be DNS resolvable!</p>
 * </li>
 * </ul>
 */
public class PropertiesBasedGreenMailConfigurationBuilder {
    /**
     * Property for list of users.
     */
    public static final String GREENMAIL_USERS = "greenmail.users";

    // User pattern for user in the form: user:password
    private static final Pattern USER_PATTERN_2 = Pattern.compile("(.*):(.*)");

    // User pattern for user in the form: user:password@domain
    private static final Pattern USER_PATTERN_3 = Pattern.compile("(.*):(.*)@(.*)");

    /**
     * Disables authentication check.
     *
     * @see GreenMailConfiguration#withDisabledAuthentication()
     */
    public static final String GREENMAIL_AUTH_DISABLED = "greenmail.auth.disabled";
    /**
     * When set, all mails are copied or moved to the user defined as mailsink user in the format logon:password[@domain]
     * Default value: Not set
     */
    public static final String GREENMAIL_MAILSINK_USER = "greenmail.mailsink.user";
    /**
     * Boolean property, when set to true, the mails are kept in the original mailboxes and only copied to the mailsink
     * user. When set to false, the mails are moved to the mailsink user, the original mail users no longer received
     * their mails.
     *
     * Default value: true
     */
    public static final String GREENMAIL_MAILSINK_KEEP_IN_ORIG_MBOX = "greenmail.mailsink.keep.in.original.mailboxes";

    /**
     * The implementation of the Store interface. Current possible implementations: com.icegreen.greenmail.store.InMemoryStore
     * or com.icegreen.greenmail.filestore.MBoxFileStore
     */
    public static final String GREENMAIL_MAIL_STORE_IMPL_CLASS = "greenmail.mailstore.impl.class";

    /**
     * The root directory for the MBoxFileStore where all mailboxes with messages will be persisted.
     */
    public static final String GREENMAIL_FILESTORE_ROOT_DIR = "greenmail.filestore.rootdir";

    /**
     * Builds a configuration object based on given properties.
     *
     * @param properties the properties.
     * @return a configuration and never null.
     */
    public GreenMailConfiguration build(Properties properties) {
        GreenMailConfiguration configuration = new GreenMailConfiguration();
        String usersParam = properties.getProperty(GREENMAIL_USERS);
        if (null != usersParam) {
            String[] usersArray = usersParam.split(",");
            for (String user : usersArray) {
                extractAndAddUser(configuration, user);
            }
        }
        String disabledAuthentication = properties.getProperty(GREENMAIL_AUTH_DISABLED);
        if (null != disabledAuthentication) {
            configuration.withDisabledAuthentication();
        }
        String mailSinkUser = properties.getProperty(GREENMAIL_MAILSINK_USER);
        if (null != mailSinkUser) {
            extractAndMailsinkUser(configuration, mailSinkUser);
        }
        String mailSinkKeepInOrigMBoxes = properties.getProperty(GREENMAIL_MAILSINK_KEEP_IN_ORIG_MBOX);
        if (null != mailSinkKeepInOrigMBoxes) {
            String mskiomb = mailSinkKeepInOrigMBoxes.trim();
            if (Boolean.FALSE.toString().equals(mskiomb)) {
                configuration.withMailsinkKeepInOriginalMailboxes(false);
            }
            else {
                configuration.withMailsinkKeepInOriginalMailboxes(true);
            }
        }
        String implClass = properties.getProperty(GREENMAIL_MAIL_STORE_IMPL_CLASS);
        if (null != implClass) {
            configuration.withStoreClassImplementation(implClass);
        }
        String fileStoreRoot = properties.getProperty(GREENMAIL_FILESTORE_ROOT_DIR);
        if (null != fileStoreRoot) {
            configuration.withFileStoreRootDirectory(fileStoreRoot);
        }
        return configuration;
    }

    protected void extractAndMailsinkUser(GreenMailConfiguration configuration, String mailSinkUser) {
        String[] userParts = parseUser(mailSinkUser);
        switch (userParts.length) {
            case 2:
                configuration.withMailsinkUser(userParts[0], userParts[1]);
                break;
            case 3:
                configuration.withMailsinkUser(userParts[0] + '@' + userParts[2], userParts[0], userParts[1]);
                break;
            default:
                throw new IllegalArgumentException("Expected format login:pwd[@domain] but got " + mailSinkUser + " parsed to " + Arrays.toString(userParts) + " for property " + GREENMAIL_MAILSINK_USER);
        }
    }

    protected void extractAndAddUser(GreenMailConfiguration configuration, String user) {
        // login:pwd@domain
        String[] userParts = parseUser(user);
        switch (userParts.length) {
            case 2:
                configuration.withUser(userParts[0], userParts[1]);
                break;
            case 3:
                configuration.withUser(userParts[0] + '@' + userParts[2], userParts[0], userParts[1]);
                break;
            default:
                throw new IllegalArgumentException("Expected format login:pwd[@domain] but got " + user + " parsed to " + Arrays.toString(userParts));
        }
    }

    /**
     * Parses a single user in the format logon:password[@domain] where the domain part is optional. Valid
     * returned Strings arrays have 2 elements (without domain) or 3 elements (with domain).
     *
     * @param user - the user in format logon:password[@domain]
     * @return a String array with the different parts, either of length 2 or 3
     */
    protected static String[] parseUser(String user) {
        String userTrimmed = user.trim();

        // Try the pattern with domain first: user:password@domain
        Matcher m = USER_PATTERN_3.matcher(userTrimmed);
        if (!m.matches()) {
            // Try the pattern without domain next: user:password
            m = USER_PATTERN_2.matcher(userTrimmed);
        }

        if (m.matches()) {
            if (m.groupCount() >= 2) {
                // At least two capture group need to exist.
                String[] result = new String[m.groupCount()];
                result[0] = m.group(1);
                result[1] = m.group(2);
                if (m.groupCount() > 2) {
                    result[2] = m.group(3);
                }
                return result;
            }
        }

        // Regex was not successfull, just return a zero-length String array
        return new String[0];
    }

}
