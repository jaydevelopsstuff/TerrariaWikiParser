package net.jay.terrariawikiparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.jay.terrariawikiparser.data.Item;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static Gson gson;

    public static final String baseUrl = "https://terraria.wiki.gg";
    public static final String dataIdsUrl = "https://terraria.wiki.gg/wiki/Data_IDs";
    public static final String itemIdsUrl = "https://terraria.wiki.gg/wiki/Item_IDs";
    public static final String tileIdsUrl = "https://terraria.wiki.gg/wiki/Tile_IDs";
    public static final int tilePartsLength = 7;

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean advanced = false;
        List<String> argsL = Arrays.asList(args);
        if(argsL.contains("--prettyPrint")) {
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            gson = builder.create();
        } else gson = new Gson();
        if(argsL.contains("--advanced")) {
            advanced = true;
        }

        Document itemIdsDoc = Jsoup.connect(itemIdsUrl).get();

        Element itemsList = null;
        for(Element element : itemIdsDoc.select(".terraria")) {
            if(element.hasClass("lined") && element.hasClass("sortable")) {
               itemsList = element.child(0);
               break;
            }
        }

        if(itemsList == null) {
            System.out.println("Failed! Couldn't find items list");
            return;
        }

        List<Item> items = parseItemsBasic(itemsList);

        // TODO: Match item and block ids even if specification is wonky
        if(advanced) parseAdvancedItemData(items);

        saveData(items);
    }

    public static List<Item> parseItemsBasic(Element itemsList) {
        List<Item> items = new ArrayList<>();
        int index = 0;
        for(Element itemEl : itemsList.children()) {
            if(index == 0) {
                index++;
                continue;
            }
            Element idEL = itemEl.child(0);
            Element nameEl = itemEl.child(1);
            Element pageLinkEl = nameEl.child(0);
            Element internalNameEl = itemEl.child(2).child(0);

            int id = Integer.parseInt(idEL.text());
            String page = pageLinkEl.attr("href");
            String name = pageLinkEl.text();
            String internalName = internalNameEl.text();

            System.out.println("Parsed basic item data for " + id + " (" + name + ")");
            items.add(new Item(id, name, internalName, page));
            index++;
        }
        return items;
    }

    public static void parseAdvancedItemData(List<Item> items) throws IOException, InterruptedException {
        for(Item item : items) {
            System.out.println("Parsing advanced data for " + item.getId() + " (" + item.getName() + ")");
            Document itemPageDoc = Jsoup.connect(baseUrl + item.getPageUrl()).get();

            Element tagsEl = itemPageDoc.selectFirst(".tags");
            if(tagsEl != null) {

                Element typeEl = tagsEl.child(0);
                if(typeEl.childrenSize() != 0) typeEl = typeEl.child(0);

                item.setType(typeEl.text());
            }

            Element rarityEl = itemPageDoc.selectFirst(".rarity");
            if(rarityEl != null) {
                Element rarityImg = rarityEl.child(1).child(0);

                String rarityStringRaw = rarityImg.attr("alt").substring(14);
                int rarity = 0;
                if(rarityStringRaw.equals("Rainbow")) {
                    rarity = -12;
                } else if(rarityStringRaw.equals("Fiery red")) {
                    rarity = -13;
                } else if(rarityStringRaw.equals("Quest")) {
                    rarity = -11;
                }else {
                    rarity = Integer.parseInt(rarityStringRaw);
                }
                item.setRarity(rarity);
            }

            Element idsElParent = itemPageDoc.selectFirst(".ids");
            if(idsElParent == null) continue;
            Element idsEl = itemPageDoc.selectFirst(".ids").child(0);

            if(idsEl.childrenSize() >= 2 ) {
                if(idsEl.child(1).child(0).text().equals("Tile ID")) {
                    String blockIdText = idsEl.child(1).child(1).text();
                    if(blockIdText.contains("–") || blockIdText.contains(",") || blockIdText.contains("-")) continue;
                    if(blockIdText.contains("(")) {
                        int startBracketIndex = blockIdText.indexOf("(");
                        int endBracketIndex = blockIdText.indexOf(")");

                        String blockIdS = blockIdText.substring(0, startBracketIndex).trim();
                        String blockSubId = blockIdText.substring(startBracketIndex + 1, endBracketIndex);
                        item.setBlockId(Integer.parseInt(blockIdS));
                        item.setBlockSubId(Integer.parseInt(blockSubId));
                    } else item.setBlockId(Integer.parseInt(blockIdText));
                }
            }

            // Avoid rate limiting
            Thread.sleep(50);
        }
    }

    public static void saveData(List<Item> items) throws IOException {
        Item[] itemsArray = items.toArray(new Item[0]);

        String itemsJson = gson.toJson(itemsArray);

        Files.writeString(Path.of("./items.json"), itemsJson);
    }
}
