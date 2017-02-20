import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import localsolver.LSExpression;
import localsolver.LSModel;
import localsolver.LSObjectiveDirection;
import localsolver.LSOperator;
import localsolver.LSPhase;
import localsolver.LSSolution;
import localsolver.LocalSolver;


public class LSOptimizer extends Optimizer{
	private boolean[] mSol=null;
	private double mOptValue=0;
	private int mOriginalDim = -1;
	private double runtime=0;
	private int tick=5;
	
	public LSOptimizer(InstanceParams params){
		super(params);
	}
	
	public LSOptimizer(InstanceParams params, int reducedDim){
		super(params, reducedDim);
	}
	
	class Problem{
		boolean[] ini;
		Variables vars;
		LSExpression solverObjective;
		LSExpression originalObjective;
		public Problem(LSExpression objective, Variables vars){
			this.vars=vars;
			this.solverObjective=objective;
			this.originalObjective=objective;
		}
		
		public Problem(LSExpression solverObjective, LSExpression originalObjective, Variables vars){
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

	
	
	private boolean[] addConstraints(LSModel model, Problem prob){
		if(this.mReducedDim==FULL_DOMAIN || this.mReducedDim == this.mOriginalDim){
			return null;
		}
		int numConstr = this.mOriginalDim-this.mReducedDim;
		LSExpression[] x = prob.vars.x;
		boolean[][] matrix=null;

		boolean elim = true;
		if(this.mParams.isPEG()){
			System.out.println("get peg");
			matrix = LDPCTools.getPEGMatrix(this.mOriginalDim,numConstr,false);
			elim = true;//false;
		}else if(this.mParams.isRegularPEG()){
			System.out.println("get reg peg");
			matrix = LDPCTools.getRPEGMatrix(this.mOriginalDim,numConstr,false);
			elim = true;//false;
		}
		
		if(matrix==null){
			elim=true;
		}
		int rank=-1;
		if(elim){
			System.out.println("elim");
			rank = BinaryMatrixHelper.gaussJordanElimination(matrix, numConstr);
			BinaryMatrixHelper.printMatrix(matrix);
		}
		boolean[] parityVec = new boolean[numConstr];
		for(int i=0;i<numConstr;i++){
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
		return BinaryMatrixHelper.findFullSolution(parityVec, BinaryMatrixHelper.transpose(BinaryMatrixHelper.copyMatrix(matrix,numConstr)));
	}
	
	
	public void estimate(String path, int numVars){
		double max=0;
		boolean isMaxAssigned=false;
		LocalSolver localsolver = new LocalSolver();
		LSModel model = localsolver.getModel();
		Problem prob=null;
		try {
			prob = this.getProblem(path, model);
			model.addObjective(prob.solverObjective, LSObjectiveDirection.Maximize);
			model.close();
			setSolverParams(localsolver);
			LSPhase phase = localsolver.createPhase();
			phase.setTimeLimit(this.mParams.getTimeLimit());
			
			long start = new Date().getTime();
			localsolver.solve();
			long end = new Date().getTime();
			this.runtime = (end-start)/(double)1000;
			LSSolution sol = localsolver.getSolution();
			if(!isMaxAssigned || sol.getDoubleValue(prob.originalObjective)>max){
				this.mSol = this.toBooleanSolution(sol, prob);
				this.mOptValue = sol.getDoubleValue(prob.originalObjective);
				isMaxAssigned=true;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		localsolver.delete();
	}
	private Problem getProblem(String path, LSModel model) throws IOException{
		Problem prob=null;
			if(this.mParams.isUnconstrained()){
				System.out.println("unconstrained");
				prob = loadProblemFromFile(path, model, this.mReducedDim);	
			}
			else{
				//parity constraints
				prob = loadProblemFromFile(path, model, FULL_DOMAIN);
				boolean[] ini = addConstraints(model, prob);
				BinaryMatrixHelper.printVector(ini);
				prob.ini = ini;
			}
		return prob;
	}
	
	private void setSolverParams(LocalSolver localsolver){
		Random rand = new Random();
		localsolver.getParam().setSeed(rand.nextInt(1000000));
		localsolver.getParam().setAnnealingLevel(9);
		localsolver.getParam().setTimeBetweenDisplays(this.tick);
		localsolver.getParam().setVerbosity(0);
	}
	
	
	public int getOriginalDim(){
		return this.mOriginalDim;
	}
	
	
	private boolean[] toBooleanSolution(LSSolution sol, Problem prob){
		if(sol==null || prob==null){
			return null;
		}
		LSExpression[] x = prob.getX();	
		boolean[] bSol=new boolean[x.length];
		for(int i=0;i<x.length;i++){
			bSol[i] = sol.getIntValue(x[i])==1;
		}
		return bSol;
	}
	
	public boolean[] getBooleanSolution(){
		return this.mSol;
	}
	
	public double getOptimalValue(){
		System.out.println("opt:"+this.mOptValue);
		return this.mOptValue;
	}


	private Problem parseUAI(LSModel model, int reducedDim, BufferedReader br) throws NumberFormatException, IOException{
		String line;
		int lineNo = 1;
		int numFunc=0;
		int numFuncVals=0;
		int funcValCounter=0;
		double[] funcVals=null;
		int[] funcVars=null;
		int funcCounter = 0;
		boolean readFuncTable = false;
		LSExpression[] x = null;
		Variables variables = null;
		LSExpression objective = model.createExpression(LSOperator.Sum);
		ArrayList<int []> funcs = new ArrayList<int []>();
		while ((line = br.readLine()) != null) {
			if(line.length() == 0){
				continue;
			}else if(lineNo==2){
				this.mOriginalDim = Integer.parseInt(line);
				if(reducedDim == FULL_DOMAIN || reducedDim>this.mOriginalDim){
					//unconstrained optimization
					variables = createVar(model);	
				}else{
					//optimize in the reduced domain
					variables = createTransformedVar(model);
				}
				x=variables.x;
			}else if(lineNo==4){
				numFunc = Integer.parseInt(line);
			}else if(lineNo>=5 && lineNo<5+numFunc){
				String[] parts = line.split("\\s+");
				int numFuncVar = Integer.parseInt(parts[0]);

				if(numFuncVar != parts.length-1){
					System.err.println("invliad function line:"+line);
					lineNo++;
					continue;
				}

				int[] curFuncVars = new int[numFuncVar];
				for(int i=1;i<parts.length;i++){
					//we put var0,var1,...,varn as [varn,...,var1,var0]
					curFuncVars[parts.length-1-i]=Integer.parseInt(parts[i]);
				}
				funcs.add(curFuncVars);
			}else if(lineNo>=5+numFunc){
				if(!readFuncTable){
					//read the first line of a function table which specifies the number of values
					numFuncVals = Integer.parseInt(line.trim());
					funcVars = funcs.get(funcCounter);
					if(funcCounter>=funcs.size() ||  Math.pow(2,funcVars.length)!=numFuncVals)
					{
						System.err.println("invalid function:"+funcCounter);
						System.err.println("line:"+line);
						return null;
					}
					funcVals = new double[numFuncVals];
					readFuncTable=true;
					funcValCounter=0;
				}else{
					//read function table
					String[] parts = line.split("\\s+");
					for(int p=0;p<parts.length;p++){
						funcVals[funcValCounter]=Double.parseDouble(parts[p]);
						funcValCounter++;
						if(funcValCounter>=numFuncVals)
						{
							LSExpression funcVal = this.createIfTree(model, x, funcVars, 0, 0, funcVals);
							objective.addOperand(model.createExpression(LSOperator.Log,funcVal));
							readFuncTable=false;
							funcCounter++;
						}						
					}
				}
			}
			lineNo++;
		}
		return new Problem(objective,variables);
	}
	private Problem loadProblemFromFile(String path, LSModel model, int reducedDim) throws IOException{
		Problem problem=null;
		BufferedReader br=null;
		FileReader fr = new FileReader(new File(path));
		try{
			br = new BufferedReader(fr);
			problem=parseUAI(model, reducedDim, br);
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			br.close();
		}	
		return problem;
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
	
	private Variables createTransformedVar(LSModel model){
		//x: the variables in 2^n
		//y: the variables in 2^d, d<n
		LSExpression[] y = new LSExpression[this.mReducedDim];
		for (int i = 0; i < y.length; i++){ 
			y[i] = model.createExpression(LSOperator.Bool);
		}

		boolean offset[] = BinaryMatrixHelper.getRandomVector(this.mOriginalDim);
				
		if(this.mOriginalDim == this.mReducedDim){
			return new Variables(y,y);
		}
		
		LSExpression[] x = new LSExpression[this.mOriginalDim];
		
		boolean matrix[][]=null;;
		if(this.mParams.isPEG()){
			boolean[][] parity = LDPCTools.getPEGMatrix(this.mOriginalDim, this.mOriginalDim-this.mReducedDim, false);
			if(parity != null){
				matrix = BinaryMatrixHelper.parityToGenerator(parity);	
			}
		}
		if(matrix == null){
			matrix = BinaryMatrixHelper.getFullRankMatrix(this.mOriginalDim);
			BinaryMatrixHelper.gaussJordanElimination(matrix, this.mReducedDim);
		}
		
		for(int i=0;i<x.length;i++){
			x[i] = model.createExpression(LSOperator.Xor);

			if(offset[i]){
				x[i].addOperand(1);
			}

			for(int j=0;j<this.mReducedDim;j++){
				if(matrix[j][i]){
					x[i].addOperand(y[j]);
				}
			}
		}

		return new Variables(x,y);
	}

	private Variables createVar(LSModel model){
		LSExpression[] x = new LSExpression[this.mOriginalDim];
		for (int i = 0; i < x.length; i++){ 
			x[i] = model.createExpression(LSOperator.Bool);
		}

		return new Variables(x,x);
	}

	@Override
	public String getSolver() {
		// TODO Auto-generated method stub
		return "LocalSolver";
	}

	@Override
	public double getRuntime() {
		// TODO Auto-generated method stub
		return this.runtime;
	}
}