package kanon;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateAction;
import org.deidentifier.arx.*;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import query.QueryUtil;
import util.ArxUtil;
import util.Counter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Anonymization {



    public static void globalGeneralization(Model model, int k, List<String> predicates, List<String> columnNames,
                                            List<String> hierarchies, List<Boolean> isNumerical, boolean produceCsv,
                                            String csvFilepath) throws IOException {

        Counter blankIdCounter = new Counter();

        List<List<String>> originalData = QueryUtil.retrieveResultsFromQuery(predicates, null, model);
        if (produceCsv) {
            QueryUtil.flushResultsToCsv(originalData, columnNames, csvFilepath);
        }
        System.out.println("SIZE ORIGINAL DATA : " + originalData.size());

        Data data = Data.create(csvFilepath, StandardCharsets.UTF_8, ';');
        ArxUtil.setDataAttributeTypes(data, columnNames);
        ArxUtil.setDataHierarchies(data, columnNames, hierarchies);


        // Define relative number of records to be generalized in each iteration
        double oMin = 0.01d;

        // Define a parameter for the quality model which only considers generalization
        double gsFactor = 0d;

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(k));
        //config.setSuppressionLimit(0d);
        config.setSuppressionLimit(1d - oMin);
        config.setQualityModel(Metric.createLossMetric(gsFactor));

        /*
        config.setAttributeWeight("age", 0.5);
        config.setAttributeWeight("sex", 0.2);
        config.setAttributeWeight("zipcode", 0.3);
        */

        ARXResult result = anonymizer.anonymize(data, config);

        DataHandle optimum = result.getOutput();

        try {
            // Now apply local recoding to the result
            result.optimizeIterativeFast(optimum, 0.1d);
        } catch (RollbackRequiredException e) {

            // This part is important to ensure that privacy is preserved, even in case of exceptions
            optimum = result.getOutput();
        }

        Iterator<String[]> transformed = optimum.iterator();
        List<List<String>> transformedData = new ArrayList<>();
        transformed.next();
        transformed.forEachRemaining(e -> transformedData.add(Arrays.asList(e)));

        System.out.println("SIZE TRANSFORMED DATA : " + transformedData.size());

        List<String> updateQueries = QueryUtil.computeGeneralizationQueries(originalData, transformedData, predicates, isNumerical, blankIdCounter);
        updateQueries.forEach(x -> UpdateAction.parseExecute(x, model));

        //transformedData.forEach(System.out::println);
    }



    public static void generalizationByGroup(Model model, int k, List<String> predicates, List<String> columnNames,
                                            List<String> hierarchies, List<Boolean> isNumerical, boolean produceCsv,
                                            String csvFilepath) throws IOException {

        Counter blankIdCounter = new Counter();

        List<String> groupsURI = QueryUtil.retrieveGroupsURI(model);

        for (int i = 0; i < groupsURI.size(); i++) {
            List<List<String>> originalData = QueryUtil.retrieveResultsFromQuery(predicates, groupsURI.get(i), model);
            String tmpFilepath = csvFilepath + "_g" + i;
            if (produceCsv) {
                QueryUtil.flushResultsToCsv(originalData, columnNames, tmpFilepath);
            }

            Data data = Data.create(tmpFilepath, StandardCharsets.UTF_8, ';');
            ArxUtil.setDataAttributeTypes(data, columnNames);
            ArxUtil.setDataHierarchies(data, columnNames, hierarchies);

            // Define relative number of records to be generalized in each iteration
            double oMin = 0.01d;

            // Define a parameter for the quality model which only considers generalization
            double gsFactor = 0d;

            ARXAnonymizer anonymizer = new ARXAnonymizer();
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(k));
            //config.setSuppressionLimit(0d);
            config.setSuppressionLimit(1d - oMin);
            config.setQualityModel(Metric.createLossMetric(gsFactor));

            ARXResult result = anonymizer.anonymize(data, config);

            DataHandle optimum = result.getOutput();
            try {
                // Now apply local recoding to the result
                result.optimizeIterativeFast(optimum, 0.1d);
            } catch (RollbackRequiredException e) {
                // This part is important to ensure that privacy is preserved, even in case of exceptions
                optimum = result.getOutput();
            }

            Iterator<String[]> transformed = optimum.iterator();
            List<List<String>> transformedData = new ArrayList<>();
            transformed.next();
            transformed.forEachRemaining(e -> transformedData.add(Arrays.asList(e)));
            List<String> updateQueries = QueryUtil.computeGeneralizationQueries(originalData, transformedData, predicates,
                    isNumerical, blankIdCounter);
            updateQueries.forEach(x -> {
                //System.out.println(x);
                UpdateAction.parseExecute(x, model);
            });
        }
    }

}
