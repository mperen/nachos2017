package nachos.threads;

import nachos.machine.Machine;
import nachos.threads.Communicator.Listener;
import nachos.threads.Communicator.Speaker;



	

public class NachosTests 
{
	
	public static class CommunicatorTests
	{
		public static void Test()
		{
			System.out.println("Testeando listener-speaker");

			Communicator comm = new Communicator();
			Alarm a = ThreadedKernel.alarm;
			Speaker s1 = new Speaker(comm, 1, a);
			Speaker s2 = new Speaker(comm, 2, a);
			Listener l1 = new Listener(comm, 1, a);
			Listener l2 = new Listener(comm, 2, a);

			KThread t1 = new KThread(s1);
			t1.setName("Speaker 1");
			KThread t2 = new KThread(s2);
			t2.setName("Speaker 2");

			KThread t3 = new KThread(l1);
			t3.setName("Listener 1");
			KThread t4 = new KThread(l2);
			t4.setName("Listener 2");

			//t3.fork();
			t1.fork();
			//t4.fork();
			t2.fork();
			
			ThreadedKernel.alarm.waitUntil(60000);
			t3.fork();
			
			try {
				t1.join();
				t2.join();
				t3.join();
				t4.join();
			} catch (Exception e) {

			}
		}
	}
	
	public static class Join
	{
		public static void Test(){
			KThread t1 = new KThread(new ChildThread());
			t1.setName("t1");
			KThread t2 = new KThread(new ParentThread(t1));
			t2.setName("t2");
			
			
			KThread t3 = new KThread(new ParentThread(null));
			t3.fork();
			t2.fork();
			t1.fork();
			
			try {
				t2.join();
			} catch (Exception e) {

			}
		}
		private static class ParentThread implements Runnable{
			private KThread child;
			
			public ParentThread(KThread c){
				this.child = c;
			}

			@Override
			public void run() {
				for (int i = 0; i &lt; 5; i++){
					System.out.println("Hello my name is "+KThread.currentThread().getName());
					if(i == 3 &amp;&amp; this.child != null){
						System.out.println("Joining "+this.child.getName()+", i'm going to sleep");
						
						this.child.join();
						System.out.println(KThread.currentThread().getName()+" says: I'M BACK!");
					}
					KThread.yield();
				}
			}
		}
		
		private static class ChildThread implements Runnable{
			@Override
			public void run() {
				Alarm a = ThreadedKernel.alarm;
				a.waitUntil(500);
				for (int i = 0; i &lt; 5; i++){
					System.out.println("Hello my name is "+KThread.currentThread().getName());
				}
			}
		}
	}

	public static class Condition2Tests {
		private static class ConditionSleeper implements Runnable {
			Condition2 cond;
			Lock conditionLock;
			int id;

			ConditionSleeper(Condition2 c, Lock l, int id) {
				this.cond = c;
				this.conditionLock = l;
				this.id = id;
			}

			@Override
			public void run() {
				conditionLock.acquire();
				cond.sleep();
				for (int i = 0; i &lt; 10; i++) {
					System.out.println("Thread " + id + " " + i);
				}
				conditionLock.release();
			}
		}

		private static class ConditionAwaker implements Runnable {
			Condition2 cond;
			Lock conditionLock;
			int threadCount;

			ConditionAwaker(Condition2 c, Lock l, int threadCount) {
				this.cond = c;
				this.conditionLock = l;
				this.threadCount = threadCount;
			}

			@Override
			public void run() {
				conditionLock.acquire();
				for (int i = 1; i &lt;= threadCount; i++) {
					System.out.println("Awaker despertando al sleeper " + i + " de " + threadCount);
					cond.wake();
				}
				conditionLock.release();
			}
		}

		public static void testCondition2() {
			/***** testeando wake ****/
			System.out.println("Testeando wake()");
			Lock conditionLock = new Lock();
			Condition2 cond = new Condition2(conditionLock);
			KThread sleeper = new KThread(new ConditionSleeper(cond, conditionLock, 1));
			sleeper.fork();
			KThread awaker = new KThread(new ConditionAwaker(cond, conditionLock, 1));
			awaker.fork();

			/***** testeando wakeAll ****/
			// Esperar que el test anterior termine
			try {
				sleeper.join();
				awaker.join();
			} catch (Exception e) {
			}
			System.out.println("\n\nTesteando wakeAll()");
			KThread sleeper2 = new KThread(new ConditionSleeper(cond, conditionLock, 2));
			KThread sleeper3 = new KThread(new ConditionSleeper(cond, conditionLock, 3));
			KThread sleeper4 = new KThread(new ConditionSleeper(cond, conditionLock, 4));
			KThread sleeper5 = new KThread(new ConditionSleeper(cond, conditionLock, 5));
			awaker = new KThread(new ConditionAwaker(cond, conditionLock, 4));
			sleeper2.fork();
			sleeper3.fork();
			sleeper4.fork();
			sleeper5.fork();
			awaker.fork();
			
			sleeper5.join();
		}
	}

	public static class PrioritySchedulerTests {
		public static void TestJoin() {

			class JoinTest implements Runnable {
				KThread t;

				public JoinTest(KThread t) {
					this.t = t;
				}

				@Override
				public void run() {
					if (this.t != null) {
						System.out.println(KThread.currentThread().getName() + " joinin " + t.getName());

						this.t.join();

					}
					System.out.println("waiting " + KThread.currentThread().getName() + " to finish");
					ThreadedKernel.alarm.waitUntil(70000);
				}
			}
			KThread t4 = new KThread(new JoinTest(null));
			t4.setName("t4");
			KThread t3 = new KThread(new JoinTest(t4));
			t3.setName("t3");
			KThread t2 = new KThread(new JoinTest(t3));
			t2.setName("t2");
			KThread t1 = new KThread(new JoinTest(t2));
			t1.setName("t1");

			Machine.interrupt().disable();
			ThreadedKernel.scheduler.setPriority(t1, 2);
			ThreadedKernel.scheduler.setPriority(t2, 1);
			ThreadedKernel.scheduler.setPriority(t3, 4);
			ThreadedKernel.scheduler.setPriority(t4, 1);
			Machine.interrupt().enable();

			t1.fork();
			t2.fork();
			t3.fork();
			t4.fork();

			t1.join();
		}

		public static void TestLock() {
			Lock lock = new Lock();

			KThread thigh = new KThread(new Runnable() {
				public void run() {
					System.out.println("high priority thread wants lock");
					lock.acquire();
					System.out.println("high priority thread acquired lock");
				}
			});

			KThread tmid = new KThread(new Runnable() {
				public void run() {
					System.out.println("Mid priority thread started running");
					for (int i = 0; i &lt; 10; i++) {
						System.out.println("Mid priority thread is working");
						KThread.yield();

						if (i == 5) {
							System.out.println("Mid priority thread will fork high priority thread");
							thigh.fork();
						}
					}
				}
			});

			KThread tlow = new KThread(new Runnable() {
				public void run() {
					lock.acquire();
					System.out.println("low priority thread acquired lock");
					System.out.println("low priority thread forking tmid");
					tmid.fork();
					System.out.println("low priority thread will now work");

					for (int i = 0; i &lt; 10; i++) {
						System.out.println("low priority thread is working");
						KThread.yield();
					}
					System.out.println("low priority thread will release lock");
					lock.release();
				}
			});

			Machine.interrupt().disable();
			ThreadedKernel.scheduler.setPriority(tmid, 3);
			ThreadedKernel.scheduler.setPriority(thigh, 5);
			Machine.interrupt().enable();

			tlow.fork();
			tlow.join();
		}

		public static void TestLock2() {
			Lock lock = new Lock();
			KThread t1 = new KThread(new Runnable() {
				public void run() {
					lock.acquire();
					System.out.println("t1 acquired lock");

					Machine.interrupt().disable();
					ThreadedKernel.scheduler.setPriority(2);
					System.out.println("t1 changed priority to 2 and will yield");
					Machine.interrupt().enable();

					KThread.yield();

					System.out.println("t1 will release lock");
					lock.release();
					KThread.yield();
					for (int i = 0; i &lt; 5; i++) {
						System.out.println("t1 working after lock was released");
						KThread.yield();
					}
				}
			});
			KThread t2 = new KThread(new Runnable() {
				public void run() {
					System.out.println("t2 wants lock");
					lock.acquire();
					System.out.println("t2 acquired lock");
					for (int i = 0; i &lt; 5; i++) {
						System.out.println("t2 working");
						KThread.yield();
					}
					lock.release();
				}
			});
			KThread t3 = new KThread(new Runnable() {
				public void run() {
					for (int i = 0; i &lt; 6; i++) {
						System.out.println("t3 working");

						if (i == 3) {
							Machine.interrupt().disable();
							ThreadedKernel.scheduler.setPriority(t2, 7);
							System.out.println("t3 changed t2's priority to 7 and will yield");
							Machine.interrupt().enable();
						}

						KThread.yield();
					}
				}
			});
			Machine.interrupt().disable();
			ThreadedKernel.scheduler.setPriority(t1, 6);
			ThreadedKernel.scheduler.setPriority(t2, 3);
			ThreadedKernel.scheduler.setPriority(t3, 4);
			Machine.interrupt().enable();

			t1.setName("t1").fork();
			t2.setName("t2").fork();
			t3.setName("t3").fork();
			t1.join();
			t2.join();
			t3.join();

		}

		private static void selfTestRun(KThread t1, int t1p, KThread t2, int t2p) {
			boolean int_state;
			int_state = Machine.interrupt().disable();
			ThreadedKernel.scheduler.setPriority(t1, t1p);
			ThreadedKernel.scheduler.setPriority(t2, t2p);
			Machine.interrupt().restore(int_state);
			t1.setName("a").fork();
			t2.setName("b").fork();
			t1.join();
			t2.join();
		}

		/**
		 * 
		 * Tests whether this module is working.
		 * 
		 */

		public static void GESTest() {
			KThread t1, t2, t3;
			final Lock lock;
			final Condition2 condition;
			/*
			 * 
			 * Case 1: Tests priority scheduler without donation
			 * 
			 * 
			 * 
			 * This runs t1 with priority 7, and t2 with priority 4.
			 * 
			 * 
			 * 
			 */
			System.out.println("Case 1:");
			t1 = new KThread(new Runnable() {
				public void run() {
					System.out.println(KThread.currentThread().getName() + " started working");
					for (int i = 0; i &lt; 10; ++i) {
						System.out.println(KThread.currentThread().getName() + " working " + i);
						KThread.yield();
					}
					System.out.println(KThread.currentThread().getName() + " finished working");
				}
			});
			t2 = new KThread(new Runnable() {
				public void run() {
					System.out.println(KThread.currentThread().getName() + " started working");
					for (int i = 0; i &lt; 10; ++i) {
						System.out.println(KThread.currentThread().getName() + " working " + i);
						KThread.yield();
					}
					System.out.println(KThread.currentThread().getName() + " finished working");
				}
			});

			selfTestRun(t1, 7, t2, 4);

			/*
			 * 
			 * Case 2: Tests priority scheduler without donation, altering
			 * 
			 * priorities of threads after they've started running
			 * 
			 * 
			 * 
			 * This runs t1 with priority 7, and t2 with priority 4, but
			 * 
			 * half-way through t1's process its priority is lowered to 2.
			 * 
			 * 
			 * 
			 */

			System.out.println("Case 2:");
			t1 = new KThread(new Runnable() {
				public void run() {
					System.out.println(KThread.currentThread().getName() + " started working");
					for (int i = 0; i &lt; 10; ++i) {
						System.out.println(KThread.currentThread().getName() + " working " + i);
						KThread.yield();
						if (i == 4) {
							System.out
									.println(KThread.currentThread().getName() + " reached 1/2 way, changing priority");
							boolean int_state = Machine.interrupt().disable();
							ThreadedKernel.scheduler.setPriority(2);
							Machine.interrupt().restore(int_state);
						}
					}
					System.out.println(KThread.currentThread().getName() + " finished working");
				}
			});

			t2 = new KThread(new Runnable() {
				public void run() {
					System.out.println(KThread.currentThread().getName() + " started working");
					for (int i = 0; i &lt; 10; ++i) {
						System.out.println(KThread.currentThread().getName() + " working " + i);
						KThread.yield();
					}
					System.out.println(KThread.currentThread().getName() + " finished working");
				}

			});
			selfTestRun(t1, 7, t2, 4);
		}
	}

	public static class AlarmTests {
		public static void test() {
			KThread t1 = new KThread(new Runnable() {
				public void run() {
					for (int i = 0; i &lt; 15; i++) {
						System.out.println("t1");
						ThreadedKernel.alarm.waitUntil(3000000);
					}
				}
			});

			KThread t2 = new KThread(new Runnable() {
				public void run() {
					for (int i = 0; i &lt; 15; i++) {
						System.out.println("t2");
						ThreadedKernel.alarm.waitUntil(6000000);
					}
				}
			});

			System.out.println("Waiting a while before starting test");
			ThreadedKernel.alarm.waitUntil(9000000);
			System.out.println("starting test");
			t2.fork();
			t1.fork();
			t2.join();
			t1.join();
		}
	}

	public static class GESPingTest 
	{

		public static void test() 
		{
			cero = new KThread(new PingTest(0)).setName("forked thread0");
			cero.fork();
			uno = new KThread(new PingTest(1)).setName("forked thread1");
			uno.fork();
			dos = new KThread(new PingTest(2)).setName("forked thread2");
			dos.fork();
			tres = new KThread(new PingTest(3)).setName("forked thread3");
			tres.fork();
			
			cero.join();
			uno.join();
			dos.join();
			tres.join();
		}
		
		public static void alarmTest() 
		{
			cero = new KThread(new PingTest(0, true)).setName("forked thread0");
			cero.fork();
			uno = new KThread(new PingTest(1, true)).setName("forked thread1");
			uno.fork();
			dos = new KThread(new PingTest(2, true)).setName("forked thread2");
			dos.fork();
			tres = new KThread(new PingTest(3, true)).setName("forked thread3");
			tres.fork();
			
			cero.join();
			uno.join();
			dos.join();
			tres.join();
		}
	}

	public static KThread tres = null;

	public static KThread uno = null;

	public static KThread dos = null;

	public static KThread cero = null;

	static class PingTest implements Runnable {

		boolean alarmTest;
		PingTest(int which) {
			this.which = which;
		}
		
		PingTest(int which, boolean alarmTest) {
			this.which = which;
			this.alarmTest = alarmTest;
		}
		
		public void run() {

			for (int i = 0; i &lt; 5; i++) {

				if (alarmTest) {

					if ((which == 2) &amp;&amp; (i == 0)) {

						long time = 1080;

						System.out.println("** " + dos.getName() + " esperara al menos " + time
								+ " ticks, despertara aprox. en " + (Machine.timer().getTime() + time));

						ThreadedKernel.alarm.waitUntil(time);

					}

					if ((which == 3) &amp;&amp; (i == 1)) {

						long time = 540;

						System.out.println("** " + tres.getName() + " esperara al menos " + time
								+ " ticks, despertara aprox. en " + (Machine.timer().getTime() + time));

						ThreadedKernel.alarm.waitUntil(time);

					}

				}

				System.out
						.println("*** thread " + which + " looped " + i + " times, Tick:" + Machine.timer().getTime());

				if ((which == 1) &amp;&amp; (i == 0))

					ThreadedKernel.alarm.waitUntil(1000);

				if ((which == 1) &amp;&amp; (i == 1))

					dos.join();

				if ((which == 0) &amp;&amp; (i == 2))

					dos.join();

				if ((which == 2) &amp;&amp; (i == 3))

					tres.join();

				if ((which == 1) &amp;&amp; (i == 3))

					dos.join();

				KThread.yield();

			}

		}

		private int which;
	}

}


