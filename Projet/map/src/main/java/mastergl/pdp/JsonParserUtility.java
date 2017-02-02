package mastergl.pdp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides utility to get a json file from url, and stock the Json object in members.
 * The task for downloading the file may be long, so it has to be done in background for a good user
 * experience. That task is called when the execute method is called (without any parameters).
 * @see WayManager
 */
public class JsonParserUtility implements Parcelable{

    /**
     * the url of the json to extract when execute() is called.
     */
    private URL m_url;

    /**
     * The reader of the stream.
     */
    private BufferedReader m_in;

    /**
     * The object stocking the json file.
     */
    protected JSONObject mJsonObject;

    /**
     * Reference on node of JsonObject.
     */
    protected JSONArray mJRoutes;

    /**
     * The identifier of the integrity.
     */
    private boolean parsedAndReady;

    /**
     * Constructor of the class.
     * @param url the url of the json to extract when execute() is called.
     */
    public JsonParserUtility(String url)
    {
        try {
            m_url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mJsonObject = null;
        parsedAndReady = false;
    }

    /**********************************************************************************************
     ******************************* PARCELABLE OVERRIDE METHODS **********************************
     **********************************************************************************************/

    /**
     * Constructor to recreate a WayManager from a parcel.
     * @param in The parcel to construct the object from.
     */
    protected JsonParserUtility(Parcel in) {
        parsedAndReady = false;
        parsedAndReady = in.readByte() != 0;
    }

    /**
     * The creator WayManager to create from a parcel.
     * call the constructor WayManager(Parcel in).
     * @see #JsonParserUtility(Parcel)
     */
    public static final Creator<JsonParserUtility> CREATOR = new Creator<JsonParserUtility>() {
        @Override
        public JsonParserUtility createFromParcel(Parcel in) {
            return new JsonParserUtility(in);
        }

        @Override
        public JsonParserUtility[] newArray(int size) {
            return new JsonParserUtility[size];
        }
    };

    /**
     * Describe the content of the parcelable object.
     * @return the number of complex field to parcel.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (parsedAndReady ? 1 : 0));
    }

    /**********************************************************************************************
     ********************************** CREATED METHODS *******************************************
     **********************************************************************************************/

    /**
     * This is the method which has to ran in background. It downloading the file out of the url passing in
     * members to the constructor.
     * it returns nothing because the result is stock in the mJsonObject member.
     */
    protected void getAndParse() {

        if(m_url == null)
            return;
        String streamInput = "";

        try {

            HttpURLConnection urlConnection = createHttpConnection();

            m_in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String tmp;
            while((tmp = m_in.readLine()) != null)
            {
                Log.e("JSON 2",tmp);
                streamInput += tmp;
            }

            /*
            PARSE THE STRING TO JSON OBJECT
            */
            mJsonObject = new JSONObject(streamInput);

        } catch (JSONException e) {
            Log.e("ERROR", "Json Exception :\n" + Arrays.toString(e.getStackTrace()));
            parsedAndReady = false;

        } catch (IOException e) {
            Log.e("ERROR", "I/O Exception :\n"+ Arrays.toString(e.getStackTrace()));
            parsedAndReady = false;

        } finally {
            if(m_in != null)
                try {
                    m_in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        checkIntegrity();
        checkStatus();
        Log.e("DEBUG","Integrity check, response: " + String.valueOf(parsedAndReady));

    }

    /**
     * Create a http connection from m_url members.
     * @return Http connections object.
     * @throws IOException
     */
    protected HttpURLConnection createHttpConnection() throws IOException
    {
        return (HttpURLConnection) m_url.openConnection();
    }

    /**
     * Check the status of the requested way (OK, ZERO_RESULT, ERROR)
     */
    public void checkStatus()
    {
        try {
            if(!mJsonObject.getString("status").equals("OK"))
            {
                Log.e("STATUS",mJsonObject.getString("status"));
                parsedAndReady = false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Check the integrity of the json file caught up.
     * @return true if the json file is correct, false if not.
     */
    public boolean checkIntegrity() {
        try
        {
            if(!mJsonObject.has("geocoded_waypoints"))
            {
                parsedAndReady = false;
                return false;
            }
            JSONArray mJLegs, mJSteps;

            mJRoutes = mJsonObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < mJRoutes.length(); i++) {
                 mJLegs = mJRoutes.getJSONObject(i).getJSONArray("legs");

                /** Traversing all legs */
                for (int j = 0; j < mJLegs.length(); j++) {
                    mJSteps = mJLegs.getJSONObject(j).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < mJSteps.length(); k++) {

                        JSONObject step = (JSONObject) mJSteps.get(k);
                        if(!(step.has("distance") && step.has("duration") && step.has("end_location")
                                && step.has("start_location") && step.has("polyline")
                                && step.has("travel_mode") && step.has("html_instructions")))
                        {
                            parsedAndReady = false;
                            Log.e("DEBUG","the json object miss a field");
                            return false;
                        }

                    }
                }
            }
       }
        catch(JSONException e)
        {
            parsedAndReady = false;
            return false;
        }
        catch(NullPointerException e2)
        {
            parsedAndReady = false;
            return false;
        }

        parsedAndReady = true;
        return true;
    }

    /**
     * Be careful to call this method AFTER the getAndParse method. Otherwise, mJsonObject will be null.
     * @return the JsonObject stocking the json file.
     */
    public JSONObject getJSObject()
    {
        if(parsedAndReady)
            return mJsonObject;
        return null;
    }

    /**
     * Get the route object at the specified index of the route array.
     * @param index the index where to search the route.
     * @return the route at the specified index.
     * @throws JSONException throws if there is a problem, like a out of bounds index or a non-well
     * parsed JSON stream.
     */
    public JSONObject getRoute(int index) throws JSONException {
        return mJRoutes.getJSONObject(index);
    }

    /**
     * In the leg array specified by indexRoute in the route array, get the leg specified by indexLeg.
     * @param indexRoute the index of the route in the route array.
     * @param indexLeg the index of the leg in the leg array.
     * @return the specified leg, which is a array of step.
     * @throws JSONException throws if there is a problem, like a out of bounds index or a non-well
     * parsed JSON stream.
     */
    public JSONObject getLeg(int indexRoute,int indexLeg) throws JSONException
    {
        return getRoute(indexRoute).getJSONArray("legs").getJSONObject(indexLeg);
    }

    /**
     * Same method than #getLeg(int indexRoute,int indexLeg) but with indexRoute equal to 0.
     * @param indexLeg the index of the leg in the leg array.
     * @return the specified leg, which is a array of step.
     * @throws JSONException throws if there is a problem, like a out of bounds index or a non-well
     * parsed JSON stream.
     */
    public JSONObject getLeg(int indexLeg) throws JSONException
    {
        return getRoute(0).getJSONArray("legs").getJSONObject(indexLeg);
    }

    /**
     * In the steps array specified by indexLeg in the leg array specified by indexRoute in the route array,
     * get the step specified by indexStep.
     * @param indexRoute the index of the route in the route array.
     * @param indexLeg the index of the leg in the leg array.
     * @param indexStep the index of the step in the steps array.
     * @return the specified step.
     * @throws JSONException throws if there is a problem, like a out of bounds index or a non-well
     * parsed JSON stream.
     */
    public JSONObject getStep(int indexRoute,int indexLeg,int indexStep) throws JSONException
    {
        return getLeg(indexRoute, indexLeg).getJSONArray("steps").getJSONObject(indexStep);
    }

    /**
     * Same method than #getStep(int indexRoute, int indexLeg,int indexStep) but with indexRoute
     * and indexLeg equals to 0.
     * @param indexStep the index of the step in the steps array.
     * @return the specified step.
     * @throws JSONException throws if there is a problem, like a out of bounds index or a non-well
     * parsed JSON stream.
     */
    public JSONObject getStep(int indexStep) throws JSONException
    {
        return getLeg(0,0).getJSONArray("steps").getJSONObject(indexStep);
    }

    /**
     * Get the camera bounds of the way written in the json file.
     * @return the southwest and the northeast LatLng points of the bounds.
     */
    public List<LatLng> getBounds()
    {
        if(!parsedAndReady)
            return null;
        try {
            JSONObject getJSON = mJsonObject.getJSONArray("routes").getJSONObject(0).getJSONObject("bounds").getJSONObject("southwest");
            LatLng sw = new LatLng(getJSON.getDouble("lat"),getJSON.getDouble("lng"));

            getJSON = mJsonObject.getJSONArray("routes").getJSONObject(0).getJSONObject("bounds").getJSONObject("northeast");
            LatLng ne = new LatLng(getJSON.getDouble("lat"),getJSON.getDouble("lng"));
            List<LatLng> ret = new ArrayList<>();
            ret.add(sw);
            ret.add(ne);
            return ret;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the next maneuver to determine the state of the app.
     * @param index the index of the next step.
     * @return the next maneuver as a string.
     */
    public String getNextManeuver(int index)
    {
        String man = "";
        try {
            man = (String) mJsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    .getJSONArray("steps").getJSONObject(index).get("maneuver");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return man;
    }

    public LatLng getNextPoint(int index) {

        try {
            JSONObject step = mJsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    .getJSONArray("steps").getJSONObject(index);
            float latitude = ((Double) step.getJSONObject("start_location").get("lat")).floatValue();
            float longitude = ((Double) step.getJSONObject("start_location").get("lng")).floatValue();
            return new LatLng(latitude,longitude);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public float getTimeToNext(int index) {
        try {
            return (float) mJsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    .getJSONArray("steps").getJSONObject(index-1).get("duration");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Check if the JsonParser has a valid json datas in it.
     * @return true if the parsing in getAndParse succeed, false if not.
     */
    public boolean isParsedAndReady()
    {
        return parsedAndReady;
    }

    /**
     * Set a new url in member
     * @param url the new url to set.
     */
    public void setUrl(URL url)
    {
        this.m_url = url;
    }
}
