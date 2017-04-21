package nachos.threads;

import nachos.machine.*;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */

    private TreeMap<Long,KThread> sleepQueue = new TreeMap<Long,KThread>();
    public long timeToWake = 0;

    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {


    //System.out.println("beggining of the timeInterrupt " + Machine.timer().getTime());

    boolean intStatus = Machine.interrupt().disable();
/*
    for(Map.Entry<Long,KThread> entry : sleepQueue.entrySet()) {

        KThread sleepingThread = entry.getValue();
        Long wakeUpTime = entry.getKey();

        if (wakeUpTime <= Machine.timer().getTime()){
            System.out.println("wakeUp time =  " + wakeUpTime);
            System.out.println("is the for in timerInterrupt working? " + Machine.timer().getTime());
            sleepingThread.ready();
            sleepQueue.remove(wakeUpTime);
            System.out.println("thread succesfully removed from sleeping queue" + Machine.timer().getTime());
        }

        else break;

    }*/

    Iterator<Map.Entry<Long, KThread>> iter = sleepQueue.entrySet().iterator();
    Map.Entry<Long, KThread> entry;
    while (iter.hasNext()) {
        entry = iter.next();
        KThread sleepingThread = entry.getValue();
        Long wakeUpTime = entry.getKey();

       // if (wakeUpTime <= Machine.timer().getTime()){
        if(wakeUpTime <= Machine.timer().getTime()){
            System.out.println("wakeUp time =  " + wakeUpTime);
            //System.out.println("is the for in timerInterrupt working? " + Machine.timer().getTime());
            sleepingThread.ready();
            iter.remove();
            break;

            //System.out.println("thread succesfully removed from sleeping queue" + Machine.timer().getTime());
        }
        else break;

    }

    Machine.interrupt().restore(intStatus);

    //System.out.println("end of the timeInterrupt " + Machine.timer().getTime());

	//KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)

    boolean intStatus = Machine.interrupt().disable();
    //System.out.println("beggining of the waitUntil " + Machine.timer().getTime());

	long wakeTime = Machine.timer().getTime() + x;
   // timeToWake = wakeTime;
    

    KThread currThread = KThread.currentThread();
    if(!sleepQueue.containsKey(wakeTime)){
        sleepQueue.put(wakeTime,currThread);
        
    }else{
        sleepQueue.put(wakeTime+1000,currThread);
    }
    
    currThread.sleep();

    Machine.interrupt().restore(intStatus);

    //System.out.println("end of the waitUntil " + Machine.timer().getTime());

	/*while (wakeTime > Machine.timer().getTime())
	    KThread.yield();*/
    }
}