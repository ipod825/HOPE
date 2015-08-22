
public class InstanceGenerator {

	
//	private int type = CPLEX;
		
//	public InstanceGenerator(RunParams params){
//		this.type = params.getSolverType();
//	}
//	
//	public Instance getInstance(String path, InstanceParams params, int reducedDim){
//		return this.type==CPLEX? new CplexInstance(path,params,reducedDim):new LSInstance(path,params,reducedDim);
//	}
//	
//	public Instance getInstance(String path, InstanceParams params){
//		if(this.type==ADAPTIVE){
//			return new CplexInstance(path,params);
//		}
//			
//		return this.type==CPLEX? new CplexInstance(path,params):new LSInstance(path,params);
//	}
//	
//	public Instance getInstance(String path, InstanceParams params, int reducedDim, int fullDim){
//		if(this.type==ADAPTIVE){
//			if(reducedDim<0)
//				return new CplexInstance(path,params,reducedDim);
////			System.out.println(""+reducedDim+";"+fullDim);
////			System.out.println(reducedDim<=fullDim/2);
//			return reducedDim>fullDim*3/5? new CplexInstance(path,params,reducedDim):new LSInstance(path,params,reducedDim);
////			return Math.random()>=reducedDim/(double)fullDim? new LSInstance(path,params,reducedDim):new CplexInstance(path,params,reducedDim);
//		}else{
//			return this.type==CPLEX? new CplexInstance(path,params,reducedDim):new LSInstance(path,params,reducedDim);
//		}
//		
//	}
	
	public Instance getInstance(String path, InstanceParams params, int reducedDim, boolean cplex){
		return cplex? new CplexInstance(path,params,reducedDim):new LSInstance(path,params,reducedDim);
	}
}
