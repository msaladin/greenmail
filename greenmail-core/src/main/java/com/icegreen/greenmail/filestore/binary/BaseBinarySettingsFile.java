package com.icegreen.greenmail.filestore.binary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.icegreen.greenmail.filestore.FileHierarchicalFolder;
import com.icegreen.greenmail.filestore.UncheckedFileStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by saladin on 12/13/16.
 */
public abstract class BinarySettingsFile {
    final Logger log = LoggerFactory.getLogger(BinarySettingsFile.class);

    private Path pathToBinaryFile;
    private String fileTitle;

    protected BinarySettingsFile(Path thePath, String theTitle) {
        this.pathToBinaryFile = thePath;
        this.fileTitle = theTitle;
    }

    /**
     * Delete the setting file from the filesystem.
     */
    void deleteFileFromFS() {
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