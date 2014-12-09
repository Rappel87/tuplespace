package chat;

import sun.misc.REException;
import tupleserver.TupleProxy;
import tuplespaces.TupleSpace;

import java.util.HashMap;

public class ChatServer {
	// Add stuff here.
    private TupleSpace t;
    private int rows;
    private String[] channelNames;

    //tuple identifier
    //common
    protected final static int CHAN_NAME = 1;
    //channel tuple
    protected final static int FIRST_MSG_ID = 2;
    protected final static int LAST_MSG_ID = 3;
    protected final static int MSG_CNT = 4;
    protected final static int IS_FULL = 5;
    protected final static int LISTENER_CNT = 6;
    protected final static String IS_NOT_FULL_TXT = "isNotFull";
    protected final static String IS_FULL_TXT = "isFull";
    //message tuple
    protected final static int MSG_ID = 2;
    protected final static int MSG = 3;
    protected final static int READ_CNT = 4;



    // channel : channelStatus, channelName, firstMsgId, lastMsgId, messageCount, isFull/isNotFull, countListeners
    // message : messageStatus, channelName, messageId, messageContent, readCount
    //(identifier,channel name, nbr listener, msg count);
    //t.put("channel", channel, Integer.toString(msg_count),Integer.toString(nbr_listener));
	
	public ChatServer(TupleSpace t, int rows, String[] channelNames) {
		this.t = t;
        this.rows = rows;
        this.channelNames = channelNames;
        StringBuilder sb = new StringBuilder();
        for (String channel: channelNames) {
            t.put("channel",channel, Integer.toString(0), Integer.toString(0), Integer.toString(0), IS_NOT_FULL_TXT, Integer.toString(0));
            sb.append(channel);
            sb.append('\0');
        }

        t.put("channelsList", sb.toString());
	}

	public ChatServer(TupleSpace t) {
        this.t = t;
        // store internally the channels and a particular tuple channelNames
	}

	public String[] getChannels() {
        String[] channelListTuple = t.read("channelsList",null);
        String[] channelsList = channelListTuple[1].split("\0");
        return channelsList;
	}

	public void writeMessage(String channel, String message) {
        int msg_count, lastMsgId, nbr_listener;
        System.out.println("write message "+ channel +" : "+ message);
        // the write blocks if the string isFull is in the channel tuple instead of isNotFull
        String[] chann_tuple = t.get("channel", channel, null, null, null, IS_NOT_FULL_TXT, null);
        msg_count = Integer.parseInt(chann_tuple[MSG_CNT]);
        lastMsgId = Integer.parseInt(chann_tuple[LAST_MSG_ID]);
        nbr_listener = Integer.parseInt(chann_tuple[LISTENER_CNT]);
        msg_count++;
        lastMsgId++;
        t.put("message",channel, Integer.toString(lastMsgId) ,message, Integer.toString(nbr_listener));
        if (msg_count < rows)
            t.put("channel", channel, chann_tuple[FIRST_MSG_ID], Integer.toString(lastMsgId), Integer.toString(msg_count),
                    IS_NOT_FULL_TXT,Integer.toString(nbr_listener));
        else
            t.put("channel", channel, chann_tuple[FIRST_MSG_ID], Integer.toString(lastMsgId), Integer.toString(msg_count),
                    IS_FULL_TXT,Integer.toString(nbr_listener));
	}

	public ChatListener openConnection(String channel) {
        //channelStatus, channelName, firstMsgId, lastMsgId, messageCount, isFull/isNotFull, countListeners
        int msgCount, msgListener, firstMsgId, LastMsgId;
        String[] message  = new String[5];
        String[] chann_tuple = t.get("channel",channel, null, null, null,null,null);
        //update channel listener
        msgCount = Integer.parseInt(chann_tuple[MSG_CNT]);
        firstMsgId = Integer.parseInt(chann_tuple[FIRST_MSG_ID]);
        msgListener = Integer.parseInt(chann_tuple[LISTENER_CNT])+1;
        chann_tuple[LISTENER_CNT] = Integer.toString(msgListener);

        // update messages read_count
        System.out.println("message_count" + msgCount);
        for (int i=firstMsgId; i<firstMsgId+msgCount;i++) {
            message = t.get("message",channel,Integer.toString(i),null,null);
            message[READ_CNT] = Integer.toString(Integer.parseInt(message[READ_CNT]) + 1);
            t.put(message);
        }

        ChatListener listener = new ChatListener(channel, this.t);
        t.put(chann_tuple);


        return listener;
	}
}
