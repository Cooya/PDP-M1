package mastergl.pdp;

import com.google.android.gms.maps.model.LatLng;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class StateDirectionsHandlerTest {

    private StateDirectionsHandler statedirectionshandler;
    private static int NbTest = RandomNumber.random(100, 1200);
    private static TestLog log;

    @Before
    public void setUp() throws Exception {
        log = TestLog.getInstance("StateDirectionHandlerTestLogs");
    }


    @BeforeClass
    public static void oneTimeSetUp() {
        log = TestLog.getInstance("StateDirectionHandlerTestLogs");
        log.writeWithNewLine("The random number of tests is: " + NbTest);
        log.newLine();


    }

    @AfterClass
    public static void oneTimeTearDown() {
        log.close();
    }


    /**
     * Test the roundAboutExit method with critical values.
     * @throws Exception
     */

    @Test
    public void testRoundAboutExit() throws Exception {

        long begin= System.currentTimeMillis();
        log.writeWithNewLine("Test of RoundAboutExit begin.");
        try {


            LatLng latLng1 = new LatLng(1, 0);
            LatLng latLng2 = new LatLng(1,-1);
            List<LatLng> list = new ArrayList<>();
            list.add(latLng1);
            list.add(latLng2);
            statedirectionshandler = new ToLeftState(list, 0, 0);
            assertEquals(true,statedirectionshandler.RoundAboutExit(new LatLng(0,0)));

            latLng1 = new LatLng(0,0);
            latLng2 = new LatLng(0,0);
            list = new ArrayList<>();
            list.add(latLng1);
            list.add(latLng2);
            statedirectionshandler = new ToLeftState(list, 0, 0);
            assertEquals(true,statedirectionshandler.RoundAboutExit(new LatLng(0,0)));

            latLng1 = new LatLng(1,0);
            latLng2 = new LatLng(1,0);
            list = new ArrayList<>();
            list.add(latLng1);
            list.add(latLng2);
            statedirectionshandler = new ToLeftState(list,0, 0);
            assertEquals(true,statedirectionshandler.RoundAboutExit(new LatLng(0,0)));


            latLng1 = new LatLng(0,0);
            latLng2 = new LatLng(1,0);
            list = new ArrayList<>();
            list.add(latLng1);
            list.add(latLng2);
            statedirectionshandler = new ToLeftState(list, 0, 0);
            assertEquals(true,statedirectionshandler.RoundAboutExit(new LatLng(0,0)));

            latLng1 = new LatLng(1,0);
            latLng2 = new LatLng(0,0);
            list = new ArrayList<>();
            list.add(latLng1);
            list.add(latLng2);
            statedirectionshandler = new ToLeftState(list, 0, 0);
            assertEquals(false,statedirectionshandler.RoundAboutExit(new LatLng(0,0)));

            long end= System.currentTimeMillis();
            float time = ((float) (end-begin)) / 1000f;
            log.writeWithNewLine("Test succeed in " + time + " seconds.");
        }
        catch(AssertionError e){
            e.printStackTrace();
            log.writeWithNewLine("Test failed:" + e.getMessage());
        }
    }
}