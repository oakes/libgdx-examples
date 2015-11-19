package net.sekao.minicraft;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import java.util.*;

public class Utils {
	public static float pixelsPerTile = 8;
	public static int mapWidth = 50;
	public static int mapHeight = 50;
	public static float cameraHeight = 20;
	public static float stageWidth = 150;
	public static Sound start, playerHurt, monsterHurt, death;
	
	public static float decelerate(float velocity) {
		float deceleration = 0.9f;
		velocity = velocity * deceleration;
		if (Math.abs(velocity) < 0.5f) {
			velocity = 0;
		}
		return velocity;
	}

	public static TextureRegion flipX(TextureRegion t) {
		TextureRegion t2 = new TextureRegion(t);
		t2.flip(true, false);
		return t2;
	}

	public static TextureRegion flipY(TextureRegion t) {
		TextureRegion t2 = new TextureRegion(t);
		t2.flip(false, true);
		return t2;
	}

	public static boolean isOnLayer(Entity e, TiledMap map, String layerName) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
		
		float endX = e.x + e.width;
		float endY = e.y + e.height;
		
		int x = (int) e.x;
		while (x < endX) {
			
			int y = (int) e.y;
			while (y < endY) {
				if (layer.getCell(x, y) == null) {
					return false;
				}
				y = y + 1;
			}
			x = x + 1;
		}
		
		return true;
	}

	public static boolean isOnlyOnLayer(Entity e, TiledMap map, String layerName) {
		int count = map.getLayers().getCount();
		while (count > 0) {
			count = count - 1;
			
			String name = map.getLayers().get(count).getName();
			if (name.equals("grass")) {
				continue;
			}

			boolean isRequired = layerName.equals(name);
			if (isOnLayer(e, map, name) != isRequired) {
				return false;
			}
		}

		return true;
	}

	public static boolean isNearEntity(Entity e, Entity e2, int minDistance) {
		if (e == e2 || e2.drawTime != -1 || e.health == 0 || e2.health == 0) {
			return false;
		}
		
		float xDiff = Math.abs(e.x - e2.x);
		float yDiff = Math.abs(e.y - e2.y);
		
		return xDiff < minDistance && yDiff < minDistance;
	}

	public static boolean isNearEntity(Entity e, ArrayList<Entity> entities, int minDistance) {
		for (Entity e2 : entities) {
			if (isNearEntity(e, e2, minDistance)) {
				return true;
			}
		}

		return false;
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
				options.add(option);
				
				y = y + 1;
			}
			x = x + 1;
		}

		return options;
	}

	public static boolean isOnMap(Entity e) {
		return e.x >= 0 && e.x < mapWidth - 1 && e.y >= 0 && e.y < mapHeight - 1;
	}

	public static int random() {
		return (int) Math.round(Math.random() * 2 - 1);
	}

	public static Image createImage(Stage s, TextureRegion t, float x, float y) {
		Image image = new Image(t);
		image.setPosition(x, y);
		s.addActor(image);
		return image;
	}

	public static boolean canAttack(Entity e, Entity e2) {
		if (e.isNpc == true && e2.isMe != true) {
			return false;
		}

		return e.stamina - e.attackTime >= 1 && Utils.isNearEntity(e, e2, 2);
	}

	public static void updateImages(ArrayList<Image> images, float percent) {
		float num = images.size() * percent;
		for (Image i : images) {
			i.setVisible(num >= 1);
			num--;
		}
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
