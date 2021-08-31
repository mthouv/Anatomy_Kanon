package evaluation;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import query.QueryUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Evaluation {

    public static Map<List<String>, Integer> evaluateOriginalDataAge(Model model) {
        Map<List<String>, Integer> mapBeforeAnon = new HashMap<>();

        List<String> sols20_30 = QueryUtil.execQuery(QueryFactory.create(Queries.query_20_30_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 20 - 30 : " + sols20_30.size());
        mapBeforeAnon.put(Arrays.asList("20", "30"), sols20_30.size());

        List<String> sols30_40 = QueryUtil.execQuery(QueryFactory.create(Queries.query_30_40_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 30 - 40 : " + sols30_40.size());
        mapBeforeAnon.put(Arrays.asList("30", "40"), sols30_40.size());

        List<String> sols40_50 = QueryUtil.execQuery(QueryFactory.create(Queries.query_40_50_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 40 - 50 : " + sols40_50.size());
        mapBeforeAnon.put(Arrays.asList("40", "50"), sols40_50.size());

        List<String> sols50_60 = QueryUtil.execQuery(QueryFactory.create(Queries.query_50_60_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 50 - 60 : " + sols50_60.size());
        mapBeforeAnon.put(Arrays.asList("50", "60"), sols50_60.size());


        List<String> sols60_70 = QueryUtil.execQuery(QueryFactory.create(Queries.query_60_70_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 60 - 70 : " + sols60_70.size());
        mapBeforeAnon.put(Arrays.asList("60", "70"), sols60_70.size());

        List<String> sols70_80 = QueryUtil.execQuery(QueryFactory.create(Queries.query_70_80_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 70 - 80 : " + sols70_80.size());
        mapBeforeAnon.put(Arrays.asList("70", "80"), sols70_80.size());

        List<String> sols80_90 = QueryUtil.execQuery(QueryFactory.create(Queries.query_80_90_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 80 - 90 : " + sols80_90.size());
        mapBeforeAnon.put(Arrays.asList("80", "90"), sols80_90.size());

        List<String> sols90_100 = QueryUtil.execQuery(QueryFactory.create(Queries.query_90_100_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 90 - 100 : " + sols90_100.size());
        mapBeforeAnon.put(Arrays.asList("90", "101"), sols90_100.size());

        List<String> sols20_40 = QueryUtil.execQuery(QueryFactory.create(Queries.query_20_40_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 20 - 40 : " + sols20_40.size());
        mapBeforeAnon.put(Arrays.asList("20", "40"), sols20_40.size());

        List<String> sols40_60 = QueryUtil.execQuery(QueryFactory.create(Queries.query_40_60_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 40 - 60 : " + sols40_60.size());
        mapBeforeAnon.put(Arrays.asList("40", "60"), sols40_60.size());

        List<String> sols60_80 = QueryUtil.execQuery(QueryFactory.create(Queries.query_60_80_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 60 - 80 : " + sols60_80.size());
        mapBeforeAnon.put(Arrays.asList("60", "80"), sols60_80.size());

        List<String> sols80_100 = QueryUtil.execQuery(QueryFactory.create(Queries.query_80_100_1), model).stream().map(e -> e.get("s").toString())
                .collect(Collectors.toList());
        System.out.println("Original 80 - 100 : " + sols80_100.size());
        mapBeforeAnon.put(Arrays.asList("80", "101"), sols80_100.size());

        return mapBeforeAnon;
    }

    public static void evaluateTransformedDataAge(Map<List<String>, Integer> mapBeforeAnon, Model model) {
        List<Integer> ages = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90);
        int count1 = 0, count2 = 0;
        double recalls1 = 0.0, recalls2 = 0.0;
        double errors1 = 0.0, errors2 = 0.0;
        double relativeErrors1 = 0.0, relativeErrors2 = 0.0;


        for (int i = 0; i < ages.size(); i++) {
            Integer minValue = ages.get(i);

            Integer maxValue = minValue + 10;
            if (minValue == 90) {
                maxValue++;
            }

            Query q = QueryFactory.create(QueryUtil.createCountAgeQuery(minValue, maxValue, null));
            int expectedResult = mapBeforeAnon.get(Arrays.asList(minValue.toString(), maxValue.toString()));
            int estimatedResult = Integer.parseInt(QueryUtil.execQuery(q, model).get(0).get("c").toString().split("\\^\\^")[0]);
            count1++;

            printResultHeader(minValue, maxValue);
            System.out.println("EXPECTED RESULT : " + expectedResult);
            System.out.println("ESTIMATED RESULT : " + estimatedResult + "\n");

            double difference = expectedResult - estimatedResult;
            if (difference < 0) {
                difference *= -1;
            }
            errors1 += difference;

            double relativeErr = (expectedResult - estimatedResult) * 1.0 / expectedResult;
            if (relativeErr < 0) {
                relativeErr *= -1;
            }
            relativeErrors1 += relativeErr;

            double recall = estimatedResult * 1.0 / expectedResult;
            recalls1 += recall;


            if (minValue % 20 == 0) {
                maxValue = minValue + 20;
                if (minValue == 80) {
                    maxValue++;
                }

                q = QueryFactory.create(QueryUtil.createCountAgeQuery(minValue, maxValue, null));
                expectedResult = mapBeforeAnon.get(Arrays.asList(minValue.toString(), maxValue.toString()));
                estimatedResult = Integer.parseInt(QueryUtil.execQuery(q, model).get(0).get("c").toString().split("\\^\\^")[0]);
                count2++;

                printResultHeader(minValue, maxValue);
                System.out.println("EXPECTED RESULT : " + expectedResult);
                System.out.println("ESTIMATED RESULT : " + estimatedResult);

                difference = expectedResult - estimatedResult;
                if (difference < 0) {
                    difference *= -1;
                }
                errors2 += difference;

                relativeErr = (expectedResult - estimatedResult) * 1.0 / expectedResult;
                if (relativeErr < 0) {
                    relativeErr *= -1;
                }
                relativeErrors2 += relativeErr;

                recall = estimatedResult * 1.0 / expectedResult;
                recalls2 += recall;
            }
        }
        printFinalResultsAfterAnon(count1, count2, errors1, errors2, relativeErrors1, relativeErrors2, 0, 0, recalls1, recalls2);
    }



    public static Map<List<String>, Integer> evaluateCorrelationAgeBeforeAnon(List<String> predicates, Model model) {
        List<Integer> ages = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90);
        Map<List<String>, Integer> mapBeforeAnon = new HashMap<>();

        for (int i = 0; i < ages.size(); i++) {
            int minValue = ages.get(i);

            int maxValue = minValue + 10;
            if (minValue == 90) {
                maxValue++;
            }
            computeResultBeforeAnon(predicates, model, minValue, maxValue, mapBeforeAnon);

/*
            if (minValue % 20 == 0) {
                int maxValue = minValue + 20;
                if (minValue == 80) {
                    maxValue++;
                }
                displayResultBeforeAnon(predicates, model, minValue, maxValue, mapBeforeAnon);
            }
*/
        }
        return mapBeforeAnon;
    }


    public static Map<List<String>, Integer> evaluateCorrelationAgeZipBeforeAnon(List<String> predicates, List<String> zipcodes , Model model) {
        List<Integer> ages = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90);
        Map<List<String>, Integer> mapBeforeAnon = new HashMap<>();
/*
        List<String> zipcodes = QueryUtil.getZipcodes(model).stream().map(z -> {
            int prefixSize = z.length() - zipcodeGeneralizationLevel;
            return z.substring(0, prefixSize);
        }).distinct().collect(Collectors.toList());
*/
        //zipcodes.forEach(System.out::println);
        System.out.println("NUMBER OF ZIPS : " + zipcodes.size());

        for (int i = 0; i < ages.size(); i++) {
            for (String z : zipcodes) {
                int minValue = ages.get(i);

                int maxValue = minValue + 10;
                if (minValue == 90) {
                    maxValue++;
                }
                computeAgeZipResultsBeforeAnon(predicates, model, minValue, maxValue, z, mapBeforeAnon);

/*
                if (minValue % 20 == 0) {
                    int maxValue = minValue + 20;
                    if (minValue == 80) {
                        maxValue++;
                    }
                    computeAgeZipResultsBeforeAnon(predicates, model, minValue, maxValue, z, mapBeforeAnon);
                }
*/
            }
        }
            return mapBeforeAnon;
    }

    private static void computeAgeZipResultsBeforeAnon(List<String> predicates, Model model, Integer minValue, Integer maxValue,
                                                       String zipcodePrefix, Map<List<String>, Integer> mapBeforeAnon) {

        //printResultHeader(minValue, maxValue);
        //System.out.println("ZIPCODE PREFIXE : " + zipcodePrefix + "\n");
        Query q = QueryFactory.create(QueryUtil.createCountAgeZipcodeQueryBeforeAnon(predicates, minValue, maxValue, zipcodePrefix));
        QueryUtil.execQuery(q, model).forEach(qs -> {
            List<String> attributesValues = new ArrayList<>();
            attributesValues.add(minValue.toString());
            attributesValues.add(maxValue.toString());
            attributesValues.add(zipcodePrefix);
            for (int j = 0; j < predicates.size(); j++) {
                String attributeVariable = "attr" + j;
                attributesValues.add(qs.get(attributeVariable).toString());
            }
            int count = Integer.parseInt(qs.get("c").toString().split("\\^\\^")[0]);
            mapBeforeAnon.put(attributesValues, count);
            //System.out.println(attributesValues + " - " + count);
        });

    }

    private static void computeResultBeforeAnon(List<String> predicates, Model model, Integer minValue, Integer maxValue, Map<List<String>, Integer> mapBeforeAnon) {
        //printResultHeader(minValue, maxValue);
        //System.out.println(QueryUtil.createCountAgeQueryBeforeAnon(minValue, maxValue, predicates));
        Query q = QueryFactory.create(QueryUtil.createCountAgeQueryBeforeAnon(minValue, maxValue, predicates));
        QueryUtil.execQuery(q, model).forEach(qs -> {
            List<String> attributesValues = new ArrayList<>();
            attributesValues.add(minValue.toString());
            attributesValues.add(maxValue.toString());
            for (int j = 0; j < predicates.size(); j++) {
                String attributeVariable = "attr" + j;
                attributesValues.add(qs.get(attributeVariable).toString());
            }
            int count = Integer.parseInt(qs.get("c").toString().split("\\^\\^")[0]);
            mapBeforeAnon.put(attributesValues, count);
            //System.out.println(attributesValues + " - " + count);
        });
    }



/*
    public static void evaluateCorrelationAgeAfterAnon(List<String> predicates, Map<List<String>, Integer> mapBeforeAnon, Model model) {
        int count1 = 0, count2 = 0;
        double errors1 = 0.0, errors2 = 0.0;
        double relativeErrors1 = 0.0, relativeErrors2 = 0.0;
        int totalExpectedResults1 = 0, totalExpectedResults2 = 0;

        List<Integer> ages = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90);

        List<String> sensitiveAttributes = QueryUtil.getSensitiveAttributes(model);
        Map<String, Integer> mapAttributeToCardinality = QueryUtil.getMapAttributeToCardinality(model);
        Map<String, String> mapAttributeToGroup = QueryUtil.getMapAttributeToGroup(model);
        Map<String, Integer> mapGroupCardinality = QueryUtil.getMapGroupCardinality(model);

        for (int i = 0; i < ages.size(); i++) {

            Integer minValue = ages.get(i);

            Integer maxValue = minValue + 10;
            if (minValue == 90) {
                maxValue++;
            }

            printResultHeader(minValue, maxValue);

            System.out.println("SA SIZE : " + sensitiveAttributes.size());
            for (String sa : sensitiveAttributes) {

                count1++;
                List<String> attributes = Arrays.asList(minValue.toString(), maxValue.toString(), sa);
                int expectedResult = mapBeforeAnon.get(attributes);
                totalExpectedResults1 += expectedResult;

                int saCardinality = mapAttributeToCardinality.get(sa);
                String saGroup = mapAttributeToGroup.get(sa);
                int saGroupCardinality = mapGroupCardinality.get(saGroup);

                int nbSubjects = QueryUtil.countSubjectsWithAgeInGroup(minValue, maxValue, saGroup, model);
                double estimatedResult = nbSubjects * saCardinality * 1.0 / saGroupCardinality;

                double difference = expectedResult - estimatedResult;
                if (difference < 0) {
                    difference *= -1;
                }
                errors1 += difference;

                double relativeErr = (expectedResult - estimatedResult) / expectedResult;
                if (relativeErr < 0) {
                    relativeErr *= -1;
                }
                relativeErrors1 += relativeErr;

                printResultsAfterAnon(sa, expectedResult, saCardinality, saGroup, saGroupCardinality, nbSubjects, estimatedResult);
            }

            if (minValue % 20 == 0) {
                maxValue = minValue + 20;
                if (minValue == 80) {
                    maxValue++;
                }

                printResultHeader(minValue, maxValue);
                for (String sa : sensitiveAttributes) {

                    count2++;
                    List<String> attributes = Arrays.asList(minValue.toString(), maxValue.toString(), sa);
                    int expectedResult = mapBeforeAnon.get(attributes);
                    totalExpectedResults2 += expectedResult;


                    int saCardinality = mapAttributeToCardinality.get(sa);
                    String saGroup = mapAttributeToGroup.get(sa);
                    int saGroupCardinality = mapGroupCardinality.get(saGroup);

                    int nbSubjects = QueryUtil.countSubjectsWithAgeInGroup(minValue, maxValue, saGroup, model);
                    double estimatedResult = nbSubjects * saCardinality * 1.0 / saGroupCardinality;

                    double difference = expectedResult - estimatedResult;
                    if (difference < 0) {
                        difference *= -1;
                    }
                    errors2 += difference;

                    double relativeErr = (expectedResult - estimatedResult) / expectedResult;
                    if (relativeErr < 0) {
                        relativeErr *= -1;
                    }
                    relativeErrors2 += relativeErr;

                    printResultsAfterAnon(sa, expectedResult, saCardinality, saGroup, saGroupCardinality, nbSubjects, estimatedResult);
                }
            }
        }
        printFinalResultsAfterAnon(count1, count2, errors1, errors2, relativeErrors1, relativeErrors2, totalExpectedResults1, totalExpectedResults2, 0.0, 0.0);
    }
*/


    public static void evaluateCorrelationAgeAfterAnon(List<String> predicates, Map<List<String>, Integer> mapBeforeAnon, Model model) {
        int count1 = 0, count2 = 0;
        double errors1 = 0.0, errors2 = 0.0;
        double relativeErrors1 = 0.0, relativeErrors2 = 0.0;
        int totalExpectedResults1 = 0, totalExpectedResults2 = 0;


        Map<String, Integer> mapAttributeToCardinality = QueryUtil.getMapAttributeToCardinality(model);
        Map<String, String> mapAttributeToGroup = QueryUtil.getMapAttributeToGroup(model);
        Map<String, Integer> mapGroupCardinality = QueryUtil.getMapGroupCardinality(model);


        for (Map.Entry<List<String>, Integer> entry : mapBeforeAnon.entrySet()) {
            List<String> attributes = entry.getKey();
            int minValue = Integer.parseInt(attributes.get(0));
            int maxValue = Integer.parseInt(attributes.get(1));
            String sensitiveAttribute = attributes.get(attributes.size() - 1);


            count1++;
            int expectedResult = mapBeforeAnon.get(attributes);
            totalExpectedResults1 += expectedResult;

            int saCardinality = mapAttributeToCardinality.get(sensitiveAttribute);
            String saGroup = mapAttributeToGroup.get(sensitiveAttribute);
            int saGroupCardinality = mapGroupCardinality.get(saGroup);

            int nbSubjects = QueryUtil.countSubjectsWithAgeInGroup(minValue, maxValue, saGroup, model);
            double estimatedResult = nbSubjects * saCardinality * 1.0 / saGroupCardinality;
            //System.out.println("---------------- Zipcode -----------------  "  + zipcode);

            double difference = expectedResult - estimatedResult;
            if (difference < 0) {
                difference *= -1;
            }
            errors1 += difference;

            double relativeErr = (expectedResult - estimatedResult) / expectedResult;
            if (relativeErr < 0) {
                relativeErr *= -1;
            }
            relativeErrors1 += relativeErr;

        }

        printFinalResultsAfterAnon(count1, count2, errors1, errors2, relativeErrors1, relativeErrors2, totalExpectedResults1, totalExpectedResults2, 0.0, 0.0);
    }


    private static void printFinalResultsAfterAnon(int count1, int count2, double errors1, double errors2, double relativeErrors1,
                                                   double relativeErrors2, int totalExpectedResults1, int totalExpectedResults2, double recall1, double recall2) {
        System.out.println("N1 : " + count1);
        System.out.println("ERRORS " + errors1);
        System.out.println("ABS MEAN ERROR 1 : " +  (errors1 / count1));
        System.out.println("AVERAGE EXPECTED RESULT 1 : " + totalExpectedResults1 * 1.0 / count1);
        System.out.println("AVERAGE RELATIVE ERR : " + relativeErrors1 / count1);
        System.out.println("AVERAGE RECALL : " + recall1 / count1);

        System.out.println("\nN2 : " + count2);
        System.out.println("ERRORS " + errors2);
        System.out.println("ABS MEAN ERROR 2 : " +  (errors2 / count2));
        System.out.println("AVERAGE EXPECTED RESULT 2 : " + totalExpectedResults2 * 1.0 / count2);
        System.out.println("AVERAGE RELATIVE ERR : " + relativeErrors2 / count2);
        System.out.println("AVERAGE RECALL : " + recall2 / count2);

        System.out.println("YYYYYYYYYYYYYYYY");
    }


    private static void printResultsAfterAnon(String sa, int expectedResult, int saCardinality, String saGroup, int saGroupCardinality, int nbSubjects, double estimatedResult) {
        System.out.println("GROUP : " + saGroup + " - SA : " + sa + " - NB_SUBS : " + nbSubjects + " - SA_CARD : " +
                saCardinality + " - GROUP_CARD : " + saGroupCardinality + " - EXPECTED : " + expectedResult + " -  ESTIMATION =======>  " + estimatedResult);
    }

    private static void printResultHeader(int minValue, int maxValue) {
        System.out.println("\n------------------------------");
        System.out.println("MIN : " + minValue + "   -   MAX : " + maxValue);
        System.out.println("------------------------------\n");
    }


    public static Map<List<String>, Integer> countCorrelationAgeZipGroupAfterAnon(List<String> zipcodes, Model model) {
        List<Integer> ages = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90);
        Map<List<String>, Integer> mapAfterAnon = new HashMap<>();

        /*
        List<String> zipcodes = QueryUtil.getZipcodes(model).stream().map(z -> {
            int prefixSize = z.length() - zipcodeGeneralizationLevel;
            return z.substring(0, prefixSize);
        }).distinct().collect(Collectors.toList());
        */
        //zipcodes.forEach(System.out::println);

        for (int i = 0; i < ages.size(); i++) {
            for (String z : zipcodes) {
                int minValue = ages.get(i);

                int maxValue = minValue + 10;
                if (minValue == 90) {
                    maxValue++;
                }
                computeAgeZipGroupResultsAfterAnon(model, minValue, maxValue, z, mapAfterAnon);

/*
                if (minValue % 20 == 0) {
                    int maxValue = minValue + 20;
                    if (minValue == 80) {
                        maxValue++;
                    }
                    //TODO CHANGE
                    computeAgeZipResultsBeforeAnon(predicates, model, minValue, maxValue, z, mapBeforeAnon);
                }
*/
            }
        }
        return mapAfterAnon;
    }


    private static void computeAgeZipGroupResultsAfterAnon(Model model, Integer minValue, Integer maxValue,
                                                           String zipcodePrefix, Map<List<String>, Integer> mapAfterAnon) {

        //printResultHeader(minValue, maxValue);
        //System.out.println("ZIPCODE PREFIXE : " + zipcodePrefix + "\n");
        Query q = QueryFactory.create(QueryUtil.createCountAgeZipcodeGroupQuery1(minValue, maxValue, zipcodePrefix));
        QueryUtil.execQuery(q, model).forEach(qs -> {
            List<String> attributesValues = new ArrayList<>();
            attributesValues.add(minValue.toString());
            attributesValues.add(maxValue.toString());
            attributesValues.add(zipcodePrefix);
            attributesValues.add(qs.get("group").toString());
            int count = Integer.parseInt(qs.get("c").toString().split("\\^\\^")[0]);
            mapAfterAnon.put(attributesValues, count);
            //System.out.println(attributesValues + " - " + count);
        });


        q = QueryFactory.create(QueryUtil.createCountAgeZipcodeGroupQuery2(minValue, maxValue, zipcodePrefix));
        QueryUtil.execQuery(q, model).forEach(qs -> {
            List<String> attributesValues = new ArrayList<>();
            attributesValues.add(minValue.toString());
            attributesValues.add(maxValue.toString());
            attributesValues.add(zipcodePrefix);
            attributesValues.add(qs.get("group").toString());
            int count = Integer.parseInt(qs.get("c").toString().split("\\^\\^")[0]);
            mapAfterAnon.computeIfPresent(attributesValues, (k, v) -> (v + count));
            mapAfterAnon.putIfAbsent(attributesValues, count);
            //System.out.println(attributesValues + " - " + count);
        });

    }



    //TODO AU lieu d'itérer sur tous les SA, zip, etc (certaines combinaisons n'existent pas : null pointer), itérer sur mapBeforeAnon
    public static void evaluateCorrelationAgeZipAfterAnon(List<String> predicates, Map<List<String>, Integer> mapBeforeAnon, List<String> zipcodes, Model model) {
        int count1 = 0, count2 = 0;
        double errors1 = 0.0, errors2 = 0.0;
        double relativeErrors1 = 0.0, relativeErrors2 = 0.0;
        int totalExpectedResults1 = 0, totalExpectedResults2 = 0;

        /*
        Integer minValue, maxValue;
        List<Integer> ages = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90);
*/

        Map<String, Integer> mapAttributeToCardinality = QueryUtil.getMapAttributeToCardinality(model);
        Map<String, String> mapAttributeToGroup = QueryUtil.getMapAttributeToGroup(model);
        Map<String, Integer> mapGroupCardinality = QueryUtil.getMapGroupCardinality(model);
        Map<List<String>, Integer> mapCountSubjectsGroup = countCorrelationAgeZipGroupAfterAnon(zipcodes, model);

        for (Map.Entry<List<String>, Integer> entry : mapBeforeAnon.entrySet()) {
            List<String> attributes = entry.getKey();
            Integer minValue = Integer.parseInt(attributes.get(0));
            Integer maxValue = Integer.parseInt(attributes.get(1));
            String zipcode = attributes.get(2);
            String sensitiveAttribute = attributes.get(attributes.size() - 1);
            System.out.println("SENSITIVE ATTRIBUTE : " + sensitiveAttribute);


            //printResultHeader(minValue, maxValue);
            count1++;
            //System.out.println(attributes);
            int expectedResult = mapBeforeAnon.get(attributes);
            //System.out.println("EXPECTED : " + expectedResult);
            totalExpectedResults1 += expectedResult;

            int saCardinality = mapAttributeToCardinality.get(sensitiveAttribute);
            String saGroup = mapAttributeToGroup.get(sensitiveAttribute);
            int saGroupCardinality = mapGroupCardinality.get(saGroup);

            //TODO CHANGE
            //int nbSubjects = QueryUtil.countSubjectsWithAgeZipcodeInGroup(minValue, maxValue, zipcode, saGroup, model);
            List<String> k = Arrays.asList(attributes.get(0), attributes.get(1), attributes.get(2), saGroup);
            //System.out.println("KEY : " + k + "\n");
            int nbSubjects = mapCountSubjectsGroup.getOrDefault(k, 0);

            double estimatedResult = nbSubjects * (saCardinality * 1.0 / saGroupCardinality);


            System.out.println("---EXPECTED - ESTIMATED ---  "  + expectedResult + " - " + estimatedResult);
            System.out.println("NBS: " + nbSubjects + " - SC: " + saCardinality + " - SGC: " + saGroupCardinality);


            double difference = expectedResult - estimatedResult;
            if (difference < 0) {
                difference *= -1;
            }
            errors1 += difference;

            double relativeErr = (expectedResult - estimatedResult) / expectedResult;
            if (relativeErr < 0) {
                relativeErr *= -1;
            }
            relativeErrors1 += relativeErr;


            //printResultsAfterAnon(sensitiveAttribute, expectedResult, saCardinality, saGroup, saGroupCardinality, nbSubjects, estimatedResult);

        }
        printFinalResultsAfterAnon(count1, count2, errors1, errors2, relativeErrors1, relativeErrors2, totalExpectedResults1, totalExpectedResults2, 0.0, 0.0);
    }



    public static void evaluateCorrelationAgeZipAfterAnonTwoSA(List<String> predicates, Map<List<String>, Integer> mapBeforeAnon, Model model) {
        int count1 = 0, count2 = 0;
        double errors1 = 0.0, errors2 = 0.0;
        double relativeErrors1 = 0.0, relativeErrors2 = 0.0;
        int totalExpectedResults1 = 0, totalExpectedResults2 = 0;


        //Integer minValue, maxValue;
        //List<Integer> ages = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90);


        Map<String, Integer> mapAttributeToCardinality = QueryUtil.getMapAttributeToCardinality(model);
        Map<String, String> mapAttributeToGroup = QueryUtil.getMapAttributeToGroup(model);
        Map<String, Integer> mapGroupCardinality = QueryUtil.getMapGroupCardinality(model);

        for (Map.Entry<List<String>, Integer> entry : mapBeforeAnon.entrySet()) {
            List<String> attributes = entry.getKey();
            Integer minValue = Integer.parseInt(attributes.get(0));
            Integer maxValue = Integer.parseInt(attributes.get(1));
            String zipcode = attributes.get(2);
            String sensitiveAttribute1 = attributes.get(attributes.size() - 2);
            String sensitiveAttribute2 = attributes.get(attributes.size() - 1);



            //printResultHeader(minValue, maxValue);
            count1++;
            //System.out.println(attributes);
            int expectedResult = mapBeforeAnon.get(attributes);
            totalExpectedResults1 += expectedResult;

            int saCardinality1 = mapAttributeToCardinality.get(sensitiveAttribute1);
            String saGroup1 = mapAttributeToGroup.get(sensitiveAttribute1);
            int saGroupCardinality1 = mapGroupCardinality.get(saGroup1);
            double probaSA1 = saCardinality1 * 1.0 / saGroupCardinality1;

            int saCardinality2 = mapAttributeToCardinality.get(sensitiveAttribute2);
            String saGroup2 = mapAttributeToGroup.get(sensitiveAttribute2);
            int saGroupCardinality2 = mapGroupCardinality.get(saGroup2);
            double probaSA2 = saCardinality2 * 1.0 / saGroupCardinality2;


            int nbSubjects = QueryUtil.countSubjectsWithAgeZipcodeInTwoGroups(minValue, maxValue, zipcode, saGroup1, saGroup2, model);
            double estimatedResult = nbSubjects * probaSA1 * probaSA2;
            /*System.out.println("POLITICS : "  + sensitiveAttribute2 + " -----  POL CARDINALITY : " + saCardinality2 + " -------  POL GROUP CARD : " + saGroupCardinality2);
            System.out.println("NB SUBS : "  + nbSubjects + " -----  PROBA SA1 : " + probaSA1 + " -------  PROBA SA2 : " + probaSA2);
            System.out.println();
*/
            double difference = expectedResult - estimatedResult;
            if (difference < 0) {
                difference *= -1;
            }
            errors1 += difference;

            double relativeErr = (expectedResult - estimatedResult) / expectedResult;
            if (relativeErr < 0) {
                relativeErr *= -1;
            }
            relativeErrors1 += relativeErr;

            //printResultsAfterAnon(sensitiveAttribute, expectedResult, saCardinality, saGroup, saGroupCardinality, nbSubjects, estimatedResult);

        }
        printFinalResultsAfterAnon(count1, count2, errors1, errors2, relativeErrors1, relativeErrors2, totalExpectedResults1, totalExpectedResults2, 0.0, 0.0);
    }

}
