package preprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceMatrix {

    private final Map<String, Map<String, Double>> matrix;

    public DistanceMatrix(List<UpwardCotopy> ucList) {
        int i, j;
        double taxonomySimilarity;
        matrix = new HashMap<>();

        for (i = 0; i < ucList.size(); i++) {
            String conceptURI1 = ucList.get(i).getConceptURI();
            matrix.put(conceptURI1, new HashMap<>());
            for (j = 0; j < ucList.size(); j++) {
                String conceptURI2 = ucList.get(j).getConceptURI();
                taxonomySimilarity = ucList.get(i).computeTaxonomySimilarity(ucList.get(j));
                matrix.get(conceptURI1).put(conceptURI2, taxonomySimilarity);
            }
        }
    }


    public double getDistance(String concept1, String concept2) {
        Map<String, Double> concepts = matrix.get(concept1);
        if (concepts == null) {
            return matrix.get(concept2).getOrDefault(concept1, -1.0);
        }
        return concepts.getOrDefault(concept2, -1.0);
    }

}
