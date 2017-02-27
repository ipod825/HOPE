package hope;

import problem.Problem;

public abstract class Optimizer {
	protected OptimizerParams params;
	
	public Optimizer(OptimizerParams params){
		this.params = params;
	}
	
	public void reportParams(){
		System.out.print(String.format("%s", this.getClass().getName()));
		this.params.describe();
	}
	
	public abstract double estimate(Problem problem, int numConstraint);
	public double getOptimalValue(){
		return 0;
	}
}
