package hope;
import java.util.Random;

import problem.Problem;

import code.CodeType;

import localsolver.LSExpression;
import localsolver.LSModel;
import localsolver.LSObjectiveDirection;
import localsolver.LSOperator;
import localsolver.LSPhase;
import localsolver.LSSolution;
import localsolver.LocalSolver;


public class LSOptimizer extends Optimizer{

	public LSOptimizer(OptimizerParams params) {
		super(params);
	}
	
	class LSProblem{
		boolean[] ini;
		Variables vars;
		LSExpression solverObjective;
		LSExpression originalObjective;
		public LSProblem(LSExpression objective, Variables vars){
			this.vars=vars;
			this.solverObjective=objective;
			this.originalObjective=objective;
		}
		
		public LSProblem(LSExpression solverObjective, LSExpression originalObjective, Variables vars){
			this.vars=vars;
			this.solverObjective=solverObjective;
			this.originalObjective=originalObjective;
		}
		public LSExpression[] getX(){
			return vars.x;
		}
		public LSExpression[] getY(){
			return vars.y;
		}
	}

	
	public double estimate(Problem problem, int numConstraint){
		LocalSolver localsolver = new LocalSolver();
		LSModel model = localsolver.getModel();
		LSProblem prob=null;
		//TODO why 0?
		double res = Double.NaN;;

		int fullDim = problem.getNumVar();
		int reducedDim = fullDim-numConstraint;
		prob = this.loadProblem(problem, model, fullDim, reducedDim);
		
		model.addObjective(prob.solverObjective, LSObjectiveDirection.Maximize);
		model.close();
		setSolverParams(localsolver);
		LSPhase phase = localsolver.createPhase();
		phase.setTimeLimit(this.params.timeLimit());
		
		localsolver.solve();
		LSSolution sol = localsolver.getSolution();
		res = sol.getDoubleValue(prob.originalObjective);	

		localsolver.delete();
		assert !Double.isNaN(res);
		return res;
	}
	
	private void setSolverParams(LocalSolver localsolver){
		Random rand = new Random();
		localsolver.getParam().setSeed(rand.nextInt(1000000));
		localsolver.getParam().setNbThreads(this.params.thread());
		localsolver.getParam().setAnnealingLevel(9);
		localsolver.getParam().setVerbosity(0);
	}
	
	
	private LSProblem loadProblem(Problem problem, LSModel model, int fullDim, int reducedDim){
		LSExpression[] x = null;
		Variables variables = null;
		LSExpression objective = model.createExpression(LSOperator.Sum);
		
		assert reducedDim<=fullDim;
		if(reducedDim==fullDim){
			//unconstrained optimization
			variables = createVar(model, fullDim);	
		}else{
			//optimize in the reduced domain
			variables = createTransformedVar(model, fullDim, reducedDim);
		}
		x=variables.x;
		for(int i=0;i<problem.getNumFn();++i){
			LSExpression funcVal = this.createIfTree(model, x, problem.getFnVarInd()[i], 0, 0, problem.getFnVal()[i]);
			objective.addOperand(model.createExpression(LSOperator.Log,funcVal));
		}
		return new LSProblem(objective,variables);
	}

	
	private LSExpression createIfTree(LSModel model, LSExpression[] x, int[] funcVars, int varIdx, int valIdx, double[] funcVals){
		int len = funcVars.length;
		int positiveIdx = (int) (valIdx + Math.pow(2,len-varIdx-1));
		if(varIdx==len-1){
			return model.createExpression(LSOperator.If, x[funcVars[varIdx]], funcVals[positiveIdx], funcVals[valIdx]);			
		}else{
			return model.createExpression(LSOperator.If, x[funcVars[varIdx]], createIfTree(model, x, funcVars, varIdx+1, positiveIdx, funcVals),
					createIfTree(model, x, funcVars, varIdx+1, valIdx, funcVals));
		}
	}
	
	class Variables{
		LSExpression[] x;
		LSExpression[] y;
		
		public Variables(LSExpression[] x,LSExpression[] y){
			this.x=x;
			this.y=y;
		}
		
		public Variables(LSExpression[] x1,LSExpression[] x2,LSExpression[] y1,LSExpression[] y2){
			this.x = this.mergeVariables(x1, x2);
			this.y = this.mergeVariables(y1, y2);
		}
		
		public Variables(Variables v1, Variables v2){
			this.x = this.mergeVariables(v1.x, v2.x);
			this.y = this.mergeVariables(v1.y, v2.y);
		}
		
		private LSExpression[] mergeVariables(LSExpression[] x1, LSExpression[] x2){
			LSExpression[] x = new LSExpression[x1.length+x2.length];
			for(int i=0;i<x1.length;i++){
				x[i]=x1[i];
			}
			for(int i=x1.length;i<x.length;i++){
				x[i]=x2[i-x1.length];
			}
			return x;
		}
	}
	
	private Variables createTransformedVar(LSModel model, int fullDim, int reducedDim){
		//x: the variables in 2^n
		//y: the variables in 2^d, d<n
		LSExpression[] y = new LSExpression[reducedDim];
		for (int i = 0; i < y.length; i++){ 
			y[i] = model.createExpression(LSOperator.Bool);
		}

		boolean offset[] = BinaryMatrixHelper.getRandomVector(fullDim);
				
		if(fullDim == reducedDim){
			return new Variables(y,y);
		}
		
		LSExpression[] x = new LSExpression[fullDim];
		
		boolean matrix[][]=null;;
		if(this.params.codeType()==CodeType.PEG){
			boolean[][] parity = LDPCTools.getPEGMatrix(fullDim, fullDim-reducedDim, false);
			if(parity != null){
				matrix = BinaryMatrixHelper.parityToGenerator(parity);	
			}
		}
		if(matrix == null){
			matrix = BinaryMatrixHelper.getFullRankMatrix(fullDim);
			BinaryMatrixHelper.gaussJordanElimination(matrix, reducedDim);
		}
		
		for(int i=0;i<x.length;i++){
			x[i] = model.createExpression(LSOperator.Xor);

			if(offset[i]){
				x[i].addOperand(1);
			}

			for(int j=0;j<reducedDim;j++){
				if(matrix[j][i]){
					x[i].addOperand(y[j]);
				}
			}
		}

		return new Variables(x,y);
	}

	private Variables createVar(LSModel model, int fullDim){
		LSExpression[] x = new LSExpression[fullDim];
		for (int i = 0; i < x.length; i++){ 
			x[i] = model.createExpression(LSOperator.Bool);
		}
		return new Variables(x,x);
	}

}