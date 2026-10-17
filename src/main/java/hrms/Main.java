package hrms;

import hrms.ui.LoginFrame;
import hrms.util.DatabaseSetup;
import java.awt.EventQueue;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/** Application entry point. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                DatabaseSetup.initialize();
                new LoginFrame().setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "The HRMS could not start. Check that Maven resolved Apache Derby and that the project folder is writable. The local database is created automatically on first launch.\n\n" + ex.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
