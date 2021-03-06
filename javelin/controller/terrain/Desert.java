package javelin.controller.terrain;

import java.util.List;
import java.util.Set;

import javelin.controller.Weather;
import javelin.controller.terrain.hazard.Break;
import javelin.controller.terrain.hazard.Cold;
import javelin.controller.terrain.hazard.Dehydration;
import javelin.controller.terrain.hazard.GettingLost;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Heat;
import javelin.controller.terrain.map.Maps;
import javelin.controller.terrain.map.desert.Rocky;
import javelin.controller.terrain.map.desert.Sandy;
import javelin.controller.terrain.map.desert.Tundra;
import javelin.controller.terrain.map.tyrant.Ruin;
import javelin.model.world.World;
import tyrant.mikera.engine.Point;

/**
 * Sandy desert, becomes {@link Tundra} in winter.
 * 
 * @author alex
 */
public class Desert extends Terrain {
	/**
	 * Used instead of normal storms on the desert, makes it easier to get lost.
	 * 
	 * @see #getweather()
	 */
	public static final String SANDSTORM = "sandstorm";

	/** Constructor. */
	public Desert() {
		this.name = "desert";
		this.difficulty = +1;
		this.difficultycap = -2;
		this.speedtrackless = 1 / 2f;
		this.speedroad = 1 / 2f;
		this.speedhighway = 1f;
		this.visionbonus = 0;
		representation = 'd';
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Tundra());
		m.add(new Rocky());
		m.add(new Sandy());
		m.add(new Ruin());
		return m;
	}

	@Override
	protected Point generatesource(World w) {
		Point source = super.generatesource(w);
		while (!w.map[source.x][source.y].equals(Terrain.FOREST)
				|| checkadjacent(source, Terrain.MOUNTAINS, w, 1) == 0) {
			source = super.generatesource(w);
		}
		return source;
	}

	@Override
	public void generatesurroundings(List<Point> area, World w) {
		int radius = 2;
		for (Point p : area) {
			for (int x = -radius; x <= +radius; x++) {
				for (int y = -radius; y <= +radius; y++) {
					int surroundingx = p.x + x;
					int surroundingy = p.y + y;
					if (!World.validatecoordinate(surroundingx, surroundingy)) {
						continue;
					}
					if (w.map[surroundingx][surroundingy]
							.equals(Terrain.FOREST)) {
						w.map[surroundingx][surroundingy] = Terrain.PLAIN;
					}
				}
			}
		}
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		hazards.add(new Dehydration());
		hazards.add(new Heat());
		hazards.add(new Cold());
		hazards.add(new GettingLost(getweather() == SANDSTORM ? 24 : 14));
		if (special) {
			hazards.add(new Break());
		}
		return hazards;
	}

	@Override
	public String getweather() {
		return Weather.current == Weather.STORM ? SANDSTORM : "";
	}
}