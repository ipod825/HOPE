package hope;

import java.io.File;
import java.util.Date;

import code.CodeType;
import problem.Problem;
import utils.LoopBody;
import utils.Parallel;
import utils.Utils;


public class Wish implements Solver{

	private OptimizerParams params;
	private int sampleSize;
	private String outputPrefix = null;
	
	public Wish(int sampleSize, OptimizerParams params){
		this.sampleSize = sampleSize;
		this.params = params;
		this.params.codeType(CodeType.DENSE);
	}
	
	public void estimateQuantile(final Problem problem, final int numConstraint, final int sampleSize){
		 final Optimizer[] optimizers = new Optimizer[sampleSize];
        	 for(int i=0;i<sampleSize;i++){
        		 String outputPath = this.outputPrefix+
        				 String.format("%d.xor%d.loglen0.%d.ILOGLUE.uai.LOG", Thread.currentThread().getId(), numConstraint, i+1);
        			
//        		 optimizers[i] = new CplexOptimizer(this.params, outputPath);
		}
		
//        	 for(int i=0;i<sampleSize;++i){
//        		 optimizers[i].estimate(problem, numConstraint);
//        	 }
		Parallel.For(0, sampleSize, new LoopBody <Integer>(){
			public void run(Integer i){
				optimizers[i].estimate(problem, numConstraint);
			}
		});
	}
	
	public double solve(final Problem problem){
		final int fullDim = problem.getNumVar();
		
		String outputDir = Config.tmpDir + "wish" + new Date().getTime() + "/";
		new File(outputDir).mkdir();
		this.outputPrefix = outputDir + Utils.basename(problem.getPath());
		this.estimateQuantile(problem, 0, 1);
		
	         for(int c=1;c<fullDim+1;++c){
	        	 this.estimateQuantile(problem, c, this.sampleSize);
	        	 System.out.println("Constraint "+c);
	         }
		
	         CmdOutputHandler handler = new CmdOutputHandler("Final log-estimate: ");
	         return handler.runCmd(String.format("python %s %s",Config.pathToWISHLogProcessor, outputDir));
	}
}