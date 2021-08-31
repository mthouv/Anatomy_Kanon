import algorithm.Algorithm;
import evaluation.Evaluation;
import kanon.Anonymization;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import query.QueryUtil;
import reader.Reader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {



    private enum Mode {
        GLOBAL,
        GROUP
    }


    public static void main(String[] args) throws MissingArgumentException, IOException {

        Dataset dataset = null;


        Model model = ModelFactory.createDefaultModel();

        boolean before = false;
        boolean after = false;


        List<String> predicatesWithTaxonomy = new ArrayList<>();
        List<String> predicatesNoTaxonomy = new ArrayList<>();
        List<String> sensitivePredicates = new ArrayList<>();
        List<String> nonSensitivePredicates = new ArrayList<>();
        List<String> allPredicates = new ArrayList<>();

        int k = 2;
        List<String> numericalPredicates = new ArrayList<>();
        List<String> literalPredicates = new ArrayList<>();
        List<String> hierarchies = new ArrayList<>();
        Mode mode = Mode.GLOBAL;
        boolean produceCsv = false;
        boolean produceOutput = false;
        String csvPath = null;

        boolean evalZipcode = false;
        int zipcodeGeneralization = 0;
        boolean evalTwoSa = false;
        Map<List<String>, Integer> mapBeforeAnon = new HashMap<>();
        List<String> predicates = new ArrayList<>();
        String beforeAnonPath = null;

        double start = System.currentTimeMillis();
        double totalTime = 0.0;

        String arg;
        try {
            for (int i = 0; i < args.length; i++) {
                arg = args[i++];
                System.out.println("Option : " + arg);
                switch (arg) {
                    case "-d":
                        before = true;
                        while (i < args.length && !args[i].startsWith("-")){
                            Reader.readModelFromDirectory(args[i], model, null);
                            System.out.println(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-f":
                        before = true;
                        while (i < args.length && !args[i].startsWith("-")){
                            model.read(args[i]);
                            System.out.println(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-tdb":
                        before = true;
                        System.out.println(args[i]);
                        String directory = args[i];
                        dataset = TDBFactory.createDataset(directory);
                        model = dataset.getDefaultModel();
                        System.out.println("SIZE :" + model.size());
                        break;
                    case "-pt":
                        while (i < args.length && !args[i].startsWith("-")){
                            predicatesWithTaxonomy.add(args[i]);
                            System.out.println(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-p":
                        while (i < args.length && !args[i].startsWith("-")){
                            System.out.println(args[i]);
                            predicatesNoTaxonomy.add(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-nsp":
                        while (i < args.length && !args[i].startsWith("-")){
                            System.out.println(args[i]);
                            nonSensitivePredicates.add(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-k":
                        k = Integer.parseInt(args[i]);
                        System.out.println(args[i]);
                        break;
                    case "-num_pred":
                        while (i < args.length && !args[i].startsWith("-")){
                            numericalPredicates.add(args[i]);
                            System.out.println(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-lit_pred":
                        while (i < args.length && !args[i].startsWith("-")){
                            literalPredicates.add(args[i]);
                            System.out.println(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-hierarchies":
                        while (i < args.length && !args[i].startsWith("-")){
                            hierarchies.add(args[i]);
                            System.out.println(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-produce_csv":
                        produceCsv = true;
                        i--;
                        break;
                    case "-csv_path":
                        csvPath = args[i];
                        break;
                    case "-group":
                        mode = Mode.GROUP;
                        i--;
                        break;
                    case "-predicates":
                        while (i < args.length && !args[i].startsWith("-")){
                            predicates.add(args[i]);
                            System.out.println(args[i]);
                            i++;
                        }
                        i--;
                        break;
                    case "-zipcode":
                        evalZipcode = true;
                        zipcodeGeneralization = Integer.parseInt(args[i]);
                        break;
                    case "-two_SA":
                        evalTwoSa = true;
                        i--;
                        break;
                    case "-out":
                        beforeAnonPath = args[i];
                        System.out.println(args[i]);
                        break;
                    default:
                        System.out.println("Wrong option : " + arg);
                        break;
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        // TODO
        //if (!before || !after) {
        //    throw new MissingArgumentException("Pre-anonymisation and post-anonymisation graphs must be provided");
        //}

        sensitivePredicates.addAll(predicatesWithTaxonomy);
        sensitivePredicates.addAll(predicatesNoTaxonomy);
        allPredicates.addAll(sensitivePredicates);
        allPredicates.addAll(nonSensitivePredicates);


        List<String> qidPredicates = new ArrayList<>();
        qidPredicates.addAll(numericalPredicates);
        qidPredicates.addAll(literalPredicates);

        List<String> columnNames = new ArrayList<>(Arrays.asList("id"));
        columnNames.addAll(numericalPredicates);
        columnNames.addAll(literalPredicates);

        List<Boolean> isNumerical = new ArrayList<>();
        numericalPredicates.forEach(p -> isNumerical.add(true));
        literalPredicates.forEach(p -> isNumerical.add(false));


        if (dataset != null) {
            dataset.begin(ReadWrite.WRITE);
        }

        double elapsed = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println("LOADING : " + elapsed);
        totalTime += elapsed;
        start = System.currentTimeMillis();

        final int zipGen = zipcodeGeneralization;
        List<String> zipcodes = QueryUtil.getZipcodes(model).stream().map(z -> {
            int prefixSize = z.length() - zipGen;
            return z.substring(0, prefixSize);
        }).distinct().collect(Collectors.toList());


        if (evalZipcode) {
            mapBeforeAnon = Evaluation.evaluateCorrelationAgeZipBeforeAnon(predicates, zipcodes , model);

            elapsed = (System.currentTimeMillis() - start) / 1000.0;
            System.out.println("CREATING MAP BEFORE : " + elapsed);
            totalTime += elapsed;
            start = System.currentTimeMillis();


            Algorithm.executeAnatomy(dataset, model, predicatesWithTaxonomy, predicatesNoTaxonomy);
            if (mode == Mode.GLOBAL) {
                Anonymization.globalGeneralization(model, k, qidPredicates, columnNames, hierarchies, isNumerical, produceCsv, csvPath);
            }
            else {
                Anonymization.generalizationByGroup(model, k, qidPredicates, columnNames, hierarchies, isNumerical, produceCsv, csvPath);
            }

            elapsed = (System.currentTimeMillis() - start) / 1000.0;
            System.out.println("ANON TIME : " + elapsed);
            totalTime += elapsed;
            start = System.currentTimeMillis();

            if (evalTwoSa) {
                Evaluation.evaluateCorrelationAgeZipAfterAnonTwoSA(predicates, mapBeforeAnon, model);
            }
            else {
                Evaluation.evaluateCorrelationAgeZipAfterAnon(predicates, mapBeforeAnon, zipcodes, model);
            }

        }
        else {
            mapBeforeAnon = Evaluation.evaluateCorrelationAgeBeforeAnon(predicates, model);

            elapsed = (System.currentTimeMillis() - start) / 1000.0;
            totalTime += elapsed;
            System.out.println("CREATING MAP BEFORE : " + elapsed);
            start = System.currentTimeMillis();

            Algorithm.executeAnatomy(dataset, model, predicatesWithTaxonomy, predicatesNoTaxonomy);
            if (mode == Mode.GLOBAL) {
                Anonymization.globalGeneralization(model, k, qidPredicates, columnNames, hierarchies, isNumerical, produceCsv, csvPath);
            }
            else {
                Anonymization.generalizationByGroup(model, k, qidPredicates, columnNames, hierarchies, isNumerical, produceCsv, csvPath);
            }

            elapsed = (System.currentTimeMillis() - start) / 1000.0;
            System.out.println("ANON TIME : " + elapsed);
            totalTime += elapsed;
            start = System.currentTimeMillis();

            if (evalTwoSa) {
                System.out.println("AFTER AGE TWO SA\n");
            }
            else {
                Evaluation.evaluateCorrelationAgeAfterAnon(predicates, mapBeforeAnon, model);
            }
        }
        elapsed = (System.currentTimeMillis() - start) / 1000.0;
        totalTime += elapsed;
        System.out.println("EVAL AFTER : " + elapsed);

        if (dataset != null) {
            dataset.abort();
            dataset.end();
        }

        System.out.println("TOTAL TIME ==>  " + totalTime);

    }

}
