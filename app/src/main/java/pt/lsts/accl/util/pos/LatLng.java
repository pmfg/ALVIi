package pt.lsts.accl.util.pos;


/**
 *
 * Latitude and Longitude in degrees from EstimatedState
 *
 * Created by jloureiro on 08-07-2015.
 */
public class LatLng{

	private double lat;
	private double lon;

	/**
	 *
	 * Build a latLng from Latitude,Longitude in degrees
	 * @param lat the Latitude in degrees
	 * @param lon the Longitude in degrees
	 */
	public LatLng(double lat, double lon){
		this.lat=lat;
		this.lon=lon;
	}

	/**
	 *
	 * Build a latLng from another
	 * @param latLng The LatLng to build form
	 */
	public LatLng(LatLng latLng){
		this.lat = latLng.getLat();
		this.lon = latLng.getLon();
	}

	/**
	 *
	 * @return The Latitude in degrees
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * Set the Latitude in degrees
	 * @param lat The new value Latitude in degrees
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 *
	 * @return The Longitude in degrees
	 */
	public double getLon() {
		return lon;
	}

	/**
	 *
	 * Set the Longitude in degrees
	 * @param lon The new value Longitude in degrees
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}

}