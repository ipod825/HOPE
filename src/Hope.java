import java.util.Arrays;
import java.util.Date;


public class Hope implements Solver{
//	private static final String testpath = Config.rootDir+"problems/timeout/grid_attractive_n10_w1.0_f0.1.uai";
	private static final String testpath = Config.rootDir+"problems/test/grid_attractive_n3_w1.0_f1.0.uai";

	private Optimizer[] optimizers;
	Estimate[] estimates;
	
	private boolean earlyStop;
	protected int sampleSize;
	private OptimizerType optimizerType;
	private OptimizerParams params;
	private OptimizerParams lsParams;
	
	
	public Hope(int sampleSize, OptimizerType optimizerType, OptimizerParams params){
		this(sampleSize, optimizerType, params, true, true);
	}
	
	public Hope(int sampleSize, OptimizerType optimizerType, OptimizerParams params, boolean earlyStop){
		this(sampleSize, optimizerType, params, earlyStop, true);
	}

	public Hope(int sampleSize, OptimizerType optimizerType, OptimizerParams params, boolean earlyStop, boolean parallelLs){
		this.sampleSize = sampleSize; 
		this.optimizerType = optimizerType;
		this.params = params;
		this.earlyStop = earlyStop;
		
		this.lsParams = new OptimizerParams(params);
		if(parallelLs){
			if(optimizerType==OptimizerType.LS || optimizerType==OptimizerType.TWO_THIRD){
				int oriTimeLimit = this.params.timeLimit();
				assert sampleSize<=oriTimeLimit;  // Make sure the following for loop will run
				for(int i=sampleSize; i<=oriTimeLimit; i++){
					if(oriTimeLimit%i == 0){
						lsParams.thread(i);
						lsParams.timeLimit(oriTimeLimit/i);
						break;
					}
				}
			}
		}
	}
	
	public static void main(String [] args) {
		try {
			
			
			 int timeLimit=10;
			 int sampleSize = 7;
			 CodeType codeType = CodeType.PEG;
	//		OptimizerType optimizerType = OptimizerType.CPLEX;
			OptimizerType optimizerType = OptimizerType.TWO_THIRD;

			OptimizerParams params = new OptimizerParams();
			params.timeLimit(timeLimit).codeType(codeType);
			
			long start = new Date().getTime();
			Hope hope = new Hope(sampleSize, optimizerType, params);
			double est = hope.solve(testpath);
			long end = new Date().getTime();
			System.out.println("time:"+(end-start)/1000);
			System.out.println("est:"+Math.log(est));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
	public double solve(String path){
		//initial run(unconstrained, full domain)
		Optimizer optimizer = this.getOptimizer();
		optimizer.estimate(path, Optimizer.FULL_DOMAIN);
		int fullDim = optimizer.getOriginalDim();
		this.estimates = new Estimate[fullDim+1];
		double max = optimizer.getOptimalValue();
		estimates[0] = new Estimate(max);
		if(this.earlyStop){
			estimates[fullDim] = estimateQuantile(path, fullDim, 0, sampleSize);
			EarlyStopResult result = null;
			while(! (result=earlyStop()).earlyStop() ){
				Interval current = result.intv;
				int d = (current.end+current.start)/2;
				estimates[d] = this.estimateQuantile(path, fullDim, fullDim-d, sampleSize);
			}
			return result.estZ;
		}
		else{
			for(int d=1;d<fullDim+1;++d)
				estimates[d] = this.estimateQuantile(path, fullDim, fullDim-d, sampleSize);
			//TODO
			return 0;
		}
	}
	
	public Estimate estimateQuantile(String path, int fullDim, int reducedDim, int sampleSize){
		double[] samples = new double[sampleSize];
		this.optimizers = new Optimizer[sampleSize];
		for(int t=0;t<sampleSize;t++)
			this.optimizers[t] = this.getOptimizer(fullDim, reducedDim);
		
		if(this.optimizers[0] instanceof LSOptimizer){
			// LocalSolver can not be parralized
			for(int i=0;i<sampleSize;i++)
				this.optimizers[i].estimate(path, fullDim);
		}else{
			final int f = fullDim;
			final String p = path;
			Parallel.For(0, sampleSize, new LoopBody <Integer>(){
				public void run(Integer i){
					optimizers[i].estimate(p, f);
				}
			});
		  }
		  
		  for(int i=0;i<sampleSize;i++)
			  samples[i]=optimizers[i].getOptimalValue();
		  Arrays.sort(samples);
		  System.out.println("Estimates: "+Arrays.toString(samples));
		  return  new Estimate(samples[sampleSize/2]);
	  }
	  
	  
	public Optimizer getOptimizer(){
		return this.getOptimizer(-1, -1);
	}
	  
	public Optimizer getOptimizer(int fullDim, int reducedDim){
		OptimizerType optimizerType = this.optimizerType;
		if(optimizerType==OptimizerType.TWO_THIRD){
			double r = reducedDim/(double)fullDim;
			if(fullDim==-1 || r*3>2 )
				optimizerType = OptimizerType.CPLEX;
			else
				optimizerType = OptimizerType.LS;
		}
		if(optimizerType==OptimizerType.CPLEX)
			return new CplexOptimizer(this.params, reducedDim);
		else
			return new LSOptimizer(this.lsParams, reducedDim);
	}
	  
	  
	public double[] getLogEstimates(){
		double[] res = new double[estimates.length];
		for(int i=this.estimates.length-1;i>=0;i--)
			res[i] = this.estimates[i].getLogEstimate();
		return res;
	}
	
	public String reportEstimates(){
		StringBuilder sb = new StringBuilder();
		int save=0;
		for(int i=this.estimates.length-1;i>=0;i--){
			if(this.estimates[i]!=null)
				sb.append("e").append(i).append(":").append(this.estimates[i].getEstimate()).append("\n");
			else
				++save;
		}
		sb.append("Saving:").append((double)save/this.estimates.length).append("\n");
		return sb.toString();
	}

  
	class EarlyStopResult{
		Interval intv;
		double estZ;
		
		public EarlyStopResult(Interval intv, double estZ){
			this.intv=intv;
			this.estZ=estZ;
		}
		
		public boolean earlyStop(){
			return intv==null;
		}
	}
	
	class Interval{
		int start;
		int end;
		double diff;
		
		public Interval(double diff){
			this.diff=diff;
		}
		public Interval(){
			this.diff=0;
		}
	}
  
    
  private EarlyStopResult earlyStop(){
	  Estimate current = estimates[0];
	  double highSum=current.getArea(0);
	  double[] highValues = new double[estimates.length];
	  highValues[0]=current.getEstimate();	  
	  for(int i=0;i<estimates.length-1;i++){
		  if(estimates[i]!=null){
			  current = estimates[i];
		  }
		  highValues[i+1] = current.getEstimate();
		  highSum += current.getArea(i);
	  }
	  
	  double lowSum=0;
	  double[] lowValues = new double[estimates.length];
	  Interval maxDiffIntv = new Interval(-1);
	  Interval currentIntv = null;
	  maxDiffIntv.end=estimates.length-1;
	  for(int i=estimates.length-1;i>=0;i--){
		  //log width of current interval
		  int logWidth=i-1>=0?i-1:0;
		  if(estimates[i]!=null){
			  current = estimates[i];
			  if(i<estimates.length-1){
				  currentIntv.start=i+1;
				  if(currentIntv.diff>maxDiffIntv.diff && currentIntv.end>currentIntv.start){
					  maxDiffIntv=currentIntv;
				  }
			  }
			  currentIntv = new Interval();
			  currentIntv.end = i;
		  }
		  lowSum += current.getArea(logWidth);
		  lowValues[i] = current.getEstimate();
		  currentIntv.diff += current.getArea((highValues[i]-lowValues[i]), logWidth);
	  }
	  System.out.print(this.reportEstimates());
	  
	  if(highSum/lowSum<3){
		  //early stop
		  return new EarlyStopResult(null,(highSum+lowSum)/2);
	  }
	  return new EarlyStopResult(maxDiffIntv,0);
  }
  
  
  class Estimate{
	  double value=0;
	  
	  public Estimate(double value){
		  this.value=value;
	  }
	  
	  public double getLogEstimate(){
		  return this.value;
	  }
	  
	  public double getEstimate(){
		  return Math.pow(Math.E, this.value);
	  }
	  public double getArea(int logWidth){
		  return this.getArea(this.getEstimate(), logWidth);
	  }
	  
	  public double getArea(double est, int logWidth){
		  return est*Math.pow(2, logWidth);
	  }
  }
  
 
}