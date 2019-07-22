package pt.lsts.accl.sys;


import pt.lsts.accl.bus.AcclBus;
import pt.lsts.accl.event.EventSystemDisconnected;

import java.util.ArrayList;
import java.util.Iterator;


/**
 *
 * The List of Systems active.
 * Used with synchronized to ensure correct usage and no duplicates or outdated results
 *
 * Created by jloureiro on 06-07-2015.
 */
public class SysList {

	/**
	 * The ArrayList with the systems itself upon the methods are applied.
	 */
	public ArrayList<Sys> sysArrayList;

	public SysList(){
		sysArrayList = new ArrayList<Sys>();
	}

	/**
	 * Add a system to the List
	 * @param sys The system to be added
	 */
	public synchronized void addSys(Sys sys){
		sysArrayList.add(sys);
	}

	/**
	 * Remove a system if it exists. Usually used automatically by a periodic task if no message has been received from this system in a pre established amount of time.
	 * @param sys The system to be removed
	 */
	public synchronized void removeSys(Sys sys){
		Iterator<Sys> iterator = sysArrayList.iterator();
        while (iterator.hasNext()){
            Sys sysIt = iterator.next();
            if (sysIt.equals(sys))
                iterator.remove();
            //post event
        }
	}

	/**
	 * Search for a system by its name.
	 * @param sysName The name of the system to search for.
	 * @return The sys, null if it doesn't exist.
	 */
	public synchronized Sys getSys(String sysName){
		Iterator<Sys> iterator = sysArrayList.iterator();
        while (iterator.hasNext()){
            Sys sysIt = iterator.next();
            if (sysIt.getName().equals(sysName))
                return sysIt;
        }
        return null;
	}

	/**
	 * Search for a system by its ID.
	 * @param ID The ID of the system to search for.
	 * @return The sys, null if it doesn't exist.
	 */
	public synchronized Sys getSys(int ID){
		Iterator<Sys> iterator = sysArrayList.iterator();
        while (iterator.hasNext()){
            Sys sysIt = iterator.next();
            if (sysIt.getID()==ID)
                return sysIt;
        }
        return null;
	}

	/**
	 * Find if a system exist and get it if it does.
	 * @param sys The system to search for.
	 * @return The system, null if it doesn't exist.
	 */
	public synchronized Sys containsSys(Sys sys){
		Iterator<Sys> iterator = sysArrayList.iterator();
        while (iterator.hasNext()){
            Sys sysIt = iterator.next();
            System.out.println("sysIt: "+sysIt.toString()+"\nsys: "+sys.toString());
            if (sysIt.equals(sys))
                return sysIt;
        }
        return null;
	}

	/**
	 * Find if a system exists and return a boolean.
	 * @param ID The system ID to search for.
	 * @return true if system exists, false otherwise.
	 */
	public synchronized boolean contains(int ID){
		Iterator<Sys> iterator = sysArrayList.iterator();
        while (iterator.hasNext()){
            Sys sysIt = iterator.next();
            if (sysIt.getID()==ID)
                return true;
        }
        return false;
	}

	/**
	 * Get the actual ArrayList. Not usual. Not advisable.
	 * @return The ArrayList containing the systems.
	 */
	public ArrayList<Sys> getList(){
		return sysArrayList;
	}

	/**
	 * Remove system which haven't received a message in over 10seconds from the list.
	 */
	public synchronized void clearInnactiveSys(){
		Iterator<Sys> iterator = sysArrayList.iterator();
        while (iterator.hasNext()){
        	Sys sysIt = iterator.next();
            if (sysIt.isConnected()==false){
            	AcclBus.post(new EventSystemDisconnected(sysIt));
                iterator.remove();
            }
        }
	}

	/**
	 *
	 * Remove all Systems
	 *
	 */
	public synchronized void clear(){
		sysArrayList.clear();
	}

}
