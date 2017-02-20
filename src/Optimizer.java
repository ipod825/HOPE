
public abstract class Optimizer {
	public static final int FULL_DOMAIN = -1;
	protected int mReducedDim=FULL_DOMAIN;
	protected ConstraintType constraint;
	protected CodeType code;
	protected int timeLimit;
	public Optimizer(ConstraintType constraint, CodeType code, int timeLimit){
		this(constraint, code, timeLimit, FULL_DOMAIN);
	}
	
	public Optimizer(ConstraintType constraint, CodeType code, int timeLimit, int reducedDim){
		this.constraint = constraint;
		this.code = code;
		this.timeLimit = timeLimit;
		this.mReducedDim = reducedDim;
		System.out.println(String.format("%s, constraints: %s, domain: %d, timeLimit: %d", 
				this.getSolver(),  constraint, reducedDim, timeLimit));
	}
	
	public abstract void estimate(String path, int dim);
	public abstract double getOptimalValue();
	public abstract double getRuntime();
	public abstract int getOriginalDim();
	public abstract String getSolver();
}
