package com.openlap.AnalyticsMethods.Prototypes.Assignment;

import com.openlap.AnalyticsMethods.Prototypes.Comparators.ValueComparator;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dynamicparam.OpenLAPDynamicParamDataType;
import com.openlap.dynamicparam.OpenLAPDynamicParamFactory;
import com.openlap.dynamicparam.OpenLAPDynamicParamType;
import com.openlap.dynamicparam.OpenLAPDynamicParams;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.OpenLAPDynamicParamException;
import com.openlap.template.AnalyticsMethod;

import java.io.InputStream;
import java.util.*;

/**
 * Created by Arham Muslim
 * on 29-Nov-16.
 */
public class SubmittedSolutions extends AnalyticsMethod {
    public SubmittedSolutions() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("assign_title", OpenLAPColumnDataType.Text, true, "Assignment Title", "List of assignment titles")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("timestamp", OpenLAPColumnDataType.Numeric, true, "Timestamp", "List of timestamps to take the latest grade of user.")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("user", OpenLAPColumnDataType.Text, true, "Submitted By", "List of students who submitted this solution.")
            );

            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("assign_title", OpenLAPColumnDataType.Text, true, "Assignment Title", "List of assignment titles.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("submit_count", OpenLAPColumnDataType.Numeric, true, "Submission count", "Number of students who submitted solution for the assignment.")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                        OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("group_submission", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Group Submission Type", "Consider group submission as one or one submission per student in the group.", "Group Submission", "Group Submission,Per User Submission", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("search_type", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Counting type", "Count all submission updates or only single submission per student/group", "All Submission", "All Submission,Unique Submission", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Count submitted solutions per assignment";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Counts the number of users who submitted solution for each assignment.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    protected void implementationExecution() {
        try {
            String groupSubmission = (String) this.getParams().getParams().get("group_submission").getValue();
            boolean isGroupSubmission = groupSubmission.equals("Group Submission");

            String searchType = (String) this.getParams().getParams().get("search_type").getValue();
            boolean isAllSubmission = searchType.equals("All Submission");

            HashMap<String, Integer> titleTimes = new HashMap<String, Integer>();
            Map<String, Map<String, Integer>> totalSubmissions = new TreeMap<String, Map<String, Integer>>();

            Iterator titles = this.getInput().getColumns().get("assign_title").getData().iterator();
            Iterator timestamps = this.getInput().getColumns().get("timestamp").getData().iterator();
            Iterator users = this.getInput().getColumns().get("user").getData().iterator();

            while (titles.hasNext() && timestamps.hasNext() && users.hasNext()) {
                String title = (String) titles.next();
                Integer timestamp = (Integer) timestamps.next();
                String user = (String) users.next();

                if(title != null && timestamp != null && user != null) {
                    if (titleTimes.containsKey(title)) {
                        if (titleTimes.get(title) > timestamp)
                            titleTimes.put(title, timestamp);
                    } else
                        titleTimes.put(title, timestamp);

                    if (isGroupSubmission) {
                        if (totalSubmissions.containsKey(title)) {
                            Map<String, Integer> userSubmitted = totalSubmissions.get(title);

                            if (userSubmitted.containsKey(user)) {
                                if (isAllSubmission)
                                    userSubmitted.put(user, userSubmitted.get(user) + 1);
                            } else {
                                userSubmitted.put(user, 1);
                            }
                        } else {
                            Map<String, Integer> userSubmitted = new HashMap<String, Integer>();
                            userSubmitted.put(user, 1);
                            totalSubmissions.put(title, userSubmitted);
                        }
                    } else {
                        List<String> groupUsers = Arrays.asList(user.split("\\s*,\\s*"));
                        if (totalSubmissions.containsKey(title)) {
                            Map<String, Integer> userSubmitted = totalSubmissions.get(title);

                            for (String curUser : groupUsers) {
                                if (userSubmitted.containsKey(curUser)) {
                                    if (isAllSubmission)
                                        userSubmitted.put(curUser, userSubmitted.get(curUser) + 1);
                                } else {
                                    userSubmitted.put(curUser, 1);
                                }
                            }
                        } else {
                            Map<String, Integer> userSubmitted = new HashMap<String, Integer>();
                            for (String curUser : groupUsers)
                                userSubmitted.put(curUser, 1);

                            totalSubmissions.put(title, userSubmitted);
                        }
                    }
                }
            }

            Comparator<String> comparator = new ValueComparator<String, Integer>(titleTimes);
            TreeMap<String, Integer> sortedTitleTimes = new TreeMap<String, Integer>(comparator);
            sortedTitleTimes.putAll(titleTimes);

            //Map<String, Integer> sortedTitleTimes = sortByValues(titleTimes);

            Set<Map.Entry<String, Integer>> titleSet = sortedTitleTimes.entrySet();

            for (Map.Entry<String, Integer> keyTitle : titleSet) {

                Set<Map.Entry<String, Integer>> userSubmissions = totalSubmissions.get(keyTitle.getKey()).entrySet();

                int countSubmissions = 0;
                for (Map.Entry<String, Integer> userMark : userSubmissions)
                    countSubmissions += userMark.getValue();

                getOutput().getColumns().get("assign_title").getData().add(keyTitle.getKey());
                getOutput().getColumns().get("submit_count").getData().add(countSubmissions);
            }


        /*String groupSubmission = (String)this.getParams().getParams().get("group_submission").getValue();
        boolean allowMultipleSubmission = groupSubmission.equals("Single Submission")? false:true;

        Map<Titles, Map<String, Integer>> submittedSolutions = new TreeMap<Titles, Map<String, Integer>>(
                new Comparator<Titles>() {
                    public int compare(Titles p1, Titles p2) {
                        if(p1.getTitle().equals(p2.getTitle()))
                            return p1.getTitle().compareTo(p2.getTitle());
                        else {
                            return p1.getTimestamp().compareTo(p2.getTimestamp());
                        }
                    }
                }
        );

        Map<String, Integer> titleTimestamp = new HashMap<String, Integer>();

        Iterator titleIterator = this.getInput().getColumns().get("assign_title").getData().iterator();
        Iterator timestampIterator = this.getInput().getColumns().get("timestamp").getData().iterator();
        Iterator userIterator = this.getInput().getColumns().get("user").getData().iterator();

         while (titleIterator.hasNext() && timestampIterator.hasNext() && userIterator.hasNext()) {
            String title = (String) titleIterator.next();
            Integer timestamp = (Integer) timestampIterator.next();
            String users = (String) userIterator.next();

            Titles curTitle;
             if(titleTimestamp.containsKey(title)) {
                 if (titleTimestamp.get(title) < timestamp)
                     curTitle = new Titles(title, titleTimestamp.get(title));
                 else {
                     Map<String, Integer> userSubmitted = submittedSolutions.get(new Titles(title, titleTimestamp.get(title)));
                     submittedSolutions.remove(new Titles(title, titleTimestamp.get(title)));

                     titleTimestamp.put(title, timestamp);
                     curTitle = new Titles(title, timestamp);

                     submittedSolutions.put(curTitle, userSubmitted);
                 }
             }
             else {
                 curTitle = new Titles(title, timestamp);
                 titleTimestamp.put(title, timestamp);
             }

             if(allowMultipleSubmission){
                 List<String> groupUsers = Arrays.asList(users.split("\\s*,\\s*"));

                 if (submittedSolutions.containsKey(curTitle)) {
                     Map<String, Integer> userSubmitted = submittedSolutions.get(curTitle);

                     for (String user : groupUsers) {
                         if (userSubmitted.containsKey(user)) {
                             if (userSubmitted.get(user) < timestamp)
                                 userSubmitted.put(user, timestamp);
                         } else {
                             userSubmitted.put(user, timestamp);
                         }
                     }
                 } else {
                     Map<String, Integer> userSubmitted = new HashMap<String, Integer>();
                     for (String user : groupUsers)
                         userSubmitted.put(user, timestamp);

                     submittedSolutions.put(curTitle, userSubmitted);
                 }
             }
             else{
                 if (submittedSolutions.containsKey(curTitle)) {
                     Map<String, Integer> userSubmitted = submittedSolutions.get(curTitle);
                     if (userSubmitted.containsKey(users)) {
                         if (userSubmitted.get(users) < timestamp)
                             userSubmitted.put(users, timestamp);
                     } else {
                         userSubmitted.put(users, timestamp);
                     }
                 } else {
                     Map<String, Integer> userSubmitted = new HashMap<String, Integer>();
                     userSubmitted.put(users, timestamp);
                     submittedSolutions.put(curTitle, userSubmitted);
                 }
             }
         }

        Set<Map.Entry<Titles, Map<String, Integer>>> assignments = submittedSolutions.entrySet();

        for (Map.Entry<Titles, Map<String, Integer>> entry : assignments) {
            getOutput().getColumns().get("assign_title").getData().add(entry.getKey().getTitle());
            getOutput().getColumns().get("submit_count").getData().add(entry.getValue().size());
        }*/
        }
        catch (Exception exc){
            System.out.println("[Exception] SubmittedSolutions: "+exc.getMessage() + " - "+ exc.getStackTrace());
        }
    }

    public Boolean hasPMML() {
        return null;
    }

    public InputStream getPMMLInputStream() {
        return null;
    }
}