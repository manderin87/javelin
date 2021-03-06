package javelin.controller.terrain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javelin.controller.terrain.hazard.Flood;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.map.Maps;
import javelin.controller.terrain.map.plain.Battlefield;
import javelin.controller.terrain.map.plain.Farm;
import javelin.controller.terrain.map.plain.Grasslands;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;

/**
 * Easiest and most even terrain type.
 * 
 * @author alex
 */
public class Plains extends Terrain {
	/** Constructor. */
	public Plains() {
		this.name = "plains";
		this.difficulty = -1;
		this.difficultycap = -4;
		this.speedtrackless = 3 / 4f;
		this.speedroad = 1f;
		this.speedhighway = 1f;
		this.visionbonus = +2;
		representation = ' ';
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Farm());
		m.add(new Grasslands());
		m.add(new Battlefield());
		return m;
	}

	@Override
	protected Point generatesource(World world) {
		return RPG.pick(new ArrayList<Point>(gettiles(world)));
	}

	@Override
	protected HashSet<Point> generatestartingarea(World world) {
		return gettiles(world);
	}

	@Override
	public boolean generatetown(Point p, World w) {
		return checkadjacent(p, DESERT, w, 1) == 0 && super.generatetown(p, w);
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		if (special) {
			Point location = new Point(Squad.active.x, Squad.active.y);
			if (checkadjacent(location, WATER, World.seed, 1) > 0
					|| checkadjacent(location, MARSH, World.seed, 1) > 0) {
				hazards.add(new Flood());
			}
		}
		return hazards;
	}
}