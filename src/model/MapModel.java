package model;

import java.util.Arrays;

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

}
