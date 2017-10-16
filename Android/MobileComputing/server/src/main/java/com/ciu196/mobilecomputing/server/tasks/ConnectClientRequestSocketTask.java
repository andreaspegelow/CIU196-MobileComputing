package com.ciu196.mobilecomputing.server.tasks;

import com.ciu196.mobilecomputing.server.util.Server;

import java.io.IOException;

public class ConnectClientRequestSocketTask extends ServerTask {

    public ConnectClientRequestSocketTask(Server server) {
        super(server, 0);
    }

    @Override
    protected boolean init() {
        return true;
    }

    @Override
    protected boolean finish() {
        return true;
    }

    @Override
    protected boolean loop() {
        try {
            System.out.println("Waiting for clients to connects to port 1337");
            server.connectRequestSocket();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
