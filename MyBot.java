import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.lang.model.SourceVersion;

public class MyBot {
	private static Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	public static void main(String[] args) throws java.io.IOException {
		InitPackage iPackage = Networking.getInit();
		int myID = iPackage.myID;
		GameMap gameMap = iPackage.map;

		Networking.sendInit("MyJavaBot");

		while(true) {
			ArrayList<Move> moves = new ArrayList<Move>();

			gameMap = Networking.getFrame();

			for(int y = 0; y < gameMap.height; y++) {
				for(int x = 0; x < gameMap.width; x++) {
					Site site = gameMap.getSite(new Location(x, y));
					if(site.owner == myID) {
						Location loc = new Location(x, y);
						Direction dir = Direction.randomDirection();
						Direction min = min(gameMap,x,y, myID);
						int ourPro = gameMap.getSite(loc).production;
						int ourStr = gameMap.getSite(loc).strength;
						Direction minLen = minLen(gameMap, x, y, myID);
						Direction maxPro = hiP(gameMap, x, y, myID);
						int maxProStr = (gameMap.getSite(loc, maxPro).strength);
						int minPro = (gameMap.getSite(loc, min).strength);
						if(ourStr == 0){
							moves.add(new Move(loc,Direction.STILL));
						}
						else if(ourPro != 0 && maxPro != null && maxProStr/ourPro <= 2*minPro/ourPro){
							if(gameMap.getSite(loc,maxPro).strength < ourStr)
								moves.add(new Move(loc, maxPro));
							else 
								moves.add(new Move(loc,Direction.STILL));
						}
						else if(min != null){
							if(gameMap.getSite(loc,min).strength < ourStr)
								moves.add(new Move(loc, min));
							else 
								moves.add(new Move(loc,Direction.STILL));
						}
						else if(minLen != null){
							if(ourStr < ourPro * 5 ){
								moves.add(new Move(loc,Direction.STILL));
							}
							else{
								moves.add(new Move(loc, minLen));
							}
						}
						else{
							moves.add(new Move(loc, Direction.randomDirection()));
						}
					}
				}
			}
			Networking.sendFrame(moves);
		}
	}
	private static boolean isEdge(GameMap map, int x, int y, int myID) {
		for(int i=0;i<dirs.length;i++){
			if(map.getSite(new Location(x, y), dirs[i]).owner != myID){
				return true;
			}
		}
		return false;
	}
	public static Direction min(GameMap map, int x, int y, int myID){
		ArrayList<Direction> directions = new ArrayList<>();
		ArrayList<Integer> str = new ArrayList<>();
		for(int i=0;i<dirs.length;i++){
			if(map.getSite(new Location(x, y), dirs[i]).owner != myID){
				str.add(map.getSite(new Location(x, y), dirs[i]).strength);
				directions.add(dirs[i]);
			}
		}
		if(str.isEmpty()) return null;
		return directions.get(str.indexOf(Collections.min(str)));
	}
	public static Direction minLen(GameMap map, int x, int y, int myID){
		int count = Integer.MAX_VALUE;
		Direction minDir = null;
		for(int i=0;i<dirs.length;i++){
			Location curL = new Location(x, y);
			int cur = 0;
			while(map.getSite(curL).owner == myID){
				curL = map.getLocation(curL, dirs[i]);
				cur++;
				if(i<2 && cur > map.height) break;
				if(i>=2 && cur > map.width) break;
			}
			if(cur < count){
				count = cur;
				minDir = dirs[i];
			}
		}
		return minDir;
	}
	public static Direction hiP(GameMap map, int x, int y, int myID){
		ArrayList<Direction> directions = new ArrayList<>();
		ArrayList<Integer> productions = new ArrayList<>();
		for(int i=0;i<dirs.length;i++){
			if(map.getSite(new Location(x, y), dirs[i]).owner != myID){
				directions.add(dirs[i]);
			}
		}
		for(int i=0;i<directions.size();i++){
			Location curL = new Location(x, y);
			productions.add(0);
			for(int j=0;j<5;j++){
				curL = map.getLocation(curL, directions.get(i));
				int p = productions.get(i)+map.getSite(curL).strength;
				if(directions.get(i) != Direction.WEST && directions.get(i) != Direction.EAST){
					if(map.getSite(curL, Direction.WEST).owner != myID) 
						p += map.getSite(curL, Direction.WEST).strength;
					if(map.getSite(curL, Direction.EAST).owner != myID) 
						p += map.getSite(curL, Direction.EAST).strength;
				}
				else{
					if(map.getSite(curL, Direction.NORTH).owner != myID) 
						p += map.getSite(curL, Direction.NORTH).strength;
					if(map.getSite(curL, Direction.SOUTH).owner != myID) 
						p += map.getSite(curL, Direction.SOUTH).strength;
				}
				productions.set(i, p);
			}
		}
		if(directions.isEmpty()) return null;
		return directions.get(productions.indexOf(Collections.max(productions)));
	}
}
