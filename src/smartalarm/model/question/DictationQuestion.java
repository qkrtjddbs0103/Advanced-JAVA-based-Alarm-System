package smartalarm.model.question;

import java.util.Random;

public class DictationQuestion implements Question {

    private static final String[] WORDS = {
        "Awake", "Morning", "Sunrise", "Vigor", "Vitality",
        "Energy", "Radiance", "Productive", "Growth", "Ambition",
        "Drive", "Focus", "Purpose", "Goal", "Discipline",
        "Achievement", "Momentum", "Consistency", "Clarity", "Awakening",
        "Refresh", "Success", "Optimize", "Mastery", "Potential",
        "Action", "Execute", "Inspired", "Bloom", "Thrive"
    };

    private final String word;

    public DictationQuestion(Random rng) {
        this.word = WORDS[rng.nextInt(WORDS.length)];
    }

    @Override
    public String getText() {
        return "[ " + word + " ]";
    }

    @Override
    public AnswerResult checkAnswer(String input) {
        if (input == null || input.isBlank()) return AnswerResult.INVALID_FORMAT;
        return input.equalsIgnoreCase(word) ? AnswerResult.CORRECT : AnswerResult.INCORRECT;
    }
}
