import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

public class CplexOptimizer extends Optimizer{
	private int mNumVars=0;
	private double mOptValue=0;
	public CplexOptimizer(OptimizerParams params) {
		super(params);
	}
	
	public CplexOptimizer(OptimizerParams params, int reducedDim) {
		super(params, reducedDim);
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
	public void estimate(String path, int numVars) {
		int m;
		boolean elim;
		boolean[][] matrix=null;
		if(numVars <0 ){
			//full dimension
			matrix=null;
			m=0;
			elim=true;
		}else if(this.params.codeType()==CodeType.DENSE){
			matrix=null;
			m=numVars-this.mReducedDim;
			elim=true;
		}
		else if(this.params.codeType()==CodeType.SPARSE){
			m=numVars-this.mReducedDim;
			double d = Math.min(0.5, 1-Math.pow((double)m/100, 0.28)+0.05);
			matrix=BinaryMatrixHelper.getRandomMatrix(m, numVars,  d);
			elim=false;
		}
		else if(this.params.codeType()==CodeType.PEG_REGULAR){
			m=numVars-this.mReducedDim;
			matrix = LDPCTools.getRPEGMatrix(numVars, m, false);
			if(matrix==null){
				elim=true;	
			}else{
				elim=false;
			}
			BinaryMatrixHelper.printMatrix(matrix);
		}else{
			m=numVars-this.mReducedDim;
			matrix = LDPCTools.getPEGMatrix(numVars, m, false);
			if(matrix==null){
				elim=true;	
			}else{
				elim=false;
			}
		}
		CplexOutput cpo = new CplexOutput();
		callCplex(path, this.params.timeLimit(), m, matrix, cpo, elim);
		this.mOptValue = cpo.getOptimalValue()*Math.log(10);
		this.mNumVars = cpo.getNumVars();
	}
	@Override
	public double getOptimalValue() {
		return this.mOptValue;
	}
	@Override
	public int getOriginalDim() {
		return this.mNumVars;
	}

	@Override
	public String getSolver() {
		return "CPLEX";
	}

	@Override
	public double getRuntime() {
		return 0;
	}

}
