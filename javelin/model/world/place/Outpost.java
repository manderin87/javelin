package javelin.model.world.place;

import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.view.screen.WorldScreen;

/**
 * An outpost allows for vision of a wide area around it. 10 outputs are created
 * per game.
 * 
 * @see World#makemap()
 * @author alex
 */
public class Outpost extends WorldPlace {

	public static final int VISIONRANGE = 3;

	public Outpost() {
		super("Outpost");
		gossip = true;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		return true;
	}

	public static void build() {
		WorldActor o = new Outpost();
		o.place();
	}

	@Override
	public boolean interact() {
		discover(this.x, this.y, VISIONRANGE);
		return super.interact();
	}

	/**
	 * Given a coordinate shows a big amount of land around that.
	 * 
	 * @param range
	 *            How far squares away will become visible.
	 * @see WorldScreen#discovered
	 */
	static public void discover(int xp, int yp, int range) {
		for (int x = xp - range; x <= xp + range; x++) {
			for (int y = yp - range; y <= yp + range; y++) {
				WorldScreen.setVisible(x, y);
			}
		}
	}

	@Override
	protected Integer getel(int attackerel) {
		return Integer.MIN_VALUE;
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || iscloseto(Outpost.class)) {
			generateawayfromtown();
		}
	}
}
