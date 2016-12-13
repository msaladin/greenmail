/*
 * Copyright (c) 2014 Wael Chatila / Icegreen Technologies. All Rights Reserved.
 * This software is released under the Apache license 2.0
 */
package com.icegreen.greenmail;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.imap.ImapHostManagerImpl;
import com.icegreen.greenmail.smtp.SmtpManager;
import com.icegreen.greenmail.store.InMemoryStore;
import com.icegreen.greenmail.store.Store;
import com.icegreen.greenmail.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wael Chatila
 * @version $Id: $
 * @since Jan 27, 2006
 */
public class Managers {
    final Logger log = LoggerFactory.getLogger(Managers.class);

    private ImapHostManager imapHostManager = null;
    private UserManager userManager = null;
    private SmtpManager smtpManager = null;

    /**
     * Public constructor wihtout Startup Configuration
     *
     * @deprecated Please use the constructor with a valid Startup Configuration
     */
    public Managers() {
        this(new InMemoryStore(null), new GreenMailConfiguration());
    }

    /**
     * Public constructor with startupConfiguration
     *
     * @param store - The store to use
     * @param startupConfig - The startup configuration
     */
    public Managers(Store store, GreenMailConfiguration startupConfig) {
        this.imapHostManager = new ImapHostManagerImpl(store);
        this.userManager = new UserManager(imapHostManager);
        this.smtpManager = new SmtpManager(imapHostManager, userManager, startupConfig);
    }

    public SmtpManager getSmtpManager() {
        return smtpManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public ImapHostManager getImapHostManager() {
        return imapHostManager;
    }

}
