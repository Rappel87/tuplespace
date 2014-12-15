package chat;

import tupleserver.TupleProxy;
import tuplespaces.TupleSpace;

import java.util.HashMap;

public class ChatServer {
  // Add stuff here.
  private TupleSpace t;
  private int rows;
  private String[] channelNames;
  protected final static int DEBUG = 1;
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
  protected final static String FIRST_MESSAGE_ID = "1";
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
      t.put("channel",channel, FIRST_MESSAGE_ID, Integer.toString(0), Integer.toString(0), IS_NOT_FULL_TXT, Integer.toString(0));
      t.put ("capacity", Integer.toString (rows));
      sb.append(channel);
      sb.append('\0');
    }

    t.put("channelsList", sb.toString());
  }

  public ChatServer(TupleSpace t) {
    this.t = t;
    this.rows = Integer.parseInt (t.read ("capacity", null)[1]);

    // store internally the channels and a particular tuple channelNames
  }

  public String[] getChannels() {
    String[] channelListTuple = t.read("channelsList",null);
    String[] channelsList = channelListTuple[1].split("\0");
    return channelsList;
  }

  public void writeMessage(String channel, String message) {
    int msg_count, lastMsgId, nbr_listener, firstMsgId, readCnt;
    String [] firstMessage = null;
    String[] chann_tuple = null;
    
    // the write blocks if the string isFull is in the channel tuple instead of isNotFull
    chann_tuple = t.get("channel", channel, null, null, null, IS_NOT_FULL_TXT, null);
    
    msg_count = Integer.parseInt(chann_tuple[MSG_CNT]);
    firstMsgId = Integer.parseInt(chann_tuple[FIRST_MSG_ID]);
    lastMsgId = Integer.parseInt(chann_tuple[LAST_MSG_ID]);
    nbr_listener = Integer.parseInt(chann_tuple[LISTENER_CNT]);

    lastMsgId++;

    if (DEBUG > 0)
    {
      System.out.println ("CS - try to write: Chan: " + channel + " ID: " + Integer.toString (lastMsgId) + " Message: " + message);
    }

    t.put("message",channel, Integer.toString(lastMsgId) ,message, Integer.toString(nbr_listener));
    if (msg_count < rows) /*  */
    {
      msg_count++;
      t.put("channel", channel, chann_tuple[FIRST_MSG_ID], Integer.toString(lastMsgId), Integer.toString(msg_count),
          IS_NOT_FULL_TXT,Integer.toString(nbr_listener));
    }
    else
    {
      firstMessage = t.get("message", channel, Integer.toString (firstMsgId), null, null);
      readCnt = Integer.parseInt (firstMessage[READ_CNT]);
      
      /* Check if we can remove a message */
      if (readCnt == 0)
      {
        /* CS - remove a message - don't need to push it back */
        chann_tuple[FIRST_MSG_ID] = Integer.toString(firstMsgId + 1);
        t.put("channel", channel, chann_tuple[FIRST_MSG_ID], Integer.toString(lastMsgId), Integer.toString(msg_count),
            IS_NOT_FULL_TXT,Integer.toString(nbr_listener));
      }
      else
      {
        msg_count++;
        /* the channel is now full and we can't remove a message */
        t.put("channel", channel, chann_tuple[FIRST_MSG_ID], Integer.toString(lastMsgId), Integer.toString(msg_count),
            IS_FULL_TXT,Integer.toString(nbr_listener));
      }

    }

    if (DEBUG > 0)
    {
      System.out.println ("CS - finished writing: Chan: " + channel + " ID: " + Integer.toString (lastMsgId) + " Message: " + message);
    }

  }

  public ChatListener openConnection(String channel) {
    //channelStatus, channelName, firstMsgId, lastMsgId, messageCount, isFull/isNotFull, countListeners
    int msgCount, msgListener, firstMsgId, LastMsgId;
    String[] message  = new String[5];
    String[] chann_tuple = null;
    
    chann_tuple = t.get("channel",channel, null, null, null,null,null);
    System.out.println ("Fetched channel status");
    //update channel listener
    msgCount = Integer.parseInt(chann_tuple[MSG_CNT]);
    firstMsgId = Integer.parseInt(chann_tuple[FIRST_MSG_ID]);
    msgListener = Integer.parseInt(chann_tuple[LISTENER_CNT])+1;
    chann_tuple[LISTENER_CNT] = Integer.toString(msgListener);

    // update messages read_count
    System.out.println("message_count: " + msgCount);
    for (int i=firstMsgId; i<firstMsgId+msgCount;i++) {
      System.out.println ("Looping");
      message = t.get("message",channel,Integer.toString(i),null,null);
      message[READ_CNT] = Integer.toString(Integer.parseInt(message[READ_CNT]) + 1);
      t.put(message);
    }
    System.out.println ("Finished looping");

    t.put(chann_tuple);
    ChatListener listener = new ChatListener(channel, this.t, firstMsgId);
    System.out.println ("Created Chat Listener");

    return listener;
  }
}
