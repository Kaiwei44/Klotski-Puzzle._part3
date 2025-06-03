package view.login;

import view.FrameUtil;
import view.game.GameFrame;
import model.UserManager;
import model.MapModel;
import view.start.LoginSelectionFrame;
import tool.tool;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField username;
    private JPasswordField password;
    private JButton submitBtn;
    private JButton registerBtn;
    private JButton returnBtn;
    private UserManager userManager;

    public LoginFrame(int width, int height) {
        this.setTitle("Login");
        this.setLayout(null);
        this.setSize(width, height);

        userManager = new UserManager(); // 初始化用户管理器
        JLabel userLabel = FrameUtil.createJLabel(this, new Point(tool.USERNAME_PASSWORD_X, tool.USERNAME_Y), tool.W_USERNAME_PASSWORD, tool.H_ALL, "Username:");
        JLabel passLabel = FrameUtil.createJLabel(this, new Point(tool.USERNAME_PASSWORD_X, tool.PASSWORD_Y), tool.W_USERNAME_PASSWORD, tool.H_ALL, "Password:");
        username = FrameUtil.createJTextField(this, new Point(tool.USERNAME_PASSWORD_X+tool.W_USERNAME_PASSWORD, tool.USERNAME_Y), tool.W_USERNAME_PASSWORD_WINDOW, tool.H_ALL);
        password = new JPasswordField();
        password.setBounds(tool.USERNAME_PASSWORD_X+tool.W_USERNAME_PASSWORD, tool.PASSWORD_Y, tool.W_USERNAME_PASSWORD_WINDOW, tool.H_ALL);
        this.add(password);

        submitBtn = FrameUtil.createButton(this, "Login", new Point(tool.USERNAME_PASSWORD_X,tool.BUTTON_Y ), tool.W_BUTTON, tool.H_ALL);
        registerBtn = FrameUtil.createButton(this, "Register", new Point(tool.REGISTER_X, tool.BUTTON_Y), tool.W_BUTTON, tool.H_ALL);
        returnBtn = FrameUtil.createButton(this, "Return",new Point(tool.RETURN_X,tool.BUTTON_Y),tool.W_BUTTON,tool.H_ALL);

        submitBtn.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword());
            if (userManager.login(user, pass)) {
                UserManager.GameState state = userManager.loadState(user);
                int[][] matrix = (state != null && state.matrix != null)
                        ? state.matrix
                        : new int[][] {
                        {3,7,7,5},
                        {3,7,7,5},
                        {4,2,2,6},
                        {4,0,0,6},
                        {1,1,1,1}
                };
                int initSteps = (state != null) ? state.steps : 0;//步数
                int initTime = (state != null) ? state.time : 0; //秒数
                MapModel map = new MapModel(matrix);
                GameFrame gf = new GameFrame(600, 450, map, user, userManager, initSteps, initTime);
                gf.setVisible(true);
                this.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        });

        registerBtn.addActionListener(e -> {
            new RegistrationFrame(this).setVisible(true);
            this.setVisible(false);
        });

        returnBtn.addActionListener(e -> {
            new LoginSelectionFrame(tool.LOGIN_FRAME_W, tool.LOGIN_FRAME_H).setVisible(true);
            this.setVisible(false);
        });

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
