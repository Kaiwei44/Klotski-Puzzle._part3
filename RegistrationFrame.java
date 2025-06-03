// view/login/RegistrationFrame.java
package view.login;

import model.UserManager;
import tool.tool;
import view.FrameUtil;
import view.start.LoginSelectionFrame;

import javax.swing.*;
import java.awt.*;

public class RegistrationFrame extends JFrame {
    private JTextField username;
    private JPasswordField password;
    private JButton registerBtn;
    private UserManager userManager;
    private JButton returnBtn;
    private final JFrame loginFrame;

    public RegistrationFrame(JFrame loginFrame) {
        this.loginFrame=loginFrame;
        this.setTitle("Register");
        this.setLayout(null);
        this.setSize(tool.LOGIN_FRAME_W, tool.LOGIN_FRAME_H);

        userManager = new UserManager();  // 初始化用户管理器

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(tool.USERNAME_PASSWORD_X, tool.USERNAME_Y, tool.W_USERNAME_PASSWORD, tool.H_ALL);//username位置
        this.add(userLabel);

        username = new JTextField();
        username.setBounds(tool.USERNAME_PASSWORD_X+tool.W_USERNAME_PASSWORD, tool.USERNAME_Y, tool.W_USERNAME_PASSWORD_WINDOW, tool.H_ALL);//username窗口位置
        this.add(username);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(tool.USERNAME_PASSWORD_X, tool.PASSWORD_Y, tool.W_USERNAME_PASSWORD, tool.H_ALL);//password位置
        this.add(passLabel);

        password = new JPasswordField();
        password.setBounds(tool.USERNAME_PASSWORD_X+tool.W_USERNAME_PASSWORD, tool.PASSWORD_Y, tool.W_USERNAME_PASSWORD_WINDOW, tool.H_ALL);//password窗口位置
        this.add(password);

        registerBtn = new JButton("Register");
        registerBtn.setBounds(tool.REGISTER_X, tool.BUTTON_Y, tool.W_BUTTON, tool.H_ALL);//register位置
        this.add(registerBtn);
        returnBtn = FrameUtil.createButton(this, "Return",new Point(20,40),tool.W_BUTTON,tool.H_ALL);//return位置

        returnBtn.addActionListener(e -> {
            // 返回到 LoginSelectionFrame
            new LoginFrame(tool.LOGIN_FRAME_W, tool.LOGIN_FRAME_H).setVisible(true);
            this.setVisible(false); // 隐藏当前登录界面
        });

        registerBtn.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword());

            if (userManager.register(user, pass)) {
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                loginFrame.setVisible(true);  // 返回登录界面
                this.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "User already exists!");
            }
        });

        // 注册按钮逻辑：调用 UserManager.register()
        registerBtn.addActionListener(e -> {
            String name = username.getText().trim();
            String pwd  = new String(password.getPassword());

            if (name.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Username and password cannot be empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 如果注册成功，则返回登录界面；否则提示用户已存在
            if (userManager.register(name, pwd)) {
                JOptionPane.showMessageDialog(this,
                        "Registration Successful!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                this.loginFrame.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Username already exists!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // 返回按钮逻辑：直接回到登录界面
        returnBtn.addActionListener(e -> {
            this.loginFrame.setVisible(true);
            this.dispose();
        });

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
