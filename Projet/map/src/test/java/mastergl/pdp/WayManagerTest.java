package mastergl.pdp;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * This class tests WayManager methods.
 */


public class WayManagerTest {


    private WayManager waymanager;
    private static int NbTest = 100000;
    private static TestLog log;




    @Before
    public void setUp() throws Exception {
        waymanager = new WayManager(new ArrayList<LatLng>(1));
        log = TestLog.getInstance("WayManagerTestLogs");
    }


    @BeforeClass
    public static void oneTimeSetUp() {
        log = TestLog.getInstance("WayManagerTestLogs");
        log.writeWithNewLine("The random number of tests is: " + NbTest);
        log.newLine();


    }

    @AfterClass
    public static void oneTimeTearDown() {
        log.close();
    }





    /**
     *This method is using in testDecodePoly method, it generate a random available polyline to decode for testing.
     *This polyline is composed by NbTest (see the static member) LatLng.
     *
     * @return a structured String corresponding in a available polyline to decode.
     * @see LatLng
     */
    public String initTestPolyLine(List<LatLng> expectedList){
        String testPolyline="~fayB_ocsF";
        int lat = -20;
        int lng = 40;
        expectedList.add(new LatLng(lat,lng));
        for(int i=0;i<NbTest;i++){
            int randcase=RandomNumber.random(0, 5);
            if(randcase % 2  == 0 && (lat + 3 >= 90 || lng+3 >= 180))
                randcase = 5;
            if (randcase % 2 == 1 && (lat - 3 <= -90 || lng-3 <= -180))
                randcase = 4;


            if(randcase == 0 ) {
                testPolyline=testPolyline.concat("_ibE_ibE");
                lat++;
                lng++;
                expectedList.add(new LatLng(lat,lng));
                continue;
            }
            if(randcase == 1 ) {
                testPolyline=testPolyline.concat("~hbE~hbE");
                lat--;
                lng--;
                expectedList.add(new LatLng(lat,lng));
                continue;
            }
            if(randcase == 2 ) {
                testPolyline=testPolyline.concat("_seK_seK");
                lat+=2;
                lng+=2;
                expectedList.add(new LatLng(lat,lng));
                continue;
            }
            if(randcase == 3 ) {
                testPolyline=testPolyline.concat("~reK~reK");
                lat-=2;
                lng-=2;
                expectedList.add(new LatLng(lat,lng));
                continue;
            }
            if(randcase == 4 ) {
                testPolyline=testPolyline.concat("_}hQ_}hQ");
                lat+=3;
                lng+=3;
                expectedList.add(new LatLng(lat,lng));
                continue;
            }
            if(randcase == 5 ) {
                testPolyline=testPolyline.concat("~|hQ~|hQ");
                lat-=3;
                lng-=3;
                expectedList.add(new LatLng(lat,lng));
                continue;
            }
        }
        return testPolyline;

    }




    /**
     * This method test if the method decodePoly in WayManager class is working.
     * It tests the extrem limits of encrypt latitude and longitude values.
     * Then the test try to stress the method with a String with random size and random LatLng object to decrypt.
     * It throws exception if the list of LatLng returned by call of decodePoly is not equals to the expected result
     * or if the testPolyLine is not fully decrypt (if the size of listTest is not equals to NbTest).
     *
     * @throws Exception
     * @see WayManager#decodePoly(String)
     */

    @Test
    public void testDecodePoly() throws Exception {
        long begin= System.currentTimeMillis();
        log.writeWithNewLine("Test of DecodePoly begin.");
        try {
            List<LatLng> listTest;
            listTest = WayManager.decodePoly("_cidP_gsia@");
            assertEquals(new LatLng(90, 180), listTest.get(0));

            listTest = WayManager.decodePoly("_cidP~fsia@");
            assertEquals(new LatLng(90, -180), listTest.get(0));

            listTest = WayManager.decodePoly("~bidP_gsia@");
            assertEquals(new LatLng(-90, 180),listTest.get(0));

            listTest = WayManager.decodePoly("~bidP~fsia@");
            assertEquals(new LatLng(-90, -180),listTest.get(0));



            List<LatLng> expectedList = new ArrayList<>();
            String testPolyline= initTestPolyLine(expectedList);
            listTest = WayManager.decodePoly(testPolyline);
            assertArrayEquals(expectedList.toArray(),listTest.toArray());


            long end = System.currentTimeMillis();
            float time = ((float) (end-begin)) / 1000f;
            log.writeWithNewLine("Test succeed in " + time + " seconds.");

        }
        catch(AssertionError e){
            e.printStackTrace();
            log.writeWithNewLine("Test failed:" + e.getMessage());
        }
    }


    /**
     * This method test if the method getAngleFromNorth in WayManager class is working.
     * It tests the extreme case where the two points are merged or totally opposed.
     * It also test a random number of LatLng objects combinations.
     * For assertEquals it throws exception when the result of the function call is different than the expected result.
     * The random test throws if the function return a wrong angle value (0-360).
     *
     * @throws Exception
     * @see WayManager#getAngleFromNorth(LatLng, LatLng)
     */
    @Test
    public void testGetAngleFromNorth() throws Exception {
        log.newLine();
        log.newLine();
        long begin= System.currentTimeMillis();
        log.writeWithNewLine("Test of GetAngleFromNorth begin.");

        try {
            float resultTest;

            resultTest = waymanager.getAngleFromNorth(new LatLng(90, 180), new LatLng(90, 180));
            assertEquals(0.0, resultTest, 0.0);

            resultTest = waymanager.getAngleFromNorth(new LatLng(90, 180), new LatLng(-90, -180));
            assertEquals(180, resultTest, 0.0);

            resultTest = waymanager.getAngleFromNorth(new LatLng(90, 180), new LatLng(90, -180));
            assertEquals(0.0, resultTest, 0.0);

            resultTest = waymanager.getAngleFromNorth(new LatLng(90, 180), new LatLng(-90, 180));
            assertEquals(180, resultTest, 0.0);

            for (int i = 0; i < NbTest; i++) {
                LatLng LL1 = new LatLng(RandomNumber.random(-90, 90), RandomNumber.random(-180, 180));
                LatLng LL2 = new LatLng(RandomNumber.random(-90, 90), RandomNumber.random(-180, 180));
                resultTest = waymanager.getAngleFromNorth(LL1, LL2);
                assertEquals(180, resultTest, 180.0);
            }

            long end = System.currentTimeMillis();
            float time = ((float) (end-begin)) / 1000f;
            log.writeWithNewLine("Test succeed in " + time + " seconds.");
        }
        catch(AssertionError e){
            e.printStackTrace();
            log.writeWithNewLine("Test failed:" + e.getMessage());
        }
    }


    /**
     * This method test if the method intersectLocationToPath in WayManager class is working.
     * First it tests if we give two merged points to the method, it can replace the point (in parameter) on the line
     * It also test a random number of LatLng objects combinations.
     * For assertEquals it throws exception when the result of the function call is different than the expected result.
     * The random test throws if the function return a wrong angle value (0-360).
     *
     * @throws Exception
     * @see WayManager#intersectLocationToPath(Point)
     */

    @Test
    public void testIntersectLocationToPath() throws Exception {

        log.newLine();
        long begin= System.currentTimeMillis();
        log.writeWithNewLine("Test of IntersectLocationToPath begin.");

        try {

            LatLng latLng1 = new LatLng(10, 10);
            LatLng latLng2 = new LatLng(10, 10);
            List<LatLng> list = new ArrayList<>();
            list.add(latLng1);
            list.add(latLng2);

            waymanager = new WayManager(list);

            StateDirectionsHandler stateH = new ToLeftState(list,0,0);

            SphericalMercatorProjection utility = new SphericalMercatorProjection(SphericalUtil.computeArea(stateH.getPoints()));

            Point test = new Point(RandomNumber.random(-90, 90), RandomNumber.random(-180, 180));
            LatLng tmp = utility.toLatLng(waymanager.intersectLocationToPath(test));
            assertTrue(PolyUtil.isLocationOnPath(tmp, stateH.getPoints(), true, 0.0) == true);


            for (int i = 0; i < NbTest; i++) {

                latLng1 = new LatLng(RandomNumber.random(-90, 90), RandomNumber.random(-180, 180));
                latLng2 = new LatLng(RandomNumber.random(-90, 90), RandomNumber.random(-180, 180));
                list = new ArrayList<>();
                list.add(latLng1);
                list.add(latLng2);

                waymanager = new WayManager(list);

                stateH = new ToLeftState(list, 1, 1);


                utility = new SphericalMercatorProjection(SphericalUtil.computeArea(stateH.getPoints()));

                test = new Point(RandomNumber.random(-90, 90), RandomNumber.random(-180, 180));
                tmp = utility.toLatLng(waymanager.intersectLocationToPath(test));
                assertTrue(PolyUtil.isLocationOnPath(tmp, stateH.getPoints(), true, 0.0));
            }

            long end= System.currentTimeMillis();
            float time = ((float) (end-begin)) / 1000f;
            log.write("Test succeed in " + time + " seconds.");
        }
        catch(AssertionError e){
            e.printStackTrace();
            log.write("Test failed:" + e.getMessage());
        }
    }



}