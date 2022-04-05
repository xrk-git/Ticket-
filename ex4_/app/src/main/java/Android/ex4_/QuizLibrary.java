package Android.ex4_;

public class QuizLibrary {
    public static int myScore = 0;
    public static int pass = 0;
    public static int fail = 0;
    public static boolean isPassed = false;

    public String[] myQuizzes = {
            "What movies did Jennifer Lawrence star in?",
            "What is the color of cloud?"
    };

    public String[][] myChoices = {
            {"DARK PHOENIX","NO TIME TO DIE","NARUTO","TOM AND JERRY"},
            {"PINK","WHITE","GOLD","BLACK"}
    };

    private String[] myCorrectAnswers = {"DARK PHOENIX","WHITE"};

    public String getQuizzes(int a) {
        String quiz = myQuizzes[a];
        return quiz;
    }

    public String getChoice1(int a) {
        String choice = myChoices[a][0];
        return choice;
    }

    public String getChoice2(int a) {
        String choice = myChoices[a][1];
        return choice;
    }

    public String getChoice3(int a) {
        String choice = myChoices[a][2];
        return choice;
    }

    public String getChoice4(int a) {
        String choice = myChoices[a][3];
        return choice;
    }

    public String getCorrectAnswer(int a) {
        String answer = myCorrectAnswers[a];
        return answer;
    }

//    public String blankQuestion = "What is the actor name initial of the heroine of The Hunger Games?";

    public static String blankAnswer = "JL";
}
