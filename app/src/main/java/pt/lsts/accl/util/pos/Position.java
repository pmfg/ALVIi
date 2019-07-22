package pt.lsts.accl.util.pos;


import pt.lsts.imc.EstimatedState;


/**
 *
 * Position of the system, useful for vehicles mostly.
 * Includes Lat, Lon, Orientation and diferent Altitudes/Heights.
 * Most Information is extrapolated from IMCMessage EstimatedState.
 *
 * @see pt.lsts.imc.EstimatedState
 *
 * Created by jloureiro on 08-07-2015.
 */
public class Position {

    private LatLng latLng;
    private double orientation;// psi, angle used for maps representation
    private double height;
    private double z;
    private double depth;
    private double altitude;
    private EulerAngles eulerAngles;//euler angles phi theta psi

    /**
     *
     * Build a Position from an IMCMessage EstimatedState.
     *
     * @param estimatedStateMsg The IMCMessage to build the position
     * @return returns the built Position
     *
     * @see pt.lsts.imc.EstimatedState
     */
    public static Position calcPositionFromEstimatedState(EstimatedState estimatedStateMsg){
        double lat= Math.toDegrees(estimatedStateMsg.getLat());
        double lon= Math.toDegrees(estimatedStateMsg.getLon());
        double xN = estimatedStateMsg.getX();//offset North
        double yE = estimatedStateMsg.getY();//offset East
        LatLng latLngAbsolute = applyOffset(lat, lon, xN, yE);

        double orientation = Math.toDegrees(estimatedStateMsg.getPsi());
        double height = estimatedStateMsg.getHeight();
        double z = estimatedStateMsg.getZ();
        double depth = estimatedStateMsg.getDepth();
        double altitude = estimatedStateMsg.getAlt();

        EulerAngles eulerAngles = new EulerAngles(estimatedStateMsg);

        return new Position(latLngAbsolute,orientation,height,z,depth,altitude,eulerAngles);
    }

    /**
     *
     * Build a Position from an Lat long coord in deg.
     *
     * @return returns the built Position
     */
    public static Position calcPosition(LatLng m_lat_lng){
        EulerAngles eulerAngles = new EulerAngles(0, 0, 0);
        return new Position(m_lat_lng,0,0,0,0,0,eulerAngles);
    }

    /**
     *
     * Apply offset to get absolute latitude,longitude
     *
     * @param lat latitude in degrees
     * @param lon longitude in degrees
     * @param xN offset North in meters
     * @param yE offset East in meters
     * @return LatLng (lat,lon) in degrees
     */
    public static LatLng applyOffset(double lat, double lon, double xN, double yE){
        //Earth’s radius, sphere
        double R=6378137;

        //Coordinate offsets in radians
        double dLat = xN/R;
        double dLon = yE/(R*Math.cos(Math.toRadians(lat)));

        //OffsetPosition, decimal degrees
        double newLat = lat + Math.toDegrees(dLat);
        double newLon = lon + Math.toDegrees(dLon);

        LatLng result = new LatLng(newLat,newLon);
        return result;
    }

    /**
     *
     * Apply offset to get absolute latitude,longitude
     *
     * @param latLng latitude,longitude in degrees
     * @param xN offset North in meters
     * @param yE offset East in meters
     * @return LatLng (lat,lon) in degrees
     */
    public static LatLng applyOffset(LatLng latLng, double xN, double yE){
        double lat = latLng.getLat();
        double lon = latLng.getLon();
        return applyOffset(lat, lon, xN, yE);
    }


    /**
     *
     * @param lat latitude in degrees
     * @param lon longitude in degrees
     * @param orientation psi in degrees
     * @param height EstimatedState height (UAV)
     * @param z EstimatedState Z in meters
     * @param depth EstimatedState depth (UUV/AUV)
     * @param altitude EstimatedState altitude in meters
     * @param phi Euler Angle phi in degrees
     * @param theta Euler Angle theta in degrees
     * @param psi Euler Angle psi in degrees
     */
    public Position(double lat, double lon, double orientation, double height, double z, double depth, double altitude, double phi, double theta, double psi) {
        this(new LatLng(lat, lon), orientation, height, z, depth, altitude, new EulerAngles(phi, theta, psi));
    }

    /**
     *
     * @param lat latitude in degrees
     * @param lon longitude in degrees
     * @param orientation psi in degrees
     * @param height EstimatedState height (UAV)
     * @param z EstimatedState Z in meters
     * @param depth EstimatedState depth (UUV/AUV)
     * @param altitude EstimatedState altitude in meters
     * @param eulerAngles (phi,theta,psi) in degrees
     */
    public Position(double lat, double lon, double orientation, double height, double z, double depth, double altitude, EulerAngles eulerAngles){
        this(new LatLng(lat,lon), orientation, height, z, depth, altitude, eulerAngles);
    }

    /**
     *
     * @param latLng (lat,lon) in degrees
     * @param orientation psi in degrees
     * @param height EstimatedState height (UAV)
     * @param z EstimatedState Z in meters
     * @param depth EstimatedState depth (UUV/AUV)
     * @param altitude EstimatedState altitude in meters
     * @param phi Euler Angle phi in degrees
     * @param theta Euler Angle theta in degrees
     * @param psi Euler Angle psi in degrees
     */
    public Position(LatLng latLng, double orientation, double height, double z, double depth, double altitude, double phi, double theta, double psi){
        this(latLng, orientation, height, z, depth, altitude, new EulerAngles(phi,theta,psi));
    }


    /**
     *
     * @param latLng (lat,lon) in degrees
     * @param orientation psi in degrees
     * @param height EstimatedState height (UAV)
     * @param z EstimatedState Z in meters
     * @param depth EstimatedState depth (UUV/AUV)
     * @param altitude EstimatedState altitude in meters
     * @param eulerAngles (phi,theta,psi) in degrees
     */
    public Position(LatLng latLng, double orientation, double height, double z, double depth, double altitude, EulerAngles eulerAngles){
        setLatLng(latLng);
        setOrientation(orientation);
        setHeight(height);
        setZ(z);
        setDepth(depth);
        setAltitude(altitude);
        setEulerAngles(eulerAngles);
    }

    /**
     *
     * @return The Orientation (Third euler angle Psi) in degrees
     */
    public double getOrientation() {
        return orientation;
    }

    /**
     * Set the orientation (Third euler angle Psi) in degrees
     * @param orientation the new (Third euler angle Psi) in degrees
     */
    public void setOrientation(double orientation) {
        this.orientation = orientation;
        if (this.eulerAngles==null)
            this.eulerAngles = new EulerAngles(0,0,orientation);
        this.eulerAngles.setPsi(orientation);
    }

    /**
     *
     * @return the Latitude in degrees
     */
    public double getLatitude(){
        return getLatLng().getLat();
    }

    /**
     *
     * @return the Longitude in degrees
     */
    public double getLongitude(){
        return getLatLng().getLon();
    }

    /**
     *
     * @return the Latitude, Longitude in degrees
     */
    public LatLng getLatLng() {
        return latLng;
    }

    /**
     *
     * Set a new (Latitude, Longitude) in degrees
     * @param latLng tje new (Latitude, Longitude) in degrees
     */
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    /**
     *
     * @return The height in meters of the system
     */
    public double getHeight() {
        return height;
    }

    /**
     *
     * Set a new Height in meters for the system
     * @param height The new height in meters
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     *
     * @return The Z in meters of the system
     */
    public double getZ() {
        return z;
    }

    /**
     *
     * Set a new Z in meters for the system
     * @param z The new Z in meters
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     *
     * @return The depth in meters of the system
     */
    public double getDepth() {
        return depth;
    }

    /**
     *
     * Set a new depth in meters for the system
     * @param depth The new depth in meters
     */
    public void setDepth(double depth) {
        this.depth = depth;
    }

    /**
     *
     * @return The altitude in meters of the system
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     *
     * Set a new altitude in meters for the system
     * @param altitude The new altitude in meters
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     *
     * @return The Euler Angles (Phi, Theta, Psi) in degrees
     */
    public EulerAngles getEulerAngles() {
        return eulerAngles;
    }

    /**
     *
     * Set a new Euler Angles (Phi, Theta, Psi) in degrees
     * @param eulerAngles The new Euler Angles (Phi, Theta, Psi) in degrees
     */
    public void setEulerAngles(EulerAngles eulerAngles) {
        this.eulerAngles = eulerAngles;
        setOrientation(eulerAngles.getPsi());
    }

    /**
     *
     * Set a new Euler Angles (Phi, Theta, Psi) from IMCMessage EulerAngles
     * @param eulerAnglesMsg The IMCMessage EulerAngles to build EulerAngles from.
     *
     * @see pt.lsts.imc.EstimatedState
     */
    public void setEulerAngles(pt.lsts.imc.EulerAngles eulerAnglesMsg){
        setEulerAngles(new EulerAngles(eulerAnglesMsg));
    }

    /**
     *
     * Set a new Euler Angles (Phi, Theta, Psi) from IMCMessage EstimatedState
     * @param estimatedStateMsg The IMCMessage EstimatedState to build EulerAngles from.
     *
     * @see pt.lsts.imc.EulerAngles
     */
    public void setEulerAngles(EstimatedState estimatedStateMsg){
        setEulerAngles(new EulerAngles(estimatedStateMsg));
    }

    /**
     * A comprehensive humand readable message describing this position.
     *
     * @return "Lat: X , Lon: Y \n Altitude: Z \n Orientation: T"
     */
    @Override
    public String toString(){
        return 
            "Lat: "+ getLatitude()
                + " , Lon: "+getLatitude()
                +"\nAltitude: "
                +String.format("%.2f",getAltitude())
                +"\nOrientation: "
                +String.format("%.2f",getOrientation())
                ;
    }

}
