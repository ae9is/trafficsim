/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import util.Log;

/**
 * Parse OpenStreetMap XML data file.
 */
public class OsmParser extends DefaultHandler {
	private File xml;
	private Bounds bounds;
	private HashMap<Long, Node> nodes = new HashMap<>();
	private HashMap<Long, Way> ways = new HashMap<>();
	private HashMap<Long, Way> roads = new HashMap<>();
	private HashMap<Long, Relation> relations = new HashMap<>();
	private OsmType currentTag; // used to keep track of where to assign <tag> elements to

	public OsmParser() {
	}

	public OsmParser(File xml) {
		this.xml = xml;
	}

	public void parse() {
		// parsing phase
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(xml, this);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			Log.error(e.toString());
		}
		// post-parse setting of values
		for (Way way : ways.values()) {
			for (Long nodeRef : way.getNodeRefs()) {
				Node node = nodes.get(nodeRef);
				if (node != null) {
					node.addWayRef(way.getId());
					way.addNode(node);
				}
			}
			// Make convenience list of navigable/desirable roads.
			// maxspeed is not actual max speed (check if tag exists) but rather cost factor multiplier for path searching.
			String r = way.getTag("highway");
			if (r != null) {
				Double maxspeed = null;
				if (r.equals("primary") || r.equals("primary_link")) {
					maxspeed = RoadTypes.SPD_PRIMARY;
				} else if (r.equals("secondary") || r.equals("secondary_link") || r.equals("tertiary")
						|| r.equals("tertiary_link") || r.equals("road")) {
					maxspeed = RoadTypes.SPD_NORMAL;
				} else if (r.equals("residential")) {
					maxspeed = RoadTypes.SPD_RESIDENTIAL;
				} else if (r.equals("service") || r.equals("unclassified") || r.equals("track") || r.equals("living_street")
						|| r.equals("construction")) {
					maxspeed = RoadTypes.SPD_SLOW;
				} else if (r.equals("motorway") || r.equals("motorway_link") || r.equals("trunk") || r.equals("trunk_link")) {
					maxspeed = RoadTypes.SPD_HIGHWAY;
				}
				if (maxspeed != null) {
					way.setMaxspeedKm(maxspeed);
					way.setRoad();
					roads.put(way.getId(), way);
				}
			}
		}
		// TODO add ways to relations
		// TODO use relations to construct routes
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("bounds")) {
			Double minlon = Double.parseDouble(attributes.getValue("minlon"));
			Double minlat = Double.parseDouble(attributes.getValue("minlat"));
			Double maxlon = Double.parseDouble(attributes.getValue("maxlon"));
			Double maxlat = Double.parseDouble(attributes.getValue("maxlat"));
			bounds = new Bounds(minlon, minlat, maxlon, maxlat);
		} else if (qName.equalsIgnoreCase("node")) {
			Long id = Long.parseLong(attributes.getValue("id"));
			Double lat = Double.parseDouble(attributes.getValue("lat"));
			Double lon = Double.parseDouble(attributes.getValue("lon"));
			currentTag = new Node(id, lat, lon);
		} else if (qName.equalsIgnoreCase("way")) {
			Long id = Long.parseLong(attributes.getValue("id"));
			Boolean visible = Boolean.parseBoolean(attributes.getValue("visible"));
			currentTag = new Way(id, visible);
		} else if (qName.equalsIgnoreCase("relation")) {
			Long id = Long.parseLong(attributes.getValue("id"));
			Boolean visible = Boolean.parseBoolean(attributes.getValue("visible"));
			currentTag = new Relation(id, visible);
		} else if (qName.equalsIgnoreCase("tag")) {
			String key = attributes.getValue("k");
			String value = attributes.getValue("v");
			currentTag.addTag(key, value);
		} else if (qName.equalsIgnoreCase("nd")) {
			Long ref = Long.parseLong(attributes.getValue("ref"));
			// must be in <way> else FAIL bad data file!
			if (!(currentTag instanceof Way)) {
				throw new SAXException("Bad data file given: a <nd> tag is a child of a non-<way> tag! " + "[Node ref =  " + ref
						+ ", Way id = " + currentTag.getId() + "]");
			}
			((Way) currentTag).addNodeRef(ref);
		} else if (qName.equalsIgnoreCase("member")) {
			// must be in <relation> else FAIL bad data file!
			Long ref = Long.parseLong(attributes.getValue("ref"));
			String type = attributes.getValue("type");
			String role = attributes.getValue("role");
			Member member = new Member(ref, type, role);
			// must be in <way> else FAIL bad data file!
			if (!(currentTag instanceof Relation)) {
				throw new SAXException("Bad data file given: a <member> tag is a child of a non-<relation> tag! "
						+ "[Member ref =  " + ref + ", Relation id = " + currentTag.getId() + "]");
			}
			((Relation) currentTag).addMember(member);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("node") || qName.equalsIgnoreCase("way") || qName.equalsIgnoreCase("relation")) {
			if (currentTag instanceof Node) {
				nodes.put(currentTag.getId(), (Node) currentTag);
			} else if (currentTag instanceof Way) {
				ways.put(currentTag.getId(), (Way) currentTag);
			} else if (currentTag instanceof Relation) {
				relations.put(currentTag.getId(), (Relation) currentTag);
			} else {
				throw new SAXException("Current end tag is one of <node>, <way>, or <relation> "
						+ "but was instantiated with incorrect Java type (Node, Way, Relation)!");
			}
			currentTag = null;
		}
	}

	public static void main(String[] args) {
		File f = new File("resources/sanfrancisco.osm");
		long start = System.currentTimeMillis();
		OsmParser p = new OsmParser(f);
		p.parse();
		long stop = System.currentTimeMillis();
		double time = (stop - start) / 1000.0;
		Log.info("Finished parsing " + f.getAbsolutePath() + "; operation took " + time + " s.");
	}

	public File getXml() {
		return xml;
	}

	public Bounds getBounds() {
		return bounds;
	}

	public HashMap<Long, Node> getNodes() {
		return nodes;
	}

	public HashMap<Long, Way> getWays() {
		return ways;
	}

	public HashMap<Long, Way> getRoads() {
		return roads;
	}

	public HashMap<Long, Relation> getRelations() {
		return relations;
	}

	public void setXml(File xml) {
		this.xml = xml;
	}
	// NOTE: no setters for other variables since set from xml data file
}
