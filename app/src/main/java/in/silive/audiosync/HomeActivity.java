package in.silive.audiosync;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    static final ArrayList<Socket> connectedClient = new ArrayList<>() ;
    final String SERVER_IP = "localhost";
    final int PORT = 4326;
    private ServerSocket serverSocket;
    final String TAG = "HomeActivity";
    Thread server ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }


    public void connect(View v){
        Thread client1 = new Thread(new ClientThread("Client1"));

        Thread client2 = new Thread(new ClientThread("Client2"));
        client1.start();client2.start();
    }

    public void hostServer(View v)
    {
         server = new Thread(new ServerThread());
        server.start();

    }

    public class ServerThread implements Runnable{

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                Log.d(TAG,"Server Listening for clients.");
                while(true){
                    Socket client = serverSocket.accept();
                    connectedClient.add(client);
                    Log.d(TAG, "Client Connected");
                    InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine() ) !=null )
                    Log.d(TAG, "Message from Client : " + line);

                    PrintStream printStream = new PrintStream(client.getOutputStream());
                  //  printStream.print("You are connected");
                    InputStreamReader songInputStream = new InputStreamReader(getApplicationContext().getAssets().open("song.mp3"));
                    byte[] buffer = new byte[1024];
                    printStream.flush();
                    client.shutdownOutput();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public class ClientThread implements Runnable{
        String name;
        public ClientThread(String name){
            this.name = name;
        }
        @Override
        public void run() {
            try {
                Socket socket = new Socket(SERVER_IP,PORT);
                Log.d(TAG, "Connected to Server");
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                printStream.print(name);
                socket.shutdownOutput();

                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                Log.d(TAG, "Message from Server : " + bufferedReader.readLine());


            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
