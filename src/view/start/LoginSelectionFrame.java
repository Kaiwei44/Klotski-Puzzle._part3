// view/start/LoginSelectionFrame.java
package view.start;

import javax.swing.*;
import java.awt.*;

import controller.GameController;
import tool.tool;
import view.FrameUtil;
import view.login.LoginFrame;
import view.game.GameFrame;
import model.MapModel;

public class LoginSelectionFrame extends JFrame {
    public LoginSelectionFrame(int w, int h) {
        setTitle("Klotski - Select Mode");
        setLayout(null);
        setSize(w, h);

        JButton guestBtn = FrameUtil.createButton(this,
                "Play as Guest", new Point(tool.PLAY_AS_GUEST_x, tool.PLAY_AS_GUEST_y), tool.PLAY_AS_GUEST_w, tool.H_ALL);
        JButton loginBtn = FrameUtil.createButton(this,
                "Login / Register", new Point(tool.PLAY_AS_GUEST_x, tool.LOGIN_y), tool.PLAY_AS_GUEST_w, tool.H_ALL);

        guestBtn.addActionListener(e -> {
            // 游客模式，直接初始化默认关卡
            int[][] matrix = {
                    {3, 7, 7, 5},
                    {3, 7, 7, 5},
                    {4, 2, 2, 6},
                    {4, 0, 0, 6},
                    {1, 1, 1, 1}
            };
            MapModel model = new MapModel(matrix);
            GameFrame gf = new GameFrame(tool.GRID_SIZE*12, tool.GRID_SIZE*10, model);
            GameController controller = new GameController(gf.getGamePanel(), model);
            gf.setVisible(true);
            dispose();
        });

        loginBtn.addActionListener(e -> {
            // 打开登录界面
            new LoginFrame(tool.LOGIN_FRAME_W, tool.LOGIN_FRAME_H).setVisible(true);
            dispose();
        });

        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
