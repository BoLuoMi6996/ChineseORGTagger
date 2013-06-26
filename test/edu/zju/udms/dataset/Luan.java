package edu.zju.udms.dataset;

import java.util.ArrayList;
import java.util.List;

public class Luan {
	public static void main(String[] args) {
		 System.out.println(4179120/1024/1024);
	}
	public int binsearch(long[] a,int len,long target){
        int low = 0,high = len-1;
        if(a[high]<=target) return high;
        if(a[low]>target) return -1;
        
        int mid;
        while(low!=high-1){
            mid = (low+high)/2;
            if(a[mid]<=target) low = mid;
            else high = mid;
        }
        return low;
    }
    
    public int divide(int dividend, int divisor) {
        // Start typing your Java solution below
        // DO NOT write main() function
  
        boolean yi = false;
        if(dividend>0 && divisor<0 || dividend<0 && divisor>0) yi = true;
        long dividendl = Math.abs((long)dividend);
        long divisorl = Math.abs((long)divisor);
        
        long exp[] = new long [34];
        long exp2[] = new long[34];
        
        exp2[0] = 1;
        for(int i = 1;i<exp2.length;i++){
            exp2[i] = exp2[i-1]+exp2[i-1];
        }
       
        exp[0] = divisorl;
        int i = 0;
        int rs = 0;
        
        while(exp[i]<=dividendl){
            ++i;
            exp[i] = exp[i-1]+exp[i-1];
        }
        
        if(i>0){
            rs += exp2[i-1];
            dividendl -= exp[i-1];
        }
        
        while(dividendl>=divisorl){
            int id = binsearch(exp,i,dividendl);
            if(id<0) break;
            rs += exp2[id];
            dividendl -= exp[id];
            i = id;
        }
        
        if(yi) rs = -rs;
        return rs;
    }
}
