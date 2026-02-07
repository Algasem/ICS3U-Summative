import javax.swing.*;
import java.awt.*;
import javax.swing.ImageIcon;


public class MainMenu {

    private static JLabel imageLabel;

    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("Main menu");
        frame.setSize(1280, 800);  // Set window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Close program when X is clicked

        // Create layered pane to stack background image and buttons
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1280, 800));

        // Load the background image
        ImageIcon originalIcon = new ImageIcon(MainMenu.class.getResource("/SoccerGUI.png"));

        // Scale the background image to fit better
        Image originalImage = originalIcon.getImage();
        double widthScaleFactor = 0.85;   // Make image 85% of original width
        double heightScaleFactor = 1;     // Keep original height
        int scaledWidth = (int)(originalIcon.getIconWidth() * widthScaleFactor);
        int scaledHeight = (int)(originalIcon.getIconHeight() * heightScaleFactor);
        Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Create label to hold the background image
        imageLabel = new JLabel(scaledIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);  // Center the image horizontally
        imageLabel.setVerticalAlignment(JLabel.CENTER);    // Center the image vertically
        imageLabel.setBounds(0, 0, 1280, 800);  // Fill entire window

        // Create panel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 40));  // Center buttons with 40px spacing
        buttonPanel.setOpaque(false);  // Make panel transparent so background shows through

        // Create "Play" button
        JButton startButton = new JButton("Play");
        startButton.setPreferredSize(new Dimension(200,50));  // Set button size
        startButton.setFont(new Font("Arial", Font.BOLD, 16));  // Make text bold and bigger

        // Style the Play button with green color
        startButton.setBackground(new Color(50, 142, 40));  // Dark green background
        startButton.setForeground(Color.WHITE);  // White text
        startButton.setOpaque(true);  // Make sure background color shows
        startButton.setBorderPainted(false);  // Remove button border

        // Create "Instructions" button with same styling
        JButton instructionsButton = new JButton("Instructions");
        instructionsButton.setPreferredSize(new Dimension(200, 50));
        instructionsButton.setFont(new Font("Arial", Font.BOLD, 16));

        instructionsButton.setBackground(new Color(50, 142, 40));  // Same green as Play button
        instructionsButton.setForeground(Color.WHITE);
        instructionsButton.setOpaque(true);
        instructionsButton.setBorderPainted(false);

        // Add buttons to the panel
        buttonPanel.add(startButton);
        buttonPanel.add(instructionsButton);

        // Position the button panel at the bottom center of screen
        int buttonPanelWidth = 700;
        int buttonPanelHeight = 200;
        int buttonPanelX = (1280 - buttonPanelWidth) / 2;  // Center horizontally
        int buttonPanelY = 550;  // Position near bottom of screen

        buttonPanel.setBounds(buttonPanelX, buttonPanelY, buttonPanelWidth, buttonPanelHeight);

        // Add components to layered pane (background first, then buttons on top)
        layeredPane.add(imageLabel, JLayeredPane.DEFAULT_LAYER);  // Background layer
        layeredPane.add(buttonPanel, JLayeredPane.PALETTE_LAYER);  // Button layer (on top)

        // When Play button is clicked, close menu and start game
        startButton.addActionListener(e -> {
            frame.dispose();  // Close the main menu window
            new Game();       // Start the game
        });

        // When Instructions button is clicked, show instructions popup
        instructionsButton.addActionListener(e -> {
            // Load the instructions background image
            ImageIcon instructionsIcon = new ImageIcon(MainMenu.class.getResource("/Instructions.png"));

            // Create custom panel that draws background image
            JPanel instructionsPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw the background image to fill the entire panel
                    if (instructionsIcon.getImage() != null) {
                        g.drawImage(instructionsIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    }
                }
            };

            instructionsPanel.setLayout(new BorderLayout());
            instructionsPanel.setPreferredSize(new Dimension(918, 516));  // Set popup size

            // Create the instructions text using HTML for formatting
            String instructionsText =
                    "<html><div style='text-align: center; padding: 40px;'>" +
                            "<h1 style='color: black; font-weight: bold; font-size: 24px; margin-bottom: 20px;'>HOW TO PLAY</h1>" +
                            "<p style='color: black; font-weight: bold; font-size: 16px; line-height: 1.8;'>" +
                            "1. You are the goalkeeper and you're in a penalty shootout.<br><br>" +
                            "2. To move around, use WASD or your arrow keys.<br><br>" +
                            "3. Buttons for diving: Q: top-left, E: top-right, Z: bottom-left, C: bottom-right.<br><br>" +
                            "4. Use the spacebar to start the striker's shot.<br><br>" +
                            "5. First to 5 points wins!!" +
                            "</p></div></html>";

            // Create label to display the instructions text
            JLabel textLabel = new JLabel(instructionsText);
            textLabel.setHorizontalAlignment(JLabel.CENTER);  // Center text horizontally
            textLabel.setVerticalAlignment(JLabel.CENTER);    // Center text vertically

            instructionsPanel.add(textLabel, BorderLayout.CENTER);  // Add text to center of panel

            // Create popup dialog window for instructions
            JDialog dialog = new JDialog(frame, "Instructions", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);  // Close dialog when X clicked
            dialog.setResizable(false);
            dialog.add(instructionsPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);  // Center dialog on main window
            dialog.setVisible(true);  // Show the dialog
        });

        // Add layered pane to main window and show it
        frame.add(layeredPane);
        frame.setVisible(true);  // Make the main menu visible
    }
}