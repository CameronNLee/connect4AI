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
    public void getNextMove(final GameStateModule game) {
        Node tree = new Node(game);
        buildTree(tree, 7);

        for(int i = 0; i < game.getWidth(); i++) {
            if(tree.getState().canMakeMove(i)) {
                chosenMove = i;
                break;
            }
        }// minimaxValue(tree);
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
        int value = 0;
        // cycle through every child of current node
        // and run getMaxValue on all of them
        for (int i = 0; i < treeNode.getChildren().size(); ++i) {
            child = treeNode.getChildren().get(i);
            value = getMaxValue(child);
        }
        return value; // placeholder
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

    // based on state of leaf node, determine the payoff
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
