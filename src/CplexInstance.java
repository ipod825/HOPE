import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

public class CplexInstance extends Instance{
	private int mNumVars=0;
	private double mOptValue=0;
	public CplexInstance(String path, InstanceParams params) {
		super(path, params);
		// TODO Auto-generated constructor stub
	}
	
	public CplexInstance(String path, InstanceParams params, int reducedDim){
		super(path, params, reducedDim);
	}
	
	public CplexInstance(String path, InstanceParams params, int reducedDim, double stopValue){
		super(path, params, reducedDim, stopValue);
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
		
		CommandLine cl = new CommandLine(Config.pathToCplex);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class CplexOutput extends LogOutputStream{
		private static final String LOG_PREFIX = "Solution value log10lik = ";
		private static final String VAR_PREFIX = "number of variables = ";
		PrintWriter out;
		
		private double optimalValue = 0;
		private int numVars = 0;
		public CplexOutput() throws IOException{
			long d = new Date().getTime();
			out = new PrintWriter(Config.output+mParams.isDense() +mReducedDim+d);
		}
		
		@Override
		protected void processLine(String line, int level) {
			// TODO Auto-generated method stub
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
		@Override
		public void close(){
			out.close();
		}
	}

	@Override
	public void solve(int numVars) {
		// TODO Auto-generated method stub
		int m;
		boolean elim;
		boolean[][] matrix=null;
		if(numVars <0 ){
			//full dimension
			matrix=null;
			m=0;
			elim=true;
		}else if(this.mParams.isDense()){
			matrix=null;
			m=numVars-this.mReducedDim;
			elim=true;
		}
		else if(this.mParams.isRegularlySparse()){
			m=numVars-this.mReducedDim;
			matrix = LDPCTools.getRegularSparseMatrix(numVars, m);//;LDPCTools.getPEGMatrix(numVars, m, false);
			if(matrix==null){
				elim=false;	
			}else{
				elim=true;
			}
		}else if(this.mParams.isRegularPEG()){
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
		try {
			CplexOutput cpo = new CplexOutput();
			callCplex(this.mPath, this.mParams.getTimeLimit(), m, matrix, cpo, elim);
			this.mOptValue = cpo.getOptimalValue()*Math.log(10);
			this.mNumVars = cpo.getNumVars();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public double getOptimalValue() {
		// TODO Auto-generated method stub
		return this.mOptValue;
	}
	@Override
	public int getOriginalDim() {
		// TODO Auto-generated method stub
		return this.mNumVars;
	}

	@Override
	public String getSolver() {
		// TODO Auto-generated method stub
		return "CPLEX";
	}

	@Override
	public double getRuntime() {
		// TODO Auto-generated method stub
		return 0;
	}

}
