package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.utils.*;
import java.util.*;

public class Rooms {
	public static final int COLS = 4;
	public static final int ROWS = 4;
	public static final int SIZE = 10;

	public static Room getRandomNeighbor(ArrayList<Room> rooms, Room room) {
		ArrayList<Room> neighbors = new ArrayList<Room>();
		neighbors.add(new Room(room.x - 1, room.y));
		neighbors.add(new Room(room.x, room.y - 1));
		neighbors.add(new Room(room.x + 1, room.y));
		neighbors.add(new Room(room.x, room.y + 1));

		Utils.shuffle(rooms);
		Utils.shuffle(neighbors);

		for (Room r : rooms) {
			for (Room n : neighbors) {
				if (r.x == n.x && r.y == n.y && r.visited == false) {
					return r;
				}
			}
		}
		
		return null;
	}

	public static void connectRoom(TiledMap map, Room r1, Room r2) {
		int randomSpot = new Random().nextInt(SIZE - 3) + 1;
		int x = r1.x * SIZE + randomSpot;
		int y = r1.y * SIZE + randomSpot;
		int xDiff = r2.x - r1.x;
		int yDiff = r2.y - r1.y;
		
		int step = 0;
		while (step < SIZE) {
			x = x + xDiff;
			y = y + yDiff;
			Utils.clearTile(map, "walls", x, y);
			Utils.clearTile(map, "walls", x + 1, y);
			Utils.clearTile(map, "walls", x, y + 1);
			Utils.clearTile(map, "walls", x + 1, y + 1);
			step++;
		}
	}

	public static boolean connectRooms(TiledMap map, ArrayList<Room> rooms, Room room, ArrayList<Room> deadEnds) {
		room.visited = true;
		
		Room nextRoom = getRandomNeighbor(rooms, room);
		if (nextRoom == null) {
			deadEnds.add(room);
			return false;
		}

		connectRoom(map, room, nextRoom);
		
		while (connectRooms(map, rooms, nextRoom, deadEnds)) {}

		return true;
	}
}
