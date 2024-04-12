/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.ArrayList;

/**
 *  
 */
public class Relation extends OsmType {
	private Boolean visible;
	private ArrayList<Member> members = new ArrayList<>(); // unordered list

	public Relation() {
	}

	public Relation(Long id, Boolean visible) {
		this.setId(id);
		this.visible = visible;
	}

	public Boolean getVisible() {
		return visible;
	}

	public ArrayList<Member> getMembers() {
		return members;
	}

	public void setMembers(ArrayList<Member> members) {
		this.members = members;
	}

	public void addMember(Member member) {
		members.add(member);
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}
}
