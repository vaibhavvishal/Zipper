import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import android.util.Log;

public class ZipUtils {

    private static final String TAG = "ZipUtils";

    private static final int COMPRESSION_LEVEL = 0;
    private static final int BUFFER_SIZE = 1024 * 10;

    public static void zip(String source, String target, String ignore) throws IOException {

        if (new File(source).isFile()) {
            zipFile(source, target);
            return;
        }

        FileOutputStream fos = new FileOutputStream(target);
        ZipOutputStream zos = new ZipOutputStream(fos);

        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(COMPRESSION_LEVEL);

        zipDirectory(source, source, zos);

        zos.closeEntry();
        zos.close();
        fos.close();
    }

    public static void zipFile(String source, String target) throws IOException {

        // Open the output stream to the destination file
        FileOutputStream fos = new FileOutputStream(target);

        // Open the zip stream to the output file
        ZipOutputStream zos = new ZipOutputStream(fos);
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(COMPRESSION_LEVEL);

        // Create a zip entry containing packed file name
        ZipEntry ze = new ZipEntry(new File(source).getName());
        zos.putNextEntry(ze);

        // Open input stream to packed file
        FileInputStream fis = new FileInputStream(source);

        // An array to which will hold byte being read from the packed file
        byte[] bytesRead = new byte[BUFFER_SIZE];

        // Read bytes from packed file and store them in the ZIP output stream
        int bytesNum;
        while ((bytesNum = fis.read(bytesRead)) > 0) {
            zos.write(bytesRead, 0, bytesNum);
        }

        // Close all streams
        fis.close();
        zos.closeEntry();
        zos.close();
        fos.close();
    }

    private static void zipDirectory(String source, String directoryPath, ZipOutputStream zos) throws IOException {
        // Iterate through the directory elements
        for (String dirElement : new File(directoryPath).list()) {

            // Construct each element full path
            String dirElementPath = directoryPath + "/" + dirElement;
            

            // For directories - go down the directory tree recursively
            if (new File(dirElementPath).isDirectory()) {
                zipDirectory(source, dirElementPath, zos);

            } else {
                // For files add a ZIP entry
                // THIS IS IMPORTANT: a ZIP entry needs to be a relative path to the file
                // To maintain directory structure relative path must be maintained
                // so we cut off the path to the directory that is being packed.
                ZipEntry ze = new ZipEntry(dirElementPath.replaceAll(source + "/", ""));
                zos.putNextEntry(ze);

                // Open input stream to packed file
                FileInputStream fis = new FileInputStream(dirElementPath);

                // An array to which will hold byte being read from the packed file
                byte[] bytesRead = new byte[BUFFER_SIZE];

                // Read bytes from packed file and store them in the ZIP output stream
                int bytesNum;
                while ((bytesNum = fis.read(bytesRead)) > 0) {
                    zos.write(bytesRead, 0, bytesNum);
                }

                // Close the stream
                fis.close();
            }
        }

    }

}