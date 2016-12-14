package com.icegreen.greenmail.filestore.binary;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;

import com.icegreen.greenmail.filestore.FileHierarchicalFolder;
import com.icegreen.greenmail.filestore.FileStoreUtil;
import com.icegreen.greenmail.filestore.MessageEntry;
import com.icegreen.greenmail.filestore.UncheckedFileStoreException;
import com.icegreen.greenmail.filestore.fs.MessageToFS;
import com.icegreen.greenmail.foedus.util.MsgRangeFilter;
import com.icegreen.greenmail.imap.commands.IdRange;
import com.icegreen.greenmail.store.StoredMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binary file which contains some information for each message in a mailbox.
 * File name is normally greenmail.messageEntries.binary
 * See methods writeToDOS() and readFromDIS() for more information about the content of the file.
 */
public class MailboxEntries extends BaseBinarySettingsFile {
    final Logger log = LoggerFactory.getLogger(MailboxEntries.class);

    // Mailbox list which are stored in the mailboxEntriesFile, every change here must be synced to the FS:
    private ArrayList<MessageEntry> list = new ArrayList<>();

    public MailboxEntries(Path pathToEntriesFile) {
        super(pathToEntriesFile, "mailbox entries");
    }

    public int getMessageCount() {
        synchronized (this.syncLock) {
            return this.list.size();
        }
    }

    public int getUnseenCount() {
        int numUnSeen = 0;
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (!FileStoreUtil.isSeenFlagSet(entry.getFlagBitSet())) {
                    numUnSeen++;
                }
            }
        }
        return numUnSeen;
    }

    public int getRecentCount() {
        int numRecent = 0;
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (FileStoreUtil.isRecentFlagSet(entry.getFlagBitSet())) {
                    numRecent++;
                }
            }
        }
        return numRecent;
    }

    public int getFirstUnseen() {
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (!FileStoreUtil.isSeenFlagSet(entry.getFlagBitSet())) {
                    return entry.getMsgNum();
                }
            }
        }
        return -1;
    }

    public int getMsnForUID(long uid) {
        synchronized (this.syncLock) {
            for (MessageEntry e : this.list) {
                if (uid == e.getUid()) {
                    return e.getMsgNum();
                }
            }
        }
        // Not found
        return -1;
    }

    public List<MessageEntry> getMessagesByRangeFilterForMsgNum(MsgRangeFilter range) {
        ArrayList<MessageEntry> matchedMessages = new ArrayList<>();

        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (range.includes(entry.getMsgNum())) {
                    matchedMessages.add(entry);
                }
            }
        }

        return matchedMessages;
    }

    public List<MessageEntry> getMessagesByUIDBetween(long start, long end) {
        List<MessageEntry> matchedMessages = new ArrayList<>();

        // First: Filter the messages which we are going to return:
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (entry.getUid() >= start && entry.getUid() <= end) {
                    matchedMessages.add(entry);
                }
            }
        }

        return matchedMessages;
    }

    public List<MessageEntry> getMessagesByUIDFromArray(long[] uids) {
        List<MessageEntry> matchedMessages = new ArrayList<>();

        // First: Filter the messages which we are going to return:
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                for (long searchUid : uids) {
                    if (entry.getUid() == searchUid) {
                        matchedMessages.add(entry);
                        break;
                    }
                }
            }
        }
        return matchedMessages;
    }

    public long[] getMessageUidByRangeFilterForUid(IdRange[] uidRange) {
        ArrayList<Long>matchedUids = new ArrayList<>();
        synchronized (this.syncLock) {
            for (MessageEntry e : this.list) {
                if (includes(uidRange, e.getUid())) {
                    matchedUids.add(e.getUid());
                }
            }
        }
        long[] result = new long[matchedUids.size()];
        int index = 0;
        for (Long matchedUid : matchedUids) {
            result[index] = matchedUid.longValue();
            index++;
        }
        return result;
    }

    public long[] getMessageUidByRangeFilterForMsgNum(IdRange[] msgNumRange) {
        ArrayList<Long>matchedUids = new ArrayList<>();
        synchronized (this.syncLock) {
            for (MessageEntry e : this.list) {
                if (includes(msgNumRange, e.getMsgNum())) {
                    matchedUids.add(e.getUid());
                }
            }
        }
        long[] result = new long[matchedUids.size()];
        int index = 0;
        for (Long matchedUid : matchedUids) {
            result[index] = matchedUid.longValue();
            index++;
        }
        return result;
    }

    protected boolean includes(IdRange[] idSet, long id) {
        for (IdRange idRange : idSet) {
            if (idRange.includes(id)) {
                return true;
            }
        }
        return false;
    }

    public List<MessageEntry> getAllMessages() {
        ArrayList<MessageEntry> matchedMessages = new ArrayList<>();
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                matchedMessages.add(entry);
            }
        }
        return matchedMessages;
    }


    public long[] getAllMessageUids() {
        synchronized (this.syncLock) {
            int num = this.list.size();
            long[] ret = new long[num];
            for (int i = 0; i < num; i++) {
                ret[i] = this.list.get(i).getUid();
            }
            return ret;
        }
    }

    public List<MessageEntry> getNonDeletedMessages() {
        List<MessageEntry> matchedMessages = new ArrayList<>();

        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (!FileStoreUtil.isDeletedFlagSet(entry.getFlagBitSet())) {
                    matchedMessages.add(entry);
                }
            }
        }

        return matchedMessages;
    }

    public int createNewMessageEntry(MessageEntry entry) {
        int newIndex = 0;
        synchronized (this.syncLock) {
            entry.setMsgNum(this.list.size() + 1);
            newIndex = this.list.size();
            this.list.add(entry);
        }
        return newIndex;
    }

    public MessageEntry getMessageByUid(long uid) {
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (entry.getUid() == uid) {
                    return entry;
                }
            }
        }
        return null;
    }

    public MessageEntry getLastMessage() {
        synchronized (this.syncLock) {
            if (!this.list.isEmpty()) {
                return this.list.get(this.list.size() - 1);
            }
        }
        return null;
    }

    public MessageEntry setFlags(Flags flags, boolean value, long uid) {
        MessageEntry me = null;
        int meIndex = 0;

        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (entry.getUid() == uid) {
                    me = entry;
                    break;
                }
                meIndex++;
            }

            if (me != null) {
                log.debug("Found message where to set the flags: " + me.getUid());
                if (value) {
                    // Set the flags
                    log.debug("  Bitset before setting Flags: " + me.getFlagBitSet());
                    int flagsToSet = FileStoreUtil.convertFlagsToFlagBitSet(flags);
                    int newFlags = me.getFlagBitSet();
                    newFlags |= flagsToSet;
                    me.setFlagBitSet(newFlags);
                    log.debug("  Bitset after  setting Flags: " + me.getFlagBitSet());
                }
                else {
                    // TODO: Delete the flags
                    // if BIT is set, we should delete it in entr.flagBitSet... not yet implemented.
                }
            }

            this.storeFileToFSForSingleEntryWithoutSync(meIndex);
        }
        return me;
    }

    public MessageEntry replaceFlags(Flags flags, long uid) {
        MessageEntry me = null;
        int meIndex = 0;

        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (entry.getUid() == uid) {
                    me = entry;
                    break;
                }
                meIndex++;
            }
            if (me != null) {
                // Set the flags
                me.setFlagBitSet(FileStoreUtil.convertFlagsToFlagBitSet(flags));
            }

            this.storeFileToFSForSingleEntryWithoutSync(meIndex);
        }
        return me;
    }

    public List<MessageEntry> expunge(IdRange[] idRanges, Path mailboxDir) {
        ArrayList<MessageEntry> toDelete = new ArrayList<>();

        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                if (FileStoreUtil.isDeletedFlagSet(entry.getFlagBitSet()) && (idRanges == null || IdRange.containsUid(idRanges, entry.getUid()))) {
                    toDelete.add(entry);
                }
            }

            for (MessageEntry delEntry : toDelete) {
                log.debug("  Expunge message with uid: " + delEntry.getUid() + " and msgNum: " + delEntry.getMsgNum());

                // Step 1: Remove from list
                this.list.remove(delEntry);

                // Step 2: Delete file:
                try {
                    Path toDelPath = mailboxDir.resolve(delEntry.getShortFileName());
                    log.debug("  Delete file for expunged message: " + toDelPath.toString());
                    Files.delete(toDelPath);
                }
                catch (IOException io) {
                    // Ugly, but it is really not so important if the file cannog be deleted. Let's log it and go ahead.
                    //TODO: logging
                }
            }

            // Finally, we have to renumber the messages again, because messageNumber is actually just an 1-based index
            // into the list:
            int index = 1;
            for (MessageEntry entry : this.list) {
                entry.setMsgNum(index);
                index++;
            }

            this.storeFileToFSWithoutSync();
        }
        return toDelete;
    }

    public List<MessageEntry> cloneList() {
        List<MessageEntry> copyList = new ArrayList<>();
        synchronized (this.syncLock) {
            copyList.addAll(this.list);
        }
        return copyList;
    }

    public void deleteAllMessages(Path mailboxDir) {
        synchronized (this.syncLock) {
            for (MessageEntry entry : this.list) {
                Path fullpath = mailboxDir.resolve(entry.getShortFileName());
                if (Files.isRegularFile(fullpath)) {
                    try {
                        Files.delete(fullpath);
                    } catch (IOException ign) {
                        log.warn("Ignore IOException while deleting message with filename: " + fullpath, ign);
                    }
                }
            }
            this.list.clear();
            this.storeFileToFSWithoutSync();
        }
    }


    public void storeFileToFSWithoutSync() {
        try {
            try (OutputStream os = Files.newOutputStream(this.pathToBinaryFile, CREATE, WRITE, TRUNCATE_EXISTING); DataOutputStream dos = new DataOutputStream(os)) {
                for (MessageEntry me : list) {
                    this.writeToDOS(me, dos);
                }
                dos.flush();
            }
        }
        catch (IOException e) {
            String errorStr = "IOException happened while trying to write message file: " + this.pathToBinaryFile;
            log.error(errorStr, e);
            throw new UncheckedFileStoreException(errorStr, e);
        }
    }

    public void storeFileToFSForSingleEntry(int index) {
        synchronized (this.syncLock) {
            storeFileToFSForSingleEntryWithoutSync(index);
        }
    }


    /**
     * Writes only one single MessageEntry to file
     *
     * @param index - zero-based index into the list of MessageEntries
     */
    public void storeFileToFSForSingleEntryWithoutSync(int index) {
        ByteBuffer toWriteBuffer;
        try {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(MessageEntry.MSG_ENTRY_SIZE); DataOutputStream dos = new DataOutputStream(bos);) {
                MessageEntry me = this.list.get(index);
                this.writeToDOS(me, dos);
                toWriteBuffer = ByteBuffer.wrap(bos.toByteArray());
            }

            try (FileChannel fc = (FileChannel.open(this.pathToBinaryFile, WRITE, CREATE))) {
                ByteBuffer copy = ByteBuffer.allocate(MessageEntry.MSG_ENTRY_SIZE);
                long pos = index * MessageEntry.MSG_ENTRY_SIZE;
                fc.position(pos);
                fc.write(toWriteBuffer);
            }
        }
        catch (IOException e) {
            String errorStr = "IOException happened while trying to write one entry (index: " + index + ") to message file: " + this.pathToBinaryFile;
            log.error(errorStr, e);
            throw new UncheckedFileStoreException(errorStr, e);
        }
    }

    /**
     * Load the settings from the settings file from the file system.
     */
    public void loadFileFromFS(MessageToFS mtf) {
        synchronized (this.syncLock) {
            if (Files.isRegularFile(this.pathToBinaryFile)) {
                try {
                    try (InputStream is = Files.newInputStream(this.pathToBinaryFile); DataInputStream dis = new DataInputStream(is)) {
                        while (true) {
                            try {
                                MessageEntry e = new MessageEntry();
                                this.readFromDIS(e, dis);
                                this.list.add(e);
                            } catch (EOFException eof) {
                                // Good to know, let's get out of the loop now.
                                break;
                            }
                        }
                    }

                    // Make sure that we really delete messages which no longer exist on the file system:
                    boolean changedEntries = mtf.cleanupAfterLoading(this.list);

                    if (changedEntries) {
                        // cleanupAfterLoading changed the entries in the file, store them immediatly back to the FS
                        this.storeFileToFSWithoutSync();
                    }
                }
                catch (IOException e) {
                    String errorStr = "IOException happened while trying to read message file: " + this.pathToBinaryFile;
                    log.error(errorStr, e);
                    throw new UncheckedFileStoreException(errorStr,e);
                }
            }
        }
    }

    /**
     * Writes a single entry to the DataOutputStream
     */
    private void writeToDOS(MessageEntry me, DataOutputStream dos) throws IOException {
        // Make sure that writing is not exceeding MessageEntry.MSG_ENTRY_SIZE bytes
        dos.writeInt(me.getMsgNum());
        dos.writeLong(me.getUid());
        dos.writeInt(me.getFlagBitSet());
        dos.writeLong(me.getRecDateMillis());

        dos.writeLong(me.getPositionInMboxFile());
        dos.writeInt(me.getLenInMboxFile());
        // Do this in a backward compatible way: Only add additional properties at the end!
    }

    /**
     * Writes a single entry to the DataOutputStream
     */
    private void readFromDIS(MessageEntry me, DataInputStream dis) throws IOException {
        // Make sure that writing is not exceeding MessageEntry.MSG_ENTRY_SIZE bytes
        me.setMsgNum(dis.readInt());
        me.setUid(dis.readLong());
        me.setFlagBitSet(dis.readInt());
        me.setRecDateMillis(dis.readLong());

        me.setPositionInMboxFile(dis.readLong());
        me.setLenInMboxFile(dis.readInt());
        // Do this in a backward compatible way: Only add additional properties at the end!
    }

}
