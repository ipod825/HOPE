package hope;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

import problem.Problem;

import code.Code;


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
	
	public void callCplex(String path, int timeout, int m, boolean[][] matrix, CplexOutput cpo, boolean elim){
		String matrixStr = convertMatrixToString(matrix);
		
		CommandLine cl = new CommandLine(Config.pathToWishCplex);
		cl.addArgument("-paritylevel");
		cl.addArgument("1");
		cl.addArgument("-timelimit");
		cl.addArgument(""+timeout);
		cl.addArgument("-number");
		cl.addArgument(""+m);
		if(!elim){
			cl.addArgument("-skipelim");
		}
		if(matrixStr!=null){
			cl.addArgument("-matrix");
			cl.addArgument(matrixStr);	
		}
		cl.addArgument(path);
		
		Executor exec = new DefaultExecutor();
		
		try {
			exec.setStreamHandler(new PumpStreamHandler(cpo));
			exec.setExitValues(null);
			exec.execute(cl);
			cpo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class CplexOutput extends LogOutputStream{
		private static final String LOG_PREFIX = "Solution value log10lik = ";
		private static final String VAR_PREFIX = "number of variables = ";
		
		private double optimalValue = 0;
		private int numVars = 0;

		@Override
		protected void processLine(String line, int level) {
			if(line==null){
				return;
			}
			if(line.startsWith(LOG_PREFIX)){
				optimalValue = Double.parseDouble(line.substring(LOG_PREFIX.length()));
			}else if(line.startsWith(VAR_PREFIX)){
				numVars = Integer.parseInt(line.substring(VAR_PREFIX.length()));
			}
		}
				
		public double getOptimalValue(){
			return this.optimalValue;
		}
		
		public int getNumVars(){
			return this.numVars;
		}
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
		
		CplexOutput cpo = new CplexOutput();
		callCplex(problem.getPath(), this.params.timeLimit(), numConstraint, matrix, cpo, elim);
		return cpo.getOptimalValue()*Math.log(10);
	}
}
