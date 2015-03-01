package Client;

import Helperclass.HTTPUtilities;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * A simple example TCP Client application
 *
 * Computer Networks, KU Leuven.
 *
 */
public class TCPClient {

    private String hostName;
    private int portNumber;

    private Socket responseSocket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;

    public TCPClient(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;

        try {
            responseSocket = new Socket(hostName, portNumber);
            outToServer =
                    new PrintWriter(responseSocket.getOutputStream(), true);
            inFromServer =
                    new BufferedReader(
                            new InputStreamReader(responseSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    public void command() throws IOException {
        BufferedReader inKeyboard =
                new BufferedReader(
                        new InputStreamReader(System.in));
        System.out.println("Give Command:");
        String command = inKeyboard.readLine();

        //headers
        String[] headers = new String[46];
        String header;
        int i = 0;
        while (!(header = inKeyboard.readLine()).equals("")) {
            headers[i] = header;
            i++;
        }

        //post||put data
        String postOrPutData = null;
        String httpType = HTTPUtilities.getHTTPCommand(command);
        if (httpType.equals("POST") || httpType.equals("PUT")) {
            postOrPutData = inKeyboard.readLine();
        }

        execute(command, headers, postOrPutData);
    }

    public void execute(String command, String[] headers, String postOrPutData) {
        try {
            outToServer.println(command);
            //needed for HTTP1.1, it's possible to have multiple adresses on 1 adress
            outToServer.println("Host: " + hostName);
            for (String header : headers) {
                if (header != null) {
                    outToServer.println(header);
                }
            }

            if (postOrPutData != null) {
                outToServer.println("");
                outToServer.println(postOrPutData);
            }

            //needed to end the http command
            outToServer.println("");

            handleResponse(HTTPUtilities.getHTTPCommand(command));

            if (!responseSocket.isClosed() && HTTPUtilities.getHTTPType(command).equals("1.1")) {
                command();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleResponse(String httpCommand) {
        try {
            String response;
            StringBuilder header = new StringBuilder();

            //First read all headers
            while (!(response = inFromServer.readLine()).equals("")) {
                System.out.println(response);
                header.append("\n").append(response);
            }


            if (!httpCommand.equals("HEAD")) {
                int cont_length = Integer.parseInt(HTTPUtilities.readHeader(header.toString(), "Content-Length"));

                StringBuilder content = new StringBuilder();
                while (content.toString().getBytes("UTF-8").length + 2 < cont_length) {
                    //  System.out.println(content.toString().getBytes("UTF-8").length);
                    response = inFromServer.readLine();
                    System.out.println(response);
                    content.append("\n").append(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Set<String> getImageSrces(String htmlString) {
        HashSet<String> set = new HashSet<>();
        Pattern p = Pattern.compile("<img.*src=\"([a-zA-Z0-9\\._\\-/:\\?=&]*)\" .*");
        Matcher m = p.matcher(htmlString);
        while (m.find()) {
            String src = m.group(1);
            set.add(src);
        }
        return set;
    }


    public String removeHeaders(String httpResponse) {
        return httpResponse.split("\\n\\n", 2)[1];
    }

    public String getHeaders(String httpResponse) {
        return httpResponse.split("\\n\\n", 2)[0];
    }

    public void saveImage(String imageString) {
        try {
            PrintWriter out = new PrintWriter("test.png");
            out.print(imageString);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String hostName = "localhost";
        int portNumber = 8080;

        TCPClient client = new TCPClient(hostName, portNumber);
        client.command();
    }


    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public Socket getResponseSocket() {
        return responseSocket;
    }

    public void setResponseSocket(Socket responseSocket) {
        this.responseSocket = responseSocket;
    }

    public PrintWriter getOutToServer() {
        return outToServer;
    }

    public void setOutToServer(PrintWriter outToServer) {
        this.outToServer = outToServer;
    }

    public BufferedReader getInFromServer() {
        return inFromServer;
    }

    public void setInFromServer(BufferedReader inFromServer) {
        this.inFromServer = inFromServer;
    }
}