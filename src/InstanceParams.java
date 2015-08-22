


public class InstanceParams{
	protected boolean logScale=true;
	protected int timeLimit=10;
	protected int retry = 1;
	protected ConstraintType constrained = ConstraintType.UNCONSTRAINED;
	protected double softStrength = 0;
	protected CodeType code=CodeType.DENSE;
	
	public InstanceParams(RunParams parent){
		this.logScale=parent.logScale;
		this.timeLimit=parent.timeLimit;
		this.retry = parent.retry;
		this.constrained = parent.constrained;
		this.softStrength = parent.softStrength;
		this.code=parent.code;
	}
	
	public void setSoftConstrainStrength(double str){
		this.softStrength=str;
	}
	
	public double getSoftConstrainedStrength(){
		return this.softStrength;
	}
	
	public boolean isUnconstrained(){
		return this.constrained==ConstraintType.UNCONSTRAINED;
	}
	
	public boolean isSoftConstrained(){
		return this.constrained==ConstraintType.SOFT_CONSTRAINED;
	}
	
	
	public boolean isDense(){
		return this.code==CodeType.DENSE;
	}
	
	public boolean isRegularlySparse(){
		return this.code==CodeType.REGULAR;
	}
	
	public boolean isRegularPEG(){
		return this.code==CodeType.PEG_REGULAR;
	}
	
//	public boolean isIrregular(){
//		return this.code==CodeType.IRREGULAR;
//	}
	
//	public boolean is63(){
//		return this.code==CodeType.LDPC63;
//	}
	
	public boolean isPEG(){
		return this.code==CodeType.PEG;
	}
	
	public boolean isLogScale(){
		return this.logScale;
	}
	
	public int getTimeLimit(){
		return this.timeLimit;
	}
	
	public int getNumOfRetry(){
		return this.retry;
	}
}
