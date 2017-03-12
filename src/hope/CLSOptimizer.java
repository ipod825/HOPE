package hope;

import problem.Problem;

import code.Code;
import code.CodeType;

import localsolver.LSExpression;
import localsolver.LSModel;
import localsolver.LSOperator;


public class CLSOptimizer extends LSOptimizer{

	public CLSOptimizer(OptimizerParams params) {
		super(params);
	}
	
	protected LSProblem loadProblem(Problem problem, LSModel model, int fullDim, int reducedDim){
		LSExpression[] x = null;
		Variables variables = null;
		LSExpression objective = model.createExpression(LSOperator.Sum);
		
		assert reducedDim<=fullDim;
        //unconstrained optimization
        variables = this.createVar(model, fullDim);	
		x=variables.x;
		for(int i=0;i<problem.getNumFn();++i){
			LSExpression funcVal = this.createIfTree(model, x, problem.getFnVarInd()[i], 0, 0, problem.getFnVal()[i]);
			objective.addOperand(model.createExpression(LSOperator.Log,funcVal));
		}
		LSProblem prob = new LSProblem(objective,variables);
		if(reducedDim>0){
            boolean[] ini = addConstraints(model, prob, fullDim, reducedDim);
            // BinaryMatrixHelper.printVector(ini);
            prob.ini = ini;
        }
        return prob;
	}

	private boolean[] addConstraints(LSModel model, LSProblem prob, int fullDim, int reducedDim){
		if(reducedDim==fullDim){
			return null;
		}
		int numConstraint = fullDim-reducedDim;
		LSExpression[] x = prob.vars.x;

		boolean elim;
		boolean[][] matrix=Code.generate(this.params.codeType(), fullDim, numConstraint, true);
		// if(this.params.codeType()==CodeType.DENSE)
		// 	elim=false;	
		// else
		// 	elim=false; //true
		elim = true;

		int rank=-1;
		if(elim){
			rank = BinaryMatrixHelper.gaussJordanElimination(matrix, numConstraint);
			// BinaryMatrixHelper.printMatrix(matrix);
		}
		boolean[] parityVec = new boolean[numConstraint];
		for(int i=0;i<numConstraint;i++){
			LSExpression xor = model.createExpression(LSOperator.Xor);
			for(int j=0;j<x.length;j++){
				if(matrix[i][j])
					xor.addOperand(x[j]);
			}
			int parity;
			if(rank>0 && i>=rank){
				parity=0;
			}else{
				parity = (int) Math.round(Math.random());
			}
			
			parityVec[i]= (parity==1?true:false);
			LSExpression constr = model.createExpression(LSOperator.Eq, xor, parity);
			model.addConstraint(constr);
		}
		return BinaryMatrixHelper.findFullSolution(parityVec, BinaryMatrixHelper.transpose(BinaryMatrixHelper.copyMatrix(matrix,numConstraint)));
	}
}
