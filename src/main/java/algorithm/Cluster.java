package algorithm;

import org.apache.jena.rdf.model.Model;
import query.QueryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cluster {

    private final List<SensitiveAttribute> attributes;
    private String concept;


    public Cluster(SensitiveAttribute attribute, String concept) {
        this.attributes = new ArrayList<>();
        this.attributes.add(attribute);
        this.concept = concept;
    }


    public String getConcept() {
        return concept;
    }

    public List<SensitiveAttribute> getSensitiveAttributes() {
        return Collections.unmodifiableList(attributes);
    }


    public void fusion(Cluster other, Model model) {
        String newSuperConcept = QueryUtil.getLeastCommonSubsumer(this.concept, other.concept, model);
        this.concept = newSuperConcept;
        this.attributes.addAll(other.attributes);
    }


    public int size() {
        return attributes.size();
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append("Concept : ").append(concept).append("\n");
        attributes.forEach(a -> sb.append(a.toString()).append("\n"));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cluster)) {
            return false;
        }
        Cluster c = (Cluster) o;
        return this.concept.equals(c.concept) && this.attributes.equals(c.attributes) ;
    }
}
