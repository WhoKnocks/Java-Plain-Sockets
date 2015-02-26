/**
 * Created by GJ on 25/02/2015.
 */
public class HTTPUtilities {


    public static String getHTTPType(String httpRequest) {
        String[] commands = httpRequest.split(" ");
        String[] httpPart = commands[2].split("/");
        return httpPart[1];
    }

    public static String getHTTPCommand(String httpRequest) {
        String[] commands = httpRequest.split(" ");
        return commands[0];
    }

    public static String getRequestedContentType(String httpRequest) {
        String urlCommand = httpRequest.split(" ")[1];
        if (urlCommand.endsWith(".jpg")) {
            return "image/jpg";
        }
        if (urlCommand.endsWith(".png")) {
            return "image/png";
        } else {
            return "text/html";
        }
    }

}
