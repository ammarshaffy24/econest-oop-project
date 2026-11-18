package econest.dao;

import econest.model.Person;
import java.io.*;
import java.util.*;

// Shared add / update / remove / save / load logic. Saves to data/<file>.
public abstract class BaseDAO<T extends Person> {
    protected final List<T> list = new ArrayList<>();
    private final File file;

    protected BaseDAO(String fileName) {
        file = new File("data", fileName);
        load();
    }

    protected abstract String toLine(T item);        // object -> one text line
    protected abstract T fromLine(String[] parts);   // text line -> object

    public List<T> getAll() { return list; }

    public void add(T item) {
        item.setId(list.stream().mapToInt(Person::getId).max().orElse(0) + 1);
        list.add(item);
        save();
    }

    public void update(T item) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getId() == item.getId()) list.set(i, item);
        save();
    }

    public void remove(int id) {
        list.removeIf(p -> p.getId() == id);
        save();
    }

    public T findById(int id) {
        for (T p : list) if (p.getId() == id) return p;
        return null;
    }

    private void load() {
        if (!file.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null)
                if (!line.isEmpty()) list.add(fromLine(line.split("\\|", -1)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        file.getParentFile().mkdirs();
        try (PrintWriter w = new PrintWriter(file)) {
            for (T p : list) w.println(toLine(p));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
