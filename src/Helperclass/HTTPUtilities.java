package Helperclass;

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

    public static String getRequestedContentPath(String httpRequest) {
        if (httpRequest.split(" ")[1].equals("/")) {
            return "./htmlpage/index.html";
        }
        return "./htmlpage" + httpRequest.split(" ")[1];
    }

    public static String readHeader(String Header, String key) {
        for (String part : Header.split("\n")) {
            if (part.split(":")[0].equalsIgnoreCase(key)) {
                return part.split(":")[1].trim();
            }
        }
        return "-1";
    }

    public static String parseCommand(String command, String path, String httpVer){
        return command + " " + "/" + path + " " + httpVer;
    }

}
