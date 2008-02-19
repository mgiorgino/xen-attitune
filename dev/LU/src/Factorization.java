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
		long startTime = System.currentTimeMillis();
		System.out.println("Beginning Calculation");
		for (int i = 0; i < 30; i++){
			Matrix M = new Matrix(createMatrix(1000));
			LUDecomposition M1 = M.lu();
		}
		System.out.println("Total Execution time : " + (System.currentTimeMillis()-startTime)/1000. + " seconds");
	}
}
