package hope;

import code.CodeType;

public class OptimizerParams {
    private int _timeLimit;
    private CodeType _codeType;
    private int _thread=-1;
    private int _seed = Integer.MIN_VALUE;
    private String _logPath = null;
    
    public OptimizerParams(){}
    public OptimizerParams(OptimizerParams that){
        this._timeLimit = that._timeLimit;
        this._codeType = that._codeType;
        this._thread = that._thread;
        this._seed = that._seed;
        this._logPath = that._logPath;
    }
    public void describe(){
        System.out.print(" timeLimit: "+this._timeLimit);
        if(this._thread>0)
            System.out.print(" numThread: "+this._thread);
        System.out.print(" code: "+this._codeType);
        System.out.println();
    }
    
    public int timeLimit(){return this._timeLimit;}
    public OptimizerParams timeLimit(int i){this._timeLimit=i; return this;}
    
    public CodeType codeType(){return this._codeType;}
    public OptimizerParams codeType(CodeType c){this._codeType=c;return this;}
    
    public int thread(){return this._thread;}
    public OptimizerParams thread(int i){this._thread=i; return this;}

    public int seed(){return this._seed;}
    public OptimizerParams seed(int i){this._seed=i; return this;}

    public String logPath(){return this._logPath;}
    public OptimizerParams logPath(String s){this._logPath=s; return this;}
}
