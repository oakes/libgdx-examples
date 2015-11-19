package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.math.*;
import java.util.*;

public class Entities {
	public static Entity create(TextureRegion[][] grid, int maskSize, float interval) {
		Entity e = new Entity();
		e.width = (float) maskSize / Utils.GRID_TILE_SIZE;
		e.height = (float) maskSize / Utils.GRID_TILE_SIZE;

		e.overlays.add(Overlays.create(grid, interval));
		
		return e;
	}

	public static Entity create(String path, int maskSize, float interval) {
		TextureRegion[][] grid = Utils.splitTexture(path, Utils.GRID_TILE_SIZE, maskSize);
		
		return create(grid, maskSize, interval);
	}

	public static ArrayList<Entity> createOgres(int maskSize, int count) {
		ArrayList<Entity> entities = new ArrayList<Entity>();

		TextureRegion[][] grid = Utils.splitTexture("characters/ogre.png", Utils.GRID_TILE_SIZE, maskSize);
		while (count > 0) {
			Entity e = create(grid, maskSize, 0.2f);
			e.xFeet = e.yFeet = 0.35f;
			e.isNPC = true;
			e.maxVelocity = 1;
			entities.add(e);
			count--;
		}

		return entities;
	}

	public static ArrayList<Entity> createMagicians(int maskSize, int count) {
		ArrayList<Entity> entities = new ArrayList<Entity>();

		TextureRegion[][] grid = Utils.splitTexture("characters/magician.png", Utils.GRID_TILE_SIZE, maskSize);
		while (count > 0) {
			Entity e = create(grid, maskSize, 0.2f);
			e.xFeet = e.yFeet = 0.1f;
			e.isNPC = true;
			e.maxVelocity = 2;
			entities.add(e);
			count--;
		}

		return entities;
	}

	public static ArrayList<Entity> createElementals(int maskSize, int count) {
		ArrayList<Entity> entities = new ArrayList<Entity>();

		TextureRegion[][] grid = Utils.splitTexture("characters/elemental.png", Utils.GRID_TILE_SIZE, maskSize);
		while (count > 0) {
			Entity e = create(grid, maskSize, 0.2f);
			e.xFeet = e.yFeet = 0.35f;
			e.isNPC = true;
			e.maxVelocity = 2;
			entities.add(e);
			count--;
		}

		return entities;
	}

	public static void initNPCs(TiledMap map, Entity player, ArrayList<Entity> entities) {
		Utils.randomizeLocations(map, player, entities);

		ArrayList<Overlay> items = Overlays.createItems();
		Random rand = new Random();
		float dropChance = 0.5f;
		for (Entity e : entities) {
			int randItem = rand.nextInt((int) (items.size() / dropChance));
			if (randItem < items.size()) {
				e.drop = items.get(randItem);
			}
		}
	}

	public static Entity createPlayer() {
		Entity player = Entities.create("characters/male_light.png", 128, 0.2f);
		player.isMe = true;
		player.maxVelocity = 2;
		player.attackInterval = 0.25f;
		player.health = 40;
		return player;
	}

	public static Entity createDragon(float x, float y) {
		Entity e = Entities.create("characters/dragon.png", 256, 0.2f);
		e.isNPC = true;
		e.isBoss = true;
		e.maxVelocity = 1;
		e.health = 80;
		e.width = e.height = 2;
		e.xFeet = e.yFeet = 0.5f;
		e.x = x;
		e.y = y;
		Utils.mapToScreen(e);
		return e;
	}

	public static Entity createStaircase(Room r) {
		TextureRegion[][] miscGrid = Utils.splitTexture("64x64.png", 64, 64);
		Entity staircase = new Entity();
		staircase.width = staircase.height = 1;
		staircase.xFeet = 0.4f;
		staircase.yFeet = 0.3f;
		staircase.isStaircase = true;
		
		Entities.putInRoom(staircase, r);
		staircase.x += 0.5f;
		staircase.y += 0.5f;

		Overlay o = new Overlay();
		o.image = miscGrid[5][5];
		staircase.overlays.add(o);
		
		return staircase;
	}

	public static void putInRoom(Entity e, Room r) {
		e.x = r.x * Rooms.SIZE + Rooms.SIZE / 2;
		e.y = r.y * Rooms.SIZE + Rooms.SIZE / 2;
		Utils.mapToScreen(e);
	}

	private static void movePlayer(Entity e) {
		if (Gdx.input.isTouched() && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			float x = Gdx.input.getX() - Gdx.graphics.getWidth() / 2;
			float y = Gdx.graphics.getHeight() / 2 - Gdx.input.getY();
			
			float xAdjust = e.maxVelocity * Math.abs(x / y);
			float yAdjust = e.maxVelocity * Math.abs(y / x);
			
			e.xVelocity = Math.signum(x) * Math.min(e.maxVelocity, xAdjust);
			e.yVelocity = Math.signum(y) * Math.min(e.maxVelocity, yAdjust);
		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			e.yVelocity = -1 * e.maxVelocity;
		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			e.yVelocity = e.maxVelocity;
		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			e.xVelocity = -1 * e.maxVelocity;
		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			e.xVelocity = e.maxVelocity;
		}
	}

	private static void moveNpc(Entity e, Entity player) {
		if (Utils.isNearEntity(e, player, Utils.AGGRO_DISTANCE)) {
			Rectangle r1 = Utils.getRectangle(e, Utils.ATTACK_DISTANCE);
			Rectangle r2 = Utils.getRectangle(player, Utils.ATTACK_DISTANCE);
			
			if (r1.overlaps(r2)) {
				return;
			}
			
			float xDiff = r1.x - r2.x;
			float yDiff = r1.y - r2.y;
			
			if (xDiff > Utils.ATTACK_DISTANCE) {
				e.xVelocity = -1 * e.maxVelocity;
			} else if (xDiff < -1 * Utils.ATTACK_DISTANCE) {
				e.xVelocity = e.maxVelocity;
			}
			
			if (yDiff > Utils.ATTACK_DISTANCE) {
				e.yVelocity = -1 * e.maxVelocity;
			} else if (yDiff < -1 * Utils.ATTACK_DISTANCE) {
				e.yVelocity = e.maxVelocity;
			}
		} else if (e.lastAttack >= e.attackInterval) {
			e.xVelocity = e.maxVelocity * Utils.random();
			e.yVelocity = e.maxVelocity * Utils.random();
			e.lastAttack = 0;
		}
	}

	public static boolean move(Entity e, Entity player, float delta) {
		e.lastAttack += delta;

		if (e.health == 0) {
			e.xVelocity = e.yVelocity = 0;
		} else {
			if (e.isMe == true) {
				movePlayer(e);
			} else if (e.isNPC == true) {
				moveNpc(e, player);
			}
		}

		e.xChange = e.xVelocity * delta;
		e.yChange = e.yVelocity * delta;

		e.x = e.x + e.xChange;
		e.y = e.y + e.yChange;

		e.xVelocity = Utils.decelerate(e.xVelocity);
		e.yVelocity = Utils.decelerate(e.yVelocity);

		return e.xChange != 0 || e.yChange != 0;
	}
	
	public static void animate(Entity e, float time) {
		Entity.Direction d = Utils.getDirection(e);
		if (d != null) {
			e.lastDirection = d;
			for (Overlay o : e.overlays) {
				o.image = o.move[d.ordinal()].getKeyFrame(time, true);
			}
		}
	}

	public static void preventMove(Entity e, ArrayList<Entity> entities, TiledMap map) {
		e.x = e.x - e.xChange;
		if (Utils.isInvalidLocation(e, entities, map)) {
			e.y = e.y - e.yChange;
			e.yVelocity = 0;
		}
		
		e.x = e.x + e.xChange;
		if (Utils.isInvalidLocation(e, entities, map)) {
			e.x = e.x - e.xChange;
			e.xVelocity = 0;
		}
	}

	public static void attack(Entity e, Entity victim) {
		if (victim != null) {
			float xDiff = (victim.x + victim.xFeet) - (e.x + e.xFeet);
			float yDiff = (victim.y + victim.yFeet) - (e.y + e.yFeet);
			Entity.Direction d = Utils.getDirection(xDiff, yDiff);
			if (d != null) {
				e.lastDirection = d;
			}

			victim.lastAttack = 0;
			victim.health = Math.max(0, victim.health - e.damage);
			victim.wounds += e.damage;
			for (Overlay o : victim.overlays) {
				if (victim.health > 0) {
					o.image = o.hit[victim.lastDirection.ordinal()];
				} else {
					o.image = o.dead[victim.lastDirection.ordinal()];
				}
			}

			if (victim.isMe == true) {
				if (victim.health > 0) {
					Utils.playerHurt.play();
				} else {
					Utils.death.play();
				}
			} else {
				Utils.monsterHurt.play();
			}
		}

		if (e.health > 0) {
			for (Overlay o : e.overlays) {
				o.image = o.attack[e.lastDirection.ordinal()];
			}
			e.lastAttack = 0;
		}
	}

	public static void recover(Entity e) {
		if (e.lastAttack >= 0.5f && e.health > 0) {
			for (Overlay o : e.overlays) {
				o.image = o.move[e.lastDirection.ordinal()].getKeyFrame(0, true);
			}
		}
	}
}
