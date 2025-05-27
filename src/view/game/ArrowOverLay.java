package view.game;

import model.Direction;
import javax.swing.*;
import java.awt.*;
import static tool.tool.GRID_SIZE; // 明确导入静态变量

class ArrowOverlay extends JComponent {
    private static final int ANIM_DURATION = 1000;
    private final Direction direction;
    private long startTime;

    public ArrowOverlay(Direction dir) {
        this.direction = dir;
        setOpaque(false);
        // 使用相对尺寸计算
        setSize(GRID_SIZE * 4, GRID_SIZE * 5);
    }

    public void startAnimation() {
        startTime = System.currentTimeMillis();
        Timer timer = new Timer(10, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > ANIM_DURATION) {
                ((Timer)e.getSource()).stop();
                setVisible(false);
            }
            repaint();
        });
        timer.start();
        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // 设置半透明效果
        float alpha = 1 - (float)(System.currentTimeMillis() - startTime) / ANIM_DURATION;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // 绘制方向箭头
        Polygon arrow = createArrowShape();
        g2d.setColor(Color.YELLOW);
        g2d.fill(arrow);
        g2d.dispose();
    }

    private Polygon createArrowShape() {
        // 基于GRID_SIZE动态计算箭头尺寸
        int baseX = GRID_SIZE * 2;
        int baseY = GRID_SIZE * 3;
        int tipOffset = GRID_SIZE;
        int wingOffset = GRID_SIZE / 2;

        int[] xPoints, yPoints;
        switch (direction) {
            case UP:
                xPoints = new int[]{baseX, baseX + tipOffset, baseX - tipOffset};
                yPoints = new int[]{baseY + GRID_SIZE, baseY, baseY};
                break;
            case DOWN:
                xPoints = new int[]{baseX, baseX + tipOffset, baseX - tipOffset};
                yPoints = new int[]{baseY - GRID_SIZE, baseY, baseY};
                break;
            case LEFT:
                xPoints = new int[]{baseX + GRID_SIZE, baseX, baseX};
                yPoints = new int[]{baseY, baseY + wingOffset, baseY - wingOffset};
                break;
            case RIGHT:
                xPoints = new int[]{baseX - GRID_SIZE, baseX, baseX};
                yPoints = new int[]{baseY, baseY + wingOffset, baseY - wingOffset};
                break;
            default:
                xPoints = new int[0];
                yPoints = new int[0];
        }
        return new Polygon(xPoints, yPoints, 3);
    }
}