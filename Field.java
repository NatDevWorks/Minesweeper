package minesweeper;

import java.util.*;

/**
 *  {@code Field} is class that holds all the cells of a game instance.
 *  @version 2022020700
 *  @author Richard Barton
 */
public class Field {
    private static final int    defaultSize             = 16;
    private static final int    defaultBombCount        = -1;
    private final int   size;
    private final int   bombCount;
    private int         hiddenCells;
    private String      status;
    private boolean     firstMove;
    private final boolean firstClickSafe;

    /**
     *  The status returned by {@code getStatus()}
     *  when the game is lost.
     */
    public final static String          loserStatus     = "Boom!";
    /**
     *  The status returned by {@code getStatus()}
     *  when the game is won.
     */
    public final static String          winnerStatus    = "Winner!";

    /*
     *  This is the mine field.
     */
    private final Cell  field[][];

    /**
     *  Construct a mine field with the default size and
     *  number of bombs.
     */
    public      Field()
    {
        this(defaultSize, defaultBombCount);
    }

    /**
     *  Construct a mine field of the given size and with the
     *  given number of bombs.
     *  @param size number of cells on a side
     *  @param bombCount number of bombs to randomly place
     *  in the field
     */
    public Field(int size, int bombCount)
    {
        this(size, bombCount, false);
    }

    /**
     *  Construct a mine field of the given size and with the
     *  given number of bombs.
     *  @param size number of cells on a side
     *  @param bombCount number of bombs to randomly place
     *  in the field
     *  @param firstClickSafe whether the first exposed cell should
     *  be protected from containing a bomb
     */
    public Field(int size, int bombCount, boolean firstClickSafe)
    {
        int     maxCells;
        Random  ourRandom;

        if (size <= 0) {
            /*
             *  Gotta have at least one cell.
             */
            size = 1;
        }
        this.size = size;

        /*
         *  Figure out how many bombs we're going to lay out in
         *  the field.
         */
        maxCells = size * size;
        hiddenCells = maxCells;
        firstMove = true;
        this.firstClickSafe = firstClickSafe;
        ourRandom = new Random();
        if (bombCount < 0) {
            /*
             *  Given a negative number of bombs, choose a
             *  pseudo random number.
             */
            bombCount = ourRandom.nextInt((maxCells / 5) + 1) + 1;
        } else if (bombCount > maxCells) {
            /*
             *  Given more bombs than can fit, make the field
             *  nothing but bombs.
             */
            bombCount = maxCells;
        }
        this.bombCount = bombCount;

        /*
         *  Create the mine field and populate it.
         */
        field = new Cell[size][size];
        populateField(bombCount, -1, -1);
    }

    /*
     *  Lay out bombs and empty cells.  When safeRow and safeColumn
     *  are valid, no bomb will be placed at that first-click position.
     */
    private void populateField(int bombsToPlace, int safeRow,
                               int safeColumn)
    {
        int     maxCells;
        int     row;
        int     column;
        Random  ourRandom;

        maxCells = size * size;
        ourRandom = new Random();
        for (row = 0; (row < field.length); ++row) {
            Arrays.fill(field[row], null);
        }

        if (isValidCoordinate(safeRow, safeColumn) == true) {
            bombsToPlace = Math.min(bombsToPlace, maxCells - 1);
        }

        /*
         *  Lay out the bombs.
         */
        for (; (bombsToPlace > 0); --bombsToPlace) {
            int         i;
            int         j;

            /*
             *  Randomly place a bomb in any of the total number
             *  of cells.
             */
            i = ourRandom.nextInt(maxCells);
            row = 0;
            column = 0;
            /*
             *  Given that position, find a reasonable place for
             *  the bomb.  A reasonable place is a place that's
             *  null or has an Empty cell.  If the place isn't
             *  reasonable, increment our position and keep looking.
             */
            for (j = maxCells; (j > 0); --j, i = ++i % maxCells) {
                Cell    whichCell;

                /*
                 *  Translate the location into coordinates.
                 */
                row = i / size;
                column = i % size;
                if ((row == safeRow) && (column == safeColumn)) {
                    continue;
                }

                /*
                 *  Let's see what's at the coordinates.
                 */
                whichCell = field[row][column];
                if ((whichCell == null) ||
                    (whichCell instanceof Empty)) {
                    /*
                     *  Either nothing is there or an Empty cell is
                     *  there.  In ether case, we'll use this spot.
                     */
                    break;
                }

                /*
                 *  Must be a bomb already at this location, let's
                 *  iterate until we find a good spot.
                 */
            }

            placeBomb(this, row, column);
        }

        /*
         *  We've placed all the bombs and Empty cells in each
         *  bomb's adjacent cells.  Now, fill the rest of the
         *  cells with Emptys.
         */
        for (row = 0; (row < field.length); ++row) {
            for (column = 0; (column < field[row].length); ++column) {
                if (field[row][column] != null) {
                    /*
                     *  Something's already here.
                     */
                    continue;
                }

                field[row][column] = new Empty();
            }
        }
    }

    /*
     *  Return whether a coordinate is inside the field.
     */
    private boolean isValidCoordinate(int row, int column)
    {
        return((row >= 0) && (row < field.length) &&
               (column >= 0) && (column < field[row].length));
    }

    /*
     *  Place a bomb at the given row and column in the given field.
     */
    private void placeBomb(Field instance, int row, int column)
    {
        int     i;

        /*
         *  Let's put the bomb.
         */
        instance.field[row][column] = new Bomb();

        /*
         *  We have to count this bomb in all the adjacent cells.
         *  Visit the row before and after this bomb.
         */
        for (i = row - 1; (i <= row + 1); ++i) {
            int         j;

            if ((i < 0) || (i >= instance.size)) {
                /*
                 *  We're off the edge.
                 */
                continue;
            }

            /*
             *  Visit the column before and after this bomb.
             */
            for (j = column - 1; (j <= column + 1); ++j) {
                Cell    whichCell;

                if ((j < 0) || (j >= instance.size)) {
                    /*
                     *  We're off the edge.
                     */
                    continue;
                }

                /*
                 *  Check this cell adjacent to the new bomb.
                 */
                whichCell = instance.field[i][j];
                if (whichCell == null) {
                    /*
                     *  Nothing there.  Put an Empty cell there.
                     */
                    whichCell = new Empty();
                    instance.field[i][j] = whichCell;
                }

                /*
                 *  Increment the number of bombs adjacent to
                 *  this cell.
                 */
                whichCell.incrementAdjacentBombs();
            }
        }
    }

    /**
     *  Return the size of the mine field.
     *  @return Size of mine field.
     */
    public int  getSize()
    {
        return(size);
    }

    /**
     *  Return the number of bombs in the field.
     *  @return Number of bombs in the field.
     */
    public int getBombCount()
    {
        return(bombCount);
    }

    /**
     *  Return the number of cells that have not been exposed.
     *  @return Number of hidden cells.
     */
    public int getHiddenCells()
    {
        return(hiddenCells);
    }

    /**
     *  Return the number of currently flagged cells.
     *  @return Number of cells marked with a flag.
     */
    public int getFlagCount()
    {
        int     flags;

        flags = 0;
        for (Cell row[] : field) {
            for (Cell thisCell : row) {
                if (thisCell.isFlagged() == true) {
                    ++flags;
                }
            }
        }

        return(flags);
    }

    /**
     *  Get the type of a specific cell.
     *  @param row The row of the cell whose type to get.
     *  @param column The column of the cell whose type to get.
     *  @return The type of the given cell.
     */
    public String       getType(int row, int column)
    {
        /*
         *  Validate the given coordinates.
         */
        if ((row < 0) || (row >= field.length)) {
            return(null);
        }
        if ((column < 0) || (column >= field[row].length)) {
            return(null);
        }

        return(field[row][column].getType());
    }

    /**
     *  Expose all {@code Cell}s in the {@code Field}.
     *  Usually used when they win or step on a bomb.
     */
    public void exposeAll()
    {
        for (Cell row[] : field) {
            for (Cell thisCell : row) {
                thisCell.expose();
            }
        }
    }

    /*
     *  When they expose a cell with no adjacent bombs, expose
     *  all the adjacent empties.
     *  Beware, this is recursive.
     */
    private void
    exposeAdjacentEmpties(int row, int column)
    {
        int     i;
        int     j;
        Cell    thisCell;

        /*
         *  Validate the given coordinates.
         */
        if ((row < 0) || (row >= field.length)) {
            return;
        }
        if ((column < 0) || (column >= field[row].length)) {
            return;
        }

        thisCell = field[row][column];
        if (thisCell.getExposed() == true) {
            /*
             *  The cell's already exposed.
             */
            return;
        }

        thisCell.expose();
        --hiddenCells;
        if (thisCell.getAdjacentBombs() != 0) {
            /*
             *  This cell has adjacent bombs.
             */
            return;
        }

        /*
         *  This cell was not exposed and doesn't have any bombs
         *  adjacent to it.  So, recursively expose adjacent empties
         *  around this cell.
         */
        for (i = row - 1; (i <= row + 1); ++i) {
            for (j = column - 1; (j <= column + 1); ++j) {
                exposeAdjacentEmpties(i, j);
            }
        }
    }

    /**
     *  Expose a specific cell.
     *  @param row The row of the cell to expose.
     *  @param column The column of the cell to expose.
     *  @return Is the game finished.
     */
    public boolean      expose(int row, int column)
    {
        Cell    thisCell;

        /*
         *  Validate the given coordinates.
         */
        if (isValidCoordinate(row, column) == false) {
            return(false);
        }

        thisCell = field[row][column];
        if (thisCell.isFlagged() == true) {
            /*
             *  Flagged cells are protected from accidental exposure.
             */
            return(false);
        }

        if ((firstClickSafe == true) &&
            (firstMove == true) &&
            (thisCell instanceof Bomb) &&
            (bombCount < (size * size))) {
            /*
             *  Classic Minesweeper protects the first click.
             */
            populateField(bombCount, row, column);
            thisCell = field[row][column];
        }
        firstMove = false;

        if (thisCell.getAdjacentBombs() == 0) {
            /*
             *  This cell has no adjacent bombs.
             */
            exposeAdjacentEmpties(row, column);
        } else if (thisCell.getExposed() == false) {
            /*
             *  This cell is not exposed, yet.
             */
            thisCell.expose();
            --hiddenCells;
        }

        if (thisCell.getType().charAt(0) == Bomb.myType.charAt(0)) {
            /*
             *  A bomb was exposed.
             *  Game over.
             */
            status = loserStatus;
            return(true);
        }

        if (hiddenCells == bombCount) {
            /*
             *  All the empty cells have been exposed.
             *  Game over.
             */
            status = winnerStatus;
            return(true);
        }

        return(false);
    }

    /**
     *  Flag a specific cell.
     *  @param row The row of the cell to flag.
     *  @param column The column of the cell to flag.
     */
    public void flag(int row, int column)
    {
        /*
         *  Validate the given coordinates.
         */
        if (isValidCoordinate(row, column) == false) {
            return;
        }

        if (field[row][column].getExposed() == true) {
            return;
        }
        field[row][column].flag();
    }

    /**
     *  Mark a specific cell.
     *  @param row The row of the cell to mark.
     *  @param column The column of the cell to mark.
     */
    public void mark(int row, int column)
    {
        /*
         *  Validate the given coordinates.
         */
        if (isValidCoordinate(row, column) == false) {
            return;
        }

        if (field[row][column].getExposed() == true) {
            return;
        }
        field[row][column].mark();
    }

    /**
     *  Clear a specific cell's mark.
     *  @param row The row of the cell to clear.
     *  @param column The column of the cell to clear.
     */
    public void clearMark(int row, int column)
    {
        /*
         *  Validate the given coordinates.
         */
        if (isValidCoordinate(row, column) == false) {
            return;
        }

        field[row][column].clearMark();
    }

    /**
     *  Return the status of the game.
     *  null unless the game's over.
     *  @return The status of the game.
     */
    public String       getStatus()
    {
        return(status);
    }

    /**
     *  @return {@code String} representation of object
     */
    public String toString()
    {
        return(getClass().getName() +
               "[size=" + size +
               ",bombCount=" + bombCount + "]");
    }

    /*
     *  Helper method to find information in toString() return value.
     */
    private static String[]     findVariableInToString(String toString,
                                                       String value)
    {
        int     toStringLength;
        int     valueLength;
        int     foundAt;
        int     occurances;
        String  returnValue[];

        occurances = 0;
        returnValue = new String[8];

        toStringLength = toString.length();
        valueLength = value.length();
        for (foundAt = 0;
             (((foundAt = toString.indexOf(value, foundAt)) > 0) &&
              (occurances < returnValue.length));
             foundAt += valueLength) {
            if ((foundAt + valueLength) >= toStringLength) {
                /*
                 *  Not going past the end of toString.
                 */
                break;
            }

            if (Character.isLetterOrDigit(toString.charAt(foundAt +
                                                          valueLength))
                                    == false) {
                int     j;

                /*
                 *  The character following value isn't a letter or
                 *  digit.  This is a candidate.
                 */
                for (j = foundAt - 1; (j >= 0); --j) {
                    char    thisChar;

                    thisChar = toString.charAt(j);
                    if (Character.isJavaIdentifierPart(thisChar) ==
                            false) {
                        for (++j; (j < foundAt); ++j) {
                            thisChar = toString.charAt(j);
                            if (Character.
                                    isJavaIdentifierStart(thisChar) ==
                                        true) {
                                break;
                            }
                        }
                        break;
                    }
                }

                returnValue[occurances] =
                                toString.substring(j, foundAt);
                ++occurances;
            }
        }

        return((occurances > 0) ? returnValue : null);
    }

    /*
     *  Test a given mine field.
     */
    private static int
    testFieldLayout(int givenSize, int givenBombCount)
    {
        int     row;
        int     column;
        int     size;
        int     bombCount;
        int     errorCount;
        char    bombType;
        Field   fieldToTest;
        String  toString;
        String  variableName[];
        char    charInField[][];

        errorCount = 0;

        /*
         *  Instantiate the field to test and check its size.
         */
        fieldToTest = new Field(givenSize, givenBombCount);
        toString = "" + fieldToTest;
        System.out.println("Testing " + toString + " given size: " +
                           givenSize + "; bomb count: " +
                           givenBombCount);
        size = fieldToTest.getSize();
        if (givenSize <= 0) {
            if (size != 1) {
                System.out.println("**** ERROR:  size " + size +
                                   " should be " + 1);
                ++errorCount;
            }
        } else if (size != givenSize) {
                System.out.println("**** ERROR:  size " + size +
                                   " should be " + givenSize);
                ++errorCount;
        }

        /*
         *  Look for the size in the toString().
         */
        if ((variableName =
                findVariableInToString(toString, "=" + size))
                            == null) {
            System.out.println("*** ERROR *** Didn't find" +
                               " size (" + size +
                               ") in toString() value");
            ++errorCount;
        } else {
            int     j;

            System.out.print("size may be stored in ");
            for (j = 0; (j < variableName.length); ++j) {
                if (variableName[j] == null) {
                    break;
                }
                System.out.print(((j == 0) ? "" : " or ") +
                                 variableName[j]);
            }
            System.out.println();
        }

        /*
         *  Test mutators out of bounds.
         */
        System.out.println("Testing expose(-1, -1)");
        fieldToTest.expose(-1, -1);
        System.out.println("Testing expose(" + size + ", " + size +
                           ")");
        fieldToTest.expose(size, size);
        System.out.println("Testing flag(-1, -1)");
        fieldToTest.flag(-1, -1);
        System.out.println("Testing flag(" + size + ", " + size + ")");
        fieldToTest.flag(size, size);
        System.out.println("Testing mark(-1, -1)");
        fieldToTest.mark(-1, -1);
        System.out.println("Testing mark(" + size + ", " + size + ")");
        fieldToTest.mark(size, size);
        System.out.println("Testing clearMark(-1, -1)");
        fieldToTest.clearMark(-1, -1);
        System.out.println("Testing clearMark(" + size + ", " +
                           size + ")");
        fieldToTest.clearMark(size, size);

        /*
         *  Need to expose all the cells so we can check
         *  their contents.
         */
        fieldToTest.exposeAll();

        /*
         *  Get all the types of the cells so we can look at
         *  each as many times as necessary without extra calls
         *  to getType().
         */
        charInField = new char[size][size];
        for (row = 0; (row < size); ++row) {
            for (column = 0; (column < size); ++column) {
                charInField[row][column] =
                        fieldToTest.getType(row, column).charAt(0);
            }
        }

        /*
         *  Examine each cell.
         */
        bombCount = 0;
        bombType = Bomb.myType.charAt(0);
        for (row = 0; (row < size); ++row) {
            for (column = 0; (column < size); ++column) {
                int     i;
                int     j;
                char    cellContents;

                cellContents = charInField[row][column];
                if (cellContents == bombType) {
                    /*
                     *  We have a bomb.  Make sure everything around
                     *  is a digit.
                     */
                    ++bombCount;
                    for (i = row - 1; (i <= row + 1); ++i) {
                        if ((i < 0) || (i >= charInField.length)) {
                            continue;
                        }

                        for (j = column - 1; (j <= column + 1); ++j) {
                            char        adjacentContents;

                            if ((j < 0) ||
                                (j >= charInField[row].length)) {
                                continue;
                            }

                            adjacentContents = charInField[i][j];
                            if ((adjacentContents != bombType) &&
                                (Character.isDigit(adjacentContents) ==
                                                            false)) {
                                System.out.println("**** ERROR: " +
                                                   " Position " +
                                                   i + "x" + j +
                                                   " should be a count");
                                ++errorCount;
                            }
                        }
                    }

                    continue;
                }

                if (Character.isDigit(cellContents) == true) {
                    int         adjacentBombs;

                    /*
                     *  We have a count.  Count the bombs around us.
                     */
                    adjacentBombs = cellContents - '0';
                    for (i = row - 1; (i <= row + 1); ++i) {
                        if ((i < 0) || (i >= charInField.length)) {
                            continue;
                        }

                        for (j = column - 1; (j <= column + 1); ++j) {
                            if ((j < 0) ||
                                (j >= charInField[row].length)) {
                                continue;
                            }

                            if (charInField[i][j] == bombType) {
                                --adjacentBombs;
                            }
                        }
                    }
                    if (adjacentBombs != 0) {
                        System.out.println("**** ERROR: " +
                                           " Position " +
                                           row + "x" + column +
                                           " should be " +
                                           (cellContents -
                                            adjacentBombs));
                        ++errorCount;
                    }

                    continue;
                }

                System.out.println("**** ERROR: Don't know what's at " +
                                   row + "x" + column + ": " +
                                   fieldToTest.getType(row, column));
                ++errorCount;
            }
        }

        if (givenBombCount >= 0) {
            int         maxSize;

            maxSize = size * size;
            if (givenBombCount > maxSize) {
                if (bombCount != maxSize) {
                    System.out.println("**** ERROR:  bomb count " +
                                       bombCount + " should be " +
                                       maxSize);
                    ++errorCount;
                }
            } else if (givenBombCount >= 0) {
                if (bombCount != givenBombCount) {
                    System.out.println("**** ERROR:  bomb count " +
                                       bombCount + " should be " +
                                       givenBombCount);
                    ++errorCount;
                }
            }
        }

        /*
         *  Look for the bomb count in the toString().
         */
        if ((variableName =
                findVariableInToString(toString, "=" + bombCount))
                            == null) {
            System.out.println("*** ERROR *** Didn't find" +
                               " bombCount (" + bombCount +
                               ") in toString() value");
            ++errorCount;
        } else {
            int     j;

            System.out.print("bombCount may be stored in ");
            for (j = 0; (j < variableName.length); ++j) {
                if (variableName[j] == null) {
                    break;
                }
                System.out.print(((j == 0) ? "" : " or ") +
                                 variableName[j]);
            }
            System.out.println();
        }
        
        return(errorCount);
    }

    /*
     *  Given a Field and a pattern, see if the contents of the Cells
     *  in the field match those of the pattern.
     */
    private static int
    comparePattern(Field field, char pattern[][])
    {
        int     row;
        int     column;
        int     errorCount;

        errorCount = 0;
        for (row = 0; (row < pattern.length - 1); ++row) {
            for (column = 0;
                 (column < pattern[row].length - 1); ++column) {
                char    found;

                found = field.getType(row, column).charAt(0);
                if (found != pattern[row][column]) {
                    /*
                     *  Found a Cell that doesn't match the pattern.
                     *  Record the error.
                     */
                    System.out.println("****  ERROR: Position " +
                                       row + "x" + column +
                                       ": " + found + " should be " +
                                       pattern[row][column]);
                    ++errorCount;
                }
            }
        }

        return(errorCount);
    }

    /*
     *  Hand craft fields to test the exposure logic.
     */
    private static int
    exposureTest()
    {
        int     whichTest;
        int     errorCount;
        char    bombType        = Bomb.myType.charAt(0);
        char    unmarkType      = Cell.unmark.charAt(0);
        /*
         *  The row and column of the position to expose in the
         *  corresponding pattern in fieldPattern.
         */
        int     positionToExpose[][]    =
            {{1, 1},
             {1, 1},
             {1, 2},
             {2, 1},
             {2, 2},
             {2, 2},
             {1, 1},
             {3, 3},
             {0, 0},
             {4, 4},
             {0, 0},
             {0, 0},
            };
        /*
         *  Strategically place bombs to test the adjacent exposure
         *  logic.  Each 2D array has a row and column position
         *  to be exposed in the corresponding entry in
         *  positionToExpose.
         */
        char    fieldPattern[][][]      =
            {{{bombType, bombType, bombType},
              {bombType, '8', bombType},
              {bombType, bombType, bombType}},
             {{bombType, bombType, bombType, bombType},
              {bombType, '5', unmarkType, bombType},
              {bombType, unmarkType, unmarkType, bombType},
              {bombType, bombType, bombType, bombType}},
             {{bombType, bombType, bombType, bombType},
              {bombType, unmarkType, '5', bombType},
              {bombType, unmarkType, unmarkType, bombType},
              {bombType, bombType, bombType, bombType}},
             {{bombType, bombType, bombType, bombType},
              {bombType, unmarkType, unmarkType, bombType},
              {bombType, '5', unmarkType, bombType},
              {bombType, bombType, bombType, bombType}},
             {{bombType, bombType, bombType, bombType},
              {bombType, unmarkType, unmarkType, bombType},
              {bombType, unmarkType, '5', bombType},
              {bombType, bombType, bombType, bombType}},
             {{bombType, bombType, bombType, bombType, bombType},
              {bombType, '5', '3', '5', bombType},
              {bombType, '3', '0', '3', bombType},
              {bombType, '5', '3', '5', bombType},
              {bombType, bombType, bombType, bombType, bombType}},
             {{bombType, bombType, bombType, bombType, bombType},
              {bombType, '5', unmarkType, unmarkType, bombType},
              {bombType, unmarkType, unmarkType, unmarkType, bombType},
              {bombType, unmarkType, unmarkType, unmarkType, bombType},
              {bombType, bombType, bombType, bombType, bombType}},
             {{bombType, bombType, bombType, bombType, bombType},
              {bombType, unmarkType, unmarkType, unmarkType, bombType},
              {bombType, unmarkType, unmarkType, unmarkType, bombType},
              {bombType, unmarkType, unmarkType, '5', bombType},
              {bombType, bombType, bombType, bombType, bombType}},
             {{'0', '0', '2', bombType, unmarkType},
              {'0', '0', '3', bombType, unmarkType},
              {'2', '3', '5', bombType, unmarkType},
              {bombType, bombType, bombType, bombType, unmarkType},
              {unmarkType, unmarkType, unmarkType,
                                            unmarkType, unmarkType}},
             {{unmarkType, unmarkType, unmarkType,
                                            unmarkType, unmarkType},
              {unmarkType, bombType, bombType, bombType, bombType},
              {unmarkType, bombType, '5', '3', '2'},
              {unmarkType, bombType, '3', '0', '0'},
              {unmarkType, bombType, '2', '0', '0'}},
             {{'b', '1'},
              {'1', '1'}},
             {{'0', '0'},
              {'0', '0'}},
            };

        assert (positionToExpose.length == fieldPattern.length);

        errorCount = 0;
        for (whichTest = 0; (whichTest < fieldPattern.length);
                                                        ++whichTest) {
            int         testSize;
            int         row;
            int         column;
            int         rowToExpose;
            int         columnToExpose;
            int         leftToExpose;
            char        thisTest[][];
            char        typeBeingExposed;
            boolean     gameOver;
            String      gameStatus;
            Field       exposureField;

            /*
             *  Collect information about this test.
             */
            thisTest = fieldPattern[whichTest];
            testSize = thisTest.length;
            rowToExpose = positionToExpose[whichTest][0];
            columnToExpose = positionToExpose[whichTest][1];
            typeBeingExposed = thisTest[rowToExpose][columnToExpose];
            leftToExpose = testSize * testSize;

            /*
             *  Create a field with no bombs of the proper size.
             */
            exposureField = new Field(testSize, 0);

            /*
             *  The bombs in the pattern tell us where to place
             *  a bomb in the field.
             */
            System.out.println("Exposing position " + rowToExpose +
                               "x" + columnToExpose +
                               " in the following pattern");
            for (row = 0; (row < testSize); ++row) {
                System.out.print("  ");
                for (column = 0; (column < testSize); ++column) {
                    System.out.print(thisTest[row][column]);
                    if (Character.isDigit(thisTest[row][column]) ==
                                                                true) {
                        /*
                         *  This should get exposed.
                         */
                        --leftToExpose;
                    }
                    if (thisTest[row][column] != bombType) {
                        continue;
                    }

                    exposureField.placeBomb(exposureField,
                                            row, column);

                    if ((row != rowToExpose) ||
                        (column != columnToExpose)) {
                        /*
                         *  Since adjacent bombs should not be exposed,
                         *  replace the bomb in the pattern with an
                         *  unmark so the comparison will work.
                         */
                        thisTest[row][column] = unmarkType;
                    } else {
                        /*
                         *  We're planning to expose this bomb.
                         */
                        --leftToExpose;
                    }
                }
                System.out.println();
            }

            /*
             *  Now that all the bombs are placed, expose the
             *  cell for this test.
             */
            gameOver = exposureField.expose(rowToExpose,
                                            columnToExpose);
            if (typeBeingExposed != bombType) {
                if (gameOver == true) {
                    if (leftToExpose > 0) {
                        System.out.println("**** ERROR: " +
                                           " Game shouldn't be over");
                        ++errorCount;
                    }
                    if (((gameStatus =
                                exposureField.getStatus()) == null) ||
                        (gameStatus.equals(winnerStatus) != true)) {
                        System.out.println("**** ERROR:  Game status" +
                                           " should be " +
                                           winnerStatus);
                        ++errorCount;
                    }
                }
            } else {
                if (gameOver != true) {
                    System.out.println("**** ERROR:  Game should" +
                                       " be over");
                    ++errorCount;
                } else if (((gameStatus =
                                exposureField.getStatus()) == null) ||
                           (gameStatus.equals(loserStatus) != true)) {
                    System.out.println("**** ERROR:  Game status" +
                                       " should be " + loserStatus);
                    ++errorCount;
                }
            }

            /*
             *  Make sure the correct cells were exposed.
             */
            errorCount += comparePattern(exposureField, thisTest);
        }

        return(errorCount);
    }

    /*
     *  Test behavior added for the graphical game.
     */
    private static int
    gameplayPolishTest()
    {
        int     errorCount;
        Field   safeField;
        Field   flagField;

        errorCount = 0;

        /*
         *  First-click safety should prevent an immediate loss even
         *  on a nearly full field.
         */
        safeField = new Field(2, 3, true);
        safeField.expose(0, 0);
        if (loserStatus.equals(safeField.getStatus()) == true) {
            System.out.println("**** ERROR: First-click safety failed");
            ++errorCount;
        }

        /*
         *  A flagged cell should not be exposed until the flag is
         *  cleared.
         */
        flagField = new Field(1, 0);
        flagField.flag(0, 0);
        flagField.expose(0, 0);
        if (flagField.getHiddenCells() != 1) {
            System.out.println("**** ERROR: Flagged cell was exposed");
            ++errorCount;
        }
        if (flagField.getFlagCount() != 1) {
            System.out.println("**** ERROR: Flag count should be 1");
            ++errorCount;
        }
        flagField.clearMark(0, 0);
        flagField.expose(0, 0);
        if (winnerStatus.equals(flagField.getStatus()) == false) {
            System.out.println("**** ERROR: Cleared safe cell should win");
            ++errorCount;
        }

        return(errorCount);
    }

    /**
     *  Performs a unit test on the {@code Field} class
     *  by instantiating several different fields
     *  and testing the methods.
     *  @param args arguments to the unit test
     */
    public static void main(String[] args)
    {
        int     errors;
        int     fakeSize;
        int     fakeBombCount;

        /*
         *  Test combinations of sizes and bomb counts.
         */
        errors = 0;
        for (fakeSize = -1; (fakeSize < 9); ++fakeSize) {
            for (fakeBombCount = (fakeSize * fakeSize) + 1;
                 (fakeBombCount >= -1); --fakeBombCount) {
                errors += testFieldLayout(fakeSize, fakeBombCount);
            }
        }

        /*
         *  Test combinations of bomb layouts and expose a cell
         *  to see what our logic exposes.
         */
        errors += exposureTest();
        errors += gameplayPolishTest();

        if (errors > 0) {
            /*
             *  We found a problem during unit test.
             */
            System.out.println("\nUNIT TEST FAILED! with " +
                               errors + " errors");
            System.exit(1);
        }
    }
}
