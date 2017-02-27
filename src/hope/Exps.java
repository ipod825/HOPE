package hope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import problem.Ising;
import problem.Problem;
import code.CodeType;
import code.PEG;


public class Exps{

    public enum SolverType{
        WISH, HOPE
    }

    public static void main(String args[]) throws IOException{
        Exps exp = new Exps();
        exp.timeoutExp(30, OptimizerType.TWO_THIRD, SolverType.WISH);
        // exp.timeoutExp(10, OptimizerType.TWO_THIRD, SolverType.HOPE);
        //      exp.constraintExp();
        //      exp.constraintTimeoutExp();
    }

    public void hammingWeightExp(){
        PEG peg = new PEG();
        peg.generate(1,2);

        HashMap<Integer, Integer> count = new HashMap<Integer, Integer>(40);
        for(int i=1;i<=40;++i)
            count.put(i, 0);

        for(long i=1;i<1L<<40;++i){
            int c = Long.bitCount(i);
            count.put(c, count.get(c)+1);
        }
    }

    public void constraintExp() throws FileNotFoundException{
        final Problem problem = new Ising(Config.rootDir+"problems/timeout/grid_attractive_n10_w1.0_f0.1.uai");

        final int timeLimit = 30;
        final int sampleSize = 10;

        Hope hope = null;
        OptimizerParams params = new OptimizerParams();
        params.timeLimit(timeLimit);

        params.codeType(CodeType.SPARSE);
        hope = new Hope(sampleSize, OptimizerType.CPLEX,  params, false);
        hope.solve(problem);
        double[] sparseEstimates = hope.getLogEstimates();

        params.codeType(CodeType.DENSE);
        hope = new Hope(sampleSize, OptimizerType.CPLEX, params, false);
        hope.solve(problem);
        double[] denseEstimates = hope.getLogEstimates();

        params.codeType(CodeType.PEG);
        hope = new Hope(sampleSize, OptimizerType.CPLEX, params, false);
        hope.solve(problem);
        double[] pegEstimates = hope.getLogEstimates();

        params.codeType(CodeType.PEG);
        hope = new Hope(sampleSize, OptimizerType.LS, params, false);
        hope.solve(problem);
        double[] affineEstimates = hope.getLogEstimates();

        PrintWriter out = new PrintWriter(Config.outputDir+"constraint.csv");
        out.write("Constraints,Affine Map,PEG LDPC,Dense Parity,Sparse Parity\n");
        for(int i=0; i<denseEstimates.length;++i){
            out.write(i+","+affineEstimates[i]+","+pegEstimates[i]+","+denseEstimates[i]+","+sparseEstimates[i]+"\n");
        }
        out.close();
    }

    public void constraintTimeoutExp() throws FileNotFoundException{
        final Problem problem = new Ising(Config.rootDir+"problems/timeout/grid_attractive_n10_w1.0_f0.1.uai");

        final int[] timeLimits = {30, 120, 240, 360, 480, 600};
        //      final int[] timeLimits = {30, 120};
        final int[] numConstraints = {50, 20};
        final int sampleSize = 10;
        final int numVars = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<Double>> futures  = new LinkedList<Future<Double>>();
        for(int numConstraint: numConstraints){
            final int reducedDim = numVars - numConstraint;
            for(int timeLimit: timeLimits){
                final int t = timeLimit;
                Future<Double> future1 = executorService.submit(new Callable<Double>(){
                    public Double call()  {
                        OptimizerParams params = new OptimizerParams().timeLimit(t).codeType(CodeType.SPARSE);
                        Hope hope = new Hope(sampleSize, OptimizerType.CPLEX,  params, false);
                        return hope.estimateQuantile(problem, reducedDim).getLogEstimate();
                    }
                });
                Future<Double> future2 = executorService.submit(new Callable<Double>(){
                    public Double call()  {
                        OptimizerParams params = new OptimizerParams().timeLimit(t).codeType(CodeType.DENSE);
                        Hope hope = new Hope(sampleSize, OptimizerType.CPLEX,  params, false);
                        return hope.estimateQuantile(problem, reducedDim).getLogEstimate();
                    }
                });
                futures.add(future1);
                futures.add(future2);
                if(timeLimit==30){
                    Future<Double> future3 = executorService.submit(new Callable<Double>(){
                        public Double call()  {
                            OptimizerParams params = new OptimizerParams().timeLimit(t).codeType(CodeType.PEG);
                            Hope hope = new Hope(sampleSize, OptimizerType.LS,  params, false);
                            return hope.estimateQuantile(problem, reducedDim).getLogEstimate();
                        }
                    });
                    futures.add(future3);
                }

            }
        }

        int ind = 0;
        double[][] sparseEstimates= new double[numConstraints.length][timeLimits.length];
        double[][] denseEstimates= new double[numConstraints.length][timeLimits.length];
        double[] affineEstimates= new double[numConstraints.length];
        for(int c=0; c<numConstraints.length;++c){
            for(int t=0; t<timeLimits.length; ++t){
                try {
                    sparseEstimates[c][t] = futures.get(ind++).get();
                    denseEstimates[c][t] = futures.get(ind++).get();
                    if(timeLimits[t]==30)
                        affineEstimates[c] = futures.get(ind++).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        executorService.shutdown();

        PrintWriter out = new PrintWriter(Config.outputDir+"constraintTimeout.csv");
        out.write(String.format("Timeout,Affine Map 30 sec (i=%1$d), Sparse Parity (i=%1$d), Dense (i=%1$d), " +
                    "Affine Map 30 sec (i=%2$d), Sparse Parity (i=%2$d), Dense (i=%2$d)\n",
                    numConstraints[0], numConstraints[1]));
        for(int t=0; t<timeLimits.length;++t){
            out.write(timeLimits[t]+","+affineEstimates[0]+","+sparseEstimates[0][t]+","+denseEstimates[0][t]+","
                    +affineEstimates[1]+sparseEstimates[1][t]+","+denseEstimates[1][t]+"\n");
        }
        out.close();
    }



    public void timeoutExp(int timeLimit, OptimizerType optimizerType, SolverType alg) throws IOException{
        String dataDir = Config.rootDir+"problems/timeout/";
        String outputPath = Config.outputDir+"timeout/";


        int sampleSize = 7;
        CodeType codeType= CodeType.PEG;

        String res = null;
        OptimizerParams params = new OptimizerParams();
        switch(alg){
            case WISH:
                System.out.println("TimeoutExp: WISH");
                params.timeLimit(timeLimit);
                Wish wish = new Wish(sampleSize, params);
                res = this.timeoutExpAux(dataDir, wish);
                outputPath += "wish";
                break;
            case HOPE:
                System.out.println("TimeoutExp: HOPE");
                params.timeLimit(timeLimit).codeType(codeType);
                Hope hope = new Hope(sampleSize, optimizerType, params);
                res = this.timeoutExpAux(dataDir, hope);
                res += hope.reportEstimates();
                outputPath += "hope";
                break;
        }


        outputPath += "_"+Integer.toString(timeLimit);
        PrintWriter out = new PrintWriter(outputPath);
        System.out.print(res);
        out.print(res);
        out.close();
    }

    private String timeoutExpAux(String dataDir, Solver solver) throws IOException{
        File folder = new File(dataDir);
        StringBuilder result = new StringBuilder();

        for(File f : folder.listFiles()){
            String fileName = f.getName();
            if(fileName.endsWith(".uai")){
                long start = new Date().getTime();
                double estimate = solver.solve(new Ising(f.getAbsolutePath()));
                long end = new Date().getTime();
                result.append(f.getName()).append(",");
                result.append(estimate).append(",");
                result.append( (end-start)/1000);
                result.append("\n");
            }
        }
        return result.toString();
    }

}
