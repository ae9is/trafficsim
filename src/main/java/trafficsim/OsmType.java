/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.io.Serializable;
import java.util.HashMap;

/**
 *  
 */
@SuppressWarnings("ComparableType")
public abstract class OsmType implements Comparable<Object>, Serializable {
	private Long id; // must be long according to OSM specification!
	private HashMap<String, String> tags = new HashMap<>();

	public Long getId() {
		return id;
	}
	public HashMap<String, String> getTags() {
		return tags;
	}
	public String getTag(String key) {
		return tags.get(key);
	}
	public void setTags(HashMap<String, String> tags) {
		this.tags = tags;
	}
	public void addTag(String key, String value) {
		tags.put(key, value);
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Override
    public int compareTo(Object o){
        if (!(o instanceof OsmType)) {
			return id.toString().compareTo(o.toString());
		}
        final OsmType osmType = (OsmType) o;
        return id.compareTo(osmType.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof OsmType)) {
			return false;
		}
        final OsmType osmType = (OsmType) o;
        if (id != null ? !id.equals(osmType.id) : osmType.id != null) {
			return false;
		}
        return true;
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }
}
