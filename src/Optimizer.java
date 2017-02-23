
public abstract class Optimizer {
	public static final int FULL_DOMAIN = -1;
	protected int mReducedDim=FULL_DOMAIN;
	protected OptimizerParams params;
	public Optimizer(OptimizerParams params){
		this(params, FULL_DOMAIN);
	}
	
	public Optimizer(OptimizerParams params, int reducedDim){
		this.params = params;
		this.mReducedDim = reducedDim;
		System.out.print(String.format("%s, domain: %d", this.getSolver(),  reducedDim));
		this.params.describe();
	}
	
	public abstract void estimate(String path, int dim);
	public abstract double getOptimalValue();
	public abstract double getRuntime();
	public abstract int getOriginalDim();
	public abstract String getSolver();
}
