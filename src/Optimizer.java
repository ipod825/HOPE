
public abstract class Optimizer {
	public static final int FULL_DOMAIN = -1;
	protected int mReducedDim=FULL_DOMAIN;
	protected CodeType code;
	protected int timeLimit;
	public Optimizer(CodeType code, int timeLimit){
		this(code, timeLimit, FULL_DOMAIN);
	}
	
	public Optimizer(CodeType code, int timeLimit, int reducedDim){
		this.code = code;
		this.timeLimit = timeLimit;
		this.mReducedDim = reducedDim;
		System.out.println(String.format("%s, domain: %d, timeLimit: %d", this.getSolver(),  reducedDim, timeLimit));
	}
	
	public abstract void estimate(String path, int dim);
	public abstract double getOptimalValue();
	public abstract double getRuntime();
	public abstract int getOriginalDim();
	public abstract String getSolver();
}
