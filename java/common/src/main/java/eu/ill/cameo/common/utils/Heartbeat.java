package eu.ill.cameo.common.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract class defining the heartbeat to ping periodically the connected sockets.
 */
public abstract class Heartbeat {

	private int period;
	private boolean terminated = false;
	private final Lock pingLock = new ReentrantLock();
	private final Condition pingCondition = pingLock.newCondition();
	private Thread pingThread;
	
	/**
	 * Constructor.
	 * @param period The heartbeat period in seconds.
	 */
	public Heartbeat(int period) {
		this.period = period;
	}
	
	/**
	 * Starts the heartbeat.
	 */
	public void start() {
		
		if (pingThread == null) {
			pingThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						
						pingLock.lock();
						
						// The heartbeat can be terminated here.
						if (terminated) {
							break;
						}
						
						// Await returns false if the waiting time elapsed
						boolean signaled;

						try {
							signaled = pingCondition.await(period, TimeUnit.SECONDS);
							if (!signaled) {
								pingAll();
							}
							else {
								break;
							}
		                }
		                catch (InterruptedException e) {
							break;
		                }
		                finally {
		                	pingLock.unlock();
		                }
					}
				}
			});
			pingThread.start();
		}
	}
	
	/**
	 * Terminates the heartbeat.
	 */
	public void terminate() {
		
		if (pingThread != null) {
			try {
				pingLock.lock();
				terminated = true;
				pingCondition.signal();
			}
			finally {
				pingLock.unlock();
			}

			try {
				pingThread.join();
			}
			catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * Pings all the objects.
	 */
	public abstract void pingAll();
}
