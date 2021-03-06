package com.ciu196.mobilecomputing.server.tasks;

import com.ciu196.mobilecomputing.common.logging.GlobalLog;
import com.ciu196.mobilecomputing.server.util.Client;
import com.ciu196.mobilecomputing.server.util.Server;

import java.io.IOException;


public class ClientRequestFetcherTask extends ServerTask {

    private final Client client;

    public ClientRequestFetcherTask(final Client client, final Server server) {
        super(server);
        this.client = client;
    }

    @Override
    protected boolean init() {
        return true;
    }

    @Override
    protected boolean finish() {
        GlobalLog.log("Finishing request fetcher task for client: " + client.getInetAddress().getHostAddress());
        return true;
    }

    @Override
    protected boolean loop() {
        if (!client.isConnected())
            return false;
        try {
            client.fetchRequests();
        } catch (IOException | ClassNotFoundException e) {
            GlobalLog.log(e);
            return false;
        }
        return true;
    }

}
