import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Grammar {
    private final String ELEMENT_SEPARATOR = "&";
    private final String SEPARATOR_OR_TRANSITION = "\\|";
    private final String TRANSITION_CONCATENATION = " ";
    private final String EPSILON = "EPS";
    private final String SEPARATOR_LEFT_RIGHT_HAND_SIDE = "->";

    // LR(0)
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private Map<List<String>, Set<List<String>>> productions;
    private String startingSymbol;
    private boolean isCFG;


    /**
     * With this method we first:
     * -> Split the production by the left right hand side separator ("->")
     * -> Then we split the left hand side by space (we can have A B -> something)
     * -> Then we split the right hand side by "|".
     * -> We go through each production from the right hand side and format each of the in order to be added to the map
     * @param production -> represents the production we are about to work
     */
    private void processProduction(String production) {
        String[] leftAndRightHandSide = production.split(this.SEPARATOR_LEFT_RIGHT_HAND_SIDE);
        List<String> splitLHS = List.of(leftAndRightHandSide[0].split(this.TRANSITION_CONCATENATION));
        String[] splitRHS = leftAndRightHandSide[1].split(this.SEPARATOR_OR_TRANSITION);

        this.productions.putIfAbsent(splitLHS, new HashSet<>());
        for (int i = 0; i < splitRHS.length; i++) {
            this.productions.get(splitLHS).add(Arrays.stream(splitRHS[i].split(this.TRANSITION_CONCATENATION)).collect(Collectors.toList()));
        }
    }

    /**
     * With this method we load the content from the file (we read every line from the file and
     * classify everything we read as a terminal/non-terminal/production and so on).
     * @param filePath - the path where the file we are reading from is
     */
    private void loadFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            this.nonTerminals = new HashSet<>(List.of(scanner.nextLine().split(this.ELEMENT_SEPARATOR)));
            this.terminals = new HashSet<>(List.of(scanner.nextLine().split(this.ELEMENT_SEPARATOR)));
            this.startingSymbol = scanner.nextLine();

            this.productions = new HashMap<>();
            while (scanner.hasNextLine()) {
                this.processProduction(scanner.nextLine());
            }

            this.isCFG = this.checkIfCFG();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * With this method we check if a grammar is a context free grammar
     * -> First we check if the starting symbols is found within the non-terminals
     * -> Second we check if on the left hand side we have only one non-terminal (for each production)
     * -> Third we check if the productions of that left hand side non-terminal can be found within the non-terminals set or terminals set or is equal to the empty sequence
     * @return true if the grammar is a CFG, false otherwise
     */
    private boolean checkIfCFG() {
        if (!this.nonTerminals.contains(this.startingSymbol)) {
            return false;
        }

        for (List<String> leftHandSide : this.productions.keySet()) {
            // On the left hand side we need to have only one element (A -> a, not AB -> a, where A and B are different non-terminals)
            if (leftHandSide.size() != 1 || !this.nonTerminals.contains(leftHandSide.get(0))) {
                return false;
            }

            for (List<String> possibleNextMoves : this.productions.get(leftHandSide)) {
                for (String possibleNextMove : possibleNextMoves) {
                    if (!this.nonTerminals.contains(possibleNextMove) && !this.terminals.contains(possibleNextMove) && !possibleNextMove.equals(this.EPSILON)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public Grammar(String filePath) {
        this.loadFromFile(filePath);
    }

    public Set<String> getNonTerminals() {
        return this.nonTerminals;
    }

    public Set<String> getTerminals() {
        return this.terminals;
    }

    public Map<List<String>, Set<List<String>>> getProductions() {
        return this.productions;
    }

    public String getStartingSymbol() {
        return this.startingSymbol;
    }

    public boolean isCFG() {
        return this.isCFG;
    }

}
