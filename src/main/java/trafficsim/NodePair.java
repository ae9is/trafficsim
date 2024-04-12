/*
 * SPDX-License-Identifier: AGPL-3.0-only
 *  
 */
package trafficsim;

import java.io.Serializable;

/**
 * This implements a pair of type "Node", a Java version of the Open Street Map (OSM) data type.
 * Which Node is a or b is irrelevant other than ordering using the compareTo() method.
 */
@SuppressWarnings("ComparableType")
public class NodePair implements Comparable<Object>, Serializable {
	// note: legal for nodes to be null
	public Node a;
	public Node b;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof NodePair)) {
			return false;
		}
		final NodePair pair = (NodePair) o;
		if (a == null) {
			if (b == null) {
				if (pair.a == null && pair.b == null) {
					return true;
				}
			} else {
				if ((b.equals(pair.a) && pair.b == null) ||
						(b.equals(pair.b) && pair.a == null)) {
					return true;
				}
			}
		} else {
			if (b != null) {
				if ((a.equals(pair.a) && b.equals(pair.b)) ||
						(a.equals(pair.b) && b.equals(pair.a))) {
					return true;
				}
			} else {
				if ((a.equals(pair.a) && pair.b == null) ||
						(a.equals(pair.b) && pair.a == null)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (a != null && b != null) ? Integer.valueOf(a.hashCode() + b.hashCode()).hashCode() : 0;
	}

	@Override
	public int compareTo(Object o) {
		// order doesn't matter (beyond equality), but must be consistent!
		if (!(o instanceof NodePair) || !this.equals((NodePair) o)) {
			if (a != null) {
				if (b != null) {
					// need to add extra character for ex. ";" to avoid introducing rare bug
					// where for ex. node set { 12, 34 } equals { 123, 4 }
					return (a.toString() + ";" + b.toString()).compareTo(o.toString());
				} else {
					return (a.toString()).compareTo(o.toString());
				}
			} else {
				if (b == null) {
					return "".compareTo(o.toString());
				} else {
					return (b.toString()).compareTo(o.toString());
				}
			}
		}
		// no need for another this.equals(o) check since all cases return if if() above is passed
		return 0;
	}
}
