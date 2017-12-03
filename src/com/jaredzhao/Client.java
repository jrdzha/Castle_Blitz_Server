package com.jaredzhao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
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
                Iterator iterator = MongoDBAccessor.queryDocument(new BasicDBObject("username", input[1]));
                if(iterator.hasNext()){
                    Document document = (Document)iterator.next();
                    if(input[2].equals(document.getString("password"))){
                        outputQueue.add("login.successful");
                    } else {
                        outputQueue.add("login.fail1");
                    }
                } else {
                    outputQueue.add("login.fail2");
                }
            } else if(input[0].equals("register")){
                Iterator iterator = MongoDBAccessor.queryDocument(new BasicDBObject("username", input[1]));
                if(!iterator.hasNext()) {
                    MongoDBAccessor.insertDocument(
                            new Document("username", input[1])
                            .append("password", input[2])
                            .append("time-stamp", new Date(System.currentTimeMillis()).toString()));
                    outputQueue.add("register.successful");
                } else {
                    outputQueue.add("username.exists");
                }
            }
        }
    }

}
