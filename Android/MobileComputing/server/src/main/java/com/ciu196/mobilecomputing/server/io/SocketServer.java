package com.ciu196.mobilecomputing.server.io;

import com.ciu196.mobilecomputing.common.requests.ResponseValue;
import com.ciu196.mobilecomputing.common.requests.ServerResponse;
import com.ciu196.mobilecomputing.common.requests.ServerResponseType;
import com.ciu196.mobilecomputing.common.tasks.TaskManager;
import com.ciu196.mobilecomputing.server.tasks.ClientConnectionCheckerTask;
import com.ciu196.mobilecomputing.server.tasks.ClientRequestFetcherTask;
import com.ciu196.mobilecomputing.server.tasks.ClientRequestHandlerTask;
import com.ciu196.mobilecomputing.server.util.Client;
import com.ciu196.mobilecomputing.server.util.Server;

import static com.ciu196.mobilecomputing.common.Constants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class SocketServer implements Server {


    private final static String NOONE = "No-one";
    private volatile long broadcastStartTime = -1;
    private volatile boolean running = false;
    private volatile Client broadcaster = null;
    private volatile String broadcasterName = NOONE;
    private ServerSocket requestSocket, serverRequestSocket, dataSocket;
    private final Map<InetAddress, Client> clientMap;
    private final Set<Client> listeners;

    public SocketServer() {
        System.out.println("Allocating server memory.");
        clientMap = Collections.synchronizedMap(new HashMap<>());
        listeners = Collections.synchronizedNavigableSet(new TreeSet<>());
    }

    public void init() throws IOException {
        System.out.println("Opening server sockets");
        requestSocket = new ServerSocket(REQUEST_PORT);
        serverRequestSocket = new ServerSocket(SERVER_REQUEST_PORT);
        dataSocket = new ServerSocket(DATA_PORT);
        running = true;
    }

    public void connectRequestSocket() throws IOException {
        Socket clientSocket = requestSocket.accept();
        SocketClient client;

        if (clientMap.containsKey(clientSocket.getInetAddress())) {
            client = (SocketClient) clientMap.get(clientSocket.getInetAddress());
            detachClient(client);
        } else {
            client = new SocketClient(clientSocket.getInetAddress());
            clientMap.put(clientSocket.getInetAddress(), client);
        }

        client.bindRequestSocket(clientSocket);

        System.out.println("Client connected: "+clientSocket.getInetAddress().getHostAddress());
        client.sendResponse(new ServerResponse(ServerResponseType.REQUEST_ACCEPTED, new ResponseValue.NoValue()));

        ClientRequestFetcherTask fetcherTask = new ClientRequestFetcherTask(client, this);
        ClientRequestHandlerTask handlerTask = new ClientRequestHandlerTask(client, this);
        ClientConnectionCheckerTask connectionTask = new ClientConnectionCheckerTask(client, this);

        client.addTask(fetcherTask);
        client.addTask(handlerTask);
        client.addTask(connectionTask);

        new Thread(fetcherTask).start();
        new Thread(handlerTask).start();
        new Thread(connectionTask).start();
    }

    public void connectServerRequestSocket() throws IOException {
        Socket clientSocket = serverRequestSocket.accept();
        SocketClient client = (SocketClient) clientMap.get(clientSocket.getInetAddress());
        if (client != null) {
            System.out.println("Server request socket opened for client: "+clientSocket.getInetAddress().getHostAddress());
            client.bindServerRequestSocket(clientSocket);
        }
    }

    public void connectDataSocket() throws IOException {
        Socket clientSocket = dataSocket.accept();
        SocketClient client = (SocketClient) clientMap.get(clientSocket.getInetAddress());
        if (client != null) {
            System.out.println("Data socket opened for client: "+clientSocket.getInetAddress().getHostAddress());
            client.bindDataSocket(clientSocket);
        }
    }

    public void receiveData(final Client c) throws IllegalStateException, IOException {
        if (c == null || !c.equals(broadcaster))
            throw new IllegalStateException("Only the broadcaster is supposed to broadcast");

        //TODO Do something with data!
    }

    @Override
    public void sendStatus(final Client client) throws IOException {
        System.out.println("Sending status to client");
        ResponseValue.Status status = new ResponseValue.Status();
        ServerResponse response = new ServerResponse(ServerResponseType.STATUS, status);
        status.putStatus("broadcasting", Boolean.toString(broadcaster != null));
        status.putStatus("broadcaster", broadcasterName);
        status.putStatus("nListeners", Integer.toString(listeners.size()));
        status.putStatus("broadcastStartTime", Long.toString(broadcastStartTime));

        client.sendResponse(response);
    }

    public void sendData(final Client c) throws IOException {
        c.sendData();
    }

    public void setBroadcaster(final Client c, final String name) throws IOException {
        System.out.println("Setting broadcaster");
        if (broadcaster != null) {
            c.sendResponse(new ServerResponse(ServerResponseType.REQUEST_DECLINED, new ResponseValue.NoValue()));
            return;
        }
        broadcasterName = name;
        broadcaster = c;
        broadcastStartTime = System.currentTimeMillis();
        listeners.remove(c);
        c.sendResponse(new ServerResponse(ServerResponseType.REQUEST_ACCEPTED, new ResponseValue.NoValue()));
    }

    public void stopBroadcast(final Client c) throws IOException {
        stopBroadcast(c, true);
    }

    private void stopBroadcast(final Client c, boolean respond) throws IOException {
        if (c.equals(broadcaster)) {
            broadcaster = null;
            broadcasterName = NOONE;
            broadcastStartTime = -1;
            if (respond)
                c.sendResponse(new ServerResponse(ServerResponseType.REQUEST_ACCEPTED, new ResponseValue.NoValue()));
        } else if (respond) {
            c.sendResponse(new ServerResponse(ServerResponseType.REQUEST_DECLINED, new ResponseValue.NoValue()));
        }
    }

    public void detachClient(final Client c) throws IOException {
        detachClient(c, true);
    }

    public void detachClient(final Client c, boolean sendResponse) throws IOException {
        if (c == null)
            throw new IllegalArgumentException("Client may not be null");
        System.out.println("Detaching client: "+c.getInetAddress().getHostAddress());
        stopBroadcast(c, false);
        removeListener(c, false);
        if (sendResponse) {
            try {
                c.sendResponse(new ServerResponse(ServerResponseType.DETACHED, new ResponseValue.NoValue()));
            } catch (IOException e) {
                System.out.println("The socket was already closed");
            }
        }
        clientMap.remove(c.getInetAddress());
        c.close();

        System.out.println("Client detached");
    }

    public void addListener(Client c) throws IOException {
        if (!c.equals(broadcaster) && broadcaster != null) {
            listeners.add(c);
            c.sendResponse(new ServerResponse(ServerResponseType.REQUEST_ACCEPTED, new ResponseValue.NoValue()));
        } else {
            c.sendResponse(new ServerResponse(ServerResponseType.REQUEST_DECLINED, new ResponseValue.NoValue()));
        }

    }

    public void removeListener(Client c) throws IOException {
        removeListener(c, true);
    }

    @Override
    public Collection<Client> getClients() {
        return clientMap.values();
    }

    public void removeListener(Client c, boolean respond) throws IOException {
        listeners.remove(c);
        if (respond)
            c.sendResponse(new ServerResponse(ServerResponseType.REQUEST_ACCEPTED, new ResponseValue.NoValue()));

    }

    public void quit() {
        try {
            if (running) {
                listeners.clear();
                clientMap.clear();
                requestSocket.close();
                dataSocket.close();
                running = false;
            }
            TaskManager.getInstance().finishAllTasks();
            System.out.println("Server has been shut down");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
