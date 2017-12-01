package com.jaredzhao;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {

    public Socket socket;
    public BlockingQueue<String> inputQueue, outputQueue;
    public int lastPing = (int)(System.currentTimeMillis() / 1000);
    public long uniqueID;

    public Client(Socket socket){
        uniqueID = UniqueIDSupplier.getNextID();
        this.socket = socket;
        inputQueue = new LinkedBlockingQueue<>();
        outputQueue = new LinkedBlockingQueue<>();
    }

    public String getAddressAndPort(){
        return socket.getRemoteSocketAddress() + ":" + socket.getLocalPort();
    }

}
