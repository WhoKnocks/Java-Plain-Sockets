package Server;

import Helperclass.FileHelper;
import Helperclass.HTTPStatusCode;
import Helperclass.HTTPUtilities;
import properties.PropertiesHelper;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class RequestWorker implements Runnable {

    protected Socket clientSocket = null;

    private BufferedReader inFromClient;
    private DataOutputStream outToClient;

    private boolean hasCloseHeader = false;

    private final String websiteToServe = "www.tcpipguide.com";

    private String path;


    public RequestWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            //open streams to send and receive
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            String httpCommandLine;

            do {

                //get first line (ex: GET / HTTP/1.1)
                do {
                    httpCommandLine = inFromClient.readLine();
                    Thread.sleep(40);
                } while (httpCommandLine == null);
                System.out.println(httpCommandLine);

                //get inputHeadersAndData (ex: GET )
                String httpCommand = HTTPUtilities.getHTTPCommand(httpCommandLine);
                try {
                    path = httpCommandLine.split(" ")[1].split("/")[1];
                } catch (Exception ex) {
                    path = "/";
                }


                if (httpCommand.equals("GET") || httpCommand.equals("HEAD")) {
                    handleGetOrHead(httpCommandLine);
                }

                // if post request append to file
                if (httpCommand.equals("POST")) {
                    String pathtoPost = "./websites/" + websiteToServe + "/" + path + "/";
                    pathtoPost = pathtoPost.replace("//", "/");
                    FileHelper.newFile(pathtoPost + "post.txt");
                    FileHelper.appendToFile(pathtoPost + "post.txt", getReceivedContent());
                }

                //if put request make new file and place content
                if (httpCommand.equals("PUT")) {
                    String pathtoPost = "./websites/" + websiteToServe + "/" + path + "/";
                    pathtoPost = pathtoPost.replace("//", "/");
                    FileHelper.deleteFile(pathtoPost + "put.txt");
                    FileHelper.newFile(pathtoPost + "put.txt");
                    FileHelper.appendToFile(pathtoPost + "put.txt", getReceivedContent());
                }

                outToClient.writeBytes("\r\n");


            } while (!hasCloseHeader);

            outToClient.close();
            inFromClient.close();


        } catch (IllegalAccessException ex) {
            try {
                outToClient.writeBytes(generateHeaders(HTTPStatusCode.BAD_REQUEST, 0, "text/html"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                outToClient.writeBytes(generateHeaders(HTTPStatusCode.SERVER_ERROR, 0, "text/html"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void handleGetOrHead(String httpCommandLine) throws IllegalAccessException, IOException {
        String headersReceived = null;
        headersReceived = readHeaders();
        String connectionHeader = HTTPUtilities.readHeaders(headersReceived, "Connection");
        String hostHeader = HTTPUtilities.readHeaders(headersReceived, "Host");
        if (hostHeader.equalsIgnoreCase("-1") && HTTPUtilities.getHTTPType(httpCommandLine).equalsIgnoreCase("1.1")) {
            throw new IllegalAccessException("HTTP 1.1 and no host header");
        }

        String ifModifiedSinceHeader = HTTPUtilities.readHeaders(headersReceived, "if-modified-since");
        if (!ifModifiedSinceHeader.equals("-1")) {
            Date date = parseIfModified(ifModifiedSinceHeader);
            String cacheStringDate = PropertiesHelper.readProps(websiteToServe + path);
            Date cacheDate = parseIfModified(cacheStringDate);
            date.after(cacheDate);
            outToClient.writeBytes(generateHeaders(HTTPStatusCode.NOT_MODIFIED, 0, "text/html"));
            return;
        }

        if (HTTPUtilities.readHeaders(headersReceived, "Connection").equalsIgnoreCase("close")) {
            hasCloseHeader = true;
        }

        String httpCommand = HTTPUtilities.getHTTPCommand(httpCommandLine);

        System.out.println(headersReceived);

        String contentPath = HTTPUtilities.getFullRequestedContentPath(httpCommandLine, websiteToServe);


        //first check if requested content is available on server
        if (!FileHelper.isContentFound(contentPath)) {
            outToClient.writeBytes(generateHeaders(HTTPStatusCode.NOT_FOUND, 0, "text/html"));
        } else {
            // find out content size
            int contentSize;
            String contentType = HTTPUtilities.getRequestedContentType(httpCommandLine);
            if (contentType.equals("image/png") || contentType.equals("image/jpg")) {
                contentSize = FileHelper.getImageSize(contentPath);
            } else {
                contentSize = FileHelper.fileReader(contentPath).getBytes("UTF-8").length;
            }

            String headersToSend = generateHeaders(HTTPStatusCode.OK, contentSize, contentType);

            outToClient.writeBytes(headersToSend);
            //send http content if request is GET
            if (httpCommand.equals("GET")) {
                if (HTTPUtilities.getRequestedContentType(httpCommandLine).equals("text/html")) {
                    outToClient.writeBytes(FileHelper.fileReader(contentPath));
                } else {
                    sendImage(contentPath);
                }
            }
        }
    }

    private Date parseIfModified(String date) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        try {
            return format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateHeaders(HTTPStatusCode statusCode, int contentLength, String contentType) {
        StringBuilder builder = new StringBuilder(statusCode.getResponse() + "\r\n");

        builder.append(("Accept-Ranges: bytes")).append("\r\n");
        builder.append("Content-Length:" + contentLength).append("\r\n");
        builder.append("Content-Type:" + contentType).append("\r\n");
        builder.append("Date:" + getDate()).append("\r\n");

        builder.append("\r\n");
        return builder.toString();
    }

    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date();
        return sdf.format(date);
    }

    private Byte sendImage(String imagePath) {
        try {
            File testF = new File(imagePath);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(testF));

            byte[] buffer = new byte[4096];
            int bytesRead;
            StringBuilder b = new StringBuilder("");
            while ((bytesRead = in.read(buffer)) != -1) {
                outToClient.write(buffer, 0, bytesRead);
                b.append(bytesRead + "\r\n");
            }

            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private String getReceivedContent() {
        try {
            String clientData;

            //First read all headers
            String headers = readHeaders();

            int cont_length = Integer.parseInt(HTTPUtilities.readHeaders(headers, "Content-Length"));

            StringBuilder content = new StringBuilder();
            while (content.toString().getBytes("UTF-8").length + 2 < cont_length) {
                clientData = inFromClient.readLine();
                content.append("\r\n").append(clientData);
            }

            return content.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String readHeaders() throws IOException {
        String clientData;
        StringBuilder headers = new StringBuilder();
        while (!(clientData = inFromClient.readLine()).equals("")) {
            headers.append(clientData).append("\n");
        }
        return headers.toString();
    }
}