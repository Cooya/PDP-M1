package mastergl.pdp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is a singleton. It writes a log file of tests results.
 */


public class TestLog {

    private BufferedWriter os;
    private static TestLog singleton = null;


    /**
     * Constructor of the singleton.
     * @param nameTestFile
     */

    private TestLog(String nameTestFile) {
        try {
            os = new BufferedWriter(new FileWriter(nameTestFile,false));
            makeHeader(nameTestFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get or create the singleton's instance.
     * @param nameTestFile
     * @return The instance of the singleton.
     */

    public static TestLog getInstance(String nameTestFile) {
        if (singleton == null) {
            synchronized(TestLog.class) {
                if (singleton== null) {
                    singleton = new TestLog(nameTestFile);
                }
            }
        }
        return singleton;
    }



    private void makeHeader(String name){
            String header =
                    "/**********************************************************************************************\n" +
                    "     *************************** " + name + " ******************************************\n" +
                    "     **********************************************************************************************/";
            this.writeWithNewLine(header);
            this.newLine();
    }

    /**
     * Write the String in file and begin new line.
     * @param str
     */
    public void writeWithNewLine(String str){
        try {
            os.write(str);

            os.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Write the String in file
     * @param str
     */
    public void write(String str){
        try {
            os.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Begin new line.
     */
    public void newLine(){
        try {
            os.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the stream.
     */
    public void close() {

        try {
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
