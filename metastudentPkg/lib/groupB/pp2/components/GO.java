package pp2.components;

public class GO {

	private String id;
	private String description;
	
	public GO(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	
}
