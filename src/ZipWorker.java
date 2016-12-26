import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

public class ZipWorker extends SwingWorker<Integer, String> {

    private static final int COMPRESSION_LEVEL = 0;
    private static final int BUFFER_SIZE = 1024 * 10;

    private final String source, target, zipIgnore;

    private final JLabel progressLbl;

    private int count;

    private final FilterRules rules;

    public ZipWorker(String sourcePath, String output, String zipIgnore, JLabel progressLbl) {
        source = sourcePath;
        target = output;
        this.zipIgnore = zipIgnore;
        this.progressLbl = progressLbl;

        rules = new FilterRules();
    }

    @Override
    protected Integer doInBackground() throws Exception {

        // --------------------------------------------------------------------------------
        // create rules
        File ignoreFile = new File(zipIgnore);

        if (ignoreFile.exists()) {

            if (rules != null) {
                rules.clear();
            }
            Scanner in = new Scanner(ignoreFile);
            while (in.hasNextLine()) {
                String s = in.nextLine();
                if (s.startsWith("#") || s.isEmpty()) {
                    continue;
                }

                if (s.contains("/")) {
                    s = s.replace("/", File.separator);
                }

                Rule rule = new Rule(s);

                if (s.endsWith(File.separator) || s.endsWith("/")) {
                    // Dir
                    if (s.contains("*")) {
                        rule.mType = Rule.Type.REGEX_DIR;
                    } else {
                        rule.mType = Rule.Type.DIR;
                    }
                } else {
                    // File
                    int index = s.lastIndexOf(File.separator);
                    if (index == -1) {
                        index = s.lastIndexOf("/");
                    }
                    String path = "";
                    String name = "";

                    if (index > 0) {
                        path = s.substring(0, index);
                        name = s.substring(index + 1);
                    }

                    if (path.contains("*") && name.contains("*")) {
                        rule.mType = Rule.Type.REGEX_FILE_DIR;
                    } else if (s.contains("*")) {
                        rule.mType = Rule.Type.REGEX_FILE;
                    } else {
                        rule.mType = Rule.Type.FILE;
                    }
                }
                rules.addRule(rule);
                System.out.println(rule.rule + " , type : " + rule.mType.toString());
            }
        }

        // -----------------------------------------------------------------------------------
        if (source == null || target == null) {
            return 0;
        }
        zip(source, target, zipIgnore);
        return count;
    }

    @Override
    protected void process(List<String> arg0) {
        // TODO Auto-generated method stub
        for (String str : arg0) {
            progressLbl.setText(str);
        }
        super.process(arg0);
    }

    public void zip(String source, String target, String ignore) throws IOException {

        if (new File(source).isFile()) {
            zipFile(source, target);
            return;
        }

        FileOutputStream fos = new FileOutputStream(target);
        ZipOutputStream zos = new ZipOutputStream(fos);

        // zos.setMethod(ZipOutputStream.DEFLATED);// this will give larger zip file size
        // zos.setLevel(COMPRESSION_LEVEL);

        zipDirectory(source, source, zos);

        zos.closeEntry();
        zos.close();
        fos.close();
    }

    public void zipFile(String source, String target) throws IOException {

        // Open the output stream to the destination file
        FileOutputStream fos = new FileOutputStream(target);

        // Open the zip stream to the output file
        ZipOutputStream zos = new ZipOutputStream(fos);
        // zos.setMethod(ZipOutputStream.DEFLATED);// this will give larger zip file size
        // zos.setLevel(COMPRESSION_LEVEL);

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

    private void zipDirectory(String source, String directoryPath, ZipOutputStream zos) throws IOException {
        System.out.println("ZipDirectory source : " + source);
        // Iterate through the directory elements
        for (String dirElement : new File(directoryPath).list()) {

            // Construct each element full path
            String dirElementPath = directoryPath + File.separator + dirElement;

            System.out.println("ZipDirectory dirElement : " + dirElement);
            System.out.println("ZipDirectory dirElementPath : " + dirElementPath);

            // For directories - go down the directory tree recursively
            if (new File(dirElementPath).isDirectory()) {
                if (rules.check(dirElement, Rule.Type.DIR)) {
                    continue;
                }
                zipDirectory(source, dirElementPath, zos);

            } else {

                if (rules.check(dirElement, Rule.Type.FILE)) {
                    continue;
                }
                // For files add a ZIP entry
                // THIS IS IMPORTANT: a ZIP entry needs to be a relative path to the file
                // To maintain directory structure relative path must be maintained
                // so we cut off the path to the directory that is being packed.
                ZipEntry ze = new ZipEntry(dirElementPath.replace(source + File.separator, ""));
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
