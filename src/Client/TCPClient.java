package Client;

import Helperclass.FileHelper;
import Helperclass.HTTPUtilities;
import properties.PropertiesHelper;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
    private String command;

    private Socket responseSocket;
    private PrintWriter outToServer;
    private DataInputStream inFromServer;

    private String imgSrc;

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
        setCommand(command);
        System.out.println("Entered inputHeadersAndData: " + command);
        outToServer.println(command);
        //needed for HTTP1.1, it's possible to have multiple adresses on 1 adress
        outToServer.println("Host: " + hostName);
        if (getModifiedSince() != null) {
            outToServer.println("if-modified-since: " + getModifiedSince());
        }

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


    public String getModifiedSince() {
        return PropertiesHelper.readProps(hostName + HTTPUtilities.extractPathFromRequest(getCommand()).split("\\.")[0]);
    }

    public void handleResponse() {
        byte[] responseData = null;
        String decoded = "";
        String headers = "";
        byte[] contentBytes = null;
        String contentString = "";
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //byte per byte inlezen, vervolgens "\r\n" herkennen
            int byteArrSize = 1;
            byte[] tempBytes = new byte[1];

            Boolean headersFound = false;
            while (!headersFound) {
                inFromServer.read(tempBytes);
                byteArrayOutputStream.write(tempBytes, 0, 1);
                if (tempBytes[0] == '\r') {
                    inFromServer.read(tempBytes);
                    if (tempBytes[0] == '\n') {
                        byteArrayOutputStream.write(tempBytes, 0, 1);
                        inFromServer.read(tempBytes);
                        byteArrayOutputStream.write(tempBytes, 0, 1);
                        if ('\r' == (char) tempBytes[0]) {
                            responseData = byteArrayOutputStream.toByteArray();
                            decoded = new String(responseData, "UTF-8");
                            System.out.println("Headers: " + decoded);
                            headersFound = true;
                        }
                    }
                }
            }
            //iets lezen anders werkt img ni
            inFromServer.read(tempBytes);

            headers = decoded;
            String contLength = HTTPUtilities.readHeaders(headers, "Content-Length");
            String date = HTTPUtilities.readHeaders(headers, "Date");
            PropertiesHelper.writeprops(hostName + HTTPUtilities.extractPathFromRequest(getCommand()).split("\\.")[0], date);
            System.out.println(contLength);

            int iContLength = Integer.parseInt(contLength);

            int totalSizeRead = 0;

            contentBytes = new byte[500];

            byteArrayOutputStream = new ByteArrayOutputStream();
            while (totalSizeRead < iContLength) {
                int length = inFromServer.read(contentBytes);
                byteArrayOutputStream.write(contentBytes, 0, length);
                // System.out.println(totalSizeRead);
                totalSizeRead += length;
            }

            contentBytes = byteArrayOutputStream.toByteArray();

            contentString = new String(contentBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String contentType = HTTPUtilities.readHeaders(headers, "Content-Type");

        switch (contentType) {
            case "image/png":
                saveImage(contentBytes, "png");
                break;
            case "image/jpg":
                saveImage(contentBytes, "jpg");
                break;
            case "image/jpeg":
                saveImage(contentBytes, "jpg");
                break;
            case "image/gif":
                saveImage(contentBytes, "gif");
                break;
            case "text/html":
                saveWebsite(contentString);
                if (httpVer.equals("HTTP/1.1")) {
                    List<String> srces = getImageSrces(contentString);
                    for (String src : srces) {
                        System.out.println(src);
                    }

                    for (int i = 0; i < srces.size(); i++) {
                        //as long it's not the last img
                        if (i < srces.size() - 2) {
                            imgSrc = srces.get(i);
                            sendHTTPCommand("GET /" + srces.get(i) + " " + httpVer, new String[]{}, null);
                        } else {
                            sendHTTPCommand("GET /" + srces.get(i) + " " + httpVer, new String[]{"Connection: Close"}, null);
                        }
                    }
                }
                break;
        }
    }

    public List<String> getImageSrces(String htmlString) {
        ArrayList<String> list = new ArrayList<>();
        Pattern pLower = Pattern.compile("<img.*src=\"([a-zA-Z0-9\\._\\-/:\\?=&]*)\".*");
        Pattern pUpper = Pattern.compile("<IMG.*SRC=\"([a-zA-Z0-9\\._\\-/:\\?=&]*)\".*");
        Matcher mLower = pLower.matcher(htmlString);
        Matcher mUpper = pUpper.matcher(htmlString);
        while (mLower.find()) {
            String src = mLower.group(1);
            list.add(src);
        }
        while (mUpper.find()) {
            String src = mUpper.group(1);
            list.add(src);
        }
        return list;
    }

    public void saveWebsite(String contentString) {
        // FileHelper.deleteFile("./htmlpage.html");
        String totalPath = "./websites/" + hostName + "/" + path + ".html";
        FileHelper.deleteFile(totalPath);
        FileHelper.newFile(totalPath);
        FileHelper.appendToFile(totalPath, contentString);
    }


    public void saveImage(byte[] imageBytes, String extension) {
        try {
            String totalPath = "./websites/" + hostName + "/" + HTTPUtilities.extractPathFromRequest(getCommand()).split("\\.")[0] + "." + extension;
            FileHelper.newFile(totalPath);
            FileOutputStream fos = new FileOutputStream(totalPath);
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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}