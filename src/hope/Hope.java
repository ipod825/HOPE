package hope;
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
            int oriTimeLimit = this.params.timeLimit();
            // assert sampleSize<=oriTimeLimit;  // Make sure the following for loop will run
            for(int i=sampleSize; i<=oriTimeLimit; i++){
                if(oriTimeLimit%i == 0){
                    lsParams.thread(i);
                    lsParams.timeLimit(oriTimeLimit/i);
                    break;
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
        estimates[0] = this.estimateQuantile(problem, 0);

        if(this.earlyStop){
            estimates[fullDim] = estimateQuantile(problem, fullDim);
            // EarlyStopResult result = null;
            int progress=1;
            while(true){
                double[] logHighAreas = this.getLogHighAreas();
                double[] logLowAreas = this.getLogLowAreas();
                double logHighArea = Utils.logSumExp(logHighAreas);
                double logLowArea = Utils.logSumExp(logLowAreas);

                // A/B<3 => log(A)-lob(B)<log(3)
                if(logHighArea-logLowArea<Math.log(2)){ 
                    System.out.print(this.reportEstimates());
                    // (A+B)/2 => log(A+B)-log(2)
                    return Utils.logSumExp(logHighArea, logLowArea)-Math.log(2);
                }

                int d = this.binarySearch(logLowAreas, logHighAreas);
                estimates[d] = this.estimateQuantile(problem, d);
                System.out.printf("%d/%d estimated...\n", progress++, fullDim);
            }
        }
        else{
            // if(this.optimizerType==OptimizerType.CPLEX){
            //     Parallel.For(1, fullDim+1, Parallel.iCPU/sampleSize, new LoopBody<Integer>(){
            //         public void run(Integer c){
            //             estimates[c] = estimateQuantile(problem, c);
            //         }
            //     });
            // }
            // else{
            //     for(int c=1; c<fullDim+1; c++)
            //         estimates[c] = estimateQuantile(problem,c);
            // }
            for(int c=1; c<fullDim+1; c++)
                estimates[c] = estimateQuantile(problem,c);
            double[] logLowAreas = this.getLogLowAreas();
            System.out.print(this.reportEstimates());
            return Utils.logSumExp(logLowAreas);
        }
    }
    
    public double estimateQuantile(final Problem problem, final int numConstraint){
        final int fullDim = problem.getNumVar();
        final int sampleSize = numConstraint==0? 1 : this.sampleSize;

        final double[] samples = new double[sampleSize];
        final Optimizer[] optimizers = new Optimizer[sampleSize];
        for(int i=0;i<sampleSize;i++)
            optimizers[i] = this.getOptimizer(fullDim, fullDim - numConstraint);

        System.out.print("Constraints: "+numConstraint+" ");
        optimizers[0].reportParams();

        if(!(optimizers[0] instanceof CplexOptimizer)){
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
        double[] nsamples = Utils.removeInf(samples);
        return Utils.median(nsamples);
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
        switch (optimizerType) {
            case CPLEX:
                return new CplexOptimizer(this.params);
            case LS:
                return new LSOptimizer(this.lsParams);
            case CLS:
                return new CLSOptimizer(this.lsParams);
            default:
                assert false;
                return null;
        }
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

    double[] getLogHighAreas(){
        double[] logHighRectH= new double[estimates.length];
        logHighRectH[0] = estimates[0];
        for(int i=1;i<estimates.length;i++){
            // If previous quantile is already estimated, use it, otherwise use earlier estimates.
            if(!Double.isNaN(estimates[i-1]))
                logHighRectH[i] = estimates[i-1];
            else
                logHighRectH[i] = logHighRectH[i-1];
        }

        double[] logHighAreas = new double[estimates.length];
        logHighAreas[0] = this.calcLogArea(logHighRectH[0], 0);
        for(int i=1; i<estimates.length; i++)
            logHighAreas[i] = this.calcLogArea(logHighRectH[i], i-1);
        return logHighAreas;
    }

    double[] getLogLowAreas(){
        double[] logLowRectH= new double[estimates.length];
        for(int i=estimates.length-1;i>=0;i--){
            if(!Double.isNaN(estimates[i]))
                logLowRectH[i] = estimates[i];
            else
                logLowRectH[i] = logLowRectH[i+1];
        }
        double[] logLowAreas = new double[estimates.length];
        logLowAreas[0] = this.calcLogArea(logLowRectH[0],0);
        for(int i=estimates.length-1;i>0;i--)
            logLowAreas[i] = this.calcLogArea(logLowRectH[i],i-1);
        return logLowAreas;
    }

    int binarySearch(double[] logLowAreas, double[] logHighAreas){
    
        double[] areaDiff = new double[logLowAreas.length];
        for(int i=0; i<areaDiff.length; i++){
            areaDiff[i] = Math.exp(logHighAreas[i]) - Math.exp(logLowAreas[i]);
        }
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

        int d = (maxDiffIntv.end+maxDiffIntv.start)/2;
        return d;
    }
}
