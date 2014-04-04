package CoroUtil.physics;

public class MatrixCalculation {

	public double[][] matrixFirst;
	public double[][] matrixSecond;
	
	public MatrixCalculation(double[][] parMatrixFirst, double[][] parMatrixSecond) {
		matrixFirst = parMatrixFirst;
		matrixSecond = parMatrixSecond;
	}
	
	public double[][] performCalculation() {
		
		double[][] newMatrix = matrixFirst.clone();
		
		for (int i = 0; i < newMatrix.length; i++) {
			for (int j = 0; j < newMatrix[i].length; j++) {
				//System.out.println("data entry: " + newMatrix[i][j]);
			}
		}
		
		return null;
	}
	
}
