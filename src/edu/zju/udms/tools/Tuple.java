package edu.zju.udms.tools;

public class Tuple {
	public static <A,B> Tuple2<A,B> two(A a, B b){
		return new Tuple2<A, B>(a, b);
	}
	
	public static class Tuple2<A,B>{
		private A first;
		private B second;
		public Tuple2(A a,B b){
			this.first = a;
			this.second = b;
		}
		public A first(){
			return this.first;
		}
		public B second(){
			return this.second;
		}
	}
}
