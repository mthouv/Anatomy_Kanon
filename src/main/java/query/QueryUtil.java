package query;

import algorithm.SensitiveAttribute;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import preprocessing.UpwardCotopy;
import util.Counter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class QueryUtil {

    private static String allConceptsQueryString =
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "SELECT DISTINCT ?type " +
                    "WHERE {" +
                    //"?s a ?type ." +
                    "{?type a rdfs:Class .}" +
                    "UNION {?type a owl:Class .}" +
                    "}";


    public static String createInitialClustersQuery(String predicate, String privacyClause) {
        return
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT ?super ?attribute (COUNT(?attribute) as ?count) " +
                        "WHERE { " +
                        "?s " + predicate + " ?attribute ." +
                        privacyClause +
                        getMostSpecificClause() +
                        "} GROUP BY ?super ?attribute";
    }

    private static String getMostSpecificClause() {
        return  "?attribute a ?super ." +
                "FILTER NOT EXISTS{" +
                "?moreSpecific rdfs:subClassOf ?super ." +
                "FILTER(?moreSpecific != ?super) " +
                "?attribute a ?moreSpecific ." +
                "}";
    }

    public static String retrieveSensitiveAttributesRequest(String predicate, String privacyClause) {
        return
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT ?attribute (COUNT(?attribute) as ?count) " +
                        "WHERE { " +
                        "?s " + predicate + " ?attribute ." +
                        privacyClause +
                        "} GROUP BY ?attribute";
    }


    public static String getClusterizationUpdateRequest(SensitiveAttribute sa, String concept, String predicate, String privacyClause, long groupId, long attributeId) {
        String groupStr = "<http://Group" + groupId + ">";
        String attributeStr = "<http://Attribute" + groupId + "-" + attributeId + ">";

        return
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "DELETE {?s " + predicate + " <" + sa.getValue() + ">.}\n" +
                        "INSERT { \n" +
                        "\t?s <http://inGroup> " + groupStr + ".\n\t" +
                        groupStr + " rdf:type <" +  concept + ">.\n\t" +
                        groupStr + " " + predicate + " " + attributeStr  + ".\n\t" +
                        attributeStr + " <http://value>  <" + sa.getValue() + ">.\n\t" +
                        attributeStr + " <http://cardinality>  " + sa.getCardinality() +
                        ".}\n" +
                        "WHERE {?s " + predicate + " <" + sa.getValue() + ">. \n" + privacyClause + " }";
    }


    public static String getUpdateStringNoTaxonomy(SensitiveAttribute sa, String predicate, String privacyClause, long groupId, long attributeId) {
        String groupStr = "<http://Group" + groupId + ">";
        String attributeStr = "<http://Attribute" + groupId + "-" + attributeId + ">";

        return
                "DELETE {?s " + predicate + " <" + sa.getValue() + ">.}\n" +
                        "INSERT { \n" +
                        "\t?s <http://inGroup> " + groupStr + ".\n\t" +
                        groupStr + " " + predicate + " " + attributeStr  + ".\n\t" +
                        attributeStr + " <http://value>  <" + sa.getValue() + ">.\n\t" +
                        attributeStr + " <http://cardinality>  " + sa.getCardinality() +
                        ".}\n" +
                        "WHERE {?s " + predicate + " <" + sa.getValue() + ">. \n" + privacyClause + " }";
    }

    public static List<String> getAllConcepts(Model model) {
        Query query = QueryFactory.create(allConceptsQueryString);
        List<QuerySolution> querySolutions = execQuery(query, model);
        return querySolutions.stream()
                .map(qs -> qs.get("type").toString())
                .collect(Collectors.toList());
    }


    public static List<UpwardCotopy> getUpwardCotopiesFromConcepts(List<String> concepts, Model model) {
        return concepts.stream()
                .map(concept -> {
                    List<String> ancestors = getAncestors(concept, model);
                    return new UpwardCotopy(concept, ancestors);
                })
                .collect(Collectors.toList());
    }


    public static List<String> getAncestors(String conceptURI, Model model) {
        String ancestorsQueryStr =
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "SELECT ?super " +
                        "WHERE {" +
                        "<" + conceptURI + ">" + " rdfs:subClassOf* ?super ." +
                        "FILTER(?super != owl:Thing) ." +
                        "FILTER(?super != rdfs:Resource) ." +
                        "}";

        Query query = QueryFactory.create(ancestorsQueryStr);
        List<QuerySolution> querySolutions = execQuery(query, model);
        return querySolutions.stream()
                .map(qs -> qs.get("super").toString())
                .collect(Collectors.toList());
    }


    public static List<String> getSubProperties(String predicate, Model model) {
        String queryStr =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT * WHERE {" +
                        "?x rdfs:subPropertyOf* " + predicate + " ." +
                        "}";

        Query query = QueryFactory.create(queryStr);
        List<QuerySolution> querySolutions = execQuery(query, model);
        return querySolutions.stream().map(sol -> "<" + sol.get("x").toString() + ">").collect(Collectors.toList());
    }


    public static List<String> getSubConcepts(String concept, Model model) {
        String queryStr =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "SELECT * WHERE {" +
                        "?x rdfs:subClassOf* <" + concept + "> ." +
                        "}";

        Query query = QueryFactory.create(queryStr);
        List<QuerySolution> querySolutions = execQuery(query, model);
        return querySolutions.stream().map(sol -> "<" + sol.get("x").toString() + ">").collect(Collectors.toList());
    }


    public static String createFilterTypeClause(List<String> concepts) {
        StringBuilder sb = new StringBuilder("FILTER (");
        String clause = concepts.stream().map(c -> "?type  = " + c).collect(Collectors.joining(" || "));
        sb.append(clause);
        sb.append(")");
        return sb.toString();
    }


    public static String getLeastCommonSubsumer(String concept1, String concept2, Model model) {
        String lcsQueryStr =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                        "SELECT ?super "+
                        "WHERE { "+
                        "<" + concept1 + ">" + " rdfs:subClassOf* ?super . "+
                        "<" + concept2 + ">" + " rdfs:subClassOf* ?super . " +
                        "FILTER NOT EXISTS {"+
                        "?moreSpecificClass rdfs:subClassOf ?super ."+
                        "<" + concept1 + ">" + " rdfs:subClassOf* ?moreSpecificClass ."+
                        "<" + concept2 + ">" + " rdfs:subClassOf* ?moreSpecificClass ."+
                        "}" +
                        "FILTER(?super != owl:Thing)" +
                        "}";

        Query query = QueryFactory.create(lcsQueryStr);
        List<QuerySolution> querySolutions = execQuery(query, model);

        if (querySolutions.isEmpty()) {
            return null;
        }
        return querySolutions.get(0).get("super").toString();
    }


    public static List<String> extractTriplesFromQuery(Query query, Model model) {
        List<String> res = new ArrayList<>();
        ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {
            @Override
            public void visit(ElementFilter el) {
                res.add(el.toString());
            }

            @Override
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> triples = el.patternElts();
                while (triples.hasNext()) {
                    TriplePath tp = triples.next();

                    if (tp.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                        res.add("?s a ?type .");
                        List<String> subConcepts = getSubConcepts(tp.getObject().toString(), model);
                        String filterType = createFilterTypeClause(subConcepts);
                        res.add(filterType);
                    }
                    else {
                        res.add(tp.getSubject().toString() + " <" + tp.getPredicate().toString() + "> " + tp.getObject().toString());
                    }
                }
            }
        });
        return res;
    }


    public static int getNumberGroups(Model model) {
        Query q = QueryFactory.create("select (COUNT(distinct ?o) as ?c) where { ?s <http://inGroup> ?o .}");
        List<QuerySolution> sols = execQuery(q, model);

        int n = Integer.parseInt(sols.get(0).get("c").toString().split("\\^\\^")[0]);
        return n;
    }


    private static String fullProfessorsQueryString =
            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?s " +
                    "WHERE {" +
                    "?s  a  ub:FullProfessor." +
                    "}";


    private static String associateProfessorsQueryString =
            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?s " +
                    "WHERE {" +
                    "?s  a  ub:AssociateProfessor." +
                    "}";

    private static String assistantProfessorsQueryString =
            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?s " +
                    "WHERE {" +
                    "?s  a  ub:AssistantProfessor." +
                    "}";


    public static List<QuerySolution> execQuery(Query query, Model model) {
        List<QuerySolution> solutionsList = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            for (; results.hasNext(); ) {
                QuerySolution soln = results.nextSolution();
                solutionsList.add(soln);
            }
        }
        return solutionsList;
    }


    public static List<String> extractFullProfessorsFromGraph(Model model) {
        Query query = QueryFactory.create(fullProfessorsQueryString);
        List<QuerySolution> querySolutions = execQuery(query, model);
        return querySolutions.stream()
                .map(qs -> qs.get("s").toString())
                .collect(Collectors.toList());
    }


    public static List<String> extractAssistantProfessorsFromGraph(Model model) {
        Query query = QueryFactory.create(assistantProfessorsQueryString);
        List<QuerySolution> querySolutions = execQuery(query, model);
        return querySolutions.stream()
                .map(qs -> qs.get("s").toString())
                .collect(Collectors.toList());
    }


    public static List<String> extractAssociateProfessorsFromGraph(Model model) {
        Query query = QueryFactory.create(associateProfessorsQueryString);
        List<QuerySolution> querySolutions = execQuery(query, model);
        return querySolutions.stream()
                .map(qs -> qs.get("s").toString())
                .collect(Collectors.toList());
    }


    public static List<String> retrieveGroupsURI(Model model) {
        List<String> res = new ArrayList<>();
        Query q = QueryFactory.create("select distinct ?o where { ?s <http://inGroup> ?o .}");
        List<QuerySolution> sols = execQuery(q, model);
        sols.forEach(s -> res.add(s.get("o").toString()));
        return res;
    }


    public static String createSelectClause(List<String> predicates) {
        StringBuilder selectClause = new StringBuilder("SELECT ?s ");
        int i;
        for (i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            selectClause.append(attributeVariable).append(" ");
        }
        selectClause.append("\n");
        return selectClause.toString();
    }

    public static String createWhereClause(List<String> predicates) {
        StringBuilder whereClause = new StringBuilder("WHERE { ");
        for (int i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            whereClause.append("?s ").append(predicates.get(i)).append(" ").append(attributeVariable).append(" . ");
        }
        return whereClause.toString();
    }


    public static String createQuery(List<String> predicates, String groupURI) {
        String selectClause = createSelectClause(predicates);
        StringBuilder whereClause = new StringBuilder(createWhereClause(predicates));
        if (groupURI != null) {
            whereClause.append(" ?s <http://inGroup> <").append(groupURI).append("> . ");
        }
        whereClause.append("} \n");
        return selectClause + whereClause.toString();
    }



    public static List<List<String>> retrieveResultsFromQuery(List<String> predicates, String groupURI, Model model) {
        Query q = QueryFactory.create(createQuery(predicates, groupURI));
        //System.out.println(createQuery(predicates, groupURI));
        List<QuerySolution> sols = execQuery(q, model);

        List<List<String>> results = sols.stream().map(qs -> {
            List<String> lst = new ArrayList<>();
            lst.add(qs.get("s").toString());
            for (int i = 0; i < predicates.size(); i++) {
                String attributeVariable = "attr" + i;
                String[] tab = qs.get(attributeVariable).toString().split("\\^\\^");
                lst.add(tab[0]);
            }
            return lst;
        }).collect(Collectors.toList());

        return results;
    }

    public static void flushResultsToCsv(List<List<String>> results, List<String> columnNames, String filepath) throws IOException {
        Path p = Paths.get(filepath);
        StringBuilder sb = new StringBuilder();
        String columnNamesLine = columnNames.stream().collect(Collectors.joining(";", "", "\n"));
        sb.append(columnNamesLine);
        for (List<String> l : results) {
            String line = l.stream().collect(Collectors.joining(";", "", "\n"));
            sb.append(line);
        }
        Files.write(p, sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }


/*
    public static List<String> execQueryCsvFormat(List<String> predicates, List<String> columnNames, Model model) {
        Query q = QueryFactory.create(createQuery(predicates));
        List<QuerySolution> sols = execQuery(q, model);

        List<String> lines = sols.stream().map(qs -> {
            StringBuilder sb = new StringBuilder();
            sb.append(qs.get("s").toString()).append(";");
            int i;
            for (i = 0; i < predicates.size() - 1; i++) {
                String attributeVariable = "attr" + i;
                String[] tab = qs.get(attributeVariable).toString().split("\\^\\^");
                sb.append(tab[0]).append(";");
            }
            String attributeVariable = "attr" + i;
            String[] tab = qs.get(attributeVariable).toString().split("\\^\\^");
            sb.append(tab[0]);
            return sb.toString();
        }).collect(Collectors.toList());

        List<String> res = new ArrayList<>();
        String firstLine = columnNames.stream().collect(Collectors.joining(";"));
        res.add(firstLine);
        res.addAll(lines);
        return res;
    }
*/


    public static String createOriginalTriplesString(List<String> originalData, List<String> predicates, List<Boolean> isNumerical) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < predicates.size(); i++) {
            sb.append("<").append(originalData.get(0)).append("> ").append(predicates.get(i)).append(" ?attr").append(i).append(".\n");
            /*
            if (isNumerical.get(i)) {
                sb.append(originalData.get(i + 1)).append(" .\n");
            }
            else {
                sb.append("\"").append(originalData.get(i + 1)).append("\" .\n");
            }
             */
        }
        return sb.toString();
    }


    public static String createBlankNodeIntervalTriples(String tab[], String subject, String predicate, Counter blankIdCounter) {
        String s =  "<http://blank" + blankIdCounter.getValue() + "> .\n" +
                "<http://blank" + blankIdCounter.getValue() + ">  <http://minValue> " + tab[0] + " .\n" +
                "<http://blank" + blankIdCounter.getValue() + ">  <http://maxValue> " + tab[1] + " .\n";

        return s;
    }


    public static String createTransformedTriplesString(List<String> transformedData, List<String> predicates,
                                                        List<Boolean> isNumerical, Counter blankIdCounter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < predicates.size(); i++) {
            sb.append("<").append(transformedData.get(0)).append("> ").append(predicates.get(i)).append(" ");

            String[] tab = transformedData.get(i + 1).split("-");
            if (tab.length > 1) {
                sb.append(createBlankNodeIntervalTriples(tab, transformedData.get(0), predicates.get(i), blankIdCounter));
                blankIdCounter.increment();
            }
            else {
                if (isNumerical.get(i) && !tab[0].equals("*")) {
                    /*if (tab[0].equals("*")) {
                        System.out.println("AAAAAAAAAA : " + i);
                    }*/
                    sb.append(tab[0]).append(" .\n");
                }
                else {
                    sb.append("\"").append(tab[0]).append("\" .\n");
                }
            }
        }
        return sb.toString();
    }


    public static List<String> computeGeneralizationQueries(List<List<String>> originalData, List<List<String>> transformedData, List<String> predicates,
                                                            List<Boolean> isNumerical, Counter blankIdCounter) {

        //Counter blankIdCounter = new Counter();
        List<String> updateQueries = new ArrayList<>();

        for (int i = 0; i < transformedData.size(); i++) {
            //List<String> od = originalData.get(i);
            List<String> td = transformedData.get(i);
            String originalTriples = createOriginalTriplesString(td, predicates, isNumerical);
            String transformedTriple = createTransformedTriplesString(td, predicates, isNumerical, blankIdCounter);
            StringBuilder sb = new StringBuilder("DELETE { ");
            sb.append(originalTriples).append("}\nINSERT { ").append(transformedTriple).append("}\nWHERE { ")
                    .append(originalTriples).append("}");
            updateQueries.add(sb.toString());
        }

        //System.out.println(updateQueries);
        return updateQueries;
    }


    public static String createSelectClauseCountQuery(List<String> predicates) {
        StringBuilder selectClause = new StringBuilder("SELECT ");
        for (int i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            selectClause.append(attributeVariable).append(" ");
        }
        selectClause.append("(COUNT(?s) as ?c) \n");
        return selectClause.toString();
    }

    public static String createSelectClauseCountAgeZipGroupQuery() {
        StringBuilder selectClause = new StringBuilder("SELECT ?group (COUNT(?s) as ?c) \n");
        return selectClause.toString();
    }


    public static String createGroupByClause(List<String> predicates) {
        StringBuilder groupByClause = new StringBuilder("GROUP BY  ");
        for (int i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            groupByClause.append(attributeVariable).append(" ");
        }
        groupByClause.append("ORDER BY ?c");
        return groupByClause.toString();
    }


    public static String createWhereClauseAgeQueryBeforeAnon(int minValue, int maxValue, List<String> predicates) {
        StringBuilder whereClause = new StringBuilder("WHERE { ");
        for (int i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            whereClause.append("?s ").append(predicates.get(i)).append(" ").append(attributeVariable).append(" . ");
        }
        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o . ")
                        .append("FILTER(?o >= ").append(minValue).append(" && ?o < ").append(maxValue).append(") . }\n");
        return whereClause.toString();
    }


    public static String createWhereClauseAgeQuery(int minValue, int maxValue, String saGroup) {
        StringBuilder whereClause = new StringBuilder("WHERE { { ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o . ")
                .append("FILTER(?o >= ").append(minValue).append(" && ?o < ").append(maxValue).append(") . ");
        if (saGroup != null) {
            whereClause.append("?s <http://inGroup> <").append(saGroup).append("> . ");
        }

        whereClause.append(" } UNION { \n");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b . ")
                .append("?b <http://minValue> ?min . ")
                .append("?b <http://maxValue> ?max . ")
                .append("FILTER(?min >= ").append(minValue).append(" && ?max < ").append(maxValue).append(") . ");

        if (saGroup != null) {
            whereClause.append("?s <http://inGroup> <").append(saGroup).append("> . ");
        }

        whereClause.append("}}");
        return whereClause.toString();
    }


    public static String createCountAgeQueryBeforeAnon(int minValue, int maxValue, List<String> predicates) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(createSelectClauseCountQuery(predicates));
        queryStr.append(createWhereClauseAgeQueryBeforeAnon(minValue, maxValue, predicates));
        if (!predicates.isEmpty()) {
            queryStr.append(createGroupByClause(predicates));
        }
        return queryStr.toString();
    }



    public static String createCountAgeQuery(int minValue, int maxValue, String saGroup) {
        String selectClause = "SELECT (COUNT(?s) as ?c) \n";
        String whereClause = createWhereClauseAgeQuery(minValue, maxValue, saGroup);

        return selectClause + whereClause;
    }


    public static String createWhereClauseAgeZipcodeQueryBeforeAnon(List<String> predicates, int minValue, int maxValue, String zipcode) {
        StringBuilder whereClause = new StringBuilder("WHERE { ");
        for (int i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            whereClause.append("?s ").append(predicates.get(i)).append(" ").append(attributeVariable).append(" . ");
        }
        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o . ")
                .append("FILTER(?o >= ").append(minValue).append(" && ?o < ").append(maxValue).append(") . ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#zipcode> ?z . ")
                .append("FILTER regex(?z, \"^").append(zipcode).append("\") . ");

        whereClause.append("} ");
        return whereClause.toString();
    }


    public static String createCountAgeZipcodeQueryBeforeAnon(List<String> predicates, int minValue, int maxValue, String zipcode) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(createSelectClauseCountQuery(predicates));
        queryStr.append(createWhereClauseAgeZipcodeQueryBeforeAnon(predicates, minValue, maxValue, zipcode));
        if (!predicates.isEmpty()) {
            queryStr.append(createGroupByClause(predicates));
        }
        return queryStr.toString();
    }


    public static String createWhereClauseAgeZipcodeQuery(List<String> predicates, int minValue, int maxValue, String zipcode, List<String> saGroups) {
        StringBuilder whereClause = new StringBuilder("WHERE { { ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o . ")
                .append("FILTER(?o >= ").append(minValue).append(" && ?o < ").append(maxValue).append(") . ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#zipcode> ?z . \n")
                .append("FILTER regex(?z, \"^").append(zipcode).append("\") . ");

        for (String g : saGroups) {
            whereClause.append("?s <http://inGroup> <").append(g).append("> . ");
        }
/*
        for (int i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            whereClause.append("?s ").append(predicates.get(i)).append(" ").append(attributeVariable).append(" . ");
        }
*/
        whereClause.append(" } UNION { \n");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b . ")
                .append("?b <http://minValue> ?min . ")
                .append("?b <http://maxValue> ?max . ")
                .append("FILTER(?min >= ").append(minValue).append(" && ?max < ").append(maxValue).append(") . ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#zipcode> ?z . \n")
                .append("FILTER regex(?z, \"^").append(zipcode).append("\") . ");

        for (String g : saGroups) {
            whereClause.append("?s <http://inGroup> <").append(g).append("> . ");
        }
/*
        for (int i = 0; i < predicates.size(); i++) {
            String attributeVariable = "?attr" + i;
            whereClause.append("?s ").append(predicates.get(i)).append(" ").append(attributeVariable).append(" . ");
        }
*/
        whereClause.append("}}");
        return whereClause.toString();
    }


    public static String createCountAgeZipcodeQuery(List<String> predicates, int minValue, int maxValue, String zipcode, List<String> saGroups) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(createSelectClauseCountQuery(predicates));
        queryStr.append(createWhereClauseAgeZipcodeQuery(predicates, minValue, maxValue, zipcode, saGroups));
        if (!predicates.isEmpty()) {
            queryStr.append(createGroupByClause(predicates));
        }
        return queryStr.toString();
    }


    public static String createCountAgeZipcodeGroupQuery1(int minValue, int maxValue, String zipcode) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT ?group (COUNT(?s) as ?c) \n");
        queryStr.append(createWhereClauseAgeZipcodeGroupQuery1(minValue, maxValue, zipcode));
        queryStr.append("GROUP BY ?group");
        return queryStr.toString();
    }


    public static String createWhereClauseAgeZipcodeGroupQuery1(int minValue, int maxValue, String zipcode) {
        StringBuilder whereClause = new StringBuilder("WHERE { ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o . ")
                .append("FILTER(?o >= ").append(minValue).append(" && ?o < ").append(maxValue).append(") . ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#zipcode> ?z . ")
                .append("FILTER regex(?z, \"^").append(zipcode).append("\") . ");

        whereClause.append("?s <http://inGroup> ?group . ");
        whereClause.append("} ");
        return whereClause.toString();
    }


    public static String createCountAgeZipcodeGroupQuery2(int minValue, int maxValue, String zipcode) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT ?group (COUNT(?s) as ?c) \n");
        queryStr.append(createWhereClauseAgeZipcodeGroupQuery2(minValue, maxValue, zipcode));
        queryStr.append("GROUP BY ?group");
        return queryStr.toString();
    }

    public static String createWhereClauseAgeZipcodeGroupQuery2(int minValue, int maxValue, String zipcode) {
        StringBuilder whereClause = new StringBuilder("WHERE { ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b . ")
                .append("?b <http://minValue> ?min . ")
                .append("?b <http://maxValue> ?max . ")
                .append("FILTER(?min >= ").append(minValue).append(" && ?max < ").append(maxValue).append(") . ");

        whereClause.append("?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#zipcode> ?z . ")
                .append("FILTER regex(?z, \"^").append(zipcode).append("\") . ");

        whereClause.append("?s <http://inGroup> ?group . ");
        whereClause.append("} ");
        return whereClause.toString();
    }

    public static String createCountQueryZipcode(String zipcodePrefix) {
        return
                "SELECT (COUNT(?s) as ?c) WHERE { ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#zipcode> ?o . \n" +
                        "FILTER regex(?o, \"^" + zipcodePrefix + "\") }";
    }


    public static String getAttributeCardinality() {
        return
                "SELECT ?val ?card WHERE { " +
                        "?attribute <http://value> ?val ." +
                        "?attribute <http://cardinality> ?card ." +
                        "}";
    }

    public static Map<String, Integer> getMapAttributeToCardinality(Model model) {
        Map<String, Integer> map = new HashMap<>();
        Query query = QueryFactory.create(getAttributeCardinality());
        List<QuerySolution> sols = QueryUtil.execQuery(query, model);

        sols.forEach(qs -> {
            String attributeValue = qs.get("val").toString();
            int card = Integer.parseInt(qs.get("card").toString().split("\\^\\^")[0]);
            map.put(attributeValue, card);
        });
        return map;
    }


    public static String getAttributesInGroups() {
        return
                "SELECT ?val ?g WHERE { " +
                        "?g ?p ?attribute ." +
                        "?attribute <http://value> ?val ." +
                        "}";
    }

    public static Map<String, String> getMapAttributeToGroup(Model model) {
        Map<String, String> map = new HashMap<>();
        Query query = QueryFactory.create(getAttributesInGroups());
        List<QuerySolution> sols = QueryUtil.execQuery(query, model);

        sols.forEach(qs -> {
            String attribute = qs.get("val").toString();
            String group = qs.get("g").toString();
            map.put(attribute, group);
        });
        return map;
    }



    public static String totalCardinalityByGroupRequest() {
        return
                "SELECT distinct ?g (sum(?card) as ?sum) where {" +
                        "?g ?p ?attr ." +
                        "?attr <http://cardinality> ?card ." +
                        "} group by ?g";
    }


    public static Map<String, Integer> getMapGroupCardinality(Model model) {
        Map<String, Integer> totalCardinalityByGroups = new HashMap<>();
        Query query = QueryFactory.create(QueryUtil.totalCardinalityByGroupRequest());
        List<QuerySolution> sols = QueryUtil.execQuery(query, model);

        sols.forEach(qs -> {
            String groupName = qs.get("g").toString();
            int cardTotal = Integer.parseInt(qs.get("sum").toString().split("\\^\\^")[0]);
            totalCardinalityByGroups.put(groupName, cardTotal);
        });
        return totalCardinalityByGroups;
    }


    public static List<String> getSensitiveAttributes(Model model) {
        List<String> sensitiveAttributes = new ArrayList<>();
        String queryStr = "select distinct ?o where { ?s <http://value> ?o . }";
        Query q = QueryFactory.create(queryStr);

        execQuery(q, model).forEach(qs -> sensitiveAttributes.add(qs.get("o").toString()));
        return sensitiveAttributes;
    }


    public static List<String> getZipcodes(Model model) {
        List<String> zipcodes = new ArrayList<>();
        String queryStr = "select distinct ?o where { ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#zipcode> ?o . }";
        Query q = QueryFactory.create(queryStr);

        execQuery(q, model).forEach(qs -> zipcodes.add(qs.get("o").toString()));
        return zipcodes;
    }




    public static int countSubjectsWithAgeInGroup(int minValue, int maxValue, String saGroup, Model model) {
        Query q = QueryFactory.create(QueryUtil.createCountAgeQuery(minValue, maxValue, saGroup));
        int res = Integer.parseInt(execQuery(q, model).get(0).get("c").toString().split("\\^\\^")[0]);
        return res;
    }

    public static int countSubjectsWithAgeZipcodeInGroup(int minValue, int maxValue, String zipcode, String saGroup, Model model) {
        Query q = QueryFactory.create(QueryUtil.createCountAgeZipcodeQuery(Collections.emptyList(), minValue, maxValue, zipcode, Arrays.asList(saGroup)));
        int res = Integer.parseInt(execQuery(q, model).get(0).get("c").toString().split("\\^\\^")[0]);
        return res;
    }

    public static int countSubjectsWithAgeZipcodeInTwoGroups(int minValue, int maxValue, String zipcode, String saGroup1, String saGroup2, Model model) {
        Query q = QueryFactory.create(QueryUtil.createCountAgeZipcodeQuery(Collections.emptyList(), minValue, maxValue, zipcode, Arrays.asList(saGroup1, saGroup2)));
        int res = Integer.parseInt(execQuery(q, model).get(0).get("c").toString().split("\\^\\^")[0]);
        return res;
    }





}
