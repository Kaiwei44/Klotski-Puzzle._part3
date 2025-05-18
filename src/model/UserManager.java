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
            // 初始无矩阵数据，或者可以写默认关卡
        }
        catch (IOException e) { e.printStackTrace(); return false; }
        return true;
    }

    public boolean login(String name, String pwd) {
        Path userFile = Paths.get(USER_DIR, name + ".txt");
        if (!Files.exists(userFile)) return false;
        String expected;
        try (BufferedReader in = Files.newBufferedReader(userFile)) {
            expected = in.readLine();  // 读第一行
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return expected != null && expected.equals(hash(pwd));
    }

    /** 保存游戏状态：包括步数和矩阵 **/
    public void saveState(String name, int steps, int[][] matrix) {
        Path userFile = Paths.get(USER_DIR, name + ".txt");
        if (!Files.exists(userFile)) return;
        try {
            // 先读出第一行（密码哈希）
            String hash;
            try (BufferedReader in = Files.newBufferedReader(userFile)) {
                hash = in.readLine();
            }
            // 写回：hash, steps, matrix...
            try (BufferedWriter out = Files.newBufferedWriter(userFile)) {
                out.write(hash); out.newLine();
                out.write(Integer.toString(steps)); out.newLine();
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

    /** 加载游戏状态：返回一个包装类 GameState **/
    public GameState loadState(String name) {
        Path userFile = Paths.get(USER_DIR, name + ".txt");
        if (!Files.exists(userFile)) return null;
        try (BufferedReader in = Files.newBufferedReader(userFile)) {
            in.readLine();               // 跳过哈希
            String stepsLine = in.readLine();
            int steps = stepsLine != null ? Integer.parseInt(stepsLine) : 0;

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                lines.add(line);
            }
            if (lines.isEmpty()) {
                return new GameState(steps, null);
            }
            int rows = lines.size();
            int cols = lines.get(0).split(",").length;
            int[][] m = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                String[] cells = lines.get(i).split(",");
                for (int j = 0; j < cols; j++) {
                    m[i][j] = Integer.parseInt(cells[j]);
                }
            }
            return new GameState(steps, m);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String hash(String pwd) {
        return Integer.toHexString(pwd.hashCode());
    }

    /** 简单的状态容器 **/
    public static class GameState {
        public final int steps;
        public final int[][] matrix;
        public GameState(int steps, int[][] matrix) {
            this.steps = steps;
            this.matrix = matrix;
        }
    }
}