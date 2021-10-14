import java.io.*;
import java.util.*;

public class Universal_Hashing extends Open_Addressing{
	int a;
	int b;
	int p;

	protected Universal_Hashing(int w, int seed) {
		super(w, seed, -1);
		int temp = this.m+1; // m is even, so temp is odd here
		while(!isPrime(temp)) {
			temp += 2;
		}
		this.p = temp;
		a = generateRandom(0, p, seed);
		b = generateRandom(-1, p, seed);
	}
	
	/**
	 * Checks if the input int is prime
	 */
	public static boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i*i <= n; i++) {
        	if (n % i == 0) return false;
        }
        return true;
    }
	
	/**
     * Implements universal hashing
     */
	@Override
    public int probe(int key, int i) {
		int mod = power2(r);
		int h = ((((this.a) * key) + this.b) % this.p) % this.m ;

		return (h + i) % mod;
    }

    /**
     * Inserts key k into hash table. Returns the number of collisions encountered,
     * and resizes the hash table if needed
     */
	@Override
    public int insertKeyResize(int key) {
		int collisions = 0;

		collisions += this.insertKey(key);

		//if the load factor is greater than the maximum load factor, then the hash table array should be resized
		if (this.size/this.m > MAX_LOAD_FACTOR){

			int[] tempTable = new int[this.Table.length];
			for(int i=0; i<tempTable.length; i++){tempTable[i] = this.Table[i];} //Copy elements in hash table over to a temporary array

			//Update Values of a,b,p,A,w,r so and create a new hashtable with double the size
			//generate new values for fields of the hash table so that the new hashtable can be built
			this.w = this.w + 2;
			this.r = (int) (this.w - 1) / 2 + 1;

			//compute a new value for prime number p
			int temp = this.m+1; // m is even, so temp is odd here
			while(!isPrime(temp)) { temp += 2; }
			this.p = temp;
			this.a = generateRandom(0, p, seed);
			this.b = generateRandom(-1, p, seed);
			this.A = generateRandom(power2(this.w-1), power2(this.w), seed);
			this.Table = new int[this.m];

			// Fill the array with -1 i.e. an empty hash table
			for(int i=0; i<this.m; i++){ this.Table[i] = -1; }

			for(int i = 0; i < tempTable.length; i++){
				if (tempTable[i] >= 0){
					//compute new hash value for the key in the old array and insert back into bigger array
					//with new hash value that is computed with the new values for A, w, r, and m
					this.insertKey(tempTable[i]);
				}
			}


		}

		return collisions;
    }
}
