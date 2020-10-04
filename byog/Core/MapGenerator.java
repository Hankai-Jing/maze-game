package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import byog.lab5.Position;
import java.util.Random;

/**
 * Generate random map.
 * @author Hankai Jing
 */
public class MapGenerator implements java.io.Serializable {

    private Position startPoint;
    private Position player;
    /*Keep record of the status of each grid of the world, 0 indicates wall, 1 indicate path
    * -1 indicates outer space, 2 indicates locked door. */
    private int[][] flag;
    private int currDirection;
    private int size;
    Random random;
    private int width;
    private int height;
    private long seed;

    public MapGenerator() {
        startPoint = new Position(20, 20);
        player = new Position(0, 0);
        currDirection = 1;
        size = 0;
        //random = new Random(System.currentTimeMillis());
    }
    public MapGenerator(long seed) {
        startPoint = new Position(20, 20);
        player = new Position(0, 0);
        currDirection = 1;
        size = 0;
        random = new Random(seed);
        this.seed = seed;
    }
    public Position getPlayerPosition() {
        return player;
    }
    /**
     * Instantiate flag and initial flag with all 0
     * and initial width and height.
     */
    private void makeFlag(TETile[][] world) {
        height = world[0].length - 2;
        width = world.length;
        flag = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                flag[i][j] = 0;
            }
        }

    }

    /**
     * Fill the whole world with walls.
     */
    private void fillWithWalls(TETile[][] world) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                world[i][j] = Tileset.WALL;
            }
        }
    }

    /**
     * Return 10 if p is within the world's boundary-1 and the tile of p is wall.
     */
    private int canGo(Position p) {
        if (p.x > 0 && p.x < width - 1 && p.y > 0 && p.y < height - 1) {
            return 10;
        }
        return 0;
    }
    /**
     * Pick next step to move.
     * Array chance indicates possibility of each direction.
     * index 0 indicates N, 1 indicates E, 2 indicates S, 3 indicates W.
     * [90,3,3,0] means 90 out of 99 chance will go North, 3 out of 99 will go East,
     * 3 out of 99 will go South, 0 out of 99 will go West.
     * We want the Pac-Man to go along current direction as much as possible.
     */
    private Position pickNextStep(Position curr) {
        int[] chance = new int[4];
        Position[] positions = new Position[4];
        positions[0] = new Position(curr.x, curr.y + 1);
        positions[1] = new Position(curr.x + 1, curr.y);
        positions[2] = new Position(curr.x, curr.y - 1);
        positions[3] = new Position(curr.x - 1, curr.y);
        for (int i = 0; i < 4; i++) {
            chance[i] = canGo(positions[i]);
        }

        if (chance[currDirection] != 0) {
            chance[currDirection] = 140;
        }
        int total = 0;
        for (int i = 0; i < 4; i++) {
            total += chance[i];
        }
        int c = random.nextInt(total);
        Position ns = null;
        int t = 0;
        for (int i = 0; i < 4; i++) {
            t += chance[i];
            if (c < t) {
                ns = positions[i];
                currDirection = i;
                break;
            }
        }
        return ns;
    }
    /**
     * Create random path, like putting a Pac-Man into
     * the world and eat walls along moving
     * to create path and stop when size of
     * path >= half of size of world.
     * */
    private void createRandomPath(TETile[][] world) {
        /*Decide start direction based on the shape of the world */
        if (height < width) {
            currDirection = 1;
        }
        /*Move startPoint to the center of the world */
        startPoint.x = width / 3;
        startPoint.y = height / 3;

        Position p = startPoint;
        int worldSize = width * height;
        while (true) {
            Position nextStep = pickNextStep(p);
            world[nextStep.x][nextStep.y] = Tileset.FLOOR;
            flag[nextStep.x][nextStep.y] = 1;
            size += 1;
            p.x = nextStep.x;
            p.y = nextStep.y;

            if (size >= worldSize / 4) {
                break;
            }
        }
    }
    /**
     * Randomly pick a position which of given tileType.
     * 0 is wall, 1 is floor.
     */
    private Position pickRandomPosition(TETile[][] world, int tileType) {
        while (true) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (flag[x][y] == tileType) {
                Position rp = new Position(x, y);
                return rp;
            }
        }
    }
    /**
     * Randomly pick one position and expand it into a room.
     */
    private void makeRoom(TETile[][] world, int sideLength) {
        Position rp = pickRandomPosition(world, 1);
        for (int i = rp.x; i < rp.x + sideLength; i++) {
            for (int j = rp.y; j < rp.y + sideLength; j++) {
                if (i < width - 1 && j < height - 1) {
                    world[i][j] = Tileset.FLOOR;
                    flag[i][j] = 1;
                }
            }
        }
    }
    /**
     * Return true if top, down, left and right of (x, y)
     * are all within boundary and
     * are all walls or nothing.
     */
    private boolean surroundByAllWallsOrNothing(int x, int y) {
        Position[] positions = new Position[4];
        positions[0] = new Position(x, y + 1);
        positions[1] = new Position(x + 1, y);
        positions[2] = new Position(x, y - 1);
        positions[3] = new Position(x - 1, y);
        for (int i = 0; i < 4; i++) {
            int n = positions[i].x;
            int m = positions[i].y;
            if (n >= 0 && n < width && m >= 0 && m < height) {
                if (flag[n][m] > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Return true if top, down, left and right of (x, y)
     * are all within boundary and
     * are all floors.
     */
    private boolean surroundByAllFloor(int x, int y) {
        Position[] positions = new Position[4];
        positions[0] = new Position(x, y + 1);
        positions[1] = new Position(x + 1, y);
        positions[2] = new Position(x, y - 1);
        positions[3] = new Position(x - 1, y);
        for (int i = 0; i < 4; i++) {
            int n = positions[i].x;
            int m = positions[i].y;
            if (n >= 0 && n < width && m >= 0 && m < height) {
                if (flag[n][m] != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Turn wall into floor if up, down, left, right of
     * the wall are all floors, or vice versa.
     * surroundBy = 0 means Wall, surroundBy = 1 means Floor.
     */
    private void flipSomeTiles(TETile[][] world, int surroundBy) {

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (surroundBy == 0) {
                    if (surroundByAllWallsOrNothing(i, j)) {
                        world[i][j] = Tileset.NOTHING;
                        flag[i][j] = -1;
                    }
                } else {
                    if (surroundByAllFloor(i, j) && flag[i][j] != 1) {
                        world[i][j] = Tileset.FLOOR;
                        flag[i][j] = 1;
                    }
                }

            }
        }
    }

    /**
     * Return true if (x, y)'s 8 surroundings include
     * at least 1 floor and 2 walls.
     */
    private boolean wall(int x, int y) {

        Position[] ps = new Position[8];
        int floorCount = 0;
        int wallCount = 0;
        ps[0] = new Position(x - 1, y + 1);
        ps[1] = new Position(x, y + 1);
        ps[2] = new Position(x + 1, y + 1);
        ps[3] = new Position(x - 1, y);
        ps[4] = new Position(x + 1, y);
        ps[5] = new Position(x - 1, y - 1);
        ps[6] = new Position(x, y - 1);
        ps[7] = new Position(x + 1, y - 1);
        for (int i = 0; i < 8; i++) {
            if (ps[i].x >= 0 && ps[i].x < width && ps[i].y >= 0 && ps[i].y < height) {
                if (flag[ps[i].x][ps[i].y] == 0) {
                    wallCount += 1;
                } else if (flag[ps[i].x][ps[i].y] == 1) {
                    floorCount += 1;
                }
            }
        }
        if (wallCount >= 2 && floorCount >= 1) {
            return true;
        }
        return false;
    }
    /**
     * Fix corners of walls, if Nothing's 8 surround
     * positions include at least 1 floor and
     * 2 walls, turn Nothing into Wall.
     */
    private void makeCornerWall(TETile[][] world) {

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (flag[i][j] == -1 && wall(i, j)) {
                    world[i][j] = Tileset.WALL;
                    flag[i][j] = 0;
                }
            }
        }
    }

    /**
     * Randomly pick a wall position whose up, down,
     * left or right has at least 1 floor
     * and turn it into a locked door.
     * And put player at the Door.
     */
    private void makeDoor(TETile[][] world) {
        Position p;
        boolean f = false;
        while (true) {
            p = pickRandomPosition(world, 0);
            Position[] ps = new Position[4];
            ps[0] = new Position(p.x, p.y + 1);
            ps[1] = new Position(p.x + 1, p.y);
            ps[2] = new Position(p.x, p.y - 1);
            ps[3] = new Position(p.x - 1, p.y);
            for (int i = 0; i < 4; i++) {
                if (ps[i].x >= 0 && ps[i].x < width && ps[i].y >= 0 && ps[i].y < height) {
                    if (flag[ps[i].x][ps[i].y] == 1) {
                        f = true;
                        break;
                    }
                }
            }
            if (f) {
                break;
            }
        }

        world[p.x][p.y] = Tileset.LOCKED_DOOR;
        flag[p.x][p.y] = 2;
        player.x = p.x;
        player.y = p.y;
    }
    /**
     * Make player move one step to (x, y) if the (x, y) is Floor.
     */
    public void playerMove(TETile[][] world, int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            if (flag[x][y] == 1) {
                if (world[player.x][player.y] == Tileset.REN) {
                    world[player.x][player.y] = Tileset.FLOOR;
                }
                player.x = x;
                player.y = y;
                world[x][y] = Tileset.REN;
            }
        }
    }

    /**
     * Method to generate a random map.
     * @param world
     */
    public void generateMap(TETile[][] world) {
        //random = new Random(seed);
        makeFlag(world);
        fillWithWalls(world);
        createRandomPath(world);
        for (int i = 1; i < 6; i++) {
            makeRoom(world, 5);
        }
        flipSomeTiles(world, 0);
        flipSomeTiles(world, 1);
        makeCornerWall(world);
        makeDoor(world);
    }

    /**
     * Method to reload map from previous save.
     * @param world
     */
    public void loadFromSave(TETile[][] world) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (flag[i][j] == 0) {
                    world[i][j] = Tileset.WALL;
                } else if (flag[i][j] == 1) {
                    world[i][j] = Tileset.FLOOR;
                } else if (flag[i][j] == -1) {
                    world[i][j] = Tileset.NOTHING;
                } else {
                    world[i][j] = Tileset.LOCKED_DOOR;
                }
            }
        }
        if (flag[player.x][player.y] == 1) {
            world[player.x][player.y] = Tileset.REN;
        }
    }
}
