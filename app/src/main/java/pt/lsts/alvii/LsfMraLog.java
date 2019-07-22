package pt.lsts.alvii;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCMessageType;
import pt.lsts.imc.lsf.LsfIndex;

/**
 * @author ZP
 * @author jqcorreia
 *
 */

class LsfMraLog extends IMraLog {
    LsfIndex index;
    String name;
    int curIndex = -1;
    int type;

    public LsfMraLog(LsfIndex index, String name) {
        this.index = index;
        this.name = name;
        type = index.getDefinitions().getMessageId(name);
        curIndex = index.getFirstMessageOfType(type);
    }

    protected void advanceUntil(long timestamp) {
        while (curIndex != -1 && currentTimeMillis() < timestamp)
            curIndex = index.getNextMessageOfType(name, curIndex);
    }

    @Override
    public IMCMessage getEntryAtOrAfter(long timestamp) {

        if (curIndex == -1)
            return null;

        if (timestamp / 1000 > index.timeOf(index.getLastMessageOfType(type)))
            return null;

        advanceUntil(timestamp);

        if (curIndex != -1)
            return index.getMessage(curIndex);

        return null;
    }

    public IMCMessage getEntryAtOrAfter(long timestamp, String entityName) {

        if (curIndex == -1)
            return null;

        if (timestamp / 1000 > index.timeOf(index.getLastMessageOfType(type)))
            return null;

        advanceUntil(timestamp);
        if (curIndex != -1) {
            IMCMessage msg = index.getMessage(curIndex);
            while (msg != null) {
                if (index.getEntityName(msg.getHeader().getInteger("src_ent")).equals(entityName)) {
                    return msg;
                }
                msg = nextLogEntry();
            }
        }

        return null;
    }

    public IMCMessage getEntryAtOrAfter(long timestamp, int source) {

        if (curIndex == -1)
            return null;

        if (timestamp / 1000 > index.timeOf(index.getLastMessageOfType(type)))
            return null;

        advanceUntil(timestamp);
        if (curIndex != -1) {
            IMCMessage msg = index.getMessage(curIndex);
            while (msg != null) {
                if (msg.getSrc()== source) {
                    return msg;
                }
                msg = nextLogEntry();
            }
        }

        return null;
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public IMCMessage getLastEntry() {
        curIndex = index.getLastMessageOfType(index.getDefinitions().getMessageId(name));
        if (curIndex == -1)
            return null;
        return index.getMessage(curIndex);
    }

    @Override
    public IMCMessageType format() {
        return index.getMessage(index.getFirstMessageOfType(name)).getMessageType();
    }

    @Override
    public LinkedHashMap<String, Object> metaInfo() {
        return new LinkedHashMap<String, Object>(); // For now return empty list //TODO
    }

    @Override
    public long currentTimeMillis() {
        if (curIndex == -1)
            return (long) (index.timeOf(index.getLastMessageOfType(name)) * 1000);
        return (long) (index.timeOf(curIndex) * 1000);
    }

    @Override
    public IMCMessage nextLogEntry() {

        curIndex = index.getNextMessageOfType(name, curIndex);

        if (curIndex == -1)
            return null;

        return index.getMessage(curIndex);
    }

    @Override
    public IMCMessage firstLogEntry() {
        curIndex = index.getFirstMessageOfType(name);
        return index.getMessage(curIndex);
    }

    @Override
    public void advance(long millis) {
        if (curIndex == -1)
            return;
        double before = currentTimeMillis();

        advanceUntil(currentTimeMillis() + millis);

        if (curIndex != -1 && currentTimeMillis() <= before) {
            // System.err.println("Messages of type "+name+" are not correctly sorted");
            curIndex = index.getNextMessageOfType(name, curIndex + 1);
        }
    }

    @Override
    public IMCMessage getCurrentEntry() {
        if (curIndex == -1)
            return null;
        return index.getMessage(curIndex);
    }

    @Override
    public Collection<IMCMessage> getExactTimeEntries(long timeStampMillis) {

        advanceUntil(timeStampMillis);

        Vector<IMCMessage> messages = new Vector<IMCMessage>();

        while (currentTimeMillis() == timeStampMillis)
            messages.add(getCurrentEntry());

        return messages;
    }

    private int numberOfEntries = -1;

    @Override
    public int getNumberOfEntries() {
        if (numberOfEntries == -1) {

            int count = 0;
            int type = index.getDefinitions().getMessageId(name);
            for (int i = index.getFirstMessageOfType(type); i != -1; i = index.getNextMessageOfType(type, i)) {

                count++;
            }
            numberOfEntries = count;
        }
        return numberOfEntries;
    }
}
