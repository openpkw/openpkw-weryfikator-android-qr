package pl.openpkw.openpkwmobile.network;

/**
 * Created by Admin on 28/03/17.
 */

public class Backend {
    private int id;
    private String description;
    private String name;
    private String link;

    public Backend(int id, String description, String name, String link) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
