package CoroUtil.formation;

import java.util.ArrayList;
import java.util.List;


public class Manager {

	
	public static List<Formation> formations = new ArrayList<Formation>();
	
	public Manager() {
		
	}
	
	public static void addFormation(Formation fm) {
		formations.add(fm);
		//System.out.println("new formations size: " + formations.size());
	}
	
	public void tickUpdate() {
		for (int i = 0; i < formations.size(); i++) {
			Formation fm = formations.get(i);
			
			if (fm != null) {
				if (fm.listEntities.size() == 1) {
					fm.leave(fm.listEntities.get(0));
					formations.remove(fm);
					//System.out.println("removing 1 member formation");
				} else if (fm.listEntities.size() == 0) {
					formations.remove(fm);
					//System.out.println("removing dead formation");
				} else {
					fm.tickUpdate();
				}
			}
		}
	}
	
}
