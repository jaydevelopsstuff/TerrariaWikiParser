package net.jay.terrariawikiparser.data;

public class Item {
    private int id;
    private String name;
    private String internalName;
    private String pageUrl;

    private String type;
    private int rarity;
    private int blockId = -1;
    private int blockSubId = 0;

    public Item(int id, String name, String internalName, String pageUrl) {
        this.id = id;
        this.name = name;
        this.internalName = internalName;
        this.pageUrl = pageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public int getBlockSubId() {
        return blockSubId;
    }

    public void setBlockSubId(int blockSubId) {
        this.blockSubId = blockSubId;
    }
}
