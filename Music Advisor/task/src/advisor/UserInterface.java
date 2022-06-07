package advisor;

import java.io.IOException;
import java.util.Scanner;

public class UserInterface {

    private Scanner userInput;
    private String[] args;
    private boolean userAuthorized;
    private String accessArg;
    private String resourceArg;

    public UserInterface(String[] args) throws IOException, InterruptedException {
        this.userInput = new Scanner(System.in);
        this.args = args;
        this.userAuthorized = false;

        parseArgs(args);
        runProgram();
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-access")) {
                accessArg = args[i + 1];
            } else if (args[i].equals("-resource")) {
                resourceArg = args[i + 1];
            }
        }
    }

    public void runProgram() throws IOException, InterruptedException {
        AdvisorEngine advisorEngine = new AdvisorEngine(resourceArg);
        Authorizer authorizer = new Authorizer(accessArg, advisorEngine);

        while (userInput.hasNext()) {
            String input = userInput.nextLine();

            if (input.equals("auth")) {
                userAuthorized = authorizer.authorizeUser();
                System.out.println(userAuthorized ? "---SUCCESS---" : "---fail---");
            }

            if (input.equals("exit")) {
                System.out.println("---GOODBYE!---");
                System.exit(0);
            }

            if (userAuthorized) {
                if (input.equals("featured")) {
                    advisorEngine.viewFeatured();
                } else if (input.equals("new")) {
                    System.out.println("---NEW RELEASES---");
                    System.out.println("The Bing Bong Song");
                } else if (input.equals("categories")) {
                    System.out.println("---CATEGORIES---");
                    System.out.println("Top\nMood\nPop");
                } else if (input.contains("playlists")) {
                    String userPlaylists = input.substring(input.indexOf(" "));
                    System.out.println("---" + userPlaylists.toUpperCase() + " PLAYLISTS---");
                    System.out.println("Emo 4ever");
                    System.out.println("Baddest bunnies");
                }
            } else {
                System.out.println("Please, provide access for application.");
            }
        }
    }
}