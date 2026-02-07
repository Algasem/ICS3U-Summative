import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;


// Main game class - handles everything that happens during the actual gameplay
public class Game extends JFrame implements KeyListener, ActionListener {
    private JLabel backgroundLabel; // the soccer field background image
    private JLayeredPane layeredPane; // lets us put things on top of each other (GUI)
    private Goalkeeper goalie; // the goalkeeper player controls
    private Ball ball; // the soccer ball that moves around
    private Timer gameTimer; // runs the game loop every 50ms
    private int saves = 0; // how many saves the goalie made this round
    private int goals = 0; // how many goals opponent scored this round
    private int wins = 0; // how many games the goalie won total
    private int losses = 0; // how many games the goalie lost total
    private String playerName; // player's name for leaderboard
    private JLabel scoreLabel; // shows saves and goals on screen
    private JLabel gameLabel; // shows wins and losses on screen
    private JLabel leaderboardLabel; // shows top 5 players

    private static final String LEADERBOARD_FILE = "leaderboard.txt"; // file where we save high scores

    // Constructor - sets up the whole game when created
    public Game() {
        getPlayerName();
        setupGUI();
        createGameObjects();
        setupGameTimer();
        updateLeaderboard();
    }

    // Ask the player what their name is
    private void getPlayerName() {
        playerName = JOptionPane.showInputDialog(this, "Enter your name:", "Player Name", JOptionPane.PLAIN_MESSAGE);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous"; // default name if they don't enter anything
        }
        playerName = playerName.trim(); // remove extra spaces
    }

    // Set up the game window and all the visual elements
    private void setupGUI() {
        setTitle("Soccer Game");
        setSize(1280, 800); // window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close program when window closes
        setFocusable(true); // so we can detect key presses
        addKeyListener(this); // listen for keyboard input

        // Create layered pane so we can stack things on top of each other
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1280, 800));

        // Load and scale the soccer field background image
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/SoccerNet.png"));
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(1280, 800, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        backgroundLabel = new JLabel(scaledIcon);
        backgroundLabel.setBounds(0, 0, 1280, 800);
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        // Score display in bottom right corner
        scoreLabel = new JLabel("<html>Saves: 0<br>Goals: 0</html>"); // Used HTML for better formatting
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 30));
        scoreLabel.setForeground(Color.WHITE); // white text
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(new Color(0, 0, 0, 150)); // semi-transparent black background
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scoreLabel.setBounds(1075, 700, 200, 80);
        layeredPane.add(scoreLabel, JLayeredPane.DRAG_LAYER);

        // Game stats in bottom left corner
        gameLabel = new JLabel("<html>Wins: 0<br>Losses: 0</html>");
        gameLabel.setFont(new Font("Arial", Font.BOLD, 30));
        gameLabel.setForeground(Color.WHITE);
        gameLabel.setOpaque(true);
        gameLabel.setBackground(new Color(0, 0, 0, 150));
        gameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gameLabel.setBounds(20, 700, 170, 80);
        layeredPane.add(gameLabel, JLayeredPane.DRAG_LAYER);

        // Leaderboard in top right corner
        leaderboardLabel = new JLabel();
        leaderboardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        leaderboardLabel.setForeground(Color.WHITE);
        leaderboardLabel.setOpaque(true);
        leaderboardLabel.setBackground(new Color(0, 0, 0, 150));
        leaderboardLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leaderboardLabel.setBounds(1050, 20, 220, 160);
        leaderboardLabel.setVerticalAlignment(SwingConstants.TOP);
        layeredPane.add(leaderboardLabel, JLayeredPane.DRAG_LAYER);

        add(layeredPane);
        setVisible(true); // show the window
    }

    // Create the goalie and ball objects
    private void createGameObjects() {
        // Create goalie at starting position
        goalie = new Goalkeeper(490, 275, "/SoccerGoalie.png");
        layeredPane.add(goalie.getLabel(), JLayeredPane.PALETTE_LAYER);

        // Create ball
        ball = new Ball("/Ball.png");
        layeredPane.add(ball.getLabel(), JLayeredPane.MODAL_LAYER);
    }

    // Start the game timer that runs the game loop
    private void setupGameTimer() {
        gameTimer = new Timer(50, this); // runs every 50ms
        gameTimer.start();
    }

    // Update the saves and goals display on screen
    private void updateScoreDisplay() {
        scoreLabel.setText("<html>Saves: " + saves + "<br>Goals: " + goals + "</html>");
    }

    // Update the wins and losses display on screen
    private void updateGameDisplay() {
        gameLabel.setText("<html>Wins: " + wins + "<br>Losses: " + losses + "</html>");
    }

    // Load leaderboard from file and update the display
    private void updateLeaderboard() {
        List<PlayerScore> leaderboard = loadLeaderboard();
        StringBuilder leaderboardText = new StringBuilder("<html><b>🏆 LEADERBOARD</b><br>");

        // Show top 5 players
        for (int i = 0; i < Math.min(5, leaderboard.size()); i++) {
            PlayerScore player = leaderboard.get(i);
            leaderboardText.append(String.format("%d. %s: %d wins<br>",
                    i + 1, player.name, player.wins));
        }

        // Fill empty spots with dashes if there aren't 5 players yet
        if (leaderboard.size() < 5) {
            for (int i = leaderboard.size(); i < 5; i++) {
                leaderboardText.append(String.format("%d. ---<br>", i + 1)); // we do i + 1 to start at 1 and not 0
            }
        }

        leaderboardText.append("</html>");
        leaderboardLabel.setText(leaderboardText.toString());
    }

    // Save current player's score to the leaderboard file (only name and wins)
    private void savePlayerScore() {
        List<PlayerScore> leaderboard = loadLeaderboard();

        // Check if this player already exists in leaderboard
        boolean playerFound = false;
        for (PlayerScore player : leaderboard) {
            if (player.name.equals(playerName)) {
                player.wins += wins; // Add current session wins to existing total
                playerFound = true;
                break;
            }
        }

        // If new player, add them to leaderboard
        if (!playerFound) {
            leaderboard.add(new PlayerScore(playerName, wins));
        }

        // Sort by wins (highest first)
        leaderboard.sort((a, b) -> Integer.compare(b.wins, a.wins));

        // Save only name and wins to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (PlayerScore player : leaderboard) {
                writer.write(player.name + "," + player.wins);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving leaderboard: " + e.getMessage());
        }
    }

    // Load all player scores from the leaderboard file (only name and wins)
    private List<PlayerScore> loadLeaderboard() {
        List<PlayerScore> leaderboard = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) { // Makes sure it's not empty
                String[] parts = line.split(","); // split by comma
                if (parts.length == 2) { // make sure we have name and wins
                    String name = parts[0];
                    int wins = Integer.parseInt(parts[1]);
                    leaderboard.add(new PlayerScore(name, wins));
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, first time running
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading leaderboard: " + e.getMessage());
        }

        // Sort by wins (highest first)
        leaderboard.sort((a, b) -> Integer.compare(b.wins, a.wins));
        return leaderboard;
    }

    // This is the method used to play my 2 sound effects (cheer and boo)
    private void playSound(String soundFilePath) {
        try {
            // Get the URL of the sound resource
            URL soundURL = getClass().getResource(soundFilePath);
            if (soundURL == null) {
                System.out.println("Sound file not found: " + soundFilePath);
                return;
            }

            // Load and play the clip
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    // Reset everything for a new game
    private void restartGame() {
        // Reset scores back to 0
        saves = 0;
        goals = 0;
        updateScoreDisplay();

        // Reset ball and goalie positions
        ball.reset();
        layeredPane.remove(goalie.getLabel());
        goalie = new Goalkeeper(490, 275, "/SoccerGoalie.png");

        // Remove old goalie and add new one
        layeredPane.add(goalie.getLabel(), JLayeredPane.PALETTE_LAYER);

        // Restart the game timer
        gameTimer.start();

        // Refresh the display
        repaint();
    }

    // Close this game and go back to main menu
    private void returnToMainMenu() {
        this.dispose(); // Close current game window
        SwingUtilities.invokeLater(() -> {
            MainMenu.main(new String[0]); // Return to main menu
        });
    }

    // Handle keyboard input - called when player presses a key
    @Override
    public void keyPressed(KeyEvent e) {
        // Only allow goalie movement when ball is moving
        if (ball.isMoving()) {
            // Basic movement with arrow keys or WASD
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    goalie.moveLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    goalie.moveRight();
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    goalie.moveUp();
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    goalie.moveDown();
                    break;
            }

            // Dive directions - more advanced moves
            switch (e.getKeyCode()) {
                case KeyEvent.VK_Q: // Top Left dive
                    goalie.dive(-45);
                    break;
                case KeyEvent.VK_E: // Top Right dive
                    goalie.dive(45);
                    break;
                case KeyEvent.VK_Z: // Bottom Left dive
                    goalie.dive(-135);
                    break;
                case KeyEvent.VK_C: // Bottom Right dive
                    goalie.dive(135);
                    break;
            }
        }

        // Start ball shot with space bar
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            ball.startShot();
        }
    }

    // Game loop - runs every 50ms while game is running
    @Override
    public void actionPerformed(ActionEvent e) {
        // Update ball position and check if it finished moving
        boolean ballFinished = ball.update();

        // Check for goal or save when ball stops moving
        if (ballFinished) {
            checkGoalOrSave();
        }
        repaint(); // redraw everything on screen
    }

    // Check if goalie saved the ball or if it was a goal
    private void checkGoalOrSave() {
        boolean collision = goalie.isCollidingWithBall(ball);
        if (collision) {
            // When there's a collision, it's a save
            playSound("/SoccerCheering.wav");
            saves++;
            JOptionPane.showMessageDialog(this, "SAVE! Great job!");
        }
        else {
            // When there isn't a collision, it's a goal
            playSound("/SoccerBoo.wav");
            goals++;
            JOptionPane.showMessageDialog(this, "GOAL! The ball got past you!");
        }

        updateScoreDisplay();

        // Check if game is over - first to 5 wins
        if (saves == 5) {
            gameTimer.stop(); // stop the game loop
            wins++;
            updateGameDisplay();
            savePlayerScore(); // Save only name and wins to file
            updateLeaderboard(); // Update leaderboard display

            // Ask if player wants to play again
            int choice = JOptionPane.showConfirmDialog(this, "Congratulations, you WON!!\n Would you like to play again?", "Win!",
                    JOptionPane.YES_NO_OPTION);

            // Lets the user play again
            if (choice == JOptionPane.YES_OPTION) {
                restartGame();
            }
            // returns user to the main menu
            else {
                returnToMainMenu();
            }
        } else if (goals == 5) {
            gameTimer.stop(); // stop the game loop
            losses++;
            updateGameDisplay();
            updateLeaderboard(); // Update leaderboard display

            // Ask if player wants to play again
            int choice = JOptionPane.showConfirmDialog(this, "Game Over. You lost.\n Would you like to play again?", "Lose.",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                returnToMainMenu();
            }
        } else {
            // Game continues - reset goalie and ball for next shot
            layeredPane.remove(goalie.getLabel());
            goalie = new Goalkeeper(490, 275, "/SoccerGoalie.png");
            layeredPane.add(goalie.getLabel(), JLayeredPane.PALETTE_LAYER);

            // Reset ball for next shot
            ball.reset();

            repaint();
        }
    }

    // Required by KeyListener but we don't need it
    @Override
    public void keyReleased(KeyEvent e) {

    }

    // Required by KeyListener but we don't need it
    @Override
    public void keyTyped(KeyEvent e) {

    }

    // Helper class to store player name and their wins
    private static class PlayerScore { // I left it in the game class since it was so short
        String name; // player's name
        int wins; // how many games they won

        // Constructor to create a new player score (only name and wins saved to file)
        PlayerScore(String name, int wins) {
            this.name = name;
            this.wins = wins;
        }
    }

    // Main method - starts the game when you run the program
    public static void main(String[] args) {
        new Game();
    }
}