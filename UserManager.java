// model/UserManager.java

package model;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UserManager {
    private static final String USER_DIR = "userdata";

    public UserManager() {
        File dir = new File(USER_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    public boolean register(String name, String pwd) {
        Path userFile = Paths.get(USER_DIR, name + ".txt");
        if (Files.exists(userFile)) return false;
        String hash = hash(pwd);
        try (BufferedWriter out = Files.newBufferedWriter(userFile)) {
            out.write(hash);       out.newLine();
            out.write("0");        // 初始步数为 0
            out.newLine();
            out.write("0");        // 初始用时为 0 秒
            out.newLine();
            // 初始无矩阵数据
        }
        catch (IOException e) { e.printStackTrace(); return false; }
        return true;
    }

    public boolean login(String name, String pwd) {
        Path userFile = Paths.get(USER_DIR, name + ".txt");
        if (!Files.exists(userFile)) return false;
        String expected;
        try (BufferedReader in = Files.newBufferedReader(userFile)) {
            expected = in.readLine();  // 读第一行（哈希）
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return expected != null && expected.equals(hash(pwd));
    }

    /** 保存游戏状态：包括步数、已用时间和矩阵 **/
    public void saveState(String name, int steps, int timeInSeconds, int[][] matrix) {
        Path userFile = Paths.get(USER_DIR, name + ".txt");
        if (!Files.exists(userFile)) return;
        try {
            // 先读出第一行（密码哈希）
            String hash;
            try (BufferedReader in = Files.newBufferedReader(userFile)) {
                hash = in.readLine();
            }
            // 写回：hash, steps, time, matrix...
            try (BufferedWriter out = Files.newBufferedWriter(userFile)) {
                // 第一行：哈希
                out.write(hash); out.newLine();
                // 第二行：步数
                out.write(Integer.toString(steps)); out.newLine();
                // 第三行：已用时间（秒）
                out.write(Integer.toString(timeInSeconds)); out.newLine();
                // 第四行起：矩阵数据
                for (int[] row : matrix) {
                    for (int i = 0; i < row.length; i++) {
                        out.write(Integer.toString(row[i]));
                        if (i < row.length - 1) out.write(",");
                    }
                    out.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 加载游戏状态：同时兼容“旧格式（无时间行）”和“新格式（有时间行）” **/
    public GameState loadState(String name) {
        Path userFile = Paths.get(USER_DIR, name + ".txt");
        if (!Files.exists(userFile)) {
            // 文件不存在，直接返回 null
            return null;
        }

        try (BufferedReader in = Files.newBufferedReader(userFile)) {
            // 1. 先读第一行：哈希，校验逻辑（登录时已验证，这里仅跳过）
            String hashLine = in.readLine();
            if (hashLine == null) {
                // 文件只写了哈希行却没有下一行，视为损坏
                return null;
            }

            // 2. 读取第二行：步数
            String stepsLine = in.readLine();
            if (stepsLine == null) {
                // 没有步数行，格式非法
                return null;
            }
            int steps;
            try {
                steps = Integer.parseInt(stepsLine.trim());
                if (steps < 0) {
                    // 不允许负数步数
                    return null;
                }
            } catch (NumberFormatException ex) {
                // 解析失败，说明步数行被破坏
                return null;
            }

            // 3. 读取第三行：可能是“时间”或直接是矩阵第一行
            String thirdLine = in.readLine();
            if (thirdLine == null) {
                // 只有两行（哈希 + 步数），说明是旧格式没有存档矩阵
                // 也算作没有可加载的游戏，直接返回一个 time=0, steps = 0, matrix = null
                // 也可以返回 new GameState(0, steps, null)；但为了统一，直接返回 null
                return null;
            }

            int timeInSeconds;
            List<String> matrixLines = new ArrayList<>();

            // 如果第三行包含逗号，说明是「旧格式」的矩阵行
            if (thirdLine.contains(",")) {
                // 旧格式：第三行就是矩阵的第一行
                timeInSeconds = 0;
                matrixLines.add(thirdLine.trim());
            } else {
                // 新格式：第三行应该是已用时间（秒）
                try {
                    timeInSeconds = Integer.parseInt(thirdLine.trim());
                    if (timeInSeconds < 0) {
                        // 不允许负数的时间
                        return null;
                    }
                } catch (NumberFormatException ex) {
                    // 如果第三行既不包含逗号，又无法解析为整数，就视作损坏
                    return null;
                }
            }

            // 4. 继续读取剩余行作为矩阵（如果有的话）
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    matrixLines.add(line);
                }
            }

            // 5. 如果没有任何矩阵行，就表示无存档矩阵，直接返回 null
            if (matrixLines.isEmpty()) {
                return null;
            }

            // 6. 将每一行以逗号分割，解析为 int[]，并且校验列数一致
            int rows = matrixLines.size();
            int cols = matrixLines.get(0).split(",").length;
            if (cols <= 0) {
                // 若列数为 0 也视为损坏
                return null;
            }
            int[][] matrix = new int[rows][cols];

            for (int i = 0; i < rows; i++) {
                String[] tokens = matrixLines.get(i).split(",");
                if (tokens.length != cols) {
                    // 如果某行列数和第一行不一致，视为损坏
                    return null;
                }
                for (int j = 0; j < cols; j++) {
                    try {
                        matrix[i][j] = Integer.parseInt(tokens[j].trim());
                    } catch (NumberFormatException ex) {
                        // 矩阵里存在无法解析的数字
                        return null;
                    }
                }
            }

            // 7. 最终返回一个合法的 GameState
            return new GameState(timeInSeconds, steps, matrix);

        } catch (IOException e) {
            // 读取文件时出现 IO 错误，也视为无法加载
            e.printStackTrace();
            return null;
        }
    }

    private String hash(String pwd) {
        return Integer.toHexString(pwd.hashCode());
    }

    /** 简单的状态容器 **/
    public static class GameState {
        public final int time;       // 已用时间（秒）
        public final int steps;      // 已走步数
        public final int[][] matrix; // 当前矩阵，null 表示无可用存档
        public GameState(int time, int steps, int[][] matrix) {
            this.time = time;
            this.steps = steps;
            this.matrix = matrix;
        }
    }
}