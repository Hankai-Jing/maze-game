package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;


import java.awt.Font;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * Class to play the game.
 * @author Hankai Jing
 */
public class Game {
    private TERenderer ter = new TERenderer();
    /* Width of the canvas. */
    public static final int WIDTH = 70;
    /* Height of the canvas. */
    public static final int HEIGHT = 40;

    /**
     * Method used for playing a fresh game. The game should
     * start from the main menu.
     */
    private void setUpCanvas() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }
    /**
     * Display main menu to the canvas and get command from user.
     */
    private String displayMenuAndGetAction() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.LIGHT_GRAY);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, "CS61B: THE GAME");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 2, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, "Quit (Q)");
        StdDraw.show();
        String action = "";
        while (action.equals("")) {
            if (StdDraw.hasNextKeyTyped()) {
                action += StdDraw.nextKeyTyped();
            } else {
                StdDraw.pause(1000);
            }
        }
        return action;
    }
    /**
     * Display a string to the center of the canvas.
     */
    private void displayString(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, s);
        StdDraw.show();
    }
    /**
     * Get a random number from user and return it.
     */
    private long getSeed() {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Please enter a number of "
                + "your choice, ends with 'S'.");
        StdDraw.show();
        String seed = "";
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char next = StdDraw.nextKeyTyped();
                if (next != 'S' && next != 's') {
                    seed += next;
                    displayString(seed);
                } else {
                    break;
                }
            } else {
                StdDraw.pause(1000);
            }
        }
        return Long.parseLong(seed);
    }
    /**
     * Fill the world with nothing.
     */
    private void initialWorld(TETile[][] world) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                world[i][j] = Tileset.NOTHING;
            }
        }
    }
    /**
     * Show tile info on the top of the canvas about Mouse hover.
     */
    private void showInfo(TETile[][] world) {
        String info = "";
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2, HEIGHT - 1, WIDTH / 2, 1);
        StdDraw.show();
        if (world[x][y] == Tileset.NOTHING) {
            info += "Outer Space";
        } else if (world[x][y] == Tileset.WALL) {
            info += "Wall";
        } else if (world[x][y] == Tileset.FLOOR) {
            info += "Floor";
        } else if (world[x][y] == Tileset.LOCKED_DOOR) {
            info += "Locked Door";
        } else if (world[x][y] == Tileset.REN) {
            info += "Player";
        }

        StdDraw.setPenColor(Color.LIGHT_GRAY);
        StdDraw.textLeft(1, HEIGHT - 1, info);
        StdDraw.show();
        info = "";
    }
    /*Return the char that user typed. */
    private char getDirection() {
        char dir = StdDraw.nextKeyTyped();
        return dir;
    }
    /**
     * Make the player move one step towards dir
     * if the tile of the next step is floor.
     */
    private void makePlayerMove(MapGenerator map, TETile[][] world, char dir) {
        int x = map.getPlayerPosition().x;
        int y = map.getPlayerPosition().y;
        if (dir == 'W' || dir == 'w') {
            map.playerMove(world, x, y + 1);
        } else if (dir == 'D' || dir == 'd') {
            map.playerMove(world, x + 1, y);
        } else if (dir == 'S' || dir == 's') {
            map.playerMove(world, x, y - 1);
        } else if (dir == 'A' || dir == 'a') {
            map.playerMove(world, x - 1, y);
        }
    }
    /**
     * Save the state of the game.
     */
    private void save(MapGenerator map) throws IOException {
        FileOutputStream fileOut = new FileOutputStream("/tmp/gameState.txt");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(map);
        out.close();
        fileOut.close();
    }
    /** Load the game from previous save.
     */
    private MapGenerator load() throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream("/tmp/gameState.txt");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        MapGenerator map = (MapGenerator) in.readObject();
        in.close();
        fileIn.close();
        return map;
    }
    /**
     * Method of playing the game with keyboard.
     */
    public void playWithKeyboard() throws IOException, ClassNotFoundException {
        setUpCanvas();
        String action = displayMenuAndGetAction();
        long s = 0;
        MapGenerator map = null;
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        initialWorld(world);
        if (action.equals("Q") || action.equals("q")) {
            java.lang.System.exit(0);
        } else {
            if (action.equals("N") || action.equals("n")) {
                s = getSeed();
                map = new MapGenerator(s);
                map.generateMap(world);
            } else if (action.equals("L") || action.equals("l")) {
                map = load();
                map.loadFromSave(world);
            }
            Font font = new Font("Geneva", Font.BOLD, 15);
            StdDraw.setFont(font);
            ter.renderFrame(world);

            while (true) {
                showInfo(world);
                if (StdDraw.hasNextKeyTyped()) {
                    char dir = getDirection();
                    if (dir == ':') {
                        save(map);
                        java.lang.System.exit(0);
                        break;
                    }
                    makePlayerMove(map, world, dir);
                    ter.renderFrame(world);
                } else {
                    StdDraw.pause(200);
                }
            }
        }
    }

    /**
     * Method used for autograding and testing the game code.
     * The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww".
     * The game should behave exactly as if the user typed these characters
     * into the game after playing playWithKeyboard.
     * If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q"
     * should return the same world. However, the behavior is
     * slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString
     * with the string "l", we'd expect
     * to get the exact same world back again,
     * since this corresponds to loading the saved game.
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
         /*Fill out this method to run the game using the input passed in,
         and return a 2D tile representation of the world that would have been
         drawn if the same inputs had been given to playWithKeyboard().*/

        MapGenerator map = null;
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        initialWorld(world);
        if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
            long seed;
            String s = "";
            int i = 1;
            while (!(input.charAt(i) == 's' || input.charAt(i) == 'S')) {
                s += input.charAt(i);
                i += 1;
            }
            seed = Long.parseLong(s);
            map = new MapGenerator(seed);
            map.generateMap(world);
            i += 1;
            while (i < input.length()) {
                if (input.charAt(i) == ':'
                        && (input.charAt(i + 1) == 'Q' || input.charAt(i + 1) == 'q')) {
                    try {
                        save(map);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    break;
                } else {
                    makePlayerMove(map, world, input.charAt(i));
                }
                i += 1;
            }

        } else if (input.charAt(0) == 'L' || input.charAt(0) == 'l') {
            try {
                map = load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            map.loadFromSave(world);
            int i = 1;
            while (i < input.length()) {
                if (input.charAt(i) == ':'
                        && (input.charAt(i + 1) == 'Q' || input.charAt(i + 1) == 'q')) {
                    try {
                        save(map);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    break;
                } else {
                    makePlayerMove(map, world, input.charAt(i));
                }
                i += 1;
            }
        }

        TETile[][] finalWorldFrame = world;
        return finalWorldFrame;
    }
}
