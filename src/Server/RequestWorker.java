package Server;

import Helperclass.FileHelper;
import Helperclass.HTTPStatusCode;
import Helperclass.HTTPUtilities;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class RequestWorker implements Runnable {

    protected Socket clientSocket = null;

    private BufferedReader inFromClient;
    private DataOutputStream outToClient;


    public RequestWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            //open streams to send and receive
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new DataOutputStream(clientSocket.getOutputStream());

            //get first line (ex: GET / HTTP/1.1)
            String httpCommandLine = inFromClient.readLine();
            //get command (ex: GET )
            String httpCommand = HTTPUtilities.getHTTPCommand(httpCommandLine);

            if (httpCommand.equals("GET") || httpCommand.equals("HEAD")) {
                String headersReceived = readHeaders();
                System.out.println(headersReceived);
                String contentPath = HTTPUtilities.getRequestedContentPath(httpCommandLine);
                //check if requested content is availaible on server
                if (!FileHelper.isContentFound(contentPath)) {
                    outToClient.writeBytes(generateHeaders(HTTPStatusCode.NOT_FOUND, 0, "text/html"));
                } else {
                    int contentSize;
                    String contentType = HTTPUtilities.getRequestedContentType(httpCommandLine);
                    if (contentType.equals("image/png") || contentType.equals("image/jpg")) {
                        contentSize = FileHelper.getImageSize(contentPath);
                    } else {
                        contentSize = FileHelper.fileReader(contentPath).getBytes("UTF-8").length;
                    }

                    String headersToSend = generateHeaders(HTTPStatusCode.OK, contentSize, contentType);

                    outToClient.writeBytes(headersToSend);
                    //send content if request is not HEAD
                    if (httpCommand.equals("GET")) {
                        if (HTTPUtilities.getRequestedContentType(httpCommandLine).equals("text/html")) {
                            outToClient.writeBytes(FileHelper.fileReader(contentPath));
                        } else {
                            sendImage(contentPath);
                        }
                    }
                }
            }

            // if post request append to file
            if (httpCommand.equals("POST")) {
                FileHelper.appendToFile("./appendedFile.txt", getReceivedContent());
            }

            //if put request make new file and place content
            if (httpCommand.equals("PUT")) {
                FileHelper.deleteFile("./newFile.txt");
                FileHelper.newFile("./newFile.txt");
                FileHelper.appendToFile("./newFile.txt", getReceivedContent());
            }

            outToClient.writeBytes("\n");
            //check if 1.1 to remain the connection open
            //if (!HTTPUtilities.getHTTPType(httpCommandLine).equals("1.1")) {
            outToClient.close();
            inFromClient.close();
            // }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                outToClient.writeBytes(generateHeaders(HTTPStatusCode.SERVER_ERROR, 0, "text/html"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    private String generateHeaders(HTTPStatusCode statusCode, int contentLength, String contentType) {
        StringBuilder builder = new StringBuilder(statusCode.getResponse() + "\n");

        builder.append(("Accept-Ranges: bytes")).append("\n");
        builder.append("Content-Length:" + contentLength).append("\n");
        builder.append("Content-Type:" + contentType).append("\n");
        builder.append("Date:" + getDate()).append("\n");

        builder.append("\n");
        return builder.toString();
    }

    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
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
                b.append(bytesRead + "\n");
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

            int cont_length = Integer.parseInt(HTTPUtilities.readHeader(headers, "Content-Length"));

            StringBuilder content = new StringBuilder();
            while (content.toString().getBytes("UTF-8").length + 2 < cont_length) {
                clientData = inFromClient.readLine();
                content.append("\n").append(clientData);
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