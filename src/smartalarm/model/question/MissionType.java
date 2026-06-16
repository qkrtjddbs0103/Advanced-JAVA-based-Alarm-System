package smartalarm.model.question;

public enum MissionType {
    MATH("Math"), DICTATION("Dictation"), BOTH("Both");

    public final String label;
    MissionType(String label) { this.label = label; }

    public MissionType next() {
        return switch (this) {
            case MATH      -> DICTATION;
            case DICTATION -> BOTH;
            case BOTH      -> MATH;
        };
    }
}
