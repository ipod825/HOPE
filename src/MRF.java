import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

public class MRF {
	private SumOfProd[] f; 
	private int len=0;
	private int numVar=0;
	private Random rand = new Random();
	public double getValue(boolean[] x){
		double val=1;
		for(int i=0;i<f.length;i++){
			val*=f[i].getValue(x);
		}
		return val;			
	}
	public static void main(String [] args) {
	}
	public MRF(String path) throws IOException{
		InputStream    fis;
		BufferedReader br;
		String         line;

		fis = new FileInputStream(path);
		br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		
		int lineNo = 1;
		int numFunc=0;
		int numFuncVals=0;
		int numFuncVars=0;
		int funcValCounter=0;
		double[] funcVals=null;
		int[] funcVars=null;
		int funcCounter = 0;
		boolean readFuncTable = false;
		SumOfProd sumOfProd = null;
		
		ArrayList<int []> funcs = new ArrayList<int []>();
		while ((line = br.readLine()) != null) {
			if(line.length() == 0){
				continue;
			}else if(lineNo==2){
				numVar = Integer.parseInt(line);
			}else if(lineNo==4){
				numFunc = Integer.parseInt(line);
				f = new SumOfProd[numFunc];
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
					numFuncVals = Integer.parseInt(line);
					funcVars = funcs.get(funcCounter);
					if(funcCounter>=funcs.size() ||  Math.pow(2,(numFuncVars = funcVars.length))!=numFuncVals)
					{
						System.err.println("invalid function:"+funcCounter);
						System.err.println("line:"+line);
						br.close();
						return;
					}
					funcVals = new double[numFuncVals];
					sumOfProd = new SumOfProd(numFuncVals);
					readFuncTable=true;
					funcValCounter=0;
				}else{
					//read function table
					String[] parts = line.split("\\s+");
					for(int p=0;p<parts.length;p++){
						double funcVal = Double.parseDouble(parts[p]);
						boolean[] bits = BinaryMatrixHelper.toBits(funcValCounter, numFuncVars);
						funcVals[funcValCounter]=funcVal;
						Monomial monomial = new Monomial(numFuncVars);					
						for(int i=0;i<bits.length;i++){
							if(bits[i])
							{
								monomial.add(new Literal(funcVars[i],false));
							}else{
								monomial.add(new Literal(funcVars[i],true));
							}
						}
						monomial.w=funcVal;
						sumOfProd.add(monomial);
						funcValCounter++;
						if(funcValCounter>=numFuncVals)
						{
							f[len++]=sumOfProd;
							readFuncTable=false;
							funcCounter++;
						}						
					}
				}
			}
			lineNo++;
		}
		br.close();
	} 
	class Literal{
		public int var;
		public boolean neg;
		public Literal(int var, boolean neg){
			this.var = var;
			this.neg = neg;
		}
		
		public int getValue(boolean[] x){
			return neg?(x[var]?0:1) : (x[var]?1:0);
		}
	} 
	
	class Monomial{
		public Literal[] terms;
		public double w;
		private int len=0;
		public Monomial(int size){
			terms = new Literal[size];
		}
		
		public void add(Literal l){
			terms[len++]=l;
		}
		
		public double getValue(boolean[] x){
			double val=w;
			for(int i=0;i<terms.length;i++){
				val*=terms[i].getValue(x);
			}
			return val;
		}
	}
	
	class SumOfProd{
		public SumOfProd(int size){
			terms = new Monomial[size];
		}
		public void add(Monomial m){
			this.terms[len++]=m;
		}
		
		private int len=0;
		public Monomial[] terms;
		public double getValue(boolean[] x){
			double val=0;
			for(int i=0;i<terms.length;i++){
				val+=terms[i].getValue(x);
			}
			return val;
		}
	}
		
	public double bruteMax() throws IOException{
		long total = (long) Math.pow(2, this.numVar);
		double max = 0;
		for(long i=0;i<total;i++){
			boolean[] x = BinaryMatrixHelper.toBits(i,this.numVar);
			double val = this.getValue(x);
			if(val>max){
				max=val;
			}
		}
		return max;
	}
	public double bruteSum() throws IOException{
		long total = (long) Math.pow(2, this.numVar);
		double sum = 0;
		for(long i=0;i<total;i++){
			boolean[] x = BinaryMatrixHelper.toBits(i,this.numVar);
			double val = this.getValue(x);
			sum+=val;
		}
		return sum;
	}
	
	private boolean[] randomSolution(int numVar){
		boolean[] sol = new boolean[numVar];
		for(int i=0;i<numVar;i++){
			sol[i] = rand.nextBoolean();
		}
		return sol;
	}
	
	private boolean[] randomNeighbor(boolean[] sol){
		boolean[] newSol = sol.clone();
		int idx = rand.nextInt(sol.length);
		newSol[idx] = newSol[idx]^true;
		return newSol;
	}

	private boolean shouldAccept(double temperature, double deltaE) {
		return (deltaE > 0.0)
				|| (new Random().nextDouble() <= probabilityOfAcceptance(
						temperature, deltaE));
	}
	
	public double probabilityOfAcceptance(double temperature, double deltaE) {
		return Math.exp(deltaE / temperature);
	}
	
	public void ascend(int limit) throws IOException{
		boolean[] sol = this.randomSolution(this.numVar);
		Scheduler sch = new Scheduler(limit);
		double max = this.getValue(sol);
		double curValue = max;
		boolean[] newSol;
		for(int i=0;i<limit;i++){
			newSol = this.randomNeighbor(sol);
			double val = this.getValue(newSol);
			if(val>max){
				max=val;
				System.out.println(val);
			}
			double temperature = sch.getTemp(i);
			if (shouldAccept(temperature, val-curValue)) {
				sol = newSol;
				curValue=val;
			}
		}
	}
	
	class Scheduler {

		private final int k;
		private final long limit;

		private final double lam;

		public Scheduler(int k, double lam, int limit) {
			this.k = k;
			this.lam = lam;
			this.limit = limit;
		}

		public Scheduler(long limit) {
			this.k = 20;
			this.lam = 0.045;
			this.limit = 100;
		}

		public double getTemp(int t) {
			if (t < limit) {
				double res = k * Math.exp((-1) * lam * t);
				return res;
			} else {
				return 0.0;
			}
		}
	}
}
