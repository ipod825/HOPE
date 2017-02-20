import java.util.Arrays;
import java.util.Date;


public class Hope {
	private static final String testpath = Config.rootDir+"problems/timeout/grid_attractive_n10_w1.0_f0.1.uai";
//	private static final String testpath = Config.rootDir+"problems/test/grid_attractive_n3_w1.0_f1.0.uai";

	private Optimizer[] optimizers;
	private boolean parallel = true;
	
  public static void main(String [] args) {
	  try {
		Hope hope = new Hope();
		long start = new Date().getTime();
		RunParams params = new RunParams(10, ConstraintType.PARITY_CONSTRAINED,
				CodeType.PEG, OptimizerType.CPLEX);
		double est = hope.solve(testpath, 7, params);
		long end = new Date().getTime();
		System.out.println("time:"+(end-start)/1000);
		System.out.println("est:"+Math.log(est));
	} catch (Exception e) {
		e.printStackTrace();
	}
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
  
  private double countSaving(Estimate[] estimates){
	  if(estimates==null)
		  return 0;
	  int sum=0;
	  for(int i=0;i<estimates.length;i++){
		  if(estimates[i]==null)
			  sum++;
	  }
	  return (double)sum/estimates.length;
  }
  
  private EarlyStopResult earlyStop(Estimate[] estimates){
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
			  System.out.println("e"+i+":"+current.getEstimate());
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
	  System.out.println("h"+highSum);
	  System.out.println("l"+lowSum);
	  if(highSum/lowSum<3){
		  //early stop
		  return new EarlyStopResult(null,(highSum+lowSum)/2);
	  }
	  return new EarlyStopResult(maxDiffIntv,0);
  }
  
  private Estimate estimateQuantile(String path, RunParams params, int fullDim, int reducedDim, int sampleSize){
	  double[] samples = new double[sampleSize];

	  this.optimizers = new Optimizer[sampleSize];
	  for(int t=0;t<sampleSize;t++)
		  this.optimizers[t] = params.getOptimizer(path, fullDim, reducedDim);

	  if(this.parallel){
		  final int f = fullDim;
		  final String p = path;
		  Parallel.For(0, sampleSize, new LoopBody <Integer>(){
			  public void run(Integer i){
				  optimizers[i].estimate(p, f);
			 }
		});
	  }else{
		  for(int i=0;i<sampleSize;i++)
			  this.optimizers[i].estimate(path, fullDim);	  
	  }
	  
	  
	  for(int i=0;i<sampleSize;i++)
		  samples[i]=optimizers[i].getOptimalValue();
	  Arrays.sort(samples);
	  System.out.print(Math.exp(samples[sampleSize/2]));
	  System.out.println(Arrays.toString(samples));
	  return  new Estimate(samples[sampleSize/2]);
  }
  

  public double solve(String path, int sampleSize, RunParams params, RunResult runResult){
	  	//initial run(unconstrained, full domain)
		Optimizer optimizer = params.getOptimizer(path);
		optimizer.estimate(path, Optimizer.FULL_DOMAIN);
		int fullDim = optimizer.getOriginalDim();
		Estimate[] estimates = new Estimate[fullDim+1];
		double max = optimizer.getOptimalValue();
		estimates[0] = new Estimate(max);
		estimates[fullDim] = estimateQuantile(path, params, fullDim, 0, sampleSize);
		EarlyStopResult result = null;
		while(! (result=earlyStop(estimates)).earlyStop() ){
			Interval current = result.intv;
			int d = (current.end+current.start)/2;  
			estimates[d]=estimateQuantile(path, params, fullDim, fullDim-d, sampleSize);
		}
		double saving = this.countSaving(estimates);
		System.out.println("saving:"+saving);
		if(runResult!=null){
			runResult.saving=saving;
			runResult.estimate=Math.log(result.estZ);
		}
		return result.estZ;
}
  
  public double solve(String path, int sampleSize, RunParams params){
	  	return solve(path,sampleSize,params, null);
  }
  
  class Estimate{
	  double value=0;
	  
	  public Estimate(double value){
		  this.value=value;
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