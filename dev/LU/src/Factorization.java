import Jama.LUDecomposition;
import Jama.Matrix;


public class Factorization {
	
	public static double[][] createMatrix(int n){
		double [][] M= new double[n][n];
		for(int i = 0 ; i < n ; i++){
			for(int j = 0 ; j < n ; j++){
				M[i][j] = (i+1) * (j+1);
				if(i == j) {
					M[i][j] *= n;
				}
			}
		}
		return M;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1){
			System.out.println("Usage:\n\t java -jar <path>/Factorization.jar <value>" +
					"\n\t where:\n\t\t-<path> is the path to this jar" +
					"\n\t\t-<value> is the number of LU factorization to be executed." +
					"\n\t\t\t30 is a convenient value if 2 CPUs are used, try less when used with just one CPU");
		} else {
		long startTime = System.currentTimeMillis();
		System.out.println("Beginning Calculation");
		int max = Integer.parseInt(args[0]);
		for (int i = 0; i < max; i++){
			Matrix M = new Matrix(createMatrix(1000));
			LUDecomposition M1 = M.lu();
		}
		System.out.println("Total Execution time : " + (System.currentTimeMillis()-startTime)/1000. + " seconds");
		}
	}
}
