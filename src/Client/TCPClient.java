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

    private String httpCommand;
    private String hostName;
    private String path;
    private int portNumber;
    private String httpVer;

    private Socket responseSocket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;

    public TCPClient(String httpCommand, String uri, int portNumber, String httpVer) {
        this.httpCommand = httpCommand;
        this.portNumber = portNumber;
        this.hostName = uri.split("/", 2)[0];
        this.path = uri.split("/", 2)[1];
        this.httpVer = httpVer;

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
        String fullHttpCommand = HTTPUtilities.parseCommand(httpCommand, path, httpVer);
        System.out.println("Entered command: " + fullHttpCommand);


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
        String httpType = HTTPUtilities.getHTTPCommand(fullHttpCommand);
        if (httpType.equals("POST") || httpType.equals("PUT")) {
            postOrPutData = inKeyboard.readLine();
        }

        execute(fullHttpCommand, headers, postOrPutData);
    }

    public void execute(String command, String[] headers, String postOrPutData) {
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

            /*
            if (!responseSocket.isClosed() && HTTPUtilities.getHTTPType(command).equals("1.1")) {
                command();
            }
            */
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

        String httpCommand = args[0];
        String hostName = args[1];
        int portNumber = Integer.parseInt(args[2]);
        String httpVer = args[3];

        TCPClient client = new TCPClient(httpCommand, hostName, portNumber, httpVer);
        client.command();
    }


}