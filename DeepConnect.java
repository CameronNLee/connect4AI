// (c) Scott Madera, Cameron Lee, (add your name here)

import java.util.ArrayList;

/// Sample AI module that picks the first legal move.
/**
 * This AI chooses the leftmost column. It is meant to illustrate the basics of how to
 * use the AIModule and GameStateModule classes.
 *
 * Since this terminates in under a millisecond, there is no reason to check for
 * the terminate flag.  However, your AI needs to check for the terminate flag.
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
        Node tree = new Node(game);
        buildTree(tree, 6);

        player = game.getActivePlayer();
        if (player == 1) {
            enemy = 2;
        }
        else {
            enemy = 1;
        }

        for(int i = 0; i < game.getWidth(); i++) {
            if(tree.getState().canMakeMove(i)) {
                chosenMove = i;
                break;
            }
        }
        chosenMove = minimaxValue(tree);
    }

    /**
     * Recurisvely build the game tree down to the specified depth.
     *
     * @param tree The current board state when this AI's getNextMove() is called
     * @param levels The depth
     * @return returns the passed in node
     */

    // Should this be void? buildTree returns a node, but it doesn't
    // assign anything to a Node variable in getNextMove().
    public Node buildTree(Node tree, int levels) {
        if (levels == 0) {
            return tree;
        }
        GameStateModule stateCopy;
        // NOTE: won't always be 7 children made per node; need to have a break somewhere
        // for special cases like when a child node happens to have a game over board state;
        // in that case, that node should have no children, as it is a leaf node.
        for (int i = 0; i < tree.getState().getWidth(); i++) {
            stateCopy = tree.getState().copy();
            try {
                stateCopy.makeMove(i);
                Node newChild = new Node(stateCopy);
                tree.addChild(newChild);
            }
            catch (Exception e) {
                //Do something fun
                ;
            }
        }
        for (int i = 0; i < tree.getChildren().size(); i++) {
            tree.getChildren().set(i, buildTree(tree.getChildren().get(i), levels-1));
        }
        return tree;
    }

    // assumption: max player is always the first player
    // NOTE: perhaps not if player1 is human while player2 is AI
    // maybe have a check somehow if player1 is an AI player or not.
    public int minimaxValue(Node treeNode) {
        Node child;
        int value = Integer.MIN_VALUE;
        int finalMove = 0;
        // cycle through every child of current board state
        // and run getMinValue on all of them, then finally
        // taking the Max value of all the min values.
        for (int colIndex = 0; colIndex < treeNode.getChildren().size(); ++colIndex) {
            child = treeNode.getChildren().get(colIndex);
            int tempValue = Math.max(value, getMinValue(child));
            if (value < tempValue) {
                finalMove = colIndex; // where to ultimately drop the coin
                value = tempValue;
            }
        }
        return finalMove; // placeholder
    }

    public int getMaxValue(Node currentNode) {
        // terminal state check
        if (currentNode.isLeafNode()) {
            return calculatePayoff(currentNode);
        }
        int utilityValue = Integer.MIN_VALUE;
        Node child;
        for (int i = 0; i < currentNode.getChildren().size(); ++i) {
            child = currentNode.getChildren().get(i);
            utilityValue = Math.max(utilityValue, getMinValue(child));
        }
        return utilityValue;
    }
    public int getMinValue(Node currentNode) {
        if (currentNode.isLeafNode()) {
            return calculatePayoff(currentNode);
        }
        int utilityValue = Integer.MAX_VALUE;
        Node child;
        for (int i = 0; i < currentNode.getChildren().size(); ++i) {
            child = currentNode.getChildren().get(i);
            utilityValue = Math.min(utilityValue, getMaxValue(child));
        }
        return utilityValue;
    }

    // based on a leaf node's board state, determine the payoff
    public int calculatePayoff(Node leaf) {

        // case 1: leaf contains a board state who's game is over.
        // So, determine who the winner is, and assign payoffs based on that.
        if (leaf.getState().isGameOver()) {
            if (leaf.getState().getWinner() == player) {
                return 1;
            }
            else if (leaf.getState().getWinner() == enemy){
                return -1; // enemy won, so discourage taking this path!
            }
            else {
                return 0; // draw
            }
        }

        // case 2: game isn't over yet. Predict what the best payoffs are
        // based on how many potential 4-in-a-rows could exist for player,
        // versus how many potential 4-in-a-rows could exist for enemy.

        // if leaf's board state has it so that there are more 4-in-a-rows for enemy
        // than there are 4-in-a-rows for player, then assign negative payoff.
        return determineIfWinningOrLosingState(leaf);

    }

    public int determineIfWinningOrLosingState(Node leaf) {
        int win = determineIfHorizontalWinningOrLosingState(leaf);
        if (win > 0) {
            return win;
        }
        win = determineIfVerticalWinningOrLosingState(leaf);
        if (win > 0) {
            return win;
        }
        win = determineIfDiagonalWinningOrLosingState(leaf);
        if (win > 0) {
            return win;
        }
        return 0;
    }

    // maybe convert this to check both Horizontal and Vertical winning
    // states (or the vertical determine) later and see if there is a speed boost at all.
    public int determineIfHorizontalWinningOrLosingState(Node leaf) {
        int playerStreak = 0;
        int enemyStreak = 0;
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
                    playerStreak = 0;
                    enemyStreak = 0;
                }
                if (playerStreak == 4) {
                    return player;
                }
                else if (enemyStreak == 4) {
                    return enemy;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }
        return 0;
    }

    public int determineIfVerticalWinningOrLosingState(Node leaf) {
        int playerStreak = 0;
        int enemyStreak = 0;
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
                    playerStreak = 0;
                    enemyStreak = 0;
                }
                if (playerStreak == 4) {
                    return player;
                }
                else if (enemyStreak == 4) {
                    return enemy;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }
        return 0;
    }

    public int determineIfDiagonalWinningOrLosingState(Node leaf) {
        int playerStreak = 0;
        int enemyStreak = 0;
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
                    playerStreak = 0;
                    enemyStreak = 0;
                }
                if (playerStreak == 4) {
                    return player;
                }
                else if (enemyStreak == 4) {
                    return enemy;
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
                    playerStreak = 0;
                    enemyStreak = 0;
                }
                if (playerStreak == 4) {
                    return player;
                }
                else if (enemyStreak == 4) {
                    return enemy;
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
                    playerStreak = 0;
                    enemyStreak = 0;
                }
                if (playerStreak == 4) {
                    return player;
                }
                else if (enemyStreak == 4) {
                    return enemy;
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
                    playerStreak = 0;
                    enemyStreak = 0;
                }
                if (playerStreak == 4) {
                    return player;
                }
                else if (enemyStreak == 4) {
                    return enemy;
                }
            }
            playerStreak = 0;
            enemyStreak = 0;
        }
        return 0;
    }
}

class Node {
    private Integer score;
    private GameStateModule state;
    private ArrayList<Node> children;

    Node() {
        score = 0;
        children = new ArrayList<Node>();
    }
    Node(final GameStateModule newState) {
        score = 0;
        state = newState;
        children = new ArrayList<Node>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }
    public Integer getScore() {
        return this.score;
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

    public void setScore(int scoreIn) {
        this.score = scoreIn;
    }
    public void setState(GameStateModule stateIn) {
        this.state = stateIn;
    }

}
