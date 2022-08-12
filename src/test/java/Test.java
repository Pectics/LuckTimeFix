import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {



    /**
     * parse 1y2mo3w4d5h6m7s to seconds
     * @param timeString string to parse
     * @return long seconds
     */
    public static long parseTimePeriod(String timeString) {
        Pattern relativeDatePattern = Pattern
                .compile("(\\d+(?:[.,]\\d+)?)(mo|[smhdwy])");
        Matcher relativeDateMatcher = relativeDatePattern.matcher(timeString);
        relativeDateMatcher.reset();
        long periodSeconds = 0;
        while (relativeDateMatcher.find()) {
            double time = Double.parseDouble(relativeDateMatcher.group(1));
            String unitStr = relativeDateMatcher.group(2).toLowerCase();
            int unitMultiplier = 0;
            if (unitStr.startsWith("mo")) {
                unitMultiplier = 30 * 24 * 60 * 60;
            } else if (unitStr.startsWith("s")) {
                unitMultiplier = 1;
            } else if (unitStr.startsWith("m")) {
                unitMultiplier = 60;
            } else if (unitStr.startsWith("h")) {
                unitMultiplier = 60 * 60;
            } else if (unitStr.startsWith("d")) {
                unitMultiplier = 24 * 60 * 60;
            } else if (unitStr.startsWith("w")) {
                unitMultiplier = 7 * 24 * 60 * 60;
            } else if (unitStr.startsWith("y")) {
                unitMultiplier = 365 * 24 * 60 * 60;
            }
            periodSeconds += (long) (time * unitMultiplier);
        }
        return periodSeconds;
    }

    public static void main(String[] args) {
        System.out.println(parseTimePeriod("6m1s"));
    }
}
