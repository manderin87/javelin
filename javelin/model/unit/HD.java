package javelin.model.unit;

import java.io.Serializable;
import java.util.TreeMap;

import tyrant.mikera.engine.RPG;

public class HD implements Serializable, Cloneable {
	private static final String SEPARATOR = " + ";
	TreeMap<Integer, Float> hitdice = new TreeMap<Integer, Float>();
	public int extrahp = 0;

	public void add(float ndice, int hitdie, int extra) {
		Float hd = hitdice.get(hitdie);
		if (hd == null) {
			hd = 0.0f;
		}
		hd += ndice;
		hitdice.put(hitdie, hd);
		extrahp += extra;
	}

	@Override
	public String toString() {
		String output = "";
		for (int hd : hitdice.keySet()) {
			output += translate(hitdice.get(hd)) + "d" + hd + SEPARATOR;
		}
		output = output.substring(0, output.length() - SEPARATOR.length());
		output += extrahp >= 0 ? " + " : " - ";
		return output + Math.abs(extrahp);
	}

	private String translate(Float hd) {
		return hd >= 1 ? Long.toString(Math.round(hd)) : "1/"
				+ Math.round(1 / (1 - hd));
	}

	public int roll(Monster m) {
		int hp = extrahp;
		for (int hd : hitdice.keySet()) {
			Float dice = hitdice.get(hd);
			if (dice >= 1) {
				for (int i = 0; i < dice; i++) {
					hp += ensureminimum(RPG.r(1, hd)
							+ Monster.getbonus(m.constitution));
				}
			} else {
				hp += ensureminimum(Math.round(RPG.r(1, hd) * dice));
			}
		}
		return hp;
	}

	public long ensureminimum(long roll) {
		return roll >= 1 ? roll : 1;
	}

	public int maximize() {
		int hp = extrahp;
		for (Integer hd : hitdice.keySet()) {
			hp += hd * hitdice.get(hd);
		}
		return hp;
	}

	@Override
	public HD clone() {
		try {
			final HD clone = (HD) super.clone();
			clone.hitdice = (TreeMap<Integer, Float>) hitdice.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public int countdice() {
		double dice = 0.0;
		for (double ndice : hitdice.values()) {
			dice += ndice;
		}
		return new Long(Math.round(dice)).intValue();
	}
}
