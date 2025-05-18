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
            /*MapModel mapModel = new MapModel(new int[][]{
                    {1, 2, 2, 1},
                    {1, 3, 2, 2},
                    {1, 3, 4, 4},
                    {0, 0, 4, 4}
            });
            GameFrame gameFrame = new GameFrame(600, 450, mapModel);
            gameFrame.setVisible(false);
            /*sel.setGameFrame(gameFrame);*/
        });
    }
}
