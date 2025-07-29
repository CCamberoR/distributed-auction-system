import java.io.Serializable;

public class AuctionItem implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String username;
	private int price;
	private long time;
	
	public AuctionItem(String username, int price) {
		this.username = username;
		this.price = price;
		this.time = System.currentTimeMillis();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append(time + " - ");
		str.append("Username: " + username + ". ");
		str.append("Price: " + price);
		
		return str.toString();
	}
}