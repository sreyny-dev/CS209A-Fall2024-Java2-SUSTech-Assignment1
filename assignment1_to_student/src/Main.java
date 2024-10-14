
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {

        String athletesPath = "D:/SUSTech/Year4-Sem1/cs209a-java2/Assignment/A1/assignment1_to_student/src/local_test_data/";

        OlympicsAnalyzer analyzer = new OlympicsAnalyzer(athletesPath);

//        Map<String, Integer> topPerformers = analyzer.topPerformantFemale();
//
        Map<String, Float> bmiSport=analyzer.bmiBySports();
//
//        Map<String, Set<Integer>> appearedSport=analyzer.leastAppearedSport();

//        Map<String, Integer> winterMedalByCountry =analyzer.winterMedalsByCountry();
//        Map<String, Integer> topCountryWithYoungAthletes = analyzer.topCountryWithYoungAthletes();
        for (Map.Entry<String, Float> entry : bmiSport.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }

//        for(Map.Entry<String, Set<Integer>> entry : appearedSport.entrySet()){
//
//            System.out.println(entry.getKey() + entry.getValue());
//        }

    }
}
