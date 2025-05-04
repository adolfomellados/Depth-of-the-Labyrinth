import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    final int TILE_SIZE = 25;
    final int MAX_COL = 30;
    final int MAX_ROW = 30;
    final int SCREEN_WIDTH = TILE_SIZE * MAX_COL;
    final int SCREEN_HEIGHT = TILE_SIZE * MAX_ROW;


    int playerX = 1 * TILE_SIZE;
    int playerY = 1 * TILE_SIZE;

    Image playerImage;

    int monsterX = 400;
    int monsterY = 400;
    int monsterMoveCount = 0;
    int monsterMoveDelay = 20;

    int monster2X = 0;
    int monster2Y = 0;
    int monster2MoveCount = 0;
    int monster2MoveDelay = 30;

    Image monsterImage;
    Image monster2Image;
    Image monster3Image;

    int monster3X = 0;
    int monster3Y = 0;
    int monster3MoveCount = 0;
    int monster3MoveDelay = 30;

    int keyX;
    int keyY;

    Image keyImage;
    boolean hasKey = false;

    int exitX;
    int exitY;
    Image doorImage;
    boolean hasEscaped = false;

    int[][] maze;

    Clip backgroundMusicClip;

    Timer timer;



    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        MazeGenerator generator = new MazeGenerator(MAX_ROW, MAX_COL);
        maze = generator.getMaze();

        Random rand = new Random();
        int playerCol = 0, playerRow = 0;
        while (true) {
            int col = rand.nextInt(MAX_COL);
            int row = rand.nextInt(MAX_ROW);
            if (maze[row][col] == 0) {
                playerCol = col;
                playerRow = row;
                playerX = col * TILE_SIZE;
                playerY = row * TILE_SIZE;
                break;
            }
        }

        while (true) {
            int col = rand.nextInt(MAX_COL);
            int row = rand.nextInt(MAX_ROW);
            int dist = Math.abs(col - playerCol) + Math.abs(row - playerRow);
            if (maze[row][col] == 0 && dist >= 6) {
                monsterX = col * TILE_SIZE;
                monsterY = row * TILE_SIZE;
                break;
            }
        }

        while (true) {
            int col = rand.nextInt(MAX_COL);
            int row = rand.nextInt(MAX_ROW);
            int distFromPlayer = Math.abs(col - playerCol) + Math.abs(row - playerRow);
            int distFromMonster1 = Math.abs(col * TILE_SIZE - monsterX) / TILE_SIZE + Math.abs(row * TILE_SIZE - monsterY) / TILE_SIZE;
            if (maze[row][col] == 0 && distFromPlayer >= 6 && distFromMonster1 >= 2) {
                monster2X = col * TILE_SIZE;
                monster2Y = row * TILE_SIZE;
                break;
            }
        }

        while (true) {
            int col = rand.nextInt(MAX_COL);
            int row = rand.nextInt(MAX_ROW);

            int distFromPlayer = Math.abs(col - playerCol) + Math.abs(row - playerRow);
            int distFromMonster1 = Math.abs(col * TILE_SIZE - monsterX) / TILE_SIZE +
                    Math.abs(row * TILE_SIZE - monsterY) / TILE_SIZE;
            int distFromMonster2 = Math.abs(col * TILE_SIZE - monster2X) / TILE_SIZE +
                    Math.abs(row * TILE_SIZE - monster2Y) / TILE_SIZE;

            if (maze[row][col] == 0 &&
                    distFromPlayer >= 6 &&
                    distFromMonster1 >= 4 &&
                    distFromMonster2 >= 4 &&
                    (col * TILE_SIZE != playerX || row * TILE_SIZE != playerY)) {

                monster3X = col * TILE_SIZE;
                monster3Y = row * TILE_SIZE;
                break;
            }
        }


        timer = new Timer(1000 / 60, this);
        timer.start();
        placeKeyandExit();
        try{
            playerImage = new ImageIcon(getClass().getResource("/adventurer.png")).getImage();
            monsterImage = new ImageIcon(getClass().getResource("/Monster1.png")).getImage();
            monster2Image = new ImageIcon(getClass().getResource("/monster2.png")).getImage();
            monster3Image = new ImageIcon(getClass().getResource("/ghost.png")).getImage();
            keyImage = new ImageIcon(getClass().getResource("/key.png")).getImage();
            doorImage = new ImageIcon(getClass().getResource("/door.png")).getImage();

        } catch (Exception e) {
            e.printStackTrace();
        }
        playBackgroundMusic("/background_music (online-audio-converter.com).wav");
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        for (int i = 0; i <= SCREEN_WIDTH; i += TILE_SIZE) {
            g.drawLine(i, 0, i, SCREEN_HEIGHT);
        }
        for (int i = 0; i <= SCREEN_HEIGHT; i += TILE_SIZE) {
            g.drawLine(0, i, SCREEN_WIDTH, i);
        }

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                if (maze[row][col] == 0) {
                    g.setColor(Color.GRAY); // path tiles
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                } else {
                    g.setColor(Color.BLACK); // wall tiles (blends into background)
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        g.drawImage(playerImage, playerX, playerY, TILE_SIZE, TILE_SIZE, this);

        g.drawImage(monsterImage, monsterX, monsterY, TILE_SIZE, TILE_SIZE, this);

        g.drawImage(monster2Image, monster2X, monster2Y, TILE_SIZE, TILE_SIZE, this);

        g.drawImage(monster3Image, monster3X, monster3Y, TILE_SIZE, TILE_SIZE, this);

        if (!hasKey) {
            g.drawImage(keyImage, keyX, keyY, TILE_SIZE, TILE_SIZE, this);
        }

        g.drawImage(doorImage, exitX, exitY, TILE_SIZE, TILE_SIZE, this)
;    }

    public void actionPerformed(ActionEvent e) {
        repaint();

        monsterMoveCount++;
        if (monsterMoveCount >= monsterMoveDelay) {
            moveMonster();
            checkMonsterCatch();
            monsterMoveCount = 0;
        }

        monster2MoveCount++;
        if (monster2MoveCount >= monster2MoveDelay) {
            moveMonster2TowardPlayer();
            checkMonster2Catch();
            monster2MoveCount = 0;
        }
        monster3MoveCount++;
        if(monster3MoveCount >= monster3MoveDelay) {
            moveMonster3TowardPlayer();
            checkMonster3Catch();
            monster3MoveCount = 0;
        }
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if(code == KeyEvent.VK_UP) {
            if(canMove(playerX, playerY - TILE_SIZE)) {
                playerY -= TILE_SIZE;
            }
        }
        if(code == KeyEvent.VK_DOWN) {
            if(canMove(playerX, playerY + TILE_SIZE)) {
                playerY += TILE_SIZE;
            }
        }
        if(code == KeyEvent.VK_LEFT) {
            if(canMove(playerX- TILE_SIZE, playerY )) {
                playerX -= TILE_SIZE;
            }
        }
        if(code == KeyEvent.VK_RIGHT) {
            if(canMove(playerX + TILE_SIZE, playerY)) {
                playerX += TILE_SIZE;
            }
        }
        checkKeyPickup();
        checkExit();
    }

    public boolean canMove(int nextX, int nextY) {
        int col = nextX / TILE_SIZE;
        int row = nextY / TILE_SIZE;

        if(col >= 0 && col < MAX_COL && row >= 0 && row < MAX_ROW) {
            return maze[row][col] == 0;
        } else {
            return false;
        }
    }

    public void moveMonster() {
        boolean moveHorizontally = Math.random() < 0.5;
        if (moveHorizontally) {
            if (playerX < monsterX) {
                if (monsterX - TILE_SIZE >= 0) {
                    monsterX -= TILE_SIZE;
                }
            } else if (playerX > monsterX) {
                if (monsterX + TILE_SIZE <= SCREEN_WIDTH - TILE_SIZE) {
                    monsterX += TILE_SIZE;
                }
            }
        } else {
            if (playerY < monsterY) {
                if (monsterY - TILE_SIZE >= 0) {
                    monsterY -= TILE_SIZE;
                }
            } else if (playerY > monsterY) {
                if (monsterY + TILE_SIZE <= SCREEN_HEIGHT - TILE_SIZE) {
                    monsterY += TILE_SIZE;
                }
            }
        }
    }

    public void checkMonsterCatch() {
        if(playerX == monsterX && playerY == monsterY) {
            timer.stop();
            if (backgroundMusicClip != null) {
                backgroundMusicClip.stop();
            }
            playSound("/monster_catch.wav");
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "GAME OVER\nThe Blood Demon got you!",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    new String[]{"Retry", "Exit"},
                    "Retry"
            );

            if (choice == JOptionPane.YES_OPTION) {
                retryGame();
            } else {
                System.exit(0);
            }
        }
    }

    public void moveMonster2TowardPlayer() {
        int startRow = monster2Y / TILE_SIZE;
        int startCol = monster2X / TILE_SIZE;
        int goalRow = playerY / TILE_SIZE;
        int goalCol = playerX / TILE_SIZE;

        boolean[][] visited = new boolean[MAX_ROW][MAX_COL];
        int[][] prevRow = new int[MAX_ROW][MAX_COL];
        int[][] prevCol = new int[MAX_ROW][MAX_COL];

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startRow, startCol));
        visited[startRow][startCol] = true;

        boolean found = false;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty() && !found) {
            Point current = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nr = current.x + dr[i];
                int nc = current.y + dc[i];

                if (nr >= 0 && nr < MAX_ROW && nc >= 0 && nc < MAX_COL &&
                        !visited[nr][nc] && maze[nr][nc] == 0) {

                    queue.add(new Point(nr, nc));
                    visited[nr][nc] = true;
                    prevRow[nr][nc] = current.x;
                    prevCol[nr][nc] = current.y;

                    if (nr == goalRow && nc == goalCol) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if (found) {
            int cr = goalRow;
            int cc = goalCol;
            while (!(prevRow[cr][cc] == startRow && prevCol[cr][cc] == startCol)) {
                int pr = prevRow[cr][cc];
                int pc = prevCol[cr][cc];
                cr = pr;
                cc = pc;
            }
            monster2Y = cr * TILE_SIZE;
            monster2X = cc * TILE_SIZE;
        }
    }

    public void moveMonster3TowardPlayer() {
        int startRow = monster3Y / TILE_SIZE;
        int startCol = monster3X / TILE_SIZE;
        int goalRow = playerY / TILE_SIZE;
        int goalCol = playerX / TILE_SIZE;

        boolean[][] visited = new boolean[MAX_ROW][MAX_COL];
        int[][] prevRow = new int[MAX_ROW][MAX_COL];
        int[][] prevCol = new int[MAX_ROW][MAX_COL];

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startRow, startCol));
        visited[startRow][startCol] = true;

        boolean found = false;
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty() && !found) {
            Point current = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nr = current.x + dr[i];
                int nc = current.y + dc[i];
                if (nr >= 0 && nr < MAX_ROW && nc >= 0 && nc < MAX_COL &&
                        !visited[nr][nc] && maze[nr][nc] == 0) {

                    queue.add(new Point(nr, nc));
                    visited[nr][nc] = true;
                    prevRow[nr][nc] = current.x;
                    prevCol[nr][nc] = current.y;

                    if (nr == goalRow && nc == goalCol) {
                        found = true;
                        break;
                    }
                }
            }
        }

        if (found) {
            int cr = goalRow;
            int cc = goalCol;
            while (!(prevRow[cr][cc] == startRow && prevCol[cr][cc] == startCol)) {
                int pr = prevRow[cr][cc];
                int pc = prevCol[cr][cc];
                cr = pr;
                cc = pc;
            }
            monster3Y = cr * TILE_SIZE;
            monster3X = cc * TILE_SIZE;
        }
    }

    public void checkMonster2Catch() {
        Rectangle playerRect =  new Rectangle(playerX, playerY, TILE_SIZE, TILE_SIZE);
        Rectangle monster2Rect = new Rectangle(monster2X, monster2Y, TILE_SIZE, TILE_SIZE);

        if (playerRect.intersects(monster2Rect)) {
            timer.stop();


            if (backgroundMusicClip != null) backgroundMusicClip.stop();
            playSound("/monster_catch.wav");
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "GAME OVER\nThe Hungry Goblin got you!",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    new String[]{"Retry", "Exit"},
                    "Retry"
            );

            if (choice == JOptionPane.YES_OPTION) {
                retryGame();
            } else {
                System.exit(0);
            }
        }
    }

    public void checkMonster3Catch() {
        Rectangle playerRect = new Rectangle(playerX, playerY, TILE_SIZE, TILE_SIZE);
        Rectangle monsterRect = new Rectangle(monster3X, monster3Y, TILE_SIZE, TILE_SIZE);

        if (playerRect.intersects(monsterRect)) {
            timer.stop();
            if (backgroundMusicClip != null) backgroundMusicClip.stop();
            playSound("/monster_catch.wav");
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "GAME OVER\nDeadly Ghost got you!",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    new String[]{"Retry", "Exit"},
                    "Retry"
            );
            if (choice == JOptionPane.YES_OPTION) retryGame();
            else System.exit(0);
        }
    }

    public void checkKeyPickup() {
        if (playerX == keyX && playerY == keyY && !hasKey) {
            hasKey = true;
            System.out.println("You found the key!");
        }
    }

    public void checkExit() {
        if (playerX == exitX && playerY == exitY) {
            if (hasKey) {
                hasEscaped = true;
                timer.stop();
                if (backgroundMusicClip != null) {
                    backgroundMusicClip.stop();
                }
                int choice = JOptionPane.showOptionDialog(
                        this,
                        "You WIN!\nYou escaped safely!",
                        "Victory",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"Play Again", "Exit"},
                        "Play Again"
                );

                if (choice == JOptionPane.YES_OPTION) {
                    retryGame();
                } else {
                    System.exit(0);
                }
            } else {
                JOptionPane.showMessageDialog(this, "You need the key to exit!", "LOCKED!",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void playSound(String soundFileName) {
        try {
            File file = new File(soundFileName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(soundFileName));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playBackgroundMusic(String soundFileName) {
        try{
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(soundFileName));
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioStream);
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void placeKeyandExit() {
        Random random = new Random();

        while(true) {
            int randomCol = random.nextInt(MAX_COL);
            int randomRow = random.nextInt(MAX_ROW);

            if(maze[randomRow][randomCol] == 0) {
                keyX = randomCol * TILE_SIZE;
                keyY = randomRow * TILE_SIZE;
                break;
            }
        }

        while(true) {
            int randomCol = random.nextInt(MAX_COL);
            int randomRow = random.nextInt(MAX_ROW);

            if(maze[randomRow][randomCol] == 0) {
                int possibleExitX = randomCol * TILE_SIZE;
                int possibleExitY = randomRow * TILE_SIZE;

                if (possibleExitX != keyX || possibleExitY != keyY) {
                    exitX = possibleExitX;
                    exitY = possibleExitY;
                    break;
                }
            }
        }
    }
    public void retryGame() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose(); // Close current window

        SwingUtilities.invokeLater(() -> {
            JFrame newFrame = new JFrame("Depth of the Labyrinth");
            newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            newFrame.setResizable(false);
            GamePanel newPanel = new GamePanel();
            newFrame.add(newPanel);
            newFrame.pack();
            newFrame.setLocationRelativeTo(null);
            newFrame.setVisible(true);
        });
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

}
class MazeGenerator {
    private final int rows, cols;
    private final int[][] maze;

    public MazeGenerator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        maze = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(maze[i], 1);
        }
        generateMaze(1, 1);
        Random rand = new Random();
        int extraPassages = (rows * cols) / 5;

        for (int i = 0; i < extraPassages; i++) {
            int r = rand.nextInt(rows - 2) + 1;
            int c = rand.nextInt( cols - 2) + 1;

            if (maze[r][c] == 1) {
                if ((maze[r+ 1][c] == 0 && maze[r -1][c] ==0) ||
                        (maze[r][c+ 1] == 0 && maze[r][c -1] == 0)) {
                    maze[r][c] = 0;
                }
            }
        }
    }

    public int[][] getMaze() {
        return maze;
    }

    private void generateMaze(int r, int c) {
        int[] dirR = {-2, 0, 2, 0};
        int[] dirC = {0, 2, 0, -2};
        maze[r][c] = 0;

        Integer[] dirs = {0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(dirs));

        for (int i : dirs) {
            int nr = r + dirR[i];
            int nc = c + dirC[i];
            if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1 && maze[nr][nc] == 1) {
                maze[r + dirR[i] / 2][c + dirC[i] / 2] = 0;
                generateMaze(nr, nc);
            }
        }
    }
}
