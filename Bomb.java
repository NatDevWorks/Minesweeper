package minesweeper;

/**
 *  This object implements a {@code Cell} of type {@code Bomb}.
 *  @version 2019010400
 *  @author Richard Barton
 */
public class Bomb extends Cell {
    /**
     *  This will be the type of any {@code Bomb} {@code Cell}.
     */
    public static final String  myType  = "bomb";

    /**
     *  Performs a unit test on the {@code Bomb} class
     *  @param args arguments to the unit test
     */
    public static void main(String[] args)
    {
        Cell  bomb;

        bomb = new Bomb();

        bomb.expose();
        System.out.print(bomb);
        if (bomb.getType().equals(myType) != true) {
            System.out.println(".getType() wrong: \"" +
                               bomb.getType() + "\" != \"" +
                               myType);
        }
        System.out.println();
    }

    /**
     *  Construct a {@code Cell} of type {@code Bomb}.
     */
    public Bomb()
    {
        super(myType);
    }
}
