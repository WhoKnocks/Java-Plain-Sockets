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
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new DataOutputStream(clientSocket.getOutputStream());

            String httpCommandLine = inFromClient.readLine();
            String httpCommand = HTTPUtilities.getHTTPCommand(httpCommandLine);

            if (httpCommand.equals("GET") || httpCommand.equals("HEAD")) {
                String contentPath = HTTPUtilities.getRequestedContentPath(httpCommandLine);
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

                    String headers = generateHeaders(HTTPStatusCode.OK, contentSize, contentType);
                    outToClient.writeBytes(headers);
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

            if (httpCommand.equals("POST")) {

                FileHelper.appendToFile("./appendedFile.txt", getSentContent());
            }
            if (httpCommand.equals("PUT")) {
                FileHelper.deleteFile("./newFile.txt");
                FileHelper.newFile("./newFile.txt");
                FileHelper.appendToFile("./newFile.txt", getSentContent());
            }

            outToClient.close();
            inFromClient.close();
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
            while ((bytesRead = in.read(buffer)) != -1) {
                outToClient.write(buffer, 0, bytesRead);
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSentContent() {
        try {
            String clientData;
            StringBuilder header = new StringBuilder();

            //First read all headers
            while (!(clientData = inFromClient.readLine()).equals("")) {
                header.append("\n").append(clientData);
            }

            int cont_length = Integer.parseInt(HTTPUtilities.readHeader(header.toString(), "Content-Length"));

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
}