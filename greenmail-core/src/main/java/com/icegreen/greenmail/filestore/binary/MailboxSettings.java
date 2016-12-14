package com.icegreen.greenmail.filestore.binary;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.icegreen.greenmail.filestore.FileHierarchicalFolder;
import com.icegreen.greenmail.filestore.UncheckedFileStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binary file which contains the necessary settings per mailbox.
 * File name is normally greenmail.mailbox.binary
 * See methods writeToDOS() and readFromDIS() for more information about the content of the file.
 */
public class MailboxSettings extends BaseBinarySettingsFile {
    final Logger log = LoggerFactory.getLogger(FileHierarchicalFolder.class);

    // Settings which are stored in the mailboxSettingsFile:
    boolean isSelectable = false;

    public MailboxSettings(Path pathToSettingsFile) {
        super(pathToSettingsFile, "mailbox settings");
    }

    /**
     * Load the settings from the settings file from the file system.
     */
    public void loadFileFromFS() {
        synchronized (this.syncLock) {
            if (Files.isRegularFile(this.pathToBinaryFile)) {
                try {
                    try (InputStream is = Files.newInputStream(this.pathToBinaryFile); DataInputStream dis = new DataInputStream(is)) {
                        this.readFromDIS(dis);
                    }
                }
                catch (IOException e) {
                    String errorStr = "IOException happened while trying to read settings file: " + this.pathToBinaryFile;
                    log.error(errorStr, e);
                    throw new UncheckedFileStoreException(errorStr, e);
                }
            }
        }
    }

    public boolean isSelectable() {
        return this.isSelectable;
    }

    public void setSelectable(boolean theSelectableBool) {
        this.isSelectable = theSelectableBool;
    }

    /**
     * Store the setting file with the settings to the file system. Overwrite existing files.
     */
    public void storeFileToFS() {
        synchronized (this.syncLock) {
            try {
                try (OutputStream os = Files.newOutputStream(this.pathToBinaryFile, CREATE, WRITE, TRUNCATE_EXISTING); DataOutputStream dos = new DataOutputStream(os)) {
                    this.writeToDOS(dos);
                    dos.flush();
                }
            }
            catch (IOException e) {
                throw new UncheckedFileStoreException("IOException happened while trying to write settings file: " + this.pathToBinaryFile, e);
            }
        }
    }

    /**
     * Writes a single entry to the DataOutputStream
     */
    private void writeToDOS(DataOutputStream dos) throws IOException {
        dos.writeBoolean(this.isSelectable);
        // Do this in a backward compatible way: Only add additional properties at the end!
    }

    /**
     * Writes a single entry to the DataOutputStream
     */
    private void readFromDIS(DataInputStream dis) throws IOException {
        this.isSelectable = dis.readBoolean();
        // Do this in a backward compatible way: Only add additional properties at the end!
    }

}
