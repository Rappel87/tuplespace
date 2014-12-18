package chat;

import tuplespaces.TupleSpace;

public class ChatListener {

  private TupleSpace t;
  private String channel;
  private int currentMsgId;

  public ChatListener(String channel, TupleSpace t, int firstMsgId) {
    this.channel = channel;
    this.t = t;
    this.currentMsgId = firstMsgId;
  }

  // channel : channelStatus, channelName, firstMsgId, lastMsgId, messageCount, isFull/isNotFull, countListeners
  // message : messageStatus, channelName, messageId, messageContent, readCount
  public String getNextMessage() {
    int msgCount, readCount;
    String message;
    String[] channelTuple = null;
    String[] messageTuple = null;
    // may be deadlock
    // TO DO : try to first get channel tuple and then message tuple

    if (ChatServer.DEBUG > 0)
    {
      System.out.println ("CL - read: Chan: " + channel + " ID: " + Integer.toString (this.currentMsgId));
    }
    this.t.read("message", this.channel, Integer.toString (this.currentMsgId), null, null);
    channelTuple = this.t.get("channel",this.channel,null,null,null,null,null);

    messageTuple = this.t.get("message", this.channel, Integer.toString (this.currentMsgId), null, null);

    msgCount = Integer.parseInt(channelTuple[ChatServer.MSG_CNT]);
    readCount = Integer.parseInt(messageTuple[ChatServer.READ_CNT]);

    if (ChatServer.DEBUG > 0)
    {
      System.out.println ("CL - fetched: Chan: " + channel + " ID: " + Integer.toString (this.currentMsgId) +
          " readCount:" + readCount + " msgCount:" + msgCount);
    }
    message = messageTuple[ChatServer.MSG];
    // System.out.println("get message "+ channel +" : "+ message);
    this.currentMsgId++;

    if (readCount > 1) {
      messageTuple[ChatServer.READ_CNT] = Integer.toString(readCount - 1);
      t.put(messageTuple);
    }
    else if (readCount == 1) {
      //message is removed because everybody has read it
      channelTuple[ChatServer.MSG_CNT] = Integer.toString(msgCount-1);
      channelTuple[ChatServer.FIRST_MSG_ID] = Integer.toString(Integer.parseInt(channelTuple[ChatServer.FIRST_MSG_ID])+1);
      channelTuple[ChatServer.IS_FULL] = ChatServer.IS_NOT_FULL_TXT;
    }
    else {
        System.out.println("ERROR : readCount negative or null: "+readCount);
    }


    this.t.put(channelTuple);

    return message;
  }

  public void closeConnection() {
    int msgCount, msgListener,firstMsgId;
    String[] channTuple = t.get("channel",channel,null,null,null,null,null);
    msgCount = Integer.parseInt(channTuple[ChatServer.MSG_CNT]);
    msgListener = Integer.parseInt(channTuple[ChatServer.LISTENER_CNT]);
    firstMsgId = Integer.parseInt(channTuple[ChatServer.FIRST_MSG_ID]);
    channTuple[ChatServer.LISTENER_CNT] = Integer.toString(msgListener-1);
    System.out.println(msgCount);

    // update message reader_count
    String[] message = new String[5];
    for (int i=currentMsgId; i<msgCount-currentMsgId;i++) {
      message = t.get("message",channel,Integer.toString(i),null,null);
      message[ChatServer.READ_CNT] = Integer.toString(Integer.parseInt(message[ChatServer.READ_CNT]) - 1);
      if (Integer.parseInt(message[ChatServer.READ_CNT]) > 0)
        t.put(message);
    }
    t.put(channTuple);
  }
}
