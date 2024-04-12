/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.io.Serializable;

/**
 *  
 */
@SuppressWarnings("ComparableType")
public class Member implements Comparable<Object>, Serializable {
	Long ref; // same as id
	String type; // could be enum
	String role; // can't be enum
	// List of example roles:
	// from, to, via, east, west, stop, backward, outer, crater, ground_zero (?!) ...

	public Member(Long ref, String type, String role) {
		this.ref = ref;
		this.type = type;
		this.role = role;
	}

	public Long getRef() {
		return ref;
	}
	public String getType() {
		return type;
	}
	public String getRole() {
		return role;
	}
	public void setRef(Long ref) {
		this.ref = ref;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setRole(String role) {
		this.role = role;
	}

	@Override
    public int compareTo(Object o){
        if (!(o instanceof Member)) {
			return ref.toString().compareTo(o.toString());
		}
        final Member member = (Member) o;
        return member.compareTo(member.getRef());
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof Member)) {
			return false;
		}
        final Member member = (Member) o;
        if (ref != null ? !ref.equals(member.ref) : member.ref != null) {
			return false;
		}
        return true;
    }

	@Override
    public int hashCode() {
        return (ref != null ? ref.hashCode() : 0);
    }
}
