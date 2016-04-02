package in.silive.audiosync;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    static final ArrayList<DatagramSocket> connectedClient = new ArrayList<>() ;
    final String SERVER_IP = "localhost";
    final int SERVER_PORT = 4326;
    final int CLIENT_PORT = 4321;
    private DatagramSocket serverSocket;
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
        byte[] recieveData = new byte[10240];
        byte[] sendData = new byte[10240];

        @Override
        public void run() {
            try {
                serverSocket = new DatagramSocket(SERVER_PORT);
                Log.d(TAG,"Server Listening for clients.");
                while(true){

                    DatagramPacket recievePacket = new DatagramPacket(recieveData,recieveData.length);
                    serverSocket.receive(recievePacket);
                    Log.d(TAG, "Client Connected");
                    String str = new String(recievePacket.getData(),0,recievePacket.getLength());
                    Log.d(TAG, "Client : " + str);
                    InputStream songStream = getApplicationContext().getAssets().open("song.mp3");
                    Thread streamer = new Thread(new ClientStreamWriter(recievePacket.getAddress(),
                            songStream,CLIENT_PORT));
                    streamer.start();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    public class ClientStreamWriter implements Runnable{
        InetAddress clientAddress;
        int port;
        InputStream is;
        byte[] buffer = new byte[10240];
        DatagramSocket clientSocket;

        public ClientStreamWriter( InetAddress clientAddress, InputStream is, int port) {

            this.clientAddress = clientAddress;
            this.is = is;
            this.port = port;
            try{
                    clientSocket = new DatagramSocket();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{
                while ((is.read(buffer,0,buffer.length))> -1 ){

                    DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,clientAddress,port);
                    clientSocket.send(sendPacket);
                    Thread.sleep(100);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public class ClientThread implements Runnable{
        String name;
        byte[] recieveData = new byte[10240];
        byte[] sendData = new byte[10240];
        public ClientThread(String name){
            this.sendData = name.getBytes();
        }
        @Override
        public void run() {
            try {

                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName(SERVER_IP),SERVER_PORT);
                DatagramSocket socket = new DatagramSocket();
                socket.send(sendPacket);

                DatagramSocket recieverSocket = new DatagramSocket(CLIENT_PORT);

                // Audio Stream initialisieren:
               int  iAudioBufSize       = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

                AudioTrack track    = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, iAudioBufSize, AudioTrack.MODE_STREAM);
                track.play();

                while (true) {
                    DatagramPacket recievePacket = new DatagramPacket(recieveData,recieveData.length);
                    recieverSocket.receive(recievePacket);
                   // DatagramPacket recievePacket = new DatagramPacket(recieveData, recieveData.length, InetAddress.getByName(SERVER_IP), SERVER_PORT);
                    String str = new String(recievePacket.getData(), 0, recievePacket.getLength());
                    Log.d(TAG, "Message from Server : " + recievePacket.getData());
                    track.write(recievePacket.getData(), 0, recievePacket.getLength());
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
