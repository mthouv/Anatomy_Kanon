package evaluation;

public class Queries {


    public static String query_20_30_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 20 && ?o < 30) .\n" +
            "}";

    public static String query_20_30_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 20 && ?o < 30) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 20 && ?max < 30) .\n" +
            "   }\n" +
            "}";



    public static String query_30_40_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 30 && ?o < 40) .\n" +
            "}";

    public static String query_30_40_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 30 && ?o < 40) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 30 && ?max < 40) .\n" +
            "   }\n" +
            "}";


    public static String query_40_50_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 40 && ?o < 50) .\n" +
            "}";

    public static String query_40_50_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 40 && ?o < 50) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 40 && ?max < 50) .\n" +
            "   }\n" +
            "}";


    public static String query_50_60_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 50 && ?o < 60) .\n" +
            "}";

    public static String query_50_60_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 50 && ?o < 60) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 50 && ?max < 60) .\n" +
            "   }\n" +
            "}";



    public static String query_60_70_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 60 && ?o < 70) .\n" +
            "}";

    public static String query_60_70_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 60 && ?o < 70) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 60 && ?max < 70) .\n" +
            "   }\n" +
            "}";



    public static String query_70_80_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 70 && ?o < 80) .\n" +
            "}";

    public static String query_70_80_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 70 && ?o < 80) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 70 && ?max < 80) .\n" +
            "   }\n" +
            "}";


    public static String query_80_90_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 80 && ?o < 90) .\n" +
            "}";

    public static String query_80_90_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 80 && ?o < 90) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 80 && ?max < 90) .\n" +
            "   }\n" +
            "}";


    public static String query_90_100_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 90 && ?o <= 100) .\n" +
            "}";

    public static String query_90_100_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 90 && ?o <= 100) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 90 && ?max <= 100) .\n" +
            "   }\n" +
            "}";


    public static String query_20_40_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 20 && ?o < 40) .\n" +
            "}";

    public static String query_20_40_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 20 && ?o < 40) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 20 && ?max < 40) .\n" +
            "   }\n" +
            "}";


    public static String query_40_60_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 40 && ?o < 60) .\n" +
            "}";

    public static String query_40_60_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 40 && ?o < 60) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 40 && ?max < 60) .\n" +
            "   }\n" +
            "}";


    public static String query_60_80_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 60 && ?o < 80) .\n" +
            "}";

    public static String query_60_80_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 60 && ?o < 80) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 60 && ?max < 80) .\n" +
            "   }\n" +
            "}";


    public static String query_80_100_1 = "select ?s where { \n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "    FILTER(?o >= 80 && ?o <= 100) .\n" +
            "}";

    public static String query_80_100_2 = "select ?s where { \n" +
            "{\n" +
            "?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?o .\n" +
            "FILTER(?o >= 80 && ?o <= 100) .\n" +
            "}\n" +
            "UNION\n" +
            "   {\n" +
            "   ?s <http://swat.cse.lehigh.edu/onto/univ-bench.owl#age> ?b .\n" +
            "   ?b <http://minValue> ?min .\n" +
            "   ?b <http://maxValue> ?max .\n" +
            "   FILTER(?min >= 80 && ?max <= 100) .\n" +
            "   }\n" +
            "}";

}
