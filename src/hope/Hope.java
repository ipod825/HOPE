package hope;
import java.util.Arrays;
import java.util.Date;

import problem.Ising;
import problem.Problem;

import utils.LoopBody;
import utils.Parallel;
import utils.Utils;

import code.CodeType;


public class Hope implements Solver{
    //  private static final String testpath = Config.rootDir+"problems/timeout/grid_attractive_n10_w1.0_f0.1.uai";
    private static final String testpath = Config.rootDir+"problems/test/grid_attractive_n3_w1.0_f1.0.uai";

    private double[] estimates;

    private boolean earlyStop;
    protected int sampleSize;
    private OptimizerType optimizerType;
    private OptimizerParams params;
    private OptimizerParams lsParams;


    public Hope(int sampleSize, OptimizerType optimizerType, OptimizerParams params){
        this(sampleSize, optimizerType, params, true, true);
    }

    public Hope(int sampleSize, OptimizerType optimizerType, OptimizerParams params, boolean earlyStop){
        this(sampleSize, optimizerType, params, earlyStop, true);
    }

    public Hope(int sampleSize, OptimizerType optimizerType, OptimizerParams params, boolean earlyStop, boolean parallelLs){
        this.sampleSize = sampleSize;
        this.optimizerType = optimizerType;
        this.params = new OptimizerParams(params);
        this.earlyStop = earlyStop;

        this.lsParams = new OptimizerParams(params);
        if(parallelLs){
            if(optimizerType==OptimizerType.LS || optimizerType==OptimizerType.TWO_THIRD){
                int oriTimeLimit = this.params.timeLimit();
                assert sampleSize<=oriTimeLimit;  // Make sure the following for loop will run
                for(int i=sampleSize; i<=oriTimeLimit; i++){
                    if(oriTimeLimit%i == 0){
                        lsParams.thread(i);
                        lsParams.timeLimit(oriTimeLimit/i);
                        break;
                    }
                }
            }
        }
    }

    public static void main(String [] args) {
        try {
            int timeLimit=10;
            int sampleSize = 7;
            CodeType codeType = CodeType.PEG;
            OptimizerType optimizerType = OptimizerType.CPLEX;
            // OptimizerType optimizerType = OptimizerType.TWO_THIRD;

            OptimizerParams params = new OptimizerParams();
            params.timeLimit(timeLimit).codeType(codeType);
            params.seed(10);

            long start = new Date().getTime();
            Hope hope = new Hope(sampleSize, optimizerType, params);
            double est = hope.solve(new Ising(testpath));
            long end = new Date().getTime();
            System.out.println("time:"+(end-start)/1000);
            System.out.println("est:"+Math.log(est));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double solve(final Problem problem){
        //initial run(unconstrained, full domain)
        int fullDim = problem.getNumVar();

        this.estimates = new double[fullDim+1];
        for(int i=0; i<fullDim+1; i++)
            this.estimates[i] = Double.NaN;


        // No-constraint, only need to optimize once
        estimates[0] = this.estimateQuantile(problem, 0, 1);

        if(this.earlyStop){
            estimates[fullDim] = estimateQuantile(problem, fullDim);
            EarlyStopResult result = null;
            int progress=1;
            while(! (result=earlyStop()).earlyStop()){
                Interval current = result.intv;
                int d = (current.end+current.start)/2;
                estimates[d] = this.estimateQuantile(problem, d);
                System.out.printf("%d/%d estimated...\n", progress++, fullDim);
            }
            System.out.print(this.reportEstimates());
            return result.logEst;
        }
        else{
            Parallel.For(1, fullDim+1, Parallel.iCPU/sampleSize, new LoopBody<Integer>(){
                public void run(Integer c){
                    estimates[c] = estimateQuantile(problem, c);
                }
            });
            double curmax = estimates[0];
            for(int i=1; i<fullDim+1; i++){
                double term = estimates[i]+(i-1)*Math.log(2);
                curmax = term>curmax? term: curmax;
            }
            double res = Math.exp(estimates[0]-curmax);
            for(int i=1; i<fullDim+1; i++){
                res += Math.exp(estimates[i]+(i-1)*Math.log(2)-curmax);
            }
            return Math.log(res)+curmax;
        }
    }
    
    public double estimateQuantile(final Problem problem, final int numConstraint){
        return this.estimateQuantile(problem, numConstraint, this.sampleSize);
    }
    public double estimateQuantile(final Problem problem, final int numConstraint, final int sampleSize){
        final int fullDim = problem.getNumVar();

        final double[] samples = new double[sampleSize];
        final Optimizer[] optimizers = new Optimizer[sampleSize];
        for(int i=0;i<sampleSize;i++)
            optimizers[i] = this.getOptimizer(fullDim, fullDim - numConstraint);

        System.out.print("Constraints: "+numConstraint+" ");
        optimizers[0].reportParams();

        if(optimizers[0] instanceof LSOptimizer){
            // LocalSolver can not be parralized
            for(int i=0;i<sampleSize;i++){
                samples[i] = optimizers[i].estimate(problem, numConstraint);
            }
        }else{
            Parallel.For(0, sampleSize, new LoopBody <Integer>(){
                public void run(Integer i){
                    samples[i] = optimizers[i].estimate(problem, numConstraint);
                }
            });
        }
        double[] nsamples = this.removeInf(samples);
        return Utils.median(nsamples);
    }

    public double[] removeInf(double[] arr){
        int i;
        for(i=0; i<arr.length; i++){
            if(!Double.isInfinite(arr[i]))
                break;
        }
        if(i==0)
            return arr;
        else if (i==arr.length) {
            return new double[]{Double.NEGATIVE_INFINITY};
        }
        return Arrays.copyOfRange(arr, i, arr.length);
    }

    public Optimizer getOptimizer(int fullDim, int reducedDim){
        OptimizerType optimizerType = this.optimizerType;
        if(optimizerType==OptimizerType.TWO_THIRD){
            double r = reducedDim/(double)fullDim;
            if(fullDim==reducedDim || r*3>2 )
                optimizerType = OptimizerType.CPLEX;
            else
                optimizerType = OptimizerType.LS;
        }
        if(optimizerType==OptimizerType.CPLEX)
            return new CplexOptimizer(this.params);
        else
            return new LSOptimizer(this.lsParams);
    }


    public double[] getLogEstimates(){
        return this.estimates;
    }

    public String reportEstimates(){
        StringBuilder sb = new StringBuilder();
        int save=0;
        for(int i=this.estimates.length-1;i>=0;i--){
            if(!Double.isNaN(this.estimates[i]))
                sb.append("e").append(i).append(":").append(this.estimates[i]).append("\n");
            else
                ++save;
        }
        sb.append("Saving:").append((double)save/this.estimates.length).append("\n");
        return sb.toString();
    }


    class EarlyStopResult{
        Interval intv;
        double logEst;

        public EarlyStopResult(Interval intv, double logEst){
            this.intv=intv;
            this.logEst=logEst;
        }

        public boolean earlyStop(){
            return intv==null;
        }
    }

    class Interval{
        int start;
        int end;
        double diff;

        public Interval(double diff){
            this.diff=diff;
        }
        public Interval(){
            this.diff=0;
        }
    }

    private double calcLogArea(double logHeight, double logWidth){
        return logHeight + logWidth * Math.log(2);
    }

    private EarlyStopResult earlyStop(){
        double[] logHighRectH= new double[estimates.length];
        logHighRectH[0] = estimates[0];
        for(int i=1;i<estimates.length;i++){
            // If previous quantile is already estimated, use it, otherwise use earlier estimates.
            if(!Double.isNaN(estimates[i-1]))
                logHighRectH[i] = estimates[i-1];
            else
                logHighRectH[i] = logHighRectH[i-1];
        }

        double[] logLowRectH= new double[estimates.length];
        for(int i=estimates.length-1;i>=0;i--){
            if(!Double.isNaN(estimates[i]))
                logLowRectH[i] = estimates[i];
            else
                logLowRectH[i] = logLowRectH[i+1];
        }

        double[] logHighAreas = new double[estimates.length];
        double[] logLowAreas = new double[estimates.length];

        logHighAreas[0] = this.calcLogArea(logHighRectH[0], 0);
        for(int i=1; i<estimates.length; i++)
            logHighAreas[i] = this.calcLogArea(logHighRectH[i], i-1);
        
        logLowAreas[0] = this.calcLogArea(logLowRectH[0],0);
        for(int i=estimates.length-1;i>0;i--)
            logLowAreas[i] = this.calcLogArea(logLowRectH[i],i-1);

        
        Interval maxDiffIntv = new Interval();
        Interval currentIntv = new Interval(Math.exp(logHighAreas[estimates.length-1])-Math.exp(logLowAreas[estimates.length-1]));
        currentIntv.end = estimates.length - 1;
        for(int i=estimates.length-2;i>=0;i--){
            if(!Double.isNaN(estimates[i])){
                currentIntv.start=i+1;
                if(currentIntv.diff>maxDiffIntv.diff && currentIntv.end>currentIntv.start){
                    maxDiffIntv=currentIntv;
                }
                currentIntv = new Interval();
                currentIntv.end = i;
            }
            currentIntv.diff += Math.exp(logHighAreas[i])-Math.exp(logLowAreas[i]);
        }

        // Interval maxDiffIntv = new Interval();
        // Interval currentIntv = new Interval();
        // currentIntv.start = 1;
        // for(int i=1;i<=estimates.length-1;i++){
        //     if(!Double.isNaN(estimates[i])){
        //         currentIntv.end = i;
        //         if(currentIntv.diff>maxDiffIntv.diff && currentIntv.end>currentIntv.start){
        //             maxDiffIntv=currentIntv;
        //         }
        //         currentIntv = new Interval();
        //         currentIntv.start = i+1;
        //     }
        //     currentIntv.diff += Math.exp(logHighAreas[i])-Math.exp(logLowAreas[i]);
        // }

        double logHighArea = logSumExp(logHighAreas);
        double logLowArea = logSumExp(logLowAreas);
        // A/B<3 => log(A)-lob(B)<log(3)
        if(logHighArea-logLowArea<Math.log(3)){ 
            // (A+B)/2 => log(A+B)-log(2)
            return new EarlyStopResult(null,logSumExp(logHighArea, logLowArea)-Math.log(2));
        }
        return new EarlyStopResult(maxDiffIntv,0);
    }

    public double logSumExp(double ... arr){
        double curmax = Double.NEGATIVE_INFINITY;
        for(double a: arr)
            curmax = a>curmax? a: curmax;
        
        double res = 0;
        for(double a: arr){
            res += Math.exp(a-curmax);
        }
        return Math.log(res)+curmax;
    }
}
