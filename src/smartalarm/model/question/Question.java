package smartalarm.model.question;

public interface Question {
    String getText();
    AnswerResult checkAnswer(String input);
}
