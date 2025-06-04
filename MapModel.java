package model;

import java.util.Arrays;
import java.util.Stack;

/**
 * MapModel 管理华容道棋盘的状态，包括原始矩阵、当前矩阵和唯一ID标记。
 */
public class MapModel {
    private int[][] matrix;
    private int[][] uniqueIds;
    private final int[][] initialMatrix;
    private final int height;
    private final int width;
    //private final int[][] original_matrix;

    /**
     * 构造时深拷贝传入矩阵，并初始化 initialMatrix、uniqueIds。
     */
    public MapModel(int[][] matrix) {
        this.height = matrix.length;
        this.width = matrix[0].length;
        this.matrix = new int[height][width];
        this.initialMatrix = new int[height][width];
        this.uniqueIds = new int[height][width];


        // 深拷贝初始矩阵
        for (int i = 0; i < height; i++) {
            System.arraycopy(matrix[i], 0, this.matrix[i], 0, width);
            System.arraycopy(matrix[i], 0, this.initialMatrix[i], 0, width);
        }
    }

    /**
     * 安全地设置单元格 ID，仅当置空或填空时生效。
     */
    public synchronized boolean setIdSafely(int row, int col, int id) {
        if (checkInHeightSize(row) && checkInWidthSize(col)) {
            if (id == 0 || matrix[row][col] == 0) {
                matrix[row][col] = id;
                return true;
            }
        }
        return false;
    }

    /** 获取单元格 ID，越界返回 -1。 */
    public synchronized int getId(int row, int col) {
        return checkInHeightSize(row) && checkInWidthSize(col) ? matrix[row][col] : -1;
    }

    /** 直接设置单元格 ID（慎用）。 */
    public synchronized void setId(int row, int col, int id) {
        if (checkInHeightSize(row) && checkInWidthSize(col)) {
            matrix[row][col] = id;
        }
    }

    /** 获取单元格的唯一 ID，用于多格方块标识。 */
    public synchronized int getUniqueId(int row, int col) {
        return checkInHeightSize(row) && checkInWidthSize(col) ? uniqueIds[row][col] : 0;
    }

    /** 设置单元格的唯一 ID。 */
    public synchronized void setUniqueId(int row, int col, int uniqueId) {
        if (checkInHeightSize(row) && checkInWidthSize(col)) {
            uniqueIds[row][col] = uniqueId;
        }
    }

    /**
     * 重置为初始状态：恢复 matrix，清空 uniqueIds。
     */
    public synchronized void reset() {
        for (int i = 0; i < height; i++) {
            System.arraycopy(initialMatrix[i], 0, matrix[i], 0, width);
            Arrays.fill(uniqueIds[i], 0);
        }
    }

    /** 打印当前矩阵和唯一 ID 信息，便于调试。 */
    public synchronized void printDebugInfo() {
        System.out.println("Current Map State:");
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println("Unique IDs:");
        for (int[] row : uniqueIds) {
            System.out.println(Arrays.toString(row));
        }
    }

    /** 检查行越界。 */
    public boolean checkInHeightSize(int row) {
        return row >= 0 && row < height;
    }

    /** 检查列越界。 */
    public boolean checkInWidthSize(int col) {
        return col >= 0 && col < width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * 获取当前矩阵的深拷贝，避免外部修改。
     */
    public synchronized int[][] getMatrix() {
        int[][] copy = new int[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    /**
     * 设置新的矩阵状态；要求尺寸一致，并清空 uniqueIds。
     */
    public synchronized void setMatrix(int[][] newMatrix) {
        if (newMatrix.length != height || newMatrix[0].length != width) {
            throw new IllegalArgumentException("Matrix dimensions must match model size");
        }
        for (int i = 0; i < height; i++) {
            System.arraycopy(newMatrix[i], 0, this.matrix[i], 0, width);
            Arrays.fill(uniqueIds[i], 0);
        }
    }

    public void setUniqueIds(int[][] uniqueIds) {
        for (int i = 0; i < uniqueIds.length; i++) {
            this.uniqueIds[i] = Arrays.copyOf(uniqueIds[i], uniqueIds[i].length);
        }
    }

    public synchronized int[] getEmptyPosition() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (matrix[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    // 生成当前状态的哈希值（用于去重）
    public synchronized long getStateHash() {
        long hash = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                hash = 31 * hash + matrix[i][j];
            }
        }
        return hash;
    }

    // 记录移动历史（用于回退）
    private final Stack<int[][]> moveHistory = new Stack<>();

    // 保存当前状态到历史记录
    public synchronized void saveState() {
        int[][] copy = new int[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, width);
        }
        moveHistory.push(copy);
    }

    // 回退到上一步
    public synchronized boolean undo() {
        if (!moveHistory.isEmpty()) {
            matrix = moveHistory.pop();
            return true;
        }
        return false;
    }

    // 复制矩阵的方法
    public int[][] getMatrixCopy() {
        int[][] copy = new int[getHeight()][getWidth()];
        for (int i = 0; i < getHeight(); i++) {
            System.arraycopy(this.matrix[i], 0, copy[i], 0, getWidth());
        }
        return copy;
    }

    public synchronized int[][] getUniqueIdsMatrix() {
        int[][] copy = new int[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(uniqueIds[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    public synchronized void restoreState(int[][] matrix, int[][] uniqueIds) {
        // 深度拷贝恢复
        for (int i = 0; i < height; i++) {
            System.arraycopy(matrix[i], 0, this.matrix[i], 0, width);
            System.arraycopy(uniqueIds[i], 0, this.uniqueIds[i], 0, width);
        }
    }

    // 添加canMove()方法
    public synchronized boolean canMove(int row, int col, Direction dir) {
        // 先判一下行列是否合法
        if (!checkInHeightSize(row) || !checkInWidthSize(col)) return false;
        int id = getId(row, col);
        if (id == 0) return false;               // 空格上当然不能“移动”
        int uniqueId = getUniqueId(row, col);

        // 根据方块的 id 决定它究竟是 1x1 / 2x1 / 1x2 / 2x2
        int width = (id == 2 || id == 7) ? 2 : 1;
        int height = (id >= 3 && id <= 7)   ? 2 : 1;

        int newRow = row + dir.getRow();
        int newCol = col + dir.getCol();

        // 边界检查
        if (newRow < 0 || newRow + height > getHeight()) return false;
        if (newCol < 0 || newCol + width  > getWidth())  return false;

        // 检查目标区域所有格子
        for (int r = newRow; r < newRow + height; r++) {
            for (int c = newCol; c < newCol + width; c++) {
                int targetId   = getId(r, c);
                int targetUid  = getUniqueId(r, c);

                // 情况 1：目标格子本身就是“自己方块”的一部分
                if (targetUid == uniqueId && targetId == id) {
                    // 允许：自己身体“滑动过去”的重叠区域
                    continue;
                }
                // 情况 2：目标格子必须是空格
                if (targetId == 0 && targetUid == 0) {
                    continue;
                }
                // 其他一律不允许
                return false;
            }
        }

        return true;
    }

    // 添加move()方法
    public synchronized boolean move(int row, int col, Direction dir) {
        // 先检查能否移动
        if (!canMove(row, col, dir)) {
            return false;
        }

        int id       = getId(row, col);
        int uniqueId = getUniqueId(row, col);
        int width    = (id == 2 || id == 7) ? 2 : 1;
        int height   = (id >= 3 && id <= 7)   ? 2 : 1;

        int newRow = row + dir.getRow();
        int newCol = col + dir.getCol();

        // 1. 清空“自己”原来位置的所有格子
        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                if (getUniqueId(r, c) == uniqueId) {
                    setId(r, c, 0);
                    setUniqueId(r, c, 0);
                }
            }
        }

        // 2. 把相同的 id/uniqueId 写到新位置
        for (int r = newRow; r < newRow + height; r++) {
            for (int c = newCol; c < newCol + width; c++) {
                setId(r, c, id);
                setUniqueId(r, c, uniqueId);
            }
        }

        return true;
    }
}