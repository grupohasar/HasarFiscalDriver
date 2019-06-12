package hasar.com.hasarfiscallibrarygithub;

public class Item {

    private String name;

    public Item(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        Item itemCompare = (Item) obj;
        return itemCompare.getName().equals(this.getName());

    }
}