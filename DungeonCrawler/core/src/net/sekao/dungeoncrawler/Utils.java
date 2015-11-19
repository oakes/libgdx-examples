package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.math.*;
import java.util.*;

public class Utils {
	public static final float PIXELS_PER_TILE = 64;
	public static final float CAMERA_HEIGHT = 4;
	public static final float HALF_TILE_WIDTH = 0.5f;
	public static final float HALF_TILE_HEIGHT = 0.25f;
	public static final int MAP_WIDTH = 40;
	public static final int MAP_HEIGHT = 40;
	public static final int GRID_TILE_SIZE = 256;
	public static final float AGGRO_DISTANCE = 2;
	public static final float ATTACK_DISTANCE = 0.25f;
	public static Sound playerHurt, monsterHurt, death;
	public static Game game;

	public static void clearTile(TiledMap map, String layerName, int x, int y) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
		layer.setCell(x, y, null);
	}

	public static float decelerate(float velocity) {
		float deceleration = 0.9f;
		velocity = velocity * deceleration;
		if (Math.abs(velocity) < 0.5f) {
			velocity = 0;
		}
		return velocity;
	}

	public static void screenToMap(Entity e) {
		float x = e.x, y = e.y;
		e.x = (x / HALF_TILE_WIDTH - y / HALF_TILE_HEIGHT) / 2;
		e.y = (y / HALF_TILE_HEIGHT + (x / HALF_TILE_WIDTH)) / 2;
	}

	public static void mapToScreen(Entity e) {
		float x = e.x, y = e.y;
		e.x = x * HALF_TILE_WIDTH + y * HALF_TILE_WIDTH;
		e.y = -1 * x * HALF_TILE_HEIGHT + y * HALF_TILE_HEIGHT;
	}

	public static boolean isOnLayer(Entity e, TiledMap map, String layerName) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);

		screenToMap(e);
		
		float endX = e.x + e.width;
		float endY = e.y + e.height;
		
		int x = (int) e.x;
		while (x < endX) {
			
			int y = (int) e.y;
			while (y < endY) {
				if (layer.getCell(x, y) != null) {
					mapToScreen(e);
					return true;
				}
				y = y + 1;
			}
			x = x + 1;
		}

		mapToScreen(e);
		return false;
	}

	public static Rectangle getRectangle(Entity e, float minDistance) {
		float x = e.x + e.xFeet - minDistance / 4;
		float y = e.y + e.yFeet - minDistance / 4;
		float width = e.width + minDistance / 2 - e.xFeet * 2;
		float height = e.height + minDistance / 2 - e.yFeet * 2;
		return new Rectangle(x, y, width, height);
	}
	
	public static boolean isNearEntity(Entity e, Entity e2, float minDistance) {
		if (e == e2 || e2.layer != null || e2.health == 0) {
			return false;
		}
		
		Rectangle r1 = getRectangle(e, minDistance);
		Rectangle r2 = getRectangle(e2, minDistance);
		
		return r1.overlaps(r2);
	}

	public static boolean isNearEntity(Entity e, ArrayList<Entity> entities, float minDistance) {
		for (Entity e2 : entities) {
			if (isNearEntity(e, e2, minDistance)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isInvalidLocation(Entity e, ArrayList<Entity> entities, TiledMap map) {
		return isNearEntity(e, entities, 0) || isOnLayer(e, map, "walls");
	}

	public static int random() {
		return (int) Math.round(Math.random() * 2 - 1);
	}

	public static TextureRegion[][] splitTexture(String path, int size, int maskSize) {
		Texture sheet = new Texture(path);
		TextureRegion[][] grid = TextureRegion.split(sheet, size, size);
		int start = (size - maskSize) / 2;

		for (TextureRegion[] row : grid) {
			for (TextureRegion t : row) {
				t.setRegion(t, start, start, maskSize, maskSize);
			}
		}

		return grid;
	}

	public static Entity.Direction getDirection(float xVelocity, float yVelocity) {
		// W, NW, N, NE, E, SE, S, SW
		float[] xDirection = {
			-1, -1, 0, 1, 1, 1, 0, -1
		};
		float[] yDirection = {
			0, 1, 1, 1, 0, -1, -1, -1
		};

		for (Entity.Direction d : Entity.Direction.values()) {
			float x = xDirection[d.ordinal()];
			float y = yDirection[d.ordinal()];
			if (x == Math.signum(xVelocity) && y == Math.signum(yVelocity)) {
				return d;
			}
		}

		return null;
	}

	public static Entity.Direction getDirection(Entity e) {
		return getDirection(e.xVelocity, e.yVelocity);
	}

	public static ArrayList<Entity> splitLayer(TiledMap map, String layerName) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
		ArrayList<Entity> rows = new ArrayList<Entity>();

		for (int x = layer.getWidth() - 1; x >= 0; x--) {
			for (int y = layer.getHeight() - 1; y >= 0; y--) {
				Entity e = new Entity();
				e.x = x;
				e.y = y;
				mapToScreen(e);

				for (Entity r : rows) {
					if (e.y == r.y) {
						e = r;
						break;
					}
				}
				
				if (e.layer == null) {
					e.layer = new TiledMapTileLayer(layer.getWidth(), layer.getHeight(), (int) layer.getTileWidth(), (int) layer.getTileHeight());
					rows.add(e);
				}

				e.layer.setCell(x, y, layer.getCell(x, y));
			}
		}

		return rows;
	}

	public static ArrayList<Entity> getLocationOptions(float width, float height, int mapWidth, int mapHeight) {
		ArrayList<Entity> options = new ArrayList<Entity>();

		int endX = mapWidth - (int) width;
		int endY = mapHeight - (int) height;

		int x = 0;
		while (x < endX) {
			
			int y = 0;
			while (y < endY) {
				Entity option = new Entity();
				option.x = x;
				option.y = y;
				option.width = width;
				option.height = height;
				mapToScreen(option);
				options.add(option);
				
				y = y + 1;
			}
			x = x + 1;
		}

		return options;
	}

	public static void randomizeLocations(TiledMap map, Entity player, ArrayList<Entity> entities) {
		for (Entity e : entities) {
			ArrayList<Entity> options = getLocationOptions(e.width, e.height, MAP_WIDTH, MAP_HEIGHT);
			Utils.shuffle(options);
			for (Entity option : options) {
				if (!isOnLayer(option, map, "walls") &&
				    !isNearEntity(option, entities, 2) &&
				    !isNearEntity(option, player, 5)) {
					e.x = option.x;
					e.y = option.y;
					break;
				}
			}
		}
	}

	public static boolean canAttack(Entity e, Entity e2) {
		if (e2 == null || e.isNPC == e2.isNPC || e.health == 0) {
			return false;
		}

		return e.lastAttack >= e.attackInterval && Utils.isNearEntity(e, e2, ATTACK_DISTANCE);
	}

	public static Entity getEntityAtCursor(Camera camera, ArrayList<Entity> entities, float x, float y) {
		Vector3 v = new Vector3(x, y, 0);
		camera.unproject(v);
		
		Rectangle r = new Rectangle();
		for (Entity e : entities) {
			if ((e.isNPC != true && e.isStaircase != true) || (e.health == 0 && e.drop == null)) {
				continue;
			}
			r.set(e.x, e.y, e.width, e.height);
			if (r.contains(v.x, v.y)) {
				return e;
			}
		}

		return null;
	}
	
	private static void swap(ArrayList list, int idx1, int idx2) {
        Object o1 = list.get(idx1);
        list.set(idx1, list.get(idx2));
        list.set(idx2, o1);
	}

	public static void shuffle(ArrayList objects) {
	    for(int i = objects.size(); i > 1; i--) {
	    	swap(objects, i - 1, new Random().nextInt(i));
	    }
	}
}
