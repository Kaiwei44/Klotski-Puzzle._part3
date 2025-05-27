import model.MapModel;
import view.game.GameFrame;
import view.login.LoginFrame;
import tool.tool;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            view.start.LoginSelectionFrame sel = new view.start.LoginSelectionFrame(tool.LOGIN_FRAME_W, tool.LOGIN_FRAME_H);
            sel.setVisible(true);
        });
    }
}
