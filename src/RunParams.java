

public class RunParams {
	protected boolean logScale=true;
	protected int timeLimit=10;
	protected int retry = 1;
	protected ConstraintType constrained = ConstraintType.UNCONSTRAINED;
	protected double softStrength = 0;
	protected CodeType code=CodeType.DENSE;

	private SolverType solver = SolverType.LS;
	
	private ConstraintType bestConstrainedType=ConstraintType.UNCONSTRAINED;
		
	
	public RunParams(){
		
	}
	
	public boolean isLogScale(){
		return this.logScale;
	}
	public RunParams(boolean logScale, int timeLimit, int retry, ConstraintType constrained, CodeType code, SolverType solver){
		this.logScale=logScale;
		if(timeLimit>0){
			this.timeLimit=timeLimit;
		}
		if(retry>0){
			this.retry=retry;
		}
		this.constrained=constrained;
		if(constrained!=ConstraintType.GO_WITH_THE_BEST){
			bestConstrainedType = constrained;
		}
		this.code = code;
		this.solver = solver;
	}
	
	
	public RunParams(boolean logScale, int timeLimit, ConstraintType constrained, CodeType code, SolverType solver){
		this.logScale=logScale;
		if(timeLimit>0){
			this.timeLimit=timeLimit;
		}
		this.retry=1;
		this.constrained=constrained;
		if(constrained!=ConstraintType.GO_WITH_THE_BEST){
			bestConstrainedType = constrained;
		}
		this.code = code;
		this.solver = solver;
	}
	public SolverType getSolverType(){
		return this.solver;
	}
	
	public InstanceParams getInstanceParams(ConstraintType ct){
		InstanceParams p =  new InstanceParams(this);
		p.constrained = ct;
		return p;
	}
	
	public ConstraintType getConstraintTypeByEntropy(int fullDim, int reducedDim){
		double r = reducedDim/(double)fullDim;
		if( 1-Utils.entropy(r)>0.5 ){
			return ConstraintType.PARITY_CONSTRAINED;
		}else{
			return ConstraintType.UNCONSTRAINED;
		}
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
		if(this.goWithTheBest()){
			p.constrained = this.bestConstrainedType;	
		}
		return p;
	}
	
	public InstanceParams getDefaultInstanceParams(){
		InstanceParams p =  new InstanceParams(this);
		p.constrained = ConstraintType.UNCONSTRAINED; 
		return p;
	}
	
	public boolean twoThirdSwitch(){
		return this.constrained == ConstraintType.TWO_THIRD;
	}
	public boolean goWithTheBest(){
		return this.constrained == ConstraintType.GO_WITH_THE_BEST;
	}
	
	public void setBestContrainedType(ConstraintType type){
		this.bestConstrainedType=type;
	}
	
	public Instance getInstance(String path, int fullDim, int reducedDim){
		if(this.solver==SolverType.BY_CONSTRAINTS){
			if(this.goWithTheBest()){
				return this.bestConstrainedType==ConstraintType.UNCONSTRAINED?new LSInstance(path,this.getInstanceParams(),reducedDim):
					new CplexInstance(path,this.getInstanceParams(),reducedDim);
			}else{
				ConstraintType ct = this.getConstraintTypeByTwoThirdRule(fullDim, reducedDim);
				return ct==ConstraintType.UNCONSTRAINED?new LSInstance(path,this.getInstanceParams(ct),reducedDim):
					new CplexInstance(path,this.getInstanceParams(ct),reducedDim);
			}
		}else if(this.solver==SolverType.CPLEX){
			return new CplexInstance(path,this.getInstanceParams(),reducedDim);
		}else{
			return new LSInstance(path,this.getInstanceParams(),reducedDim);
		}
	}
	
	public Instance getInstance(String path, int fullDim, int reducedDim, double stopValue){
		if(this.solver==SolverType.BY_CONSTRAINTS){
			if(this.goWithTheBest()){
				return this.bestConstrainedType==ConstraintType.UNCONSTRAINED?new LSInstance(path,this.getInstanceParams(),reducedDim):
					new CplexInstance(path,this.getInstanceParams(),reducedDim, stopValue);
			}else{
				ConstraintType ct = this.getConstraintTypeByEntropy(fullDim, reducedDim);
				return ct==ConstraintType.UNCONSTRAINED?new LSInstance(path,this.getInstanceParams(ct),reducedDim):
					new CplexInstance(path,this.getInstanceParams(ct),reducedDim, stopValue);
			}
		}else if(this.solver==SolverType.CPLEX){
			return new CplexInstance(path,this.getInstanceParams(),reducedDim, stopValue);
		}else{
			return new LSInstance(path,this.getInstanceParams(),reducedDim, stopValue);
		}
	}
	
	public Instance getInstance(String path){
		if(this.solver==SolverType.BY_CONSTRAINTS){
			return new CplexInstance(path,this.getInstanceParams());	
		}else if(this.solver==SolverType.CPLEX){
			return new CplexInstance(path,this.getInstanceParams());
		}else{
			return new LSInstance(path,this.getInstanceParams());
		}
	}
}
