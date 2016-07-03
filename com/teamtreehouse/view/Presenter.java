package com.teamtreehouse.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Arrays;

/**
 * Encapsulates i/o and provides helper methods
 * for handling menus and numerical input
 */
public class Presenter {
    private BufferedReader mInput;
    private PrintWriter mOutput;

    public Presenter() {
        mInput = new BufferedReader(new InputStreamReader(System.in));
        mOutput = new PrintWriter(System.out);
    }
    
    public void flush(){ mOutput.flush(); }

    /**
     * Print the start of a new screen.
     * Intended to help prevent user's disorientation when many lines are 
     * printed at once
     */
    public void newScreen() {
	// Console cannot clear the screen, so print a separator
        //   to show start of new section.
        mOutput.println("\n========================================\n");
    }

    /**
     * Will print a line of dashes(-) to the screen.
     * (e.g. as an underline)
     * @param count number of dashes to print (e.g. 3 = "---")
     */
    public void printDashes(int count) {
        char[] result = new char[count];
        Arrays.fill(result, '-');
        mOutput.printf("%s\n", new String(result));
    }

    /**
     * returns the index of the option chosen by user from an array of
     * options passed in.
     * @param menuTitle text to display before menu options
     * @param menuOptions List of options to display
     * @return index of option from menuOptions parameter that was chosen by user
     * @throws IOException from underlying input stream
     */
    public int presentMenu(String menuTitle, List<String> menuOptions) throws IOException {
        String[] options = new String[menuOptions.size()];
        menuOptions.toArray(options);
        return presentMenu(menuTitle, options);
    }

    /**
     * returns the index of the option chosen by user from the array of
     * options passed in.
     * @param menuTitle text to display before menu options
     * @param menuOptions array of options to display
     * @return index of option from menuOptions parameter that was chosen by user
     * @throws IOException from underlying input stream
     */
    public int presentMenu(String menuTitle, String[] menuOptions) throws IOException {
        int optionChosen;
        int numOptions = menuOptions.length;

        //newScreen();
        mOutput.printf("%s\n", menuTitle);
        printDashes(menuTitle.length());
        for (int i = 0; i < numOptions; i++) {
            // Note: numbers are padded to 2, if > 99 options, all options 100+ 
            //    will be misalligned from options < 100.
            //    Could change to have pad determined at runtime by log10, if needed
            mOutput.printf("%2s) %s\n", i + 1, menuOptions[i]);
        }
        mOutput.println();

        String msg = String.format("Please select an option(1-%d): ",
                numOptions);

        while (true) {
            optionChosen = readInt(msg);
            if (optionChosen < 1)
                continue;
            if (optionChosen > numOptions)
                continue;
            return optionChosen - 1;
        } // loop
    }

    /**
     * Will print text to the screen
     * @param msg text to be printed
     */
    public void print(String msg) {
        mOutput.print(msg);
    }

    /**
     * Will print text to the screen ending with a new line
     * @param msg text to be printed
     */
    public void println(String msg) {
        mOutput.println(msg);
    }

    /**
     * Will start a new line
     */
    public void println() {
        mOutput.println();
    }

    /**
     * Display prompt to user and wait for user to respond.
     * (i.e. pause until user presses [Enter])
     */
    public void waitForUser() {
        waitForUser(true);
    }

    /**
     * Wait for user to respond.
     * (i.e. pause until user presses [Enter])
     * @param showPrompt True to show prompt informing user that the 
     * program has paused.
     * False to pause without showing a prompt
     */
    public void waitForUser(boolean showPrompt) {
        if (showPrompt) {
            mOutput.print("\n(press [Enter] to continue)");
        }
        mOutput.flush();
        try {
            mInput.readLine();
        } catch (IOException e) {
            mOutput.println("An IOException was thrown while waiting for user input.\n\n");
            mOutput.println(e);
            mOutput.println("\n\n");
            // Execution will continue as though user had pressed [Enter]
        }            
    }

    /**
     * Prompt user and return response as a string
     * if input is not acceptable (i.e. user enters blank line)
     *   will loop until valid input is received
     * @param msg prompt to show for user
     * @return response entered by user
     * @throws IOException 
     */
    public String readString(String msg) throws IOException {
        String result;
        do {
            mOutput.print(msg);
            mOutput.flush();
            result = mInput.readLine();
        } while (result.length() < 1);
        return result;
    }


    /**
     * Prompt user and return response as an int
     * if input is not acceptable (i.e. user enters blank line,
     *   user enters something other than a number)
     *   will loop until valid input is received
     * @param msg prompt to show for user
     * @return number entered by user
     * @throws IOException 
     */
    public int readInt(String msg) throws IOException {
        String result;
        while (true) {
            mOutput.print(msg);
            mOutput.flush();
            result = mInput.readLine();
            try {
                return Integer.parseInt(result);
            } catch (Exception ex) {

            }
        }
    }
}
