package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class ServerThread extends Thread {

    private int port = 0;
    private ServerSocket serverSocket = null;

    String rateUSD = null;
    String rateEUR = null;
    String lastUpdate = null;

    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
}

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public synchronized void setRates(String rateUSD, String rateEUR) {
        this.rateUSD = rateUSD;
        this.rateEUR = rateEUR;
    }
    public synchronized void setLastUpdate(String lastUpdate){
        this.lastUpdate = lastUpdate;
    }

    public synchronized String getRateUSD() {
        return rateUSD;
    }
    public synchronized String getRateEUR() {
        return rateEUR;
    }
    public synchronized String getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public void run() {
        try {
            updateRates();
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                updateRates();
                communicationThread.start();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    private void updateRates(){

        Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + Constants.EUR_STRING + Constants.WEB_ADDRESS_ENDING);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String pageSourceCode = null;
        try {
            pageSourceCode = httpClient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pageSourceCode == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
            return;
        }
        Document document = Jsoup.parse(pageSourceCode);
        Element element = document.child(0);
        Elements elements = element.getElementsByTag("body");
        for (Element elem: elements) {
            String elemData = elem.text();
            if (elemData.contains("EUR")) {
                int position = elemData.indexOf("EUR") + "EUR".length();
                elemData = elemData.substring(position);
                position = elemData.indexOf("rate") + 7;
                elemData = elemData.substring(position);
                rateEUR = elemData.substring(0, elemData.indexOf("\""));
            }
        }

        elements = element.getElementsByTag("body");
        for (Element elem: elements) {
            String elemData = elem.text();
            if (elemData.contains("USD")) {
                int position = elemData.indexOf("USD") + "USD".length();
                elemData = elemData.substring(position);
                position = elemData.indexOf("rate") + 7;
                elemData = elemData.substring(position);
                rateEUR = elemData.substring(0, elemData.indexOf("\""));
            }
        }

        elements = element.getElementsByTag("time");
        for (Element elem: elements) {
            String elemData = elem.data();
            try {
                JSONObject content = new JSONObject(elemData);
                lastUpdate = content.getString("updated");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}

