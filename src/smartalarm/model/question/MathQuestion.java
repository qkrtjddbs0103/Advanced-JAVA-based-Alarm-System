package smartalarm.model.question;

import java.util.Random;

public class MathQuestion implements Question {

    private final int correctAnswer;
    private final String text;

    public MathQuestion(Random rng) {
        int a = rng.nextInt(50) + 1;
        int b = rng.nextInt(50) + 1;
        int op = rng.nextInt(4);
        String symbol;
        int answer;
        switch (op) {
            case 0 -> { symbol = "+"; answer = a + b; }
            case 1 -> { symbol = "-"; answer = a - b; }
            case 2 -> { symbol = "*"; answer = a * b; }
            default -> {
                b = rng.nextInt(9) + 2;
                a = b * (rng.nextInt(10) + 1);
                symbol = "/";
                answer = a / b;
            }
        }
        this.correctAnswer = answer;
        this.text = a + "  " + symbol + "  " + b + "  =  ?";
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public AnswerResult checkAnswer(String input) {
        try {
            int given = Integer.parseInt(input.trim());
            return given == correctAnswer ? AnswerResult.CORRECT : AnswerResult.INCORRECT;
        } catch (NumberFormatException ex) {
            return AnswerResult.INVALID_FORMAT;
        }
    }
}
