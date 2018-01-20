package com.jaredzhao;

import java.io.*;
import java.util.Date;

public class Main {
    public static String version = "Build 5";

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
        gameServer.startClientHandleThread();

        System.out.println("Castle Blitz Server " + version);
        System.out.println("Port [" + port + "]");
        System.out.println("Started on " + new Date());
    }
}