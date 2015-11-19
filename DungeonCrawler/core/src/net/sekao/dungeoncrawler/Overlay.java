package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import java.util.*;

public class Overlay implements Comparable {
	Animation[] move;
	TextureRegion[] attack, special, hit, dead;
	TextureRegion image;

	int zIndex;
	float health, damage;
	String name;

	public int compareTo(Object o) {
		Overlay overlay = (Overlay) o;
		int diff = zIndex - overlay.zIndex;
		return (int) Math.signum(diff);
	}
}
