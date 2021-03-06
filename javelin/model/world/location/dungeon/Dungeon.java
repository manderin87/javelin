package javelin.model.world.location.dungeon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.old.Game;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.map.Maps;
import javelin.controller.terrain.map.tyrant.Caves;
import javelin.model.BattleMap;
import javelin.model.Realm;
import javelin.model.item.ItemSelection;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Caravan;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.crawler.HorizontalCorridor;
import javelin.model.world.location.dungeon.crawler.VerticalCorridor;
import javelin.model.world.location.dungeon.temple.TempleDungeon;
import javelin.model.world.location.dungeon.temple.features.Brazier;
import javelin.model.world.location.dungeon.temple.features.FruitTree;
import javelin.model.world.location.dungeon.temple.features.Portal;
import javelin.model.world.location.dungeon.temple.features.Spirit;
import javelin.model.world.location.dungeon.temple.features.StairsDown;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.haxor.Win;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Tile;

/**
 * A dungeon is an underground area of the world where the combats are harder
 * but have extra treasure laying around.
 * 
 * The in-game logic for dungeons is that they are a hideout of bandits or
 * similar, which is why they are sacrificeable by {@link Incursion}s and are
 * removed from the game after a {@link Squad} leaves one (in this case it's
 * assumed the bandits packed their stuff and left).
 * 
 * @author alex
 */
public class Dungeon extends Location {
	/** Screen dimensions. */
	public final static int SIZE = 7 + 1 + 7;
	final static float WALLRATIO = 1 / 4f;
	final static int WALKABLEAREA = Math.round(SIZE * SIZE * WALLRATIO);
	/**
	 * Assumes you will explore all the dungeon, being able to rest at the
	 * fountain mid-way for a total of 8 moderate encounters. This ignores the
	 * returning path towards the exit and getting to and from the dungeon
	 * itself, which means squads are supposed to be well equipped before diving
	 * in.
	 */
	public static final float ENCOUNTERRATIO = 8f / WALKABLEAREA;
	/** Approximate number of steps per encounter. */
	public static final int STEPSPERENCOUNTER = Math.round(1 / ENCOUNTERRATIO);

	static final int MAXTRIES = 1000;
	final static int[] DELTAS = { -1, 0, 1 };
	/**
	 * Number of starting dungeons in the {@link World} map. Since {@link Key}s
	 * are important to {@link Win}ning the game this should be a fair amount,
	 * otherwise the player will depend only on {@link Caravan}s if too many
	 * dungeons are destroyed or if unable to find the proper {@link Chest}
	 * inside the dungeons he does find. Not that dungeons also spawn during the
	 * course of a game but since this is highly randomized a late-game player
	 * who ran out of dungeons should not be required to depend on that alone.
	 * 
	 * @see WorldActor#destroy(Incursion)
	 * @see FeatureGenerator
	 */
	public static final Integer STARTING = Realm.values().length * 2;

	/** Current {@link Dungeon} or <code>null</code> if not in one. */
	public static Dungeon active = null;
	/** All of this dungeon's {@link Feature}s. */
	public List<Feature> features = new ArrayList<Feature>();
	/** Set of points that are occupied by walls. */
	public HashSet<Point> walls = new HashSet<Point>();
	/** Explored squares in this dungeon. */
	public boolean[][] visible;
	/**
	 * Current {@link Squad} location.
	 * 
	 * TODO is this needed?
	 */
	public Point herolocation;

	transient BattleMap map = null;
	/** TODO remove from 2.0+ */
	transient public Thing hero;
	transient boolean generated = false;
	/** File to use under 'avatar' folder. */
	public String floor = "dungeonfloor";
	/** File to use under 'avatar' folder. */
	public String wall = "dungeonwall";

	/** Constructor. */
	public Dungeon() {
		super("A dungeon");
		sacrificeable = true;
	}

	@Override
	public boolean interact() {
		super.interact();
		return true;
	}

	/** Create or recreate dungeon. */
	public void activate(boolean loading) {
		while (map == null) {
			map = new BattleMap(SIZE, SIZE);
			if (features.isEmpty()) {
				/* not loading a game */
				try {
					map();
				} catch (GaveUpException e) {
					map = null;
					features.clear();
					walls.clear();
				}
			}
		}
		regenerate(loading);
		Game.instance().setHero(hero);
		JavelinApp.context = new DungeonScreen(map);
		active = this;
		BattleScreen.active = JavelinApp.context;
		Squad.active.updateavatar();
	}

	void map() throws GaveUpException {
		final Set<Point> free = new HashSet<Point>();
		final Set<Point> used = new HashSet<Point>();
		for (int i = 0; i < SIZE; i++) {
			used.add(new Point(i, 0));
			used.add(new Point(i, SIZE - 1));
			used.add(new Point(0, i));
			used.add(new Point(SIZE - 1, i));
		}
		Point root = new Point(RPG.r(2, SIZE - 3), RPG.r(2, SIZE - 3));
		free.add(root);
		used.add(root);
		build(new StairsUp("stairs up", root), used);
		for (int x = root.x - 1; x <= root.x + 1; x++) {
			for (int y = root.y - 1; y <= root.y + 1; y++) {
				Point p = new Point(x, y);
				free.add(p);
				used.add(p);
			}
		}
		herolocation =
				new Point(root.x + RPG.pick(DELTAS), root.y + RPG.pick(DELTAS));
		carve(free, used);

		placefeatures(free, used);
		visible = new boolean[SIZE][SIZE];
		for (int x = 0; x < Dungeon.SIZE; x++) {
			for (int y = 0; y < Dungeon.SIZE; y++) {
				visible[x][y] = false;
				Point p = new Point(x, y);
				if (!free.contains(p)) {
					walls.add(p);
				}
			}
		}
	}

	/**
	 * Places {@link Fountain}, {@link Chest}s and {@link Trap}s then those from
	 * {@link #getextrafeatures(Set, Set)}.
	 * 
	 * @param free
	 * @param used
	 */
	protected void placefeatures(final Set<Point> free, final Set<Point> used) {
		Point fountain = findspot(free, used);
		build(new Fountain("fountain", fountain.x, fountain.y), used);
		free.add(fountain);
		createchests(free, used);
		for (Feature f : getextrafeatures(free, used)) {
			build(f, used);
		}
	}

	void carve(final Set<Point> free, Set<Point> used) {
		while (free.size() < WALKABLEAREA) {
			Point start = new Point(RPG.pick(new ArrayList<Point>(free)));
			int length = RPG.r(5, 7);
			int step = RPG.r(1, 2) == 1 ? -1 : +1;
			if (RPG.r(1, 2) == 1) {
				new HorizontalCorridor(start, used, step).fill(length, free);
			} else {
				new VerticalCorridor(start, used, step).fill(length, free);
			}
		}
	}

	/**
	 * @return <code>true</code> if given point is between 0 and {@link #SIZE}.
	 */
	public static boolean valid(int coordinate) {
		return 0 <= coordinate && coordinate <= SIZE;
	}

	void createchests(Set<Point> free, final Set<Point> used) {
		int chests = RPG.r(3, 5);
		int pool = RewardCalculator.receivegold(Squad.active.members);
		for (int i = 0; i < chests; i++) {// equal trap/treasure find chance
			Trap t = new Trap(findspot(free, used));
			build(t, used);
			pool += RewardCalculator.getgold(t.cr);
		}
		for (int i = chests; i > 0; i--) {
			int gold = i == 1 ? pool : pool / RPG.r(2, i);
			pool -= gold;
			Point p = findspot(free, used);
			used.add(p);
			Feature t;
			if (i == chests) {
				t = createspecialchest(p);
			} else {
				t = RewardCalculator.createchest(gold, p.x, p.y);
			}
			build(t, used);
		}
	}

	/**
	 * @param p
	 *            Chest's location.
	 * @return Most special chest here.
	 */
	protected Feature createspecialchest(Point p) {
		Chest t = new Chest("chest", p.x, p.y, 0, new ItemSelection());
		t.key = Key.generate();
		return t;
	}

	/**
	 * @param feature
	 *            Adds this to {@link #features}...
	 * @param all
	 *            and it's location to this set.
	 */
	protected void build(Feature feature, Set<Point> all) {
		features.add(feature);
		all.add(new Point(feature.x, feature.y));
	}

	/**
	 * @param freep
	 *            Pick a location from here...
	 * @param used
	 *            as long as it's not being used...
	 * @return and returns it.
	 */
	public Point findspot(Collection<Point> freep, Set<Point> used) {
		ArrayList<Point> free = new ArrayList<Point>(freep);
		Point p = null;
		while (p == null || used.contains(p)) {
			p = RPG.pick(free);
		}
		return p;
	}

	/** Exit and destroy this dungeon. */
	public void leave() {
		Dungeon.active = null;
		JavelinApp.context = new WorldScreen(JavelinApp.overviewmap);
		BattleScreen.active = JavelinApp.context;
		hero.remove();
	}

	void regenerate(boolean loading) {
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				map.setTile(x, y, Tile.METALFLOOR);
			}
		}
		for (Point wall : walls) {
			map.setTile(wall.x, wall.y, Tile.STONEWALL);
		}
		hero = Squad.active.createvisual();
		setlocation(loading);
		map.addThing(hero, hero.x, hero.y);
		if (!generated) {
			for (Feature f : features) {
				f.generate(map);
			}
			generated = true;
		}
	}

	/**
	 * Responsible for deciding where the player should be.
	 * 
	 * @param loading
	 *            <code>true</code> if activating this {@link Dungeon} when
	 *            starting the application (loading up a save game with an
	 *            active dungeon).
	 */
	protected void setlocation(boolean loading) {
		hero.x = herolocation.x;
		hero.y = herolocation.y;
	}

	@Override
	protected Integer getel(int attackel) {
		return attackel - 3;
	}

	/**
	 * @return Possible battle maps.
	 * @see Terrain#getmaps()
	 */
	public static Maps getmaps() {
		Maps m = new Maps();
		m.add(new Caves());
		return m;
	}

	/**
	 * Called when reaching {@link StairsUp}
	 */
	public void goup() {
		Dungeon.active.leave();
	}

	/**
	 * Called when reaching {@link StairsDown}.
	 */
	public void godown() {
		// typical dungeon have 1 level only
	}

	/** See {@link WorldScreen#encounter()}. */
	public Fight encounter() {
		return new RandomDungeonEncounter();
	}

	/**
	 * Akin to terrain {@link Hazard}s.
	 */
	public void hazard() {
		// no hazards in normal dungeons
	}

	/**
	 * Similar to {@link #placefeatures(Set, Set)} but usually reserved to
	 * placing {@link TempleDungeon} {@link Feature}s.
	 * 
	 * Called after placing all basic features.
	 *
	 * @param used
	 *            Don't forget to update this as you generate new features!
	 * @return Extra features to be placed using {@link #build(Feature, Set)}.
	 */
	protected List<Feature> getextrafeatures(Set<Point> free, Set<Point> used) {
		ArrayList<Feature> extra = new ArrayList<Feature>();
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot(free, used);
			used.add(spot);
			extra.add(new Brazier(spot.x, spot.y));
		}
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot(free, used);
			used.add(spot);
			extra.add(new FruitTree(spot.x, spot.y));
		}
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot(free, used);
			used.add(spot);
			extra.add(new Portal(spot.x, spot.y));
		}
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot(free, used);
			used.add(spot);
			extra.add(new Spirit(spot.x, spot.y));
		}
		return extra;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}
}
