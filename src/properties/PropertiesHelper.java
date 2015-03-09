package properties;

import java.io.*;
import java.util.Properties;

/**
 * Created by GJ on 9/03/2015.
 */
public class PropertiesHelper {

    public static void writeprops(String path, String date) {

        OutputStream output = null;
        try {
            InputStream inputStream = new FileInputStream("config.properties");
            Properties prop = new Properties();
            prop.load(inputStream);
            inputStream.close();


            output = new FileOutputStream("config.properties");
            // set the properties value
            prop.setProperty(path, date);

            // save properties to project root folder
            prop.store(output, null);


        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readProps(String path) {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out

            String propVal = prop.getProperty(path);

            return propVal;

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
