package utils;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Parallel {
	    public static final int iCPU = Runtime.getRuntime().availableProcessors();
	    public static <T> void ForEach(Iterable <T> parameters, final LoopBody<T> loopBody) {
		    ExecutorService executor = Executors.newFixedThreadPool(iCPU);
		    List<Future<?>> futures  = new LinkedList<Future<?>>();
		    for (final T param : parameters) {
			    Future<?> future = executor.submit(new Runnable(){
				    	public void run() { loopBody.run(param); }
			    });
			    futures.add(future);
		    }
		    for (Future<?> f : futures) {
			    try {
				    f.get();
			    } catch (InterruptedException e) {
				e.printStackTrace();
		            } catch (ExecutionException e) {
				e.printStackTrace();
		            }
		    }
		    executor.shutdown();
	    }
	
	    public static void For(int start, int stop, final LoopBody<Integer> loopBody) {
		    Parallel.For(start, stop, iCPU, loopBody);
	    }
	    
	    public static void For(int start, int stop, int numThread, final LoopBody<Integer> loopBody) {
	        numThread = numThread>0? numThread: 1;
		    ExecutorService executor = Executors.newFixedThreadPool(numThread);
		    List<Future<?>> futures  = new LinkedList<Future<?>>();
		    for (int i=start; i<stop; i++) {
			    final Integer k = i;
			    Future<?> future = executor.submit(new Runnable(){
				    public void run() { loopBody.run(k); }
			    });
			    futures.add(future);
		    }
		    
		    for (Future<?> f : futures) {
			    try {
				    f.get();
			    } catch (InterruptedException e) {
				    e.printStackTrace();
			    } catch (ExecutionException e) {
				    e.printStackTrace();
			    }
		    }
		    executor.shutdown();
	    }
	}
