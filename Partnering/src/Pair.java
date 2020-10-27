public class Pair implements Comparable<Pair> {

	private String x;
	private String y;
	private int conflictLocation;
	private Dancer thePartner;
	
	public Pair(String a, String b) {
		
		x = a;
		y = b;
	}
	
	public Pair(int i, Dancer d) {
		
		conflictLocation = i;
		thePartner = d;		
	}	
	
	public boolean has(String s) {
		return (x.equals(s) || y.equals(s));
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}


	public int getConflictLocation() {
		return conflictLocation;
	}
	

	public void setConflictLocation(int conflictLocation) {
		this.conflictLocation = conflictLocation;
	}

	public Dancer getThePartner() {
		return thePartner;
	}

	public void setThePartner(Dancer thePartner) {
		this.thePartner = thePartner;
	}

	@Override
	public int compareTo(Pair o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
