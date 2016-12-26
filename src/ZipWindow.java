import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ZipWindow implements ActionListener {

    private JFrame frame;
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;
    private JLabel progress;

    private JButton srcBtn, targetBtn, ignoreBtn, startBtn;

    private JProgressBar progressBar;

    private final String ACTION_SOURCE = "SOURCE";
    private final String ACTION_TARGET = "TARGET";
    private final String ACTION_IGNORE = "IGNORE";
    private final String ACTION_ZIP = "ZIP";

    private String source, target, zipIgnore;

    private JFileChooser fileChooser;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ZipWindow window = new ZipWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ZipWindow() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("V:\\Studio_Workspace"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblNewLabel = new JLabel("Source");
        lblNewLabel.setBounds(10, 35, 46, 14);
        frame.getContentPane().add(lblNewLabel);

        textField = new JTextField();
        textField.setBounds(56, 32, 309, 20);
        frame.getContentPane().add(textField);
        textField.setColumns(10);

        srcBtn = new JButton("...");
        srcBtn.setBounds(373, 31, 30, 23);
        frame.getContentPane().add(srcBtn);
        srcBtn.addActionListener(this);
        srcBtn.setActionCommand(ACTION_SOURCE);

        JLabel lblNewLabel_1 = new JLabel("Target");
        lblNewLabel_1.setBounds(10, 81, 46, 14);
        frame.getContentPane().add(lblNewLabel_1);

        textField_1 = new JTextField();
        textField_1.setText("");
        textField_1.setBounds(56, 78, 309, 20);
        frame.getContentPane().add(textField_1);
        textField_1.setColumns(10);

        targetBtn = new JButton("...");
        targetBtn.setBounds(373, 77, 30, 23);
        frame.getContentPane().add(targetBtn);
        targetBtn.addActionListener(this);
        targetBtn.setActionCommand(ACTION_TARGET);

        JLabel lblNewLabel_2 = new JLabel("Ignore file");
        lblNewLabel_2.setBounds(10, 119, 46, 44);
        frame.getContentPane().add(lblNewLabel_2);

        textField_2 = new JTextField();
        textField_2.setBounds(56, 131, 309, 20);
        frame.getContentPane().add(textField_2);
        textField_2.setColumns(10);

        ignoreBtn = new JButton("...");
        ignoreBtn.setBounds(372, 130, 31, 23);
        frame.getContentPane().add(ignoreBtn);
        ignoreBtn.addActionListener(this);
        ignoreBtn.setActionCommand(ACTION_IGNORE);

        progressBar = new JProgressBar();
        progressBar.setBounds(10, 220, 393, 14);
        frame.getContentPane().add(progressBar);
        progressBar.setVisible(false);

        progress = new JLabel("Zipping");
        progress.setBounds(171, 201, 91, 14);
        frame.getContentPane().add(progress);

        startBtn = new JButton("Start");
        startBtn.setBounds(173, 167, 89, 23);
        frame.getContentPane().add(startBtn);
        startBtn.addActionListener(this);
        startBtn.setActionCommand(ACTION_ZIP);
    }

    @SuppressWarnings("unused")
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

        if (e.getActionCommand().equals(ACTION_ZIP)) {

            try {
            	File targetFile = new File(target);
            	if(target != null && targetFile.exists()) {
            		targetFile.delete();
            	}
            	
                ZipWorker worker = new ZipWorker(source, target, zipIgnore, progress);
                worker.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        String name = event.getPropertyName();

                        switch (name) {
                        case "progress":
                            // progressBar.setIndeterminate(false);
                            // progressBar.setValue((Integer) event.getNewValue());
                            break;
                        }
                    }
                });
                worker.execute();
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            return;
        }

        int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            switch (e.getActionCommand()) {
            case ACTION_SOURCE:
                textField_2.setEnabled(true);
                ignoreBtn.setEnabled(true);

                textField.setText(file.getPath());
                source = file.getPath();

                // Set target
                String fileName = file.getName();
                String parent = file.getParent();
                String zipPath = "";
                if (file.isDirectory()) {
                    zipPath = file.getPath() + ".zip";

                } else {
                    String onlyName = fileName.substring(0, fileName.indexOf("."));
                    String zipName = onlyName + ".zip";
                    zipPath = parent + File.separator + zipName;
                }

                textField_1.setText(zipPath);
                target = zipPath;

                // Search for .zipignore in source directory

                if (file.isDirectory()) {
                    String[] files = file.list();

                    for (String s : files) {
                        if (".zipignore".equals(s)) {
                            textField_2.setText(source + File.separator + ".zipignore");
                            zipIgnore = source + File.separator + ".zipignore";
                            break;
                        }
                    }
                } else {
                    textField_2.setEnabled(false);
                    ignoreBtn.setEnabled(false);
                }

                break;
            case ACTION_TARGET:
                textField_1.setText(file.getPath());
                target = file.getPath();
                break;
            case ACTION_IGNORE:
                textField_2.setText(file.getPath());
                zipIgnore = file.getPath();
                break;
            default:
                break;
            }
        }
    }
}
