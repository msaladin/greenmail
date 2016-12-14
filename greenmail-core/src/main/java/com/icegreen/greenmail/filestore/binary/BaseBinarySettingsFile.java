package com.icegreen.greenmail.filestore.binary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.icegreen.greenmail.filestore.UncheckedFileStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by saladin on 12/13/16.
 */
public abstract class BaseBinarySettingsFile {
    final Logger log = LoggerFactory.getLogger(BaseBinarySettingsFile.class);

    protected final Object syncLock = new Object();

    protected Path pathToBinaryFile;
    protected String fileTitle;

    protected BaseBinarySettingsFile(Path thePath, String theTitle) {
        this.pathToBinaryFile = thePath;
        this.fileTitle = theTitle;
    }

    /**
     * Delete the setting file from the filesystem.
     */
    public void deleteFileFromFS() {
        if (Files.isRegularFile(this.pathToBinaryFile)) {
            try {
                Files.delete(this.pathToBinaryFile);
            }
            catch (IOException e) {
                String errorStr = "IOException happened while trying to delete " + this.fileTitle + ": " + this.pathToBinaryFile;
                log.error(errorStr, e);
                throw new UncheckedFileStoreException(errorStr, e);
            }
        }
    }



}