package mastergl.pdp;

import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by alaguitard on 04/03/16.
 */
public class JsonParserUtilityTest {

    JsonParserUtility parser;

    @Before
    public void setUp() throws Exception {
        /*parser = new JsonParserUtility(); {
            @Override
            protected HttpURLConnection createHttpConnection()
            {
                HttpURLConnection mock = Mockito.mock(HttpURLConnection.class);
                try {
                    Mockito.when(mock.getInputStream()).thenReturn(new InputStream() {
                        @Override
                        public int read() throws IOException {
                            return 2;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return mock;
            }
        };*/
    }

    /**
     * Test with json url
     */
    /*@Test
    public void testGetAndParseJsonUrl() throws Exception {
        try{
            parser.setUrl(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&"));
        } catch(MalformedURLException e)
        {
            assertEquals(true,false);
        }

        parser.getAndParse();
        assertEquals(true, parser.isParsedAndReady());
    }*/

    /**
     * Test with empty string
     */
    @Test
    public void testGetAndParseEmpty() throws Exception {

        try {
            parser.setUrl(new URL(""));
        } catch(MalformedURLException e)
        {
            return;
        }

        parser.getAndParse();
        assertEquals(false,parser.isParsedAndReady());
    }

    /**
     * Test with url with random url
     */
    @Test
    public void testGetAndParseRandom() throws Exception {

        try {
            parser.setUrl(new URL("http://www.google.com"));
        } catch(MalformedURLException e)
        {
            assertEquals(true,false);
        }
        parser.getAndParse();
        assertEquals(false, parser.isParsedAndReady());
    }

    /**
     * Test with json url with some mistakes in it.
     */
    @Test
    public void testGetAndParseJsonUrlWithError() throws Exception {
        try {
            parser.setUrl(new URL("www.maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&"));
        } catch(MalformedURLException e)
        {
            return;
        }
        parser.getAndParse();
        assertEquals(false,parser.isParsedAndReady());
    }
}