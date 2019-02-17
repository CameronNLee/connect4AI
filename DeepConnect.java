// (c) Scott Madera, Cameron Lee, Marshall Fan

import java.util.ArrayList;

/// Minimax+AlphaBeta-Pruning AI module that picks moves after looking 10 levels down the game tree.
/**
 * This AI chooses columns to drop coins into based on the minimax algorithm.
 * The AI, given a game state's board configuration, creates a game tree that
 * looks at most 6 levels down from that game state. It then uses minimax
 * to search through the game tree, and picks the column index with the highest
 * payoff after propagating payoffs up the tree recursively.
 *
 * @author Scott Madera
 * @author Cameron Lee
 * @author Marshall Fan
 */
public class DeepConnect extends AIModule {
    private int player;
    private int enemy;

    DeepConnect() {
        player = 0;
        enemy = 0;
    }

    public void getNextMove(final GameStateModule game) {
        Node root = new Node(game);
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        player = game.getActivePlayer();
        if (player == 1) {
            enemy = 2;
        }
        else {
            enemy = 1;
        }
        chosenMove = alphaBeta(root, 9, alpha, beta);
    }

    /**
     * Kickstarts the minimax algorithm by traveling through the tree,
     * using the passed in game state as the "root".
     *
     * @param treeNode The current board state when this AI's getNextMove() is called
     * @return The column index with the highest payoff value.
     */
    public int alphaBeta(Node treeNode, int depth, int alpha, int beta) {
        int value = Integer.MIN_VALUE;
        int tempValue = Integer.MIN_VALUE;
        int finalMove = -1;
        GameStateModule stateCopy;
        Node newChild;
        ArrayList<Integer> orderedCols = buildOrderedList();
        for (int col : orderedCols) {
            stateCopy = treeNode.getState().copy();
            if (!stateCopy.canMakeMove(col)) {
                continue; // i.e. ignore making impossible children nodes
            }
            stateCopy.makeMove(col);
            newChild = new Node(col, stateCopy);
            tempValue = Math.max(tempValue, getMinValue(newChild, depth-1, alpha, beta));
            if (tempValue >= beta) {
                return tempValue;
            }
            alpha = Math.max(alpha, tempValue);
            if (tempValue > value) {
                finalMove = newChild.getCol(); // where to ultimately drop the coin
                value = tempValue;
            }
        } // end of for
        return finalMove;
    }

    public int getMaxValue(Node currentNode, int depth, int alpha, int beta) {
        // terminal state check
        if ((currentNode.getState().isGameOver()) || (depth == 0)) {
            return calculatePayoff(currentNode);
        }
        int utilityValue = Integer.MIN_VALUE;
        GameStateModule stateCopy;
        ArrayList<Integer> orderedCols = buildOrderedList();
        for (int col : orderedCols) {
            stateCopy = currentNode.getState().copy();
            if (!stateCopy.canMakeMove(col)) {
                continue; // i.e. ignore making impossible children nodes
            }
            stateCopy.makeMove(col);
            Node newChild = new Node(col, stateCopy);
            utilityValue = Math.max(utilityValue, getMinValue(newChild, depth-1, alpha, beta));
            if (utilityValue >= beta) {
                return utilityValue;
            }
            alpha = Math.max(alpha, utilityValue);
        }
        return utilityValue;
    }
    public int getMinValue(Node currentNode, int depth, int alpha, int beta) {
        if ((currentNode.getState().isGameOver()) || (depth == 0)) {
            return calculatePayoff(currentNode);
        }
        int utilityValue = Integer.MAX_VALUE;
        GameStateModule stateCopy;
        ArrayList<Integer> orderedCols = buildOrderedList();
        for (int col : orderedCols) {
            stateCopy = currentNode.getState().copy();
            if (!stateCopy.canMakeMove(col)) {
                continue;
            }
            stateCopy.makeMove(col);
            Node newChild = new Node(col, stateCopy);
            utilityValue = Math.min(utilityValue, getMaxValue(newChild, depth-1, alpha, beta));
            if (utilityValue <= alpha) {
                return utilityValue;
            }
            beta = Math.min(beta, utilityValue);
        }
        return utilityValue;
    }

    /**
     * Based on a leaf node's board state, determine the payoff.
     *
     * @param leaf A leaf of the created tree, i.e. a node with a game over board
     *             state, or a node who has reached the final depth level.
     * @return The calculated payoff or utility value associated with the leaf.
     */
    public int calculatePayoff(Node leaf) {
        int score = 0;
        // case 1: leaf contains a board state who's game is over.
        // So, determine who the winner is, and assign payoffs based on that.
        if (leaf.getState().isGameOver()) {
            if (leaf.getState().getWinner() == player) {
                score = 10000;
            }
            else if (leaf.getState().getWinner() == enemy){
                score = -10000; // enemy won, so discourage taking this path!
            }
        }
        // case 2: game isn't over yet. Call the evaluation function.
        else {
            score += determineStreaks(leaf);
        }
        return score;
    }

    /**
     * Evaluation function that determines payoffs for non-GameOver leaf nodes.
     * It analyzes leaf's board state and determines how many possible 4-in-a-rows
     * (called "streaks" in our functions) both player and enemy can make based on
     * that given board state. It does this by separately calculating number of
     * horizontal streaks, number of vertical streaks, and number of diagonal streaks.
     *
     * @param leaf A leaf of the created tree, i.e. a node with a game over board
     *             state, or a node who has reached the final depth level.
     * @return The payoff generated by the difference between streak totals.
     */
    public int determineStreaks(Node leaf) {
        int streakBalance = 0;
        streakBalance += determineHorizontalStreaks(leaf, 3);
        streakBalance += determineVerticalStreaks(leaf, 3);
        streakBalance += determineDiagonalStreaks(leaf, 3);
        return streakBalance;
    }

    public int determineHorizontalStreaks(Node leaf, int totalStreak) {
        int playerStreak = 0;
        int enemyStreak = 0;
        int totalPlayerStreaks = 0;
        int totalEnemyStreaks = 0;
        int occupies = 0;

        for (int row = 0; row < leaf.getState().getHeight(); row++) {
            for (int col = 0; col < leaf.getState().getWidth(); col++) {
                occupies = leaf.getState().getAt(col,row);
                if (occupies == player) {
                    playerStreak += 1;
                    enemyStreak = 0;
                }
                else if (occupies == enemy) {
                    enemyStreak += 1;
                    playerStreak = 0;
                }
                else {
                    if (playerStreak > 0) {
                        playerStreak += 1;
                    }
                    else if (enemyStreak > 0) {
                        enemyStreak += 1;
                    }
                }
                if (playerStreak >= totalStreak) {
                    totalPlayerStreaks += 1;
                    playerStreak = 0;
                }
                if (enemyStreak >= totalStreak) {
                    totalEnemyStreaks += 1;
                    enemyStreak = 0;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }
        return (totalPlayerStreaks - totalEnemyStreaks);
    }

    public int determineVerticalStreaks(Node leaf, int totalStreak) {
        int playerStreak = 0;
        int enemyStreak = 0;
        int totalPlayerStreaks = 0;
        int totalEnemyStreaks = 0;
        int occupies = 0;

        for (int col = 0; col < leaf.getState().getWidth(); col++) {
            for (int row = 0; row < leaf.getState().getHeight(); row++) {
                occupies = leaf.getState().getAt(col,row);
                if (occupies == player) {
                    playerStreak += 1;
                    enemyStreak = 0;
                }
                else if (occupies == enemy) {
                    enemyStreak += 1;
                    playerStreak = 0;
                }
                else {
                    if (playerStreak > 0) {
                        playerStreak += 1;
                    }
                    else if (enemyStreak > 0) {
                        enemyStreak += 1;
                    }
                }
                if (playerStreak == totalStreak) {
                    totalPlayerStreaks += 1;
                    playerStreak = 0;
                }
                if (enemyStreak == totalStreak) {
                    totalEnemyStreaks += 1;
                    enemyStreak = 0;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }
        return (totalPlayerStreaks - totalEnemyStreaks);
    }

    public int determineDiagonalStreaks(Node leaf, int totalStreak) {
        int playerStreak = 0;
        int enemyStreak = 0;
        int totalPlayerStreaks = 0;
        int totalEnemyStreaks = 0;
        int occupies = 0;
        int maxRow = leaf.getState().getHeight();
        int maxCol = leaf.getState().getWidth();

        // left to right

        // top left to bottom right, moving down the rows
        for (int rowBegin = maxRow-1; rowBegin > 2; rowBegin--) {
            for (int row = rowBegin, col = 0; row >= 0 && col < maxCol; row--, col++) {
                occupies = leaf.getState().getAt(col,row);
                if (occupies == player) {
                    playerStreak += 1;
                    enemyStreak = 0;
                }
                else if (occupies == enemy) {
                    enemyStreak += 1;
                    playerStreak = 0;
                }
                else {
                    ++playerStreak;
                    ++enemyStreak;
                }
                if (playerStreak == totalStreak) {
                    totalPlayerStreaks += 1;
                }
                if (enemyStreak == totalStreak) {
                    totalEnemyStreaks += 1;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }

        // top-left to bottom-right, moving up the columns
        for (int colBegin = 1; colBegin < maxCol-3; colBegin++) {
            for (int row = maxRow-1, col = colBegin; row >= 0 && col < maxCol; row--, col++) {
                occupies = leaf.getState().getAt(col,row);
                if (occupies == player) {
                    playerStreak += 1;
                    enemyStreak = 0;
                }
                else if (occupies == enemy) {
                    enemyStreak += 1;
                    playerStreak = 0;
                }
                else {
                    ++playerStreak;
                    ++enemyStreak;
                }
                if (playerStreak == totalStreak) {
                    totalPlayerStreaks += 1;
                }
                if (enemyStreak == totalStreak) {
                    totalEnemyStreaks += 1;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }

        // right to left

        // top-right to bottom-left, moving down the columns
        for (int colBegin = maxCol-2; colBegin >= 3; colBegin--) {
            for (int row = maxRow-1, col = colBegin; row >= 0 && col >= 0; row--, col--) {
                occupies = leaf.getState().getAt(col,row);
                if (occupies == player) {
                    playerStreak += 1;
                    enemyStreak = 0;
                }
                else if (occupies == enemy) {
                    enemyStreak += 1;
                    playerStreak = 0;
                }
                else {
                    ++playerStreak;
                    ++enemyStreak;
                }
                if (playerStreak == totalStreak) {
                    totalPlayerStreaks += 1;
                }
                if (enemyStreak == totalStreak) {
                    totalEnemyStreaks += 1;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }

        // top-right to bottom-left, moving down the rows
        for (int rowBegin = maxRow-1; rowBegin >= 2; rowBegin--) {
            for (int row = rowBegin, col = maxCol-1; row >= 0 && col >= 0; row--, col--) {
                occupies = leaf.getState().getAt(col,row);
                if (occupies == player) {
                    playerStreak += 1;
                    enemyStreak = 0;
                }
                else if (occupies == enemy) {
                    enemyStreak += 1;
                    playerStreak = 0;
                }
                else {
                    ++playerStreak;
                    ++enemyStreak;
                }
                if (playerStreak == totalStreak) {
                    totalPlayerStreaks += 1;
                }
                if (enemyStreak == totalStreak) {
                    totalEnemyStreaks += 1;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }
        return (totalPlayerStreaks - totalEnemyStreaks);
    }

    /// pre-determined move ordering for alpha-beta pruning
    public ArrayList<Integer> buildOrderedList() {
        ArrayList<Integer> orderedCols = new ArrayList<Integer>();
        orderedCols.add(3);
        orderedCols.add(2);
        orderedCols.add(4);
        orderedCols.add(1);
        orderedCols.add(5);
        orderedCols.add(0);
        orderedCols.add(6);
        return orderedCols;
    }
}
/// A class that associates game states to nodes in a game tree.
/**
 * Node associates a created board state configuration to its
 * GameStateModule object member. This is to facilitate creation of the
 * game tree, which is essential for our implementation of minimax.
 *
 */
class Node {
    private Integer col;
    private GameStateModule state;
    private ArrayList<Node> children;

    Node() {
        col = -1;
        children = new ArrayList<Node>();
    }
    Node(final GameStateModule newState) {
        state = newState;
        children = new ArrayList<Node>();
    }
    Node(Integer column, final GameStateModule newState) {
        col = column;
        state = newState;
        children = new ArrayList<Node>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }
    public Integer getCol() {
        return col;
    }
    public GameStateModule getState() {
        return state;
    }

    public Boolean isLeafNode() {
        if (children.size() == 0) {
            return true;
        }
        else {
            return false;
        }
    }
    public void setState(GameStateModule stateIn) {
        this.state = stateIn;
    }

}
