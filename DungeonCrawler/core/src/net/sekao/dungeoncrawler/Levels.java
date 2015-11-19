package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Timer;
import java.util.*;

public class Levels {
	public static void connectRooms(Level l) {
		int startX = new Random().nextInt(Rooms.COLS);
		int startY = new Random().nextInt(Rooms.ROWS);
		
		ArrayList<Room> rooms = new ArrayList<Room>();
		for (int x = 0; x < Rooms.COLS; x++) {
			for (int y = 0; y < Rooms.ROWS; y++) {
				Room room = new Room(x, y);
				if (x == startX && y == startY) {
					l.startRoom = room;
				}
				rooms.add(room);
			}
		}

		ArrayList<Room> deadEnds = new ArrayList<Room>();
		Rooms.connectRooms(l.map, rooms, l.startRoom, deadEnds);
		l.endRoom = deadEnds.get(0);
	}

	public static Level createLevel(Entity player, int num) {
		if (num == 1) {
			return createLevel1(player);
		} else if (num == 2) {
			return createLevel2(player);
		} else if (num == 3) {
			return createLevel3(player);
		}

		return null;
	}
	
	private static Level createLevel1(Entity player) {
		Level l = new Level();
		
		l.map = new TmxMapLoader().load("level1.tmx");

		Levels.connectRooms(l);
		Entities.putInRoom(player, l.startRoom);

		l.entities = new ArrayList<Entity>();
		l.entities.addAll(Entities.createOgres(256, 20));
		l.entities.addAll(Entities.createMagicians(128, 20));
		Entities.initNPCs(l.map, player, l.entities);
		
		l.entities.add(player);
		
		l.staircase = Entities.createStaircase(l.endRoom);
		l.entities.add(l.staircase);
		
		l.entities.addAll(Utils.splitLayer(l.map, "walls"));
		
		return l;
	}
	
	private static Level createLevel2(Entity player) {
		Level l = new Level();
		
		l.map = new TmxMapLoader().load("level2.tmx");

		Levels.connectRooms(l);
		Entities.putInRoom(player, l.startRoom);

		l.entities = new ArrayList<Entity>();
		l.entities.addAll(Entities.createOgres(256, 20));
		l.entities.addAll(Entities.createElementals(256, 20));
		Entities.initNPCs(l.map, player, l.entities);
		
		l.entities.add(player);
		
		l.staircase = Entities.createStaircase(l.endRoom);
		l.entities.add(l.staircase);
		
		l.entities.addAll(Utils.splitLayer(l.map, "walls"));
		
		return l;
	}
	
	private static Level createLevel3(Entity player) {
		Level l = new Level();
		
		l.map = new TmxMapLoader().load("level3.tmx");

		player.x = 18;
		player.y = 2;
		Utils.mapToScreen(player);

		l.entities = new ArrayList<Entity>();
		l.entities.add(Entities.createDragon(10, 10));
		l.entities.add(player);
		
		l.entities.addAll(Utils.splitLayer(l.map, "walls"));
		
		return l;
	}
}
