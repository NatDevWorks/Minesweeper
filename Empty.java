package minesweeper;

/**
 *  This object implements a {@code Cell} of type {@code Empty}.
 *  @version 2019010400
 *  @author Richard Barton
 */
public class Empty extends Cell {
    /**
     *  This will be the type of any {@code Empty} {@code Cell}.
     */
    public static final String  myType  = "empty";

    /**
     *  Performs a unit test on the {@code Empty} class
     *  @param args arguments to the unit test
     */
    public static void main(String[] args)
    {
        Cell  empty;

        empty = new Empty();

        empty.expose();
        System.out.print(empty);
        if (empty.getType().equals("0") != true) {
            System.out.print(".getType() wrong: \"" +
                             empty.getType() + "\" != \"0\"");
        }
        System.out.println();
    }

    /**
     *  Construct a {@code Cell} of type {@code Empty}.
     */
    public Empty()
    {
        super(myType);
    }

    /**
     *  Return the number of adjacent bombs when exposed.
     *  @return If exposed, the number of adjacent bombs.
     */
    public String       getType()
    {
        if (super.getExposed() == true) {
            return("" + super.getAdjacentBombs());
        }

        return(super.getType());
    }
}
