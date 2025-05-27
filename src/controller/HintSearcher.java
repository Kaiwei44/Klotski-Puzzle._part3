package controller;

import model.Direction;
import model.MapModel;
import java.util.*;

public class HintSearcher {
    // 定义移动可能性封装类
    private static class PossibleMove {
        final int row;
        final int col;
        final Direction direction;

        PossibleMove(int row, int col, Direction direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }
    }

    // 状态节点类
    private static class Node {
        final MapModel state;
        final Direction direction;
        final Node parent;
        final int steps;

        Node(MapModel s, Direction d, Node p, int steps) {
            this.state = deepCopyModel(s);
            this.direction = d;
            this.parent = p;
            this.steps = steps;
        }
    }

    public static Direction findNextMove(MapModel initialModel) {
        if (initialModel == null) throw new IllegalArgumentException("Model cannot be null");

        Queue<Node> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(new Node(initialModel, null, null, 0));
        visited.add(getStateHash(initialModel));

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (isWinningState(current.state)) {
                return getFirstMove(current);
            }

            for (PossibleMove move : findAllPossibleMoves(current.state)) {
                MapModel newModel = deepCopyModel(current.state);
                if (tryMove(newModel, move.row, move.col, move.direction)) {
                    String hash = getStateHash(newModel);
                    if (!visited.contains(hash)) {
                        visited.add(hash);
                        queue.add(new Node(newModel, move.direction, current, current.steps + 1));
                    }
                }
            }
        }
        return null;
    }

    // 核心方法：查找所有合法移动
    private static List<PossibleMove> findAllPossibleMoves(MapModel model) {
        List<PossibleMove> moves = new ArrayList<>();
        int[][] matrix = model.getMatrix();

        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[0].length; col++) {
                if (matrix[row][col] != 0) {
                    // 使用final局部变量解决lambda作用域问题
                    final int currentRow = row;
                    final int currentCol = col;
                    checkDirections(matrix, currentRow, currentCol).stream()
                            .map(dir -> new PossibleMove(currentRow, currentCol, dir))
                            .forEach(moves::add);
                }
            }
        }
        return moves;
    }

    // 方向验证逻辑
    private static List<Direction> checkDirections(int[][] matrix, int row, int col) {
        List<Direction> validDirections = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            int newRow = row + dir.getRow();
            int newCol = col + dir.getCol();

            if (isValidPosition(matrix, newRow, newCol) && matrix[newRow][newCol] == 0) {
                validDirections.add(dir);
            }
        }
        return validDirections;
    }

    private static boolean isValidPosition(int[][] matrix, int row, int col) {
        return row >= 0 && row < matrix.length &&
                col >= 0 && col < matrix[0].length;
    }

    private static boolean tryMove(MapModel model, int row, int col, Direction dir) {
        GameController tempController = new GameController(null, model);
        return tempController.simulateMove(row, col, dir);
    }

    private static Direction getFirstMove(Node node) {
        while (node.parent != null && node.parent.parent != null) {
            node = node.parent;
        }
        return node.direction;
    }

    private static String getStateHash(MapModel model) {
        return Arrays.deepToString(model.getMatrix());
    }

    private static MapModel deepCopyModel(MapModel original) {
        int[][] originalMatrix = original.getMatrix();
        int[][] newMatrix = new int[originalMatrix.length][];
        for (int i = 0; i < originalMatrix.length; i++) {
            newMatrix[i] = Arrays.copyOf(originalMatrix[i], originalMatrix[i].length);
        }
        return new MapModel(newMatrix);
    }

    private static boolean isWinningState(MapModel model) {
        int[][] m = model.getMatrix();
        return m[3][1] == 7 && m[3][2] == 7 &&
                m[4][1] == 7 && m[4][2] == 7;
    }
}