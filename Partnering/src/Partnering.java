import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Stack;
import java.util.Scanner;
import java.util.Random;
import java.io.*;
//System.out.print();

public class Partnering implements Comparator<Dancer>{
	
	public int consCount = 0;
	
	public Dancer tempPartner = new Dancer ("",1,false, -99);
	public int skippedDancers = 0;
	
	public ArrayList<Dancer> dynamicTeam = new ArrayList<Dancer>();
	public int dynamicID = 0;
	
	public ArrayList<Dancer> team = new ArrayList<Dancer>();
	public ArrayList<Dancer> subTeam = new ArrayList<Dancer>();
	public ArrayList<Dancer> leads = new ArrayList<Dancer>();
	public ArrayList<Dancer> follows = new ArrayList<Dancer>();
	public ArrayList<Pair> biConstraints = new ArrayList<Pair>();
	public Stack<TreeSet<Dancer>>[] reductionsAll;
	TreeSet<Dancer> reducs;
	
	public int idCount;

	private boolean consistent;
	private String status;
	
	public ArrayList<Dancer> reuseSaver = new ArrayList<Dancer>();
	
	@Override
	public String toString() {
		String s = "";
		for (int x = 0; x<team.size(); x++) {
			s = s.concat(team.get(x).toString());
		}
		return s;		
	}
	
	public void printSplit() {
		String l = "";
		String f = "";
		for (int x = 0; x<leads.size(); x++) {
			l = l.concat(leads.get(x).toString());
		}
		for (int x = 0; x<follows.size(); x++) {
			f = f.concat(follows.get(x).toString());
		}
		System.out.print("\t\t\t====  LEADS  ====\t\t\t\n"+l);
		System.out.print("\t\t\t==== FOLLOWS ====\t\t\t\n"+f);
	}
	
	public boolean isComplete(){		
		int i = 0;
		while (i < team.size() && team.get(i).getPartner(0) != null && team.get(i).getPartner(1) != null) {
			i++;
		}
		
		if(i == team.size()) {
			return true;
		}
		else {return false;}
	}
	
	public boolean isDynamicComplete(){		
		int i = 0;
		while (i < dynamicTeam.size() && dynamicTeam.get(i).getPartner(0) != null && dynamicTeam.get(i).getPartner(1) != null) {
			i++;
		}
		
		if(i == dynamicTeam.size()) {
			return true;
		}
		else {return false;}
	}
	
	private void buildTeamFromFile(String fileName) {
		idCount = 0;
		try (Scanner scanner = new Scanner(new File(fileName));) {
		    while (scanner.hasNextLine()) {
		        team.add(getDancerInfoFromLine(scanner.nextLine(), idCount));
		        idCount++;
		    }
		} catch (FileNotFoundException e) {
			System.out.print("Cannot Find File " + fileName);
			e.printStackTrace();
		}
		splitDancers();
	}
	
	private void buildNextDynamicDancer() {
		if (team.isEmpty()) {return;} 
		Dancer tempD = team.get(0);
		for (int i = 0; i < team.size(); i++) {
			if (team.get(i).getCurrentDomain().size() < tempD.getCurrentDomain().size()) {
				tempD = team.get(i);
			}
		}
		team.remove(tempD);
		dynamicTeam.add(tempD);
		tempD.dynID = dynamicID;
		dynamicID++;
	}
	
	private Dancer getDancerInfoFromLine(String line, int c) {
		String name = "";
		int level = -2;
		boolean leader = true;
	    try (Scanner rowScanner = new Scanner(line)) {
	        rowScanner.useDelimiter("\t");
	        while (rowScanner.hasNext()) {
	            name = rowScanner.next();
	            level = Integer.parseInt(rowScanner.next());
	            leader = rowScanner.next().contains("l");
	        }
	    }
	    Dancer temp = new Dancer(name, level, leader, c);
	    return temp;
	}

	private void buildBiConstraintsFromFile(String fileName) {
		
		try (Scanner scanner = new Scanner(new File(fileName));) {
		    while (scanner.hasNextLine()) {
		    	biConstraints.add(new Pair(scanner.next(), scanner.next()));
		    }
		} catch (FileNotFoundException e) {
			System.out.print("Cannot Find File " + fileName);
			e.printStackTrace();
		}
		
	}
	
	public void splitDancers() {
		for (Dancer d : team) {
			if (d.isLeader()) {leads.add(d);}
			else {follows.add(d);}
		}
	}
	
	public void naivePairAll() {	
		for(Dancer lead : leads) {
			while(lead.getUsage() < 2)	{
				Dancer follow = follows.get(new Random().nextInt(follows.size()));
				if( !lead.hasPartner(follow) && follow.getUsage() < 2 && lead.getUsage() < 2) {
					lead.addPartner(follow);					
				}
			}
		}
	}
	
	public void printLeadPartners() {	
		for(Dancer lead : leads) {
			if (lead.getUsage() == 2) {
				System.out.print(lead.getName() + " has these partners: " + lead.getPartner(0).getName() + " " + lead.getPartner(1).getName() +"\n");
			}
			else {System.out.print(lead.getName() + " has " + lead.getUsage() + " partners!\n");}
		}
		System.out.print("\n");
	}
	
	public void printFollowPartners() {
		for(Dancer follow : follows) {
			if (follow.getUsage() == 2) {
				System.out.print(follow.getName() + " has these partners: " + follow.getPartner(0).getName() + " " + follow.getPartner(1).getName() +"\n");
			}
			else {System.out.print(follow.getName() + " has " + follow.getUsage() + " partners!\n");}
		}
		System.out.print("\n");
	}
	
	public boolean isLevelConsistent() {
		
		boolean consistent = true;
		
		for (Dancer lead : leads) {
			for (int i = 0; i<lead.getUsage(); i++) {
				if(Math.abs(lead.getLevel() - lead.getPartner(i).getLevel()) > 2) {
					consistent = false;
					System.out.printf("%s has a level conflict with %s \n", lead.getName(), lead.getPartner(i).getName());
				}
			}
		}
		
		return consistent;
	}
	
	public boolean isBinaryConsistent() {
		
		boolean consistent = true;
		
		for (Dancer lead : leads) {
			if (lead.getUsage() == 2) {
				for (Pair cons : biConstraints) {
					if (cons.has(lead.getName())) {
						if (cons.has(lead.getPartner(0).getName()) || cons.has(lead.getPartner(1).getName())) {
							consistent = false;
						}
					}
				}
			}
			else if (lead.getUsage() == 1) {
				for (Pair cons : biConstraints) {
					if (cons.has(lead.getName())) {
						if (cons.has(lead.getPartner(0).getName())) {
							consistent = false;
						}
					}
				}
			}
		}
		
		return consistent;
	}
	
	public boolean isConsistentWith(Dancer i, Dancer ip, Dancer j, Dancer jp) {
		consCount++;
		boolean cons = true;
		if (jp.isComplete()) {return false;}
		if (i == jp && j == ip) { 
			return false;
		}
		if (i == jp || ip == jp) {
			if (jp.isComplete()) {return false;}
			if (jp.getFlagLevel() == 0) {cons = (Math.abs(jp.getLevel() - j.getLevel()) <= 2);}
			else {cons = (Math.abs(jp.getLevel() - j.getLevel()) <= 1);}
		}
		if (ip == j) {
			if (j.getFlagLevel() == 0) {cons = (Math.abs(jp.getLevel() - j.getLevel()) <= 2);}
			else {cons = (Math.abs(jp.getLevel() - j.getLevel()) <= 1);}
		}
		if (j.hasPartner(ip) && ip.getFlagLevel() != 0) {
			if (Math.abs(ip.getLevel() - j.getLevel()) == 2 && Math.abs(ip.getLevel() - i.getLevel()) == 2) {return false;}
			cons = (Math.abs(jp.getLevel() - j.getLevel()) <= 1);
		}
		return cons;
	}
	
	public boolean checkForward(int i, Dancer ip, int j) {
		reducs = new TreeSet<Dancer>();	
		for (Dancer d : team.get(j).getCurrentDomain()) {
			if (!isConsistentWith(team.get(i), ip, team.get(j), d)) { 
//				System.out.print("adding reducs " + d.getName() + " to " + team.get(j).getName() + "\n");
				reducs.add(d);
			}
		}
		
		if (!reducs.isEmpty()) { 
//			System.out.print("Reducing " + team.get(j).getName() + "'s domain. Current size: " + team.get(j).getCurrentDomain().size() + "\n");
			team.get(j).getCurrentDomain().removeAll(reducs);
//			System.out.print(team.get(j).getName() + "'s current domain size: " + team.get(j).getCurrentDomain().size() + "\n");
			reductionsAll[j].push(reducs);
			team.get(i).getFutureFC().add(j); // dancer J added with index j
			team.get(j).getPastFC().add(new Pair(i, ip)); // dancer (i's partner being tested) added with index i
		}
		//System.out.print(!team.get(j).getCurrentDomain().isEmpty() + "\n");
		
		if (team.get(j).getCurrentDomain().isEmpty()) {
			return (team.get(j).isComplete());
		}
		
		return (!team.get(j).getCurrentDomain().isEmpty());
	}
	
	public boolean checkForward2(int i, Dancer ip, int j) {
		reducs = new TreeSet<Dancer>();	
		for (Dancer d : dynamicTeam.get(j).getCurrentDomain()) {
			if (!isConsistentWith(dynamicTeam.get(i), ip, dynamicTeam.get(j), d)) { 
//				System.out.print("adding reducs " + d.getName() + " to " + dynamicTeam.get(j).getName() + "\n");
				reducs.add(d);
			}
		}
		
		if (!reducs.isEmpty()) { 
//			System.out.print("Reducing " + dynamicTeam.get(j).getName() + "'s domain. Current size: " + dynamicTeam.get(j).getCurrentDomain().size() + "\n");
			dynamicTeam.get(j).getCurrentDomain().removeAll(reducs);
//			System.out.print(dynamicTeam.get(j).getName() + "'s current domain size: " + dynamicTeam.get(j).getCurrentDomain().size() + "\n");
			reductionsAll[j].push(reducs);
			dynamicTeam.get(i).getFutureFC().add(j); // dancer J added with index j
			dynamicTeam.get(j).getPastFC().add(new Pair(i, ip)); // dancer (i's partner being tested) added with index i
		}
		//System.out.print(!team.get(j).getCurrentDomain().isEmpty() + "\n");
		
		if (dynamicTeam.get(j).getCurrentDomain().isEmpty()) {
			return (dynamicTeam.get(j).isComplete());
		}
		
		return (!dynamicTeam.get(j).getCurrentDomain().isEmpty());
	}
	
	public boolean dynamicCheckForward(int i, Dancer ip, int j) {
		reducs = new TreeSet<Dancer>();	
		for (Dancer d : team.get(j).getCurrentDomain()) {
			if (!isConsistentWith(dynamicTeam.get(i), ip, team.get(j), d)) { 
//				System.out.print("adding reducs " + d.getName() + " to " + team.get(j).getName() + "\n");
				reducs.add(d);
			}
		}
		
		if (!reducs.isEmpty()) { 
//			System.out.print("Reducing " + team.get(j).getName() + "'s domain. Current size: " + team.get(j).getCurrentDomain().size() + "\n");
			team.get(j).getCurrentDomain().removeAll(reducs);
//			System.out.print(team.get(j).getName() + "'s current domain size: " + team.get(j).getCurrentDomain().size() + "\n");
			reductionsAll[j].push(reducs);
			dynamicTeam.get(i).getFutureFC().add(j); // dancer J added with index j
			team.get(j).getPastFC().add(new Pair(i, ip)); // dancer (i's partner being tested) added with index i
		}
		//System.out.print(!team.get(j).getCurrentDomain().isEmpty() + "\n");
		
		if (team.get(j).getCurrentDomain().isEmpty()) {
			return (team.get(j).isComplete());
		}
		
		return (!team.get(j).getCurrentDomain().isEmpty());
	}
	
	public void updateCurrentDomain(int i) {
		team.get(i).setCurrentDomain(team.get(i).getDomain());
		if (!reductionsAll[i].isEmpty()) {
			for (TreeSet<Dancer> set : reductionsAll[i]) {
				team.get(i).getCurrentDomain().removeAll(set);
			}
		}
		for (Iterator<Dancer> iter = team.get(i).getCurrentDomain().iterator(); iter.hasNext();) {
			Dancer d = iter.next();
			if (d.isComplete()) {iter.remove();}
			else if ( (d.getFlagLevel() != 0 || team.get(i).getFlagLevel() != 0) && Math.abs(team.get(i).getLevel() - d.getLevel()) > 1) {
				iter.remove();
			}
		}
	}
	
	public void dyn2UpdateCurrentDomain(int i) {
		dynamicTeam.get(i).setCurrentDomain(dynamicTeam.get(i).getDomain());
		if (!reductionsAll[i].isEmpty()) {
			for (TreeSet<Dancer> set : reductionsAll[i]) {
				dynamicTeam.get(i).getCurrentDomain().removeAll(set);
			}
		}
		for (Iterator<Dancer> iter = dynamicTeam.get(i).getCurrentDomain().iterator(); iter.hasNext();) {
			Dancer d = iter.next();
			if (d.isComplete()) {iter.remove();}
			else if ( (d.getFlagLevel() != 0 || dynamicTeam.get(i).getFlagLevel() != 0) && Math.abs(dynamicTeam.get(i).getLevel() - d.getLevel()) > 1) {
				iter.remove();
			}
		}
	}
	
	public void undoReductions(int i) {
		//System.out.println("PLEASE BE HERE");
		for (int d : team.get(i).getFutureFC()) {
			//System.out.print("Please tell me something good \n\n\n\n\n I AM HERE?!?!?!?\n");
			reducs = reductionsAll[d].pop();
			team.get(d).getCurrentDomain().addAll(reducs);
			team.get(d).getPastFC().removeIf(p -> (p.getConflictLocation() == i)); // Maybe a bug here due to removal of multiple p
		}
		team.get(i).getFutureFC().clear();
	}
	
	public void dynamicUndoReductions(int i) {
		//System.out.println("PLEASE BE HERE");
		for (int d : dynamicTeam.get(i).getFutureFC()) {
			//System.out.print("Please tell me something good \n\n\n\n\n I AM HERE?!?!?!?\n");
			reducs = reductionsAll[d].pop();
			team.get(d).getCurrentDomain().addAll(reducs);
			team.get(d).getPastFC().removeIf(p -> (p.getConflictLocation() == i)); // Maybe a bug here due to removal of multiple p
		}
		dynamicTeam.get(i).getFutureFC().clear();
	}
	public void dyn2UndoReductions(int i) {
		//System.out.println("PLEASE BE HERE");
		for (int d : dynamicTeam.get(i).getFutureFC()) {
			//System.out.print("Please tell me something good \n\n\n\n\n I AM HERE?!?!?!?\n");
			reducs = reductionsAll[d].pop();
			dynamicTeam.get(d).getCurrentDomain().addAll(reducs);
			dynamicTeam.get(d).getPastFC().removeIf(p -> (p.getConflictLocation() == i)); // Maybe a bug here due to removal of multiple p
		}
		dynamicTeam.get(i).getFutureFC().clear();
	}
	
	public int fcLabel(int x) {
		if (team.get(x).isComplete()) {
//			System.out.print(team.get(x).getName() + " IS COMPLETE, WE SKIPPING!\n");
			skippedDancers ++;
			return x+1;
		}
		consistent = false;
		// Check all possible values`
		for (Iterator<Dancer> iter = team.get(x).getCurrentDomain().iterator(); iter.hasNext();) {
			Dancer d = iter.next();
			if (!consistent) {
//				System.out.print("Trying " + team.get(x).getName() + " with a new partner, " + d.getName() + "\n");
				consistent = team.get(x).addPartner(d);
				
				// Check all future variables
				for (int j = x+1; j < team.size(); j++) {
					if (consistent) {
						updateCurrentDomain(j);
						consistent = checkForward(x, d, j);
						
						if (!consistent) { // THIS IS WHERE YOU TRACK THE CONF-SET INDEX!
							iter.remove();
							undoReductions(x);
							team.get(x).getConflictSet().addAll(team.get(j).getPastFC());
							team.get(x).removePartner(d);
				}}}
				
			}
		}
		
		if (consistent) {return x+1;}
		else {
//			System.out.printf("No possible partners for %s, will begin backtracking on them.\n", team.get(x).getName());
			return fcUnlabel(x);
		}
	}
	
	public int fcUnlabel(int x) { 
		int h = maxConf(team.get(x).getConflictSet(), team.get(x).getPastFC());
		team.get(h).getConflictSet().addAll(team.get(x).getPastFC());
		team.get(h).getConflictSet().addAll(team.get(x).getConflictSet());
		for (Pair p : team.get(h).getConflictSet()) {
			if (p.getConflictLocation() == h) { tempPartner = p.getThePartner();}
		}
		team.get(h).getConflictSet().removeIf(p -> (p.getConflictLocation() == h));

		for (int j = x; j > h; j--) {
			team.get(j).setConflictSet(new TreeSet<Pair>());
			undoReductions(j);
			updateCurrentDomain(j);
		}
		undoReductions(h);
		team.get(h).removePartner(tempPartner);
		team.get(h).getCurrentDomain().remove(tempPartner);
		//this.removeAllPartnersWhenBacktrack(h, x);
		consistent = (!team.get(h).getCurrentDomain().isEmpty());
		return fcLabel(h);
	}
	
	public int dynamicLabel(int x) {
		if (dynamicTeam.get(x).isComplete()) {
//			System.out.print(dynamicTeam.get(x).getName() + " IS COMPLETE, WE SKIPPING!\n");
			skippedDancers ++;
			return x+1;
		}
		consistent = false;
		// Check all possible values`
		for (Iterator<Dancer> iter = dynamicTeam.get(x).getCurrentDomain().iterator(); iter.hasNext();) {
			Dancer d = iter.next();
			if (!consistent) {
//				System.out.print("Trying " + dynamicTeam.get(x).getName() + " with a new partner, " + d.getName() + "\n");
				consistent = dynamicTeam.get(x).addPartner(d);
				
				// Check all future variables
				for (int j = 0; j < team.size(); j++) {
					if (consistent) {
						updateCurrentDomain(j);
						consistent = dynamicCheckForward(x, d, j);
						
						if (!consistent) { // THIS IS WHERE YOU TRACK THE CONF-SET INDEX!
							iter.remove();
							dynamicUndoReductions(x);
							dynamicTeam.get(x).getConflictSet().addAll(team.get(j).getPastFC());
							dynamicTeam.get(x).removePartner(d);
				}}}
				
			}
		}
		
		if (consistent) {return x+1;}
		else {
//			System.out.printf("No possible partners for %s, will begin backtracking on them.\n", team.get(x).getName());
			return dynamicUnlabel(x);
		}
	}
	
	public int dynamicUnlabel(int x) { 
		int h = maxConf(dynamicTeam.get(x).getConflictSet(), dynamicTeam.get(x).getPastFC());
		dynamicTeam.get(h).getConflictSet().addAll(dynamicTeam.get(x).getPastFC());
		dynamicTeam.get(h).getConflictSet().addAll(dynamicTeam.get(x).getConflictSet());
		for (Pair p : dynamicTeam.get(h).getConflictSet()) {
			if (p.getConflictLocation() == h) { tempPartner = p.getThePartner();}
		}
		dynamicTeam.get(h).getConflictSet().removeIf(p -> (p.getConflictLocation() == h));

		for (int j = x; j > h; j--) {
			dynamicTeam.get(j).setConflictSet(new TreeSet<Pair>());
			dynamicUndoReductions(j);
			updateCurrentDomain(j);
			team.add(dynamicTeam.get(j));
			dynamicTeam.remove(j);
			Collections.sort(team);
		}
		undoReductions(h);
		dynamicTeam.get(h).removePartner(tempPartner);
		dynamicTeam.get(h).getCurrentDomain().remove(tempPartner);
		//this.removeAllPartnersWhenBacktrack(h, x);
		consistent = (!dynamicTeam.get(h).getCurrentDomain().isEmpty());
		return dynamicLabel(h);
	}
	
	public int dyn2Unlabel(int x) { 
		int h = maxConf(dynamicTeam.get(x).getConflictSet(), dynamicTeam.get(x).getPastFC());
		dynamicTeam.get(h).getConflictSet().addAll(dynamicTeam.get(x).getPastFC());
		dynamicTeam.get(h).getConflictSet().addAll(dynamicTeam.get(x).getConflictSet());
		for (Pair p : dynamicTeam.get(h).getConflictSet()) {
			if (p.getConflictLocation() == h) { tempPartner = p.getThePartner();}
		}
		dynamicTeam.get(h).getConflictSet().removeIf(p -> (p.getConflictLocation() == h));

		for (int j = x; j > h; j--) {
			dynamicTeam.get(j).setConflictSet(new TreeSet<Pair>());
			dyn2UndoReductions(j);
			dyn2UpdateCurrentDomain(j);
		}
		dyn2UndoReductions(h);
		dynamicTeam.get(h).removePartner(tempPartner);
		dynamicTeam.get(h).getCurrentDomain().remove(tempPartner);
		//this.removeAllPartnersWhenBacktrack(h, x);
		consistent = (!dynamicTeam.get(h).getCurrentDomain().isEmpty());
		return dynamicLabel(h);
	}
	
	public void removeAllPartnersWhenBacktrack(int min, int max) {
		for (int i = min+1; i<max; i++) {
			if (team.get(i).isComplete()) {
				if(team.get(i).getPartner(1).ID > min) {
					team.get(i).removePartner(team.get(i).getPartner(1));
				}
				if(team.get(i).getPartner(0).ID > min) {
					team.get(i).removePartner(team.get(i).getPartner(0));
				}
			}
			else if (team.get(i).getUsage() > 0){
				if(team.get(i).getPartner(0).ID > min) {
					team.get(i).removePartner(team.get(i).getPartner(0));
				}
			}
		}
	}
	
	public void completeForSkipped() {
//		System.out.print("COMPLETE FOR SKIPPED CALLED\n");
		subTeam.clear();
		for (int i = 0; i<team.size(); i++) {
			if (!team.get(i).isComplete()) {
				subTeam.add(team.get(i));
			}
		}
		skippedDancers = 0;
		int i = 0;
		while (status.equals("unknown")) {
			if (consistent) {
				i = skippedLabel(i);
			}
			else {
				i = skippedUnlabel(i);
			}
			
			if (i >= subTeam.size()) {
				
				if (skippedDancers == 0) {status = "Solution found";}
				
				if (!isComplete()) {
					while (skippedDancers!=0) {
						completeForSkipped();
					}
				}
				else {status = "Solution found";}

			}
			else if (i == 0) { status = "Need more backtracking"; }
		}
	}
	
	public void completeDynamicSkipped() {
//		System.out.print("COMPLETE FOR SKIPPED CALLED\n");
		subTeam.clear();
		for (int i = 0; i<dynamicTeam.size(); i++) {
			if (!dynamicTeam.get(i).isComplete()) {
				subTeam.add(dynamicTeam.get(i));
			}
		}
		skippedDancers = 0;
		int i = 0;
		while (status.equals("unknown")) {
			if (consistent) {
				i = skippedDynamicLabel(i);
			}
			else {
				i = skippedDynamicUnlabel(i);
			}
			
			if (i >= subTeam.size()) {
				
				if (skippedDancers == 0) {status = "Solution found";}
				
				if (!isComplete()) {
					while (skippedDancers!=0) {
						completeForSkipped();
					}
				}
				else {status = "Solution found";}

			}
			else if (i == 0) { status = "Need more backtracking"; }
		}
	}
	
	public int skippedLabel(int x) {
		if (subTeam.get(x).isComplete()) {
//			System.out.print(subTeam.get(x).getName() + " IS COMPLETE, WE SKIPPING!\n");
			skippedDancers ++;
			return x+1;
		}
		consistent = false;
		// Check all possible values
		subTeam.get(x).setCurrentDomain(subTeam.get(x).getDomain());
		for (Iterator<Dancer> iter = subTeam.get(x).getCurrentDomain().iterator(); iter.hasNext()&&consistent;) {
			
			if (!consistent) {
				Dancer d = iter.next();
//				System.out.print("(s) Trying " + subTeam.get(x).getName() + " with a new partner, " + d.getName() + "\n");
				consistent = subTeam.get(x).addPartner(d);
				
				if (!consistent && !subTeam.get(x).hasPartner(d)) {
//					System.out.printf("%s adding conflict from %s \n", subTeam.get(x).getName(), d.getName());
					subTeam.get(x).getConflictSet().add(new Pair(d.ID, d.getPartner(d.getUsage()-1)));
				}
				
				// Check all future variables
				for (int j = x+1; j < subTeam.size(); j++) {
					if (consistent) {
						consistent = checkForward(x, d, j);
						
						if (!consistent) { // THIS IS WHERE YOU TRACK THE CONF-SET INDEX!
							iter.remove();
							undoReductions(x);
							subTeam.get(x).getConflictSet().addAll(subTeam.get(j).getPastFC());
							subTeam.get(x).removePartner(d);
				}}}
				
			}
		}
		
		if (consistent) {return x+1;}
		else {
			//System.out.printf("No possible partners for %s, will begin backtracking on them.\n", subTeam.get(x).getName());
			return x;
		} 
	}
	
	public int skippedUnlabel(int x) { 
		int h = maxConf(subTeam.get(x).getConflictSet(), subTeam.get(x).getPastFC());
//		System.out.println("h value is set to: " + h);
		subTeam.get(h).getConflictSet().addAll(subTeam.get(x).getPastFC());
		subTeam.get(h).getConflictSet().addAll(subTeam.get(x).getConflictSet());
		for (Pair p : subTeam.get(h).getConflictSet()) {
			if (p.getConflictLocation() == h) { tempPartner = p.getThePartner();}
		}
		subTeam.get(h).getConflictSet().removeIf(p -> (p.getConflictLocation() == h));

		for (int j = x; j > h; j--) {
			subTeam.get(j).setConflictSet(new TreeSet<Pair>());
			undoReductions(j);
			updateCurrentDomain(j);
		}
		undoReductions(h);
		subTeam.get(h).removePartner(tempPartner);
		subTeam.get(h).getCurrentDomain().remove(tempPartner);
		//removeAllPartnersWhenBacktrack(h, x);
		consistent = (!subTeam.get(h).getCurrentDomain().isEmpty());
		return skippedLabel(h);
	}
	
	public int skippedDynamicLabel(int x) {
		if (subTeam.get(x).isComplete()) {
//			System.out.print(subTeam.get(x).getName() + " IS COMPLETE, WE SKIPPING!\n");
			skippedDancers ++;
			return x+1;
		}
		consistent = false;
		// Check all possible values
		subTeam.get(x).setCurrentDomain(subTeam.get(x).getDomain());
		for (Iterator<Dancer> iter = subTeam.get(x).getCurrentDomain().iterator(); iter.hasNext()&&consistent;) {
			
			if (!consistent) {
				Dancer d = iter.next();
//				System.out.print("(s) Trying " + subTeam.get(x).getName() + " with a new partner, " + d.getName() + "\n");
				consistent = subTeam.get(x).addPartner(d);
				
				if (!consistent && !subTeam.get(x).hasPartner(d)) {
//					System.out.printf("%s adding conflict from %s \n", subTeam.get(x).getName(), d.getName());
					subTeam.get(x).getConflictSet().add(new Pair(d.ID, d.getPartner(d.getUsage()-1)));
				}
				
				// Check all future variables
				for (int j = x+1; j < subTeam.size(); j++) {
					if (consistent) {
						consistent = dynamicCheckForward(x, d, j);
						
						if (!consistent) { // THIS IS WHERE YOU TRACK THE CONF-SET INDEX!
							iter.remove();
							dyn2UndoReductions(x);
							subTeam.get(x).getConflictSet().addAll(subTeam.get(j).getPastFC());
							subTeam.get(x).removePartner(d);
				}}}
				
			}
		}
		
		if (consistent) {return x+1;}
		else {
			//System.out.printf("No possible partners for %s, will begin backtracking on them.\n", subTeam.get(x).getName());
			return x;
		} 
	}
	
	public int skippedDynamicUnlabel(int x) { 
		int h = maxConf(subTeam.get(x).getConflictSet(), subTeam.get(x).getPastFC());
//		System.out.println("h value is set to: " + h);
		subTeam.get(h).getConflictSet().addAll(subTeam.get(x).getPastFC());
		subTeam.get(h).getConflictSet().addAll(subTeam.get(x).getConflictSet());
		for (Pair p : subTeam.get(h).getConflictSet()) {
			if (p.getConflictLocation() == h) { tempPartner = p.getThePartner();}
		}
		subTeam.get(h).getConflictSet().removeIf(p -> (p.getConflictLocation() == h));

		for (int j = x; j > h; j--) {
			subTeam.get(j).setConflictSet(new TreeSet<Pair>());
			dyn2UndoReductions(j);
			dyn2UpdateCurrentDomain(j);
		}
		dyn2UndoReductions(h);
		subTeam.get(h).removePartner(tempPartner);
		subTeam.get(h).getCurrentDomain().remove(tempPartner);
		//removeAllPartnersWhenBacktrack(h, x);
		consistent = (!subTeam.get(h).getCurrentDomain().isEmpty());
		return skippedDynamicLabel(h);
	}
	
	public int maxConf(TreeSet<Pair> conf, TreeSet<Pair> past) {
		int i = 0;
		//if (conf.isEmpty() && past.isEmpty()) {	System.out.println("We are fucked"); }
		for (Iterator<Pair> iter = conf.iterator(); iter.hasNext();) {
			Pair p = iter.next();
			if (p.getConflictLocation() > i) { i = p.getConflictLocation();}
		}
		
		for (Pair p : past) {
			if (p.getConflictLocation() > i) { i = p.getConflictLocation();}
		}
		
		return i;		
	}
	
	public void setInitialDomains() {
		for (Iterator<Dancer> iterator = team.iterator(); iterator.hasNext();) {
		    Dancer d = iterator.next();
		    if(d.isLeader()) {
		        for (Iterator<Dancer> iter2 = follows.iterator(); iter2.hasNext();) {
		        	iter2.forEachRemaining(p -> addToDomain(d,p));
		        }
		    }
		    else {
		        for (Iterator<Dancer> iter2 = leads.iterator(); iter2.hasNext();) {
		        	Dancer ww = iter2.next();
		        	addToDomain(d,ww);
		        }
		    }
		    for (Iterator<Dancer> iter2 = d.getDomain().iterator(); iter2.hasNext();) {
		    	Dancer p = iter2.next();
		    	if (Math.abs(d.getLevel() - p.getLevel()) > 2) {iter2.remove();}
		    }
		}
	}
	
	public void addToDomain(Dancer d, Dancer p) {
		if (clear(d,p)) {
			d.getDomain().add(p);
		}
	}
	
	public boolean clear(Dancer d, Dancer p) {
		for (Pair cons : biConstraints) {
			if (cons.has(d.getName()) && cons.has(p.getName())) {
				return false;
			}
		}
		return true;
	}
	
	public void setAllCurrentDomains() {
		for (int i = 0; i<team.size(); i++) {updateCurrentDomain(i);}
	}
	
	public void solveCSP(int start, boolean cons, boolean dyn2) { 
		consistent = cons;
		status = "unknown";
		int i = start;
		skippedDancers = 0;
		while (status.equals("unknown")) {
			if (consistent) {
				i = fcLabel(i);
			}
			else {
				i = fcUnlabel(i);
			}
			if (i >= team.size()) {
				if (skippedDancers == 0) {
					status = "Solution found";
				}
				if (!isComplete()) {
					while (skippedDancers!=0) {
						completeForSkipped();
					}
				}
				else {status = "Solution found";}
			}
			else if (i == 0) { status = "Unsolvable CSP"; }
		}
		if (status.equals("Need more backtracking")) {
			solveCSP(subTeam.get(0).ID, false, true);
		}
	}
	
	public void solveDVOCSP(int start, boolean cons, boolean dyn2) { 
		consistent = cons;
		status = "unknown";
		int i = start;
		skippedDancers = 0;
		boolean useDyn2 = dyn2;
		while (status.equals("unknown")) {
			
			
			if (consistent) {
				buildNextDynamicDancer(); 
//				System.out.println("Moving on to new dancer: " + dynamicTeam.get(i).getName());
				i = dynamicLabel(i);
			}
			else {
				if (useDyn2) {i = dyn2Unlabel(i);}
				else {i = dynamicUnlabel(i);}
			}
			
			if (i >= dynamicTeam.size() && team.isEmpty()) {
				if (skippedDancers == 0) {
					status = "Solution found";
				}
				
				if (!isDynamicComplete()) {
					while (skippedDancers!=0) {
						completeDynamicSkipped();
					}
				}
				else {status = "Solution found";}
			}
			else if (i == 0) { status = "Unsolvable CSP"; }
			
		}
		if (status.equals("Need more backtracking")) {
			solveDVOCSP(subTeam.get(0).dynID, false, true);
		}
	}
	
	public boolean reuseSolve(ArrayList<Dancer> v1) {
		boolean solved = reuseVariables(new ArrayList<Dancer>(), new ArrayList<Dancer>(), v1);
		return solved;
	}
	
	public boolean reuseVariables(ArrayList<Dancer> v1, ArrayList<Dancer> v2, ArrayList<Dancer> v3) {
		if (v3.isEmpty()) {status = "Solution found"; return true;}
		for (Dancer d : v3) {
			if(!reuseVariables(v1, v2, d, d.getDomain())) {
				return false;
			}
			else {
				v2.add(d);
				v3.remove(d);
				return (reuseVariables(v1, v2, v3));
			}
		}
		return false;
	}
	
	public boolean reuseVariables(ArrayList<Dancer> v1, ArrayList<Dancer> v2, Dancer d, TreeSet<Dancer> domain) {
		if (domain.isEmpty()) {
			return false;
		}
		else {
			for (Dancer val : domain) {
				reuseSaver.clear();
				reuseSaver.addAll(v2);
				if (reuseValue(v1, v2, d, val)) {
					return true;
				}
				else {
					d.removePartner(val);
					v2.clear();
					v2.addAll(reuseSaver);
					domain.remove(val);
					return reuseVariables(v1, v2, d, domain);
				}
			}
		}
		
		return false;
	}

	public boolean reuseValue(ArrayList<Dancer> v1, ArrayList<Dancer> v2, Dancer d, Dancer value) {
//		if (!isReusableConsistent(d, value, v1)) {
//			return false;
//		}
//		else {
//			ArrayList<Dancer> templist = new ArrayList<Dancer>();
//			templist.addAll(v1);
//			templist.addAll(v2);
//			if (isReusableConsistent(d, value, templist)) {
//				return true;
//			}
//			else {
//				for (int i = 1; i < v2.size(); i++) {
//					if
//				}
//			}
//		}
		
		return false;
	}
	
	public static void main(String[] args) { // TODO Need to initialize an order/SVO (unless you leave it as file)
		
//		Partnering myCommittee = new Partnering();
//		
//		myCommittee.buildTeamFromFile("teamFile.txt");
//		myCommittee.buildBiConstraintsFromFile("constraints.txt");
//		int s = myCommittee.team.size();
//		@SuppressWarnings("unchecked")
//		Stack<TreeSet<Dancer>>[] stacks = (Stack<TreeSet<Dancer>>[]) new Stack[s];
//		myCommittee.reductionsAll = stacks;
//		for (int i = 0; i<s; i++) {myCommittee.reductionsAll[i] = (Stack<TreeSet<Dancer>>) new Stack<TreeSet<Dancer>>();}
//		myCommittee.setInitialDomains();
//		myCommittee.setAllCurrentDomains();
//		myCommittee.solveCSP(0, true, false);
//		System.out.print("Normal Complete: " + myCommittee.isComplete() + "\n");
//		System.out.println(myCommittee.status);
//		System.out.printf("We performed %d consistency checks.\n", myCommittee.consCount);

		// ==================== //
		// ==================== //
		
		Partnering dynamicComm = new Partnering();
		
		dynamicComm.buildTeamFromFile("teamFile.txt");
		dynamicComm.buildBiConstraintsFromFile("constraints.txt");
		int w = dynamicComm.team.size();
		@SuppressWarnings("unchecked")
		Stack<TreeSet<Dancer>>[] stackerer = (Stack<TreeSet<Dancer>>[]) new Stack[w];
		dynamicComm.reductionsAll = stackerer;
		for (int i = 0; i<w; i++) {dynamicComm.reductionsAll[i] = (Stack<TreeSet<Dancer>>) new Stack<TreeSet<Dancer>>();}
		dynamicComm.setInitialDomains();
		dynamicComm.setAllCurrentDomains();
		dynamicComm.solveDVOCSP(0, true, false);
		System.out.print("======== \n");
		System.out.print("Dynamic Complete: " + dynamicComm.isDynamicComplete() + "\n");
		System.out.println(dynamicComm.status);
		System.out.printf("We performed %d consistency checks.\n", dynamicComm.consCount);
		
	}

	@Override
	public int compare(Dancer arg0, Dancer arg1) {
		
		return arg0.ID - arg1.ID;
	}
	
}
