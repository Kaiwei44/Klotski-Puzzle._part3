package controller;

import model.Direction;
import model.MapModel;

import java.util.*;

/**
 * HintSearcher：为当前的 MapModel 做一次 BFS，找到“最短解法路径”，
 * 并返回从根状态到成功状态的第 1 步。若深度／状态数超限或无解，则返回 direction=NONE。
 */
public class HintSearcher {
    private static final int MAX_DEPTH = 150;      // 最大搜索深度
    private static final int MAX_STATES = 1000000;  // 最大状态数上限
    private static final int CAO_CAO_ID = 7;      // 曹操块的 id
    private static final int TARGET_ROW = 3;      // 胜利时曹操左上角 Row
    private static final int TARGET_COL = 1;      // 胜利时曹操左上角 Col

    private final GameController controller;

    /** BFS 过程中用来记录一条路径节点的类 **/
    private static class Node {
        final MapModel model;
        final Node parent;
        final Direction moveDirection; // 从 parent 到这个节点是哪个方向的移动
        final int boxRow;              // 该步移动的块的 row
        final int boxCol;              // 该步移动的块的 col
        final int depth;               // 当前深度

        Node(MapModel model, Node parent,
             Direction moveDirection,
             int boxRow, int boxCol, int depth) {
            this.model = model;
            this.parent = parent;
            this.moveDirection = moveDirection;
            this.boxRow = boxRow;
            this.boxCol = boxCol;
            this.depth = depth;
        }
    }

    public HintSearcher(GameController controller) {
        this.controller = controller;
    }

    /**
     * 查找下一步移动：如果找到了从初始状态到胜利状态的最短路径，就返回第1步的 HintResult；
     * 否则 direction=NONE。如果队列耗尽或达到 MAX_STATES，都返回 direction=NONE，并标记 isUndo=true。
     */
    public HintResult findNextMove() {
        System.out.println("【提示搜索】开始查找下一步移动...");
        MapModel initialModel = controller.getModel();
        if (initialModel == null) {
            System.out.println("【错误】初始模型为空，无法提供提示");
            return new HintResult(Direction.NONE, -1, -1);
        }

        HintResult result = bfsSearch(initialModel);
        if (result.direction == Direction.NONE) {
            System.out.println("【提示搜索】未找到有效解决方案");
        } else {
            System.out.printf("【提示搜索】建议移动：在(%d,%d)向%s方向移动%n",
                    result.boxRow, result.boxCol, result.direction);
        }
        return result;
    }

    /**
     * 执行实际的 BFS。返回一个 HintResult，要么是第一步方向，要么 direction=NONE。
     */
    private HintResult bfsSearch(MapModel initialModel) {
        System.out.println("【BFS搜索】启动广度优先搜索");
        System.out.printf("【状态限制】最大深度=%d, 最大状态数=%d%n", MAX_DEPTH, MAX_STATES);

        Queue<Node> queue = new LinkedList<>();
        Set<StateWrapper> visited = new HashSet<>();

        // 把初始状态加入队列
        MapModel rootCopy = createModelCopy(initialModel);
        queue.offer(new Node(rootCopy, null, Direction.NONE, -1, -1, 0));
        visited.add(new StateWrapper(rootCopy));
        System.out.println("【BFS搜索】初始状态已加入队列");

        int statesProcessed = 0;
        int lastReported = 0;

        while (!queue.isEmpty() && statesProcessed < MAX_STATES) {
            Node current = queue.poll();
            statesProcessed++;

            // 定期输出进度
            if (statesProcessed - lastReported >= 1000) {
                System.out.printf("【搜索进度】已处理状态：%d, 队列大小：%d, 当前深度：%d%n",
                        statesProcessed, queue.size(), current.depth);
                lastReported = statesProcessed;
            }

            // 胜利检测：检查曹操块是否到达 (3,1)-(4,2) 区域
            if (isWinningState(current.model)) {
                System.out.printf("【胜利状态】在深度 %d 找到解决方案 (已处理状态: %d)%n",
                        current.depth, statesProcessed);
                return extractFirstMove(current);
            }

            // 深度限制
            if (current.depth >= MAX_DEPTH) {
                if (current.depth == MAX_DEPTH) {
                    System.out.println("【深度警告】达到最大深度限制，停止探索更深节点");
                }
                continue;
            }

            // 生成所有可行的下一步 (只对每个块的“左上角”坐标尝试移动)
            List<PossibleMove> moves = generatePossibleMoves(current.model);
            if (moves.isEmpty()) {
                System.out.printf("【状态警告】深度 %d 无有效移动 (位置: %d,%d)%n",
                        current.depth, current.boxRow, current.boxCol);
            }

            for (PossibleMove move : moves) {
                // 复制一份新的 MapModel
                MapModel newModel = createModelCopy(current.model);

                // 应用移动并检查返回值
                if (applyMove(newModel, move.row, move.col, move.direction)) {
                    StateWrapper newState = new StateWrapper(newModel);
                    if (!visited.contains(newState)) {
                        visited.add(newState);
                        Node nextNode = new Node(
                                newModel,
                                current,
                                move.direction,
                                move.row,
                                move.col,
                                current.depth + 1
                        );
                        queue.offer(nextNode);

                        // 仅在前几步或前几状态时打印详细调试信息
                        if (statesProcessed < 10 || current.depth < 3) {
                            System.out.printf("【新状态】深度 %d | 移动 %s@(%d,%d) | 队列大小: %d%n",
                                    nextNode.depth, move.direction, move.row, move.col, queue.size());
                        }
                    }
                }
            }
        }

        // BFS 走到这里要么队列空，要么达到状态数上限
         if (queue.isEmpty()) {
             System.out.println("【搜索终止】队列已耗尽，未找到解决方案");
         } else {
             System.out.printf("【搜索终止】达到状态数上限 %d (队列剩余: %d)%n",
                     MAX_STATES, queue.size());
         }
         // 退而求其次：做一次“单步贪心启发”找一个近似解（尽量朝出口走）
        System.out.println("【退而求其次】BFS失败，尝试单步贪心启发");
         return greedyHint(initialModel);
    }

    /**
     * 从找到的胜利节点回溯到起点，提取“第一步”移动信息
     */
    private HintResult extractFirstMove(Node solutionNode) {
        System.out.println("【回溯路径】开始提取第一步移动...");
        if (solutionNode == null) {
            return new HintResult(Direction.NONE, -1, -1);
        }

        Node curr = solutionNode;
        // 一直往上追溯，直到 parent.parent == null，此时 curr 就是第一步
        while (curr.parent != null && curr.parent.parent != null) {
            curr = curr.parent;
        }

        System.out.printf("【回溯结果】第一步移动：%s@(%d,%d)%n",
                curr.moveDirection, curr.boxRow, curr.boxCol);
        return new HintResult(curr.moveDirection, curr.boxRow, curr.boxCol);
    }

    /**
     * 胜利判断：只要曹操（id=7）覆盖 (3,1),(3,2),(4,1),(4,2) 四个单元，就算胜利
     */
    private boolean isWinningState(MapModel model) {
        boolean win = model.getId(TARGET_ROW, TARGET_COL) == CAO_CAO_ID &&
                model.getId(TARGET_ROW, TARGET_COL + 1) == CAO_CAO_ID &&
                model.getId(TARGET_ROW + 1, TARGET_COL) == CAO_CAO_ID &&
                model.getId(TARGET_ROW + 1, TARGET_COL + 1) == CAO_CAO_ID;
        if (win) {
            System.out.println("【胜利检测】当前状态满足胜利条件！");
        }
        return win;
    }

    /**
     * 只对每个“块”的左上角那一格尝试 4 个方向的 canMove。返回所有合法的 PossibleMove。
     */
    private List<PossibleMove> generatePossibleMoves(MapModel model) {
        List<PossibleMove> moves = new ArrayList<>();
        int width = model.getWidth();
        int height = model.getHeight();
        Set<Integer> seenBlocks = new HashSet<>();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int uid = model.getUniqueId(r, c);
                if (uid == 0) continue;

                // 判断 (r,c) 是否该块的“左上角”：如果上面或左面有相同 uniqueId，则跳过
                if (r > 0 && model.getUniqueId(r - 1, c) == uid) continue;
                if (c > 0 && model.getUniqueId(r, c - 1) == uid) continue;

                // 如果之前已经处理过这个 uniqueId（同一块），也跳过
                if (seenBlocks.contains(uid)) continue;
                seenBlocks.add(uid);

                // 此时 (r,c) 就是这个块的“左上角”
                for (Direction dir : Direction.values()) {
                    if (dir == Direction.NONE) continue;
                    if (model.canMove(r, c, dir)) {
                        moves.add(new PossibleMove(r, c, dir));
                    }
                }
            }
        }
        return moves;
    }

    /**
     * 复制一个 MapModel，包括 matrix 和 uniqueIds，两者都要拷贝到新对象里。
     */
    private MapModel createModelCopy(MapModel original) {
        int w = original.getWidth();
        int h = original.getHeight();

        // 1. 先复制 matrix
        int[][] gridCopy = new int[h][w];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                gridCopy[r][c] = original.getId(r, c);
            }
        }

        // 2. 用复制的 matrix 构造一个新的 MapModel
        MapModel newModel = new MapModel(gridCopy);

        // 3. 再复制 uniqueIds
        int[][] uidsCopy = original.getUniqueIdsMatrix();
        newModel.setUniqueIds(uidsCopy);

        return newModel;
    }

    /**
     * 在 newModel 上执行一条移动：row,col 指向块的左上角，dir 是方向。
     * 直接调用 MapModel.move(...)，如果返回 true 就说明移动成功。
     */
    private boolean applyMove(MapModel model, int row, int col, Direction dir) {
        return model.move(row, col, dir);
    }

    /** 代表一次“可能的移动”：对 (row,col) 这个左上角尝试 dir 方向 */
    private static class PossibleMove {
        final int row, col;
        final Direction direction;

        PossibleMove(int row, int col, Direction direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }

        @Override
        public String toString() {
            return String.format("(%d,%d)->%s", row, col, direction);
        }
    }

    /**
     * HintResult：把 direction/boxRow/boxCol/是否需要 undo(isUndo) 打包返回
     */
    public static class HintResult {
        public final Direction direction;
        public final int boxRow;
        public final int boxCol;
        public final boolean isUndo;

        public HintResult(Direction direction, int boxRow, int boxCol) {
            this(direction, boxRow, boxCol, false);
        }

        public HintResult(Direction direction, int boxRow, int boxCol, boolean isUndo) {
            this.direction = direction;
            this.boxRow = boxRow;
            this.boxCol = boxCol;
            this.isUndo = isUndo;
        }

        @Override
        public String toString() {
            if (isUndo) return "Hint[UNDO]";
            return String.format("Hint[%s@(%d,%d)]", direction, boxRow, boxCol);
        }
    }

    /**
     * StateWrapper：把 MapModel 的 grid（id 矩阵）深度拷贝到一个 int[][]，
     * 用于 equals/hashCode 判断“状态是否重复”。
     */
    private static class StateWrapper {
        private final int[][] grid;
        private final int hashCode;

        StateWrapper(MapModel model) {
            int w = model.getWidth();
            int h = model.getHeight();
            this.grid = new int[h][w];
            int code = 17;
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    int id = model.getId(r, c);
                    grid[r][c] = id;
                    code = 31 * code + id;
                }
            }
            this.hashCode = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StateWrapper)) return false;
            StateWrapper that = (StateWrapper) o;
            return Arrays.deepEquals(this.grid, that.grid);
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        @Override
        public String toString() {
            return "StateHash:" + hashCode;
        }
    }

    /**
     * 如果 BFS 终止了，就退而求其次，用“单步贪心”来给一个提示：
     * 列举当前布局里所有 PossibleMove，模拟每一步，
     * 计算曹操块在模拟后到目标 (TARGET_ROW, TARGET_COL) 的曼哈顿距离，
     * 返回距离最小的一步作为 HintResult。
     */
    private HintResult greedyHint(MapModel currentModel) {
        // 1. 先找出曹操块（id=7）的左上角
        int caoCaoRow = -1, caoCaoCol = -1;
        for (int r = 0; r < currentModel.getHeight(); r++) {
            for (int c = 0; c < currentModel.getWidth(); c++) {
                if (currentModel.getId(r, c) == CAO_CAO_ID) {
                    // 检查是否完整的 2x2 曹操块左上角
                    if (r + 1 < currentModel.getHeight() &&
                            c + 1 < currentModel.getWidth() &&
                            currentModel.getId(r,   c   ) == CAO_CAO_ID &&
                            currentModel.getId(r,   c+1 ) == CAO_CAO_ID &&
                            currentModel.getId(r+1, c   ) == CAO_CAO_ID &&
                            currentModel.getId(r+1, c+1 ) == CAO_CAO_ID) {
                        caoCaoRow = r;
                        caoCaoCol = c;
                        break;
                    }
                }
            }
            if (caoCaoRow != -1) break;
        }
        if (caoCaoRow == -1) {
            // 如果真的没找到曹操块，就返回“无解”
            return new HintResult(Direction.NONE, -1, -1, true);
        }

        // 2. 枚举当前所有合法的 PossibleMove
        List<PossibleMove> allMoves = generatePossibleMoves(currentModel);
        if (allMoves.isEmpty()) {
            return new HintResult(Direction.NONE, -1, -1, true);
        }

        // 3. 对每个 PossibleMove，复制当前模型，做一次 applyMove(...)
        //    找到“模拟后”的曹操块左上角，计算曼哈顿距离
        int bestDist = Integer.MAX_VALUE;
        PossibleMove bestMove = null;
        for (PossibleMove mv : allMoves) {
            MapModel copy = createModelCopy(currentModel);
            boolean moved = applyMove(copy, mv.row, mv.col, mv.direction);
            if (!moved) continue;

            int newCaoR = -1, newCaoC = -1;
            outer:
            for (int r = 0; r < copy.getHeight(); r++) {
                for (int c = 0; c < copy.getWidth(); c++) {
                    if (copy.getId(r, c) == CAO_CAO_ID) {
                        if (r + 1 < copy.getHeight() &&
                                c + 1 < copy.getWidth() &&
                                copy.getId(r,   c   ) == CAO_CAO_ID &&
                                copy.getId(r,   c+1 ) == CAO_CAO_ID &&
                                copy.getId(r+1, c   ) == CAO_CAO_ID &&
                                copy.getId(r+1, c+1 ) == CAO_CAO_ID) {
                            newCaoR = r;
                            newCaoC = c;
                            break outer;
                        }
                    }
                }
            }
            if (newCaoR == -1) continue;

            int dist = Math.abs(newCaoR - TARGET_ROW) + Math.abs(newCaoC - TARGET_COL);
            if (dist < bestDist) {
                bestDist = dist;
                bestMove = mv;
            }
        }

        // 4. 返回“距离最小”的那一步
        if (bestMove != null) {
            return new HintResult(bestMove.direction, bestMove.row, bestMove.col);
        } else {
            return new HintResult(Direction.NONE, -1, -1, true);
        }
    }
}
