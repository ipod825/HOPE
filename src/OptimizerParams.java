
public class OptimizerParams {
	private int _timeLimit;
	private CodeType _codeType;
	private int _thread=-1;
	
	public OptimizerParams(){}
	public OptimizerParams(OptimizerParams that){
		this._timeLimit = that._timeLimit;
		this._codeType = that._codeType;
		this._thread = that._thread;
	}
	public void describe(){
		System.out.print(" timeLimit: "+this._timeLimit);
		if(this._thread>0)
			System.out.print(" numThread: "+this._thread);
		System.out.print(" code: "+this._codeType);
		System.out.println();
	}
	
	public int timeLimit(){return this._timeLimit;}
	public OptimizerParams timeLimit(int t){this._timeLimit=t; return this;}
	
	public CodeType codeType(){return this._codeType;}
	public OptimizerParams codeType(CodeType c){this._codeType=c;return this;}
	
	public int thread(){return this._thread;}
	public OptimizerParams thread(int t){this._thread=t; return this;}
}
