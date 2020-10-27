import java.util.TreeSet;

public class Dancer implements Comparable<Dancer>{

	// ===== Variables per dancer =======	
	private String name;
	private int level;
	private int flagLevel;
	private boolean leader;
	private Dancer[] partners;
	private int usage;
	private TreeSet<Dancer> domain;
	private TreeSet<Dancer> currentDomain;
	public int ID;
	public int dynID;
	
	private TreeSet<Integer> futureFC; // Set of which future Variables you reduced their currDomain 
	
	private TreeSet<Pair> pastFC; // Set of which past Variables AND Value caused you to reduce your currDomain
	private TreeSet<Pair> conflictSet; // Set of past Variable AND Value (whole partnership basically) cause conflict to attempted value
	
	public Dancer(String s, int l, boolean m, int c) {
		ID = c;
		name = s;
		level = l;
		leader = m;
		partners = new Dancer[2];
		usage = 0;
		domain = new TreeSet<Dancer>();
		currentDomain = new TreeSet<Dancer>();
		futureFC = new TreeSet<Integer>();
		pastFC = new TreeSet<Pair>();
		conflictSet = new TreeSet<Pair>();
		
	}
	
	public String getName() {return name;}
	
	public int getLevel() {return level;}
	
	public boolean isLeader() {return leader;}
	
	public Dancer getPartner(int index) {return partners[index];}
	
	public int getUsage() {return usage;}

	public void removePartner(Dancer d) {
//		System.out.printf("Removing %s's partner, %s \n", name, d.getName());
		
		if (partners[0] == d) {
			partners[0] = partners[1];
			partners[1] = null;
			usage--;
			if (Math.abs(d.getLevel() - level) == 2) {flagLevel--;}
		}
		else if (partners[1] == d) {
			partners[1] = null;
			usage--;
			if (Math.abs(d.getLevel() - level) == 2) {flagLevel--;}
		}
		
		d.removePartnerHelper(this);
		
	}
	
	private void removePartnerHelper(Dancer d) {

		if (partners[0] == d) {
			partners[0] = partners[1];
			partners[1] = null;
			usage--;
			if (Math.abs(d.getLevel() - level) == 2) {flagLevel--;}
		}
		else if (partners[1] == d) {
			partners[1] = null;
			usage--;
			if (Math.abs(d.getLevel() - level) == 2) {flagLevel--;}
		}
		
	}
	
	public boolean addPartner(Dancer d) {
		
		if (usage == 2 || d.getUsage() == 2) {
//			System.out.print(name + " Or " + d.getName() +" already has 2 partners!\nAction aborted.\n");
			return false;
		}
		
		if (Math.abs(d.getLevel() - level) == 2) {flagLevel++;}
		
//		System.out.println(usage);
		partners[usage] = d;
		usage++;
		
		d.addPartnerHelper(this);
		return true;
		
	}
	
	private boolean addPartnerHelper(Dancer d) {
		if (usage == 2) {
//			System.out.print(name + " Or " + d.getName() +" already has 2 partners!\nAction aborted.\n");
			return false;
		}
		
		if (Math.abs(d.getLevel() - level) == 2) {flagLevel++;}
		
		partners[usage] = d;
		usage++;
		return true;
	}
	
	public boolean hasPartner(Dancer d) {return (partners[0] == d || partners[1] == d);}
	
	public boolean isComplete() { return usage == 2;}
	
	@Override
	public String toString() {
		String s;
		if(leader) {
			s = ("Name: " + name + "\t\tLevel: " + level + "\t\tRole: Lead\n");
		}
		else {
			s = ("Name: " + name + "\t\tLevel: " + level + "\t\tRole: Follow\n");
		}
		
		return s;
	}

	
	public TreeSet<Dancer> getDomain() {
		return domain;
	}
	

	public void setDomain(TreeSet<Dancer> domain) {
		this.domain = domain;
	}

	public TreeSet<Dancer> getCurrentDomain() {
		return currentDomain;
	}

	public void setCurrentDomain(TreeSet<Dancer> currentDomain) {
		this.currentDomain.addAll(currentDomain);
	}

	public TreeSet<Integer> getFutureFC() {
		return futureFC;
	}

	public void setFutureFC(TreeSet<Integer> futureFC) {
		this.futureFC = futureFC;
	}

	public TreeSet<Pair> getPastFC() {
		return pastFC;
	}

	public void setPastFC(TreeSet<Pair> pastFC) {
		this.pastFC = pastFC;
	}

	public TreeSet<Pair> getConflictSet() {
		return conflictSet;
	}

	public void setConflictSet(TreeSet<Pair> conflictSet) {
		this.conflictSet = conflictSet;
	}

	public int getFlagLevel() {
		return flagLevel;
	}

	public void setFlagLevel(int flagLevel) {
		this.flagLevel = flagLevel;
	}

	@Override
	public int compareTo(Dancer o) {
		// TODO Auto-generated method stub
		return ID - o.ID;
	}
	
}
