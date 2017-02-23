import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Exps{
	
	public static void main(String args[]) throws IOException{
		Exps exp = new Exps();
//		exp.timeoutExp(60, OptimizerType.TWO_THIRD);
//		exp.constraintExp();
		exp.constraintTimeoutExp();
	}
	
	
	public void constraintExp() throws FileNotFoundException{
		final String problem = Config.rootDir+"problems/timeout/grid_attractive_n10_w1.0_f0.1.uai";
		
		final int timeLimit = 30;
		final int sampleSize = 9;
		
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		Future<double[]> future1 = executorService.submit(new Callable<double[]>(){
			    public double[] call()  {
				    OptimizerParams params = new OptimizerParams().timeLimit(timeLimit).codeType(CodeType.SPARSE);
				    Hope hope = new Hope(sampleSize, OptimizerType.CPLEX,  params, false);
				    hope.solve(problem);
				    return hope.getLogEstimates();
			    }
		});

		Future<double[]> future2 = executorService.submit(new Callable<double[]>(){
			    public double[] call()  {
				    OptimizerParams params = new OptimizerParams().timeLimit(timeLimit).codeType(CodeType.DENSE);
				    Hope hope = new Hope(sampleSize, OptimizerType.CPLEX, params, false);
				    hope.solve(problem);
				    return hope.getLogEstimates();
			    }
		});
		
		Future<double[]> future3 = executorService.submit(new Callable<double[]>(){
			    public double[] call()  {
				    OptimizerParams params = new OptimizerParams().timeLimit(timeLimit).codeType(CodeType.PEG);
				    Hope hope = new Hope(sampleSize, OptimizerType.LS, params, false);
				    hope.solve(problem);
				    return hope.getLogEstimates();
			    }
		});
		
		double[] sparseEstimates=null;
		double[] denseEstimates=null;
		double[] affineEstimates=null;
		try {
			sparseEstimates = future1.get();
			denseEstimates = future2.get();
			affineEstimates = future3.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		executorService.shutdown();
		
		PrintWriter out = new PrintWriter(Config.outputDir+"constraint.csv");
		out.write("Constraints,Dense Parity,Sparse Parity,Affine Map\n");
		for(int i=0; i<denseEstimates.length;++i){
			out.write(i+","+denseEstimates[i]+","+sparseEstimates[i]+","+affineEstimates[i]+"\n");
		}
		out.close();
	}
	
	
	
	public void constraintTimeoutExp() throws FileNotFoundException{
		final String problem = Config.rootDir+"problems/timeout/grid_attractive_n10_w1.0_f0.1.uai";
		
		final int[] timeLimits = {30, 120, 240, 360, 480, 600};
//		final int[] timeLimits = {30, 120};
		final int[] numConstraints = {16, 50};
		final int sampleSize = 9;
		final int numVars = 100;
		
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		List<Future<Double>> futures  = new LinkedList<Future<Double>>();
		for(int numConstraint: numConstraints){
			final int reducedDim = numVars - numConstraint;
			for(int timeLimit: timeLimits){
				final int t = timeLimit;
				Future<Double> future1 = executorService.submit(new Callable<Double>(){
					    public Double call()  {
						    OptimizerParams params = new OptimizerParams().timeLimit(t).codeType(CodeType.SPARSE);
						    Hope hope = new Hope(sampleSize, OptimizerType.CPLEX,  params, false);
						    return hope.estimateQuantile(problem, numVars, reducedDim, sampleSize).getLogEstimate();
					    }
				});
				Future<Double> future2 = executorService.submit(new Callable<Double>(){
					    public Double call()  {
						    OptimizerParams params = new OptimizerParams().timeLimit(t).codeType(CodeType.DENSE);
						    Hope hope = new Hope(sampleSize, OptimizerType.CPLEX,  params, false);
						    return hope.estimateQuantile(problem, numVars, reducedDim, sampleSize).getLogEstimate();
					    }
				});				
				futures.add(future1);
				futures.add(future2);
				if(timeLimit==30){
					Future<Double> future3 = executorService.submit(new Callable<Double>(){
						    public Double call()  {
							    OptimizerParams params = new OptimizerParams().timeLimit(t).codeType(CodeType.PEG);
							    Hope hope = new Hope(sampleSize, OptimizerType.LS,  params, false);
							    return hope.estimateQuantile(problem, numVars, reducedDim, sampleSize).getLogEstimate();
						    }
					});
					futures.add(future3);
				}

			}
		}
		
		int ind = 0;
		double[][] sparseEstimates= new double[numConstraints.length][timeLimits.length];
		double[][] denseEstimates= new double[numConstraints.length][timeLimits.length];
		double[] affineEstimates= new double[numConstraints.length];
		for(int c=0; c<numConstraints.length;++c){
			for(int t=0; t<timeLimits.length; ++t){
				try {
					sparseEstimates[c][t] = futures.get(ind++).get();
					denseEstimates[c][t] = futures.get(ind++).get();
					if(timeLimits[t]==30)
						affineEstimates[c] = futures.get(ind++).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}	 
		executorService.shutdown();
		
		PrintWriter out = new PrintWriter(Config.outputDir+"constraintTimeout.csv");
		out.write(String.format("Timeout,Sparse%1$d,Sparse%2$d,Dense%1$d,Dense%2$d,Affine%1$d,Affine%2$d\n",
				numConstraints[0], numConstraints[1]));
		for(int t=0; t<timeLimits.length;++t){
			out.write(timeLimits[t]+","+sparseEstimates[0][t]+","+sparseEstimates[1][t]+","
						+denseEstimates[0][t]+","+denseEstimates[1][t]+","
						+affineEstimates[0]+","+affineEstimates[1]+"\n");
		}
		out.close();
	}

	
	public enum SolverType{
		 WISH, HOPE
	 }
	public void timeoutExp(int timeLimit, OptimizerType optimizerType) throws IOException{
		String dataDir = Config.rootDir+"problems/timeout/";
		String outputPath = Config.outputDir+"timeout/";
		SolverType alg = SolverType.HOPE;
		
		int sampleSize = 7;
		CodeType codeType= CodeType.PEG;
		
		String res = null;
		
		switch(alg){
			case WISH:
				System.out.println("TimeoutExp: WISH");
//				Wish wish = new Wish(timeLimit);
//				res = this.timeoutExpAux(dataDir, wish);
				OptimizerParams pwish = new OptimizerParams().timeLimit(timeLimit).codeType(CodeType.DENSE);
				Hope wish = new Hope(sampleSize, optimizerType, pwish, false);
				res = this.timeoutExpAux(dataDir, wish);
				outputPath += "wish";
				break;
			case HOPE:
				System.out.println("TimeoutExp: HOPE");
				OptimizerParams params = new OptimizerParams().timeLimit(timeLimit).codeType(codeType);
				Hope hope = new Hope(sampleSize, optimizerType, params);
				res = this.timeoutExpAux(dataDir, hope);
				res += hope.reportEstimates();
				outputPath += "hope";
				break;
		}
		

		outputPath += "_"+Integer.toString(timeLimit);
		PrintWriter out = new PrintWriter(outputPath);
		System.out.print(res);
		out.print(res);                                                              
	        out.close();
	}
		
	private String timeoutExpAux(String dataDir, Solver solver) throws IOException{
		File folder = new File(dataDir);
		StringBuilder result = new StringBuilder();
		
		for(File f : folder.listFiles()){
			String fileName = f.getName();
			if(fileName.endsWith(".uai")){
				long start = new Date().getTime();
				double estimate = solver.solve(f.getAbsolutePath());
				long end = new Date().getTime();
				result.append(f.getName()).append(",");
				result.append(Math.log(estimate)).append(",");
				result.append( (end-start)/1000);
				result.append("\n");
			}
		}
		return result.toString();
	}

}