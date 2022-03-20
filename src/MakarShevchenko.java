import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class MakarShevchenko {
    public static void main(String[] args) {
        try {
            // BookFinding solver = new BookFinding(new BookFinding.AStarSolver(1));
            BookFinding solver = new BookFinding(new BookFinding.BacktrackingSolver());
            System.out.print("Type anything if you want to insert the positions of agents and perception scenario " +
                    "manually, \notherwise (map will be generated automatically) just hit enter\n> ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String inputLine = reader.readLine();
            BookFinding.InputReader inputReader = new BookFinding.AutogenInputReader();
            if (inputLine.length() > 0) {
                inputReader = new BookFinding.ConsoleInputReader();
            }
            solver.readInput(inputReader);
            if (!solver.isConfigValid()) {
                throw new Exception("Invalid input. Enter valid data.");
            }
            solver.findPath();
            solver.printOutput();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class BookFinding {
        Map initMap;
        int actorPerception;
        int initActorX, initActorY;
        int exitX, exitY;
        Solver solver;

        static String FILCH = "fitch";
        static String CAT = "cat";
        static String ACTOR = "actor";
        static String BOOK = "book";
        static String CLOAK = "cloak";
        static String EXIT = "exit";

        public BookFinding(Solver solver) {
            this.solver = solver;
        }

        public void readInput(InputReader inputReader) {
            Vector<Object> input = inputReader.readInput();
            this.initMap = (Map) input.get(0);
            this.actorPerception = (int) input.get(1);
            Pair<Integer, Integer> initActorPos = (Pair<Integer, Integer>) input.get(2);
            this.initActorX = initActorPos.first;
            this.initActorY = initActorPos.second;
            Pair<Integer, Integer> exitPos = (Pair<Integer, Integer>) input.get(3);
            this.exitX = exitPos.first;
            this.exitY = exitPos.second;
        }

        public boolean isConfigValid() {
            if (!isMapValid() ||
                // "Possible scenarios are 1 and 2"
                !(this.actorPerception == 1 || this.actorPerception == 2) ||
                // "You start from bottom left"
                !(this.initActorX == 0 && this.initActorY == 0)) {
                return false;
            }
            return true;
        }

        public void findPath() {

        }

        public void printOutput() {
            Vector<Object> outputValues = this.solver.solve(this.initMap, new Pair<>(initActorX, initActorY),
                                                            new Pair<>(exitX, exitY), actorPerception);
            String name = (String) outputValues.get(0);
            boolean isSuccess = (boolean) outputValues.get(1);
            Vector<Pair<Integer, Integer>> path = (Vector<Pair<Integer, Integer>>) outputValues.get(2);
            Long spentTimeNs = (Long) outputValues.get(3);

            System.out.printf("Name of the algorithm: %s\n", name);

            String outcome = "Lose";
            if (isSuccess) {
                outcome = "Win";
            }
            System.out.printf("Outcome: %s\n", outcome);

            System.out.printf("The number of steps algorithm took to reach exit door: %d\n", path.size());

            System.out.print("The path on the map: ");
            for (Pair<Integer, Integer> coordinate: path) {
                System.out.printf("[%d,%d] ", coordinate.first, coordinate.second);
            }
            System.out.println();

            System.out.println("Path is highlighted on the map:");
            String actor = "🤠";
            String cat = "\uD83D\uDE3E";
            String fitch = "\uD83E\uDDD9\u200D";
            String book = "\uD83D\uDCD3";
            String cloak = "\uD83E\uDDE5";
            String perception = "\uD83D\uDED1";
            String step = "\uD83E\uDDB6";
            String empty = "▪️";
            String mix = "\uD83C\uDF81";
            String exit = "\uD83D\uDEAA";
            for (int y = this.initMap.sizeY - 1; y >= 0; y--) {
                System.out.printf("%s ", numToEmoji(y));
                for (int x = 0; x < this.initMap.sizeX; x++) {
                    Map.Cell cell = this.initMap.getCell(x, y);
                    boolean isVisited = false;
                    for (Pair<Integer, Integer> coordinate: path) {
                        if ((int) coordinate.first == x && (int) coordinate.second == y) {
                            isVisited = true;
                            break;
                        }
                    }
                    if (isVisited) {
                        System.out.printf("%s", step);
                    }
                    else if (cell.container.size() > 1) {
                        System.out.printf("%s", mix);
                    }
                    else if (cell.contains(EXIT)) {
                        System.out.printf("%s", exit);
                    }
                    else if (cell.contains(BOOK)) {
                        System.out.printf("%s", book);
                    }
                    else if (cell.contains(CLOAK)) {
                        System.out.printf("%s", cloak);
                    }
                    else if (cell.contains(ACTOR)) {
                        System.out.printf("%s", actor);
                    }
                    else if (cell.contains(CAT)) {
                        System.out.printf("%s", cat);
                    }
                    else if (cell.contains(FILCH)) {
                        System.out.printf("%s", fitch);
                    }
                    else if (cell.isUnderPerception) {
                        System.out.printf("%s", perception);
                    }
                    else {
                        System.out.printf("%s", empty);
                    }
                }
                System.out.println();
            }
            System.out.print("\uD83D\uDD3C ");
            for (int x = 0; x < this.initMap.sizeX; x++) {
                System.out.print(numToEmoji(x));
            }
            System.out.println();
            System.out.printf("Time taken by the algorithm to reach the exit door: %d ns\n", spentTimeNs);
        }

        private String numToEmoji(int num) {
            String result = "";
            String numStr = String.valueOf(num);
            for (int i = 0; i < numStr.length(); i++) {
                int digit = Character.getNumericValue(numStr.charAt(i));
                Vector<String> emojiSym = new Vector<>();
                emojiSym.add("0️⃣");
                emojiSym.add("1️⃣");
                emojiSym.add("2️⃣");
                emojiSym.add("3️⃣");
                emojiSym.add("4️⃣");
                emojiSym.add("5️⃣");
                emojiSym.add("6️⃣");
                emojiSym.add("7️⃣");
                emojiSym.add("8️⃣");
                emojiSym.add("9️⃣");
                result += emojiSym.elementAt(digit);
            }
            return result;
        }

        private boolean isMapValid() {
            for (int y = 0; y < this.initMap.sizeY; y++) {
                for (int x = 0; x < this.initMap.sizeX; x++) {
                    Map.Cell cell = this.initMap.getCell(x, y);
                    boolean hasExit = false;
                    boolean hasInspector = false;
                    boolean hasBook = false;
                    boolean hasCloak = false;
                    for (Object item: cell.container) {
                        if (item.equals(EXIT)) {
                            hasExit = true;
                        }
                        if (item.equals(CAT) || item.equals(FILCH)) {
                            hasInspector = true;
                        }
                        if (item.equals(BOOK)) {
                            hasBook = true;
                        }
                        if (item.equals(CLOAK)) {
                            hasCloak = true;
                        }
                    }
                    if (hasExit && (cell.isUnderPerception || hasBook)) {
                        return false;
                    }
                    if (hasBook && cell.isUnderPerception) {
                        return false;
                    }
                    if (hasCloak && cell.isUnderPerception) {
                        return false;
                    }
                }
            }
            return true;
        }

        public interface Solver {
            Vector<Object> solve(Map map, Pair<Integer, Integer> initActorPos, Pair<Integer, Integer> exitPos,
                                 int perception); // Output: [String name, boolean isSuccess, \
                                                  // Vector<Pair<Integer, Integer>> path, Long spentTimeNs]
        }

        public static class AStarSolver implements Solver {
            private int stepSize;

            public AStarSolver(int stepSize) {
                this.stepSize = stepSize;
            }

            @Override
            public Vector<Object> solve(Map initMap, Pair<Integer, Integer> initActorPos, Pair<Integer, Integer> exitPos,
                              int perception) {
                // Output: [String name, boolean isSuccess, \
                // Vector<Pair<Integer, Integer>> path, Long spentTimeNs]

                Instant previous = Instant.now();

                // init map
                Map map = initMap.clone();
                calculateHeuristics(exitPos, map);

                // init -> try to find book or cloak
                Vector<Object> objsToFind = new Vector<>();
                objsToFind.add(CLOAK);
                objsToFind.add(BOOK);
                Vector<Object> isDead_path_resultMap = findPath(map, initActorPos, objsToFind, false,
                                                                perception);
                boolean isDead = (boolean) isDead_path_resultMap.elementAt(0);
                Vector<Pair<Integer, Integer>> pathToBookOrCloak =
                        (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                map = (Map) isDead_path_resultMap.elementAt(2);

                if (isDead) {
                    // init, death
                    Instant current = Instant.now();
                    Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                    Vector<Object> result = new Vector<>();
                    result.add("A*");
                    result.add(false);
                    result.add(pathToBookOrCloak);
                    result.add(spentTimeNs);
                    return result;
                }

                Pair<Integer, Integer> lastCellPos = pathToBookOrCloak.lastElement();
                Map.Cell lastCell = map.getCell(lastCellPos);
                if (lastCell.contains(CLOAK)) {
                    // init, cloak -> try to find book
                    objsToFind = new Vector<>();
                    objsToFind.add(BOOK);
                    isDead_path_resultMap = findPath(map, lastCellPos, objsToFind, true, perception);
                    isDead = (boolean) isDead_path_resultMap.elementAt(0);
                    Vector<Pair<Integer, Integer>> pathFromCloakToBook =
                            (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                    map = (Map) isDead_path_resultMap.elementAt(2);

                    if (isDead) {
                        // init, cloak, death
                        Instant current = Instant.now();
                        Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);
                        Vector<Pair<Integer, Integer>> path = new Vector<>();
                        path.addAll(pathToBookOrCloak);
                        pathFromCloakToBook.remove(0);
                        path.addAll(pathFromCloakToBook);

                        Vector<Object> result = new Vector<>();
                        result.add("A*");
                        result.add(false);
                        result.add(path);
                        result.add(spentTimeNs);
                        return result;
                    }

                    lastCellPos = pathFromCloakToBook.lastElement();
                    // init, cloak, book -> try to find exit
                    objsToFind = new Vector<>();
                    objsToFind.add(EXIT);
                    isDead_path_resultMap = findPath(map, lastCellPos, objsToFind, true, perception);
                    isDead = (boolean) isDead_path_resultMap.elementAt(0);
                    Vector<Pair<Integer, Integer>> pathFromCloakBookToExit =
                            (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                    map = (Map) isDead_path_resultMap.elementAt(2);


                    // init, cloak, book, exit
                    Instant current = Instant.now();
                    Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);
                    Vector<Pair<Integer, Integer>> path = new Vector<>();
                    path.addAll(pathToBookOrCloak);
                    pathFromCloakToBook.remove(0);
                    path.addAll(pathFromCloakToBook);
                    pathFromCloakBookToExit.remove(0);
                    path.addAll(pathFromCloakBookToExit);

                    Vector<Object> result = new Vector<>();
                    result.add("A*");
                    result.add(!isDead && map.getCell(path.lastElement()).contains(EXIT));

                    result.add(path);
                    result.add(spentTimeNs);
                    return result;
                }

                lastCellPos = pathToBookOrCloak.lastElement();
                // init, book -> try to find exit or cloak
                objsToFind = new Vector<>();
                objsToFind.add(EXIT);
                objsToFind.add(CLOAK);
                isDead_path_resultMap = findPath(map, lastCellPos, objsToFind, false, perception);
                isDead = (boolean) isDead_path_resultMap.elementAt(0);
                Vector<Pair<Integer, Integer>> pathFromBookToExitOrCloak =
                        (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                map = (Map) isDead_path_resultMap.elementAt(2);

                lastCellPos = pathFromBookToExitOrCloak.lastElement();
                lastCell = map.getCell(lastCellPos);
                if (lastCell.contains(CLOAK)) {
                    // init, book, cloak -> try to find exit
                    objsToFind = new Vector<>();
                    objsToFind.add(EXIT);
                    isDead_path_resultMap = findPath(map, lastCellPos, objsToFind, false, perception);
                    isDead = (boolean) isDead_path_resultMap.elementAt(0);
                    Vector<Pair<Integer, Integer>> pathFromBookCloakToExit =
                            (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                    map = (Map) isDead_path_resultMap.elementAt(2);

                    // init, book, cloak, exit or nothing
                    Instant current = Instant.now();
                    Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);
                    Vector<Pair<Integer, Integer>> path = new Vector<>();
                    path.addAll(pathToBookOrCloak);
                    path.addAll(pathFromBookToExitOrCloak);
                    path.addAll(pathFromBookCloakToExit);

                    Vector<Object> result = new Vector<>();
                    result.add("A*");
                    result.add(!isDead && map.getCell(path.lastElement()).contains(EXIT));
                    result.add(path);
                    result.add(spentTimeNs);
                    return result;
                }

                // init, book, exit or nothing
                Instant current = Instant.now();
                Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);
                Vector<Pair<Integer, Integer>> path = new Vector<>();
                path.addAll(pathToBookOrCloak);
                pathFromBookToExitOrCloak.remove(0);
                path.addAll(pathFromBookToExitOrCloak);

                Vector<Object> result = new Vector<>();
                result.add("A*");
                result.add(!isDead && map.getCell(path.lastElement()).contains(EXIT));
                result.add(path);
                result.add(spentTimeNs);
                return result;
            }

            private Vector<Object> findPath(Map map, Pair<Integer, Integer> initPos, Vector<Object> objsToFind,
                                            boolean hasCloak, int perception) {
                // returns [boolean isDead, Vector<Pair<Integer, Integer>> path, Map resultMap] to first found obj from objsToFind
                // implements Dijkstra search algorithm

                Map resultMap = map.clone();
                for (int y = 0; y < resultMap.sizeY; y++) {
                    for (int x = 0; x < resultMap.sizeX; x++) {
                        resultMap.getCell(x, y).distance = Map.Cell.INFINITELY_FAR;
                    }
                }
                resultMap.getCell(initPos).distance = 0;

                // map with parent
                Vector<Vector<Pair<Integer, Integer>>> parentMap = new Vector<>();
                for (int y = 0; y < resultMap.sizeY; y++) {
                    Vector<Pair<Integer, Integer>> mapRow = new Vector<>();
                    for (int x = 0; x < resultMap.sizeX; x++) {
                        mapRow.add(null);
                    }
                    parentMap.add(mapRow);
                }

                // create a container with cells that could make further optimal path
                Vector<Pair<Integer, Integer>> activeCellsCoords = new Vector<>();
                activeCellsCoords.add(initPos);

                // try to find objsToFind
                Pair<Integer, Integer> activeCellCoords = this.popNextActiveCellCoords(activeCellsCoords, resultMap,
                                                                                       hasCloak);
                Map.Cell activeCell = resultMap.getCell(activeCellCoords);
                // interact with cell
                resultMap.visitCell(activeCellCoords, perception);
                if (isActorDead(activeCell, hasCloak)) {
                    // game over
                    Vector<Pair<Integer, Integer>> path = reconstructPath(activeCellCoords, initPos, parentMap);
                    Vector<Object> result = new Vector<>();
                    result.add(true);
                    result.add(path);
                    result.add(resultMap);
                    return result;
                }

                while (!activeCell.contains(objsToFind)) {
                    // calculate distance to neighbors and update activeCells
                    int potentialDist = activeCell.distance + stepSize;
                    for (int y = activeCellCoords.second - 1; y <= activeCellCoords.second + 1; y++) {
                        if (y >= 0 && y < resultMap.sizeY) {
                            for (int x = activeCellCoords.first - 1; x <= activeCellCoords.first + 1; x++) {
                                if (x >= 0 && x < resultMap.sizeX &&
                                        !(x == activeCellCoords.first && y == activeCellCoords.second)) {
                                    Map.Cell cell = resultMap.getCell(x, y);
                                    if (potentialDist < cell.distance) {
                                        cell.distance = potentialDist;
                                        parentMap.elementAt(y).setElementAt(activeCellCoords, x);
                                        activeCellsCoords.add(new Pair<>(x, y));
                                    }
                                }
                            }
                        }
                    }
                    // choose next cell
                    Pair<Integer, Integer> nextActiveCellCoords =
                            popNextActiveCellCoords(activeCellsCoords, resultMap, hasCloak);
                    if(nextActiveCellCoords == null) {
                        // no more steps possible
                        Vector<Pair<Integer, Integer>> path = reconstructPath(activeCellCoords, initPos, parentMap);
                        Vector<Object> result = new Vector<>();
                        result.add(false);
                        result.add(path);
                        result.add(resultMap);
                        return result;
                    }
                    activeCellCoords = nextActiveCellCoords;
                    activeCell = resultMap.getCell(activeCellCoords.first, activeCellCoords.second);
                    // interact with next cell
                    resultMap.visitCell(activeCellCoords, perception);
                    if (isActorDead(activeCell, hasCloak)) {
                        // game over
                        Vector<Pair<Integer, Integer>> path = reconstructPath(activeCellCoords, initPos, parentMap);
                        Vector<Object> result = new Vector<>();
                        result.add(true);
                        result.add(path);
                        result.add(resultMap);
                        return result;
                    }
                }

                // success
                Vector<Pair<Integer, Integer>> path = reconstructPath(activeCellCoords, initPos, parentMap);
                Vector<Object> result = new Vector<>();
                result.add(false);
                result.add(path);
                result.add(resultMap);
                return result;
            }

            private Vector<Pair<Integer, Integer>> reconstructPath(Pair<Integer, Integer> endPos,
                                                                   Pair<Integer, Integer> initPos,
                                                                   Vector<Vector<Pair<Integer, Integer>>> parentMap) {
                Vector<Pair<Integer, Integer>> reversedPath = new Vector<>();
                reversedPath.add(endPos.clone());
                Pair<Integer, Integer> currentPos = endPos;
                while (!currentPos.equals(initPos)) {
                    currentPos = parentMap.elementAt(currentPos.second).elementAt(currentPos.first);
                    reversedPath.add(currentPos.clone());
                }
                Vector<Pair<Integer, Integer>> path = new Vector<>();
                for (int i = reversedPath.size() - 1; i >= 0; i--) {
                    path.add(reversedPath.elementAt(i));
                }
                return path;
            }

            private boolean isActorDead(Map.Cell currentCell, boolean hasCloak) {
                return (currentCell.contains(FILCH) || currentCell.contains(CAT) ||
                       (!hasCloak && currentCell.isUnderPerception));
            }

            private void calculateHeuristics(Pair<Integer, Integer> goalCoords, Map map) {
                for (int y = 0; y < map.sizeY; y++) {
                    for (int x = 0; x < map.sizeX; x++) {
                        int dx = abs(x - (int) goalCoords.first);
                        int dy = abs(y - (int) goalCoords.second);
                        int h = max(dx, dy);
                        map.getCell(x, y).heuristics = h;
                    }
                }
            }

            private Pair<Integer, Integer> popNextActiveCellCoords(Vector<Pair<Integer, Integer>> activeCellsCoords,
                                                                   Map map, boolean hasCloak) {
                // return null if it is there is no active cells
                // otherwise return coordinates of active cell with min score that guaranteed to be safe, otherwise -
                // the cell that is undiscovered
                if (activeCellsCoords.size() == 0) {
                    return null;
                }
                int minSeenI = -1;
                Pair<Integer, Integer> nextSeenActiveCellCoords = null;
                int minSeenScore = Map.Cell.INFINITELY_FAR;
                int minNotSeenI = -1;
                Pair<Integer, Integer> nextNotSeenActiveCellCoords = null;
                int minNotSeenScore = Map.Cell.INFINITELY_FAR;
                for (int i = 0; i < activeCellsCoords.size(); i++) {
                    Pair<Integer, Integer> activeCellCoords = activeCellsCoords.elementAt(i);
                    Map.Cell activeCell = map.getCell(activeCellCoords.first, activeCellCoords.second);
                    if (activeCell.isSeen) {
                        if (!(activeCell.contains(FILCH) || activeCell.contains(CAT)) &&
                            !(!hasCloak && activeCell.isUnderPerception) &&
                            activeCell.score() < minSeenScore) {
                            minSeenScore = activeCell.score();
                            minSeenI = i;
                            nextSeenActiveCellCoords = activeCellCoords;
                        }
                    }
                    else {
                        if (activeCell.score() < minNotSeenScore) {
                            minNotSeenScore = activeCell.score();
                            minNotSeenI = i;
                            nextNotSeenActiveCellCoords = activeCellCoords;
                        }
                    }
                }
                if (nextSeenActiveCellCoords != null) {
                    activeCellsCoords.remove(minSeenI);
                    return nextSeenActiveCellCoords;
                }
                else if(nextNotSeenActiveCellCoords != null) {
                    activeCellsCoords.remove(minNotSeenI);
                    return nextNotSeenActiveCellCoords;
                }
                return null;
            }
        }

        public static class BacktrackingSolver implements Solver {

            @Override
            public Vector<Object> solve(Map initMap, Pair<Integer, Integer> initActorPos, Pair<Integer, Integer> exitPos,
                                        int perception) {
                // Output: [String name, boolean isSuccess, \
                // Vector<Pair<Integer, Integer>> path, Long spentTimeNs]

                Instant previous = Instant.now();

                // init map
                Map map = initMap.clone();
                map.calculateHeuristics(exitPos);

                // try to discovery book or cloak from init
                Vector<Object> objsToFind = new Vector<>();
                objsToFind.add(CLOAK);
                objsToFind.add(BOOK);
                Vector<Object> isDead_path_resultMap = findPathToObj(objsToFind, initActorPos, map,
                                                            false, perception);
                Vector<Pair<Integer, Integer>> pathToCloakOrBook =
                        (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                map = (Map) isDead_path_resultMap.elementAt(2);
                Pair<Integer, Integer> objCoords = pathToCloakOrBook.lastElement();

                if (map.getCell(objCoords).contains(CLOAK)) {
                    // find optimal path to cloak from init
                    isDead_path_resultMap = findPathToCoords(objCoords, initActorPos, map, false, perception);
                    pathToCloakOrBook = (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                    map = (Map) isDead_path_resultMap.elementAt(2);

                    // try to discovery book from init, cloak
                    objsToFind = new Vector<>();
                    objsToFind.add(BOOK);
                    isDead_path_resultMap = findPathToObj(objsToFind, objCoords, map, true, perception);
                    Vector<Pair<Integer, Integer>> pathCloakToBook =
                            (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                    map = (Map) isDead_path_resultMap.elementAt(2);
                    Pair<Integer, Integer> prevObjCoords = objCoords;
                    objCoords = pathCloakToBook.lastElement();

                    if (map.getCell(objCoords).contains(BOOK)) {
                        // find optimal path to book from init, cloak
                        isDead_path_resultMap =
                                findPathToCoords(objCoords, prevObjCoords, map, true, perception);
                        pathCloakToBook = (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                        map = (Map) isDead_path_resultMap.elementAt(2);

                        // find optimal path to exit from init, cloak, book
                        prevObjCoords = objCoords;
                        objCoords = exitPos;
                        isDead_path_resultMap =
                                findPathToCoords(objCoords, prevObjCoords, map, true, perception);
                        Vector<Pair<Integer, Integer>> pathCloakBookToExit =
                                (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);

                        if (map.getCell(pathCloakBookToExit.lastElement()).contains(EXIT)) {
                            // init, cloak, book, exit
                            Instant current = Instant.now();
                            Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                            Vector<Pair<Integer, Integer>> path = new Vector<>();
                            path.addAll(pathToCloakOrBook);
                            pathCloakToBook.remove(0);
                            path.addAll(pathCloakToBook);
                            pathCloakBookToExit.remove(0);
                            path.addAll(pathCloakBookToExit);

                            Vector<Object> result = new Vector<>();
                            result.add("Backtracking");
                            result.add(true);
                            result.add(path);
                            result.add(spentTimeNs);
                            return result;
                        }

                        // init, cloak, book, death or no way
                        Instant current = Instant.now();
                        Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                        Vector<Pair<Integer, Integer>> path = new Vector<>();
                        path.addAll(pathToCloakOrBook);
                        pathCloakToBook.remove(0);
                        path.addAll(pathCloakToBook);
                        pathCloakBookToExit.remove(0);
                        path.addAll(pathCloakBookToExit);

                        Vector<Object> result = new Vector<>();
                        result.add("Backtracking");
                        result.add(false);
                        result.add(path);
                        result.add(spentTimeNs);
                        return result;
                    }

                    // init, cloak, death or no way
                    Instant current = Instant.now();
                    Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                    Vector<Pair<Integer, Integer>> path = new Vector<>();
                    path.addAll(pathToCloakOrBook);
                    pathCloakToBook.remove(0);
                    path.addAll(pathCloakToBook);

                    Vector<Object> result = new Vector<>();
                    result.add("Backtracking");
                    result.add(false);
                    result.add(path);
                    result.add(spentTimeNs);
                    return result;
                }
                else if (map.getCell(objCoords).contains(BOOK)) {
                    // find optimal path to book from init
                    isDead_path_resultMap = findPathToCoords(objCoords, initActorPos, map, false, perception);
                    pathToCloakOrBook = (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                    map = (Map) isDead_path_resultMap.elementAt(2);

                    // try to discovery cloak or exit from book
                    objsToFind = new Vector<>();
                    objsToFind.add(CLOAK);
                    objsToFind.add(EXIT);
                    isDead_path_resultMap = findPathToObj(objsToFind, objCoords, map,
                            false, perception);
                    Vector<Pair<Integer, Integer>> pathBookToCloakOrExit =
                            (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                    map = (Map) isDead_path_resultMap.elementAt(2);
                    Pair<Integer, Integer> prevObjCoords = objCoords;
                    objCoords = pathBookToCloakOrExit.lastElement();

                    if (map.getCell(objCoords).contains(CLOAK)) {
                        // find optimal path to cloak from book
                        isDead_path_resultMap =
                                findPathToCoords(objCoords, prevObjCoords, map, false, perception);
                        pathBookToCloakOrExit =
                                (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);
                        map = (Map) isDead_path_resultMap.elementAt(2);

                        // find optimal path to exit from book, cloak
                        prevObjCoords = objCoords;
                        objCoords = exitPos;
                        isDead_path_resultMap =
                                findPathToCoords(objCoords, prevObjCoords, map, true, perception);
                        Vector<Pair<Integer, Integer>> pathBookCloakToExit =
                                (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);

                        if (map.getCell(pathBookCloakToExit.lastElement()).contains(EXIT)) {
                            // init, book, cloak, exit
                            Instant current = Instant.now();
                            Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                            Vector<Pair<Integer, Integer>> path = new Vector<>();
                            path.addAll(pathToCloakOrBook);
                            pathBookToCloakOrExit.remove(0);
                            path.addAll(pathBookToCloakOrExit);
                            pathBookCloakToExit.remove(0);
                            path.addAll(pathBookCloakToExit);

                            Vector<Object> result = new Vector<>();
                            result.add("Backtracking");
                            result.add(true);
                            result.add(path);
                            result.add(spentTimeNs);
                            return result;
                        }

                        // init, book, cloak, death or no way
                        Instant current = Instant.now();
                        Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                        Vector<Pair<Integer, Integer>> path = new Vector<>();
                        path.addAll(pathToCloakOrBook);
                        pathBookToCloakOrExit.remove(0);
                        path.addAll(pathBookToCloakOrExit);
                        pathBookCloakToExit.remove(0);
                        path.addAll(pathBookCloakToExit);

                        Vector<Object> result = new Vector<>();
                        result.add("Backtracking");
                        result.add(false);
                        result.add(path);
                        result.add(spentTimeNs);
                        return result;
                    }
                    else if (map.getCell(objCoords).contains(EXIT)) {
                        // find optimal path to exit from book
                        isDead_path_resultMap =
                                findPathToCoords(objCoords, prevObjCoords, map, false, perception);
                        pathBookToCloakOrExit =
                                (Vector<Pair<Integer, Integer>>) isDead_path_resultMap.elementAt(1);

                        // init, book, exit
                        Instant current = Instant.now();
                        Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                        Vector<Pair<Integer, Integer>> path = new Vector<>();
                        path.addAll(pathToCloakOrBook);
                        pathBookToCloakOrExit.remove(0);
                        path.addAll(pathBookToCloakOrExit);

                        Vector<Object> result = new Vector<>();
                        result.add("Backtracking");
                        result.add(true);
                        result.add(path);
                        result.add(spentTimeNs);
                        return result;
                    }

                    // init, book, death or no way
                    Instant current = Instant.now();
                    Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                    Vector<Pair<Integer, Integer>> path = new Vector<>();
                    path.addAll(pathToCloakOrBook);
                    pathBookToCloakOrExit.remove(0);
                    path.addAll(pathBookToCloakOrExit);

                    Vector<Object> result = new Vector<>();
                    result.add("Backtracking");
                    result.add(false);
                    result.add(path);
                    result.add(spentTimeNs);
                    return result;
                }

                // init, death or no way
                Instant current = Instant.now();
                Long spentTimeNs = ChronoUnit.NANOS.between(previous, current);

                Vector<Object> result = new Vector<>();
                result.add("Backtracking");
                result.add(false);
                result.add(pathToCloakOrBook);
                result.add(spentTimeNs);
                return result;
            }

            private Vector<Object> findPathToObj(Vector<Object> objsToFind, Pair<Integer, Integer> initPos, Map map,
                                                 boolean hasCloak, int perception) {
                // returns [boolean isDead, Vector<Pair<Integer, Integer>> path, Map resultMap]
                return findPathToObjOrCoords(objsToFind, null, initPos, map, hasCloak, perception,
                        false);
            }

            private Pair<Integer, Integer> chooseNextCellCoords(Pair<Integer, Integer> currentPos, Map map,
                                                                boolean hasCloak, Vector<Vector<Boolean>> isVisited,
                                                                boolean considerUnsafeCells) {
                // return safest and closest to dest unvisited cell from neighborhood, if all the neighbors are visited
                // or guaranteed lead to die, then null is returned

                Vector<Pair<Integer, Integer>> safeCellsCoords = new Vector<>();
                Vector<Pair<Integer, Integer>> unknownCellsCoords = new Vector<>();
                Vector<Pair<Integer, Integer>> unsafeCellsCoords = new Vector<>();
                for (int y = currentPos.second - 1; y <= currentPos.second + 1 && y < map.sizeY; y++) {
                    if (y >= 0) {
                        for (int x = currentPos.first - 1; x <= currentPos.first + 1 && x < map.sizeX; x++) {
                            if (x >= 0 && !(x == currentPos.first && y == currentPos.second)) {
                                if (!isVisited.elementAt(y).elementAt(x)) {
                                    Map.Cell cell = map.getCell(x, y);
                                    Pair<Integer, Integer> cellPos = new Pair<>(x, y);
                                    if (cell.isSeen) {
                                        if (cell.isUnderPerception) {
                                            if (hasCloak) {
                                                if (cell.isBorderPerception || cell.isVisited) {
                                                    safeCellsCoords.add(cellPos);
                                                }
                                                else {
                                                    unsafeCellsCoords.add(cellPos);
                                                }
                                            }
                                        }
                                        else {
                                            safeCellsCoords.add(cellPos);
                                        }
                                    }
                                    else {
                                        unknownCellsCoords.add(cellPos);
                                    }
                                }
                            }
                        }
                    }
                }
                Vector<Pair<Integer, Integer>> interestingCells = null;
                if (safeCellsCoords.size() > 0) {
                    interestingCells = safeCellsCoords;
                }
                else if (unknownCellsCoords.size() > 0) {
                    interestingCells = unknownCellsCoords;
                }
                else if (unsafeCellsCoords.size() > 0 && considerUnsafeCells) {
                    interestingCells = unsafeCellsCoords;
                }
                if (interestingCells != null) {
                    Pair<Integer, Integer> minCellPos = interestingCells.elementAt(0);
                    int minCellCost = map.getCell(minCellPos).score();
                    for (int i = 1; i < interestingCells.size(); i++) {
                        Pair<Integer, Integer> cellPos = interestingCells.elementAt(i);
                        Map.Cell cell = map.getCell(cellPos);
                        int cellCost = cell.score();
                        if (cellCost < minCellCost) {
                            minCellPos = cellPos;
                            minCellCost = cellCost;
                        }
                    }
                    return minCellPos;
                }
                return null;
            }

            private boolean isActorDead(Map.Cell currentCell, boolean hasCloak) {
                return (currentCell.contains(FILCH) || currentCell.contains(CAT) ||
                        (!hasCloak && currentCell.isUnderPerception));
            }

            private Vector<Object> findPathToCoords(Pair<Integer, Integer> destPos,
                                                                    Pair<Integer, Integer> initPos, Map map,
                                                                    boolean hasCloak, int perception) {
                // return [boolean isDead, Vector<Pair<Integer, Integer>> path, Map resultMap]
                return findPathToObjOrCoords(null, destPos, initPos, map, hasCloak, perception,
                               false);
            }

            private Vector<Object> findPathToObjOrCoords(Vector<Object> objsToFind, Pair<Integer, Integer> destPos,
                                                         Pair<Integer, Integer> initPos, Map map, boolean hasCloak,
                                                         int perception, boolean visitUnsafeCells) {
                // returns [boolean isDead, Vector<Pair<Integer, Integer>> path, Map resultMap]

                if (objsToFind == null) {
                    objsToFind = new Vector<>();
                }

                Map resultMap = map.clone();
                if (destPos != null) {
                    resultMap.calculateHeuristics(destPos);
                }
                Vector<Vector<Boolean>> isVisited = new Vector<>();
                for (int y = 0; y < resultMap.sizeY; y++) {
                    Vector<Boolean> mapRow = new Vector<>();
                    for (int x = 0; x < resultMap.sizeX; x++) {
                        mapRow.add(false);
                    }
                    isVisited.add(mapRow);
                }
                isVisited.elementAt(initPos.second).setElementAt(true, initPos.first);
                Vector<Pair<Integer, Integer>> path = new Vector<>();
                path.add(initPos);
                Map.Cell activeCell = resultMap.getCell(initPos);
                Pair<Integer, Integer> activeCellPos = path.lastElement();
                // interact with cell
                resultMap.visitCell(initPos, perception);
                if (isActorDead(activeCell, hasCloak)) {
                    // game over
                    Vector<Object> result = new Vector<>();
                    result.add(true);
                    result.add(path);
                    result.add(resultMap);
                    return result;
                }

                while (!activeCell.contains(objsToFind) && !activeCellPos.equals(destPos)) {
                    // choose next cell
                    Pair<Integer, Integer> nextActiveCellPos =
                            chooseNextCellCoords(activeCellPos, resultMap, hasCloak, isVisited, visitUnsafeCells);
                    if (nextActiveCellPos == null) {
                        // no more steps possible from active cell
                        if (path.size() > 1) {
                            path.removeElementAt(path.size() - 1);
                            activeCellPos = path.lastElement();
                            activeCell = resultMap.getCell(activeCellPos);
                            continue;
                        }
                        // no more steps are possible at all
                        if (hasCloak && !visitUnsafeCells) {
                            // try to find ignoring risks
                            return findPathToObjOrCoords(objsToFind, destPos, initPos, resultMap, hasCloak, perception,
                                    true);
                        }
                        Vector<Object> result = new Vector<>();
                        result.add(false);
                        result.add(path);
                        result.add(resultMap);
                        return result;
                    }
                    activeCellPos = nextActiveCellPos;
                    activeCell = resultMap.getCell(activeCellPos);
                    path.add(activeCellPos);
                    // interact with next cell
                    resultMap.visitCell(activeCellPos, perception);
                    isVisited.elementAt(activeCellPos.second).setElementAt(true, activeCellPos.first);
                    if (isActorDead(activeCell, hasCloak)) {
                        // game over
                        Vector<Object> result = new Vector<>();
                        result.add(true);
                        result.add(path);
                        result.add(resultMap);
                        return result;
                    }
                }

                // success
                Vector<Object> result = new Vector<>();
                result.add(false);
                result.add(path);
                result.add(resultMap);
                return result;
            }
        }

        public interface InputReader {
            Vector<Object> readInput();  // output: [Map initMap, int actorPerception, \
            //          Pair<Integer, Integer> initActorPos, \
            //          Pair<Integer, Integer> exitPos]
        }

        public static class ConsoleInputReader implements InputReader {

            @Override
            public Vector<Object> readInput() {
                Vector<Object> result = new Vector<Object>();

                Map initMap = new Map(9, 9);

                Scanner scanner = new Scanner(System.in);
                String inputStr = scanner.nextLine();
                inputStr = inputStr.replaceAll("\\[", " ");
                inputStr = inputStr.replaceAll("]", " ");
                inputStr = inputStr.replaceAll(",", " ");
                Scanner scanner1 = new Scanner(inputStr);
                int initActorX = scanner1.nextInt();
                int initActorY = scanner1.nextInt();
                int filchX = scanner1.nextInt();
                int filchY = scanner1.nextInt();
                int catX = scanner1.nextInt();
                int catY = scanner1.nextInt();
                int bookX = scanner1.nextInt();
                int bookY = scanner1.nextInt();
                int cloakX = scanner1.nextInt();
                int cloakY = scanner1.nextInt();
                int exitX = scanner1.nextInt();
                int exitY = scanner1.nextInt();
                int actorPerception = scanner.nextInt();
                scanner.close();
                scanner1.close();

                initMap.addEnemy(FILCH, 2, filchX, filchY);
                initMap.addEnemy(CAT, 1, catX, catY);
                initMap.addItem(ACTOR, initActorX, initActorY);
                initMap.addItem(BOOK, bookX, bookY);
                initMap.addItem(CLOAK, cloakX, cloakY);
                initMap.addItem(EXIT, exitX, exitY);

                result.add(initMap);
                result.add(actorPerception);
                result.add(new Pair<Integer, Integer>(initActorX, initActorY));
                result.add(new Pair<Integer, Integer>(exitX, exitY));

                return result;
            }
        }

        public static class AutogenInputReader implements InputReader {

            @Override
            public Vector<Object> readInput() {
                // TODO
                return null;
            }
        }

        private static class Map {
            Vector<Vector<Cell>> map;
            public int sizeX, sizeY;

            public Map(int sizeY, int sizeX) {
                this.sizeX = sizeX;
                this.sizeY = sizeY;
                this.map = new Vector<>();
                for (int y = 0; y < sizeY; y++) {
                    Vector<Cell> mapRow = new Vector<>();
                    for (int x = 0; x < sizeX; x++) {
                        mapRow.add(new Cell());
                    }
                    this.map.add(mapRow);
                }
            }

            public void calculateHeuristics(Pair<Integer, Integer> goalCoords) {
                for (int y = 0; y < sizeY; y++) {
                    for (int x = 0; x < sizeX; x++) {
                        int dx = abs(x - goalCoords.first);
                        int dy = abs(y - goalCoords.second);
                        getCell(x, y).heuristics = max(dx, dy);
                    }
                }
            }

            public void addEnemy(Object enemy, int perception, int x, int y) {
                this.addItem(enemy, x, y);
                for (int y_ = y - perception; y_ <= y + perception; y_++) {
                    if (y_ >= 0 && y_ < this.sizeY) {
                        for (int x_ = x - perception; x_ <= x + perception; x_++) {
                            if (x_ >= 0 && x_ < this.sizeX) {
                                this.getCell(x_, y_).isUnderPerception = true;
                            }
                        }
                    }
                }
            }

            public void addItem(Object item, int x, int y) {
                this.getCell(x, y).container.add(item);
            }

            public Cell getCell(int x, int y) {
                return this.map.elementAt(y).elementAt(x);
            }

            public Cell getCell(Pair<Integer, Integer> pos) {
                return getCell(pos.first, pos.second);
            }

            public Map clone() {
                Map mapCopy = new Map(sizeY, sizeX);
                for (int y = 0; y < sizeY; y++) {
                    Vector<Cell> mapRow = this.map.elementAt(y);
                    for (int x = 0; x < sizeX; x++) {
                        Cell cell = mapRow.elementAt(x);
                        Cell cellCopy = (Cell) cell.clone();
                        mapCopy.map.elementAt(y).setElementAt(cellCopy, x);
                    }
                }
                return mapCopy;
            }

            public void visitCell(int x, int y, int perception) {
                if (perception == 1) {
                    for (int y_ = y - 1; y_ <= y + 1 && y_ < this.sizeY; y_++) {
                        if (y_ >= 0) {
                            for (int x_ = x - 1; x_ <= x + 1 && x_ < this.sizeX; x_++) {
                                if (x_ >= 0) {
                                    makeCellSeen(x_, y_);
                                }
                            }
                        }
                    }
                }
                else if (perception == 2) {
                    for (int i = -1; i <= 1; i++) {
                        int x_ = x + i;
                        if (x_ >= 0 && x_ < this.sizeX) {
                            int y_ = y - 2;
                            if (y_ >= 0 && y_ < this.sizeY) {
                                makeCellSeen(x_, y_);
                            }
                            y_ = y + 2;
                            if (y_ >= 0 && y_ < this.sizeY) {
                                makeCellSeen(x_, y_);
                            }
                        }

                        int y_ = y + i;
                        if (y_ >= 0 && y_ < this.sizeY) {
                            x_ = x - 2;
                            if (x_ >= 0 && x_ < this.sizeX) {
                                makeCellSeen(x_, y_);
                            }
                            x_ = x + 2;
                            if (x_ >= 0 && x_ < this.sizeX) {
                                makeCellSeen(x_, y_);
                            }
                        }
                    }
                    makeCellSeen(x, y);
                }
                this.getCell(x, y).isVisited = true;
            }

            private void makeCellSeen(int x, int y) {
                Cell cell = getCell(x, y);
                cell.isSeen = true;
                if (cell.isUnderPerception) {
                    for (int y_ = y - 1; y_ <= y + 1 && y_ < sizeY; y_++) {
                        if (y_ >= 0) {
                            for (int x_ = x - 1; x_ <= x + 1 && x_ < sizeX; x_++) {
                                if (x_ >= 0 && !(x_ == x && y_ == y)) {
                                    Cell neighbor = getCell(x_, y_);
                                    if (neighbor.isSeen && !neighbor.isUnderPerception) {
                                        cell.isBorderPerception = true;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            public void visitCell(Pair<Integer, Integer> pos, int perception) {
                visitCell(pos.first, pos.second, perception);
            }

            private static class Cell {
                public static int INFINITELY_FAR = 9999;
                public boolean isUnderPerception = false;
                public boolean isSeen = false;
                public boolean isBorderPerception = false;
                public boolean isVisited = false;
                public int heuristics = 0;
                public int distance = INFINITELY_FAR;
                public Vector<Object> container;

                public Cell() {
                    this.container = new Vector<>();
                }

                public Cell clone() {
                    Cell cellCopy = new Cell();
                    cellCopy.isUnderPerception = this.isUnderPerception;
                    cellCopy.container.addAll(this.container);
                    cellCopy.isBorderPerception = this.isBorderPerception;
                    cellCopy.heuristics = this.heuristics;
                    cellCopy.distance = this.distance;
                    cellCopy.isSeen = this.isSeen;
                    return cellCopy;
                }

                public boolean contains(Object obj) {
                    for (Object item: this.container) {
                        if (item.equals(obj)) {
                            return true;
                        }
                    }
                    return false;
                }

                public boolean contains(Vector<Object> objs) {
                    // return if cell contains at least one object from objs
                    for (Object obj: objs) {
                        if (contains(obj)) {
                            return true;
                        }
                    }
                    return false;
                }

                public int score() {
                    return this.heuristics + this.distance;
                }
            }
        }
    }

    private static class Pair<T1, T2> {
        public T1 first;
        public T2 second;
        public Pair() {}
        public Pair(T1 first_, T2 second_) {
            this.first = first_;
            this.second = second_;
        }

        public Pair<T1, T2> clone() {
            Pair<T1, T2> clone = new Pair<>(this.first, this.second);
            return clone;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }
    }
}