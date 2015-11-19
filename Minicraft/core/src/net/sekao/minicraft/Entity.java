package net.sekao.minicraft;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import java.util.*;

public class Entity implements Comparable {
	float x, y;
	float xChange, yChange;
	float width, height;
	float xVelocity, yVelocity;
	TextureRegion image;
	Animation down, up, left, right;
	boolean isMe, isNpc;
	int minDistance = 2;
	String startLayer;
	float drawTime = -1;
	float attackTime = 0, stamina = 1;
	Entity related;
	int health, damage;

	public int compareTo(Object o) {
		Entity e = (Entity) o;
		if (y > e.y) {
			return -1;
		}
		return 1;
	}

	enum Direction {
		DOWN, UP, LEFT, RIGHT
	};
	Direction lastDirection = Direction.DOWN;
}
