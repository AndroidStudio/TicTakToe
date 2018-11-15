package tic.tack.toe.arduino.sockets;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketManager extends Thread {

    //private static final String SERVER_ADDRESS = "40.118.44.196";//prod paweł
    //private static final String SERVER_ADDRESS = "176.119.44.252";//paweł
    //private static final String SERVER_ADDRESS = "192.168.1.41";//lokal witch
    private static final String SERVER_ADDRESS = "176.119.40.186";//paweł 15-11-2018 // prod


    private static final String TAG = "SocketManager";

    private final Handler responseHandler = new Handler(Looper.getMainLooper());
    private final Handler sendMessageHandler;

    private MessageListener mMessageListener = null;

    private Socket socket;

    private boolean running = true;
    private boolean ping = false;

    public SocketManager() {
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        sendMessageHandler = new Handler(handlerThread.getLooper());
    }

    public void setMessageListener(MessageListener messageListener) {
        this.mMessageListener = messageListener;
    }

    @Override
    public void run() {
        connectSocketServer();
    }

    private void connectSocketServer() {
        try {
            initSocket();
            sendUdid();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while (running) {
                        try {
                            if (ping)
                                ping();
                            Thread.sleep(3000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
            while (running) {
                readMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
            responseHandler.post(() -> {
                if (mMessageListener != null) {
                    mMessageListener.onConnectionError();
                }
            });
        }
    }

    private void sendUdid() {
        try {
            String udid = UDID.getUDID();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("udid", "udid");
            jsonObject.put("udid", udid);
            writeMessage(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        try {
            String udid = UDID.getUDID();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "init_game");
            jsonObject.put("udid", udid);
            writeMessage(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ping() {
        try {
            String udid = UDID.getUDID();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "ping");
            jsonObject.put("udid", udid);
            writeMessage(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reconnect() {
        Log.e(TAG, "reconnect");
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectSocketServer();
    }

    private void writeMessage(final String message) {
        sendMessageHandler.post(() -> {
            try {
                Log.e(TAG, "writeMessage: " + message);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                printWriter.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void readMessage() throws Exception {
        final InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        final String message = bufferedReader.readLine();

        if (message == null) {
            throw new Exception("Błąd połączenia");
        }

        Log.e(TAG, "readMessage: " + message);
        responseHandler.post(() -> {
            if (mMessageListener != null) {
                mMessageListener.onMessage(message);
            }
        });

    }

    private void initSocket() throws Exception {
        Log.e(TAG, "initSocket: " + SERVER_ADDRESS);
        socket = new Socket(SERVER_ADDRESS, 9696);
    }

    public void sendMessage(String message) {
        writeMessage(message);
    }

    public void startPing() {
        ping = true;
    }
}

