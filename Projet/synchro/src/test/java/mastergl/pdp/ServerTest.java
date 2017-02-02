package mastergl.pdp;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by Florian on 03/04/2016.
 * This class tests the Server class.
 */
public class ServerTest {

    private static TestLog log;
    private static Server server;
    private static Client client;

    @BeforeClass
    public static void oneTimeSetUp() {
        log = TestLog.getInstance("ServerTestLogs");
        log.newLine();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        log.close();
    }

    @Before
    public void setUp(){
        log = TestLog.getInstance("ServerTestLogs");
        server = new ServerMock();
        client = new ClientMock();
        server.start();
        client.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * This test the synchronisation protocol.
     * It create one server and one client and it test all case of the protocol.
     * Each "verify" methods throw an exception.
     * @throws Exception
     */
    @Test
    public void testWriteBlue() throws Exception {
        long begin= System.currentTimeMillis();
        log.writeWithNewLine("Test of WriteBlue begin.");
        try {
            server.writeBlue("left");
            log.writeWithNewLine("Send left to client");
            Thread.sleep(5000);
            Mockito.verify(((ClientMock) client).getManageDataMock(), Mockito.times(1)).vibrate();
            log.writeWithNewLine("Client vibrates");
            Mockito.verify(((ClientMock) client).getManageDataMock(), Mockito.times(1)).writeBlue("ackClient");
            log.writeWithNewLine("Client acknowledge");
            Mockito.verify(((ClientMock) client).getManageDataMock(), Mockito.atLeast(1)).writeBlue("ping");
            log.writeWithNewLine("Client sends ping");
            Mockito.verify(((ServerMock) server).getManageDataMock(), Mockito.atLeast(1)).writeBlue("ackPing");
            log.writeWithNewLine("Server sends ping acknowledge");
            server.writeBlue("right");
            log.writeWithNewLine("Send right to server");
            Thread.sleep(6000);

            Mockito.verify(((ServerMock) server).getVibrator(), Mockito.times(1)).vibrate(1000);
            log.writeWithNewLine("Server vibrates right");

            client.cancel();
            log.writeWithNewLine("Client disconnected");

            Thread.sleep(10000);


            server.writeBlue("right");
            log.writeWithNewLine("Send right to server");

            Mockito.verify(((ServerMock) server).getVibrator(), Mockito.times(3)).vibrate(1000);
            log.writeWithNewLine("Server vibrates twice");


            long end = System.currentTimeMillis();
            float time = ((float) (end-begin)) / 1000f;
            log.writeWithNewLine("Test succeed in " + time + " seconds.");
        }
        catch (Exception e) {
            log.writeWithNewLine("Test failed:" + e.getMessage());
            e.printStackTrace();
        }
    }
}