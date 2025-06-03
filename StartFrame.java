package view.start;

import model.MapModel;
import view.FrameUtil;
import view.game.GameFrame;

import javax.swing.*;
import java.awt.*;
public class StartFrame extends JFrame {
    public StartFrame(int width, int height) {
        this.setTitle("Klotski Puzzle");
        this.setLayout(null);
        this.setSize(width, height);

        // “开始游戏”按钮
        JButton newGameBtn = FrameUtil.createButton(
                this, "New Game", new Point( width/2-50, 50 ), 100, 40
        );
        // “退出”按钮
        JButton exitBtn = FrameUtil.createButton(
                this, "Exit", new Point( width/2-50, 120 ), 100, 40
        );

        newGameBtn.addActionListener(e -> {
            // —— 这里用一个 4×5 的示例数组初始化 MapModel ——
            // 你可以根据 Task1 要求，把下面的 matrix 调整成：
            // 1×1 士兵 4 片，1×2 将军 4 片，2×1 关羽 1 片，2×2 曹操 1 片，和 2 个空格
            int[][] matrix = {
                    {1, 4, 4, 1},
                    {1, 4, 4, 1},
                    {3, 2, 2, 3},
                    {3, 0, 0, 3},
                    {1, 1, 1, 1}
            };

            MapModel model = new MapModel(matrix);
            GameFrame gameFrame = new GameFrame(600, 450, model);
            gameFrame.setVisible(true);
            this.setVisible(false);
        });

        exitBtn.addActionListener(e -> System.exit(0));

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
