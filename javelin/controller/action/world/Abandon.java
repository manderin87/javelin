package javelin.controller.action.world;

import javelin.controller.db.StateManager;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Quit the current game forever, delete the save game so the player can start a
 * new campaign.
 * 
 * @author alex
 */
public class Abandon extends WorldAction {
	public Abandon() {
		super("Start a completely new game (clears saved data and closes game)",
				new int[] {}, new String[] { "Q" });
	}

	@Override
	public void perform(final WorldScreen screen) {
		screen.messagepanel.clear();
		Game.message(
				"Are you sure you want to abandon the current game forever? Press c to confirm.",
				null, Delay.NONE);
		if (InfoScreen.feedback() == 'c') {
			StateManager.abandoned = true;
			StateManager.save();
			System.exit(0);
		}
	}
}
