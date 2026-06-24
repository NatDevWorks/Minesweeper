package minesweeper;

/**
 *  {@code Cell} is an abstract class defining the contents of
 *  a cell.
 *  @version 2022020700
 *  @author Richard Barton
 */
public abstract class Cell {
    private final String        type;
    private boolean             exposed;
    private String              marked;
    /**
     *  This will be the value returned by {@code getType()} for
     *  any unexposed {@code Cell} that's been "flagged".
     */
    public static final String  flag    = "X";
    /**
     *  This will be the value returned by {@code getType()} for
     *  any unexposed {@code Cell} that's been "marked".
     */
    public static final String  mark    = "?";
    /**
     *  This will be the value returned by {@code getType()} for
     *  any unexposed {@code Cell} that's not "flagged" or "marked".
     */
    public static final String  unmark  = "-";
    private int                 adjacentBombs;

    /**
     *  Assign a type to a {@code Cell}.
     *  Types of {@code Cell}s are stored at the time they are
     *  instantiated.
     *  @param type required type of {@code Cell}
     */
    public Cell(String type)
    {
        this.type = type;
        exposed = false;
        marked = null;
    }

    /**
     *  Return the type of the {@code Cell} when exposed.
     *  This method will return the type of the {@code Cell}
     *  when the {@code Cell} is exposed.
     *  When the {@code Cell} is not exposed, it'll return
     *  any mark the player may have given the {@code Cell}.
     *  If not marked, then just return {@code unmark}.
     *  @return the type of the cell
     */
    public String       getType()
    {
        if (exposed == true) {
            /*
             *  This cell is exposed so return the type.
             */
            return(type);
        }

        if (marked != null) {
            /*
             *  This cell is marked so return the mark.
             */
            return(marked);
        }

        /*
         *  Not exposed or marked.
         */
        return(Cell.unmark);
    }

    /**
     *  Increment the number of adjacent bombs.
     */
    public void incrementAdjacentBombs()
    {
        ++adjacentBombs;
    }

    /**
     *  Return the number of adjacent bombs
     *  @return the number of adjacent bombs
     */
    public int  getAdjacentBombs()
    {
        return(adjacentBombs);
    }

    /**
     *  Expose the contents of the {@code Cell}.
     */
    public void expose()
    {
        exposed = true;
        marked = null;
    }

    /**
     *  Flag the {@code Cell} as containing a bomb.
     */
    public void flag()
    {
        marked = Cell.flag;
    }

    /**
     *  Mark the {@code Cell} as might contain a bomb.
     */
    public void mark()
    {
        marked = Cell.mark;
    }

    /**
     *  Clear any mark.
     */
    public void clearMark()
    {
        marked = null;
    }

    /**
     *  Return whether the {@code Cell} is exposed.
     *  @return whether the {@code Cell} is exposed.
     */
    public boolean      getExposed()
    {
        return(exposed);
    }

    /**
     *  Return whether the {@code Cell} is currently flagged.
     *  @return whether the cell has a flag mark.
     */
    public boolean      isFlagged()
    {
        return(Cell.flag.equals(marked));
    }

    /**
     *  Return whether the {@code Cell} is currently question marked.
     *  @return whether the cell has a question mark.
     */
    public boolean      isMarked()
    {
        return(Cell.mark.equals(marked));
    }

    /**
     *  Return {@code String} representation of the {@code Cell}.
     *  @return {@code String} representation of the {@code Cell}.
     */
    public String toString()
    {
        return(getClass().getName() + "[type=" + type + "]");
    }

    private static      int
    checkCellType(Cell which, String expected)
    {
        String      cellType;

        cellType = which.getType();
        if (cellType.equals(expected) == true) {
            return(0);
        }

        /*
         *  We didn't expect what getType() returned.
         */
        System.out.println("*** ERROR *** " + which +
                           ".getType(): \"" +
                           cellType + "\" != \"" + expected + "\"");

        return(1);
    }

    /**
     *  Performs a unit test on the {@code Cell} class
     *  by instantiating all subclasses and testing the
     *  methods.
     *  @param args arguments to the unit test
     */
    public static void main(String[] args)
    {
        int     errors;
        Cell    cell[]  = {new Bomb(),
                           new Empty()};

        errors = 0;
        for (Cell which : cell) {
            int         i;
            String      toString;
            String      type;
            String      exposedType;

            /*
             *  See what toString() does for each object.
             */
            toString = "" + which;
            System.out.println(toString);

            if (which.getExposed() == true) {
                /*
                 *  The cell should start out as not exposed.
                 */
                System.out.println("*** ERROR *** " + which +
                                   " should be unexposed.");
                ++errors;
            }

            errors += checkCellType(which, Cell.unmark);

            which.flag();
            errors += checkCellType(which, Cell.flag);

            which.mark();
            errors += checkCellType(which, Cell.mark);

            which.clearMark();
            errors += checkCellType(which, Cell.unmark);

            /*
             *  Now expose the cell.
             */
            which.expose();
            if (which.getExposed() != true) {
                /*
                 *  The cell should be exposed.
                 */
                System.out.println("*** ERROR *** " + which +
                                   " should be exposed.");
                ++errors;
            }

            /*
             *  See if the toString() return value is formatted
             *  correctly.  Look for the type preceded by an = .
             */
            type = "unknown";
            if (which instanceof Empty) {
                type = Empty.myType;
            } else if (which instanceof Bomb) {
                type = Bomb.myType;
            }
            if ((i = toString.indexOf("=" + type)) < 0) {
                System.out.println("*** ERROR *** Didn't find type (" +
                                   type + ") in toString() value");
                ++errors;
            } else {
                int     j;

                /*
                 *  Found the type with a = in front.
                 *  See what the variable name is.
                 */
                for (j = i - 1; (j >= 0); --j) {
                    char    thisChar;

                    thisChar = toString.charAt(j);
                    if (Character.isJavaIdentifierPart(thisChar) ==
                                        false) {
                        for (++j; (j < i); ++j) {
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
                System.out.println("type must be stored in " +
                                   toString.substring(j, i));
            }

            exposedType = "unknown";
            if (which instanceof Empty) {
                exposedType = "0";
            } else if (which instanceof Bomb) {
                exposedType = Bomb.myType;
            } else {
                System.out.println("*** ERROR ***" +
                                   " Unknown type of cell: " +
                                   which);
                ++errors;
            }

            errors += checkCellType(which, exposedType);

            which.flag();
            errors += checkCellType(which, exposedType);

            which.mark();
            errors += checkCellType(which, exposedType);

            which.clearMark();
            errors += checkCellType(which, exposedType);

            /*
             *  Make sure adjacent bomb counts work.
             */
            for (i = 1; (i < 9); ++i) {
                int adjacentBombs;

                which.incrementAdjacentBombs();
                adjacentBombs = which.getAdjacentBombs();
                if (adjacentBombs != i) {
                    System.out.println("*** ERROR *** " + which +
                                       ": adjacent bombs: " +
                                       adjacentBombs + " != " + i);
                    ++errors;
                }

                if ((which instanceof Empty) == false) {
                    continue;
                }

                errors += checkCellType(which, "" + i);
            }
        }

        if (errors > 0) {
            /*
             *  We found a problem during unit test.
             */
            System.out.println("\n" + errors +
                               " errors found during unit test.");
            System.out.println("UNIT TEST FAILED!");
            System.exit(1);
        }
    }
}
