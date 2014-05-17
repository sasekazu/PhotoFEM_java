package info.sasekazu.photofem_java;

import java.util.ArrayList;

import org.jblas.FloatMatrix;

public class FEM {

	// mesh data
	private float[][]		pos;
	private float[][]		initpos;
	private int				npos;
	private int[][]			tri;
	private int				ntri;

	// material property
	// homogeneous elasticity
	private float 			young;		// [Pa]	
	private float 			poisson;	// [-]
	private float			density;	// [kg/m^3]
	private float			thickness;	// [m]

	// boundary condition list
	private ArrayList<Integer>	dlist = new ArrayList<Integer>();
	private ArrayList<Integer>	flist = new ArrayList<Integer>();

	// displacement vector and force vector
	private FloatMatrix		u;
	private FloatMatrix		ud;
	private FloatMatrix		uf;
	private FloatMatrix		f;
	private FloatMatrix		fd;
	private FloatMatrix		ff;

	// stiffness matrices
	private FloatMatrix		De;
	private FloatMatrix[]	Be;
	private FloatMatrix[]	Ke;
	private FloatMatrix		K;
	private FloatMatrix		Kff;
	private FloatMatrix		Kfd;
	private FloatMatrix		Kdd;
	
	// dynamic property
	private FloatMatrix		mass;
	
	public FEM(float[][] vertex, int[][] index, float young, float poisson, float density, float thickness){
		
		// mesh
		pos = (float[][])vertex.clone();
		initpos = (float[][])pos.clone();
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
		u = FloatMatrix.zeros(npos*2);
		f = FloatMatrix.zeros(npos*2);
		int nd = dlist.size();
		int nf = flist.size();
		ud = FloatMatrix.zeros(nd);
		uf = FloatMatrix.zeros(nf);
		fd = FloatMatrix.zeros(nd);
		ff = FloatMatrix.zeros(nf);
		
		makeK();
		Kff.copy(K);
	}
	
	// make De, Be, Ke, K
	private void makeK(){
		// De
		De = FloatMatrix.zeros(3,3);
		float tmp = young / (1.0f - poisson * poisson);
		De.put(0, 0, tmp);
		De.put(0, 1, poisson * tmp);
		De.put(1, 0, poisson * tmp);
		De.put(1, 1, tmp);
		De.put(2, 2, 0.5f * (1.0f - poisson) * tmp);
		// Be, mass, Ke
		Be = new FloatMatrix[ntri];
		Ke = new FloatMatrix[ntri];
		mass = FloatMatrix.zeros(npos);
		FloatMatrix Bt;
		float[] p1,p2,p3;
		float[][] mat;
		float delta, dd, area;
		for(int i=0; i<ntri; ++i){
			// Be
			Be[i] = FloatMatrix.zeros(3,6);
			p1 = new float[]{pos[tri[i][0]][0],pos[tri[i][0]][1]};
			p2 = new float[]{pos[tri[i][1]][0],pos[tri[i][1]][1]};
			p3 = new float[]{pos[tri[i][2]][0],pos[tri[i][2]][1]};
			mat = new float[][]{
					{1,p1[0],p1[1]},
					{1,p2[0],p2[1]},
					{1,p3[0],p3[1]},
			};
			delta = mat[0][0]*mat[1][1]*mat[2][2]
					+ mat[1][0]*mat[2][1]*mat[0][2]
					+ mat[2][0]*mat[0][1]*mat[1][2]
					- mat[2][0]*mat[1][1]*mat[0][2]
					- mat[1][0]*mat[0][1]*mat[2][2]
					- mat[0][0]*mat[2][1]*mat[1][2];
			dd = 1.0f/delta;
			Be[i].put(0,0,(p2[1]-p3[1])*dd);
			Be[i].put(0,2,(p3[1]-p1[1])*dd);
			Be[i].put(0,4,(p1[1]-p2[1])*dd);
			Be[i].put(1,1,(p3[0]-p2[0])*dd);
			Be[i].put(1,3,(p1[0]-p3[0])*dd);
			Be[i].put(1,5,(p2[0]-p1[0])*dd);
			Be[i].put(2,0,(p3[0]-p2[0])*dd);
			Be[i].put(2,1,(p2[1]-p3[1])*dd);
			Be[i].put(2,2,(p1[0]-p3[0])*dd);
			Be[i].put(2,3,(p3[1]-p1[1])*dd);
			Be[i].put(2,4,(p2[0]-p1[0])*dd);
			Be[i].put(2,5,(p1[1]-p2[1])*dd);
			// Ke and mass
			Bt = Be[i].transpose();
			area = Math.abs(0.5f*delta);
			System.out.println("Bt\n" + Bt);
			System.out.println("De\n" + De);
			System.out.println("Be[i]\n" + Be[i]);
			System.out.println("mul\n" + area*thickness);
			Ke[i] = Bt.mmul(De).mmul(Be[i]).mul(area*thickness);
			// mass
			for(int j=0; j<3; ++j){
				mass.put(tri[i][j], mass.get(tri[i][j]) + area*density*thickness*0.333333f);
			}
		}
		// K
		K = FloatMatrix.zeros(2*npos, 2*npos);
		{
			int r, c;
			for(int i=0; i<ntri; ++i){
				for(int j=0; j<3; ++j){
					for(int k=0; k<3; ++k){
						for(int l=0; l<2; ++l){
							for(int m=0; m<2; ++m){
								r = 2*tri[i][j]+l;
								c = 2*tri[i][k]+m;
								K.put(r, c, K.get(r,c)+Ke[i].get(2*j+l, 2*k+m));
							}
						}
					}
				}
			}
		}
	}

}
