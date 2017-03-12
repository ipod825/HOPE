package code;

import java.util.Random;

public abstract class Code {
	static public boolean[][] generate(CodeType codeType, int numVars, int numConstraints){
	    return Code.generate(codeType, numVars, numConstraints, false);
	}

	static public boolean[][] generate(CodeType codeType, int numVars, int numConstraints, boolean createDenseIfNull){
		if(numVars==numConstraints){
//			full dimension
			return null;
		}
		boolean[][] res = null;
		switch(codeType){
			case DENSE:
			    res = null;
			    break;
			case SPARSE:
				Sparse c = new Sparse();
				res = c.generate(numVars, numConstraints);
				break;
			case PEG:
				PEG p = new PEG();
				res = p.generate(numVars, numConstraints);
				break;
			default:
				assert false;
				return null;
		}
		if(res==null && createDenseIfNull){
            res = new boolean[numConstraints][numVars];
            Random rand = new Random();
            for(int i=0;i<numConstraints;i++){
                for(int j=0;j<numVars;j++){
                    res[i][j]=rand.nextBoolean();
                }
            }
		}
		return res;
	}
	public  abstract boolean[][] generate(int numVars, int numConstraints);
}
