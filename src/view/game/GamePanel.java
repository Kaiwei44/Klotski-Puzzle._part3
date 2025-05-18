package view.game;

import controller.GameController;
import model.Direction;
import model.MapModel;
import tool.tool;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends ListenerPanel {
    private List<BoxComponent> boxes;
    private MapModel model;
    private GameController controller;
    private JLabel stepLabel;
    private int steps;
    private final int GRID_SIZE = tool.GRID_SIZE;
    private BoxComponent selectedBox;
    private boolean victory = false;
    private BufferedImage backgroundImage;
    private int bigBlockUniqueId = 1000; // 大块唯一ID生成器


    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.setSize(model.getWidth() * GRID_SIZE + (int)(GRID_SIZE * 7 / 5),
                model.getHeight() * GRID_SIZE + (int)(GRID_SIZE * 12 / 5));
        this.model = model;
        this.selectedBox = null;
        backgroundImage = tool.BACKGROUND;
        initialGame();
    }

    public void initialGame() {
        //this.steps = 0;
        int[][] map = new int[model.getHeight()][model.getWidth()];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = model.getId(i, j);
            }
        }
        int soldierId = 1;
        bigBlockUniqueId = 1000; // 重置大块ID

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                BoxComponent box = null;
                BufferedImage characterImage = null;
                Color boxColor = Color.GRAY; // 默认颜色

                if (map[i][j] == 1) {  // 士兵块
                    characterImage = tool.SOLDIER_IMAGE;
                    boxColor = Color.ORANGE;
                    box = new BoxComponent(boxColor, i, j, characterImage);
                    box.setSize(GRID_SIZE, GRID_SIZE);
                    box.setUniqueId(soldierId);
                    model.setUniqueId(i, j, soldierId++);
                    map[i][j] = 0;

                } else if (map[i][j] == 2) {  // 关羽（横向2x1）
                    characterImage = tool.GUANYU_IMAGE;
                    boxColor = Color.PINK;
                    box = new BoxComponent(boxColor, i, j, characterImage);
                    box.setSize(GRID_SIZE*2, GRID_SIZE);
                    int uniqueId = bigBlockUniqueId++;
                    box.setUniqueId(uniqueId);
                    model.setUniqueId(i, j, uniqueId);
                    model.setUniqueId(i, j+1, uniqueId);
                    map[i][j] = 0;
                    map[i][j + 1] = 0;

                } else if (map[i][j] >= 3 && map[i][j] <= 6) {  // 纵向1x2块
                    switch (map[i][j]) {
                        case 3:
                            characterImage = tool.MACHAO_IMAGE;
                            boxColor = Color.YELLOW;
                            break;
                        case 4:
                            characterImage = tool.HUANGZHONG_IMAGE;
                            boxColor = Color.BLUE;
                            break;
                        case 5:
                            characterImage = tool.ZHAOYUN_IMAGE;
                            boxColor = Color.RED;
                            break;
                        case 6:
                            characterImage = tool.ZHANGFEI_IMAGE;
                            boxColor = Color.CYAN;
                            break;
                    }
                    box = new BoxComponent(boxColor, i, j, characterImage);
                    box.setSize(GRID_SIZE, GRID_SIZE * 2);
                    int uniqueId = bigBlockUniqueId++;
                    box.setUniqueId(uniqueId);
                    model.setUniqueId(i, j, uniqueId);
                    model.setUniqueId(i + 1, j, uniqueId);
                    map[i][j] = 0;
                    map[i + 1][j] = 0;

                } else if (map[i][j] == 7) {  // 曹操块（2x2）
                    characterImage = tool.CAOCAO_IMAGE;
                    boxColor = Color.GREEN;
                    box = new BoxComponent(boxColor, i, j, characterImage);
                    box.setSize(GRID_SIZE * 2, GRID_SIZE * 2);
                    int uniqueId = bigBlockUniqueId++;
                    box.setUniqueId(uniqueId);
                    model.setUniqueId(i, j, uniqueId);
                    model.setUniqueId(i, j + 1, uniqueId);
                    model.setUniqueId(i + 1, j, uniqueId);
                    model.setUniqueId(i + 1, j + 1, uniqueId);
                    map[i][j] = 0;
                    map[i + 1][j] = 0;
                    map[i][j + 1] = 0;
                    map[i + 1][j + 1] = 0;
                }

                if (box != null) {
                    int boxX = j * GRID_SIZE + (int)(0.7*GRID_SIZE);
                    int boxY = i * GRID_SIZE + (int)(1.5*GRID_SIZE);
                    box.setLocation(boxX, boxY);
                    boxes.add(box);
                    this.add(box);
                }
            }
        }
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
        g.setColor(Color.LIGHT_GRAY);
        int x = (int)(0.7*GRID_SIZE);
        int y = (int)(1.5*GRID_SIZE);
        g.fillRect(x, y, model.getWidth() * GRID_SIZE, model.getHeight() * GRID_SIZE);
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
    }

    public void setStepCount(int s) {
        this.steps = s;
        if (stepLabel != null) {
            stepLabel.setText("Step: " + steps);
        }
    }
    public int getSteps() {
        return steps;
    }



    public void afterMove(int oldRow, int oldCol, int newRow, int newCol) {
        // 1. 更新步数显示
        this.steps++;
        if (stepLabel != null) {
            stepLabel.setText(String.format("Step: %d", this.steps));
        }

        // 2. 更新方块的模型坐标
        if (selectedBox != null) {
            selectedBox.setRow(newRow);
            selectedBox.setCol(newCol);
        }

        // 3. 计算新位置的实际像素坐标
        int newX = newCol * GRID_SIZE + (int)(0.7 * GRID_SIZE);
        int newY = newRow * GRID_SIZE + (int)(1.5 * GRID_SIZE);

        // 4. 设置方块的新位置（需在EDT线程操作）
        SwingUtilities.invokeLater(() -> {
            if (selectedBox != null) {
                selectedBox.setLocation(newX, newY);
                selectedBox.repaint();
            }
            this.repaint(); // 强制重绘整个面板
        });

        // 5. 解除选中状态（可选，根据需求）
        // selectedBox.setSelected(false);
        // selectedBox = null;

        // 6. 检查胜利条件
        checkVictory();
    }

    private void checkVictory() {
        if (victory) return;

        for (BoxComponent box : boxes) {
            // 检查是否为曹操块（2x2）
            if (box.getWidth() == GRID_SIZE * 2 && box.getHeight() == GRID_SIZE * 2) {
                // 确保不会越界
                if (box.getRow() + 1 >= model.getHeight() || box.getCol() + 1 >= model.getWidth()) {
                    continue;
                }
                // 检查是否覆盖目标区域（row=3, col=1）
                if (box.getRow() == 3 && box.getCol() == 1) {
                    victory = true;
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("🎉 Victory! Steps: %d", steps),
                            "Congratulations",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    break;
                }
            }
        }
    }

    // 其他原有方法保持不变（reset, doMouseClick, doMoveRight等）...

    public void reset() {
        this.removeAll();
        boxes.clear();
        selectedBox = null;
        steps = 0;
        if (stepLabel != null) {
            stepLabel.setText("Step: 0");
            this.add(stepLabel);
        }
        initialGame();
        this.repaint();
        this.requestFocusInWindow();
    }

    @Override
    public void doMouseClick(Point point) {
        Component component = this.getComponentAt(point);
        if (component instanceof BoxComponent clickedComponent) {
            if (selectedBox == null) {
                selectedBox = clickedComponent;
                selectedBox.setSelected(true);
            } else if (selectedBox != clickedComponent) {
                selectedBox.setSelected(false);
                clickedComponent.setSelected(true);
                selectedBox = clickedComponent;
            } else {
                clickedComponent.setSelected(false);
                selectedBox = null;
            }
        }
    }

    @Override
    public void doMoveRight() {
        if (selectedBox != null) {
            int oldRow = selectedBox.getRow();
            int oldCol = selectedBox.getCol();

            // 调用控制器验证移动是否合法
            if (controller.doMove(oldRow, oldCol, Direction.RIGHT)) {
                // 更新视图（新位置：列+1）
                afterMove(oldRow, oldCol, oldRow, oldCol + 1);
            }
        }
    }

    @Override
    public void doMoveLeft() {
        if (selectedBox != null) {
            int oldRow = selectedBox.getRow();
            int oldCol = selectedBox.getCol();

            if (controller.doMove(oldRow, oldCol, Direction.LEFT)) {
                // 更新视图（新位置：列-1）
                afterMove(oldRow, oldCol, oldRow, oldCol - 1);
            }
        }
    }

    @Override
    public void doMoveUp() {
        if (selectedBox != null) {
            int oldRow = selectedBox.getRow();
            int oldCol = selectedBox.getCol();

            if (controller.doMove(oldRow, oldCol, Direction.UP)) {
                // 更新视图（新位置：行-1）
                afterMove(oldRow, oldCol, oldRow - 1, oldCol);
            }
        }
    }

    @Override
    public void doMoveDown() {
        if (selectedBox != null) {
            int oldRow = selectedBox.getRow();
            int oldCol = selectedBox.getCol();

            if (controller.doMove(oldRow, oldCol, Direction.DOWN)) {
                // 更新视图（新位置：行+1）
                afterMove(oldRow, oldCol, oldRow + 1, oldCol);
            }
        }
    }

    public int getPanelWidth() {
        return this.getWidth(); // 返回面板实际宽度
    }

    public int getPanelHeight() {
        return this.getHeight(); // 返回面板实际高度
    }

    public void setStepLabel(JLabel stepLabel) {
        this.stepLabel = stepLabel;
        this.add(stepLabel); // 将标签添加到面板
        stepLabel.setLocation(10, 10); // 设置标签位置示例
    }

    public void setController(GameController controller) {
        this.controller = controller; // 确保类中有成员变量声明
    }
}