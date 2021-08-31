
package algorithm;

public class SensitiveAttribute implements Comparable<SensitiveAttribute>{

    private final String value;
    private final int cardinality;


    public SensitiveAttribute(String value, int cardinality) {
        this.value =value;
        this.cardinality = cardinality;
    }


    public String getValue() {
        return value;
    }

    public int getCardinality() {
        return cardinality;
    }


    @Override
    public String toString() {
        return "(" + value + " -> " + cardinality + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SensitiveAttribute)) {
            return false;
        }
        SensitiveAttribute sa = (SensitiveAttribute) o;
        return this.cardinality == sa.cardinality && this.value.equals(sa.value) ;
    }


    @Override
    public int compareTo(SensitiveAttribute sensitiveAttribute) {
        return this.value.compareTo(sensitiveAttribute.value);
    }
}
