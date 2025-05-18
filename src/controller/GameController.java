package controller;

import model.Direction;
import model.MapModel;
import view.game.GamePanel;

public class GameController {
    private final GamePanel view;
    private final MapModel model;

    public GameController(GamePanel view, MapModel model) {
        this.view = view;
        this.model = model;
        // 将自身设置到视图，以便面板内调用 doMove
        view.setController(this);
    }

    /**
     * 重新开始游戏，重置模型和界面
     */
    public void restartGame() {
        model.reset();
        view.reset();
        System.out.println("Game restarted.");
    }

    public void totallyRestart(){
        view.reset();
    }

    /**
     * 执行一次移动，返回是否成功
     */
    public boolean doMove(int row, int col, Direction direction) {
        int currentId = getCompleteBoxId(row, col);
        if (currentId == 0) return false;

        int currentUniqueId = model.getUniqueId(row, col);
        int currentWidth = getCurrentWidth(currentId);
        int currentHeight = getCurrentHeight(currentId);

        int newRow = row + direction.getRow();
        int newCol = col + direction.getCol();

        // 校验边界与碰撞
        if (!isMoveValid(newRow, newCol, currentWidth, currentHeight, currentUniqueId)) {
            return false;
        }

        // 执行移动
        return performMove(row, col, newRow, newCol, currentWidth, currentHeight, currentUniqueId);
    }

    // 判断方块完整 ID，用于确认 2x1、1x2、2x2 大小
    private int getCompleteBoxId(int row, int col) {
        int baseId = model.getId(row, col);
        if (baseId <= 0) return 0;

        switch (baseId) {
            case 2: // 横向 2x1
                boolean right = (col + 1 < model.getWidth()) && model.getId(row, col + 1) == 2;
                boolean left = (col - 1 >= 0) && model.getId(row, col - 1) == 2;
                return (right || left) ? 2 : 0;

            case 3: case 4: case 5: case 6: // 竖向 1x2
                boolean down = (row + 1 < model.getHeight()) && model.getId(row + 1, col) == baseId;
                boolean up = (row - 1 >= 0) && model.getId(row - 1, col) == baseId;
                return (down || up) ? baseId : 0;

            case 7: // 2x2
                int startRow = row;
                while (startRow > 0 && model.getId(startRow - 1, col) == 7) startRow--;
                int startCol = col;
                while (startCol > 0 && model.getId(row, startCol - 1) == 7) startCol--;
                if (startRow + 1 >= model.getHeight() || startCol + 1 >= model.getWidth()) return 0;
                boolean ok = model.getId(startRow, startCol) == 7
                        && model.getId(startRow, startCol + 1) == 7
                        && model.getId(startRow + 1, startCol) == 7
                        && model.getId(startRow + 1, startCol + 1) == 7;
                return ok ? 7 : 0;

            default: // 1x1
                return baseId;
        }
    }

    // 边界与撞块检测
    private boolean isMoveValid(int newRow, int newCol,
                                int width, int height,
                                int uniqueId) {
        if (newRow < 0 || newRow + height > model.getHeight()) return false;
        if (newCol < 0 || newCol + width > model.getWidth()) return false;

        for (int r = newRow; r < newRow + height; r++) {
            for (int c = newCol; c < newCol + width; c++) {
                int uid = model.getUniqueId(r, c);
                if (uid != 0 && uid != uniqueId) {
                    return false;
                }
            }
        }
        return true;
    }

    // 执行移动动作，包括清空旧位置和设置新位置
    private boolean performMove(int oldRow, int oldCol,
                                int newRow, int newCol,
                                int width, int height,
                                int uniqueId) {
        BackupData backup = backupArea(oldRow, oldCol, width, height);
        if (!clearOldPosition(oldRow, oldCol, width, height, uniqueId)) {
            restoreBackup(oldRow, oldCol, backup);
            return false;
        }
        if (!setNewPosition(newRow, newCol, width, height, backup.idBackup[0][0], uniqueId)) {
            restoreBackup(oldRow, oldCol, backup);
            return false;
        }
        return true;
    }

    private BackupData backupArea(int row, int col, int width, int height) {
        int[][] ids = new int[height][width];
        int[][] uids = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int r = row + i;
                int c = col + j;
                ids[i][j] = model.getId(r, c);
                uids[i][j] = model.getUniqueId(r, c);
            }
        }
        return new BackupData(ids, uids);
    }

    private static class BackupData {
        final int[][] idBackup;
        final int[][] uniqueIdBackup;

        BackupData(int[][] ids, int[][] uids) {
            this.idBackup = ids;
            this.uniqueIdBackup = uids;
        }
    }

    private boolean clearOldPosition(int row, int col,
                                     int width, int height,
                                     int uniqueId) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int r = row + i;
                int c = col + j;
                if (model.getUniqueId(r, c) == uniqueId) {
                    model.setIdSafely(r, c, 0);
                    model.setUniqueId(r, c, 0);
                }
            }
        }
        return true;
    }

    private boolean setNewPosition(int row, int col,
                                   int width, int height,
                                   int id, int uniqueId) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int r = row + i;
                int c = col + j;
                model.setIdSafely(r, c, id);
                model.setUniqueId(r, c, uniqueId);
            }
        }
        return true;
    }

    private void restoreBackup(int row, int col, BackupData bak) {
        for (int i = 0; i < bak.idBackup.length; i++) {
            for (int j = 0; j < bak.idBackup[0].length; j++) {
                int r = row + i;
                int c = col + j;
                model.setId(r, c, bak.idBackup[i][j]);
                model.setUniqueId(r, c, bak.uniqueIdBackup[i][j]);
            }
        }
    }

    private int getCurrentWidth(int id) {
        return (id == 2 || id == 7) ? 2 : 1;
    }

    private int getCurrentHeight(int id) {
        return (id >= 3 && id <= 7) ? 2 : 1;
    }
}
