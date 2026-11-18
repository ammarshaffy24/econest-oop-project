# EcoNest – Task 2 (Manage Clients) and Task 4 (Manage Delivery Agents)

## Folder
```
src/econest/model/   Person, Client, DeliveryAgent
src/econest/dao/     BaseDAO, ClientDAO, DeliveryAgentDAO  (saves to data/*.txt)
src/econest/util/    Validator
src/econest/ui/      ClientPanel, DeliveryAgentPanel  (Swing screens)
src/econest/Main.java  test window with both screens as tabs
diagrams/            Use Case, Class, ER diagrams
```

## Run in an IDE (NetBeans / IntelliJ / Eclipse)
1. New Java project -> copy the `econest` folder into `src`.
2. Run `econest.Main`.
3. Data is saved in a `data` folder next to the project (created automatically).

## Run from the command line (JDK 11+)
```
javac -d out $(find src -name "*.java")
java -cp out econest.Main
```

## Joining the group project
In the main window, add the two panels:
```java
tabs.add("Clients", new ClientPanel());
tabs.add("Delivery Agents", new DeliveryAgentPanel());
```
Other tasks can read the data like this:
```java
Client c = new ClientDAO().findById(id);   // c.getEmail() for Task 8
DeliveryAgent a = new DeliveryAgentDAO().findById(id); // a.getEmail() for Task 9
```
Note: each `new ...DAO()` loads the file again. Share one DAO object, or create a new one
after changes, so teammates see fresh data.
