package Main.telegram_bot.TestFunctionality;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

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
        Type weaponsType = new TypeToken<List<Weapon>>(){}.getType();
        Gson g = new GsonBuilder().
                registerTypeAdapter(Dwarf.class, dwarfJsonSerializer).
                registerTypeAdapter(FacialHair.class, facialHairJsonSerializer).
                registerTypeAdapter(DwarvesBand.class, dwarvesBandJsonSerializer).registerTypeAdapter(weaponsType,
                weaponAdapter).
                setPrettyPrinting().create();
        try (FileWriter fw = new FileWriter("src/main/java/Main/UBot/TestFunctionality/dwarfs.json")) {
            fw.write(g.toJson(company));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(FacialHair.class, facialHairJsonDeserializer).
                registerTypeAdapter(Dwarf.class, dwarfJsonDeserializer).
                registerTypeAdapter(DwarvesBand.class, dwarvesBandJsonDeserializer).
                registerTypeAdapter(weaponsType, weaponAdapter).create();
        try (FileReader fr = new FileReader("src/main/java/Main/UBot/TestFunctionality/dwarfs.json")) {
            DwarvesBand dw = gson.fromJson(fr, DwarvesBand.class);
//            dw.dwarves.stream().map(gson::toJson).forEach(o -> System.out.println(o + "\n------------------"));
            dw.dwarves.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static JsonDeserializer<FacialHair> facialHairJsonDeserializer = (json, typeOfT, context) -> {
        System.out.println("Тип бороды : " + typeOfT.getTypeName());
        FacialHair fh = new FacialHair();
        String data = json.getAsJsonObject().get("Facial hair").getAsString();
        String[] ar = data.split(" ");
        fh.setHaveBeard(data.contains("beard"));
        fh.setHaveMustache(data.contains("mustache"));
        if (fh.isHaveBeard()) fh.setColor(ar[0]);
        return fh;
    };

    static JsonDeserializer<DwarvesBand> dwarvesBandJsonDeserializer = (json, typeOfT, context) -> {
        DwarvesBand company = new DwarvesBand();
        JsonObject all = json.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> set = all.entrySet();
        for (Map.Entry<String, JsonElement> el :
                set) {
            Dwarf dwarf = context.deserialize(el.getValue(), Dwarf.class);
            dwarf.setName(el.getKey());
            company.addDwarf(dwarf);
        }
        return company;
    };

    static JsonDeserializer<Dwarf> dwarfJsonDeserializer = (json, typeOfT, context) -> {
        Dwarf dwarf = new Dwarf();
        dwarf.setDwarfAge(json.getAsJsonObject().get("Dwarf age").getAsInt());
        if (json.getAsJsonObject().has("Dwarf lunch"))
            dwarf.setLunch(json.getAsJsonObject().get("Dwarf lunch").getAsString());
        dwarf.setFacialHair(context.deserialize(json, FacialHair.class));
        if (json.getAsJsonObject().get("Weapons").isJsonArray()) {
            JsonArray weapons = json.getAsJsonObject().get("Weapons").getAsJsonArray();
            List<Weapon> weaponList = context.deserialize(weapons, new TypeToken<List<Weapon>>(){}.getType());
            dwarf.setWeapons(weaponList);
        }
        return dwarf;
    };


    static TypeAdapter<List<Weapon>> weaponAdapter = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, List<Weapon> value) throws IOException {
            out.beginArray();
            for (Weapon w:
                    value) {
                if(w instanceof UniqueWeapon){
                    out.beginObject();
                    out.name("type").value(w.getType());
                    out.name("name").value(String.format("%s from %s", ((UniqueWeapon) w).getName(),
                            ((UniqueWeapon) w).getOrigin()));
                    out.endObject();
                }
                else {
                    out.value(w.getType());
                }
            }
            out.endArray();
        }

        @Override
        public List<Weapon> read(JsonReader in) throws IOException {
            List<Weapon> weapons = new ArrayList<>();
            in.beginArray();
            while (in.hasNext()){
                switch (in.peek()){
                    case STRING ->
                        weapons.add(new Weapon(in.nextString()));
                    case BEGIN_OBJECT -> {
                        in.beginObject();
                        UniqueWeapon uw = new UniqueWeapon();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "name" -> {
                                    String[] ar = in.nextString().split(" from ");
                                    uw.setOrigin(ar[1]);
                                    uw.setName(ar[0]);
                                }
                                case "type" -> uw.setType(in.nextString());
                            }
                        }
                        weapons.add(uw);
                        in.endObject();
                    }
                    default -> in.skipValue();
                }
            }
            in.endArray();
            return weapons;
        }
    };


    static JsonSerializer<Dwarf> dwarfJsonSerializer = (src, typeOfSrc, context) -> {
        JsonObject exitDwarf = new JsonObject();
        exitDwarf.addProperty("Dwarf age", src.getDwarfAge());
//        exitDwarf.addProperty("Dwarf name", src.getName());
        if (src.getLunch() != null) {
            exitDwarf.addProperty("Dwarf lunch", src.getLunch());
        }
        exitDwarf.add("Facial hair", context.serialize(src.getFacialHair()));
        if (src.getWeapons().isEmpty()) {
            exitDwarf.add("Weapons", new JsonPrimitive("No weapons"));
            return exitDwarf;
        }
        exitDwarf.add("Weapons", context.serialize(src.getWeapons(), new TypeToken<List<Weapon>>(){}.getType()));
        return exitDwarf;
    };
    static JsonSerializer<FacialHair> facialHairJsonSerializer = (src, typeOfSrc, context) -> {
        if (!src.isHaveBeard() && !src.isHaveMustache()) return new JsonPrimitive("Is he really a dwarf?");
        StringBuilder j_hair = new StringBuilder();
        if (src.isHaveBeard()) {
            j_hair.append(src.getColor()).append(" beard").append(src.isHaveMustache() ? " and " : "");
        }
        if (src.isHaveMustache()) {
            if (!src.isHaveBeard()) j_hair.append(src.getColor()).append(" ");
            j_hair.append("mustache");
        }
        return new JsonPrimitive(j_hair.toString());
    };
    static JsonSerializer<DwarvesBand> dwarvesBandJsonSerializer = (src, typeOfSrc, context) -> {
        JsonObject dwarves = new JsonObject();
        for (Dwarf d :
                src.dwarves) {
            dwarves.add(d.getName(), context.serialize(d));
        }
        return dwarves;
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

    public FacialHair() {

    }

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
        return "Weapon type : " + type;
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
        return super.toString() + ", weapon name : " + name + ", weapon origin : " + origin;
    }
}
