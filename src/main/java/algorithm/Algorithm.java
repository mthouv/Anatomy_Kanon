package algorithm;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateAction;
import preprocessing.DistanceMatrix;
import preprocessing.UpwardCotopy;
import query.QueryUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static query.QueryUtil.execQuery;

public class Algorithm {


    public static List<Cluster> getInitialClustersExtended(String predicate, String privacyClause, Model model) {
        String aggregationQueryStr = QueryUtil.createInitialClustersQuery(predicate, privacyClause);//QueryUtil.createInitialClustersQuery(predicate, privacyQuery, model);
        Query query = QueryFactory.create(aggregationQueryStr);
        List<QuerySolution> querySolutions = execQuery(query, model);

        List<Cluster> clusters = querySolutions.stream()
                .map(sol -> {
                    int cardinality = Integer.parseInt(sol.get("count").toString().split("\\^\\^")[0]);
                    SensitiveAttribute sa = new SensitiveAttribute(sol.get("attribute").toString(), cardinality);
                    return new Cluster(sa, sol.get("super").toString());
                }).collect(Collectors.toList());

        //System.out.println("COUNT LEAF " + clusters.stream().map(x -> x.getSensitiveAttributes().get(0).getValue()).count());
        //System.out.println("COUNT LEAF DISTINCT " + clusters.stream().map(x -> x.getSensitiveAttributes().get(0).getValue()).distinct().count());

        return clusters;
    }



    public static List<Cluster> getInitialClustersExtended2(String predicate, String privacyClause, Model model) {
        String aggregationQueryStr = QueryUtil.createInitialClustersQuery(predicate, privacyClause);//QueryUtil.createInitialClustersQuery(predicate, privacyQuery, model);
        Query query = QueryFactory.create(aggregationQueryStr);
        List<QuerySolution> querySolutions = execQuery(query, model);

        List<Cluster> clusters = new ArrayList<>();

        /*
        In case a sensitive attribute has multiple types, we only store it once
        */
        Set<String> alreadySeenSaValue = new HashSet<>();

        for (QuerySolution qs : querySolutions) {
            String saValue = qs.get("attribute").toString();
            int cardinality = Integer.parseInt(qs.get("count").toString().split("\\^\\^")[0]);
            if (!alreadySeenSaValue.contains(saValue)) {
                alreadySeenSaValue.add(saValue);
                SensitiveAttribute sa = new SensitiveAttribute(saValue, cardinality);
                Cluster tmp = new Cluster(sa, qs.get("super").toString());
                clusters.add(tmp);
            }
        }
        //System.out.println("COUNT LEAF " + clusters.stream().map(x -> x.getSensitiveAttributes().get(0).getValue()).count());
        //System.out.println("COUNT LEAF DISTINCT " + clusters.stream().map(x -> x.getSensitiveAttributes().get(0).getValue()).distinct().count());

        return clusters;
    }



    public static List<SensitiveAttribute> getSensitiveAttributes(String predicate, String privacyClause, Model model) {
        String aggregationQueryStr = QueryUtil.retrieveSensitiveAttributesRequest(predicate, privacyClause);
        Query query = QueryFactory.create(aggregationQueryStr);
        List<QuerySolution> querySolutions = execQuery(query, model);

        List<SensitiveAttribute> attributes = new ArrayList<>();


        for (QuerySolution qs : querySolutions) {
            String saValue = qs.get("attribute").toString();
            int cardinality = Integer.parseInt(qs.get("count").toString().split("\\^\\^")[0]);
            SensitiveAttribute sa = new SensitiveAttribute(saValue, cardinality);
            attributes.add(sa);
        }
        return attributes;
    }


    public static List<Cluster> hierarchicalClustering(List<Cluster> leafs, DistanceMatrix matrix, Model model) {
        ArrayList<Cluster> clusters = new ArrayList<>();
        double similarity, similarityTmp;
        int indexToFusion = 0, i;
        boolean inLeafs = true;

        if (leafs.size() <= 1) {
            return leafs;
        }

        while (!leafs.isEmpty()) {
            int nbLeafs = leafs.size();
            Cluster leafToCluster = leafs.get(nbLeafs - 1);
            leafs.remove(nbLeafs - 1);
            similarity = 0.0;
            for (i = 0; i < nbLeafs - 1; i++) {
                similarityTmp = matrix.getDistance(leafToCluster.getConcept(), leafs.get(i).getConcept());
                if (similarityTmp > similarity) {
                    similarity = similarityTmp;
                    indexToFusion = i;
                }
            }

            for (i = 0; i < clusters.size(); i++) {
                similarityTmp = matrix.getDistance(leafToCluster.getConcept(), clusters.get(i).getConcept());
                if (similarityTmp >= similarity) {
                    similarity = similarityTmp;
                    indexToFusion = i;
                    inLeafs = false;
                }
            }

            if (inLeafs) {
                Cluster closestLeaf = leafs.get(indexToFusion);
                leafs.remove(indexToFusion);
                closestLeaf.fusion(leafToCluster, model);
                clusters.add(closestLeaf);
            }
            else {
                Cluster closestCluster = clusters.get(indexToFusion);
                closestCluster.fusion(leafToCluster, model);
            }
            inLeafs = true;
        }
        return clusters;
    }



    public static List<Cluster> balanceClusters(List<Cluster> clusters, DistanceMatrix matrix, Model model, int minSize) {
        ArrayList<Cluster> balancedClusters = new ArrayList<>();
        double similarity, similarityTmp;
        int indexToFusion = 0, i;

        while (!clusters.isEmpty()) {
            int nbClusters = clusters.size();
            Cluster currentCluster = clusters.get(nbClusters - 1);
            if (currentCluster.size() >= minSize) {
                clusters.remove(nbClusters - 1);
                balancedClusters.add(currentCluster);
                continue;
            }

            similarity = 0.0;
            for (i = 0; i < nbClusters - 1; i++) {
                similarityTmp = matrix.getDistance(currentCluster.getConcept(), clusters.get(i).getConcept());
                if (similarityTmp > similarity) {
                    similarity = similarityTmp;
                    indexToFusion = i;
                }
            }

            Cluster closestCluster = clusters.get(indexToFusion);
            clusters.remove(indexToFusion);
            currentCluster.fusion(closestCluster, model);
            if (currentCluster.size() >= minSize) {
                clusters.remove(nbClusters - 2);
                balancedClusters.add(currentCluster);
            }
        }
        return balancedClusters;
    }



    public static List<String> computeAnatomisationOps(List<Cluster> clusters, String predicate, String privacyClause, long groupId) {
        ArrayList<String> ops = new ArrayList<>();
        int count = 0;
        //System.out.println("CLUSTER SIZE :" + clusters.size());
        for (Cluster c : clusters) {
           // System.out.println("ID " + groupId + " " + c.getConcept() + "\nXXXX\n");
                long attributeId = 0;
                for (SensitiveAttribute sa : c.getSensitiveAttributes()) {
                    count++;
                    ops.add(QueryUtil.getClusterizationUpdateRequest(sa, c.getConcept(), predicate, privacyClause, groupId, attributeId));
                    attributeId++;
                }
            groupId++;
        }
        System.out.println("COUNT ATTRIBUTES : " + count);
        return ops;
    }


    public static List<String> computeAnatomisationOpsNoTaxonomy(List<SensitiveAttribute> attributes, String predicate, String privacyClause, long groupId) {
        ArrayList<String> ops = new ArrayList<>();
        long attributeId = 0;
        System.out.println("NUMBER OF SA :" + attributes.size());
        for (SensitiveAttribute sa : attributes) {
                ops.add(QueryUtil.getUpdateStringNoTaxonomy(sa, predicate, privacyClause, groupId, attributeId));
                attributeId++;
        }
        return ops;
    }




    public static List<Cluster> applyAnatomizationAux(String predicate, String privacyClause, Model model) {

        List<String> concepts = QueryUtil.getAllConcepts(model);
        List<UpwardCotopy> ucs = QueryUtil.getUpwardCotopiesFromConcepts(concepts, model);
        DistanceMatrix dm = new DistanceMatrix(ucs);

        List<Cluster> leafs = Algorithm.getInitialClustersExtended2(predicate, privacyClause, model);
        double start = System.currentTimeMillis();
        List<Cluster> finalClusters = Algorithm.hierarchicalClustering(leafs, dm, model);
        double elapsed = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println("CLUSTERING TIME : " + elapsed);
        return finalClusters;
    }


    public static List<String> applyAnatomization(String predicate, Query privacyQuery, Model model) {
        List<String> ops = new ArrayList<>();
        List<String> allSubProperties = QueryUtil.getSubProperties(predicate, model);
        long groupId = QueryUtil.getNumberGroups(model);
        String privacyClause = "";

        if(privacyQuery != null) {
            List<String> privacyTriples = QueryUtil.extractTriplesFromQuery(privacyQuery, model);
            if (!privacyTriples.isEmpty()) {
                privacyClause = privacyTriples.stream().collect(Collectors.joining("\n", "", " .\n"));
            }
        }
        for (String p : allSubProperties) {
            List<Cluster> clusters = applyAnatomizationAux(p, privacyClause, model);
            ops.addAll(computeAnatomisationOps(clusters, p, privacyClause, groupId));
            groupId += clusters.size();
        }
        return ops;
    }


    public static List<String> applyAnatomizationNoTaxonomy(String predicate, Query privacyQuery, Model model) {
        List<String> ops = new ArrayList<>();
        List<String> allSubProperties = QueryUtil.getSubProperties(predicate, model);
        long groupId = QueryUtil.getNumberGroups(model);
        String privacyClause = "";

        if(privacyQuery != null) {
            List<String> privacyTriples = QueryUtil.extractTriplesFromQuery(privacyQuery, model);
            if (!privacyTriples.isEmpty()) {
                privacyClause = privacyTriples.stream().collect(Collectors.joining("\n", "", " .\n"));
            }
        }
        for (String p : allSubProperties) {
            List<SensitiveAttribute> attributes  = Algorithm.getSensitiveAttributes(predicate, privacyClause, model);
            ops.addAll(computeAnatomisationOpsNoTaxonomy(attributes, p, privacyClause, groupId));
            groupId += attributes.size();
        }
        return ops;
    }


    public static void executeAnatomy(Dataset dataset, Model model, List<String> predicatesWithTaxonomy, List<String> predicates) {
        for (String s : predicatesWithTaxonomy) {
            System.out.println("Predicate : " + s);
            List<String> updateQueries = Algorithm.applyAnatomization(s, null, model);
            //System.out.println("UPDATE SIZE :  " + updateQueries.size());
            for (String q : updateQueries) {
                if (dataset != null) {
                    UpdateAction.parseExecute(q, dataset);
                }
                else {
                    UpdateAction.parseExecute(q, model);
                }
            }
        }

        for (String p : predicates) {
            List<String> updateQueries = Algorithm.applyAnatomizationNoTaxonomy(p, null, model);
            for (String q : updateQueries) {
                if (dataset != null) {
                    UpdateAction.parseExecute(q, dataset);
                }
                else {
                    UpdateAction.parseExecute(q, model);
                }
            }
        }
    }

}

