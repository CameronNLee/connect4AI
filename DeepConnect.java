// (c) Scott Madera, (add your name here)

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
 */
public class DeepConnect extends AIModule
{
	public void getNextMove(final GameStateModule game)
	{
		for(int i = 0; i < game.getWidth(); i++)
			if(game.canMakeMove(i))
			{
				chosenMove = i;
				break;
			}
	}
}

public class Node {
    private Int score;
    private GameStateModule state;
    private ArrayList<Node> children;

    public Bool isLeafNode() {
        if (children.length() == 0) {
            return true;
        }
        else {
            return false;
        }
    }
}