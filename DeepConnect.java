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
        buildTree(tree, 7);

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

    public Node buildTree(Node tree, int levels) {
        if (levels == 0) {
            return tree;
        }
        GameStateModule stateCopy;
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
        /* do stuff here */
        return 0; // placeholder
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
