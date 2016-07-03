import com.teamtreehouse.model.*;
import com.teamtreehouse.view.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class LeagueManager {

    private Presenter mPresenter;
    private Set<Player> mUnassignedPlayers;
    private Map<String, Team> mTeams;
    private int mMaxTeams;

    public LeagueManager(Presenter presenter) {
        mPresenter = presenter;
        Player[] players = Players.load();
        mMaxTeams = players.length;
        mUnassignedPlayers = new TreeSet(Arrays.asList(players));
        mTeams = new TreeMap<>();
        // Note: if functionality added to save teams, when teams are loaded
        //    will need to remove players from mUnassignedPlayers 
        //    which are already on a team.
        

        // For testing purposes
        /* Generate teams * /
        mTeams.put("Birch Logs".toLowerCase(), new Team("Birch Logs", "Druid McFlowers"));
        mTeams.put("Apples".toLowerCase(), new Team("Apples", "That Guy"));
        mTeams.put("Carnivores".toLowerCase(), new Team("Carnivores", "Butch Headthumper"));
        mTeams.put("Wolves".toLowerCase(), new Team("Wolves", "Wolfie Wolferson"));

        /* Randomly assign all players (note: needs teams or will crash) * /
        Random rand = new Random();
        Team[] theTeams = mTeams.values().toArray(new Team[0]);
        int teamCount = theTeams.length;
        for (Player p : players) {
            theTeams[rand.nextInt(teamCount)].addPlayer(p);
            mUnassignedPlayers.remove(p);
        }
        /**/
    }

    public static void main(String[] args) {
        try {
            Presenter presenter = new Presenter();
            LeagueManager app = new LeagueManager(presenter);
            app.mainMenu();
            presenter.flush();
        } catch (Exception e) {
            System.out.println("\n\nAn exception has been thrown and the application must quit.\n");
            e.printStackTrace();
        }
    }

    public void mainMenu() throws IOException {
        String menuTitle = "Please select an option:";
        String[] menuOptions = {"Manage Teams", "Print Team Roster", "Exit"};

        int selected;

        while (true) {
            printHeader();
            selected = mPresenter.presentMenu(menuTitle, menuOptions);
            switch (selected) {
                case 0: // "Manage Teams"
                    manageTeams();
                    break;
                case 1: // "Print Team Roster"
                    printTeamRoster();
                    break;
                default: // "Exit"
                    mPresenter.println("Exiting...");
                    return;
            }
        }
    }

    private void printHeader() {
        mPresenter.newScreen();
        mPresenter.println("Welcome to the Soccer League Organizer");
        mPresenter.println("--------------------------------------");
        mPresenter.println();
    }

    private void manageTeams() throws IOException, UnsupportedOperationException {
        String menuTitle = "Please select an option:";
        String[] menuOptions = {
            "Create New Team", //Note: missing options to rename and delete teams
            "Add Player to Team",
            "Remove Player from Team",
            "View Team Height Report (single team)",
            "View League Balance Report (all teams)",
            "Main Menu"
        };

        int numPlayers;
        int numTeams;
        int selected;

        while (true) {
            printHeader();
            numPlayers = mUnassignedPlayers.size();
            numTeams = mTeams.size();
            mPresenter.println(String.format(
                    "There are %s players which need to be assigned to a team.",
                    numPlayers > 0 ? numPlayers : "no"));
            mPresenter.println(String.format(
                    "There are %s teams.",
                    numTeams > 0 ? numTeams : "no"));
            mPresenter.println();
            selected = mPresenter.presentMenu(menuTitle, menuOptions);
            switch (selected) {
                case 0: // "Create New Team"
                    createNewTeam();
                    break;
                case 1: // "Add Player to Team"
                    addPlayerToTeam();
                    break;
                case 2: // "Remove Player from Team"
                    removePlayerFromTeam();
                    break;
                case 3: // "View Team Height Report (single team)"
                    viewTeamHeightReport();
                    break;
                case 4: // "View League Balance Report (all teams)"
                    viewLeagueBalanceReport();
                    break;
                default: // "Main Menu"
                    return;
            } // switch
        } // while
    }

    private void createNewTeam() throws IOException {
        mPresenter.println();
        if (mTeams.size() >= mMaxTeams) {
            mPresenter.println("You may not create a new team at this time.");
            mPresenter.println(String.format("There are %d players this season.", mMaxTeams));
            mPresenter.println(String.format("There are %d teams already created.", mTeams.size()));
            mPresenter.waitForUser();
            return;
        }
        mPresenter.println("Enter \"cancel\" to go back without creating a new team.");
        String teamName = mPresenter.readString("Please enter a name for the new team: ");
        String teamNameLower = teamName.toLowerCase();
        if (teamNameLower.equals("cancel"))
            return;
        if (teamNameLower.equals("\"cancel\""))
            return;
        boolean alreadyExists = mTeams.containsKey(teamNameLower);
        if (alreadyExists) {
            mPresenter.print(String.format("\nError: a team already exists with the name \"%s\".", teamName));
            mPresenter.waitForUser();
        } else {
            String coachName = mPresenter.readString("Please enter the coach's name: ");
            String coachNameLower = coachName.toLowerCase();
            if (coachNameLower.equals("cancel"))
                return;
            if (coachNameLower.equals("\"cancel\""))
                return;
            mTeams.put(teamNameLower, new Team(teamName, coachName));
        }
    }

    private void addPlayerToTeam() throws IOException {
        if (mUnassignedPlayers.size() < 1) {
            mPresenter.println("\nCannot continue.\nThere are no more unassigned players.");
            mPresenter.waitForUser();
            return;
        }
        if (mTeams.size() < 1) {
            mPresenter.println("\nCannot continue.\nThere are no teams.");
            mPresenter.waitForUser();
            return;
        }
        String menuTitle = "Add a player to a team";
        String[] menuOptions = {"Select a team", "Select a player", "Cancel"};

        int selected;
        Team team;
        Player player;
        StringBuilder prompt = new StringBuilder();

        while (mUnassignedPlayers.size() > 0) {
            printHeader();
            mPresenter.println();
            selected = mPresenter.presentMenu(menuTitle, menuOptions);
            switch (selected) {
                case 0: // "Select a team"
                    team = selectTeam("Select a team for adding a new player.");
                    if (team == null) // user cancelled
                    {
                        return;
                    }
                    prompt.setLength(0);
                    prompt.append(String.format("Select a player to add to the team: \n"));
                    prompt.append(String.format("  Name: %s\n", team.getName()));
                    prompt.append(String.format("  Average Height: %.2f\"\n", team.getAverageHeight()));
                    prompt.append(String.format("  Players: %d experienced\n", team.getCount_ExperiencedPlayers()));
                    prompt.append(String.format("           %d inexperienced\n", team.getCount_InexperiencedPlayers()));
                    player = selectPlayer(prompt.toString(), mUnassignedPlayers);
                    if (player == null) // user cancelled
                    {
                        return;
                    }
                    break;
                case 1: // "Select a player"
                    player = selectPlayer("Select a player for adding to a team.", mUnassignedPlayers);
                    if (player == null) // user cancelled
                    {
                        return;
                    }
                    prompt.setLength(0);
                    prompt.append(String.format("Select a team for adding the player: \n"));
                    prompt.append(String.format("  Name: %s, %s\n", player.getLastName(), player.getFirstName()));
                    prompt.append(String.format("  Height: %d\"\n", player.getHeightInInches()));
                    prompt.append(String.format("  Experienced? %s\n", player.isPreviousExperience() ? "Yes" : "No"));
                    team = selectTeam(prompt.toString());
                    if (team == null) // user cancelled
                    {
                        return;
                    }
                    break;
                default: // "Cancel"
                    return;
            } // switch

            team.addPlayer(player);
            mUnassignedPlayers.remove(player);
        } // while
    }

    private Team selectTeam(String prompt) throws IOException {
        Collection<Team> teams = mTeams.values();
        
        int maxNameLength = 0;
        for (Team team : teams) {
            maxNameLength = Math.max(maxNameLength, team.getName().length());
        }
        String format = String.format("%%-%ds  Average Height: %%.2f\"  Experienced: %%2d/%%2d (%%s)",
                maxNameLength);
        
        int numPlayers;
        List<String> optionsText = new ArrayList<>();
        List<Team> optionsValue = new ArrayList<>();
        for (Team team : teams) {
            numPlayers = team.getCount_AllPlayers();
            optionsText.add( // teamMap holds the display string for a team
                    String.format(
                        format, // {name}, Average Height: ##", Experienced: #/## (0.00%)"
                        team.getName(), 
                        team.getAverageHeight(), 
                        team.getCount_ExperiencedPlayers(),
                        numPlayers,
                        numPlayers > 0 ? // if no players, prevent printing "NaN" from div by zero error
                            String.format( // percentage of players with experience
                                "%2.0f%%", 
                                100d * team.getCount_ExperiencedPlayers() / numPlayers
                            ) : 
                            "no players"
                    )
            );
            optionsValue.add(team);
        } // for loop
        
        if ( mTeams.size() < 1)
            optionsText.add("Cancel    (no teams available to select)");
        else
            optionsText.add("Cancel");         
        optionsValue.add(null); // will return null if Cancel selected

        printHeader();
        mPresenter.println();
        int selected = mPresenter.presentMenu(prompt, optionsText);
        return optionsValue.get(selected);
    }

    private Player selectPlayer(String prompt, Set<Player> fromPlayerSet) throws IOException {
        ArrayList<Player> sortedByName = new ArrayList<>(fromPlayerSet); // Sorted by name as default
        ArrayList<Player> sortedByHeight = null; // Note: sortedByHeight isn't created unless it's needed
        ArrayList<Player> currentSort = sortedByName;
        Map<Player,String> playerMap = new HashMap<>(); // playerMap holds the display string for a player
        List<String> optionsText = new ArrayList<>(); // the display strings in playerMap sorted by currentSort
                
        int firstNameLength = 0;
        int lastNameLength = 0;
        for (Player player : fromPlayerSet) {
            firstNameLength = Math.max(firstNameLength, player.getFirstName().length());
            lastNameLength = Math.max(lastNameLength, player.getLastName().length());
        }
        String format = String.format("%%-%ds %%-%ds  Height: %%d\"  Experienced: %%s",
                lastNameLength + 1, firstNameLength);
        
        for (Player player : fromPlayerSet) {
            playerMap.put( // playerMap holds the display string for a player
                player, 
                String.format(
                    format, // {last}, {first}  Height: ##", Experienced: {Yes/No}
                    player.getLastName() + ",",
                    player.getFirstName(),
                    player.getHeightInInches(),
                    player.isPreviousExperience() ? "Yes" : "No"
                )
            );
        }
               
        while (true){ 
            // loop allows user to resort teams by name or height
            printHeader();
            mPresenter.println();

            optionsText.clear();
            for( Player player : currentSort){
                optionsText.add(playerMap.get(player)); // playerMap holds the display string for a player
            }
            
            int optionResortByName = -1;
            int optionResortByHeight = -1;        
            // Note: excluded sorting by experience, deemed clutter
            int optionCancel;

            if ( fromPlayerSet.size() < 1){
                optionCancel = optionsText.size();
                optionsText.add("Cancel    (no players available to select)");
            } else {
                // HACK: insert a space between the list of players and the sortBy options
                String lastPlayerText = optionsText.remove(optionsText.size()-1);
                optionsText.add(lastPlayerText + "\n");
                
                optionResortByName = optionsText.size();
                optionsText.add("(re-sort by last name)"); 
                optionResortByHeight = optionsText.size();
                optionsText.add("(re-sort by height)"); 
                optionCancel = optionsText.size();
                optionsText.add("Cancel"); 
            }

            int selected = mPresenter.presentMenu(prompt, optionsText);
            if ( selected == optionResortByName){
                currentSort = sortedByName;
                continue;
            }
            if ( selected == optionResortByHeight){
                if (sortedByHeight == null){
                    sortedByHeight = new ArrayList<Player>(fromPlayerSet);
                    Collections.sort(
                            sortedByHeight, 
                            //Comparator.comparing(Player::getHeightInInches) // Sort Ascending
                            Comparator.comparing(k -> -1 * k.getHeightInInches()) // Sort Descending                            
                    );
                }
                currentSort = sortedByHeight;
                continue;
            }
            if ( selected == optionCancel)
                return null;
            
            return currentSort.get(selected);
        } // loop
    }

    private void removePlayerFromTeam() throws IOException {
        if (mTeams.size() < 1) {
            mPresenter.println("\nCannot continue.\nThere are no teams.");
            mPresenter.waitForUser();
            return;
        }
        Team team = selectTeam("Select a team to remove a player.");
        if (team == null) // user cancelled
        {
            return;
        }
        if (team.getCount_AllPlayers() < 1) {
            mPresenter.println("\nCannot continue.\nTeam has no players to remove.");
            mPresenter.waitForUser();
            return;
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("Select a player to remove from the team: \n"));
        prompt.append(String.format("  Name: %s\n", team.getName()));
        prompt.append(String.format("  Average Height: %.2f\"\n", team.getAverageHeight()));
        prompt.append(String.format("  Players: %d experienced\n", team.getCount_ExperiencedPlayers()));
        prompt.append(String.format("           %d inexperienced\n", team.getCount_InexperiencedPlayers()));
        Player player = selectPlayer(prompt.toString(), team.getPlayers());
        if (player == null) // user cancelled
        {
            return;
        }
        team.removePlayer(player);
        mUnassignedPlayers.add(player);
    }

    private void viewTeamHeightReport() throws IOException {
        if (mTeams.size() < 1) {
            mPresenter.println("\nCannot continue.\nThere are no teams.");
            mPresenter.waitForUser();
            return;
        }
        while (true) {
            Team team = selectTeam("Select a team to view its report.");
            if (team == null) // user cancelled
            {
                return;
            }
            displayTeamHeightReport(team);
        }
    }

    private void displayTeamHeightReport(Team team) {
        printHeader();
        String teamName = team.getName();
        mPresenter.println(teamName);
        mPresenter.printDashes(teamName.length() + 5);
        mPresenter.println(String.format("Coach: %s", team.getCoach()));
        mPresenter.println(String.format("Average Player Height: %.2f\"", team.getAverageHeight()));
        mPresenter.println("Experience:");
        mPresenter.println(String.format(" %2d experienced players", team.getCount_ExperiencedPlayers()));
        mPresenter.println(String.format(" %2d inexperienced players", team.getCount_InexperiencedPlayers()));
        int numPlayers = team.getCount_AllPlayers();
        mPresenter.println(
            String.format(
                " %s of %d players have previous experience", 
                numPlayers > 0 ? // if no players, prevent printing "NaN" from div by zero error
                    String.format( // percentage of players with experience
                        "%2.0f%%", 
                        100d * team.getCount_ExperiencedPlayers() / numPlayers
                    ) : 
                    " 0%",
                numPlayers
            )
        );
        mPresenter.println();
        mPresenter.println();
        
        Map<Integer,Set<Player>> playersByHeight = team.getPlayersGroupedByHeight();
        if (playersByHeight.isEmpty()){
            mPresenter.println("  (no players on this team)");
            mPresenter.waitForUser();
            return;
        }
        
        int firstNameLength = 0;
        int lastNameLength = 0;
        for (Player player : team.getPlayers()) {
            firstNameLength = Math.max(firstNameLength, player.getFirstName().length());
            lastNameLength = Math.max(lastNameLength, player.getLastName().length());
        }
        String detailFormat = String.format("  %%-%ds %%-%ds  Height: %%d\", Experienced: %%s\n",
                lastNameLength + 1, firstNameLength);
                    
        Integer[] heights = playersByHeight.keySet().toArray(new Integer[0]);
        Set<Player> players;
        int height;
        StringBuilder overviewReport = new StringBuilder();
        StringBuilder detailReport = new StringBuilder();
        
        for(int indexHeight = heights.length - 1; indexHeight >= 0; indexHeight--){            
            height = heights[indexHeight];
            players = playersByHeight.get(height);
            
            numPlayers = players.size();
            overviewReport.append(String.format("inches %2d -%2d player%s\n", height, numPlayers, numPlayers == 1 ? "" : "s" ));
                                
            detailReport.append(String.format("%d inches\n", height));
            for (Player player : players) {
                detailReport.append(String.format(detailFormat, //"%-s, %-s  Height: %d\", Experienced: %s"
                        player.getLastName() + ",", player.getFirstName(),
                        player.getHeightInInches(),
                        player.isPreviousExperience() ? "Yes" : "No"));
            }
            detailReport.append("\n");
        }
        
        mPresenter.println("Heights:");
        mPresenter.println("---------");
        mPresenter.print(overviewReport.toString());
        mPresenter.println();
        mPresenter.println("Players:");
        mPresenter.println("---------");
        mPresenter.print(detailReport.toString());
        mPresenter.waitForUser();
    }

    private void viewLeagueBalanceReport() throws IOException {
        printHeader();
        
        boolean includeUnassignedPlayers;
        if (mUnassignedPlayers.size() < 1)
            includeUnassignedPlayers = false;
        else{
            String menuTitle = "Include unassigned players in the League Balance Report?";
            String[] menuOptions = {
                "Yes, include the unassigned players.", 
                "No, only include players already assigend to teams."
            };
            includeUnassignedPlayers = (0 == mPresenter.presentMenu(menuTitle, menuOptions));
        }
        printHeader();
        mPresenter.println("League Balance Report");
        mPresenter.println("-----------------------\n");

        if (!includeUnassignedPlayers && mTeams.size() < 1) { // no teams
            mPresenter.println("Team Name");
            mPresenter.println("-------------");
            mPresenter.println("(no teams)");
            mPresenter.waitForUser();
            return;
        }
        
        Set<Team> teams = new TreeSet(mTeams.values());
        if (includeUnassignedPlayers){
            // Note: Since using a TreeSet instead of a List, the unassigned
            //    players (if included) will be printed at the top of the Report
            //    instead of the bottom (not that that's an issue)
            Team unassignedPlayers = new Team("(unassigned)", "Not a real team.");
            teams.add(unassignedPlayers);
            for( Player player : mUnassignedPlayers)
                unassignedPlayers.addPlayer(player);
        }
        
        int maxNameLength = "Team Name".length(); // minimum column width
        for (Team team : teams) {
            maxNameLength = Math.max(maxNameLength, team.getName().length());
        }
        
        printLeagueBalanceReport_mainReport(teams, maxNameLength);
        printLeagueBalanceReport_heightChart(teams, maxNameLength);
        
        mPresenter.waitForUser();
    }
    
    private void printLeagueBalanceReport_mainReport(Collection<Team> teams, int maxNameLength){
        
        String headerFormat = String.format(" %%-%ds  Average Height  Experienced - Inexperienced Players",
                maxNameLength);
        //                                  {name} ... {height} ... (100%){numExp} - {numInexp} (100% of {total} players)
        String teamFormat = String.format(" %%-%ds    %%5.2f\"          %%6s %%2d -%%2d %%5s of %%2d players)",
                maxNameLength);
        int numPlayers;

        String header = String.format(headerFormat, "Team Name");
        mPresenter.println(header);
        mPresenter.printDashes(header.length());
        
        /**************************************
         * Note: The "How you'll be graded" section said I had to use
         *   "a map like solution to properly report experienced vs. 
         *   inexperienced for each team" ???
         *   I don't really get how a map is useful in this situation, 
         *   maybe I'm just doing it wrong. I suppose if I had separated
         *   the data from the display and passed a Map<Team,Integer>,
         *   or maybe a Map<Team,List<Integer>> into a.. eh.. no, I still 
         *   don't get how a Map is supposed to be used here. Having a method 
         *   on Team that returned a Map<Boolean,Integer> is the only other 
         *   thing I can think of, but that would have been either 
         *   *really* funny or *really* weird so I'll pass on that idea.
         * 
         *   But here's a map anyways, and it's being used to "report 
         *   experienced vs. inexperienced players for each team."
         *   (it's not being used correctly, but it is being used)
         **************************************/
        Map<String,Integer> teamData = new HashMap<>();
        for (Team team : teams) {
            numPlayers = team.getCount_AllPlayers();
            int percentExperienced;
            int percentInexperienced;
            if(numPlayers < 1){
                percentExperienced = 0;
                percentInexperienced = 0;
            } else {
                percentExperienced = 100 * team.getCount_ExperiencedPlayers() / numPlayers;
                percentInexperienced = 100 - percentExperienced;
            }
            
            teamData.put("countExperienced",team.getCount_ExperiencedPlayers());
            teamData.put("countInexperienced",team.getCount_InexperiencedPlayers());
            mPresenter.println(
                String.format(
                    teamFormat, // {name} ... {height} ... (100%) {numExp} - {numInexp} (100% of {total} players)
                    team.getName(), 
                    team.getAverageHeight(),
                    String.format( "(%2d%%)",percentExperienced),
                    teamData.get("countExperienced"), //team.getCount_ExperiencedPlayers(),
                    teamData.get("countInexperienced"), //team.getCount_InexperiencedPlayers(),
                    String.format( "(%2d%%",percentInexperienced), 
                    numPlayers
                )
            );
        }
    }
    
    private void printLeagueBalanceReport_heightChart(Collection<Team> teams, int maxNameLength){
        Map<Team,Map<Integer, Integer>> heightChart = new TreeMap<>();
        Map<Integer,Integer> teamHeightCounts;
        Set<Integer> allHeights = new TreeSet<>();
        
        
        for( Team team : teams){
            teamHeightCounts = new TreeMap<>();
            heightChart.put(team, teamHeightCounts);
            int heightValue;
            Integer heightCount;
            
            for( Player player : team.getPlayers()){
                heightValue = player.getHeightInInches();
                allHeights.add(-1 * heightValue); // store in descending order
                heightCount = teamHeightCounts.get(heightValue);
                if (heightCount == null)
                    heightCount = 0;
                teamHeightCounts.put(heightValue, heightCount + 1);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        String teamNameFormat = String.format(" %%-%ds  ", maxNameLength);
        sb.append(String.format(teamNameFormat, "Team Name"));
        for(int negativeHeightValue : allHeights){
            sb.append(String.format("%2d\" ", -1 * negativeHeightValue));
        }
        mPresenter.println();
        mPresenter.println();
        mPresenter.println("Height Breakdown:");
        mPresenter.println(sb.toString());
        mPresenter.printDashes(sb.length());
        
        for( Team team : teams){
            sb.setLength(0); 
            sb.append(String.format(teamNameFormat, team.getName()));
            
            int heightValue;
            Integer heightCount;
            teamHeightCounts = heightChart.get(team);
            
            for(int negativeHeightValue : allHeights){
                heightValue = -1 * negativeHeightValue;
                heightCount = teamHeightCounts.get(heightValue);
                if ( heightCount == null)
                    sb.append("    "); // Note: prints a blank inplace of a zero
                else
                    sb.append(String.format("%2d  ", heightCount));
            }
            mPresenter.println(sb.toString());
        }
        
    }

    private void printTeamRoster() throws IOException {
        if (mTeams.size() < 1) {
            mPresenter.println("\nCannot continue.\nThere are no teams.");
            mPresenter.waitForUser();
            return;
        }
        while (true) {
            Team team = selectTeam("Select a team to print its roster.");
            if (team == null) // user cancelled
                return;
                    
            // Note: if updated to support printing to printer/file/email/etc,
            //  add menu here to let user select where to print to (screen/printer/file/email/whatever)
            
            printHeader();
            mPresenter.println(String.format("Printing team roster for %s...\n", team.getName()));
            // printing to screen is the only option available at this time
            printTeamRoster_toScreen(team);
            mPresenter.println("\nPrintout complete.");
            mPresenter.waitForUser();
        }
    }

    private void printTeamRoster_toScreen(Team team) {
        mPresenter.println("Team Roster");
        mPresenter.println("-----------------------");
        mPresenter.println(String.format("Team Name: %s", team.getName()));
        mPresenter.println(String.format("Coach: %s", team.getCoach()));
        mPresenter.println();
        mPresenter.println("Players:");
        mPresenter.println("---------");
        
        Set<Player> players = team.getPlayers();
        if (players.size() > 0) {
            int firstNameLength = 0;
            int lastNameLength = 0;
            for (Player player : players) {
                firstNameLength = Math.max(firstNameLength, player.getFirstName().length());
                lastNameLength = Math.max(lastNameLength, player.getLastName().length());
            }
            String format = String.format("%%-%ds %%-%ds  Height: %%d\", Experienced: %%s",
                    lastNameLength + 1, firstNameLength);

            for (Player player : team.getPlayers()) {
                mPresenter.println(String.format(format, //"%-s, %-s  Height: %d\", Experienced: %s"
                        player.getLastName() + ",", player.getFirstName(),
                        player.getHeightInInches(),
                        player.isPreviousExperience() ? "Yes" : "No"));
            }
        } else { // no players
            mPresenter.println("(no players)");
        }
        mPresenter.println("\n");
    }
}
