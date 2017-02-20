

public class RunParams {
	protected int timeLimit=10;
	protected ConstraintType constraint = ConstraintType.UNCONSTRAINED;
	protected CodeType code=CodeType.DENSE;
	private OptimizerType optimizer = OptimizerType.LS;
	
	public RunParams(){}
	
	public RunParams(int timeLimit, ConstraintType constraint, CodeType code, OptimizerType optimizer){
		this.timeLimit=timeLimit;
		this.constraint=constraint;
		this.code = code;
		this.optimizer = optimizer;
	}

	public OptimizerType getOptimizerType(){
		return this.optimizer;
	}
	
	public InstanceParams getInstanceParams(ConstraintType ct){
		InstanceParams p =  new InstanceParams(this);
		p.constraint = ct;
		return p;
	}
	public ConstraintType getConstraintTypeByTwoThirdRule(int fullDim, int reducedDim){
		double r = reducedDim/(double)fullDim;
		if( r*3>2 ){
			return ConstraintType.PARITY_CONSTRAINED;
		}else{
			return ConstraintType.UNCONSTRAINED;
		}
	}
	
	public InstanceParams getInstanceParams(){
		InstanceParams p =  new InstanceParams(this);
		return p;
	}
	
	public InstanceParams getDefaultInstanceParams(){
		InstanceParams p =  new InstanceParams(this);
		p.constraint = ConstraintType.UNCONSTRAINED; 
		return p;
	}
	
	public Optimizer getOptimizer(String path, int fullDim, int reducedDim){
		if(this.optimizer==OptimizerType.BY_CONSTRAINTS){
			ConstraintType ct = this.getConstraintTypeByTwoThirdRule(fullDim, reducedDim);
			return ct==ConstraintType.UNCONSTRAINED?new LSOptimizer(this.getInstanceParams(ct),reducedDim):
				new CplexOptimizer(this.getInstanceParams(ct),reducedDim);
		}else if(this.optimizer==OptimizerType.CPLEX){
			return new CplexOptimizer(this.getInstanceParams(),reducedDim);
		}else{
			return new LSOptimizer(this.getInstanceParams(),reducedDim);
		}
	}
	
	public Optimizer getOptimizer(String path){
		if(this.optimizer==OptimizerType.BY_CONSTRAINTS){
			return new CplexOptimizer(this.getInstanceParams());	
		}else if(this.optimizer==OptimizerType.CPLEX){
			return new CplexOptimizer(this.getInstanceParams());
		}else{
			return new LSOptimizer(this.getInstanceParams());
		}
	}
}
