import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

// Goalkeeper class - handles the player-controlled goalkeeper
public class Goalkeeper {
    private int x; // current x position on screen
    private int y; // current y position on screen
    private int width; // width of goalkeeper
    private int height; // height of goalkeeper
    private JLabel label; // visual component that shows the goalkeeper
    private final int SPEED = 22; // how fast goalkeeper moves
    private String originalImagePath; // path to original image for resetting

    // Constructor - creates goalkeeper at starting position
    public Goalkeeper(int startX, int startY, String imagePath) {
        this.x = startX;
        this.y = startY;
        this.width = 300;
        this.height = 400;
        this.originalImagePath = imagePath;
        loadImage(imagePath);

    }

    // Load and scale the goalkeeper image
    private void loadImage(String imagePath) {
        ImageIcon goalieIcon = new ImageIcon(getClass().getResource(imagePath));
        Image goalieImage = goalieIcon.getImage();
        Image scaledGoalie = goalieImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledGoalieIcon = new ImageIcon(scaledGoalie);

        // Create new label if this is the first time, otherwise just update the image
        if (label == null) {
            label = new JLabel(scaledGoalieIcon); // First time - create the visual component
        } else {
            label.setIcon(scaledGoalieIcon); // Already exists - just change the image
        }
        label.setBounds(x, y, width, height); // Set position and size on screen
    }

    // Move goalkeeper left (with boundary checking)
    public void moveLeft() {
        if (x > 50) {
            x -= SPEED;
            updatePosition();
        }
    }

    // Move goalkeeper right (with boundary checking)
    public void moveRight() {
        if (x < 930) {
            x += SPEED;
            updatePosition();
        }
    }

    // Move goalkeeper up (with boundary checking)
    public void moveUp() {
        if (y > 50) {
            y -= SPEED;
            updatePosition();
        }
    }

    // Move goalkeeper down (with boundary checking)
    public void moveDown() {
        if (y < 400) {
            y += SPEED;
            updatePosition();
        }
    }

    // Make goalkeeper dive in specified direction with rotation
    public void dive(int direction) {
        try {
            // Reset to original image first
            loadImage(originalImagePath);

            ImageIcon originalIcon = (ImageIcon) label.getIcon();
            Image originalImage = originalIcon.getImage();

            // Convert to BufferedImage for rotation
            BufferedImage bufferedImage = new BufferedImage(
                    originalImage.getWidth(null),
                    originalImage.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D bGr = bufferedImage.createGraphics();
            bGr.drawImage(originalImage, 0, 0, null);
            bGr.dispose();

            // Set rotation angle and dive distance based on direction
            double rotationAngle = 0;
            int diveDistanceX = 0;
            int diveDistanceY = 0;
            switch (direction) {
                case -45: // Top Left dive (Q key)
                    rotationAngle = Math.toRadians(-45);  // Rotate player 45° counter-clockwise
                    diveDistanceX = -150;                 // Move 150 pixels left
                    diveDistanceY = -100;                 // Move 100 pixels up
                    break;

                case 45: // Top Right dive (E key)
                    rotationAngle = Math.toRadians(45);   // Rotate player  45° clockwise
                    diveDistanceX = 150;                  // Move 150 pixels right
                    diveDistanceY = -100;                 // Move 100 pixels up
                    break;

                case -135: // Bottom Left dive (Z key)
                    rotationAngle = Math.toRadians(-90);  // Rotate player 90° counter-clockwise
                    diveDistanceX = -180;                 // Move 180 pixels left
                    diveDistanceY = 30;                   // Move 30 pixels down
                    break;

                case 135: // Bottom Right dive (C key)
                    rotationAngle = Math.toRadians(90);   // Rotate player 90° clockwise
                    diveDistanceX = 180;                  // Move 180 pixels right
                    diveDistanceY = 30;                   // Move 30 pixels down
                    break;
            }
            // Calculate new image dimensions after rotation
            int w = bufferedImage.getWidth(); // Original width
            int h = bufferedImage.getHeight(); // Original Height
            double sin = Math.abs(Math.sin(rotationAngle)); // Complicated math used for rotation
            double cos = Math.abs(Math.cos(rotationAngle));
            int newW = (int) (w * cos + h * sin); // New width
            int newH = (int) (w * sin + h * cos); // New Height

            // Create rotated image
            BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = rotated.createGraphics();

            // Enable smooth rotation
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Set transparent background
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, newW, newH);
            g2d.setComposite(AlphaComposite.SrcOver);

            // Rotate around center and draw image
            g2d.translate(newW / 2, newH / 2);
            g2d.rotate(rotationAngle);
            g2d.translate(-w / 2, -h / 2);
            g2d.drawImage(bufferedImage, 0, 0, null);
            g2d.dispose();

            // Update label with rotated image
            label.setIcon(new ImageIcon(rotated));

            // Move goalkeeper in diving direction
            x += diveDistanceX;
            y += diveDistanceY;

            // Keep goalkeeper within screen bounds
            x = Math.max(0, Math.min(x, 1280 - width));
            y = Math.max(0, Math.min(y, 800 - height));

            updatePosition();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update the visual position of the goalkeeper
    private void updatePosition() {
        label.setBounds(x, y, width, height);
    }

    // Get smaller, more precise collision area for ball detection
    public Rectangle getCollisionBounds() {
        int collisionWidth = width * 19 / 40;
        int collisionHeight = height * 19 / 40;
        int collisionX = x + (width - collisionWidth) / 2;
        int collisionY = y + (height - collisionHeight) / 2;
        return new Rectangle(collisionX, collisionY, collisionWidth, collisionHeight);
    }

    // Check if goalkeeper is touching the ball
    public boolean isCollidingWithBall(Ball ball) {
        Rectangle goalieCollision = getCollisionBounds();
        Rectangle ballCollision = ball.getCollisionBounds();
        return goalieCollision.intersects(ballCollision);
    }

    // Get the visual component for adding to game window
    public JLabel getLabel() {
        return label;
    }

}