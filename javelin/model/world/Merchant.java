package javelin.model.world;

import java.util.ArrayList;
import java.util.Collections;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.artifact.Artifact;
import javelin.model.world.place.town.Town;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.MerchantScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * A figure that travels from one city to a human (neutral) city. It can be
 * visited to buy {@link Item}s. If it reaches a human {@link Town} it grows by
 * 1 {@link Town#size} - this is an incentive for the player to protect
 * merchants.
 * 
 * Unlike {@link Town} {@link Item}s, these are not crafted but sold as-is, and
 * as such are removed after purchase.
 * 
 * @author alex
 */
public class Merchant extends WorldActor {
	static final int NUMBEROFITEMS = 6;
	static final int MINARTIFACTS = 1;
	static final int MAXARTIFACTS = 3;

	private final int tox;
	private final int toy;
	/** Selection of {@link Item}s available for purchase. */
	public ItemSelection inventory = new ItemSelection();
	/** Merchants are slow, act once very other turn. */
	private boolean ignoreturn = true;

	/** Creates a merchant in the world map but doesn't {@link #place()} it. */
	public Merchant() {
		visualname = "merchant";
		ArrayList<WorldActor> towns = WorldActor.getall(Town.class);
		Collections.shuffle(towns);
		WorldActor from = towns.get(0);
		x = from.x;
		y = from.y;
		displace();
		WorldActor to = null;
		for (int i = 1; i < towns.size(); i++) {
			Town t = (Town) towns.get(i);
			if (t.garrison.isEmpty()) {
				to = t;
				break;
			}
		}
		if (to == null) {
			tox = RPG.r(0, World.MAPDIMENSION - 1);
			toy = RPG.r(0, World.MAPDIMENSION - 1);
		} else {
			tox = to.x;
			toy = to.y;
		}
		while (inventory.size() < NUMBEROFITEMS) {
			Item i = RPG.pick(Item.ALL);
			if (!(i instanceof Artifact)) {
				inventory.add(i);
			}
		}
		int withartifacts =
				inventory.size() + RPG.r(MINARTIFACTS, MAXARTIFACTS);
		while (inventory.size() < withartifacts) {
			Item i = RPG.pick(Item.ARTIFACT);
			inventory.add(i);
		}
	}

	@Override
	public void turn(long time, WorldScreen world) {
		if (ignoreturn) {
			ignoreturn = false;
			return;
		}
		ignoreturn = true;
		int x = this.x + calculatedelta(this.x, tox);
		int y = this.y + calculatedelta(this.y, toy);
		WorldActor here = WorldScreen.getactor(x, y);
		this.x = x;
		this.y = y;
		visual.remove();
		place();
		if (x == tox && y == toy) {
			if (here instanceof Town) {
				Town town = (Town) here;
				if (town.garrison.isEmpty()) {
					town.size += 1;
					Game.messagepanel.clear();
					Game.message(
							"A merchant arrives at " + town
									+ ", city grows! Press ENTER to continue...",
							null, Delay.NONE);
					while (Game.getInput().getKeyChar() != '\n') {
						// wait for ENTER
					}
					Game.messagepanel.clear();
				}
			}
			remove();
		} else if (here != null) {
			turn(0, null);// jump over other Actors
		}
	}

	int calculatedelta(int from, int to) {
		if (to > from) {
			return +1;
		}
		if (to < from) {
			return -1;
		}
		return 0;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		return true;
	}

	@Override
	public boolean interact() {
		new MerchantScreen(this).show();
		return true;
	}
}
