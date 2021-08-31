package preprocessing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UpwardCotopy {

    private final String conceptURI;
    private final Set<String> ancestorsSet;


    public UpwardCotopy(String conceptURI, List<String> ancestors) {
        this.conceptURI = conceptURI;
        this.ancestorsSet = new HashSet<>(ancestors);;
    }

    public String getConceptURI() {
        return conceptURI;
    }

    public Set<String> computeIntersection(UpwardCotopy other) {
        Set<String> result;
        if (this.ancestorsSet.size() < other.ancestorsSet.size()) {
            result = this.ancestorsSet.stream()
                    .filter(other.ancestorsSet::contains)
                    .collect(Collectors.toSet());
        }
        else {
            result = other.ancestorsSet.stream()
                    .filter(this.ancestorsSet::contains)
                    .collect(Collectors.toSet());
        }
        return result;
    }


    public double computeTaxonomySimilarity(UpwardCotopy other) {

        if (this.conceptURI.equals(other.conceptURI)) {
            return 1;
        }

        int intersectionSize = this.computeIntersection(other).size();
        HashSet<String> tmp= new HashSet<>(this.ancestorsSet);
        tmp.addAll(other.ancestorsSet);
        int unionSize = tmp.size();

        double conceptMatch = intersectionSize * 1.0 / unionSize;
        return conceptMatch / 2;
    }


    @Override
    public String toString() {
        return conceptURI + "(" + ancestorsSet.stream().collect(Collectors.joining(" -- ")) + ")";
    }

}
