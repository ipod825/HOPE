package utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;


public class Utils {
	private static double[] frequencies=null;
	
	public static double getFrequency(int i){
		if(frequencies==null){
			try {
				frequencies = Utils.loadFrequencies();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return -1;
			}
		}
		
		if(i<=0 || i>= frequencies.length){
			return -1;
		}
		
		return frequencies[i];
	}
	private static double[] loadFrequencies() throws IOException{
		double[] fs = new double[101];
		int counter=1;
		FileReader fr = new FileReader(new File("/home/jp/Dropbox/HOPE/ldpcreg/f.csv"));
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line,",");
			while(st.hasMoreTokens()){
				fs[counter++]=Double.parseDouble(st.nextToken());
			}
		}
		br.close();
		return fs;
	}
	
	public static double entropy(double p){
		if(p<=0 || p>=1){
			return 0;
		}
		return p*log2(1/p)+(1-p)*log2(1/(1-p));
	}
	
	private static double log2(double p){
		return Math.log(p)/Math.log(2);
	}
	
	public  static String basename(String path){
		int sep = path.lastIndexOf("/");
		return path.substring(sep + 1);
	}

	public static long getDate(){
        return new Date().getTime();
	}

	public static long getThreadID(){
	    return Thread.currentThread().getId();
	}

    public static double median(double[] arr){
        Arrays.sort(arr);
        int middle = arr.length/2;
        if (arr.length%2 == 1) {
            return arr[middle];
        } else {
            return (arr[middle-1] + arr[middle]) / 2.0;
        }
    }


}
