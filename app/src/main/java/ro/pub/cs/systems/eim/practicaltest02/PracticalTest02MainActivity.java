package ro.pub.cs.systems.eim.practicaltest02;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PracticalTest02MainActivity extends AppCompatActivity {

    private EditText portEditText;
    private EditText currencyEditText;
    private TextView resultTextView;
    private Button clientStartButton;
    private Button serverStartButton;

    private ServerThread serverThread = null;
    private ClientThread clientThread = null;

    private ServerStartClickListener serverStartClickListener = new ServerStartClickListener();
    private class ServerStartClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String serverPort = portEditText.getText().toString();
            if (serverPort == null || serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                return;
            }
            serverThread.start();
        }
    }

    private ClientStartClickListener clientStartClickListener = new ClientStartClickListener();
    private class ClientStartClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String port = portEditText.getText().toString();
            if (port == null || port.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }
            String currency = currencyEditText.getText().toString();
            if (currency == null || currency.isEmpty()
                    || !(currency.equals(Constants.EUR_STRING) || currency.equals(Constants.USD_STRING))) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Currency can only be USD or EUR", Toast.LENGTH_SHORT).show();
                return;
            }

            resultTextView.setText(Constants.EMPTY_STRING);

            clientThread = new ClientThread(currency, Integer.parseInt(port), resultTextView);
            clientThread.start();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");
        setContentView(R.layout.activity_practical_test02_main);

        portEditText = findViewById(R.id.portEditText);
        currencyEditText = findViewById(R.id.currencyEditText);
        resultTextView = findViewById(R.id.resultTextView);
        clientStartButton = findViewById(R.id.clientStartButton);
        clientStartButton.setOnClickListener(clientStartClickListener);
        serverStartButton = findViewById(R.id.serverStartButton);
        serverStartButton.setOnClickListener(serverStartClickListener);
    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}
