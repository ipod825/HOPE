#!/usr/bin/env python
# -*- coding: utf-8 -*-

from utils import enum

FULL_DOMAIN = -1
ConstraintType = enum('UNCONSTRAINED', 'DENSE', 'PEG', 'PEG_REGULAR')
OptimizerStrategy = enum('FIXXED', 'TWO_THIRD', 'BEST')


class LocalSolverOptimizer(object):
    pass


class CplexOptimizer(object):
    pass


class Hope(object):
    def __init__(self, samplesize=7, timeout=10,
                 isLogScale=True, retry=1,
                 optimizerCls=LocalSolverOptimizer,
                 constraintType=ConstraintType.PEG,
                 optimizerStrategy=OptimizerStrategy.FIXXED):

        self.optimizerCls = optimizerCls
        self.constraintType = constraintType
        self.optimzerStrategy = optimizerStrategy

        self.optimizerParams = dict(constraintType=constraintType,
                                    sampleSize=sampleSize,
                                    timeout=timeout,
                                    isLogScale=isLogScale,
                                    retry=retry)

    def estimateQuantile(self, path, fullDim, reducedDim, sampleSize):
        samples = []
        optimizerCls = None
        s = 0

        # choose solverType based on solverStrategy
        if self.solverStrategy == SolverStrategy.FIXXED:
            optimizerCls = self.optimizerCls
        elif self.solverStrategy == SolverStrategy.TWO_THIRD:
            if double(reducedDim)/fullDim*3>2:
                optimizerCls = LocalSolverOptimizer
            else:
                optimizerCls = CplexOptimizer
        elif self.solverStrategy == SolverStrategy.BEST:
            s = 2
            cplex = CplexOptimizer(**self.optimizerParams)
            cplex.estimate(path, fullDim)
            samples.append(cplex.getOptimalValue())
            optimizerCls = CplexOptimizer
            if sampleSize>1:
                ls = LocalSolverOptimizer(**self.optimizerParams)
                ls.estimate(path, fullDim)
                sample.append(ls.getOptimalValue())
                if samples[1]>samples[0]:
                    optimizerCls = LocalSolverOptimizer

    for _ in range(s,sampleSize):
        optimizer = optimizerCls(reducedDim=reducedDim, **self.optimizerParams)
        optimizer.estimate(path, fullDim)
        samples.append(optimizer.getOptimalValue())

	Arrays.sort(samples)
	return  new Estimate(samples[sampleSize/2], self.isLogScale)
  
    def solve(path):
        # initial run(unconstrained, full domain)
        slover = self.optimizerCls()
        solver.estimate(path, FULL_DOMAIN)
        fullDim = solver.getOriginalDim()
        estimates = [None]*len(fullDim+1)
        opt = solve.getOptimalValue()
        extimates.append()
        estimates[0] = Estimate(opt, self.isLogScale)
        estimates[-1] = estimateQuantile(path, fullDim, reducedDim=0, sampleSize=self.sampleSize)
        EarlyStopResult = None
        result = earlyStop(estimates)
        while not result.earlyStop():
            current = result.intv
            d = (current.end+current.start)/2
            estimates[d]=estimateQuantile(path, fullDim, reducedDim=fullDim-d, sampleSize=self.sampleSize)

        prop=0
        for est in estimates:
            if est:
                prop+=2
        print("Estimate proportion:"+ float(prop)/len(estimates))


class Estimate(object):
    def __init__(self, value=0, isLogScale=True):
        self.value = value
        self.isLogScale = isLogScale

    def getEstimate(self):
        return math.log(self.value) if self.isLogScale else self.value

    def getArea(self, logWidth, est=None):
        est = est if est else self.getEstimate()
        return est*math.pow(2, logWidth)
  
class EarlyStopResult{
	  Interval intv
	  double estZ
	  
	  public EarlyStopResult(Interval intv, double estZ){
		  self.intv=intv
		  self.estZ=estZ
	  }
	  
	  public boolean earlyStop(){
		  return intv==null
	  }
}
class Interval{
	  int start
	  int end
	  double diff
	  public Interval(double diff){
		  self.diff=diff
	  }
	  public Interval(){
		  self.diff=0
	  }
}

private EarlyStopResult earlyStop(Estimate[] estimates){
	  Estimate current = estimates[0]
	  double highSum=current.getArea(0)
	  double[] highValues = new double[estimates.length]
	  highValues[0]=current.getEstimate()  
	  for(int i=0<estimates.length-1++){
		  if(estimates[i]!=null){
			  current = estimates[i]
		  }
		  highValues[i+1] = current.getEstimate()
		  highSum += current.getArea(i)
	  }
	  
	  double lowSum=0
	  double[] lowValues = new double[estimates.length]
	  Interval maxDiffIntv = new Interval(-1)
	  Interval currentIntv = null
	  maxDiffIntv.end=estimates.length-1
	  for(int i=estimates.length-1>=0--){
		  //log width of current interval
		  int logWidth=i-1>=0?i-1:0
		  if(estimates[i]!=null){
			  current = estimates[i]
			  System.out.println("e"+i+":"+current.getEstimate())
			  if(i<estimates.length-1){
				  currentIntv.start=i+1
				  if(currentIntv.diff>maxDiffIntv.diff && currentIntv.end>currentIntv.start){
					  maxDiffIntv=currentIntv
				  }
			  }
			  currentIntv = new Interval()
			  currentIntv.end = i
		  }
		  lowSum += current.getArea(logWidth)
		  lowValues[i] = current.getEstimate()
		  currentIntv.diff += current.getArea((highValues[i]-lowValues[i]), logWidth)
	  }
	  System.out.println("h"+highSum)
	  System.out.println("l"+lowSum)
	  if(highSum/lowSum<3){
		  //early stop
		  return new EarlyStopResult(null,(highSum+lowSum)/2)
	  }
	  return new EarlyStopResult(maxDiffIntv,0)
} 

}

if __name__ == '__main__':
    testpath = "/home/mingo/projects/HOPE/problems/test/grid_attractive_n3_w1.0_f1.0.uai"
    hop = Hope()
    hope.solve(testpath)
