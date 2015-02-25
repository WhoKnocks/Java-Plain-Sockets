/**
 * Created by GJ on 25/02/2015.
 */
public class HTTPUtilities {


    public static String getHTTPType(String command) {
        String[] commands = command.split(" ");
        String[] httpPart = commands[2].split("/");
        return httpPart[1];
    }

    public static String getHTTPCommand(String command) {
        String[] commands = command.split(" ");
        return commands[0];
    }

}
