package pt.SecDepVNE.Common;

import java.util.Random;

/**
 * Generates random resources to the nodes and links
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class ResourceGenerator {

	private static int MIN_REQ_LIFETIME = 250;
	private static int REQ_LIFETIME = 1000;

	private Random random;

	public ResourceGenerator(){
		random = new Random();
	}

	public double generateCPU(int maxCPU){
		return (random.nextInt(Integer.MAX_VALUE)/(double)Integer.MAX_VALUE + 1.) * maxCPU * 0.5; // if max = 100; (0 to 1) + 1 = (1 to 2) * 50 = (50 to 100)
	}

	public double generateBandwidth(int maxBw){
		return (random.nextInt(Integer.MAX_VALUE)/(double)Integer.MAX_VALUE + 1.) * maxBw * 0.5;
	}

	// Exponential distribution for lifeTime - Ok 31jan17
	public int generateLifeTime(){
		return MIN_REQ_LIFETIME + (int)(-Math.log((random.nextInt(Integer.MAX_VALUE)) / (double)Integer.MAX_VALUE) * (REQ_LIFETIME - MIN_REQ_LIFETIME));
	}

	// Poisson distribution for arrivalTime
	public int generateArrivalTime(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;

		do {
			k++;
			p *= Math.random();
		} while (p > L);

		return k - 1;
	}


	public int generateSecurity(int max){
		return (random.nextInt(max));
	}

	public double generateWeightedSecurity(int max){

		switch(generateSecurity(max)){
		case 0:
			return 1;
		case 1:
			return 1.1;
		case 2:
			return 1.2;
		}
		
		return 1;
	}
	
	public double generateWeightedSecurity(int percentOfNoSecNodes, int percentOfMediumSecNodesmax){

		int res = random.nextInt(100);
		
		if(res >= 0 && res <= percentOfNoSecNodes -1){
			return 1;
		}else if(res >= percentOfNoSecNodes + 1 && res <= percentOfNoSecNodes + percentOfMediumSecNodesmax -1){
			return 1.1;
		}else{
			return 1.2;
		}
	}

	// Define a security level taking into account some probability
	public double randomWithProbability() {
		double res = 0;

		res = generateSecurity(100);

		if(res >= 0 && res <= 4)
			res = 1;
		else if(res >= 5 && res <= 44)
			res = 1.2;
		else
			res = 5;

		return res;
	}
	
	public double generateWeightedSecurityNewSecParam(int max){

		switch(generateSecurity(max)){
		case 0:
			return 1.0;
		case 1:
			return 1.2;
		case 2:
			return 5.0;
		default:
			return 1.0;
		}
		
	}
	
	public double generateWeightedSecurityNewSecParam(int percentOfNoSecNodes, int percentOfMediumSecNodesmax){

		int res = random.nextInt(100);
		
		if(res >= 0 && res <= percentOfNoSecNodes -1){
			return 1;
		}else if(res >= percentOfNoSecNodes && res <= percentOfNoSecNodes + percentOfMediumSecNodesmax -1){
			return 1.2;
		}else{
			return 5.0;
		}
	}
}
