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

    public void update(){
        if(inputQueue.size() != 0){
            String[] input = inputQueue.poll().split("\\.");
            if(input[0].equals("login")){
                if(input[1].equals("JRDZHA") && input[2].equals("Imjahrudz3!")){
                    outputQueue.add("login.successful");
                }
            } else if(input[0].equals("register")){
                outputQueue.add("register.successful");
            }
        }
    }

}
