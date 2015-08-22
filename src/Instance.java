
public abstract class Instance {
	protected String mPath=null;
	public static final int FULL_DOMAIN = -1;
	protected int mReducedDim=FULL_DOMAIN;
	protected InstanceParams mParams = null;
	protected double stopValue; 
	protected boolean stopUntilReached = false; 
	public Instance(String path, InstanceParams params){
		this.mPath = path;
		this.mReducedDim = FULL_DOMAIN;
		
		if(params!=null){
			this.mParams = params;
			System.out.println("constraints:"+params.constrained);
			System.out.println("code:"+params.code);
			System.out.println("solver:"+this.getSolver());
			System.out.println("domain:full");
		}else{
			this.mParams = new RunParams().getDefaultInstanceParams();
		}
	}
	
	public Instance(String path, InstanceParams params, int reducedDim){
		this.mPath = path;
		this.mReducedDim = reducedDim;
		
		if(params!=null){
			this.mParams = params;
			System.out.println("constraints:"+params.constrained);
			System.out.println("code:"+params.code);
			System.out.println("solver:"+this.getSolver());
			System.out.println("domain:"+reducedDim);
		}else{
			this.mParams = new RunParams().getDefaultInstanceParams();
		}
	}
	
	public Instance(String path, InstanceParams params, int reducedDim, double stopValue){
		this.mPath = path;
		this.mReducedDim = reducedDim;
		
		if(params!=null){
			this.mParams = params;
			System.out.println("constraints:"+params.constrained);
			System.out.println("code:"+params.code);
			System.out.println("solver:"+this.getSolver());
			System.out.println("domain:"+reducedDim);
		}else{
			this.mParams = new RunParams().getDefaultInstanceParams();
		}
		
		this.stopUntilReached = true;
		this.stopValue = stopValue;
	}
	
	public abstract void solve(int dim);
	public abstract double getOptimalValue();
	public abstract double getRuntime();
	public abstract int getOriginalDim();
	public abstract String getSolver();
}
