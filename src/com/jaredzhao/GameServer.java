package com.jaredzhao;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameServer{

    private ServerSocket serverSocket;
    private BlockingQueue<Client> clients;

    public GameServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new LinkedBlockingQueue<>();
    }

    public boolean startAcceptingThread() {
        Thread acceptingThread = new Thread("Accepting Thread") {
            public void run() {
                try{
                    while (true) {
                        Client client = new Client(serverSocket.accept());
                        clients.put(client);
                        System.out.println("[" + client.uniqueID + "] " + client.getAddressAndPort() + " - CONN - ");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        acceptingThread.start();
        return true;
    }

    public boolean startReceivingThread(){
        final Thread receivingThread = new Thread("Receiving Thread") {
            public void run() {
                try {
                    while(true) {
                        for(Client client = clients.poll(); client != null; client = clients.poll()) {
                            if (client.socket.getInputStream().available() > 0) {
                                String input = new BufferedReader(new InputStreamReader(client.socket.getInputStream())).readLine();
                                if (input.equals("PING")) {
                                    client.lastPing = (int) (System.currentTimeMillis() / 1000);
                                    System.out.println("[" + client.uniqueID + "] " + client.getAddressAndPort() + " - IN   - PING " + new Date());
                                    if(client.loggedIn()){
                                        client.outputQueue.add("PING.OK");
                                    } else {
                                        client.outputQueue.add("PING.ANON");
                                    }
                                } else {
                                    client.inputQueue.put(input);
                                    System.out.println("[" + client.uniqueID + "] " + client.getAddressAndPort() + " - IN   - " + input);
                                }
                            }
                            clients.put(client);
                        }
                        Thread.sleep(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        receivingThread.start();
        return true;
    }

    public boolean startSendingThread(){
        Thread sendingThread = new Thread("Sending Thread") {
            public void run() {
                try {
                    while(true) {
                        for(Client client = clients.poll(); client != null; client = clients.poll()) {
                            DataOutputStream output = new DataOutputStream(client.socket.getOutputStream());
                            while (client.outputQueue.size() != 0) {
                                String next_output = client.outputQueue.poll();
                                System.out.println("[" + client.uniqueID + "] " + client.getAddressAndPort() + " - OUT  - " + next_output);
                                output.writeBytes(next_output + "\n");
                                output.flush();
                            }
                            clients.put(client);
                        }
                        Thread.sleep(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        sendingThread.start();
        return true;
    }

    public boolean startDeadConnectionCleanUpThread(){
        Thread deadCollectionCleanUpThread = new Thread("Dead Connection Clean Up Thread") {
            public void run() {
                try {
                    while(true) {
                        for(Client client = clients.poll(); client != null; client = clients.poll()) {
                            if ((int)(System.currentTimeMillis() / 1000) - client.lastPing > 60) {
                                System.out.println("[" + client.uniqueID + "] " + client.getAddressAndPort() + " has timed out");
                            } else {
                                clients.put(client);
                            }
                        }
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        deadCollectionCleanUpThread.start();
        return true;
    }

    public boolean startClientHandleThread(){
        Thread clientHandleThread = new Thread("Client Handle Thread") {
            public void run() {
                try {
                    while(true) {
                        for(Client client = clients.poll(); client != null; client = clients.poll()) {
                            client.update();
                            clients.put(client);
                        }
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        clientHandleThread.start();
        return true;
    }
}
