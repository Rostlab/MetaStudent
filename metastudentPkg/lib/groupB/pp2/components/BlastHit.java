package pp2.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlastHit {
	private String alignment="";
	private String hitname="";
	private String evalue="";
	private int round = 0;
	
	public BlastHit(String hit, int round){
		this.round = round;
		Pattern name = Pattern.compile(">([\\w:_\\-,\\s]+)Length\\s+=\\s+\\d+");
		Pattern ev = Pattern.compile("Expect\\s+=\\s+([\\-eE\\d]+\\.{0,1}\\d+)");
		Matcher ma = name.matcher(hit);
		if (ma.find()){
			hitname = ma.group(1).replaceAll("\\s", "");
		}
		ma = ev.matcher(hit);
		if (ma.find()){
			evalue = ma.group(1);
		}
		
	}
	
	public int getRound() {
		return round;
	}
	
	public String getAlignment(){
		return alignment;
	}
	public String getHitname(){
		return hitname;
	}
	public String getEvalue(){
		return evalue;
	}
	public String toString(){
		return evalue+"\t"+hitname;
	}
}
