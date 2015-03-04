package Client;

import Helperclass.FileHelper;
import Helperclass.HTMLParser;
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

    private int counter = 0;

    private String httpCommand;
    private String hostName;
    private String path;
    private int portNumber;
    private String httpVer;

    private Socket responseSocket;
    private PrintWriter outToServer;
    private DataInputStream inFromServer;

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
                    new DataInputStream(responseSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }


    public void inputHeadersAndData() throws IOException {
        BufferedReader inKeyboard =
                new BufferedReader(
                        new InputStreamReader(System.in));
        String fullHttpCommand = HTTPUtilities.parseCommand(httpCommand, path, httpVer);


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

        sendHTTPCommand(fullHttpCommand, headers, postOrPutData);
    }

    public void sendHTTPCommand(String command, String[] headers, String dataPostPut) {
        System.out.println("Entered inputHeadersAndData: " + command);
        outToServer.println(command);
        //needed for HTTP1.1, it's possible to have multiple adresses on 1 adress
        outToServer.println("Host: " + hostName);
        for (String header : headers) {
            if (header != null) {
                outToServer.println(header);
            }
        }

        //check if there is additional data to send
        if (dataPostPut != null) {
            outToServer.println("");
            outToServer.println(dataPostPut);
        }

        //needed to end the http inputHeadersAndData
        outToServer.println("");

        handleResponse();

    }

    public void handleResponse() {
        byte[] responseData = null;
        String decoded = "";
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] tempBytes = new byte[4096];
            int length = 0;

            while ((length = inFromServer.read(tempBytes)) > -1) {
                byteArrayOutputStream.write(tempBytes, 0, length);
            }
            responseData = byteArrayOutputStream.toByteArray();

            decoded = new String(responseData, "UTF-8");
            System.out.println(decoded);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String headers = HTMLParser.parseDataToHeaders(responseData);
        byte[] content = HTMLParser.parseDataToContent(responseData);

        String contentType = HTTPUtilities.readHeaders(headers, "Content-Type");
        switch (contentType) {
            case "image/png":
                saveImage(content, "png");
                break;
            case "image/jpg":
                saveImage(content, "jpg");
                break;
            default:
                FileHelper.newFile("./htmlpage.html");
                FileHelper.appendToFile("./htmlpage.html", decoded);
                Set<String> srces = getImageSrces(decoded);
                for (String srce : srces) {
                    System.out.println(srce);
                }
                for (String srce : srces) {
                    sendHTTPCommand("GET /" + srce + " HTTP/1.1", new String[]{}, null);
                }
                break;
        }
    }

    public Set<String> getImageSrces(String htmlString) {
        HashSet<String> set = new HashSet<>();
        Pattern pLower = Pattern.compile("<img.*src=\"([a-zA-Z0-9\\._\\-/:\\?=&]*)\".*");
        Pattern pUpper = Pattern.compile("<IMG.*SRC=\"([a-zA-Z0-9\\._\\-/:\\?=&]*)\".*");
        Matcher mLower = pLower.matcher(htmlString);
        Matcher mUpper = pUpper.matcher(htmlString);
        while (mLower.find()) {
            String src = mLower.group(1);
            set.add(src);
        }
        while (mUpper.find()) {
            String src = mUpper.group(1);
            set.add(src);
        }
        return set;
    }


    public void saveImage(byte[] imageBytes, String extension) {
        try {
            FileOutputStream fos = new FileOutputStream("image" + counter + "." + extension);
            counter++;
            fos.write(imageBytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        String httpCommand = args[0];
        String hostName = args[1];
        int portNumber = Integer.parseInt(args[2]);
        String httpVer = args[3];

        TCPClient client = new TCPClient(httpCommand, hostName, portNumber, httpVer);
        client.inputHeadersAndData();
    }


}