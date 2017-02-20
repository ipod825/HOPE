
public abstract class Optimizer {
	public static final int FULL_DOMAIN = -1;
	protected int mReducedDim=FULL_DOMAIN;
	protected InstanceParams mParams = null;  
	public Optimizer(InstanceParams params){
		this.mReducedDim = FULL_DOMAIN;
		
		if(params!=null){
			this.mParams = params;
			System.out.println(String.format("%s, constraints: %s, domain: %d", 
					this.getSolver(), params.constraint, this.mReducedDim));
		}else{
			this.mParams = new RunParams().getDefaultInstanceParams();
		}
	}
	
	public Optimizer(InstanceParams params, int reducedDim){
		this.mReducedDim = reducedDim;
		
		if(params!=null){
			this.mParams = params;
			System.out.println(String.format("%s, constraints: %s, domain: %d", 
					this.getSolver(), params.constraint, this.mReducedDim));
		}else{
			this.mParams = new RunParams().getDefaultInstanceParams();
		}
	}
	
	public abstract void estimate(String path, int dim);
	public abstract double getOptimalValue();
	public abstract double getRuntime();
	public abstract int getOriginalDim();
	public abstract String getSolver();
}
