import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranspositionTable implements Serializable {
    private final Map<String, List<Integer>> table = new HashMap<>();

    public List<Integer> get(String fen) {
        return table.get(fen);
    }

    public void put(String fen, int score, int depth) {
        if (table.containsKey(fen)) {
            Integer d = table.get(fen).get(1);
            if (depth > d) {
                table.get(fen).set(depth, score);
            }
        } else {
            List<Integer> list = new ArrayList<>();
            list.add(score);
            list.add(depth);
            table.put(fen, list);
        }
    }

    public boolean contains(String fen) {
        return table.containsKey(fen);
    }
}