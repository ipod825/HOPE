


public class InstanceParams{
	protected int timeLimit=10;
	protected ConstraintType constraint = ConstraintType.UNCONSTRAINED;
	protected CodeType code=CodeType.DENSE;
	
	public InstanceParams(RunParams parent){
		this.timeLimit=parent.timeLimit;
		this.constraint = parent.constraint;
		this.code=parent.code;
	}
	
	
	public boolean isUnconstrained(){
		return this.constraint==ConstraintType.UNCONSTRAINED;
	}
	
	public boolean isDense(){
		return this.code==CodeType.DENSE;
	}
		
	public boolean isRegularPEG(){
		return this.code==CodeType.PEG_REGULAR;
	}
	
	public boolean isPEG(){
		return this.code==CodeType.PEG;
	}
	
	public int getTimeLimit(){
		return this.timeLimit;
	}
}
