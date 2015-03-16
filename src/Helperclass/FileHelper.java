package Helperclass;

import java.io.*;

/**
 * Created by GJ on 25/02/2015.
 */
public class FileHelper {

    /**
     * reads a file
     *
     * @param path
     * @return
     */
    public static String fileReader(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
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

    /**
     * checks if the file alerady exists
     *
     * @param contentPath
     * @return
     */
    public static Boolean isContentFound(String contentPath) {
        File f = new File(contentPath);
        if (f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * calculates the image size in bytes
     *
     * @param imagePath
     * @return returns the image size
     * @throws FileNotFoundException
     */
    public static int getImageSize(String imagePath) throws FileNotFoundException {
        try {
            File testF = new File(imagePath);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(testF));

            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalByteSize = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                totalByteSize += bytesRead;
            }
            return totalByteSize;

        } catch (FileNotFoundException e) {
            throw e;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * appends data to file
     *
     * @param path
     * @param text
     */
    public static void appendToFile(String path, String text) {
        try {

            FileWriter fw = new FileWriter(path, true);
            fw.write(text + "\n");//appends the string to the file
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * creats a new file
     *
     * @param fileName
     */
    public static void newFile(String fileName) {
        fileName = fileName.replace("//", "/");
        File file = new File(fileName);
        if (file.exists()) {
            return;
        }
        File filee = file.getParentFile();
        boolean isMade = file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * delete a file
     *
     * @param fileName
     */
    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        file.delete();
    }


}
