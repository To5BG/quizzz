package commons;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class QuestionGenerator {

    private static final List<Question.QuestionType> QUESTION_TYPES = Arrays.stream(Question.QuestionType.values())
            .filter(qt -> qt != Question.QuestionType.UNKNOWN).toList();

    /**
     * Generate a Comparison style question
     * @param activities List of activities to compare
     * @return The question and the list of expected answers
     */
    private static Pair<Question, List<Integer>> generateComparisonQuestion(List<Activity> activities) {
        Question q = new Question("Which activity takes the most energy?", "N/A",
                Question.QuestionType.COMPARISON);

        int answerIndex = 0;
        int maxUsage = Integer.MIN_VALUE;

        for (int i = 0; i < activities.size(); ++i) {
            Activity a = activities.get(i);
            q.addAnswerOption(a.title);
            if (Integer.parseInt(a.consumption) > maxUsage) {
                answerIndex = i;
                maxUsage = Integer.parseInt(a.consumption);
            }
        }

        return Pair.of(q, List.of(answerIndex));
    }

    /**
     * Generate a multiple choice type question
     * @param activity The activity to guess the consumption of
     * @param difficultyFactor The difficulty factor of the question
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Integer>> generateMultipleChoiceQuestion(Activity activity,
                                                                                double difficultyFactor) {
        Question q = new Question("Guess how much energy the following activity takes\n" + activity.title,
                activity.imagePath, Question.QuestionType.MULTIPLE_CHOICE);

        Random rng = new Random();
        for (int i = 0; i < 4; ++i) {
            int randomOption = Math.abs((Integer.parseInt(activity.consumption) - (int) (3000/difficultyFactor))
                    + rng.nextInt((int) (6000/difficultyFactor)));
            if(randomOption == Integer.parseInt(activity.consumption)) {
                randomOption += rng.nextInt((int) (3000/difficultyFactor));
            }
            q.addAnswerOption(randomOption + " Wh");
        }
        int answerOption = rng.nextInt(4);
        q.answerOptions.set(answerOption, activity.consumption + " Wh");
        return Pair.of(q, List.of(answerOption));
    }

    /**
     * Generate an estimation style question
     * @param activity The activity with the consumption to be estimated
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Integer>> generateEstimationQuestion(Activity activity) {
        Question q = new Question("Guess how much Wh of energy the following activity takes\n" + activity.title,
                activity.imagePath, Question.QuestionType.RANGE_GUESS);

        return Pair.of(q, List.of(Integer.parseInt(activity.consumption)));
    }

    /**
     * Generate an equivalence style question
     * @param a The activity being Compared to alternatives
     * @param other Alternative activities instead of the first one
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Integer>> generateEquivalenceQuestion(Activity a, List<Activity> other) {
        Question q = new Question(
                "What could you do instead of the following activity to use the same energy?\n" +
                        a.title, a.imagePath, Question.QuestionType.EQUIVALENCE);

        int diff = Integer.MAX_VALUE;
        int closestIndex = 0;

        for (int i = 0; i < other.size(); ++i) {
            Activity act = other.get(i);
            q.addAnswerOption(act.title);
            int curDiff = Math.abs(Integer.parseInt(a.consumption) - Integer.parseInt(act.consumption));
            if (curDiff < diff) {
                diff = curDiff;
                closestIndex = i;
            }
        }

        return Pair.of(q, List.of(closestIndex));
    }

    /**
     * Generate a question from the 4 basic types
     * @return The question and the list of expected answers
     */
    public static Pair<Question, List<Integer>> generateQuestion(double difficultyFactor) {
        Random rng = new Random();
        return generateTypeQuestion(QUESTION_TYPES.get(rng.nextInt(QUESTION_TYPES.size())), difficultyFactor);
    }

    /**
     * Generate a question of the provided type
     * @param type The type of the question to generate
     * @return The question and the list of expected answers
     */
    public static Pair<Question, List<Integer>> generateTypeQuestion
    (Question.QuestionType type, double difficultyFactor) {
        List<Activity> activitybank = List.of(
                new Activity("Act 1", "6650", "img1", "no"),
                new Activity("Act 2", "6800", "img2", "no"),
                new Activity("Act 3", "6500", "img3", "no"),
                new Activity("Act 4", "4000", "img4", "no"),
                new Activity("Act 5", "6000", "img1", "no"),
                new Activity("Act 6", "5700", "img2", "no"),
                new Activity("Act 7", "5000", "img3", "no"),
                new Activity("Act 8", "6600", "img4", "no"),
                new Activity("Act 9", "5500", "img1", "no"),
                new Activity("Act 10", "5100", "img2", "no"),
                new Activity("Act 11", "6100", "img3", "no"),
                new Activity("Act 12", "6400", "img4", "no"));

        Random rng = new Random();

        int pivotindex = rng.nextInt(activitybank.size());
        Activity pivot = activitybank.get(pivotindex);

        if(type == Question.QuestionType.MULTIPLE_CHOICE) {
            return generateMultipleChoiceQuestion(pivot, difficultyFactor);
        }
        else if(type == Question.QuestionType.RANGE_GUESS) {
            return generateEstimationQuestion(pivot);
        }

        List<Activity> valid = activitybank
                .stream()
                .filter(a -> Math.abs(Integer.parseInt(a.consumption) - Integer.parseInt(pivot.consumption))
                        <= 3000/difficultyFactor)
                .filter(a -> !a.equals(pivot))
                .collect(Collectors.toList());

        while (valid.size() < 3) {
            int validindex = rng.nextInt(activitybank.size());
            if (validindex == pivotindex) {
                validindex = (pivotindex + 1) % valid.size();
            }
            valid.add(activitybank.get(validindex));
        }
        Collections.shuffle(valid);

        List<Activity> options = new ArrayList<>();
        for(int i = 0; i < 3; ++i) {
            options.add(valid.get(i));
        }

        if(type == Question.QuestionType.EQUIVALENCE) {
            return generateEquivalenceQuestion(pivot, options);
        }
        else {
            options.add(pivot);
            Collections.shuffle(options);
            return generateComparisonQuestion(options);
        }
    }
}

