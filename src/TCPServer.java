//Import necessary IO and NETwork libraries

import java.io.*;
import java.net.*;

/*
 * A simple example TCP Server application
 *
 * Computer Networks, KU Leuven.
 *
 */
class TCPServer {

    private int serverPort = 8080;
    private ServerSocket serverSocket = null;
    private boolean isStopped = false;
    private Thread runningThread = null;


    public static void main(String argv[]) throws Exception {
        // Create server (incoming) socket on port 6789.
        ServerSocket welcomeSocket = new ServerSocket(8080);
        System.out.println("Socket active");

        // Wait for a connection to be made to the server socket.
        while (true) {
            // Create	 a 'real' socket from the Server socket.
            Socket clientSocket = welcomeSocket.accept();
            new Thread(new RequestWorker(clientSocket)).start();
        }
    }

    private String makeHeaders() {
        return null;
    }

}