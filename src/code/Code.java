package code;

public abstract class Code {
	static public boolean[][] generate(CodeType codeType, int numVars, int numConstraint){
		if(numVars==numConstraint){
//			full dimension
			return null;
		}
		switch(codeType){
			case DENSE:
				return null;
			case SPARSE:
				Sparse c = new Sparse();
				return c.generate(numVars, numConstraint);
			case PEG:
				PEG p = new PEG();
				return p.generate(numVars, numConstraint);
			default:
				assert false;
				return null;
		}
	}
	public  abstract boolean[][] generate(int numVars, int numConstraint);
}