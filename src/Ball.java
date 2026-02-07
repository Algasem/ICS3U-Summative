import javax.swing.*;
import java.awt.*;
import java.util.Random;

// This is the Ball class that handles the soccer ball movement
class Ball {
    private int x; // ball's x position
    private int y; // ball's y position
    private int targetX; // where the ball is trying to go (x)
    private int targetY; // where the ball is trying to go (y)
    private int width; // how wide the ball is
    private int height; // how tall the ball is
    private JLabel label; // the actual image of the ball
    private boolean isMoving; // boolean variable to check if the ball is moving
    private final int SPEED = 24; // how fast the ball moves
    private final int START_X = 580; // starting x position (center)
    private final int START_Y = 705; // starting y position (bottom)

    // 8 possible shot positions
    private int[][] shotPositions = {
            {310, 260},   // Top left corner
            {810, 250},   // Top right corner
            {300, 350},   // Middle left
            {810, 350},   // Middle right
            {300, 450},   // Bottom left
            {810, 450},   // Bottom right
            {580, 200},   // Top middle
            {580, 465}    // Bottom middle
    };

    private Random random = new Random(); // for picking random shot positions

    // Constructor - creates a new ball with an image
    public Ball(String imagePath) {
        this.x = START_X;
        this.y = START_Y;
        this.width = 120;
        this.height = 120;
        this.isMoving = false;

        // Load and scale ball image to the right size
        ImageIcon ballIcon = new ImageIcon(getClass().getResource(imagePath));
        Image ballImage = ballIcon.getImage();
        Image scaledBall = ballImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledBallIcon = new ImageIcon(scaledBall);

        // Create the label that shows the ball on screen
        label = new JLabel(scaledBallIcon);
        label.setBounds(x, y, width, height);
    }

    // This method starts the ball moving to a random position
    public void startShot() {
        if (!isMoving) { // only start if ball isn't already moving
            isMoving = true;
            // Reset to starting position first
            x = START_X;
            y = START_Y;

            // Pick a random target position from our array
            int randomShot = random.nextInt(shotPositions.length);
            targetX = shotPositions[randomShot][0];
            targetY = shotPositions[randomShot][1];

        }
    }

    // This method moves the ball each frame - returns true when ball reaches target
    public boolean update() {
        if (isMoving) {
            // Calculate how far we need to move in x and y
            int deltaX = targetX - x;
            int deltaY = targetY - y;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY); // pythagorean theorem

            if (distance > SPEED) {
                // Move towards target at our speed
                x += (int) ((deltaX / distance) * SPEED);
                y += (int) ((deltaY / distance) * SPEED);
                label.setBounds(x, y, width, height); // update position on screen
                return false; // Still moving
            } else {
                // We've reached the target - snaps to exact position
                x = targetX;
                y = targetY;
                label.setBounds(x, y, width, height);
                isMoving = false;
                return true; // Finished moving
            }
        }
        return false;
    }

    // Reset ball back to the starting position
    public void reset() {
        x = START_X;
        y = START_Y;
        isMoving = false;
        label.setBounds(x, y, width, height);
    }

    // Creates a rectangular collision area for the ball (used for detection of collision)
    public Rectangle getCollisionBounds() {
        int collisionSize = Math.min(width, height) * 22 / 40; // Make collision area smaller and rectangle

        // Center the collision rectangle within the ball image
        int collisionX = x + (width - collisionSize) / 2;
        int collisionY = y + (height - collisionSize) / 2;

        return new Rectangle(collisionX, collisionY, collisionSize, collisionSize);
    }

    // Returns the JLabel so other classes can add it to the screen
    public JLabel getLabel() {
        return label;
    }

    // Check if the ball is currently moving
    public boolean isMoving() {
        return isMoving;
    }
}