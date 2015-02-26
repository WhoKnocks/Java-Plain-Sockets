import java.io.*;

/**
 * Created by GJ on 25/02/2015.
 */
public class FileHelper {

    public static String fileReader() {
        try (BufferedReader br = new BufferedReader(new FileReader("./htmlpage/index.html"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        System.out.println(FileHelper.fileReader());
    }

}
