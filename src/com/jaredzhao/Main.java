package com.jaredzhao;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java Castle/ Blitz/ Server/ Build/ XXX.jar [port]");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        GameServer gameServer = new GameServer(port);

        gameServer.startAcceptingThread();
        gameServer.startReceivingThread();
        gameServer.startSendingThread();
        gameServer.startDeadConnectionCleanUpThread();

        System.out.println("Castle Blitz Server started on port " + port);
    }
}