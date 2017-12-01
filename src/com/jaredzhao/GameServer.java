package com.jaredzhao;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
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
                        System.out.println(client.getAddressAndPort() + " has connected");
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
                                if (input.equals("ping")) {
                                    client.lastPing = (int) (System.currentTimeMillis() / 1000);
                                    System.out.println("[" + client.uniqueID + "] " + client.getAddressAndPort() + " ping " + client.lastPing);
                                } else {
                                    client.inputQueue.put(input);
                                    System.out.println("[" + client.uniqueID + "] " + client.getAddressAndPort() + " " + input);
                                }
                            }
                            clients.put(client);
                        }
                        Thread.sleep(100);
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
                                output.writeBytes(client.outputQueue.poll() + "\n");
                                output.flush();
                            }
                            clients.put(client);
                        }
                        Thread.sleep(100);
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
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        deadCollectionCleanUpThread.start();
        return true;
    }
}