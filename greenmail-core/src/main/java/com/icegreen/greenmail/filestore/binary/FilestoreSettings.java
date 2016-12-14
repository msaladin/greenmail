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
 * Created by saladin on 12/13/16.
 */
public class FilestoreSettings extends BaseBinarySettingsFile {
    final Logger log = LoggerFactory.getLogger(FilestoreSettings.class);

    private long uidNextRange = -1;

    public FilestoreSettings(Path thePath) {
        super(thePath, "filestore settings");
    }

    public void setUidNextRange(long theRange) {
        this.uidNextRange = theRange;
    }

    public long getUidNextRange() {
        return this.uidNextRange;
    }

    /**
     * The settings file for the MBoxFileStore contains binary instance variables which need to be
     * persisted during invocations.
     */
    public void loadFileFromFS() {
        if (Files.isRegularFile(this.pathToBinaryFile)) {
            try {
                try (InputStream is = Files.newInputStream(this.pathToBinaryFile); DataInputStream dis = new DataInputStream(is)) {
                    this.readFromDIS(dis);
                }
            }
            catch (IOException e) {
                throw new UncheckedFileStoreException("IOException happened while trying to read the Filestore settings file: " + this.pathToBinaryFile,e);
            }
        }
    }

    /**
     * The settings file for the MBoxFileStore contains binary instance variables which need to be
     * persisted during invocations.
     */
    public void storeFileToFS() {
        try {
            try (OutputStream os = Files.newOutputStream(this.pathToBinaryFile, CREATE, WRITE, TRUNCATE_EXISTING); DataOutputStream dos = new DataOutputStream(os)) {
                this.writeToDOS(dos);
                // Do this in a backward compatible way: Only add additional properties at the end!
                dos.flush();
            }
        }
        catch (IOException e) {
            throw new UncheckedFileStoreException("IOException happened while trying to write the Filestore settings file: " + this.pathToBinaryFile, e);
        }
    }

    /**
     * Writes a single entry to the DataOutputStream
     */
    private void writeToDOS(DataOutputStream dos) throws IOException {
        dos.writeLong(this.uidNextRange);
        // Do this in a backward compatible way: Only add additional properties at the end!
    }

    /**
     * Writes a single entry to the DataOutputStream
     */
    private void readFromDIS(DataInputStream dis) throws IOException {
        this.uidNextRange = dis.readLong();
        // Do this in a backward compatible way: Only add additional properties at the end!
    }

}
