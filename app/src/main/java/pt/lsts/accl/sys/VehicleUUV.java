package pt.lsts.accl.sys;

import pt.lsts.imc.VehicleState;

/**
 *
 * Class representative of UUV
 * Unmaned Underwater Vehicles, also refered to as (L)AUV - (Light) Autnomous Underwater Vehicles
 *
 * Extends Vehicle, which includes:
 * Position
 * TrueSpeed also refered to as Ground Speed
 * IndicatedSpeed
 * PlanID
 * ManeuverID
 *
 * Created by jloureiro on 01-07-2015.
 */
public class VehicleUUV extends Vehicle{

    /**
     *
     * SERVICE(0L),
     * CALIBRATION(1L),
     * ERROR(2L),
     * MANEUVER(3L),
     * EXTERNAL(4L),
     * BOOT(5L);
     *
     * @see pt.lsts.imc.VehicleState
     */
    private VehicleState.OP_MODE vehicleState;

    /**
     *
     * Update the Vehicle State, also refered to as operating mode.
     * @param vehicleStateMsg The IMCMessage VehicleState to extrapolate operating mode from
     *
     * @see pt.lsts.imc.VehicleState
     */
    public void updateVehicleState(VehicleState vehicleStateMsg){
        setVehicleState(vehicleStateMsg.getOpMode());
    }

    /**
     * Get the Vehicle State, also refered to as operating mode.
     * @return The current Vehicle State
     *
     * @see pt.lsts.imc.VehicleState
     */
    public VehicleState.OP_MODE getVehicleState() {
        return vehicleState;
    }

    /**
     * Set vehicle State, also refered to as operating mode
     * @param vehicleState The new Vehicle State
     *
     * @see pt.lsts.imc.VehicleState
     */
    public void setVehicleState(VehicleState.OP_MODE vehicleState) {
        this.vehicleState = vehicleState;
    }

}
