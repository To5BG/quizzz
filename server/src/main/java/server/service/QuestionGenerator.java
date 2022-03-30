package server.service;

import commons.Activity;
import commons.Question;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import server.api.ActivityController;

import java.util.*;

public class QuestionGenerator {

    private static final List<Question.QuestionType> QUESTION_TYPES = Arrays.stream(Question.QuestionType.values())
            .filter(qt -> qt != Question.QuestionType.UNKNOWN).toList();

    private static final List<Question.QuestionType> SURVIVAL_QN_TYPES = QUESTION_TYPES.stream()
            .filter(qt -> qt != Question.QuestionType.RANGE_GUESS).toList();

    /**
     * Generate a Comparison style question
     *
     * @param activities List of activities to compare
     * @return The question and the list of expected answers
     */
    private static Pair<Question, List<Long>> generateComparisonQuestion(List<Activity> activities) {
        Question q = new Question("Which activity takes the most energy?", "N/A",
                Question.QuestionType.COMPARISON);

        long answerIndex = 0;
        long maxUsage = Integer.MIN_VALUE;

        for (int i = 0; i < activities.size(); ++i) {
            Activity a = activities.get(i);
            q.addAnswerOption(a.title);
            q.addActivityPath(a.image_path);
            if (a.consumption_in_wh > maxUsage) {
                answerIndex = i;
                maxUsage = a.consumption_in_wh;
            }
        }

        return Pair.of(q, List.of(answerIndex));
    }

    /**
     * Generate a multiple choice type question
     *
     * @param activity         The activity to guess the consumption of
     * @param difficultyFactor The difficulty factor of the question
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Long>> generateMultipleChoiceQuestion(Activity activity,
                                                                             double difficultyFactor) {
        Question q = new Question("Guess how much energy the following activity takes\n" + activity.title,
                activity.image_path, Question.QuestionType.MULTIPLE_CHOICE);

        Random rng = new Random();
        long answerOption = rng.nextInt(4);
        for (int i = 0; i < 4; ++i) {
            if (i == answerOption) {
                q.answerOptions.add(activity.consumption_in_wh + " Wh");
            } else {
                long activityCons = activity.consumption_in_wh;
                long randomOption = Math.abs(activityCons - (int) (activityCons * 0.5 / difficultyFactor)) +
                        rng.nextInt((int) (activityCons / difficultyFactor));
                if (randomOption == activity.consumption_in_wh) {
                    randomOption += rng.nextInt((int) (activityCons * 0.5 / difficultyFactor));
                }
                q.addAnswerOption(randomOption + " Wh");
            }
        }
        return Pair.of(q, List.of(answerOption));
    }

    /**
     * Generate an estimation style question
     *
     * @param activity The activity with the consumption to be estimated
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Long>> generateEstimationQuestion(Activity activity) {
        Question q = new Question("Guess how much Wh of energy the following activity takes\n" + activity.title,
                activity.image_path, Question.QuestionType.RANGE_GUESS);

        return Pair.of(q, List.of(activity.consumption_in_wh));
    }

    /**
     * Generate an equivalence style question
     *
     * @param a     The activity being Compared to alternatives
     * @param other Alternative activities instead of the first one
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Long>> generateEquivalenceQuestion(Activity a, List<Activity> other) {
        Question q = new Question(
                "What could you do instead of the following activity to use the same energy?\n" +
                        a.title, a.image_path, Question.QuestionType.EQUIVALENCE);

        long diff = Integer.MAX_VALUE;
        long closestIndex = 0;

        for (int i = 0; i < other.size(); ++i) {
            Activity act = other.get(i);
            q.addAnswerOption(act.title);
            q.addActivityPath(act.image_path);
            long curDiff = Math.abs(a.consumption_in_wh - act.consumption_in_wh);
            if (curDiff < diff) {
                diff = curDiff;
                closestIndex = i;
            }
        }

        return Pair.of(q, List.of(closestIndex));
    }

    /**
     * Generate a question from the 4 basic types
     *
     * @return The question and the list of expected answers
     */
    public static Pair<Question, List<Long>> generateQuestion(double difficultyFactor, ActivityController ctrl) {
        Random rng = new Random();
        return generateTypeQuestion(QUESTION_TYPES.get(rng.nextInt(QUESTION_TYPES.size())), difficultyFactor, ctrl);
    }

    public static Pair<Question, List<Long>>
    generateSurvivalQuestion(double difficultyFactor, ActivityController ctrl) {
        Random rng = new Random();
        return generateTypeQuestion(SURVIVAL_QN_TYPES.get(rng.nextInt(SURVIVAL_QN_TYPES.size())),
                difficultyFactor, ctrl);
    }

    /**
     * Generate a question of the provided type
     *
     * @param type The type of the question to generate
     * @return The question and the list of expected answers
     */
    public static Pair<Question, List<Long>> generateTypeQuestion
    (Question.QuestionType type, double difficultyFactor, ActivityController ctrl) {

        Activity activity = fetchActivity(ctrl);
        if (type == Question.QuestionType.MULTIPLE_CHOICE) {
            return generateMultipleChoiceQuestion(activity, difficultyFactor);
        } else if (type == Question.QuestionType.RANGE_GUESS) {
            return generateEstimationQuestion(activity);
        }

        List<Activity> activities = new ArrayList<>();
        activities.add(activity);
        int attempt = 0;
        while (activities.size() < 4) {

            Activity potential = fetchActivity(ctrl);
            if (attempt >= 5) {
                activities.add(potential);
                continue;
            }
            long potentialCons = potential.consumption_in_wh;
            long pivotCons = activity.consumption_in_wh;

            if (!activities.contains(potential)) {
                if (Math.abs(potentialCons - pivotCons) <= 0.5 * potentialCons / difficultyFactor) {
                    activities.add(potential);
                } else attempt++;
            }
        }

        if (type == Question.QuestionType.EQUIVALENCE) {
            return generateEquivalenceQuestion(activity, activities.subList(1, activities.size()));
        }

        Collections.shuffle(activities);
        return generateComparisonQuestion(activities);
    }

    /**
     * Fetch an activity from the database
     *
     * @param ctrl ActivityController used to get entries
     * @return Randomly taken activities from the database, if one exists
     */
    public static Activity fetchActivity(ActivityController ctrl) {
        var fetchedActivity = ctrl.getRandomActivity();
        if (fetchedActivity.getStatusCode() == HttpStatus.NO_CONTENT) return null;
        return fetchedActivity.getBody();
    }
}

