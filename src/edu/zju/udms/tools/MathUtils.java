package edu.zju.udms.tools;

public class MathUtils {
	public static boolean rangeCheck(int p,int max,int min){
		return p>=min && p<max;
	}
	public static class MatrixIndex{
		private int rowId;
		private int colId;
		public MatrixIndex(int rowId,int colId){
			this.rowId = rowId;
			this.colId = colId;
		}
		public int getRowId(){
			return this.rowId;
		}
		public int getColId(){
			return this.colId;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof MatrixIndex){
				MatrixIndex mio = (MatrixIndex) o;
				return mio.rowId==this.rowId && mio.colId==this.colId;
			}
			return false;
		}
	}
}
