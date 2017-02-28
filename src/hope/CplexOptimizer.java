package hope;

import problem.Problem;

import code.Code;
import utils.Utils;


public class CplexOptimizer extends Optimizer{

	public CplexOptimizer(OptimizerParams params) {
		super(params);
	}

	private String convertMatrixToString(boolean[][] matrix){
		if(matrix==null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<matrix.length;i++){
			for(int j=0;j<matrix[0].length;j++){
				sb.append(matrix[i][j]?1:0);
			}
			sb.append("_");
		}
		return sb.toString();
	} 
	
	public double callCplex(String path, int timeLimit, int numConstraint, boolean[][] matrix,  boolean elim){
		String matrixStr = convertMatrixToString(matrix);
		String cmd = Config.pathToWishCplex;
		cmd += "  -paritylevel 1";
		cmd += " -timelimit " + timeLimit;
		cmd += " -number " + numConstraint;
		if(this.params.seed()!=Integer.MIN_VALUE)
            cmd += " -seed " + this.params.seed();
		if(!elim)
			cmd += " -skipelim";
		if(matrixStr!=null)
			cmd += " -matrix " + matrixStr;
		cmd += " " + path;
		CmdExecutor handler = new CmdExecutor("Solution value log10lik = ");

        String outputPath = null;
		if(this.params.logPath()!=null){
            outputPath = this.params.logPath() + Utils.basename(path) + String.format(".%d.%d%d", numConstraint, Utils.getThreadID(), Utils.getDate()) ;
		}

		return handler.runCmd(cmd, outputPath);
	}


	@Override
	public double estimate(Problem problem, int numConstraint) {
		int numVars = problem.getNumVar();
		
		boolean elim;
		boolean[][] matrix=Code.generate(this.params.codeType(), numVars, numConstraint);
		if(matrix==null)
			elim=true;	
		else
			elim=false;
		
		double res = callCplex(problem.getPath(), this.params.timeLimit(), numConstraint, matrix, elim);
		return res*Math.log(10);
	}
}
