package view.game;

import controller.GameController;
import model.Direction;
import model.MapModel;
import model.UserManager;
import model.UserManager.GameState;
import tool.tool;
import view.FrameUtil;
import tool.MusicUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {
    private GameController controller;
    private GamePanel gamePanel;
    private JLabel stepLabel;
    private JLabel timeLabel;           // 用于显示计时的 JLabel
    private JComboBox<String> levelSelector;
    private JButton restartBtn;
    private JButton loadBtn;
    private JButton saveBtn;
    private JButton hintBtn;
    private final String currentUser;
    private final UserManager userManager;
    private MapModel mapModel;
    private int currentSteps;
    private int[][] originalMatrix;
    private int elapsedTime;            // 已用秒数
    private Timer timer;                // Swing Timer 每秒 +1

    // 保留游客模式构造器，游客模式下不显示存档控件，初始时间为 0
    public GameFrame(int width, int height, MapModel mapModel) {
        this(width, height, mapModel, null, null, 0, 0);
    }

    // 登录用户模式构造器：传入初始步数、初始时间
    public GameFrame(int width, int height, MapModel model,
                     String user, UserManager um,
                     int initialSteps, int initialTime) {
        super("2025 CS109 Project Demo");
        this.mapModel = model;
        this.currentUser = user;
        this.userManager = um;
        this.currentSteps = initialSteps;
        this.elapsedTime = initialTime;  // 从存档载入时传入的秒数
        initUI(width, height);
        initController();
        startTimer();                   // 构造完成后，启动计时器
    }

    private void initUI(int width, int height) {
        setLayout(null);
        setSize(width, height);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        MusicUtil.playBGM("/resources/bgm.wav");

        // 设置窗口关闭时的监听器（除了停止BGM，还要自动存档）
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MusicUtil.stopBGM();
                // 登录用户模式下自动存档：把当前 steps、matrix、elapsedTime 都存进去
                if (userManager != null && currentUser != null) {
                    userManager.saveState(currentUser,
                            gamePanel.getSteps(),
                            elapsedTime,
                            mapModel.getMatrix());
                }
                super.windowClosing(e);
            }
        });

        // 关卡选择
        String[] levels = {
                "横刀立马", "指挥若定", "将拥曹营", "齐头并进", "兵分三路",
                "捷足先登", "左右布兵", "围而不坚", "插翅难飞", "守口如瓶",
                "近在咫尺", "五将逼供"
        };
        levelSelector = new JComboBox<>(levels);
        levelSelector.setBounds(20, 20, 120, 30);
        add(levelSelector);
        levelSelector.addActionListener(e -> loadLevelByName((String) levelSelector.getSelectedItem()));

        // 游戏面板
        gamePanel = new GamePanel(mapModel);
        gamePanel.setLocation(tool.GRID_SIZE, tool.GRID_SIZE);
        gamePanel.setStepCount(currentSteps);
        add(gamePanel);

        // 步数标签
        stepLabel = FrameUtil.createJLabel(this,
                "Step: " + currentSteps,
                new Font("serif", Font.ITALIC, 22),
                new Point(gamePanel.getPanelWidth() + 80, 70),
                180, 50);
        gamePanel.setStepLabel(stepLabel);

        // 计时标签
        timeLabel = FrameUtil.createJLabel(this,
                formatTime(elapsedTime),
                new Font("serif", Font.PLAIN, 20),
                new Point(gamePanel.getPanelWidth() + 80, 20),
                150, 40);
        add(timeLabel);

        // 按钮：Restart
        restartBtn = FrameUtil.createButton(this,
                "Restart",
                new Point(gamePanel.getPanelWidth() + 80, 120),
                150, 50);
        restartBtn.addActionListener(e -> {
            controller.clearUndoStack();
            mapModel.setMatrix(originalMatrix);
            totallyReset();

            // 重置步数与显示
            currentSteps = 0;
            updateStepLabel();

            // 重置计时：归零
            elapsedTime = 0;
            updateTimeLabel();
            gamePanel.requestFocusInWindow();
        });
        add(restartBtn);

        // 撤销按钮
        JButton undoBtn = FrameUtil.createButton(this, "Undo",
                new Point(gamePanel.getPanelWidth() + 80, 330), 150, 50);
        undoBtn.addActionListener(e -> {
            controller.undo();
            gamePanel.revalidate();
            gamePanel.repaint();
            gamePanel.requestFocusInWindow();
        });
        add(undoBtn);

        hintBtn = FrameUtil.createButton(this,
                "Hint",
                new Point(gamePanel.getPanelWidth() + 80, 390),
                150, 50);
        hintBtn.addActionListener(e -> {
            Direction hint = controller.getHint();
            if (hint != null) {
                showHintAnimation(hint);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No available hint!",
                        "Hint",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        add(hintBtn);

        // 仅登录用户模式下，显示 Load/Save
        if (userManager != null && currentUser != null) {
            loadBtn = FrameUtil.createButton(this,
                    "Load",
                    new Point(gamePanel.getPanelWidth() + 80, 190),
                    150, 50);
            loadBtn.addActionListener(e -> {
                UserManager.GameState gs = userManager.loadState(currentUser);
                if (gs == null) {
                    // 说明文件不存在或被解析出错，提示并直接返回
                    JOptionPane.showMessageDialog(
                            this,
                            "无法加载存档（文件不存在或已损坏），请检查保存文件。",
                            "加载失败",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                // 只有当 gs.matrix 非空才代表确实有可用的存档
                if (gs.matrix == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "当前账号暂无有效存档。",
                            "加载信息",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    // 矩阵合法，从存档恢复
                    mapModel.setMatrix(gs.matrix);
                    resetCurrentLevel();
                    // 恢复步数
                    currentSteps = gs.steps;
                    updateStepLabel();
                    // 恢复时间
                    elapsedTime = gs.time;
                    updateTimeLabel();
                }
                gamePanel.requestFocusInWindow();
            });
            add(loadBtn);

            saveBtn = FrameUtil.createButton(this,
                    "Save",
                    new Point(gamePanel.getPanelWidth() + 80, 260),
                    150, 50);
            saveBtn.addActionListener(e -> {
                userManager.saveState(currentUser,
                        gamePanel.getSteps(),
                        elapsedTime,
                        mapModel.getMatrix());
                JOptionPane.showMessageDialog(
                        this,
                        "游戏已成功保存！",
                        "保存成功",
                        JOptionPane.INFORMATION_MESSAGE
                );
                gamePanel.requestFocusInWindow();
            });
            add(saveBtn);
        }


        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initController() {
        controller = new GameController(gamePanel, mapModel);
        gamePanel.setController(controller);
    }

    private void loadLevelByName(String name) {
        int[][] matrix;
        switch (name) {
            case "横刀立马": matrix = tool.hengdaolima_1; break;
            case "指挥若定": matrix = tool.zhihuiruoding_1; break;
            case "将拥曹营": matrix = tool.jiangyongcaoying_1; break;
            case "齐头并进": matrix = tool.qitoubingjin_1; break;
            case "兵分三路": matrix = tool.bingfensanlu_1; break;
            case "捷足先登": matrix = tool.jiezuxiandeng_1; break;
            case "左右布兵": matrix = tool.zuoyoububing_1; break;
            case "围而不坚": matrix = tool.weierbujian_1; break;
            case "插翅难飞": matrix = tool.chachinanfei_2; break;
            case "守口如瓶": matrix = tool.shoukouruping_2; break;
            case "近在咫尺": matrix = tool.jinzaizhichi_2; break;
            case "五将逼供": matrix = tool.wujiangbigong_3; break;
            default: matrix = tool.hengdaolima_1;
        }
        mapModel.setMatrix(matrix);
        currentSteps = 0;
        totallyReset();
        updateStepLabel();
        // 切换关卡时，也将计时归零
        elapsedTime = 0;
        updateTimeLabel();
        originalMatrix = matrix;
    }

    private void resetCurrentLevel() {
        controller.restartGame();
    }
    private void totallyReset(){
        controller.totallyRestart();
    }

    private void updateStepLabel() {
        stepLabel.setText("Step: " + currentSteps);
        gamePanel.setStepCount(currentSteps);
    }

    private void updateTimeLabel() {
        timeLabel.setText(formatTime(elapsedTime));
    }

    // 格式化秒数为 mm:ss，比如 83 -> "01:23"
    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("Time: %02d:%02d", m, s);
    }

    // 启动 Swing Timer，让 elapsedTime 从 initialTime 开始每秒 +1
    private void startTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        timer = new Timer(1000, e -> {
            elapsedTime++;
            updateTimeLabel();
        });
        timer.start();
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    private void showHintAnimation(Direction dir) {
        new ArrowOverlay(dir).startAnimation();
    }
}

