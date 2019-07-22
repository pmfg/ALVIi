package pt.lsts.accl.sys;

import pt.lsts.imc.AutopilotMode;
import pt.lsts.imc.EstimatedState;

/**
 *
 * Class representative of UAV Vehicles.
 * Unmanned Aerial Vehicles, commercially known as drones.
 *
 * Extends Vehicle, which includes:
 * Position
 * TrueSpeed also refered to as Ground Speed
 * IndicatedSpeed
 * PlanID
 * ManeuverID
 *
 *
 * Created by jloureiro on 30-06-2015.
 */
public class VehicleUAV extends Vehicle {

    /**
     *
     * MANUAL(0L),
     * ASSISTED(1L),
     * AUTO(2L);
     */
    private AutopilotMode.AUTONOMY autonomy;

    /**
     *
     * The Relative Altitude, the value most used by CCUs, extrapolated from IMCMessage EstimatedState: Height - Z
     *
     * @see pt.lsts.imc.EstimatedState
     */
    private double relativeAlt;

    /**
     *
     * Update the Relative Altitude from IMCMessage EstimatedState: Height - Z
     * @param estimatedStateMsg
     *
     * @see pt.lsts.imc.EstimatedState
     */
    public void updateRelativeAltidue(EstimatedState estimatedStateMsg){
        setRelativeAlt(estimatedStateMsg.getHeight()-estimatedStateMsg.getZ());
    }

    /**
     *
     * Update Autonomy mode from IMCMessage AutopilotMode
     * @param autopilotModeMsg
     *
     * @see pt.lsts.imc.AutopilotMode
     */
    public void updateAutonomy(AutopilotMode autopilotModeMsg){
        setAutonomy(autopilotModeMsg.getAutonomy());
    }

    /**
     *
     * Get the Autonomy mode the UAV is operating on.
     * @return MANUAL(0L), ASSISTED(1L) or AUTO(2L);
     *
     * @see pt.lsts.imc.AutopilotMode
     */
    public AutopilotMode.AUTONOMY getAutonomy() {
        return autonomy;
    }

    /**
     * Set the Autonomy mode
     * @param autonomy The new autonomy mode
     *
     * @see pt.lsts.imc.AutopilotMode
     */
    public void setAutonomy(AutopilotMode.AUTONOMY autonomy) {
        this.autonomy = autonomy;
    }

    /**
     *
     * Relative Altitude, the value most used by CCUs, extrapolated from IMCMessage EstimatedState: Height - Z
     * @return the last known Relative Altitude of the UAV
     *
     * @see pt.lsts.imc.EstimatedState
     */
    public double getRelativeAlt() {
        return relativeAlt;
    }

    /**
     *
     * Set the Relative Altidue, extrapolated from IMCMessage EstimatedState: Height - Z
     * @param relativeAlt
     *
     * @see pt.lsts.imc.EstimatedState
     */
    public void setRelativeAlt(double relativeAlt) {
        this.relativeAlt = relativeAlt;
    }

}
