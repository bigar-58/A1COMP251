import java.io.*;
import java.util.*;

public class Open_Addressing {
	public static final double MAX_LOAD_FACTOR = 0.75;
	
	public int m; // number of slots
	public int A; // the default random number
	int w;
	int r;
	int seed;
	public int[] Table;
	int size; // number of elements stored in the hash table

	protected Open_Addressing(int w, int seed, int A) {
		this.seed = seed;
		this.w = w;
		this.r = (int) (w - 1) / 2 + 1;
		this.m = power2(r);
		if (A == -1) {
			this.A = generateRandom((int) power2(w - 1), (int) power2(w), seed);
		} else {
			this.A = A;
		}
		this.Table = new int[m];
		for (int i = 0; i < m; i++) {
			Table[i] = -1;
		}
		this.size = 0;
	}

	/**
	 * Calculate 2^w
	 */
	public static int power2(int w) {
		return (int) Math.pow(2, w);
	}

	public static int generateRandom(int min, int max, int seed) {
		Random generator = new Random();
		if (seed >= 0) {
			generator.setSeed(seed);
		}
		int i = generator.nextInt(max - min - 1);
		return i + min + 1;
	}

	/**
	 * Implements the hash function g(k)
	 */
	public int probe(int key, int i) {
		//Calculate the value of h to compute the probe function g
		int h = (this.A * key) % ((int) power2(this.w)) >> (this.w - this.r);
		int mod = power2(r);

		return (h + i) % this.m;
	}

	/**
	 * Inserts key k into hash table. Returns the number of collisions encountered
	 */
	public int insertKey(int key) {
		int i = 0;
		int hashValue = probe(key, i);
		int collisions = 0;

		//while the hashValue index is not empty, recompute the hashValue to check if the next address is open
		while(this.Table[hashValue] != -1){
			//i+=1;
			hashValue = (probe(key, ++i)) % power2(r);
			//i += 1;
			collisions += 1;
		}

		this.Table[hashValue] = key; //insert the key into the computed open address
		this.size += 1; //Increment the size of the hash table

		return collisions;
	}


	/**
	 * Sequentially inserts a list of keys into the HashTable. Outputs total number of collisions
	 */
	public int insertKeyArray(int[] keyArray) {
		int collision = 0;
		for (int key : keyArray) {
			collision += insertKey(key);
		}
		return collision;
	}

	/**
	 * @para the key k to be searched
	 * @return an int array containing 2 elements:
	 * first element = index of k in this.Table if the key is present, = -1 if not present
	 * second element = number of collisions occured during the search
	 */
	public int[] searchKey(int k) {
		int[] output;
		int i = 0; // might need to start at 1
		int hashValue = probe(k, i);
		int collisions = 0;

		//probe through the hash table to check for the key k
		while(this.Table[hashValue] != k){

			//if the number of collisions is equal to the number of slots in the hash table
			//we know that the hash table does not contain the key k and we can exit
			if (collisions == power2(r) || this.Table[hashValue] == -1){
				output = new int[] {-1, collisions};
				return output;
			}

			//if the key at index hashValue is not k we want to probe to the next hashValue and say we encountered a collision
			//i += 1;
			hashValue = (probe(k, ++i)) % power2(r);
			//i+=1;
			collisions += 1;


		}

		output = new int[] {hashValue, collisions};
		return output;
	}
	
	/**
	 * Removes key k from hash table. Returns the number of collisions encountered
	 */
	public int removeKey(int k){
		int removed = -2; 	// declare a variable for the value that designates a removed element
		int[] searchInfo = searchKey(k);	//integer array with returned values from search
		int collisions = searchInfo[1];		//Number of collisions that were encountered when searching for key k

		if(searchInfo[0] == -1) return collisions;

		if(this.Table[searchInfo[0]] != -1){
			this.Table[searchInfo[0]] = removed;	//Declare the slot that key k was found as deleted using removed identifier
			return collisions; 		// End the function
		}

		return collisions;	//Return the number of slots visited/collisions that occurred while removing k
	}

	/**
	 * Inserts key k into hash table. Returns the number of collisions encountered,
	 * and resizes the hash table if needed
	 */
	public int insertKeyResize(int key) {
		int collisions = 0;

		collisions += this.insertKey(key); //Insert the key and increment by the number of collisions it takes to insert k

		//if the load factor is greater than the maximum load factor, then the hash table array should be resized
		if ((float) this.size/this.m > MAX_LOAD_FACTOR){

			int[] tempTable = new int[this.Table.length];
			for(int i=0; i<tempTable.length; i++){tempTable[i] = this.Table[i];} //Copy elements in hash table over to a temporary array

			this.w = this.w + 2;
			this.r = (int) (this.w - 1) / 2 + 1;
			this.A = generateRandom(power2(this.w-1), power2(this.w), seed);
			this.m = power2(this.r);
			this.Table = new int[this.m];

			// Fill the array with -1 i.e. an empty hash table
			for(int i=0; i<this.m; i++){ this.Table[i] = -1; }

			for(int i = 0; i < this.m; i++){
				if (i < tempTable.length && tempTable[i] >=0){
					//compute new hash value for the key in the old array and insert back into bigger array
					//with new hash value that is computed with the new values for A, w, r, and m
					this.insertKey(tempTable[i]);
				}
			}

		}
		return collisions;
	}

	/**
	 * Sequentially inserts a list of keys into the HashTable, and resize the hash table
	 * if needed. Outputs total number of collisions
	 */
	public int insertKeyArrayResize(int[] keyArray) {
		int collision = 0;
		for (int key : keyArray) {
			collision += insertKeyResize(key);
		}
		return collision;
	}

	/**
	 * @para the key k to be searched (and relocated if needed)
	 * @return an int array containing 2 elements:
	 * first element = index of k in this.Table (after the relocation) if the key is present, 
	 * 				 = -1 if not present
	 * second element = number of collisions occured during the search
	 */
	public int[] searchKeyOptimized(int k) {
		//use the unoptimized search to get the old index and collisions of the last key k
		int[] unopOutput = searchKey(k);
		int collisions = unopOutput[1]; //collisions encountered when using the unoptimized search
		int[] output = new int[2];

		//check if the unoptimized search yielded a non-member of the hash table
		if(unopOutput[0] == -1){
			output[0] = unopOutput[0];
			output[1] = unopOutput[1];
			return output;
		}

		int i = 0; // might need to start at 1
		int hashValue = probe(k, i);

		//probe through the hash table to check for the key k
		while(this.Table[hashValue] != k){
			if(this.Table[hashValue] == -2){
				//change the value of the tombstone value we are searching for to reduce steps
				//and change the value of the old index/hashValue into an empty slot
				this.Table[hashValue] = this.Table[unopOutput[0]];
				this.Table[unopOutput[0]] = -1;

				output[0] = hashValue; //Change the value of the output into the current index/hash value
				output[1] = collisions;
				return output; //Return the output with new hashValue, but same number of collisions that occurred prior to the movement
			}

			//compute new hash value to iterate to the next address in the hash table
			//i += 1;
			hashValue = (probe(k, ++i)) % power2(r);
			//hashValue = (hashValue + probe(k, i)) % power2(r);
			//i += 1;

		}

		output[0] = hashValue;
		output[1] = collisions;
		return output;
	}

	/**
	 * @return an int array of n keys that would collide with key k
	 */
	public int[] collidingKeys(int k, int n, int w) {
		//Integers for finding values that hash to value that k hashes to
		int p =0;
		int q = 0;

		//Compute the value of A with given w
		int A = generateRandom(power2(w-1), power2(w), seed);
		int r = (int) (w - 1) / 2 + 1;
		//Compute the value of h(k) and store into h
		int h = (A * k) % ((int) power2(w)) >> (w - r);
		int[] output = new int[n];
		int i = 0;
		int g = (h + i) % power2(r);


		for(i = 0; i<n; i++){
			g = (h + i) % power2(r);
			output[i] = ((q * power2(w)) + (((p*power2(r)) + g -i) << (w-r)))/A;
			p++;
			q++;
		}

		return output;
	}
}
