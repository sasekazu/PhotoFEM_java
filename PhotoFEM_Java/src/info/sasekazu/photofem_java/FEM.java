package info.sasekazu.photofem_java;

import java.util.ArrayList;

import org.la4j.factory.Basic1DFactory;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic1DMatrix;
import org.la4j.vector.Vector;

public class FEM {

	// mesh data
	private double[][]		pos;
	private double[][]		initpos;
	private int				npos;
	private int[][]			tri;
	private int				ntri;

	// material property
	// homogeneous elasticity
	private double 			young;		// [Pa]	
	private double 			poisson;	// [-]
	private double			density;	// [kg/m^3]
	private double			thickness;	// [m]

	// boundary condition list
	private ArrayList<Integer>	dlist = new ArrayList<Integer>();
	private ArrayList<Integer>	flist = new ArrayList<Integer>();

	// displacement vector and force vector
	private Vector		u;
	private Vector		ud;
	private Vector		uf;
	private Vector		f;
	private Vector		fd;
	private Vector		ff;

	// stiffness matrices
	private Matrix		De;
	private Matrix[]	Be;
	private Matrix[]	Ke;
	private Matrix		K;
	private Matrix		Kff;
	private Matrix		Kfd;
	private Matrix		Kdd;
	
	// dynamic property
	private Vector		mass;
	
	// Matrix factory
	Basic1DFactory factory = new Basic1DFactory();
	
	public FEM(float[][] vertexf, int[][] index, double young, double poisson, double density, double thickness){

		// cast float[][] -> double[][]
		double vertexd[][] = new double[vertexf.length][vertexf[0].length];
		for(int i=0; i<vertexd.length; ++i){
			for(int j=0; j<vertexd[0].length; ++j){
				vertexd[i][j] = (double)vertexf[i][j];
			}
		}
		
		// mesh
		pos = (double[][])vertexd.clone();
		initpos = (double[][])pos.clone();
		npos = pos.length;
		tri = (int[][])index.clone();
		ntri = tri.length;
		
		// material property
		this.young = young;
		this.poisson = poisson;
		this.density = density;
		this.thickness = thickness;
		
		// boundary condition is set as non-constraint
		flist.clear();
		for(int i=0; i<npos; ++i){
			flist.add(i);
		}
		dlist.clear();
		
		// disp and force
		u = factory.createConstantVector(npos*2, 0);
		f = factory.createConstantVector(npos*2, 0);
		int nd = dlist.size();
		int nf = flist.size();
		ud = factory.createConstantVector(nd, 0);
		uf = factory.createConstantVector(nf, 0);
		fd = factory.createConstantVector(nd, 0);
		ff = factory.createConstantVector(nf, 0);
		
		makeK();
		Kff = K.copy();
	}
	
	
	// make De, Be, Ke, K
	private void makeK(){
		// De
		De = factory.createConstantMatrix(3, 3, 0);
		double tmp = young / (1.0 - poisson * poisson);
		De.set(0, 0, tmp);
		De.set(0, 1, poisson * tmp);
		De.set(1, 0, poisson * tmp);
		De.set(1, 1, tmp);
		De.set(2, 2, 0.5 * (1.0 - poisson) * tmp);
		// Be, mass, Ke
		Be = new Matrix[ntri];
		Ke = new Matrix[ntri];
		mass = factory.createConstantVector(npos, 0);
		double[] p1 = new double[3];
		double[] p2 = new double[3];
		double[] p3 = new double[3];
		double[][] mat = new double[3][3];
		double[][] be = new double[3][6];
		Matrix pmat;
		double delta, dd, area;
		for(int i=0; i<ntri; ++i){
			// prepare ..
			p1[0] = 1.0; p1[1] = pos[tri[1][0]][0]; p1[2] = pos[tri[i][0]][1];
			p2[0] = 1.0; p2[1] = pos[tri[1][1]][0]; p2[2] = pos[tri[i][1]][1];
			p3[0] = 1.0; p3[1] = pos[tri[1][2]][0]; p3[2] = pos[tri[i][2]][1];
			mat[0][0] = 1.0; mat[0][1] = p1[0]; mat[0][2] = p1[1];
			mat[1][0] = 1.0; mat[1][1] = p2[0]; mat[1][2] = p2[1];
			mat[2][0] = 1.0; mat[2][1] = p3[0]; mat[2][2] = p3[1];
			pmat = new Basic1DMatrix(mat);
			delta = pmat.determinant();
			// Be
			dd = 1.0/delta;
			be[0][0] = (p2[1]-p3[1])*dd;
			be[0][1] = 0;
			be[0][2] = (p3[1]-p1[1])*dd;
			be[0][3] = 0;
			be[0][4] = (p1[1]-p2[1])*dd;
			be[0][5] = 0;
			be[1][0] = 0;
			be[1][1] = (p3[0]-p2[0])*dd;
			be[1][2] = 0;
			be[1][3] = (p1[0]-p3[0])*dd;
			be[1][4] = 0;
			be[1][5] = (p2[0]-p1[0])*dd;
			be[2][0] = (p3[0]-p2[0])*dd;
			be[2][1] = (p2[1]-p3[1])*dd;
			be[2][2] = (p1[0]-p3[0])*dd;
			be[2][3] = (p3[1]-p1[1])*dd;
			be[2][4] = (p2[0]-p1[0])*dd;
			be[2][5] = (p1[1]-p2[1])*dd;
			Be[i] = new Basic1DMatrix(be);
			// Ke
			area = Math.abs(0.5*delta);
			Ke[i] = Be[i].transpose().multiply(De).multiply(Be[i]).multiply(area*thickness);
			// mass
			for(int j=0; j<3; ++j){
				mass.set(tri[i][j], mass.get(tri[i][j]) + area*density*thickness*0.333333);
			}
		}
		// K
		K = factory.createConstantMatrix(2*npos, 2*npos, 0);
		{
			int r, c;
			for(int i=0; i<ntri; ++i){
				for(int j=0; j<3; ++j){
					for(int k=0; k<3; ++k){
						for(int l=0; l<2; ++l){
							for(int m=0; m<2; ++m){
								r = 2*tri[i][j]+l;
								c = 2*tri[i][k]+m;
								K.set(r, c, K.get(r,c)+Ke[i].get(2*j+l, 2*k+m));
							}
						}
					}
				}
			}
		}
	}

}
