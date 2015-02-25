import com.sun.org.apache.xpath.internal.SourceTree;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String hostName = "www.gva.be";
        int portNumber = 80;

        TCPClient client = new TCPClient(hostName, portNumber);
        client.command();
    }


    public void command() throws IOException {
        BufferedReader inKeyboard =
                new BufferedReader(
                        new InputStreamReader(System.in));
        System.out.println("Give Command:");
        String command = inKeyboard.readLine();

        String[] headers = new String[46];
        String header;
        int i = 0;
        while (!(header = inKeyboard.readLine()).equals("")) {
            headers[i] = header;
        }

        switch (getHTTPCommand(command)) {
            case "GET":
                this.get(command, headers);
                break;
            case "POST":
                this.post(command);
                break;
            case "HEAD":
                this.get(command, headers);
                break;
            case "PUT":
                this.put(command);
                break;
        }
    }


    public void get(String command, String[] headers) {
        try (
                Socket responseSocket = new Socket(hostName, portNumber);
                PrintWriter outToServer =
                        new PrintWriter(responseSocket.getOutputStream(), true);
                BufferedReader inFromServer =
                        new BufferedReader(
                                new InputStreamReader(responseSocket.getInputStream()));

        ) {

            outToServer.println(command);
            outToServer.println("Host: " + hostName);
            for (String header : headers) {
                if (header != null) {
                    outToServer.println(header);
                }
            }
            //outToServer.println("From: gertjanheir@hotmail.com");
            //outToServer.println("User-Agent: G-J'sTaak/1.0");
            outToServer.println("");

            String response;
            StringBuilder completeResponse = new StringBuilder();
            while ((response = inFromServer.readLine()) != null) {
                completeResponse.append("\n").append(response);
            }

            String responseNoHeaders = removeHeaders(completeResponse.toString());
            getImageSrces(completeResponse.toString());

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }

    }



    public void getImageSrces(String htmlString) {
        Pattern p = Pattern.compile("<img.*src=\"([a-zA-Z0-9\\._\\-/:\\?]*)\".*");
        Matcher m = p.matcher(htmlString);
        while (m.find()) {
            String src = m.group(1);
            System.out.println(src);
        }

        System.out.println("done");


    }

    public String getHTTPType(String command) {
        String[] commands = command.split(" ");
        String[] httpPart = commands[2].split("/");
        return httpPart[1];
    }

    public String getHTTPCommand(String command) {
        String[] commands = command.split(" ");
        return commands[0];
    }


    public String removeHeaders(String httpResponse) {
        return httpResponse.split("\\n\\n", 2)[1];
    }

    public String getHeaders(String httpResponse) {
        return httpResponse.split("\\n\\n\\n", 2)[0];
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

    private void head(String command) {
        throw new UnsupportedOperationException();
    }

    private void post(String command) {
        throw new UnsupportedOperationException();
    }

    private void put(String command) {
        throw new UnsupportedOperationException();
    }
}