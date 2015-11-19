package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import java.util.*;

public class Entity implements Comparable {
	float x, y;
	float width, height;
	float xFeet, yFeet;
	float xChange, yChange;
	float xVelocity, yVelocity, maxVelocity;
	boolean isMe, isNPC, isStaircase, isBoss;
	float lastAttack, attackInterval = 1;
	float health = 10, wounds = 0, damage = 2;
	TiledMapTileLayer layer;
	
	enum Direction {
		W, NW, N, NE, E, SE, S, SW
	};
	Direction lastDirection = Direction.S;

	ArrayList<Overlay> overlays = new ArrayList<Overlay>();
	Overlay drop;
	
	public int compareTo(Object o) {
		Entity e = (Entity) o;
		if (health == 0 && e.health > 0) {
			return -1;
		} else if (health > 0 && e.health == 0) {
			return 1;
		} else if (y > e.y) {
			return -1;
		} else if (e.y > y) {
			return 1;
		}
		return 0;
	}
}
