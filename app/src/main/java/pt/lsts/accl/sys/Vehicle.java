package pt.lsts.accl.sys;

import pt.lsts.accl.util.pos.*;

import pt.lsts.imc.Announce;
import pt.lsts.imc.EstimatedState;


/**
 *
 * Class Representative of vehicles with specific types of vehicles may have classes that extend this.
 *
 * Extends Sys, which included:
 * ID
 * Name
 * Type
 * IpAddress
 * LastMsgReceived
 *
 * Created by jloureiro on 30-06-2015.
 */
public class Vehicle extends Sys {

    /**
     *
     * Position class, includes:
     *
     * LatLng (lat, lon) in degrees
     * orientation
     * Altitudes from EstimatedState: height, z, depth, alt
     * Euler Angles (phi, theta, psi) in degrees
     *
     * @see pt.lsts.accl.util.pos.Position
     */
    private Position position;

    /**
     *
     * also referred to as ground speed
     */
    private double trueSpeed;
    private double indicatedSpeed;
    private float fuelLevel;

    /**
     *
     *  current Plan ID
     */
    private String planID;

    /**
     *
     * Current Maneuver ID
     */
    private String maneuverID;

    /**
     *
     * Update the position of the vehicle from an IMCMessage EstimatedState
     * @param estimatedStateMsg the IMCMessage EstimatedState to extrapolate position from.
     */
    public void updatePosition(EstimatedState estimatedStateMsg){
        setPosition(Position.calcPositionFromEstimatedState(estimatedStateMsg));
    }

    /**
     *
     * Build Vehicle from IMCMessage Announce
     * @param announceMsg IMCMessage Announce to build the vehicle class from
     *
     * @see pt.lsts.imc.Announce
     */
    public Vehicle(Announce announceMsg){
        super(announceMsg);
    }

    /**
     *
     * Build a Vehicle from a Sys
     * @param sys The Sys to build vehicle from
     */
    public Vehicle(Sys sys){
        setID(sys.getID());
        setName(sys.getName());
        setSysType(sys.getSysType());
        setIpAddress(sys.getIpAddress());
        setLastMsgReceived(sys.getLastMsgReceived());
    }

    /**
     *
     * Necessary empty constructor for extending this Class
     */
    public Vehicle(){}

    /**
     *
     * Get the position
     * @return the Position of the vehicle
     *
     * @see pt.lsts.accl.util.pos.Position
     */
    public Position getPosition() {
        return position;
    }

    /**
     *
     * Set a new position
     * @param position The new Position
     *
     * @see pt.lsts.accl.util.pos.Position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     *
     * The current percentage of Fuel/Battery. values under 20% may trigger alerts
     * @return The percentage of Fuel/battery.
     */
    public float getFuelLevel() {
        return fuelLevel;
    }

    /**
     *
     * The current percentage of Fuel/Battery. values under 20% may trigger alerts
     * @param fuelLevel The new percentage of Fuel/battery.
     */
    public void setFuelLevel(float fuelLevel) {
        this.fuelLevel = fuelLevel;
        //if (fuelLevel<10) generateAlert
    }

    /**
     *
     * Get the current Plan ID
     * @return The current Plan ID
     */
    public String getPlanID() {
        return planID;
    }

    /**
     *
     * Set a new Plan ID
     * @param planID The new Plan ID
     */
    public void setPlanID(String planID) {
        this.planID = planID;
    }

    /**
     *
     * Get the current Maneuver ID
     * @return The current Maneuver ID
     */
    public String getManeuverID() {
        return maneuverID;
    }

    /**
     *
     * Set a new Maneuver ID
     * @param maneuverID The new Maneuver ID
     */
    public void setManeuverID(String maneuverID) {
        this.maneuverID = maneuverID;
    }

    /**
     *
     * Get the TrueSpeed, also refered to as GroundSpeed
     * @return The TrueSpeed
     *
     * @see pt.lsts.imc.TrueSpeed
     */
    public double getTrueSpeed() {
        return trueSpeed;
    }

    /**
     *
     * Set a new TrueSpeed, also refered to as GroundSpeed
     * @param trueSpeed The new TrueSpeed
     *
     * @see pt.lsts.imc.TrueSpeed
     */
    public void setTrueSpeed(double trueSpeed) {
        this.trueSpeed = trueSpeed;
    }

    /**
     *
     * Get the IndicatedSpeed
     * @return The IndicatedSpeed
     *
     * @see pt.lsts.imc.IndicatedSpeed
     */
    public double getIndicatedSpeed() {
        return indicatedSpeed;
    }

    /**
     *
     * Set a new IndicatedSpeed
     * @param indicatedSpeed The new IndicatedSpeed
     *
     * @see pt.lsts.imc.IndicatedSpeed
     */
    public void setIndicatedSpeed(double indicatedSpeed) {
        this.indicatedSpeed = indicatedSpeed;
    }

}
