package hasar.com.hasarfiscallibrarygithub;


public class SelectableItem extends Item {
    private boolean isSelected = false;
    private int pos = -1;

    public SelectableItem(Item item, boolean isSelected, int position) {
        super(item.getName());
        this.isSelected = isSelected;
        this.pos = position;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public int getPosition() {
        return pos;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setPosition(int position) {
        pos = position;
    }


}