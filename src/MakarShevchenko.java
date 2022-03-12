import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class MakarShevchenko {
    public static void main(String[] args) {
        try {
            BookFinding solver = new BookFinding(new BookFinding.AStarSolver(1));
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

        static String FITCH = "fitch";
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
            this.solver.solve(this.initMap, new Pair<Integer, Integer>(initActorX, initActorY),
                    new Pair<Integer, Integer>(exitX, exitY), actorPerception);
        }

        public void printOutput() {
            Vector<Object> outputValues = this.solver.getOutputValues();
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
                    else if (cell.contains(FITCH)) {
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
                        if (item.equals(CAT) || item.equals(FITCH)) {
                            hasInspector = true;
                        }
                        if (item.equals(BOOK)) {
                            hasBook = true;
                        }
                        if (item.equals(CLOAK)) {
                            hasCloak = true;
                        }
                    }
                    if (hasExit && (hasInspector || hasBook)) {
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

        private static class ActorAttributes {
            boolean hasBook = false;
            boolean hasCloak = false;
            boolean isAlive = true;
            boolean reachedExit = false;

            private void activateCell(Map.Cell cell) {
                if (cell.contains(BOOK)) {
                    this.hasBook = true;
                }
                if (cell.contains(CLOAK)) {
                    this.hasCloak = true;
                }
                if (!hasCloak && cell.isUnderPerception || cell.contains(FITCH) || cell.contains(CAT)) {
                    this.isAlive = false;
                }
                if (cell.contains(EXIT)) {
                    this.reachedExit = true;
                }
            }
        }

        public interface Solver {
            void solve(Map map, Pair<Integer, Integer> initActorPos, Pair<Integer, Integer> exitPos, int perception);
            Vector<Object> getOutputValues();  // Output: [String name, boolean isSuccess, \
                                               // Vector<Pair<Integer, Integer>> path, Long spentTimeNs]
        }

        public static class AStarSolver implements Solver {
            private int stepSize;
            private Map map;
            private ActorAttributes actorAttributes;
            private Long spentTimeNs;
            private Vector<Pair<Integer, Integer>> path = new Vector<>();
            private Vector<Vector<Pair<Integer, Integer>>> parentCell;

            public AStarSolver(int stepSize) {
                this.stepSize = stepSize;
            }

            @Override
            public void solve(Map initMap, Pair<Integer, Integer> initActorPos, Pair<Integer, Integer> exitPos,
                              int perception) {
                Instant previous = Instant.now();
                this.map = initMap.clone();
                this.parentCell = new Vector<>();
                for (int y = 0; y < this.map.sizeY; y++) {
                    Vector<Pair<Integer, Integer>> mapRow = new Vector<>();
                    for (int x = 0; x < this.map.sizeX; x++) {
                        mapRow.add(new Pair<Integer, Integer>());
                    }
                    this.parentCell.add(mapRow);
                }
                this.calculateHeuristics(exitPos);
                Pair<Integer, Integer> actorCoords = initActorPos;
                this.path.add(actorCoords);

                this.actorAttributes = new ActorAttributes();
                Map.Cell initCell = this.map.getCell(actorCoords.first, actorCoords.second);
                initCell.distance = 0;
                this.actorAttributes.activateCell(initCell);
                this.map.visitCell(actorCoords.first, actorCoords.second, perception);

                while (this.actorAttributes.isAlive && !(this.actorAttributes.hasBook && this.actorAttributes.reachedExit)) {
                    calculateDistanceToNeighbors(actorCoords);
                    Pair<Integer, Integer> nextCoords = chooseNextStep(actorAttributes.hasCloak);
                    if (nextCoords == null) {
                        this.actorAttributes.isAlive = false;
                        break;
                    }
                    actorCoords = nextCoords;
                    Map.Cell nextCell = this.map.getCell((int) actorCoords.first, (int) actorCoords.second);
                    this.actorAttributes.activateCell(nextCell);
                    this.map.visitCell(actorCoords.first, actorCoords.second, perception);
                    path.add(actorCoords);
                }

                Instant current = Instant.now();
                this.spentTimeNs = ChronoUnit.NANOS.between(previous, current);
            }

            @Override
            public Vector<Object> getOutputValues() {
                Vector<Object> result = new Vector<Object>();
                result.add("A*");
                result.add(this.actorAttributes.isAlive);
                result.add(this.path);
                result.add(this.spentTimeNs);
                return result;
            }

            private void calculateDistanceToNeighbors(Pair<Integer, Integer> currentPosCoords) {
                Map.Cell currentCell = map.getCell((int) currentPosCoords.first, (int) currentPosCoords.second);
                int potentialDist = currentCell.distance + stepSize;
                for (int y = (int) currentPosCoords.second - 1; y <= (int) currentPosCoords.second + 1; y++) {
                    if (y >= 0 && y < map.sizeY) {
                        for (int x = (int) currentPosCoords.first - 1; x <= (int) currentPosCoords.first + 1; x++) {
                            if (x >= 0 && x < map.sizeX &&
                                    !(x == (int) currentPosCoords.first && y == (int) currentPosCoords.second)) {
                                Map.Cell cell = map.getCell(x, y);
                                if (potentialDist < cell.distance) {
                                    cell.distance = potentialDist;
                                    this.parentCell.elementAt(y).setElementAt(currentPosCoords, x);
                                }
                            }
                        }
                    }
                }
            }

            private void calculateHeuristics(Pair<Integer, Integer> goalCoords) {
                for (int y = 0; y < map.sizeY; y++) {
                    for (int x = 0; x < map.sizeX; x++) {
                        int dx = abs(x - (int) goalCoords.first);
                        int dy = abs(y - (int) goalCoords.second);
                        int h = max(dx, dy);
                        map.getCell(x, y).heuristics = h;
                    }
                }
            }

            private Pair<Integer, Integer> chooseNextStep(boolean hasCloak) {
                int minScore = Map.Cell.INFINITELY_FAR;
                int minX = -1;
                int minY = -1;
                for (int y = 0; y < map.sizeY; y++) {
                    for (int x = 0; x < map.sizeX; x++) {
                        Map.Cell cell = map.getCell(x, y);
                        if (!cell.isVisited && cell.isPossibleToVisit &&
                            !(cell.isSeen && (cell.isUnderPerception && !hasCloak ||
                                              cell.contains(FITCH) || cell.contains(CAT))) &&
                            cell.score() < minScore) {
                            minScore = cell.score();
                            minX = x;
                            minY = y;
                        }
                    }
                }
                if (minScore < Map.Cell.INFINITELY_FAR) {
                    return new Pair<Integer, Integer>(minX, minY);
                }
                return null;
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

                initMap.addEnemy(FITCH, 2, filchX, filchY);
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
                this.getCell(x, y).isVisited = true;
                for (int y_ = y - 1; y_ <= y + 1; y_++) {
                    if (y_ >= 0 && y_ < this.sizeY) {
                        for (int x_ = x - 1; x_ <= x + 1; x_++) {
                            if (x_ >= 0 && x_ < this.sizeX) {
                                Cell cell = this.getCell(x_, y_);
                                cell.isPossibleToVisit = true;
                                if (!(x == x_ && y == y_) && perception == 1) {
                                    cell.isSeen = true;
                                }
                            }
                        }
                    }
                }
                if (perception == 2) {
                    for (int i = -1; i <= 1; i++) {
                        int x_ = x + i;
                        if (x_ >= 0 && x_ < this.sizeX) {
                            int y_ = y - 2;
                            if (y_ >= 0 && y_ < this.sizeY) {
                                this.getCell(x_, y_).isSeen = true;
                            }
                            y_ = y + 2;
                            if (y_ >= 0 && y_ < this.sizeY) {
                                this.getCell(x_, y_).isSeen = true;
                            }
                        }

                        int y_ = y + i;
                        if (y_ >= 0 && y_ < this.sizeY) {
                            x_ = x - 2;
                            if (x_ >= 0 && x_ < this.sizeX) {
                                this.getCell(x_, y_).isSeen = true;
                            }
                            x_ = x + 2;
                            if (x_ >= 0 && x_ < this.sizeX) {
                                this.getCell(x_, y_).isSeen = true;
                            }
                        }
                    }
                }
            }

            private static class Cell {
                public static int INFINITELY_FAR = 9999;
                public boolean isUnderPerception = false;
                public boolean isVisited = false;
                public boolean isPossibleToVisit = false;
                public boolean isSeen = false;
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
                    cellCopy.isVisited = this.isVisited;
                    cellCopy.heuristics = this.heuristics;
                    cellCopy.distance = this.distance;
                    cellCopy.isPossibleToVisit = this.isPossibleToVisit;
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
    }
}