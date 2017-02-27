package hope;
import code.CodeType;
import problem.Problem;


public class Wish implements Solver{

    private OptimizerParams params;
    private Hope hope;

    public Wish(int sampleSize, OptimizerParams params){
        this.params = new OptimizerParams(params);
        this.params.codeType(CodeType.DENSE);
        this.hope = new Hope(sampleSize, OptimizerType.CPLEX, this.params, false);
    }

    public double solve(final Problem problem){
        return this.hope.solve(problem);
    }
}
