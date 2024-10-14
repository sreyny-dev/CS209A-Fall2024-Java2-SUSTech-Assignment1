
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class OlympicsAnalyzer implements OlympicsAnalyzerInterface {
    private final List<String[]> athleteData;
    List<String[]> eventData;
    List<String []> medalTally;
    List<String []> countries;

    public OlympicsAnalyzer(String dataPath) {
        this.athleteData = loadData(dataPath+"/Olympic_Athlete_Bio_filtered.csv");
         this.eventData = loadData(dataPath +"/Olympic_Athlete_Event_Results.csv");
         this.medalTally = loadData(dataPath + "/Olympic_Games_Medal_Tally.csv");
         this.countries = loadData(dataPath + "/Olympics_Country.csv");
    }

    public List<String[]> filterFemaleAthletes() {
        return athleteData.stream()
                .filter(row -> row.length > 2 && "Female".equalsIgnoreCase(row[2])) // Check gender
                .collect(Collectors.toList());
    }


    public List<String[]> filterFemaleGoldMedalResults() {

        List<String[]> femaleAthletes = filterFemaleAthletes();

        List<String []> eventDataResult = eventData;

        Set<String> femaleAthleteIds = femaleAthletes.stream()
                .map(row -> row[0])
                .collect(Collectors.toSet());

        return eventDataResult.stream()
                .filter(row -> {

                    String athleteId = row[7];
                    String medalType = row[9];
                    String isTeam = row[10];

                    return femaleAthleteIds.contains(athleteId) &&
                            "Gold".equalsIgnoreCase(medalType) &&
                            "False".equalsIgnoreCase(isTeam);
                })
                .collect(Collectors.toList());
    }


    @Override
    public Map<String, Integer> topPerformantFemale() {

        List<String[]> femaleGoldMedalResults = filterFemaleGoldMedalResults();

        Map<String, Integer> medalCountMap = new HashMap<>();

        for (String[] result : femaleGoldMedalResults) {
            String athleteName = result[6];
            medalCountMap.put(athleteName, medalCountMap.getOrDefault(athleteName, 0) + 1);
        }

        return medalCountMap.entrySet()
                .stream()
                .sorted((e1, e2) -> {

                    int cmp = e2.getValue().compareTo(e1.getValue());
                    if (cmp != 0) {
                        return cmp;
                    }
                    return e1.getKey().compareTo(e2.getKey());

                })
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }



    @Override
    public Map<String, Float> bmiBySports() {

        Map<String, Set<String>> athleteToSports = new HashMap<>();
        Map<String, List<Float>> sportBmiMap = new HashMap<>();

        for (String[] eventResult : eventData) {

            String athleteId =eventResult[7].trim();
            if (!athleteId.matches("\\d+")) {
                continue;
            }
            String sport = eventResult[3];
            athleteToSports.computeIfAbsent(sport, k -> new HashSet<>()).add(athleteId);
        }
//        for(Map.Entry<String, Set<String>> entry : athleteToSports.entrySet()){
//            System.out.println(entry.getKey()+entry.getValue());
//        }

        for(String[] bio: athleteData){

            String athleteId = bio[0];
            String heightStr = bio[4];
            String weightStr = bio[5];

            if(heightStr == null || heightStr.isEmpty() || weightStr == null || weightStr.isEmpty()){
                continue;
            }
            try{
                float height = Float.parseFloat(heightStr);
                float weight = Float.parseFloat(weightStr);
                if(height > 0 && weight > 0){
                    height = height / 100;
                    float bmi = weight / (height * height);
                    for (Map.Entry<String, Set<String>> entry: athleteToSports.entrySet()){
                        String sport = entry.getKey();
                        Set<String> athletesInSport = entry.getValue();
                        if(athletesInSport.contains(athleteId)){
                            sportBmiMap.computeIfAbsent(sport, k -> new ArrayList<>()).add(bmi);
                        }
                    }
                }
            }
            catch(NumberFormatException e){
                continue;
            }
        }
        Map<String, Float> sportAverageBmi=new HashMap<>();
        for(Map.Entry<String, List<Float>> entry: sportBmiMap.entrySet()){
            String sport = entry.getKey();
            List<Float> bmis = entry.getValue();
            float averageBmi = (float) bmis.stream()
                    .mapToDouble(Float::doubleValue).average().orElse(0.0);
            sportAverageBmi.put(sport, Math.round(averageBmi * 10.0f)/10.0f);
        }
        return sportAverageBmi.entrySet().stream()
                .sorted(Map.Entry.<String, Float> comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1,e2)->e1,
                        LinkedHashMap::new));
    }

    @Override
    public Map<String, Set<Integer>> leastAppearedSport() {

        Map<String, Set<Integer>> appearSport = new HashMap<>();

        for(String[] result : eventData){
            String[] date = result[0].split(" ");
            if (date.length < 2 || !date[0].matches("\\d{4}")) {
               continue;
            }
            int year =Integer.parseInt(date[0]);
            String season = date[1];
            String sport =result[3];
            if(season.equalsIgnoreCase("summer")){
                appearSport.computeIfAbsent(sport, k -> new HashSet<>()).add(year);
            }
        }
        return appearSport.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Set<Integer>>, Integer> comparing(entry ->
                        entry.getValue().size()).thenComparing(Map.Entry::getKey))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2)->e1,
                        LinkedHashMap::new
                ));

    }

    @Override
    public Map<String, Integer> winterMedalsByCountry() {

        Map<String, Integer> medalCountByCountryMap= new HashMap<>();

        medalTally.stream()
                .filter(row -> {
                    String[] date = row[0].split(" ");
                    if (date.length < 2 || !date[0].matches("\\d{4}")) {
                        return false; // Skip rows with invalid or missing year
                    }
                    int year = Integer.parseInt(date[0]);
                    String season = date[1];

                    return year >= 2000 && season.equalsIgnoreCase("winter")  ;
                })
                .forEach(row->{
                    String countryCode = row[4];
                    int medal = Integer.parseInt(row[8]);
                    medalCountByCountryMap.put(countryCode, medalCountByCountryMap.getOrDefault(countryCode, 0)+medal);
                });


        return medalCountByCountryMap.entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    int compare = e2.getValue().compareTo(e1.getValue());
                    if (compare == 0) {
                        return e1.getKey().compareTo(e2.getKey());
                    }
                    return compare;
                })
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
        }


    @Override
    public Map<String, Integer> topCountryWithYoungAthletes() {
        int currentYear = 2020;

        //filter athletes from 2020 summer game
        Set<Integer> athleteId2020= eventData.stream()
                .filter(row -> {
                    String[] date =row[0].split(" ");
                    if (date.length < 2 || !date[0].matches("\\d{4}")) {
                        return false; // Skip rows with invalid or missing year
                    }
                    int year = Integer.parseInt(date[0].trim());
                    String season = date[1];
                    return year == 2020 && season.equalsIgnoreCase("summer");
                })
                .map(row -> row[7])
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        return athleteData.stream()
                .filter(athlete -> {
                    try {
                        int athleteId = Integer.parseInt(athlete[0]); // Convert athlete[0] to Integer
                        return athleteId2020.contains(athleteId);
                    } catch (NumberFormatException e) {
                        return false; // Skip invalid athlete IDs
                    }
                })
                .collect(Collectors.groupingBy(
                        athlete -> athlete[6],
                        Collectors.collectingAndThen(
                                Collectors.averagingInt(athlete -> {
                                    String[] dateParts = athlete[3].split(" ");
                                    int birthYear = Integer.parseInt(dateParts[2]);
                                    return currentYear - birthYear;
                                }),
                                avg -> (int) Math.round(avg) // Round to nearest integer
                        )
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .thenComparing(Map.Entry::getKey))
                .limit(10) // Limit to top 10 countries
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    public List<String[]> loadData(String filePath) {
        List<String[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip lines that start with "
                if (line.startsWith("\"")) {
                    continue; // Skip this line and move to the next one
                }
                // Process each line, handle quotes manually
                List<String> row = parseLine(line);
                data.add(row.toArray(new String[0]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    private List<String> parseLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (currentChar == '"') {
                inQuotes = !inQuotes; // Toggle the inQuotes flag when encountering a quote
            } else if (currentChar == ',' && !inQuotes) {
                // If not inside quotes, treat comma as a separator
                result.add(sb.toString().trim());
                sb.setLength(0); // Reset the StringBuilder for the next cell
            } else {
                // Append the character (either inside quotes or outside)
                sb.append(currentChar);
            }
        }

        // Add the last part of the line (after the final comma)
        result.add(sb.toString().trim());
        return result;
    }



}