package Main.UBot.TestFunctionality;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TestFunc2 {
    public static void main(String[] args) {

        DwarvesBand company = new DwarvesBand();

        Dwarf tmpDwarf;

        tmpDwarf = new Dwarf("Orin", 90);
        tmpDwarf.setLunch("Ale with chicken");
        tmpDwarf.setFacialHair(new FacialHair(true, true, "black"));
        tmpDwarf.addWeapon(new UniqueWeapon("sword", "Slasher", "Gondolin"));
        tmpDwarf.addWeapon(new UniqueWeapon("shield", "Oaken Shield", "Moria"));
        tmpDwarf.addWeapon(new Weapon("dagger"));
        company.addDwarf(tmpDwarf);

        tmpDwarf = new Dwarf("Kori", 60);
        // no lunch :(
        tmpDwarf.setFacialHair(new FacialHair(false, true, "red"));
        tmpDwarf.addWeapon(new Weapon("mace"));
        tmpDwarf.addWeapon(new Weapon("bow"));
        company.addDwarf(tmpDwarf);

        tmpDwarf = new Dwarf("Billy Bob", 45);
        tmpDwarf.setLunch("Ale with chicken and potatoes, tea with some cakes");
        tmpDwarf.setFacialHair(new FacialHair(false, false, ""));
        company.addDwarf(tmpDwarf);
//================================================================================================
        Gson g = new GsonBuilder().
                registerTypeAdapter(Dwarf.class, dwarfJsonSerializer).
                registerTypeAdapter(FacialHair.class, facialHairJsonSerializer).
                registerTypeAdapter(DwarvesBand.class, dwarvesBandJsonSerializer).setPrettyPrinting().create();
        try (FileWriter fw = new FileWriter("src/main/java/Main/UBot/TestFunctionality/dwarfs.json")) {
            fw.write(g.toJson(company));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().
                registerTypeAdapter(FacialHair.class, facialHairJsonDeserializer).
                registerTypeAdapter(Dwarf.class, dwarfJsonDeserializer).
                registerTypeAdapter(DwarvesBand.class, dwarvesBandJsonDeserializer).create();
        try (FileReader fr = new FileReader("src/main/java/Main/UBot/TestFunctionality/dwarfs.json")) {
            DwarvesBand dw = gson.fromJson(fr, DwarvesBand.class);
            dw.dwarves.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static JsonDeserializer<FacialHair> facialHairJsonDeserializer = (json, typeOfT, context) -> {
        String hairDescription = json.getAsString();
        boolean hasBeard = false, hasMustache = false;
        String color = "null";
        if(!hairDescription.equals("Is he really a dwarf?")){
            hasBeard = hasMustache = true;
            List<String> list = Arrays.asList(hairDescription.split(" "));
            color = list.get(0).replaceAll(",", "");
        }
        return new FacialHair(hasBeard, hasMustache, color);
    };

    static JsonDeserializer<DwarvesBand> dwarvesBandJsonDeserializer = (json, typeOfT, context) -> {
        DwarvesBand result = new DwarvesBand();
        JsonObject object = json.getAsJsonObject();
        result.dwarves = object.entrySet().stream().map(o -> {
            Dwarf dwarf = context.deserialize(o.getValue(), Dwarf.class);
            dwarf.setName(o.getKey());
            return dwarf;
        }).collect(Collectors.toList());
        return result;
    };

    static JsonDeserializer<Dwarf> dwarfJsonDeserializer = (json, typeOfT, context) -> {
        JsonObject jsonObject = json.getAsJsonObject();
        Dwarf dwarf = new Dwarf();
        dwarf.setDwarfAge(jsonObject.get("age").getAsInt());
        dwarf.setFacialHair(context.deserialize(jsonObject.get("facialHear"), FacialHair.class));
        JsonArray weapons = json.getAsJsonObject().getAsJsonArray("weapons");
        for (JsonElement w:
             weapons) {
            if(w.isJsonPrimitive()){
                dwarf.addWeapon(new Weapon(w.getAsString()));
            }else{
                dwarf.addWeapon(context.deserialize(w, UniqueWeapon.class));
            }
        }
        return dwarf;
    };

    static JsonSerializer<FacialHair> facialHairJsonSerializer = (src, typeOfSrc, context) -> {
        if(!src.isHaveBeard() && !src.isHaveMustache()){
            return new JsonPrimitive("Is he really a dwarf?");
        }
        List<String> l = new ArrayList<>();
        if(src.isHaveBeard()){
            l.add("beard");
        }
        if(src.isHaveMustache()){
            l.add("mustache");
        }
        return new JsonPrimitive(src.getColor() + ", " + String.join(" and ", l));
    };

    static JsonSerializer<Dwarf> dwarfJsonSerializer = (src, typeOfSrc, context) -> {
        JsonObject j_Object = new JsonObject();
        j_Object.addProperty("age", src.getDwarfAge());
        j_Object.add("facialHear", context.serialize(src.getFacialHair()));
        List<Weapon> weapons = src.getWeapons();
        JsonArray j_array = weapons.stream().map(o -> {
            if(o instanceof UniqueWeapon){
                JsonObject wObj = new JsonObject();
                wObj.addProperty("type", o.getType());
                wObj.addProperty("name", ((UniqueWeapon) o).getName());
                wObj.addProperty("origin", ((UniqueWeapon) o).getOrigin());
                return wObj;
            }
            return new JsonPrimitive(o.getType());
        }).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        j_Object.add("weapons", j_array);
        return j_Object;
    };
    static JsonSerializer<DwarvesBand> dwarvesBandJsonSerializer = (src, typeOfSrc, context) -> {
        JsonObject j_obj = new JsonObject();
        for (Dwarf f:
             src.dwarves) {
            j_obj.add(f.getName(), context.serialize(f));
        }
        return j_obj;
    };
}
class DwarvesBand {
    List<Dwarf> dwarves = new LinkedList<>();

    public void addDwarf(Dwarf tmpDwarf) {
        dwarves.add(tmpDwarf);
    }

    // getters & setters
}

class Dwarf {
    private String name;
    private FacialHair facialHair;
    private List<Weapon> weapons = new LinkedList<>();
    private String lunch;
    private int dwarfAge;

    public Dwarf() {
    }

    public Dwarf(String name, int dwarfAge) {
        this.name = name;
        this.dwarfAge = dwarfAge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FacialHair getFacialHair() {
        return facialHair;
    }

    public void setFacialHair(FacialHair facialHair) {
        this.facialHair = facialHair;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    public String getLunch() {
        return lunch;
    }

    public void setLunch(String lunch) {
        this.lunch = lunch;
    }

    public int getDwarfAge() {
        return dwarfAge;
    }

    public void setDwarfAge(int dwarfAge) {
        this.dwarfAge = dwarfAge;
    }

    public void addWeapon(Weapon uniqueWeapon) {
        weapons.add(uniqueWeapon);
    }
    // getters & setters

    @Override
    public String toString() {
        return "Dwarf{" +
                "name='" + name + '\'' +
                ", facialHair=" + facialHair +
                ", weapons=" + weapons +
                ", lunch='" + lunch + '\'' +
                ", dwarfAge=" + dwarfAge +
                '}';
    }
}

/**
 * Описание растительности на лице
 */
class FacialHair {
    private boolean haveBeard;
    private boolean haveMustache;
    private String color;

    public FacialHair(boolean haveBeard, boolean haveMustache, String color) {
        this.haveBeard = haveBeard;
        this.haveMustache = haveMustache;
        this.color = color;
    }

    public boolean isHaveBeard() {
        return haveBeard;
    }

    public void setHaveBeard(boolean haveBeard) {
        this.haveBeard = haveBeard;
    }

    public boolean isHaveMustache() {
        return haveMustache;
    }

    public void setHaveMustache(boolean haveMustache) {
        this.haveMustache = haveMustache;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    // getters & setters

    @Override
    public String toString() {
        return "FacialHair{" +
                "haveBeard=" + haveBeard +
                ", haveMustache=" + haveMustache +
                ", color='" + color + '\'' +
                '}';
    }
}

class Weapon {
    private String type;

    public Weapon() {
        // do nothing
    }

    public Weapon(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
// getters & setters

    @Override
    public String toString() {
        return "Weapon{" +
                "type='" + type + '\'' +
                '}';
    }
}

class UniqueWeapon extends Weapon {
    private String name;
    private String origin;

    public UniqueWeapon() {
        super();
    }

    public UniqueWeapon(String type, String name, String origin) {
        super(type);
        this.name = name;
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
    // getters & setters

    @Override
    public String toString() {
        return "UniqueWeapon{" +
                "name='" + name + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }
}
