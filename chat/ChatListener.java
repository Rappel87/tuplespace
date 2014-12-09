package chat;

import tuplespaces.TupleSpace;

public class ChatListener {

    private TupleSpace t;
    private String channel;
    private int currentMsgId;

    public ChatListener(String channel, TupleSpace t) {
        this.channel = channel;
        this.t = t;
        this.currentMsgId = Integer.parseInt(t.read("channel",channel,null ,null, null, null, null)[ChatServer.FIRST_MSG_ID]);
    }

    // channel : channelStatus, channelName, firstMsgId, lastMsgId, messageCount, isFull/isNotFull, countListeners
    // message : messageStatus, channelName, messageId, messageContent, readCount
	public String getNextMessage() {
        // may be deadlock
        // TO DO : try to first get channel tuple and then message tuple
        String[] messageTuple = this.t.get("message", this.channel,Integer.toString(currentMsgId), null, null);
        String[] channelTuple = this.t.get("channel",this.channel,null,null,null,null,null);

        int msgCount = Integer.parseInt(channelTuple[ChatServer.MSG_CNT]);
        System.out.println("get next message : count"+ msgCount +" value : "+messageTuple[ChatServer.READ_CNT]);


        int read = Integer.parseInt(messageTuple[ChatServer.READ_CNT]);
        String message = messageTuple[ChatServer.MSG];
       // System.out.println("get message "+ channel +" : "+ message);
        if (read > 1) {
            messageTuple[ChatServer.READ_CNT] = Integer.toString(read-1);
            t.put(messageTuple);
        }
        else {
            //message is removed because everybody has read it
            channelTuple[ChatServer.MSG_CNT] = Integer.toString(msgCount-1);
            channelTuple[ChatServer.FIRST_MSG_ID] = Integer.toString(Integer.parseInt(channelTuple[ChatServer.FIRST_MSG_ID])+1);
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
        for (int i=firstMsgId; i<firstMsgId+msgCount;i++) {
            message = t.get("message",channel,Integer.toString(i),null,null);
            message[ChatServer.READ_CNT] = Integer.toString(Integer.parseInt(message[ChatServer.READ_CNT]) - 1);
            t.put(message);
        }
        t.put(channTuple);
    }
}
