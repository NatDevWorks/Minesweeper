package minesweeper;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import javax.swing.*;

/**
 *  {@code Graphical} is class that implements playing the game
 *  graphically.
 *  @version 2026062400
 *  @author Nathan Douangkesone
 */
public class Graphical
{
    /**
     *  Prevent construction of this launcher class.
     */
    private Graphical()
    {
    }

    /**
     *  The entry point to the graphical game.
     *  @param arg Command line arguments.
     */
    public static void main(String arg[])
    {
        EventQueue.invokeLater(() -> {
                FieldFrame      frame;

                frame = new FieldFrame();

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setTitle("Minesweeper");
                frame.setVisible(true);
            });
    }
}

/**
 *  {@code Difficulty} stores the board settings available from the
 *  graphical menu.
 */
enum Difficulty
{
    EASY("Easy", 9, 10),
    MEDIUM("Medium", 16, 40),
    HARD("Hard", 24, 99);

    private final String label;
    private final int size;
    private final int bombs;

    /**
     *  Construct a difficulty preset.
     *  @param label Display name.
     *  @param size Number of cells on each side.
     *  @param bombs Number of bombs.
     */
    Difficulty(String label, int size, int bombs)
    {
        this.label = label;
        this.size = size;
        this.bombs = bombs;
    }

    /**
     *  Return the display label.
     *  @return Difficulty label.
     */
    public String getLabel()
    {
        return(label);
    }

    /**
     *  Return the board size.
     *  @return Number of cells on each side.
     */
    public int getSize()
    {
        return(size);
    }

    /**
     *  Return the bomb count.
     *  @return Number of bombs.
     */
    public int getBombs()
    {
        return(bombs);
    }
}

/**
 *  {@code BestTimes} stores and retrieves best game times by difficulty.
 */
class BestTimes
{
    private static final Preferences preferences =
            Preferences.userNodeForPackage(BestTimes.class);

    /**
     *  Return the saved best time for a difficulty.
     *  @param difficulty Difficulty to look up.
     *  @return Best time in seconds, or zero when none is saved.
     */
    public static int getBestTime(Difficulty difficulty)
    {
        return(preferences.getInt(difficulty.name(), 0));
    }

    /**
     *  Save a time when it is better than the previous saved time.
     *  @param difficulty Difficulty that was completed.
     *  @param seconds Finished time in seconds.
     *  @return {@code true} when the saved best time changed.
     */
    public static boolean recordWin(Difficulty difficulty, int seconds)
    {
        int     previousBest;

        previousBest = getBestTime(difficulty);
        if ((previousBest != 0) && (seconds >= previousBest)) {
            return(false);
        }

        preferences.putInt(difficulty.name(), seconds);
        return(true);
    }
}

/**
 *  {@code FieldFrame} is the class that contains the {@code JFrame}
 *  of the field.
 *  @version 2026062400
 *  @author Nathan Douangkesone
 */
class FieldFrame extends JFrame
{
    private final JPanel        panel;
    private final GridBagConstraints constraints;
    private final JLabel       elapsedSecondsLabel;
    private final JLabel       flagCountLabel;
    private final JLabel       statusLabel;
    private final JButton      startStopButton;
    private FieldComponent     fieldComponent;
    private Difficulty         difficulty;

    /**
     *  Construct the graphical frame for the field.
     */
    public FieldFrame()
    {
        GridBagLayout       layout;

        difficulty = Difficulty.MEDIUM;
        panel = new JPanel();
        layout = new GridBagLayout();
        panel.setLayout(layout);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 4);

        setJMenuBar(createMenuBar());

        elapsedSecondsLabel = new JLabel("0 seconds");
        flagCountLabel = new JLabel();
        statusLabel = new JLabel();
        startStopButton = new JButton("Start");

        addStatusBar();
        add(panel);
        newGame(difficulty);
    }

    /*
     *  Add labels and the pause/resume button to the frame.
     */
    private void addStatusBar()
    {
        JPanel      topPanel;

        topPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        topPanel.add(elapsedSecondsLabel);
        topPanel.add(flagCountLabel);
        topPanel.add(statusLabel);

        constraints.gridy = 0;
        panel.add(topPanel, constraints);

        constraints.gridy = 2;
        panel.add(startStopButton, constraints);
    }

    /*
     *  Build the menu used for new games, difficulty, and help.
     */
    private JMenuBar createMenuBar()
    {
        JMenuBar    menuBar;
        JMenu       gameMenu;
        JMenu       difficultyMenu;
        JMenu       helpMenu;
        JMenuItem   newGameItem;
        JMenuItem   exitItem;
        JMenuItem   aboutItem;

        menuBar = new JMenuBar();
        gameMenu = new JMenu("Game");
        difficultyMenu = new JMenu("Difficulty");
        helpMenu = new JMenu("Help");

        newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(event -> newGame(difficulty));
        gameMenu.add(newGameItem);

        for (Difficulty option : Difficulty.values()) {
            JMenuItem   optionItem;

            optionItem = new JMenuItem(option.getLabel());
            optionItem.addActionListener(event -> newGame(option));
            difficultyMenu.add(optionItem);
        }

        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> dispose());
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event ->
                JOptionPane.showMessageDialog(this,
                    "Minesweeper\nLeft-click to expose a cell.\n" +
                    "Right-click cycles flag, question mark, and clear.",
                    "About Minesweeper",
                    JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(difficultyMenu);
        menuBar.add(helpMenu);

        return(menuBar);
    }

    /**
     *  Start a fresh game at the requested difficulty.
     *  @param newDifficulty Difficulty to use for the new board.
     */
    public void newGame(Difficulty newDifficulty)
    {
        difficulty = newDifficulty;
        if (fieldComponent != null) {
            fieldComponent.disposeTimer();
            panel.remove(fieldComponent);
        }

        fieldComponent = new FieldComponent(this, elapsedSecondsLabel,
                                            flagCountLabel, statusLabel,
                                            startStopButton, difficulty);
        constraints.gridy = 1;
        panel.add(fieldComponent, constraints);
        statusLabel.setText(difficulty.getLabel());

        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
    }
}

/**
 *  {@code FieldComponent} is the class that contains the
 *  {@code JComponent} of the field.
 *  @version 2026062400
 *  @author Nathan Douangkesone
 */
class FieldComponent extends JComponent
{
    private final JFrame        ourFrame;
    private final Font          monospaceFont;
    private final JLabel        elapsedSecondsLabel;
    private final JLabel        flagCountLabel;
    private final JLabel        statusLabel;
    private final JButton       startStopButton;
    private final Difficulty    difficulty;
    private final Timer         tickTimer;
    private Field               field;
    private boolean             gameOver;
    private boolean             paused;
    private boolean             gameStart;
    private int                 elapsedSeconds;
    private JButton             buttonField[][];
    private FrameActive         frameListener;

    /*
     *  This is the class that manages a button in the field.
     */
    private class MouseHandler extends MouseAdapter
    {
        private final int   row;
        private final int   column;
        private int         whichMark;

        public MouseHandler(int row, int column)
        {
            /*
             *  Remember what button we're associated with.
             */
            this.row = row;
            this.column = column;
        }

        /*
         *  What to do when the mouse buttons are clicked.
         */
        public void mouseClicked(MouseEvent event)
        {
            if ((gameOver == true) || (paused == true && gameStart == true)) {
                return;
            }

            if (SwingUtilities.isLeftMouseButton(event) == true) {
                String  status;

                /*
                 *  Primary button pressed.  Expose the associated
                 *  cell.  The method tells us if the game is over.
                 */
                gameStart = true;
                startTicks();
                gameOver = field.expose(row, column);
                if ((status = field.getStatus()) != null) {
                    if (status.equals(Field.loserStatus) == true) {
                        buttonField[row][column].setBackground(Color.RED);
                    }
                    finishGame(status);
                }
            } else if (SwingUtilities.isRightMouseButton(event) == true) {
                gameStart = true;
                if (whichMark == 0) {
                    field.flag(row, column);
                    whichMark = 1;
                } else if (whichMark == 1) {
                    field.mark(row, column);
                    whichMark = 2;
                } else {
                    field.clearMark(row, column);
                    whichMark = 0;
                }
            }

            updateLabels();
            repaint();
        }
    }

    /*
     *  This class toggles the start/stop button.
     */
    private class StartStopAction implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            if (gameOver == true) {
                return;
            }

            if (paused == true) {
                startTicks();
            } else {
                stopTicks();
            }
        }
    }

    /*
     *  This class will catch window events.
     */
    private class FrameActive extends WindowAdapter
    {
        private boolean     stoppedWhenDeactivated;

        public void windowActivated(WindowEvent event)
        {
            if ((gameStart == true) && (stoppedWhenDeactivated == false)) {
                startTicks();
            }
        }

        public void windowDeactivated(WindowEvent event)
        {
            stoppedWhenDeactivated = paused;
            stopTicks();
        }
    }

    /**
     *  Construct the Component that contains and manages the game.
     *  @param ourFrame The frame that contains this component.
     *  @param elapsedSecondsLabel The label that displays the timer.
     *  @param flagCountLabel The label that displays flag usage.
     *  @param statusLabel The label that displays difficulty and status.
     *  @param startStopButton The button created by our frame.
     *  @param difficulty Difficulty settings for this game.
     */
    public FieldComponent(JFrame ourFrame,
                          JLabel elapsedSecondsLabel,
                          JLabel flagCountLabel,
                          JLabel statusLabel,
                          JButton startStopButton,
                          Difficulty difficulty)
    {
        this.ourFrame = ourFrame;
        this.elapsedSecondsLabel = elapsedSecondsLabel;
        this.flagCountLabel = flagCountLabel;
        this.statusLabel = statusLabel;
        this.startStopButton = startStopButton;
        this.difficulty = difficulty;

        gameOver = false;
        paused = true;
        gameStart = false;
        elapsedSeconds = 0;
        monospaceFont = new Font("Monospaced", Font.BOLD, 18);

        field = new Field(difficulty.getSize(), difficulty.getBombs(), true);
        buildButtons();

        tickTimer = new Timer(1000, event -> {
                if (paused == false) {
                    ++elapsedSeconds;
                    updateLabels();
                }
            });
        tickTimer.start();

        startStopButton.setText("Start");
        for (ActionListener listener : startStopButton.getActionListeners()) {
            startStopButton.removeActionListener(listener);
        }
        startStopButton.addActionListener(new StartStopAction());

        frameListener = new FrameActive();
        ourFrame.addWindowListener(frameListener);
        updateLabels();
    }

    /*
     *  Build the grid of buttons used as playable cells.
     */
    private void buildButtons()
    {
        int     size;
        int     row;

        size = field.getSize();
        setLayout(new GridLayout(size, size));
        buttonField = new JButton[size][size];
        for (row = 0; (row < size); ++row) {
            int     column;

            for (column = 0; (column < size); ++column) {
                JButton     thisButton;

                thisButton = new JButton(" ");
                thisButton.setPreferredSize(new Dimension(34, 34));
                thisButton.setMargin(new Insets(0, 0, 0, 0));
                thisButton.setFont(monospaceFont);
                thisButton.addMouseListener(new MouseHandler(row, column));
                buttonField[row][column] = thisButton;
                add(thisButton);
            }
        }
    }

    /**
     *  Stop the timer before this component is discarded.
     */
    public void disposeTimer()
    {
        tickTimer.stop();
        if (frameListener != null) {
            ourFrame.removeWindowListener(frameListener);
        }
    }

    /*
     *  Start timer.
     */
    private void startTicks()
    {
        if (paused == false) {
            return;
        }

        startStopButton.setText("Pause");
        statusLabel.setText(difficulty.getLabel());
        paused = false;
    }

    /*
     *  Stop timer.
     */
    private void stopTicks()
    {
        if (paused == true) {
            return;
        }

        startStopButton.setText("Resume");
        statusLabel.setText("Paused");
        paused = true;
    }

    /*
     *  Finish a won or lost game and display the result.
     */
    private void finishGame(String status)
    {
        String      message;
        boolean     newBest;

        gameOver = true;
        paused = true;
        field.exposeAll();
        startStopButton.setText("Game Over");
        statusLabel.setText(status);
        updateLabels();

        if (status.equals(Field.winnerStatus) == true) {
            newBest = BestTimes.recordWin(difficulty, elapsedSeconds);
            message = "You won in " + elapsedSeconds + " seconds.";
            if (newBest == true) {
                message += "\nNew best time for " +
                           difficulty.getLabel() + "!";
            }
        } else {
            message = "Boom! You lost.";
        }

        JOptionPane.showMessageDialog(ourFrame, message,
                                      "Game Over",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    /*
     *  Update the labels above the board.
     */
    private void updateLabels()
    {
        int     bestTime;

        bestTime = BestTimes.getBestTime(difficulty);
        elapsedSecondsLabel.setText(elapsedSeconds + " seconds");
        flagCountLabel.setText("Flags: " + field.getFlagCount() +
                               " / " + field.getBombCount());
        if ((gameOver == false) && (paused == false)) {
            if (bestTime > 0) {
                statusLabel.setText(difficulty.getLabel() +
                                    " | Best: " + bestTime + "s");
            } else {
                statusLabel.setText(difficulty.getLabel());
            }
        }
    }

    /*
     *  Apply text and color for a button based on cell state.
     */
    private void updateButtonAppearance(JButton thisButton,
                                        char displayChar,
                                        boolean enabled)
    {
        Color   foreground;

        foreground = Color.BLACK;
        thisButton.setEnabled(enabled);
        thisButton.setBackground(null);
        thisButton.setOpaque(true);

        if ((displayChar == Cell.unmark.charAt(0)) ||
            (displayChar == '0')) {
            displayChar = ' ';
        }

        if (Character.isDigit(displayChar) == true) {
            thisButton.setBackground(new Color(225, 225, 225));
            if (displayChar == '1') {
                foreground = new Color(0, 82, 180);
            } else if (displayChar == '2') {
                foreground = new Color(0, 128, 64);
            } else if (displayChar == '3') {
                foreground = Color.RED;
            } else {
                foreground = new Color(92, 42, 130);
            }
        } else if (displayChar == Cell.flag.charAt(0)) {
            foreground = new Color(170, 0, 0);
        } else if (displayChar == Bomb.myType.charAt(0)) {
            foreground = Color.BLACK;
            thisButton.setBackground(new Color(255, 205, 205));
        }

        thisButton.setForeground(foreground);
        thisButton.setText("" + displayChar);
    }

    /**
     *  Provide Swing a way to redraw our playing field.
     *  @param graphics Swing's information about the
     *  state of the graphics.
     */
    public void paintComponent(Graphics graphics)
    {
        int     size;
        int     row;
        Font    originalFont;

        /*
         *  Remember the font we were given and install the font
         *  we want for the game.
         */
        originalFont = graphics.getFont();
        graphics.setFont(monospaceFont);

        size = field.getSize();
        for (row = 0; (row < size); ++row) {
            int     column;

            for (column = 0; (column < size); ++column) {
                char        displayChar;
                JButton     thisButton;
                boolean     enabled;

                thisButton = buttonField[row][column];
                displayChar = field.getType(row, column).charAt(0);
                enabled = (gameOver == false) &&
                          (Character.isDigit(displayChar) == false);
                updateButtonAppearance(thisButton, displayChar, enabled);
            }
        }

        /*
         *  Restore the original font.
         */
        graphics.setFont(originalFont);
    }
}
