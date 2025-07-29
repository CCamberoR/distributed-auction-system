import java.util.SortedSet;
import java.util.TreeSet;

import java.io.Serializable;

public class Product implements Serializable{
	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	private int initialPrice;
	private SortedSet<AuctionItem> bids;
	
	public Product(String name, String description, int initialPrice) {
		this.name = name;
		this.description = description;
		this.initialPrice = initialPrice;
		this.bids = new TreeSet<AuctionItem>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getInitialPrice() {
		return initialPrice;
	}

	public void setInitialPrice(int initialPrice) {
		this.initialPrice = initialPrice;
	}
	
	public SortedSet<AuctionItem> getAuctions() {
		return bids;
	}

	public void setAuctions(SortedSet<AuctionItem> auctions) {
		this.bids = auctions;
	}
	
	public boolean addBid(String username, int price) {
		return this.bids.add(new AuctionItem(username, price));
	}
	
	@Override
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
 
        if (!(obj instanceof Product)) {
            return false;
        }
        
        Product p = (Product) obj;
        
        return name.equals(p.getName());
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Product name: " + this.name + "\n");
		str.append("Description: " + this.description + "\n");
		str.append("Initial price: " + this.initialPrice + "€\n");
		str.append("Bids:\n");
		
		for(AuctionItem item : bids) {
			str.append("\t" + item.toString() + "\n");
		}
		
		return str.toString();
	}
}
