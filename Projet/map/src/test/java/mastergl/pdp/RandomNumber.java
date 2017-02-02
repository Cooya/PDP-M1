package mastergl.pdp;

/**
 * Created by Florian on 22/03/2016.
 * Is class is just for the random method
 */



public class RandomNumber {

    /**
     * Generate a random number between min and max
     * @param min the minimal number can generate
     * @param max the maximum number can generate
     * @return a random int between min and max
     */
    public static int random(int min,int max){

        return min + (int)(Math.random() * ((max - (min)) + 1));

    }

}
