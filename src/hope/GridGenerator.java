package hope;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;


public class GridGenerator {
	Random rand = new Random();

    public static void main(String args[]) {
        GridGenerator g = new GridGenerator();
        // double[] fs = new double[]{0.1};
        // double[] ws = new double[]{0.25,0.5,0.75,1.0,1.25,1.50,1.75,2.0,2.25,2.5,2.75,3.0};
        // for (double f : fs) {
        //    for (double w : ws) {
        //         g.generateGridModel(20, Config.rootDir+"problems/Grids20/", f, w, true);
        //    } 
        // }
        
        g.generateGridModel(20, Config.rootDir+"problems/timeout20/", 0.1, 1.0, true);
    }
	
	public void generateGridModel(int size, String outputFolder, double field, double strength, boolean mixed){
		String outputPath = outputFolder+"grid_"+(mixed?"mixed":"attractive")+"_n"+size+"_w"+strength+"_f"+field+".uai";
		String evidPath = outputPath + ".evid";
		int numVars = size*size;
		int numEdges = (4*2+(size-2)*(size-2)*4+(4*size-8)*3)/2;
		int numFuncs = numVars+numEdges;
		try {
			PrintWriter out = new PrintWriter(outputPath);
			out.println("MARKOV");
			out.println(numVars);
			for(int i=0;i<numVars;i++){
				out.print("2 ");
			}
			out.println();
			out.println(numFuncs);
			printEdgeDomain(out,size);
			printVertexDomain(out,numVars);
			generateInteractions(out, size, strength, mixed);
			generateFields(out, numVars, field);
			out.close();
			
			PrintWriter evidOut = new PrintWriter(evidPath);
			evidOut.println(0);
			evidOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	
	private void printEdgeDomain(PrintWriter out, int size){
		for(int i=0;i<size;i++){
			int rowStart = i*size;
			for(int j=0;j<size;j++){
				int cur = rowStart+j;
				if(j<size-1){
					out.println("2 "+cur+" "+(cur+1));
				}
				if(i<size-1){
					out.println("2 "+cur+" "+(cur+size));
				}
			}
		}
	}
	
	private void generateInteractions(PrintWriter out, int size, double strength, boolean mixed){
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				if(j<size-1){
					this.generateInteraction(out, strength, mixed);
				}
				if(i<size-1){
					this.generateInteraction(out, strength, mixed);
				}
			}
		}
	}
	
	private void generateInteraction(PrintWriter out, double strength, boolean mixed){
		double c =  mixed?(rand.nextDouble()-0.5)*2*strength : rand.nextDouble()*strength;
		out.println(4);
		out.print(Math.exp(c)+" ");
		out.print(Math.exp(-1*c)+" ");
		out.print(Math.exp(-1*c)+" ");
		out.println(Math.exp(c));
		out.println();
	}
	
	private void printVertexDomain(PrintWriter out, int numVars){
		for(int i=0;i<numVars;i++){
			out.println("1 "+i);
		}
	}
	
	private void generateFields(PrintWriter out, int numVars, double field){
		for(int i=0;i<numVars;i++){
			out.println("2");
			double f =  (rand.nextDouble()-0.5)*2*field;
			out.print(Math.exp(-1*f)+" ");
			out.println(Math.exp(f));
			out.println();
		}
	}
}
