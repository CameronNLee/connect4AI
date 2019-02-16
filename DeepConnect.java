// (c) Scott Madera, Cameron Lee, (add your name here)

import java.util.ArrayList;

/// Minimax AI module that picks moves after looking 6 levels down the game tree.
/**
 * This AI chooses columns to drop coins into based on the minimax algorithm.
 * The AI, given a game state's board configuration, creates a game tree that
 * looks at most 6 levels down from that game state. It then uses minimax
 * to search through the game tree, and picks the column index with the highest
 * payoff after propagating payoffs up the tree recursively.
 *
 * @author Scott Madera
 * @author Cameron Lee
 * (add your name here)
 */
public class DeepConnect extends AIModule {
    private int player;
    private int enemy;

    DeepConnect() {
        player = 0;
        enemy = 0;
    }

    public void getNextMove(final GameStateModule game) {
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        Node root = new Node(game);
        buildTree(root, 6);
        player = game.getActivePlayer();
        if (player == 1) {
            enemy = 2;
        }
        else {
            enemy = 1;
        }
        chosenMove = minimaxValue(root, alpha, beta);
    }

    /**
     * Recurisvely build the game tree down to the specified depth.
     *
     * @param root The current board state when this AI's getNextMove() is called
     * @param levels The depth
     * @return returns the passed in node (base case condition)
     */
    public Node buildTree(Node root, int levels) {
        if (levels == 0) { // base case
            return root;
        }
        GameStateModule stateCopy;
        for (int col = 0; col < root.getState().getWidth(); col++) {
            stateCopy = root.getState().copy();
            if (stateCopy.isGameOver()) {
                break; // i.e. don't bother making children for this node
            }
            if (!stateCopy.canMakeMove(col)) {
                continue; // i.e. ignore making impossible children nodes
            }
            stateCopy.makeMove(col);
            Node newChild = new Node(col, stateCopy, root);
            root.addChild(newChild);
            if (levels == 1) { // leaf node
                newChild.setUtility(calculatePayoff(newChild));
            }
            buildTree(newChild, levels-1);
        }
        return root;
    }

    /**
     * Kickstarts the minimax algorithm by traveling through the tree,
     * using the passed in game state as the "root".
     *
     * @param treeNode The current board state when this AI's getNextMove() is called
     * @return The column index with the highest payoff value.
     */
    public int minimaxValue(Node treeNode, int alpha, int beta) {
        int value = Integer.MIN_VALUE;
        int finalMove = 0;
        // cycle through every child of current board state
        // and run getMinValue on all of them, then finally
        // taking the Max value of all the min values.
        for (Node child : treeNode.getChildren()) {
            int tempValue = Math.max(value, getMinValue(child, alpha, beta));
            if (tempValue > value) {
                finalMove = child.getCol(); // where to ultimately drop the coin
                value = tempValue;
            }
        }
        return finalMove;
    }

    public int getMaxValue(Node currentNode, int alpha, int beta) {
        // terminal state check
        if (currentNode.isLeafNode()) {
            return calculatePayoff(currentNode);
        }
        int utilityValue = Integer.MIN_VALUE;
        Node child;
        for (int i = 0; i < currentNode.getChildren().size(); ++i) {
            child = currentNode.getChildren().get(i);
            utilityValue = Math.max(utilityValue, getMinValue(child, alpha, beta));
            if (utilityValue >= beta) {
                return utilityValue;
            }
            alpha = Math.max(alpha, utilityValue);
        }
        return utilityValue;
    }

    public int getMinValue(Node currentNode, int alpha, int beta) {
        if (currentNode.isLeafNode()) {
            return calculatePayoff(currentNode);
        }
        int utilityValue = Integer.MAX_VALUE;
        Node child;
        for (int i = 0; i < currentNode.getChildren().size(); ++i) {
            child = currentNode.getChildren().get(i);
            utilityValue = Math.min(utilityValue, getMaxValue(child, alpha, beta));
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
                score += 10;
            }
            else if (leaf.getState().getWinner() == enemy){
                score = -10; // enemy won, so discourage taking this path!
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
        streakBalance += determineHorizontalStreaks(leaf, 4);
        streakBalance += determineVerticalStreaks(leaf, 4);
        streakBalance += determineDiagonalStreaks(leaf, 4);
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
    private Node parent;
    private Integer alpha;
    private Integer beta;
    private Integer utility;

    Node() {
        col = -1;
        children = new ArrayList<Node>();
    }
    Node(final GameStateModule newState) {
        state = newState;
        children = new ArrayList<Node>();
    }
    Node(Integer column, final GameStateModule newState, Node newParent) {
        col = column;
        state = newState;
        parent = newParent;
        children = new ArrayList<Node>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public Integer getAlpha() {
        return alpha;
    }
    public Integer getBeta() {
        return beta;
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
    public Integer getUtility() {
        return utility;
    }

    public Boolean isLeafNode() {
        if (children.size() == 0) {
            return true;
        }
        else {
            return false;
        }
    }
    public void setAlpha(Integer alphaIn) {
        alpha = alphaIn;
    }
    public void setBeta(Integer betaIn) {
        beta = betaIn;
    }
    public void setUtility(Integer utilityIn) {
        utility = utilityIn;
    }
    public void setState(GameStateModule stateIn) {
        this.state = stateIn;
    }

}
