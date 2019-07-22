package pt.lsts.accl.util.pos;


import pt.lsts.imc.EstimatedState;


/**
 *
 * Euler Angles (phi,theta,psi) in degrees
 *
 * Created by jloureiro on 08-07-2015.
 */
public class EulerAngles {

    private double phi;
    private double theta;
    private double psi;

    /**
     *
     * @param eulerAnglesMsg The IMCMessage to generate EulerAngles class
     *
     * @see pt.lsts.imc.EulerAngles
    **/
    public EulerAngles(pt.lsts.imc.EulerAngles eulerAnglesMsg){
        this(Math.toDegrees(eulerAnglesMsg.getPhi()), Math.toDegrees(eulerAnglesMsg.getTheta()), Math.toDegrees(eulerAnglesMsg.getPsi()));
    }

    /**
     *
     * @param estimatedStateMsg The IMCMessage to generate EulerAngles class
     *
     * @see pt.lsts.imc.EstimatedState
    **/
    public EulerAngles(EstimatedState estimatedStateMsg){
        this(Math.toDegrees(estimatedStateMsg.getPhi()), Math.toDegrees(estimatedStateMsg.getTheta()), Math.toDegrees(estimatedStateMsg.getPsi()));
    }

    /**
     *
     * @param phi Euler Angle phi in degrees
     * @param theta Euler Angle theta in degrees
     * @param psi Euler Angle psi in degrees
     */
    public EulerAngles(double phi, double theta, double psi){
        setPhi(phi);
        setTheta(theta);
        setPsi(psi);
    }

    /**
     *
     * @return The First Euler Angle Phi in degrees
     */
    public double getPhi() {
        return phi;
    }

    /**
     * Set the First Euler Angle Phi in degrees
     * @param phi The new value Phi in degrees
     */
    public void setPhi(double phi) {
        this.phi = phi;
    }

    /**
     *
     * @return The Second Euler Angle theta in degrees
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Set the Second Euler Angle Theta in degrees
     * @param theta The new value Theta in degrees
     */
    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     *
     * @return The Third Euler Angle Psi in degrees
     */
    public double getPsi() {
        return psi;
    }

    /**
     * Set the Third Euler Angle Psi in degrees
     * @param psi The new value Psi in degrees
     */
    public void setPsi(double psi) {
        this.psi = psi;
    }
    
}
