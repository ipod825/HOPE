package hope;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;

import problem.Problem;
import utils.LoopBody;
import utils.Parallel;


public class Wish implements Solver{

	private OptimizerParams params;
	private int sampleSize;
	
	public Wish(int sampleSize, OptimizerParams params){
		this.sampleSize = sampleSize;
		this.params = params;
	}
	
	
	public void estimateQuantile(final Problem problem, final int numConstraint){
		final Optimizer[] optimizers = new Optimizer[this.sampleSize];
		for(int i=0;i<sampleSize;i++)
			optimizers[i] = new CplexOptimizer(params);;
		
		Parallel.For(0, sampleSize, new LoopBody <Integer>(){
			public void run(Integer i){
				optimizers[i].estimate(problem, numConstraint);
			}
		});
	  }
	
	public double solve(final Problem problem) throws ExecuteException, IOException{
		final int fullDim = problem.getNumVar();
		CplexOptimizer cplex = new CplexOptimizer(this.params);
		cplex.estimate(problem, 0);
	
		for(int d=1;d<fullDim+1;++d)
			this.estimateQuantile(problem, problem.getNumVar()-d);
		
		return 0;
//	        CommandLine cl = new CommandLine("python");
//	        cl.addArgument(Config.pathToWISHLogProcessor);
//	        cl.addArgument(path);
//	        cl.addArgument("log_"+new Date().getTime());
//	        cl.addArgument("-timeout");
//	        cl.addArgument(""+this.timeout);

	}
}