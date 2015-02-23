import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;
import java.net.*;

/*
 * A simple example TCP Client application
 *
 * Computer Networks, KU Leuven.
 *
 */
public class TCPClient {


    String hostName;
    int portNumber;

    public TCPClient(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public static void main(String[] args) throws IOException {

        String hostName = "www.google.com";
        int portNumber = 80;

        TCPClient client = new TCPClient(hostName,portNumber);
        client.command();
    }


    public void command() throws IOException {
        BufferedReader inKeyboard =
                new BufferedReader(
                        new InputStreamReader(System.in));
        System.out.println("Give Command:");
        String command = inKeyboard.readLine();
        String[] commands = command.split(" ");
        switch(commands[0]) {
            case "GET": this.get(command);
            case "POST": this.post(command);
            case "HEAD": this.head(command);
        }

    }
    

    public void get(String command){
        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter outToServer =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader inFromServer =
                        new BufferedReader(
                                new InputStreamReader(echoSocket.getInputStream()));

        ) {

            outToServer.println(command);
            outToServer.println("Host: " + hostName);
            outToServer.println("");

            String response;
            StringBuilder completeResponse = new StringBuilder();
            while ((response = inFromServer.readLine()) != null)
            {
                System.out.println(response);
                completeResponse.append(response);
            }

            System.out.println(completeResponse);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }

    private void head(String command) {
        throw new UnsupportedOperationException();
    }

    private void post(String command) {
        throw new UnsupportedOperationException();
    }



}