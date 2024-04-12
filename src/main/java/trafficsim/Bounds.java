/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

/**
 *  
 */
public class Bounds {
	public Double minlon;
	public Double minlat;
	public Double maxlon;
	public Double maxlat;

	public Bounds() {
	}

	public Bounds(Double minlon, Double minlat, Double maxlon, Double maxlat) {
		this.minlon = minlon;
		this.minlat = minlat;
		this.maxlon = maxlon;
		this.maxlat = maxlat;
	}

	public Double getMinlon() {
		return minlon;
	}

	public Double getMinlat() {
		return minlat;
	}

	public Double getMaxlon() {
		return maxlon;
	}

	public Double getMaxlat() {
		return maxlat;
	}

	public Double[] getBounds() {
		return new Double[] { minlon, minlat, maxlon, maxlat };
	}

	public void setMinlon(Double minlon) {
		this.minlon = minlon;
	}

	public void setMinlat(Double minlat) {
		this.minlat = minlat;
	}

	public void setMaxlon(Double maxlon) {
		this.maxlon = maxlon;
	}

	public void setMaxlat(Double maxlat) {
		this.maxlat = maxlat;
	}

	public void setBounds(Double minlon, Double minlat, Double maxlon, Double maxlat) {
		this.minlon = minlon;
		this.minlat = minlat;
		this.maxlon = maxlon;
		this.maxlat = maxlat;
	}

	public Double getDeltaLon() {
		return Double.valueOf(Math.abs(maxlon - minlon));
	}

	public Double getDeltaLat() {
		return Double.valueOf(Math.abs(maxlat - minlat));
	}
}
