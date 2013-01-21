package pp2.components;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Protein {

	private String id;
	private String description;
	private String sequence;
	private GO[] annotations;
	
	public Protein(String id) {
		this.id = id;
	}
	
	public Protein(StringBuffer swissProtEntry) {
		Pattern sq = Pattern.compile("SQ\\s+SEQUENCE.+;([\\w\\s]+)//");
		Pattern id = Pattern.compile("ID\\s+([\\w\\d_]+)");
		Pattern de = Pattern.compile("DE\\s+(.+);\\n");
		Pattern go = Pattern.compile("DR\\s+GO; GO:(\\d+);");
		
		
		Matcher ma = id.matcher(swissProtEntry.toString());
		if(ma.find()) {
			this.id = ma.group(1);
		}
		ma = de.matcher(swissProtEntry.toString());
		if(ma.find()) {
			this.description = ma.group(1);
		}
		ma = sq.matcher(swissProtEntry.toString());
		if(ma.find()) {
			this.sequence = parseSequence(ma.group(1));
		}
		ma = go.matcher(swissProtEntry.toString());
		List<GO> list = new ArrayList<GO>();
		while(!ma.hitEnd()) {
			if(ma.find()) {
				String goID = ma.group(1);
				list.add(new GO(goID));
			}
		}
		annotations = list.toArray(new GO[list.size()]);
	}
	
	public String parseSequence(String sq) {
		return sq.replaceAll("\\s", "").toUpperCase();
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(">");
		for(GO an : annotations) {
			buffer.append(an.toString()+",");
		}
		return buffer.substring(0, buffer.length()-1)+"\n"+sequence;
	}
	
	public void addAnnotation(GO an) {
		//TODO
	}
	
	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getSequence() {
		return sequence;
	}

	public GO[] getAnnotations() {
		return annotations;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
