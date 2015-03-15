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
        String[] commands = new String[0];
        try {
            commands = httpRequest.split(" ");
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            System.out.println(httpRequest);

        }

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

    public static String extractPathFromRequest(String httpRequest) {
        if (httpRequest.split(" ")[1].equals("/")) {
            return "/";
        } else {
            return httpRequest.split(" ")[1];
        }

    }

    public static String getFullRequestedContentPath(String httpRequest, String websiteToServe) {
        if (httpRequest.split(" ")[1].equals("/")) {
            return "./websites/" + websiteToServe + "/.html";
        }

        String part = "./websites/" + websiteToServe;
        if (!httpRequest.split(" ")[1].split("/", 2)[1].startsWith("/")) {
            part += "/";
        }

        return part + httpRequest.split(" ")[1].split("/", 2)[1];
    }


    public static String readHeaders(String Headers, String key) {
        for (String part : Headers.split("\n")) {
            if (key.equals("Content-Type") && part.startsWith("Content-Type:")) {
                String s = part.substring(13).trim();
                return part.substring(13).trim().split(" ")[0].replace(";", "");
            }
            if (part.split(":", 2)[0].equalsIgnoreCase(key)) {
                return part.split(":", 2)[1].trim();
            }
        }
        return "-1";
    }

    public static String parseCommand(String command, String path, String httpVer) {
        return command + " " + "/" + path + " " + httpVer;
    }

}
